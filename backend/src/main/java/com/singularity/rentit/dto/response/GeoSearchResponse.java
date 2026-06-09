package com.singularity.rentit.dto.response;

public record GeoSearchResponse(
        String displayName,
        double latitude,
        double longitude,
        String city,
        String county,
        String country
) {}
