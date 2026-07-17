package com.geojob.repository;

import com.geojob.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByNamaPerusahaanAndAlamat(String namaPerusahaan, String alamat);

    /**
     * Ambil daftar lokasi unik, urut alphabet.
     */
    @Query("SELECT DISTINCT c.lokasi FROM Company c WHERE c.lokasi IS NOT NULL AND c.lokasi != '' ORDER BY c.lokasi ASC")
    List<String> findAllLocations();
}
