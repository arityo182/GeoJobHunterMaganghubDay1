export default function MetricsCards({ data, loading }) {
  return (
    <div className="flex gap-3 px-4 pt-3">
      <div className={`flex-1 bg-white rounded-lg shadow-sm border border-gray-100 px-3 py-2 ${loading ? 'opacity-50' : ''}`}>
        <p className="text-xs text-gray-400 uppercase tracking-wide">Lowongan Cocok</p>
        <p className="text-lg font-bold text-gray-800">
          {loading ? '...' : (data?.totalLowongan ?? 0)}
        </p>
      </div>
      <div className={`flex-1 bg-white rounded-lg shadow-sm border border-gray-100 px-3 py-2 ${loading ? 'opacity-50' : ''}`}>
        <p className="text-xs text-gray-400 uppercase tracking-wide">Perusahaan Terdekat</p>
        <p className="text-lg font-bold text-gray-800">
          {loading ? '...' : (data?.totalPerusahaan ?? 0)}
        </p>
      </div>
    </div>
  );
}
