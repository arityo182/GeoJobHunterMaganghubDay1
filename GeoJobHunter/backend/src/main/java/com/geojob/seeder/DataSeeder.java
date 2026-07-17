package com.geojob.seeder;

import com.geojob.entity.Company;
import com.geojob.entity.Job;
import com.geojob.repository.CompanyRepository;
import com.geojob.repository.JobRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.util.*;
import java.util.zip.GZIPInputStream;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CompanyRepository companyRepo;
    private final JobRepository jobRepo;

    public DataSeeder(CompanyRepository companyRepo, JobRepository jobRepo) {
        this.companyRepo = companyRepo;
        this.jobRepo = jobRepo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // ── Jika data lama (Jakarta-only, < 50 company), hapus & seed ulang ──
        long existingCount = companyRepo.count();
        if (existingCount > 0 && existingCount < 50) {
            System.out.println("[Seeder] 🗑️ Data lama terdeteksi (" + existingCount + " company), menghapus untuk seed ulang...");
            jobRepo.deleteAll();
            companyRepo.deleteAll();
        } else if (existingCount >= 50) {
            System.out.println("[Seeder] ⏭ Database sudah memiliki " + existingCount + " company, skip seed.");
            return;
        }

        System.out.println("[Seeder] 🌱 Memulai seeding dari CSV...");

        try (var gzip = new GZIPInputStream(new ClassPathResource("lowongan.csv.gz").getInputStream());
             var reader = new InputStreamReader(gzip);
             var csv = new CSVReader(reader)) {

            // Skip header
            csv.readNext();

            // ── Group by company key: nama + lat + lon ──────────────
            Map<String, List<String[]>> companyMap = new LinkedHashMap<>();
            String[] row;
            int totalRows = 0;

            while ((row = csv.readNext()) != null) {
                if (row.length < 12) continue;
                String namaPerusahaan = row[1].trim();
                String latStr = row[9].trim();
                String lonStr = row[10].trim();
                if (namaPerusahaan.isEmpty() || latStr.isEmpty() || lonStr.isEmpty()) continue;

                String key = namaPerusahaan + "|" + latStr + "|" + lonStr;
                companyMap.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
                totalRows++;
            }

            System.out.println("[Seeder] 📊 Total baris CSV: " + totalRows);
            System.out.println("[Seeder] 🏢 Total perusahaan unik: " + companyMap.size());

            // ── Seed each company with its jobs ─────────────────────
            int companyCount = 0;
            int jobCount = 0;

            for (Map.Entry<String, List<String[]>> entry : companyMap.entrySet()) {
                List<String[]> rows = entry.getValue();
                String[] firstRow = rows.get(0);

                String namaPerusahaan = firstRow[1].trim();
                String lokasi = firstRow[2].trim();
                String alamat = firstRow[3].trim();
                double lat = Double.parseDouble(firstRow[9].trim());
                double lon = Double.parseDouble(firstRow[10].trim());

                Company company = new Company(namaPerusahaan, alamat, lokasi, lat, lon);

                for (String[] r : rows) {
                    String judulLowongan = r[0].trim();
                    String kuotaStr = r[4].trim();
                    String persyaratan = r.length > 8 ? r[8].trim() : "";
                    String link = r.length > 11 ? r[11].trim() : "";

                    // Parse kuota (default 1 if invalid)
                    int kuota = 1;
                    try { kuota = Integer.parseInt(kuotaStr); } catch (NumberFormatException ignored) {}

                    // Extract program studi dari persyaratan
                    String prodiSyarat = extractProdi(persyaratan);

                    Job job = new Job(judulLowongan, prodiSyarat, kuota, link);
                    job.setCompany(company);
                    company.getJobs().add(job);
                    jobCount++;
                }

                companyRepo.save(company);
                companyCount++;

                if (companyCount % 50 == 0) {
                    System.out.println("[Seeder] ✅ " + companyCount + " perusahaan ter-seed (" + jobCount + " lowongan)");
                }
            }

            System.out.println("[Seeder] ✅ Selesai! " + companyCount + " perusahaan, " + jobCount + " lowongan");

        } catch (Exception e) {
            System.err.println("[Seeder] ❌ Gagal seeding: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Extract program studi dari field persyaratan.
     * Format: "Tingkat pendidikan: ... Program studi: Manajemen, Administrasi, ..."
     */
    private String extractProdi(String persyaratan) {
        if (persyaratan == null || persyaratan.isBlank()) return "Umum";

        // Cari "Program studi:" atau "Program Studi:"
        int idx = persyaratan.toLowerCase().indexOf("program studi");
        if (idx == -1) {
            // Fallback: cari "Prodi:" 
            idx = persyaratan.toLowerCase().indexOf("prodi:");
            if (idx == -1) return "Umum";
        }

        // Cari titik dua setelah "Program studi"
        int colonIdx = persyaratan.indexOf(':', idx);
        if (colonIdx == -1) return "Umum";

        // Ambil teks setelah titik dua
        String after = persyaratan.substring(colonIdx + 1).trim();

        // Ambil sampai titik, baris baru, atau akhir string
        int endIdx = after.length();
        int dotIdx = after.indexOf('.');
        int newlineIdx = after.indexOf('\n');
        if (dotIdx > 0) endIdx = Math.min(endIdx, dotIdx);
        if (newlineIdx > 0) endIdx = Math.min(endIdx, newlineIdx);

        String prodi = after.substring(0, endIdx).trim();

        // Bersihkan dari karakter noise
        prodi = prodi.replaceAll("[\\r\\n]+", ", ").trim();
        if (prodi.isEmpty() || prodi.equals(".")) return "Umum";

        return prodi;
    }
}
