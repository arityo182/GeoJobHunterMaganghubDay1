package com.geojob.dto;

public class JobDTO {
    private Long id;
    private String namaLowongan;
    private String prodiSyarat;
    private Integer kuota;
    private String linkLamar;
    private String badge;
    private String tips;

    public JobDTO() {}

    public JobDTO(Long id, String namaLowongan, String prodiSyarat, Integer kuota, String linkLamar) {
        this.id = id;
        this.namaLowongan = namaLowongan;
        this.prodiSyarat = prodiSyarat;
        this.kuota = kuota;
        this.linkLamar = linkLamar;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNamaLowongan() { return namaLowongan; }
    public void setNamaLowongan(String namaLowongan) { this.namaLowongan = namaLowongan; }

    public String getProdiSyarat() { return prodiSyarat; }
    public void setProdiSyarat(String prodiSyarat) { this.prodiSyarat = prodiSyarat; }

    public Integer getKuota() { return kuota; }
    public void setKuota(Integer kuota) { this.kuota = kuota; }

    public String getLinkLamar() { return linkLamar; }
    public void setLinkLamar(String linkLamar) { this.linkLamar = linkLamar; }

    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }

    public String getTips() { return tips; }
    public void setTips(String tips) { this.tips = tips; }
}
