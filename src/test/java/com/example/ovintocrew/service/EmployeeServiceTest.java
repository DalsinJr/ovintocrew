package com.example.ovintocrew.service;

import com.example.ovintocrew.client.LocationClient;
import com.example.ovintocrew.model.dto.Coordinates;
import com.example.ovintocrew.model.dto.Employee;
import com.example.ovintocrew.model.dto.EmployeeProximityResponse;
import com.example.ovintocrew.model.dto.LocationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private LocationClient locationClient;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    @DisplayName("Should correctly parse employees from XML")
    public void testParseEmployeesFromXml() throws Exception {
        String xml = mockXml();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        List<Employee> employees = employeeService.parseEmployeesFromXml(inputStream);
        assertNotNull(employees);
        assertEquals(2, employees.size());
        assertEquals("1", employees.getFirst().id());
        assertEquals("John", employees.getFirst().firstName());
        assertEquals("Doe", employees.getFirst().lastName());
        assertEquals("john.doe@example.com", employees.getFirst().email());
    }

    @Test
    @DisplayName("Should process employees incrementally and identify the furthest employee")
    public void testProcessEmployeesIncrementally() throws Exception {
        Employee e1 = getMockedEmployee("1");
        Employee e2 = getMockedEmployee("2");
        List<Employee> employees = List.of(e1, e2);

        LocationResponse coords1 = new LocationResponse("1",new Coordinates(51.1056, 3.4312));
        LocationResponse coords2 = new LocationResponse("2",new Coordinates(99.9999, 3.4312));

        when(locationClient.getLocation("1")).thenReturn(coords1);
        when(locationClient.getLocation("2")).thenReturn(coords2);

        EmployeeProximityResponse response = employeeService.processEmployeesIncrementally(employees);
        assertNotNull(response);

        List<Employee> withinRange = response.withinRange();
        assertNotNull(withinRange);
        Employee furthest = response.furthest();
        assertNotNull(furthest);
        assertEquals("2", furthest.id(), "Employee 2 should be the furthest");
    }

    private static Employee getMockedEmployee(String id) {
        return Employee.builder()
                .id(id)
                .build();
    }

    private static String mockXml() {
        return """
                <?xml version="1.0"?>
                <Company name="Ovinto">
                    <Employee id="1">
                        <FirstName>John</FirstName>
                        <LastName>Doe</LastName>
                        <Email>john.doe@example.com</Email>
                    </Employee>
                    <Employee id="2">
                        <FirstName>Jane</FirstName>
                        <LastName>Smith</LastName>
                        <Email>jane.smith@example.com</Email>
                    </Employee>
                </Company>
                """;
    }
}
