package com.geojob.service;

import com.geojob.dto.CompanyJobDTO;
import com.geojob.dto.JobDTO;
import com.geojob.dto.MapResponseDTO;
import com.geojob.entity.Job;
import com.geojob.repository.CompanyRepository;
import com.geojob.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobMapService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;

    public JobMapService(JobRepository jobRepository, CompanyRepository companyRepository) {
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
    }

    /**
     * Haversine formula untuk menghitung jarak (km) antara 2 koordinat.
     */
    public double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Menentukan warna pin berdasarkan kombinasi kuota & jumlah prodi.
     * - Hijau: kuota banyak (>=5) ATAU prodi syarat sedikit (<=2) → peluang bagus
     * - Merah: kuota sedikit (<3) DAN prodi syarat banyak (>3) → sulit
     * - Orange: sisanya
     */
    private String determineWarnaPin(int totalKuota, int maxProdiCount) {
        boolean banyakKuota = totalKuota >= 5;
        boolean sedikitProdi = maxProdiCount <= 2;
        boolean sedikitKuota = totalKuota < 3;
        boolean banyakProdi = maxProdiCount > 3;

        if (banyakKuota || sedikitProdi) return "green";
        if (sedikitKuota && banyakProdi) return "red";
        return "orange";
    }

    /**
     * Menentukan badge & tips berdasarkan kuota & jumlah prodi syarat.
     * - Peluang Emas: kuota >= 5 ATAU prodi <= 2
     * - Persaingan Ketat: kuota <= 2 DAN prodi > 3
     * - Standar: sisanya
     */
    private String[] determineBadge(int kuota, int jumlahProdi) {
        boolean peluangBagus = kuota >= 5 || jumlahProdi <= 2;
        boolean sulit = kuota <= 2 && jumlahProdi > 3;

        if (peluangBagus) {
            return new String[]{"🎯 Peluang Emas!", "Lowongan besar & syarat spesifik. Segera lamar!"};
        } else if (sulit) {
            return new String[]{"🔥 Persaingan Ketat", "Kuota terbatas & banyak jurusan bersaing. Siapkan CV terbaik!"};
        } else {
            return new String[]{"⚖️ Standar", "Peluang cukup terbuka. Siapkan portofolio terbaikmu!"};
        }
    }

    /**
     * Endpoint utama: filter, grouping, badge, sort.
     */
    public MapResponseDTO getJobsMap(Double latUser, Double lonUser, Double radius,
                                      String prodi, String keyword, String lokasi, String sortBy) {
        // Validasi: default radius 10 km jika null
        if (radius == null) radius = 10.0;
        if (sortBy == null) sortBy = "asc";

        // 1. Ambil data dari DB (filter prodi & keyword via query)
        // Step 1: Dapatkan ID yang cocok dengan filter
        String lokasiParam = (lokasi != null && !lokasi.isBlank()) ? lokasi : null;
        List<Long> jobIds = jobRepository.findFilteredJobIds(prodi, keyword, lokasiParam);
        if (jobIds.isEmpty()) {
            return new MapResponseDTO(0, 0, List.of());
        }
        // Step 2: Ambil entity lengkap dengan company
        List<Job> jobs = jobRepository.findByIdsWithCompany(jobIds);

        // 2. Filter berdasarkan radius (Haversine)
        //    Kelompokkan per company sambil hitung jarak & total kuota
        Map<Long, CompanyJobDTO> companyMap = new LinkedHashMap<>();

        for (Job job : jobs) {
            var company = job.getCompany();
            double jarak = haversine(latUser, lonUser, company.getLat(), company.getLon());

            if (jarak > radius) continue; // Lewati jika di luar radius

            Long companyId = company.getId();

            CompanyJobDTO companyDTO = companyMap.get(companyId);
            if (companyDTO == null) {
                companyDTO = new CompanyJobDTO();
                companyDTO.setCompanyId(companyId);
                companyDTO.setNamaPerusahaan(company.getNamaPerusahaan());
                companyDTO.setAlamat(company.getAlamat());
                companyDTO.setLokasi(company.getLokasi());
                companyDTO.setLat(company.getLat());
                companyDTO.setLon(company.getLon());
                companyDTO.setTotalKuota(0);
                companyDTO.setJarakKm(jarak);
                companyDTO.setLowongan(new ArrayList<>());
                companyMap.put(companyId, companyDTO);
            }

            // Buat DTO job + badge
            JobDTO jobDTO = new JobDTO(
                job.getId(),
                job.getNamaLowongan(),
                job.getProdiSyarat(),
                job.getKuota(),
                job.getLinkLamar()
            );

            int jumlahProdi = (job.getProdiSyarat() != null && !job.getProdiSyarat().isBlank())
                    ? job.getProdiSyarat().split(",").length
                    : 0;
            String[] badgeInfo = determineBadge(job.getKuota(), jumlahProdi);
            jobDTO.setBadge(badgeInfo[0]);
            jobDTO.setTips(badgeInfo[1]);

            // Track juga jumlah prodi tertinggi per perusahaan (untuk warna pin)
            if (jumlahProdi > companyDTO.getMaxProdiCount()) {
                companyDTO.setMaxProdiCount(jumlahProdi);
            }

            companyDTO.getLowongan().add(jobDTO);
            companyDTO.setTotalKuota(companyDTO.getTotalKuota() + job.getKuota());
        }

        // 3. Hitung warna_pin untuk setiap company (kombinasi kuota & prodi)
        for (CompanyJobDTO c : companyMap.values()) {
            c.setWarnaPin(determineWarnaPin(c.getTotalKuota(), c.getMaxProdiCount()));
        }

        // 4. Sorting
        List<CompanyJobDTO> companyList = new ArrayList<>(companyMap.values());
        if ("desc".equalsIgnoreCase(sortBy)) {
            companyList.sort((a, b) -> Double.compare(b.getJarakKm(), a.getJarakKm()));
        } else {
            companyList.sort((a, b) -> Double.compare(a.getJarakKm(), b.getJarakKm()));
        }

        // 5. Buat response
        int totalLowongan = companyList.stream()
                .mapToInt(c -> c.getLowongan().size())
                .sum();

        return new MapResponseDTO(totalLowongan, companyList.size(), companyList);
    }

    /**
     * Autocomplete: cari judul lowongan, dengan filter prodi jika diberikan.
     */
    public List<String> searchJobTitles(String keyword, String prodi) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        // Jika prodi blank/null, kirim null agar query tidak memfilter prodi
        String prodiParam = (prodi != null && !prodi.isBlank()) ? prodi : null;
        return jobRepository.findJobTitlesByKeyword(keyword, prodiParam);
    }

    /**
     * Dapatkan daftar lokasi unik dari semua perusahaan.
     */
    public List<String> getAllLocations() {
        return companyRepository.findAllLocations();
    }
}
