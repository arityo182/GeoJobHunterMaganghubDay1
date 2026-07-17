package com.geojob.controller;

import com.geojob.dto.MapResponseDTO;
import com.geojob.service.JobMapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobMapController {

    private final JobMapService jobMapService;

    public JobMapController(JobMapService jobMapService) {
        this.jobMapService = jobMapService;
    }

    /**
     * GET /api/jobs/map?lat_user=-6.2&lon_user=106.8&radius=10&prodi=Informatika&keyword=Developer&lokasi=Kota+Jakarta&sortBy=asc
     */
    @GetMapping("/map")
    public ResponseEntity<?> getJobsMap(
            @RequestParam("lat_user") Double latUser,
            @RequestParam("lon_user") Double lonUser,
            @RequestParam(value = "radius", defaultValue = "10.0") Double radius,
            @RequestParam(value = "prodi", required = false) String prodi,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "lokasi", required = false) String lokasi,
            @RequestParam(value = "sortBy", defaultValue = "asc") String sortBy) {

        // Validasi input lat/lon
        if (latUser == null || lonUser == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "lat_user dan lon_user wajib diisi");
            return ResponseEntity.badRequest().body(error);
        }
        if (latUser < -90 || latUser > 90 || lonUser < -180 || lonUser > 180) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Koordinat tidak valid. Lat: -90..90, Lon: -180..180");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            MapResponseDTO response = jobMapService.getJobsMap(latUser, lonUser, radius, prodi, keyword, lokasi, sortBy);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Terjadi kesalahan server: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * GET /api/jobs/locations
     * Daftar lokasi unik dari semua perusahaan.
     */
    @GetMapping("/locations")
    public ResponseEntity<List<String>> getLocations() {
        List<String> locations = jobMapService.getAllLocations();
        return ResponseEntity.ok(locations);
    }
}
