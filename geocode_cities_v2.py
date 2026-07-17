#!/usr/bin/env python3
"""
Geocode City Lookup v2 — Lebih akurat via Nominatim
====================================================
Prioritas administrative region, fallback bertingkat.
"""

import pandas as pd
import requests
import time
import json
import os

CSV_INPUT = "lowongan_final_v3_with_link.csv"
CACHE_FILE = "city_coords_cache_v2.json"
CSV_OUTPUT = "lowongan_final_v4_with_geocode.csv"
NOMINATIM_DELAY = 1.1
USER_AGENT = "GeoJobHunter/2.0"

# Load cache
cache = {}
if os.path.exists(CACHE_FILE):
    with open(CACHE_FILE) as f:
        cache = json.load(f)
    print(f"[Cache] Loaded {len(cache)} cities")

# Load old cache too, for reference
old_cache = {}
if os.path.exists("city_coords_cache.json"):
    with open("city_coords_cache.json") as f:
        old_cache = json.load(f)
    print(f"[Old Cache] {len(old_cache)} cities available for reference")

def haversine_km(lat1, lon1, lat2, lon2):
    """Hitung jarak antara dua koordinat dalam km."""
    from math import radians, sin, cos, sqrt, atan2
    R = 6371
    dlat = radians(lat2 - lat1)
    dlon = radians(lon2 - lon1)
    a = sin(dlat/2)**2 + cos(radians(lat1)) * cos(radians(lat2)) * sin(dlon/2)**2
    c = 2 * atan2(sqrt(a), sqrt(1-a))
    return R * c

def is_coord_in_indonesia(lat, lon):
    """Cek apakah koordinat masuk bounding box Indonesia."""
    return -11 <= lat <= 6 and 94 <= lon <= 142

def search_nominatim(query, params_extra=None):
    """Search Nominatim dengan parameter tertentu."""
    url = "https://nominatim.openstreetmap.org/search"
    params = {
        "q": query,
        "format": "jsonv2",
        "limit": 5,
        "countrycodes": "id",
        "accept-language": "id",
    }
    if params_extra:
        params.update(params_extra)
    
    headers = {"User-Agent": USER_AGENT}
    
    try:
        resp = requests.get(url, params=params, headers=headers, timeout=15)
        resp.raise_for_status()
        return resp.json()
    except Exception as e:
        return []

def geocode_city(city_name):
    """
    Geocode dengan strategi bertingkat:
    1. Cari full name "Kabupaten X, Indonesia" 
    2. Cari "X, Indonesia" 
    3. Fallback ke old cache jika ada
    """
    city_clean = city_name.strip()
    
    # ── Tentukan jenis kota ──
    is_kab = city_clean.lower().startswith("kab.")
    is_kota = city_clean.lower().startswith("kota")
    is_kota_adm = city_clean.lower().startswith("kota adm.")
    
    # Extract nama inti
    nama_inti = city_clean
    for prefix in ["Kota Adm. ", "Kota ", "Kab. ", "Kab "]:
        if nama_inti.startswith(prefix):
            nama_inti = nama_inti[len(prefix):]
            break
    
    candidates = []
    
    # Strategy 1: "Kabupaten X" atau "Kota X"
    if is_kab:
        q1 = f"Kabupaten {nama_inti}, Indonesia"
    elif is_kota_adm:
        q1 = f"Kota {nama_inti}, Indonesia"
    elif is_kota:
        q1 = f"Kota {nama_inti}, Indonesia"
    else:
        q1 = f"{city_clean}, Indonesia"
    
    results1 = search_nominatim(q1)
    for r in results1:
        rtype = r.get('type', '')
        cat = r.get('category', '')
        lat, lon = float(r['lat']), float(r['lon'])
        if is_coord_in_indonesia(lat, lon):
            candidates.append({
                'lat': lat, 'lon': lon,
                'display': r.get('display_name', ''),
                'type': rtype,
                'category': cat,
                'importance': r.get('importance', 0),
                'strategy': 'strategy1'
            })
    
    # Strategy 2: langsung "X, Indonesia" (tanpa prefix)
    if not candidates:
        q2 = f"{nama_inti}, Indonesia"
        results2 = search_nominatim(q2)
        for r in results2:
            rtype = r.get('type', '')
            cat = r.get('category', '')
            lat, lon = float(r['lat']), float(r['lon'])
            if is_coord_in_indonesia(lat, lon):
                candidates.append({
                    'lat': lat, 'lon': lon,
                    'display': r.get('display_name', ''),
                    'type': rtype,
                    'category': cat,
                    'importance': r.get('importance', 0),
                    'strategy': 'strategy2'
                })
    
    # Strategy 3: cari dengan "Kecamatan X" fallback
    if not candidates:
        q3 = f"Kecamatan {nama_inti}, Indonesia"
        results3 = search_nominatim(q3)
        for r in results3:
            rtype = r.get('type', '')
            lat, lon = float(r['lat']), float(r['lon'])
            if is_coord_in_indonesia(lat, lon):
                candidates.append({
                    'lat': lat, 'lon': lon,
                    'display': r.get('display_name', ''),
                    'type': rtype,
                    'category': r.get('category', ''),
                    'importance': r.get('importance', 0),
                    'strategy': 'strategy3'
                })
    
    if candidates:
        # Pilih yang terbaik: prioritaskan administrative > city > territory > other
        def score(c):
            s = c.get('importance', 0)
            bonus = {
                'administrative': 10,
                'city': 8,
                'town': 6,
                'territory': 5,
                'county': 4,
                'municipality': 3,
                'village': 2,
            }.get(c.get('type', ''), 0)
            return s + bonus
        
        best = max(candidates, key=score)
        return {
            'lat': best['lat'],
            'lon': best['lon'],
            'display': best['display'],
            'type': best['type'],
            'strategy': best['strategy'],
            'status': 'ok'
        }
    
    # Strategy 4: old cache
    if city_name in old_cache and old_cache[city_name].get('lat') is not None:
        oc = old_cache[city_name]
        if is_coord_in_indonesia(oc['lat'], oc['lon']):
            return {
                'lat': oc['lat'], 'lon': oc['lon'],
                'display': oc.get('display', city_name),
                'type': oc.get('type', 'old_cache'),
                'status': 'ok_from_old'
            }
    
    return {'lat': None, 'lon': None, 'display': city_name, 'type': 'unknown', 'status': 'not_found'}


