package com.geojob.dto;

import java.util.List;

public class CompanyJobDTO {
    private Long companyId;
    private String namaPerusahaan;
    private String alamat;
    private String lokasi;
    private Double lat;
    private Double lon;
    private Integer totalKuota;
    private Integer maxProdiCount = 0; // jumlah prodi terbanyak di antara lowongan perusahaan ini
    private String warnaPin;
    private Double jarakKm;
    private List<JobDTO> lowongan;

    public CompanyJobDTO() {}

    public CompanyJobDTO(Long companyId, String namaPerusahaan, String alamat, String lokasi, Double lat, Double lon,
                          Integer totalKuota, String warnaPin, Double jarakKm, List<JobDTO> lowongan) {
        this.companyId = companyId;
        this.namaPerusahaan = namaPerusahaan;
        this.alamat = alamat;
        this.lat = lat;
        this.lon = lon;
        this.totalKuota = totalKuota;
        this.warnaPin = warnaPin;
        this.jarakKm = jarakKm;
        this.lowongan = lowongan;
    }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

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

    public Integer getTotalKuota() { return totalKuota; }
    public void setTotalKuota(Integer totalKuota) { this.totalKuota = totalKuota; }

    public Integer getMaxProdiCount() { return maxProdiCount; }
    public void setMaxProdiCount(Integer maxProdiCount) { this.maxProdiCount = maxProdiCount; }

    public String getWarnaPin() { return warnaPin; }
    public void setWarnaPin(String warnaPin) { this.warnaPin = warnaPin; }

    public Double getJarakKm() { return jarakKm; }
    public void setJarakKm(Double jarakKm) { this.jarakKm = jarakKm; }

    public List<JobDTO> getLowongan() { return lowongan; }
    public void setLowongan(List<JobDTO> lowongan) { this.lowongan = lowongan; }
}
