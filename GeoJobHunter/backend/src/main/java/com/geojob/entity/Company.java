package com.geojob.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "company")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nama_perusahaan", nullable = false)
    private String namaPerusahaan;

    @Column(name = "alamat", length = 2000)
    private String alamat;

    @Column(length = 255)
    private String lokasi;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lon;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Job> jobs = new ArrayList<>();

    public Company() {}

    public Company(String namaPerusahaan, String alamat, String lokasi, Double lat, Double lon) {
        this.namaPerusahaan = namaPerusahaan;
        this.alamat = alamat;
        this.lokasi = lokasi;
        this.lat = lat;
        this.lon = lon;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNamaPerusahaan() { return namaPerusahaan; }
    public void setNamaPerusahaan(String namaPerusahaan) { this.namaPerusahaan = namaPerusahaan; }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    public String getLokasi() { return lokasi; }
    public void setLokasi(String lokasi) { this.lokasi = lokasi; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLon() { return lon; }
    public void setLon(Double lon) { this.lon = lon; }

    public List<Job> getJobs() { return jobs; }
    public void setJobs(List<Job> jobs) { this.jobs = jobs; }
}
