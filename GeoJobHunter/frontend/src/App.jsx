import { useEffect, useState, useCallback } from 'react';
import Sidebar from './components/Sidebar';
import MetricsCards from './components/MetricsCards';
import JobMap from './components/JobMap';
import JobTable from './components/JobTable';
import { useJobMap } from './hooks/useJobMap';

export default function App() {
  const {
    data,
    loading,
    error,
    filters,
    setFilters,
    search,
    getUserLocation,
  } = useJobMap();

  // Koordinat untuk map.flyTo saat klik baris tabel
  const [flyToCoords, setFlyToCoords] = useState(null);

  // Mobile: sidebar drawer + table bottom sheet
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [showTable, setShowTable] = useState(false);

  const totalLowongan = data?.totalLowongan || 0;

  // Fetch awal saat komponen mount
  useEffect(() => {
    search();
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // Klik di peta → pindah lokasi pencarian & refresh data
  const handleMapClick = useCallback((lat, lon) => {
    const newFilters = {
      ...filters,
      latUser: lat,
      lonUser: lon,
    };
    setFilters(newFilters);
    search(newFilters);
  }, [filters, search, setFilters]);

  // Klik baris tabel → terbang ke perusahaan di peta
  const handleSelectCompany = useCallback((lat, lon) => {
    setFlyToCoords({ lat, lon });
    // Di mobile, sembunyikan tabel setelah klik
    setShowTable(false);
  }, []);

  // Search dari sidebar (tutup drawer di mobile)
  const handleSearch = useCallback((overrides) => {
    search(overrides);
    setSidebarOpen(false);
  }, [search]);

  return (
    <div className="flex h-screen bg-gray-50">
      {/* ─── OVERLAY SIDEBAR MOBILE ─── */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 bg-black/40 z-40 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* ─── SIDEBAR ─── */}
      <div className={`
        fixed lg:static inset-y-0 left-0 z-50
        transition-transform duration-300
        ${sidebarOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
      `}>
        <Sidebar
          filters={filters}
          setFilters={setFilters}
          onSearch={handleSearch}
          onGetLocation={getUserLocation}
          onClose={() => setSidebarOpen(false)}
        />
      </div>

      {/* ─── MAIN PANEL ─── */}
      <div className="flex-1 flex flex-col overflow-hidden relative">
        {/* Top Bar Mobile — hamburger + judul */}
        <div className="lg:hidden flex items-center justify-between px-4 py-2 bg-white border-b border-gray-200 z-10">
          <button
            onClick={() => setSidebarOpen(true)}
            className="p-1 rounded-md hover:bg-gray-100"
            aria-label="Buka filter"
          >
            <svg className="w-6 h-6 text-gray-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
          <span className="text-sm font-semibold text-blue-700">🗺️ GeoJob Hunter</span>
          <span className="text-xs text-gray-400">{totalLowongan} lowongan</span>
        </div>

        {/* MAP */}
        <div className="flex-1 relative">
          {loading && (
            <div className="absolute inset-0 flex items-center justify-center bg-white/80 z-[1000]">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
              <span className="ml-3 text-gray-600">Memuat data...</span>
            </div>
          )}
          {error && (
            <div className="absolute top-4 left-4 right-4 z-[1000] bg-red-100 border-l-4 border-red-500 text-red-700 px-4 py-3 rounded shadow">
              <p>{error}</p>
            </div>
          )}
          <JobMap
            userLat={filters.latUser}
            userLon={filters.lonUser}
            radius={filters.radius}
            companies={data?.perusahaan || []}
            onMapClick={handleMapClick}
            flyToCoords={flyToCoords}
          />

          {/* Tombol toggle tabel — mobile */}
          <button
            onClick={() => setShowTable(!showTable)}
            className="lg:hidden absolute bottom-4 left-1/2 -translate-x-1/2 z-[1000]
                       bg-white border border-gray-200 rounded-full shadow-lg
                       px-4 py-2 text-sm font-medium text-gray-700
                       hover:bg-gray-50 transition"
          >
            📋 {showTable ? 'Tutup' : `Lihat Lowongan (${totalLowongan})`}
          </button>
        </div>

        {/* ─── BOTTOM SHEET: METRICS + TABLE ─── */}
        <div className={`
          lg:max-h-[220px] lg:overflow-y-auto border-t border-gray-200 bg-gray-50
          fixed lg:static bottom-0 left-0 right-0 z-30
          transition-transform duration-300
          ${showTable ? 'translate-y-0 max-h-[70vh]' : 'translate-y-full lg:translate-y-0 lg:max-h-[220px]'}
          overflow-y-auto rounded-t-2xl lg:rounded-none shadow-2xl lg:shadow-none
        `}>
          {/* Handle bar — mobile */}
          <div className="lg:hidden flex justify-center pt-2 pb-1">
            <div className="w-10 h-1 rounded-full bg-gray-300" />
          </div>

          <MetricsCards data={data} loading={loading} />

          <div className="px-4 pb-3 pt-2">
            <JobTable
              companies={data?.perusahaan || []}
              loading={loading}
              onSelectCompany={handleSelectCompany}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
