-- GeoJob Hunter - Database Schema
-- PostgreSQL version

CREATE TABLE company (
    id BIGSERIAL PRIMARY KEY,
    nama_perusahaan VARCHAR(255) NOT NULL,
    alamat TEXT,
    lokasi VARCHAR(255),
    lat DOUBLE PRECISION NOT NULL,
    lon DOUBLE PRECISION NOT NULL
);

CREATE TABLE job (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    nama_lowongan VARCHAR(255) NOT NULL,
    prodi_syarat TEXT,
    kuota INTEGER DEFAULT 1,
    link_lamar VARCHAR(500),
    FOREIGN KEY (company_id) REFERENCES company(id) ON DELETE CASCADE
);

CREATE INDEX idx_job_company ON job(company_id);
CREATE INDEX idx_company_coord ON company(lat, lon);
