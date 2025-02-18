package com.example.ovintocrew.service;

import com.example.ovintocrew.client.GoogleDistanceClient;
import com.example.ovintocrew.client.LocationClient;
import com.example.ovintocrew.model.dto.Employee;
import com.example.ovintocrew.model.dto.EmployeeProximityResponse;
import com.example.ovintocrew.model.dto.LocationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.example.ovintocrew.Util.DistanceUtil.calculateDistance;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    @Value("${location.ovinto.hq.lat}")
    private double hqLat;

    @Value("${location.ovinto.hq.lon}")
    private double hqLon;

    @Value("${employee.threshold.km}")
    private double thresholdKm;

    private final LocationClient locationClient;
    private final GoogleDistanceClient googleDistanceClient;

    public EmployeeProximityResponse getEmployeeProximityResponse(InputStream inputStream) throws Exception {
        List<Employee> employees = parseEmployeesFromXml(inputStream);
        log.info("Parsed {} employees from XML", employees.size());
        return processEmployeesIncrementally(employees);
    }

    public List<Employee> parseEmployeesFromXml(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
        Document document = getDocument(inputStream);
        return extractEmployees(document);
    }

    private static Document getDocument(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(inputStream);
        document.getDocumentElement().normalize();
        return document;
    }

    private static List<Employee> extractEmployees(Document document) {
        NodeList nodeList = document.getElementsByTagName("Employee");
        return IntStream.range(0, nodeList.getLength())
                .mapToObj(nodeList::item)
                .filter(node -> node instanceof Element)
                .map(node -> (Element) node)
                .map(EmployeeService::parseEmployee)
                .collect(Collectors.toList());
    }

    private static Employee parseEmployee(Element employeeElement) {
        String id = employeeElement.getAttribute("id");
        String firstName = getTagValue(employeeElement, "FirstName");
        String lastName = getTagValue(employeeElement, "LastName");
        String email = getTagValue(employeeElement, "Email");
        return Employee.builder()
                .id(id)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .build();
    }

    private static String getTagValue(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);
        return (list.getLength() > 0) ? list.item(0).getTextContent() : "";
    }

    public Employee updateEmployeeLocation(Employee employee) {
        try {
            LocationResponse locationResponse = locationClient.getLocation(employee.id());
            if (locationResponse != null && locationResponse.getCoordinates() != null) {
                double lat = locationResponse.getCoordinates().getLatitude();
                double lon = locationResponse.getCoordinates().getLongitude();
                double distance;
                try {
                    distance = googleDistanceClient.getDistance(hqLat, hqLon, lat, lon);
                    log.debug("Calculated distance using Google API for employee {}: {}", employee.id(), distance);
                } catch (Exception e) {
                    distance = calculateDistance(hqLat, hqLon, lat, lon);
                    log.warn("Google API failed for employee {}. Falling back to Haversine formula. Calculated distance: {}",
                            employee.id(), distance, e);
                }
                return Employee.builder()
                        .id(employee.id())
                        .firstName(employee.firstName())
                        .lastName(employee.lastName())
                        .email(employee.email())
                        .latitude(lat)
                        .longitude(lon)
                        .distanceFromHQ(distance)
                        .build();
            }
        } catch (Exception ex) {
            log.error("Exception fetching location for employee {}: {}", employee.id(), ex.getMessage(), ex);
        }
        return employee;
    }

    public EmployeeProximityResponse processEmployeesIncrementally(List<Employee> employees) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            AtomicReference<Employee> furthestEmployee = new AtomicReference<>(null);
            List<Employee> withinRange = Collections.synchronizedList(new ArrayList<>());

            List<CompletableFuture<Void>> futures = employees.stream()
                    .map(employee -> CompletableFuture
                            .supplyAsync(() -> updateEmployeeLocation(employee), executor)
                            .thenAccept(updatedEmployee -> {
                                if (updatedEmployee.distanceFromHQ() != null &&
                                        updatedEmployee.distanceFromHQ() <= thresholdKm) {
                                    withinRange.add(updatedEmployee);
                                    log.debug("Employee {} is within threshold", updatedEmployee.id());
                                }
                                furthestEmployee.updateAndGet(current -> {
                                    if (current == null ||
                                            (updatedEmployee.distanceFromHQ() != null &&
                                                    (current.distanceFromHQ() == null ||
                                                            updatedEmployee.distanceFromHQ() > current.distanceFromHQ()))) {
                                        log.debug("Employee {} is now the furthest", updatedEmployee.id());
                                        return updatedEmployee;
                                    }
                                    return current;
                                });
                            }))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            log.info("Processed {} employees: {} within range, furthest employee is {}",
                    employees.size(), withinRange.size(), furthestEmployee.get() != null ? furthestEmployee.get().firstName() : "none");

            return EmployeeProximityResponse.builder()
                    .withinRange(withinRange)
                    .furthest(furthestEmployee.get())
                    .build();
        }
    }

}
