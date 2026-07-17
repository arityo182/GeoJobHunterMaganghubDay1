import { useEffect, useRef } from 'react';
import { MapContainer, TileLayer, Marker, Circle, CircleMarker, Popup, useMap, useMapEvents } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Fix default marker icon Leaflet
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
});

const homeIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-blue.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
});

const clickIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41],
});

const COLOR_MAP = {
  green: '#22c55e',
  orange: '#f97316',
  red: '#ef4444',
};

// ─── MapUpdater: geser peta saat userLat/userLon berubah ───
function MapUpdater({ lat, lon }) {
  const map = useMap();
  useEffect(() => {
    map.setView([lat, lon], map.getZoom());
  }, [lat, lon, map]);
  return null;
}

// ─── MapFlyTo: terbang ke koordinat tertentu (dari klik tabel) ───
function MapFlyTo({ coords }) {
  const map = useMap();
  const prevRef = useRef(null);

  useEffect(() => {
    if (!coords) return;
    // Hindari flyTo ganda ke koordinat yang sama
    const key = `${coords.lat.toFixed(5)}_${coords.lon.toFixed(5)}`;
    if (prevRef.current === key) return;
    prevRef.current = key;

    map.flyTo([coords.lat, coords.lon], 15, { duration: 0.8 });
  }, [coords, map]);

  return null;
}

// ─── MapClickHandler: tangkap klik di peta ───
function MapClickHandler({ onMapClick }) {
  useMapEvents({
    click(e) {
      if (onMapClick) {
        onMapClick(e.latlng.lat, e.latlng.lng);
      }
    },
  });
  return null;
}

export default function JobMap({ userLat, userLon, radius, companies, onMapClick, flyToCoords }) {
  return (
    <MapContainer
      center={[userLat, userLon]}
      zoom={12}
      className="w-full h-full"
      scrollWheelZoom={true}
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />

      <MapUpdater lat={userLat} lon={userLon} />
      <MapFlyTo coords={flyToCoords} />
      <MapClickHandler onMapClick={onMapClick} />

      {/* Pin Rumah User */}
      <Marker position={[userLat, userLon]} icon={homeIcon}>
        <Popup>
          <div className="text-center">
            <strong>📍 Lokasi Saya</strong>
            <br />
            <span className="text-xs text-gray-500">
              {userLat.toFixed(4)}, {userLon.toFixed(4)}
            </span>
            <p className="text-xs text-blue-600 mt-1">Klik peta untuk pindah lokasi</p>
          </div>
        </Popup>
      </Marker>

      {/* Lingkaran Radius */}
      <Circle
        center={[userLat, userLon]}
        radius={radius * 1000} // km → meter
        pathOptions={{
          color: '#3b82f6',
          fillColor: '#3b82f6',
          fillOpacity: 0.08,
          weight: 2,
        }}
      />

      {/* CircleMarker Perusahaan */}
      {companies.map((company) => (
        <CircleMarker
          key={company.companyId}
          center={[company.lat, company.lon]}
          radius={Math.max(8, Math.min(company.totalKuota * 3, 30))}
          pathOptions={{
            color: '#fff',
            fillColor: COLOR_MAP[company.warnaPin] || '#6b7280',
            fillOpacity: 0.9,
            weight: 2,
          }}
        >
          <Popup>
            <div className="min-w-[240px] max-w-[320px]">
              <h3 className="font-bold text-base">{company.namaPerusahaan}</h3>
              <p className="text-xs text-gray-500 mt-1">{company.alamat}</p>
              <p className="text-sm mt-2">
                <span className="font-semibold">Total Kuota:</span>{' '}
                <span className={`font-bold ${
                  company.warnaPin === 'green' ? 'text-green-600' :
                  company.warnaPin === 'orange' ? 'text-orange-600' : 'text-red-600'
                }`}>
                  {company.totalKuota}
                </span>
              </p>
              <p className="text-xs text-gray-500 mb-2">
                Jarak: {company.jarakKm.toFixed(1)} km
              </p>
              <hr className="my-2" />
              <p className="text-xs font-semibold text-gray-600 mb-1">
                Lowongan ({company.lowongan.length}):
              </p>
              <div className="max-h-[180px] overflow-y-auto space-y-2 pr-1 scrollbar-thin">
                {company.lowongan.map((job) => (
                  <div key={job.id} className="pb-2 border-b border-gray-100 last:border-0">
                    <p className="text-sm font-medium">{job.namaLowongan}</p>
                    <p className="text-xs text-gray-500">Kuota: {job.kuota}</p>
                    {job.badge && (
                      <span className="inline-block text-xs bg-yellow-100 text-yellow-800 px-2 py-0.5 rounded-full mt-1">
                        {job.badge}
                      </span>
                    )}
                    {job.tips && (
                      <p className="text-xs text-gray-400 italic mt-1">{job.tips}</p>
                    )}
                    {job.linkLamar && (
                      <a
                        href={job.linkLamar}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-xs text-blue-600 hover:underline block mt-1"
                      >
                        🔗 Lamar Sekarang
                      </a>
                    )}
                  </div>
                ))}
              </div>
              <a
                href={`https://www.google.com/maps/dir/?api=1&origin=${userLat},${userLon}&destination=${company.lat},${company.lon}`}
                target="_blank"
                rel="noopener noreferrer"
                className="mt-2 block text-center px-3 py-1.5 bg-blue-600 text-white rounded text-sm hover:bg-blue-700 transition"
              >
                🗺️ Rute ke Sana
              </a>
            </div>
          </Popup>
        </CircleMarker>
      ))}
    </MapContainer>
  );
}
