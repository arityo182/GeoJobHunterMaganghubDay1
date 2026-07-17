import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 15000,
});

/**
 * Fetch lowongan berdasarkan filter.
 *
 * @param {Object} params
 * @param {number} params.lat_user
 * @param {number} params.lon_user
 * @param {number} params.radius
 * @param {string} [params.prodi]
 * @param {string} [params.lokasi]
 * @param {string} [params.sortBy]
 * @returns {Promise<Object>}
 */
export const fetchJobsMap = async (params) => {
  try {
    const response = await api.get('/api/jobs/map', { params });
    return response.data;
  } catch (error) {
    if (error.response) {
      throw new Error(error.response.data.error || 'Gagal mengambil data');
    }
    throw new Error('Network error: server tidak merespon');
  }
};

/**
 * Dapatkan daftar lokasi unik untuk dropdown filter.
 * @returns {Promise<string[]>}
 */
export const fetchLocations = async () => {
  try {
    const response = await api.get('/api/jobs/locations');
    return response.data;
  } catch {
    return [];
  }
};
