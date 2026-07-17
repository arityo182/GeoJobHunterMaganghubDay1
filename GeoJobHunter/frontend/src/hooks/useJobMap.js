import { useState, useCallback, useRef } from 'react';
import { fetchJobsMap } from '../services/api';

const DEFAULT_LAT = parseFloat(import.meta.env.VITE_MAP_DEFAULT_LAT || '-6.2000');
const DEFAULT_LON = parseFloat(import.meta.env.VITE_MAP_DEFAULT_LON || '106.8166');

export function useJobMap() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [filters, setFilters] = useState({
    latUser: DEFAULT_LAT,
    lonUser: DEFAULT_LON,
    radius: 10,
    prodi: '',
    sortBy: 'asc',
  });
  const abortRef = useRef(null);

  const search = useCallback(async (overrides = {}) => {
    // Cancel previous request
    if (abortRef.current) {
      abortRef.current.abort();
    }
    abortRef.current = new AbortController();

    const params = { ...filters, ...overrides };
    setFilters(params);
    setLoading(true);
    setError(null);

    try {
      const result = await fetchJobsMap({
        lat_user: params.latUser,
        lon_user: params.lonUser,
        radius: params.radius,
        prodi: params.prodi || undefined,
        sortBy: params.sortBy,
      });
      setData(result);
    } catch (err) {
      setError(err.message);
      setData(null);
    } finally {
      setLoading(false);
    }
  }, [filters]);

  const getUserLocation = useCallback(() => {
    if (!navigator.geolocation) {
      setError('Geolocation tidak didukung browser Anda');
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const newFilters = {
          ...filters,
          latUser: pos.coords.latitude,
          lonUser: pos.coords.longitude,
        };
        setFilters(newFilters);
        search(newFilters);
      },
      () => {
        setError('Gagal mendapatkan lokasi. Izinkan akses GPS atau masukkan manual.');
      },
      { enableHighAccuracy: true, timeout: 10000 },
    );
  }, [filters, search]);

  return {
    data,
    loading,
    error,
    filters,
    setFilters,
    search,
    getUserLocation,
  };
}
