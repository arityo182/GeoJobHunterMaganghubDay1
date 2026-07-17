package com.geojob.seeder;

import com.geojob.entity.Company;
import com.geojob.entity.Job;
import com.geojob.repository.CompanyRepository;
import com.geojob.repository.JobRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CompanyRepository companyRepo;
    private final JobRepository jobRepo;

    public DataSeeder(CompanyRepository companyRepo, JobRepository jobRepo) {
        this.companyRepo = companyRepo;
        this.jobRepo = jobRepo;
    }

    @Override
    public void run(String... args) {
        // ── Only seed if database is empty ──────────────────────────
        if (companyRepo.count() > 0) {
            System.out.println("[Seeder] ⏭ Database already has data, skipping seed.");
            return;
        }

        System.out.println("[Seeder] 🌱 Seeding data...");

        // ── Companies in Jakarta ────────────────────────────────────
        seed("PT Teknologi Nusantara",
             "Jl. MH Thamrin No.1, Menteng, Jakarta Pusat",
             "Jakarta Pusat", -6.1917, 106.8401,
             "Backend Developer", "Teknik Informatika, Ilmu Komputer", 3,
             "https://example.com/apply/backend-dev",
             "Frontend Developer", "Teknik Informatika, Sistem Informasi", 2,
             "https://example.com/apply/frontend-dev",
             "UI/UX Designer", "Desain Komunikasi Visual, Teknik Informatika", 2,
             "https://example.com/apply/ui-ux");

        seed("PT Bank Digital Indonesia",
             "Jl. Sudirman Kav. 45, Senayan, Jakarta Selatan",
             "Jakarta Selatan", -6.2274, 106.8035,
             "Full Stack Developer", "Teknik Informatika, Ilmu Komputer", 5,
             "https://example.com/apply/fs-dev",
             "Data Analyst", "Statistika, Matematika, Teknik Informatika", 3,
             "https://example.com/apply/data-analyst",
             "Mobile Developer", "Teknik Informatika, Ilmu Komputer", 2,
             "https://example.com/apply/mobile-dev");

        seed("PT Ecommerce Maju Bersama",
             "Jl. Gatot Subroto No.28, Kuningan, Jakarta Selatan",
             "Jakarta Selatan", -6.2382, 106.8309,
             "DevOps Engineer", "Teknik Informatika, Ilmu Komputer", 2,
             "https://example.com/apply/devops",
             "Product Manager", "Teknik Industri, Manajemen, Sistem Informasi", 1,
             "https://example.com/apply/pm",
             "QA Engineer", "Teknik Informatika, Sistem Informasi", 3,
             "https://example.com/apply/qa");

        seed("PT Startup Edukasi Kreatif",
             "Jl. Kemang Raya No.12, Mampang, Jakarta Selatan",
             "Jakarta Selatan", -6.2580, 106.8190,
             "React Native Developer", "Teknik Informatika, Ilmu Komputer", 2,
             "https://example.com/apply/rn-dev",
             "Content Writer", "Sastra, Ilmu Komunikasi, Pendidikan", 2,
             "https://example.com/apply/content-writer",
             "Graphic Designer", "Desain Komunikasi Visual, Seni Rupa", 1,
             "https://example.com/apply/gd");

        seed("PT Logistik Pintar Indonesia",
             "Jl. Yos Sudarso No.15, Kelapa Gading, Jakarta Utara",
             "Jakarta Utara", -6.1580, 106.9060,
             "Data Engineer", "Teknik Informatika, Ilmu Komputer, Matematika", 3,
             "https://example.com/apply/de",
             "Backend Developer (Go)", "Teknik Informatika, Ilmu Komputer", 2,
             "https://example.com/apply/go-dev",
             "System Analyst", "Sistem Informasi, Teknik Informatika", 2,
             "https://example.com/apply/sa");

        seed("PT Fintech Syariah Nusantara",
             "Jl. TB Simatupang No.5, Cilandak, Jakarta Selatan",
             "Jakarta Selatan", -6.2880, 106.7965,
             "Java Developer", "Teknik Informatika, Ilmu Komputer", 3,
             "https://example.com/apply/java-dev",
             "Cyber Security Analyst", "Teknik Informatika, Ilmu Komputer", 2,
             "https://example.com/apply/csa",
             "Database Administrator", "Teknik Informatika, Sistem Informasi", 1,
             "https://example.com/apply/dba");

        seed("PT Media Digital Terdepan",
             "Jl. Asia Afrika No.8, Gelora Bung Karno, Jakarta Pusat",
             "Jakarta Pusat", -6.2182, 106.8015,
             "Digital Marketing", "Ilmu Komunikasi, Manajemen, Ekonomi", 3,
             "https://example.com/apply/dm",
             "SEO Specialist", "Ilmu Komputer, Teknik Informatika", 2,
             "https://example.com/apply/seo",
             "Social Media Specialist", "Ilmu Komunikasi, Desain Komunikasi Visual", 2,
             "https://example.com/apply/sms");

        seed("PT Kesehatan Digital Indonesia",
             "Jl. Rasuna Said Kav. 18, Kuningan, Jakarta Selatan",
             "Jakarta Selatan", -6.2310, 106.8380,
             "AI Engineer", "Teknik Informatika, Ilmu Komputer, Matematika", 2,
             "https://example.com/apply/ai",
             "iOS Developer", "Teknik Informatika, Ilmu Komputer", 1,
             "https://example.com/apply/ios",
             "Technical Writer", "Sastra, Ilmu Komputer, Teknik Informatika", 1,
             "https://example.com/apply/tw");

        seed("PT Properti Modern Tbk",
             "Jl. Thamrin Boulevard No.55, Kelapa Gading, Jakarta Utara",
             "Jakarta Utara", -6.1510, 106.8990,
             "React Developer", "Teknik Informatika, Ilmu Komputer", 3,
             "https://example.com/apply/react-dev",
             "Node.js Developer", "Teknik Informatika, Ilmu Komputer", 2,
             "https://example.com/apply/node-dev",
             "IT Support Engineer", "Teknik Informatika, Sistem Informasi", 3,
             "https://example.com/apply/it-support");

        seed("PT Konsultan Bisnis Global",
             "Jl. Sudirman Central Business District (SCBD) No.1",
             "Jakarta Selatan", -6.2250, 106.8070,
             "SAP Consultant", "Akuntansi, Sistem Informasi, Teknik Industri", 2,
             "https://example.com/apply/sap",
             "Business Analyst", "Sistem Informasi, Manajemen, Teknik Industri", 3,
             "https://example.com/apply/ba",
             "Project Manager IT", "Teknik Informatika, Manajemen, Sistem Informasi", 1,
             "https://example.com/apply/pm-it");

        System.out.println("[Seeder] ✅ Data seeding completed!");
    }

    /**
     * Seed one company with up to 3 job positions.
     */
    private void seed(String companyName, String alamat, String lokasi,
                      Double lat, Double lon,
                      String job1, String prodi1, Integer kuota1, String link1,
                      String job2, String prodi2, Integer kuota2, String link2,
                      String job3, String prodi3, Integer kuota3, String link3) {

        Company company = new Company(companyName, alamat, lokasi, lat, lon);

        Job j1 = new Job(job1, prodi1, kuota1, link1);
        Job j2 = new Job(job2, prodi2, kuota2, link2);
        Job j3 = new Job(job3, prodi3, kuota3, link3);

        j1.setCompany(company);
        j2.setCompany(company);
        j3.setCompany(company);

        company.getJobs().add(j1);
        company.getJobs().add(j2);
        company.getJobs().add(j3);

        companyRepo.save(company);
        System.out.println("[Seeder] ✅ " + companyName + " (" + lokasi + ") — 3 lowongan");
    }
}
