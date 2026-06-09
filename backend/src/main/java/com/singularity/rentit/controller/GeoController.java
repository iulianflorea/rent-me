package com.singularity.rentit.controller;

import com.singularity.rentit.dto.response.GeoSearchResponse;
import com.singularity.rentit.service.GeoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/geo")
@RequiredArgsConstructor
public class GeoController {

    private final GeoService geoService;

    @GetMapping("/search")
    public ResponseEntity<List<GeoSearchResponse>> search(@RequestParam String q) {
        return ResponseEntity.ok(geoService.search(q));
    }
}
