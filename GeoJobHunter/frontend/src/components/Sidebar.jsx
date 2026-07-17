import { useState, useEffect } from 'react';

const PRODI_OPTIONS = [
  { value: '', label: 'Semua Jurusan' },
  { value: 'Manajemen', label: 'Manajemen' },
  { value: 'Ilmu Komunikasi', label: 'Ilmu Komunikasi' },
  { value: 'Akuntansi', label: 'Akuntansi' },
  { value: 'Sistem Informasi', label: 'Sistem Informasi' },
  { value: 'Teknik Informatika', label: 'Teknik Informatika' },
  { value: 'Administrasi Bisnis', label: 'Administrasi Bisnis' },
  { value: 'Teknik Industri', label: 'Teknik Industri' },
  { value: 'Desain Komunikasi Visual', label: 'Desain Komunikasi Visual / DKV' },
  { value: 'Psikologi', label: 'Psikologi' },
  { value: 'Hukum', label: 'Hukum' },
  { value: 'Ekonomi', label: 'Ekonomi' },
  { value: 'Ilmu Komputer', label: 'Ilmu Komputer' },
  { value: 'Administrasi Perkantoran', label: 'Administrasi Perkantoran' },
  { value: 'Keperawatan', label: 'Keperawatan' },
  { value: 'Komunikasi', label: 'Komunikasi' },
  { value: 'Manajemen Pemasaran', label: 'Manajemen Pemasaran / Marketing' },
  { value: 'Hubungan Masyarakat', label: 'Hubungan Masyarakat / Humas' },
  { value: 'Teknik Elektro', label: 'Teknik Elektro' },
  { value: 'Teknik Mesin', label: 'Teknik Mesin' },
  { value: 'Manajemen Sumber Daya Manusia', label: 'Manajemen SDM' },
  { value: 'Teknologi Informasi', label: 'Teknologi Informasi' },
  { value: 'Teknik Sipil', label: 'Teknik Sipil' },
  { value: 'Multimedia', label: 'Multimedia' },
  { value: 'Ilmu Gizi', label: 'Ilmu Gizi' },
  { value: 'Farmasi', label: 'Farmasi' },
  { value: 'Kedokteran', label: 'Kedokteran' },
  { value: 'Administrasi Publik', label: 'Administrasi Publik / Negara' },
  { value: 'Statistika', label: 'Statistika' },
  { value: 'Matematika', label: 'Matematika' },
  { value: 'Jurnalistik', label: 'Jurnalistik' },
];

export default function Sidebar({ filters, setFilters, onSearch, onGetLocation, onClose }) {
  const [latInput, setLatInput] = useState(filters.latUser);
  const [lonInput, setLonInput] = useState(filters.lonUser);

  // Sync input fields when GPS updates filters from parent
  useEffect(() => {
    setLatInput(filters.latUser);
    setLonInput(filters.lonUser);
  }, [filters.latUser, filters.lonUser]);

  const handleApply = () => {
    onSearch({
      latUser: parseFloat(latInput) || filters.latUser,
      lonUser: parseFloat(lonInput) || filters.lonUser,
    });
  };

  return (
    <aside className="w-80 bg-white border-r border-gray-200 p-5 overflow-y-auto flex flex-col gap-5 shadow-xl lg:shadow-none">
      {/* Header + Close button */}
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold text-blue-700">🗺️ GeoJob Hunter</h1>
        {onClose && (
          <button
            onClick={onClose}
            className="lg:hidden p-1 rounded-md hover:bg-gray-100 text-gray-400 hover:text-gray-600"
            aria-label="Tutup sidebar"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        )}
      </div>
      <p className="text-xs text-gray-500 -mt-3">Cari lowongan magang di sekitarmu</p>

      {/* Lokasi User */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Lokasi Saya
        </label>
        <div className="flex gap-2 mb-2">
          <input
            type="number"
            step="0.0001"
            placeholder="Latitude"
            value={latInput}
            onChange={(e) => setLatInput(e.target.value)}
            className="w-1/2 px-2 py-1.5 border rounded text-sm"
          />
          <input
            type="number"
            step="0.0001"
            placeholder="Longitude"
            value={lonInput}
            onChange={(e) => setLonInput(e.target.value)}
            className="w-1/2 px-2 py-1.5 border rounded text-sm"
          />
        </div>
        <button
          onClick={onGetLocation}
          className="w-full px-3 py-2 bg-blue-600 text-white rounded-lg text-sm hover:bg-blue-700 transition"
        >
          📡 Gunakan GPS Saya
        </button>
      </div>

      {/* Radius Slider */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Radius Jarak: <span className="font-bold">{filters.radius} km</span>
        </label>
        <input
          type="range"
          min="1"
          max="5000"
          step="1"
          value={Math.min(filters.radius, 5000)}
          onChange={(e) => setFilters({ ...filters, radius: parseInt(e.target.value) })}
          className="w-full"
        />
        <div className="flex justify-between text-xs text-gray-400">
          <span>1 km</span>
          <span>100 km</span>
          <span>5000 km</span>
        </div>
      </div>

      {/* Dropdown Prodi */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Program Studi
        </label>
        <select
          value={filters.prodi}
          onChange={(e) => setFilters({ ...filters, prodi: e.target.value })}
          className="w-full px-3 py-2 border rounded-lg text-sm"
        >
          {PRODI_OPTIONS.map((opt) => (
            <option key={opt.value} value={opt.value}>{opt.label}</option>
          ))}
        </select>
      </div>

      {/* Sorting */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Urutkan Jarak
        </label>
        <select
          value={filters.sortBy}
          onChange={(e) => setFilters({ ...filters, sortBy: e.target.value })}
          className="w-full px-3 py-2 border rounded-lg text-sm"
        >
          <option value="asc">Terdekat → Terjauh</option>
          <option value="desc">Terjauh → Terdekat</option>
        </select>
      </div>

      {/* Tombol Terapkan */}
      <button
        onClick={handleApply}
        className="w-full px-4 py-2.5 bg-green-600 text-white rounded-lg font-medium hover:bg-green-700 transition"
      >
        🔍 Terapkan Filter
      </button>
    </aside>
  );
}
