package com.example.ovintocrew.controller;

import com.example.ovintocrew.model.dto.Employee;
import com.example.ovintocrew.model.dto.EmployeeProximityResponse;
import com.example.ovintocrew.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employee Controller", description = "Operations for processing employee XML data")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Operation(
            summary = "Process employees from file",
            description = "Parses an XML file provided as a multipart upload, processes the employees, and returns proximity information."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employees processed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EmployeeProximityResponse.class))),
            @ApiResponse(responseCode = "400", description = "Error processing the file", content = @Content),
            @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content)
    })
    @PostMapping(value = "/process/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmployeeProximityResponse> processEmployeesFromFile(
            @Parameter(description = "XML file containing employee data", required = true)
            @RequestParam("file") MultipartFile file) throws Exception {

        try (InputStream inputStream = file.getInputStream()) {
            EmployeeProximityResponse result = employeeService.getEmployeeProximityResponse(inputStream);
            return ResponseEntity.ok(result);
        }
    }

    @Operation(
            summary = "Process employees from XML body",
            description = "Parses XML content provided in the request body, processes the employees, and returns proximity information."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employees processed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EmployeeProximityResponse.class))),
            @ApiResponse(responseCode = "400", description = "Error processing the XML content", content = @Content),
            @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content)
    })
    @PostMapping(value = "/process", consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<EmployeeProximityResponse> processEmployeesFromBody(
            @Parameter(description = "XML content containing employee data", required = true)
            @RequestBody String xmlContent) throws Exception {

        try (InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8))) {
            EmployeeProximityResponse result = employeeService.getEmployeeProximityResponse(inputStream);
            return ResponseEntity.ok(result);
        }
    }
}
