package com.example.ovintocrew.model.dto;

import lombok.Builder;
import java.util.List;

@Builder
public record EmployeeProximityResponse(List<Employee> withinRange, Employee furthest) {}
