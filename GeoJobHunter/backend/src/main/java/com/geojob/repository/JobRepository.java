package com.geojob.repository;

import com.geojob.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    /**
     * Cari ID lowongan berdasarkan prodi, keyword, dan lokasi.
     * Gunakan native query untuk menghindari issue Hibernate + PostgreSQL.
     */
    @Query(value = "SELECT j.id FROM job j " +
           "JOIN company c ON c.id = j.company_id " +
           "WHERE (:prodi IS NULL OR LOWER(CAST(j.prodi_syarat AS VARCHAR)) LIKE LOWER(CONCAT('%', :prodi, '%'))) " +
           "AND (:keyword IS NULL OR LOWER(j.nama_lowongan) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:lokasi IS NULL OR LOWER(c.lokasi) = LOWER(:lokasi))",
           nativeQuery = true)
    List<Long> findFilteredJobIds(@Param("prodi") String prodi,
                                    @Param("keyword") String keyword,
                                    @Param("lokasi") String lokasi);

    /**
     * Ambil lowongan + company sekaligus berdasarkan daftar ID.
     */
    @Query("SELECT j FROM Job j JOIN FETCH j.company WHERE j.id IN :ids")
    List<Job> findByIdsWithCompany(@Param("ids") List<Long> ids);

    /**
     * Autocomplete: cari judul lowongan yang mengandung keyword (untuk dropdown).
     * Jika prodi diberikan, filter hanya lowongan yang prodi_syarat-nya mengandung prodi tersebut.
     */
    @Query(value = "SELECT DISTINCT j.nama_lowongan FROM job j " +
           "WHERE LOWER(j.nama_lowongan) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND (:prodi IS NULL OR LOWER(CAST(j.prodi_syarat AS VARCHAR)) LIKE LOWER(CONCAT('%', :prodi, '%'))) " +
           "ORDER BY j.nama_lowongan ASC LIMIT 20",
           nativeQuery = true)
    List<String> findJobTitlesByKeyword(@Param("keyword") String keyword,
                                        @Param("prodi") String prodi);
}