# ── Baca CSV ──
print("[CSV] Reading...")
df = pd.read_csv(CSV_INPUT)
print(f"[CSV] {len(df)} rows")

# ── Unique cities ──
cities = sorted(df['lokasi'].dropna().str.strip().unique())
print(f"[City] {len(cities)} unique cities")

# Handle '-'
if '-' in cities:
    cache['-'] = {'lat': -6.2088, 'lon': 106.8456, 'display': 'Jakarta (Default)', 'type': 'default', 'status': 'default'}

# ── Geocode yang belum ada ──
to_geocode = [c for c in cities if c not in cache]
print(f"[Geocode] {len(to_geocode)} need geocoding ({len(cities) - len(to_geocode)} already cached)")
print(f"[Geocode] ETA: {len(to_geocode) * NOMINATIM_DELAY:.0f}s (~{len(to_geocode) * NOMINATIM_DELAY / 60:.1f} min)\n")

for i, city in enumerate(to_geocode):
    print(f"  [{i+1}/{len(to_geocode)}] '{city}'...", end=" ", flush=True)
    
    result = geocode_city(city)
    cache[city] = result
    
    if result['lat'] is not None:
        print(f"✅ ({result['lat']:.4f}, {result['lon']:.4f}) [{result['type']}] via {result.get('strategy','?')}")
    else:
        print(f"❌ {result['status']}")
    
    # Save per 5 kota
    if (i + 1) % 5 == 0:
        with open(CACHE_FILE, "w") as f:
            json.dump(cache, f, indent=2)
        print(f"     [Cache: {len(cache)}/{len(cities)} cities]")
    
    time.sleep(NOMINATIM_DELAY)

# ── Final save ──
with open(CACHE_FILE, "w") as f:
    json.dump(cache, f, indent=2)
print(f"\n[Cache] Final: {len(cache)} cities")

# ── Generate CSV final ──
print("\n[CSV] Generating final dataset...")

new_rows = []
fixed_count = 0
skip_count = 0

for idx, row in df.iterrows():
    city = str(row['lokasi']).strip()
    old_lat = float(row['latitude']) if pd.notna(row['latitude']) else None
    old_lon = float(row['longitude']) if pd.notna(row['longitude']) else None
    
    # Cari koordinat baru
    if city in cache and cache[city]['lat'] is not None:
        new_lat = cache[city]['lat']
        new_lon = cache[city]['lon']
    else:
        new_lat = old_lat
        new_lon = old_lon
    
    # Hitung jarak perubahan
    if old_lat and old_lon and new_lat and new_lon:
        dist = haversine_km(old_lat, old_lon, new_lat, new_lon)
        if dist > 50:
            status = 'FIXED'
            fixed_count += 1
        else:
            status = 'OK'
            skip_count += 1
    else:
        status = 'NO_COORDS'
        fixed_count += 1
    
    new_rows.append({
        'judul_lowongan': row['judul_lowongan'],
        'nama_perusahaan': row['nama_perusahaan'],
        'lokasi': city,
        'alamat_detail': row['alamat_detail'],
        'kuota': row['kuota'],
        'durasi': row['durasi'],
        'uang_saku': row['uang_saku'],
        'deskripsi_pekerjaan': row['deskripsi_pekerjaan'],
        'persyaratan': row['persyaratan'],
        'old_lat': old_lat,
        'old_lon': old_lon,
        'new_lat': new_lat,
        'new_lon': new_lon,
        'status': status,
        'link': row['link'],
    })

output_df = pd.DataFrame(new_rows)
output_df.to_csv(CSV_OUTPUT, index=False)

print(f"\n✅ CSV FINAL: {CSV_OUTPUT}")
print(f"   Total rows: {len(output_df)}")
print(f"   Fixed (moved >50km): {fixed_count}")
print(f"   Unchanged: {skip_count}")

# ── Summary ──
print("\n📊 SUMMARY")
print(f"   Cities processed: {len(cities)}")
print(f"   Geocode OK: {sum(1 for c in cities if cache.get(c,{}).get('lat') is not None)}")
print(f"   Geocode FAILED: {sum(1 for c in cities if cache.get(c,{}).get('lat') is None)}")
failed = [c for c in cities if cache.get(c,{}).get('lat') is None]
if failed:
    print(f"\n⚠️ Failed cities ({len(failed)}):")
    for c in failed[:15]:
        print(f"   - '{c}'")
    if len(failed) > 15:
        print(f"   ... and {len(failed)-15} more")
