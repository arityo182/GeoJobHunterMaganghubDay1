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
  }, []);

  return (
    <div className="flex h-screen bg-gray-50">
      {/* Sidebar Kiri */}
      <Sidebar
        filters={filters}
        setFilters={setFilters}
        onSearch={search}
        onGetLocation={getUserLocation}
      />

      {/* Main Panel Kanan */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* MAP — takes remaining space */}
        <div className="flex-1 min-h-[300px] mx-4 mt-4 rounded-lg overflow-hidden border border-gray-200 relative">
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
        </div>

        {/* BAGIAN BAWAH MAP — Compact: Metrics + Tabel */}
        <div className="max-h-[220px] overflow-y-auto border-t border-gray-200 bg-gray-50">
          {/* Metrics Cards — compact */}
          <MetricsCards data={data} loading={loading} />

          {/* Tabel Lowongan */}
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
