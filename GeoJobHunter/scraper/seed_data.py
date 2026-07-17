"""
GeoJob Hunter - Data Seeder from CSV
Script untuk mengisi database dari file CSV lowongan_final_v3_filled.csv
Program studi dipisah & dibersihkan dari kolom persyaratan agar filtering web optimal.
"""

import csv
import os
import re
import psycopg2

# ======================= KONFIGURASI DATABASE =======================
DB_CONFIG = {
    "host": os.getenv("DB_HOST", "localhost"),
    "port": os.getenv("DB_PORT", "5433"),
    "user": os.getenv("DB_USER", "core"),
    "password": os.getenv("DB_PASSWORD", "arie12345"),
    "dbname": os.getenv("DB_NAME", "jobhun"),
}

# Path ke CSV (relative dari script ini) — yang sudah ada link
CSV_PATH = os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))),
                        "lowongan_final_v3_with_link.csv")


def clean_prodi_name(name: str) -> str:
    """Bersihkan satu nama prodi: strip, hapus trailing dot, title-case."""
    name = name.strip().rstrip('.')
    name = name.strip()
    # Hapus tanda kutip berlebih
    name = name.strip('"\'')
    name = name.strip()
    # Skip jika kosong setelah dibersihkan
    if not name:
        return ""
    # Title case: huruf pertama kapital (kecuali kata depan tertentu)
    words = name.split()
    exceptions = {"dan", "atau", "di", "ke", "dari", "untuk", "yang", "dengan", "pada"}
    result_words = []
    for i, w in enumerate(words):
        if i == 0 or w.lower() not in exceptions:
            result_words.append(w[0].upper() + w[1:].lower() if len(w) > 1 else w.upper())
        else:
            result_words.append(w.lower())
    return " ".join(result_words)


def parse_prodi(prodi_text: str) -> str:
    """
    Parse program studi dari teks persyaratan.
    Hasil akhir berupa string comma-separated yang bersih.
    """
    if not prodi_text or not prodi_text.strip():
        return ""

    text = prodi_text.strip()

    # Strategi 1: Cari "Program studi:" (paling umum)
    patterns = [
        r'Program\s+studi\s*:\s*(.+?)(?:\.\s*Tingkat|\Z|\.\s*$)',
        r'Program\s+studi\s*:\s*(.+?)(?:\.\s*\w|\Z)',
        r'Program\s+studi\s*:\s*(.+)',
    ]

    prodi_str = None
    for pat in patterns:
        match = re.search(pat, text, re.IGNORECASE | re.DOTALL)
        if match:
            candidate = match.group(1).strip().rstrip('.')
            # Validasi: pastikan tidak terlalu panjang (bukan paragraf)
            if len(candidate) > 5 and len(candidate) < 500:
                prodi_str = candidate
                break

    # Strategi 2: Cari setelah "Tingkat pendidikan:" jika strategi 1 gagal
    if not prodi_str:
        tp_match = re.search(r'Tingkat\s+pendidikan\s*:\s*(.+?)(?:\.\s*\w|\Z)', text, re.IGNORECASE | re.DOTALL)
        if tp_match:
            # Ambil teks setelah "Tingkat pendidikan:" lalu cari prodi
            tp_text = tp_match.group(1)
            # Kadang prodi disebut setelah koma atau di akhir
            # Contoh: "Sarjana, Diploma. Program studi: ..." -> sudah tertangkap di strategi 1
            # Untuk kasus tanpa "Program studi:", cari kata benda setelah titik koma
            pass

    if not prodi_str:
        return ""

    # Bersihkan: hapus trailing dots, quotes, extra spaces
    prodi_str = prodi_str.strip().rstrip('.')
    prodi_str = re.sub(r'\s+', ' ', prodi_str)  # normalize whitespace

    # Split by koma
    raw_list = [p.strip().rstrip('.') for p in prodi_str.split(",")]

    # Bersihkan tiap item
    cleaned = []
    for item in raw_list:
        item = clean_prodi_name(item)
        if item and len(item) > 1:  # Skip kosong atau single char
            cleaned.append(item)

    # Hapus duplikat (case-insensitive)
    seen = set()
    unique = []
    for item in cleaned:
        key = item.lower()
        if key not in seen:
            seen.add(key)
            unique.append(item)

    return ", ".join(unique)


