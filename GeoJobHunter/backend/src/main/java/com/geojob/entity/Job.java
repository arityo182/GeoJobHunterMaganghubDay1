package com.geojob.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "job")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @JsonBackReference
    private Company company;

    @Column(name = "nama_lowongan", nullable = false)
    private String namaLowongan;

    @Column(name = "prodi_syarat", length = 2000)
    private String prodiSyarat;

    @Column(nullable = false)
    private Integer kuota = 1;

    @Column(name = "link_lamar", length = 500)
    private String linkLamar;

    public Job() {}

    public Job(String namaLowongan, String prodiSyarat, Integer kuota, String linkLamar) {
        this.namaLowongan = namaLowongan;
        this.prodiSyarat = prodiSyarat;
        this.kuota = kuota;
        this.linkLamar = linkLamar;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public String getNamaLowongan() { return namaLowongan; }
    public void setNamaLowongan(String namaLowongan) { this.namaLowongan = namaLowongan; }

    public String getProdiSyarat() { return prodiSyarat; }
    public void setProdiSyarat(String prodiSyarat) { this.prodiSyarat = prodiSyarat; }

    public Integer getKuota() { return kuota; }
    public void setKuota(Integer kuota) { this.kuota = kuota; }

    public String getLinkLamar() { return linkLamar; }
    public void setLinkLamar(String linkLamar) { this.linkLamar = linkLamar; }
}
