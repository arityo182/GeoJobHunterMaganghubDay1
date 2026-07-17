package com.geojob.dto;

import java.util.List;

public class MapResponseDTO {
    private Integer totalLowongan;
    private Integer totalPerusahaan;
    private List<CompanyJobDTO> perusahaan;

    public MapResponseDTO() {}

    public MapResponseDTO(Integer totalLowongan, Integer totalPerusahaan, List<CompanyJobDTO> perusahaan) {
        this.totalLowongan = totalLowongan;
        this.totalPerusahaan = totalPerusahaan;
        this.perusahaan = perusahaan;
    }

    public Integer getTotalLowongan() { return totalLowongan; }
    public void setTotalLowongan(Integer totalLowongan) { this.totalLowongan = totalLowongan; }

    public Integer getTotalPerusahaan() { return totalPerusahaan; }
    public void setTotalPerusahaan(Integer totalPerusahaan) { this.totalPerusahaan = totalPerusahaan; }

    public List<CompanyJobDTO> getPerusahaan() { return perusahaan; }
    public void setPerusahaan(List<CompanyJobDTO> perusahaan) { this.perusahaan = perusahaan; }
}
