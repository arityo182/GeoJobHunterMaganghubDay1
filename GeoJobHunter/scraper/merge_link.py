"""
Gabung kolom 'link' dari lowongan_detail_v3_with_links.csv
ke lowongan_final_v3_filled.csv berdasarkan kecocokan seluruh kolom (9 kolom shared).
Hasil: lowongan_final_v3_with_link.csv
"""

import csv
import sys

FINAL_CSV = "/home/arie12345/Belajar/JobHun/lowongan_final_v3_filled.csv"
LINKS_CSV = "/home/arie12345/Belajar/JobHun/lowongan_detail_v3_with_links.csv"
OUTPUT_CSV = "/home/arie12345/Belajar/JobHun/lowongan_final_v3_with_link.csv"

KEY_COLS = [
    "judul_lowongan",
    "nama_perusahaan",
    "alamat_detail",
]

def clean(val):
    """Normalize value for comparison: strip whitespace, lowercase."""
    return val.strip().lower() if val else ""

def make_key(row, cols):
    """Buat tuple key dari kolom-kolom tertentu."""
    return tuple(clean(row[c]) for c in cols)

print("📖 Membaca lowongan_detail_v3_with_links.csv...")
links_lookup = {}
with open(LINKS_CSV, "r", encoding="utf-8-sig") as f:
    reader = csv.DictReader(f)
    total_links = 0
    for row in reader:
        key = make_key(row, KEY_COLS)
        link = row.get("link", "").strip()
        if link:  # Hanya simpan jika link tidak kosong
            if key not in links_lookup:  # Pertahankan link pertama
                links_lookup[key] = link
        total_links += 1
print(f"   ✅ {total_links} baris, {len(links_lookup)} unique key with link")

print("📖 Membaca lowongan_final_v3_filled.csv & menggabungkan link...")
matched = 0
unmatched = 0
total_rows = 0

with open(FINAL_CSV, "r", encoding="utf-8-sig") as fin, \
     open(OUTPUT_CSV, "w", encoding="utf-8-sig", newline="") as fout:

    reader = csv.DictReader(fin)
    fieldnames = reader.fieldnames + ["link"]
    writer = csv.DictWriter(fout, fieldnames=fieldnames)
    writer.writeheader()

    for row in reader:
        total_rows += 1
        key = make_key(row, KEY_COLS)
        link = links_lookup.get(key, "")
        row["link"] = link
        writer.writerow(row)
        if link:
            matched += 1
        else:
            unmatched += 1

print(f"\n✅ Selesai!")
print(f"   Total baris di final CSV : {total_rows}")
print(f"   Link cocok (matched)     : {matched}")
print(f"   Tidak punya link         : {unmatched}")
print(f"   Output: {OUTPUT_CSV}")