def main():
    print("=" * 60)
    print("GeoJob Hunter - CSV Data Seeder (Enhanced)")
    print("=" * 60)

    # Koneksi ke database
    conn = psycopg2.connect(**DB_CONFIG)
    cursor = conn.cursor()
    print(f"\n✓ Terkoneksi ke database: {DB_CONFIG['dbname']}")

    # Bersihkan data lama
    cursor.execute("DELETE FROM job")
    cursor.execute("DELETE FROM company")
    conn.commit()
    print("✓ Data lama dibersihkan")

    # Baca CSV
    companies_cache = {}  # key: (nama_perusahaan, alamat) -> company_id

    with open(CSV_PATH, "r", encoding="utf-8-sig") as f:
        reader = csv.DictReader(f)
        total_jobs = 0
        total_companies = 0
        no_prodi_count = 0

        for i, row in enumerate(reader, 1):
            nama_perusahaan = row.get("nama_perusahaan", "").strip()
            alamat_detail = row.get("alamat_detail", "").strip()
            lokasi = row.get("lokasi", "").strip()
            judul_lowongan = row.get("judul_lowongan", "").strip()
            kuota_str = row.get("kuota", "").strip()
            persyaratan = row.get("persyaratan", "").strip()
            link_lamar = row.get("link", "").strip()
            lat_str = row.get("latitude", "").strip()
            lon_str = row.get("longitude", "").strip()

            if not nama_perusahaan or not judul_lowongan:
                continue

            # Parse kuota
            try:
                kuota = int(re.search(r'\d+', kuota_str).group()) if kuota_str else 1
            except (ValueError, AttributeError):
                kuota = 1

            # Parse koordinat
            try:
                lat = float(lat_str) if lat_str else -6.2000
                lon = float(lon_str) if lon_str else 106.8166
            except ValueError:
                lat = -6.2000
                lon = 106.8166

            # Parse prodi
            prodi_syarat = parse_prodi(persyaratan)
            if not prodi_syarat:
                no_prodi_count += 1
                prodi_syarat = "Umum"

            # Cek apakah company sudah ada di cache
            company_key = (nama_perusahaan, alamat_detail)
            if company_key in companies_cache:
                company_id = companies_cache[company_key]
            else:
                # Insert company baru
                cursor.execute(
                    "INSERT INTO company (nama_perusahaan, alamat, lokasi, lat, lon) VALUES (%s, %s, %s, %s, %s) RETURNING id",
                    (nama_perusahaan, alamat_detail, lokasi, lat, lon)
                )
                company_id = cursor.fetchone()[0]
                companies_cache[company_key] = company_id
                total_companies += 1

            # Insert job
            cursor.execute(
                "INSERT INTO job (company_id, nama_lowongan, prodi_syarat, kuota, link_lamar) VALUES (%s, %s, %s, %s, %s)",
                (company_id, judul_lowongan, prodi_syarat, kuota, link_lamar)
            )
            total_jobs += 1

            # Progress setiap 5000 records
            if i % 5000 == 0:
                print(f"  Progress: {i} baris diproses...")
                conn.commit()

        conn.commit()

    print(f"\n✓ Selesai!")
    print(f"  Total perusahaan: {total_companies}")
    print(f"  Total lowongan: {total_jobs}")
    print(f"  Lowongan tanpa prodi spesifik (label: Umum): {no_prodi_count}")

    # Tampilkan sample prodi
    cursor.execute("SELECT prodi_syarat FROM job WHERE prodi_syarat != 'Umum' LIMIT 10")
    print("\n Sample prodi_syarat:")
    for row in cursor.fetchall():
        print(f"  - {row[0]}")

    cursor.close()
    conn.close()


if __name__ == "__main__":
    main()
