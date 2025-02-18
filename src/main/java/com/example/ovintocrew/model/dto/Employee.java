package com.example.ovintocrew.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
public record Employee(
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String id,
        String firstName,
        String lastName,
        String email,
        Double latitude,
        Double longitude,
        Double distanceFromHQ
) {}