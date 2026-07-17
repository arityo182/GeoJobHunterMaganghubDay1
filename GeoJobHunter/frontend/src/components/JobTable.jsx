import { useState, useMemo, useEffect, useRef } from 'react';
import { fetchLocations } from '../services/api';

const MAX_LOCATIONS_SHOWN = 200;

function LocationPicker({ locations, value, onChange }) {
  const [input, setInput] = useState(value || '');
  const [open, setOpen] = useState(false);
  const [highlightIdx, setHighlightIdx] = useState(-1);
  const wrapperRef = useRef(null);

  const filtered = useMemo(() => {
    if (!input.trim()) return locations.slice(0, MAX_LOCATIONS_SHOWN);
    const q = input.toLowerCase();
    return locations.filter((loc) => loc.toLowerCase().includes(q)).slice(0, MAX_LOCATIONS_SHOWN);
  }, [input, locations]);

  // Click outside to close
  useEffect(() => {
    const handler = (e) => {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) {
        setOpen(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const select = (loc) => {
    setInput(loc);
    setOpen(false);
    onChange(loc);
  };

  const clear = () => {
    setInput('');
    setOpen(false);
    onChange('');
  };

  const handleKeyDown = (e) => {
    if (!open || filtered.length === 0) return;
    if (e.key === 'ArrowDown') { e.preventDefault(); setHighlightIdx((p) => Math.min(p + 1, filtered.length - 1)); }
    if (e.key === 'ArrowUp') { e.preventDefault(); setHighlightIdx((p) => Math.max(p - 1, 0)); }
    if (e.key === 'Enter' && highlightIdx >= 0) { e.preventDefault(); select(filtered[highlightIdx]); }
    if (e.key === 'Escape') { setOpen(false); }
  };

  return (
    <div ref={wrapperRef} className="relative w-44">
      <input
        type="text"
        placeholder="📍 Cari kota..."
        value={input}
        onChange={(e) => { setInput(e.target.value); setOpen(true); setHighlightIdx(-1); }}
        onFocus={() => setOpen(true)}
        onKeyDown={handleKeyDown}
        className="w-full px-2 py-1.5 pr-6 text-xs border border-gray-200 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-400"
      />
      {input && (
        <button
          onClick={clear}
          className="absolute right-1 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 text-xs px-1"
        >
          ✕
        </button>
      )}
      {open && (
        <ul className="absolute z-50 left-0 right-0 mt-0.5 bg-white border border-gray-200 rounded-md shadow-lg max-h-48 overflow-y-auto">
          <li
            onClick={() => onChange('') || setInput('') || setOpen(false)}
            className="px-2 py-1.5 text-[10px] text-gray-400 hover:bg-gray-50 cursor-pointer border-b border-gray-100"
          >
            Semua Kota
          </li>
          {filtered.length === 0 && (
            <li className="px-2 py-1.5 text-[10px] text-gray-300 italic">Tidak ditemukan</li>
          )}
          {filtered.map((loc, idx) => (
            <li
              key={loc}
              onClick={() => select(loc)}
              className={`px-2 py-1.5 text-xs cursor-pointer truncate ${
                idx === highlightIdx ? 'bg-blue-100 font-medium' : 'hover:bg-gray-50'
              }`}
            >
              {loc}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default function JobTable({ companies, loading, onSelectCompany }) {
  const [searchText, setSearchText] = useState('');
  const [filterLokasi, setFilterLokasi] = useState('');
  const [locations, setLocations] = useState([]);
  const [sortField, setSortField] = useState(null);   // 'kuota' | 'jarak' | null
  const [sortDir, setSortDir] = useState('asc');       // 'asc' | 'desc'

  // Ambil daftar lokasi dari API
  useEffect(() => {
    fetchLocations().then(setLocations);
  }, []);

  // Flatten semua lowongan dari semua company jadi satu list
  const allJobs = useMemo(() =>
    (companies || []).flatMap((company) =>
      (company.lowongan || []).map((job) => ({
        ...job,
        namaPerusahaan: company.namaPerusahaan,
        alamat: company.alamat,
        lokasi: company.lokasi || '',
        jarakKm: company.jarakKm,
        warnaPin: company.warnaPin,
        companyLat: company.lat,
        companyLon: company.lon,
      }))
    ),
    [companies]
  );

  // Filter & sort
  const filteredJobs = useMemo(() => {
    let result = allJobs;

    // Filter teks (cari di semua kolom)
    if (searchText.trim()) {
      const q = searchText.toLowerCase();
      result = result.filter((job) =>
        (job.namaPerusahaan && job.namaPerusahaan.toLowerCase().includes(q)) ||
        (job.namaLowongan && job.namaLowongan.toLowerCase().includes(q)) ||
        (job.prodiSyarat && job.prodiSyarat.toLowerCase().includes(q)) ||
        (job.badge && job.badge.toLowerCase().includes(q)) ||
        (job.kuota && String(job.kuota).includes(q)) ||
        (job.jarakKm && job.jarakKm.toFixed(1).includes(q)) ||
        (job.lokasi && job.lokasi.toLowerCase().includes(q)) ||
        (job.linkLamar && job.linkLamar.toLowerCase().includes(q))
      );
    }

    // Filter lokasi
    if (filterLokasi) {
      result = result.filter((job) => job.lokasi === filterLokasi);
    }

    // Sort
    if (sortField === 'kuota') {
      result.sort((a, b) => sortDir === 'asc' ? a.kuota - b.kuota : b.kuota - a.kuota);
    } else if (sortField === 'jarak') {
      result.sort((a, b) => sortDir === 'asc' ? a.jarakKm - b.jarakKm : b.jarakKm - a.jarakKm);
    }

    return result;
  }, [allJobs, searchText, filterLokasi, sortField, sortDir]);

  const handleSort = (field) => {
    if (sortField === field) {
      // Toggle direction
      setSortDir((prev) => (prev === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortField(field);
      setSortDir('asc');
    }
  };

  const sortIcon = (field) => {
    if (sortField !== field) return ' ↕';
    return sortDir === 'asc' ? ' ▲' : ' ▼';
  };

  if (loading) {
    return (
      <div className="bg-white rounded shadow-sm p-4 text-center text-xs text-gray-400">
        Memuat data...
      </div>
    );
  }

  if (allJobs.length === 0) {
    return (
      <div className="bg-white rounded shadow-sm p-4 text-center text-xs text-gray-400">
        Tidak ada data lowongan
      </div>
    );
  }

  const handleRowClick = (job) => {
    if (onSelectCompany && job.companyLat != null && job.companyLon != null) {
      onSelectCompany(job.companyLat, job.companyLon);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-sm">
      {/* Filter bar: search + lokasi */}
      <div className="px-3 pt-2 pb-1 flex gap-2">
        <input
          type="text"
          placeholder="🔍 Cari dalam tabel..."
          value={searchText}
          onChange={(e) => setSearchText(e.target.value)}
          className="flex-1 px-2.5 py-1.5 text-xs border border-gray-200 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-400"
        />
        <LocationPicker
          locations={locations}
          value={filterLokasi}
          onChange={setFilterLokasi}
        />
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-xs">
          <thead className="bg-gray-50 border-b">
            <tr>
              <th className="px-3 py-2 text-left font-semibold text-gray-500 uppercase tracking-wide">Perusahaan</th>
              <th className="px-3 py-2 text-left font-semibold text-gray-500 uppercase tracking-wide">Posisi</th>
              <th
                className="px-3 py-2 text-center font-semibold text-gray-500 uppercase tracking-wide w-[40px] cursor-pointer hover:text-gray-700 select-none"
                onClick={() => handleSort('kuota')}
                title="Urutkan berdasarkan kuota"
              >
                Kuota<span className="text-gray-400 text-[10px]">{sortIcon('kuota')}</span>
              </th>
              <th
                className="px-3 py-2 text-center font-semibold text-gray-500 uppercase tracking-wide w-[60px] cursor-pointer hover:text-gray-700 select-none"
                onClick={() => handleSort('jarak')}
                title="Urutkan berdasarkan jarak"
              >
                Jarak<span className="text-gray-400 text-[10px]">{sortIcon('jarak')}</span>
              </th>
              <th className="px-3 py-2 text-left font-semibold text-gray-500 uppercase tracking-wide">Badge</th>
              <th className="px-3 py-2 text-left font-semibold text-gray-500 uppercase tracking-wide">Prodi Syarat</th>
              <th className="px-3 py-2 text-center font-semibold text-gray-500 uppercase tracking-wide w-[38px]">Link</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {filteredJobs.map((job) => (
              <tr
                key={job.id}
                onClick={() => handleRowClick(job)}
                className="hover:bg-blue-50 transition cursor-pointer"
                title={`Klik untuk lihat di peta — ${job.namaPerusahaan}`}
              >
                <td className="px-3 py-1.5 font-medium text-gray-700 truncate max-w-[110px]">
                  {job.namaPerusahaan}
                  {job.lokasi && <span className="block text-[9px] text-gray-400 font-normal">{job.lokasi}</span>}
                </td>
                <td className="px-3 py-1.5 text-gray-600 truncate max-w-[140px]">{job.namaLowongan}</td>
                <td className="px-3 py-1.5 text-center text-gray-600 font-medium">{job.kuota}</td>
                <td className="px-3 py-1.5 text-center text-gray-500 whitespace-nowrap">{job.jarakKm.toFixed(1)} km</td>
                <td className="px-3 py-1.5">
                  {job.badge && (
                    <span className="inline-block text-[10px] bg-yellow-100 text-yellow-800 px-1.5 py-0.5 rounded-full whitespace-nowrap">
                      {job.badge}
                    </span>
                  )}
                </td>
                <td className="px-3 py-1.5 text-[10px] text-gray-400 max-w-[160px] whitespace-normal leading-tight">
                  {job.prodiSyarat}
                </td>
                <td className="px-3 py-1.5 text-center">
                  {job.linkLamar ? (
                    <a
                      href={job.linkLamar}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="inline-flex items-center justify-center w-6 h-6 bg-blue-100 hover:bg-blue-200 text-blue-700 rounded transition"
                      title="Buka link lamaran"
                      onClick={(e) => e.stopPropagation()}
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                      </svg>
                    </a>
                  ) : (
                    <span className="text-gray-300 text-[10px]">—</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Info jumlah */}
      <div className="px-3 py-1.5 text-[10px] text-gray-400 border-t border-gray-100 flex justify-between">
        <span>
          {filterLokasi && <span className="mr-2">📍 {filterLokasi}</span>}
          {sortField && <span>Urut: {sortField} {sortDir === 'asc' ? '↑' : '↓'}</span>}
        </span>
        <span>{filteredJobs.length}/{allJobs.length} lowongan — klik baris untuk lihat di peta</span>
      </div>
    </div>
  );
}
