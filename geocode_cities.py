#!/usr/bin/env python3
"""
Geocode City Lookup Table
=========================
- Membaca CSV, extract unique cities
- Nominatim geocode tiap kota (dengan cache JSON)
- Output: city_coords_cache.json + city_coords.csv
- Output final: lowongan_final_v4_with_geocode.csv
"""

import pandas as pd
import requests
import time
import json
import os
import sys

# ── Konfigurasi ──
CSV_INPUT = "lowongan_final_v3_with_link.csv"
CACHE_FILE = "city_coords_cache.json"
CITY_CSV = "city_coords_lookup.csv"
CSV_OUTPUT = "lowongan_final_v4_with_geocode.csv"
NOMINATIM_DELAY = 1.0  # 1 request / detik (sesuai aturan Nominatim)
USER_AGENT = "GeoJobHunter/1.0 (geocoding-fixer)"

# ── Load cache jika ada ──
cache = {}
if os.path.exists(CACHE_FILE):
    with open(CACHE_FILE, "r") as f:
        cache = json.load(f)
    print(f"[Cache] Loaded {len(cache)} cached cities")

# ── Baca CSV ──
print("[CSV] Reading...")
df = pd.read_csv(CSV_INPUT)
print(f"[CSV] {len(df)} rows, columns: {list(df.columns)}")

# ── Unique cities ──
cities = sorted(df['lokasi'].dropna().str.strip().unique())
print(f"[City] {len(cities)} unique cities")

# ── Handle kota '-' (tidak diketahui) ──
if '-' in cities:
    cache['-'] = {'lat': -6.2088, 'lon': 106.8456, 'display': 'Jakarta (Default)', 'status': 'default'}
    print("[City] '-' → default Jakarta")

# ── Fungsi Nominatim ──
def geocode_nominatim(city_name):
    """
    Geocode a city name using Nominatim (OpenStreetMap).
    Returns (lat, lon, display_name) or (None, None, None) if not found.
    """
    url = "https://nominatim.openstreetmap.org/search"
    params = {
        "q": f"{city_name}, Indonesia",
        "format": "jsonv2",
        "limit": 1,
        "addressdetails": 1
    }
    headers = {
        "User-Agent": USER_AGENT
    }
    
    try:
        resp = requests.get(url, params=params, headers=headers, timeout=15)
        resp.raise_for_status()
        data = resp.json()
        
        if data and len(data) > 0:
            lat = float(data[0]['lat'])
            lon = float(data[0]['lon'])
            display = data[0].get('display_name', city_name)
            
            # Cek tipe: prefer city/regency level
            addr = data[0].get('address', {})
            result_type = data[0].get('type', '')
            
            return {
                'lat': lat,
                'lon': lon,
                'display': display,
                'type': result_type,
                'status': 'ok'
            }
        else:
            return {'lat': None, 'lon': None, 'display': city_name, 'type': 'unknown', 'status': 'not_found'}
    
    except requests.exceptions.HTTPError as e:
        if resp.status_code == 429:
            print(f"  [⚠️ Rate limited!] Waiting 5s...")
            time.sleep(5)
            return geocode_nominatim(city_name)  # retry
        return {'lat': None, 'lon': None, 'display': city_name, 'type': 'error', 'status': f'http_{resp.status_code}'}
    except Exception as e:
        return {'lat': None, 'lon': None, 'display': city_name, 'type': 'error', 'status': str(e)[:50]}

# ── Geocode cities yang belum di-cache ──
to_geocode = [c for c in cities if c not in cache]
print(f"\n[Geocode] {len(to_geocode)} cities need geocoding")
print(f"[Geocode] Estimated time: {len(to_geocode) * NOMINATIM_DELAY:.0f}s ({len(to_geocode) * NOMINATIM_DELAY / 60:.1f} min)")

for i, city in enumerate(to_geocode):
    print(f"  [{i+1}/{len(to_geocode)}] Geocoding: '{city}'...", end=" ", flush=True)
    
    result = geocode_nominatim(city)
    cache[city] = result
    
    if result['lat'] is not None:
        print(f"✅ ({result['lat']:.4f}, {result['lon']:.4f}) [{result['type']}]")
    else:
        print(f"❌ {result['status']}")
    
    # Save progress setiap 10 kota
    if (i + 1) % 10 == 0:
        with open(CACHE_FILE, "w") as f:
            json.dump(cache, f, indent=2)
        print(f"     [Cache saved: {len(cache)} cities]")
    
    time.sleep(NOMINATIM_DELAY)

# ── Save final cache ──
with open(CACHE_FILE, "w") as f:
    json.dump(cache, f, indent=2)
print(f"\n[Cache] Final: {len(cache)} cities saved to {CACHE_FILE}")

# ── Export city lookup CSV ──
city_rows = []
for city, data in cache.items():
    city_rows.append({
        'city': city,
        'lat': data.get('lat'),
        'lon': data.get('lon'),
        'status': data.get('status', 'unknown'),
        'type': data.get('type', ''),
    })
city_df = pd.DataFrame(city_rows)
city_df.to_csv(CITY_CSV, index=False)
print(f"[City CSV] Saved to {CITY_CSV}")

# ── Flag cities that failed ──
failed = [c for c, v in cache.items() if v.get('lat') is None]
if failed:
    print(f"\n[⚠️] {len(failed)} cities FAILED geocoding:")
    for c in failed[:20]:
        print(f"  - '{c}': {cache[c].get('status', 'unknown')}")
    if len(failed) > 20:
        print(f"  ... and {len(failed)-20} more")

print("\n[Phase 1 Selesai] ✅")
print(f"  - {len(cities)} cities processed")
print(f"  - {len(cities) - len(failed)} successful")
print(f"  - {len(failed)} failed (need manual fix)")
