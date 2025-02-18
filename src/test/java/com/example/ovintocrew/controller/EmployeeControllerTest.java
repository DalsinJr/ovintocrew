package com.example.ovintocrew.controller;

import com.example.ovintocrew.model.dto.Employee;
import com.example.ovintocrew.model.dto.EmployeeProximityResponse;
import com.example.ovintocrew.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@DisplayName("EmployeeController Tests")
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;


    @DisplayName("Should process employees from multipart file and return JSON response")
    @Test
    public void testProcessEmployeesFromFile() throws Exception {
        String xmlContent = getXmlDummyContent();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "employees.xml",
                MediaType.APPLICATION_XML_VALUE,
                xmlContent.getBytes(StandardCharsets.UTF_8)
        );
        Employee employee = buildDummyEmployee();
        EmployeeProximityResponse dummyResponse = buildDummyResponse(employee);

        Mockito.when(employeeService.parseEmployeesFromXml(any(InputStream.class)))
                .thenReturn(List.of(employee));
        Mockito.when(employeeService.processEmployeesIncrementally(any(List.class)))
                .thenReturn(dummyResponse);

        mockMvc.perform(multipart("/api/employees/process/file")
                        .file(file))
                .andExpect(status().isOk());
    }

    private static Employee buildDummyEmployee() {
        return Employee.builder()
                .id("1")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();
    }

    @DisplayName("Should process employees from XML body and return JSON response")
    @Test
    public void testProcessEmployeesFromBody() throws Exception {
        String xmlContent = getXmlDummyContent();
        Employee employee = buildDummyEmployee();

        EmployeeProximityResponse dummyResponse = buildDummyResponse(employee);

        Mockito.when(employeeService.parseEmployeesFromXml(any(InputStream.class)))
                .thenReturn(List.of(employee));
        Mockito.when(employeeService.processEmployeesIncrementally(any(List.class)))
                .thenReturn(dummyResponse);

        mockMvc.perform(post("/api/employees/process")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(xmlContent))
                .andExpect(status().isOk());
    }

    private static EmployeeProximityResponse buildDummyResponse(Employee employee) {
        return EmployeeProximityResponse.builder()
                .withinRange(List.of(employee))
                .furthest(employee)
                .build();
    }

    private static String getXmlDummyContent() {
        return """
                <?xml version="1.0"?>
                <Company name="Ovinto">
                  <Employee id="1">
                    <FirstName>John</FirstName>
                    <LastName>Doe</LastName>
                    <Email>john.doe@example.com</Email>
                  </Employee>
                </Company>
                """;
    }
}
