import React, { useEffect, useState, useRef } from 'react';
import { MapContainer, TileLayer, GeoJSON, CircleMarker, Popup, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import { useCrime } from '../../context/CrimeContext';
import { ShieldAlert, Crosshair, AlertTriangle, Camera, X, Layers, ExternalLink } from 'lucide-react';
import MapillaryViewer from './MapillaryViewer';

// Component to handle programmatic panning when a district is selected
function MapRecenter({ center, zoom }) {
  const map = useMap();
  useEffect(() => {
    if (center) {
      map.flyTo(center, zoom, { duration: 1.2 });
    }
  }, [center, zoom, map]);
  return null;
}

export default function TamilNaduMap() {
  const {
    districts,
    selectedDistrict,
    setSelectedDistrict,
    overviewData,
    mapLayerMode,
    showBoundaries,
  } = useCrime();

  const [geoData, setGeoData] = useState(null);
  const [geoError, setGeoError] = useState(false);
  const [streetExplorerOpen, setStreetExplorerOpen] = useState(false);
  const [explorerCoords, setExplorerCoords] = useState(null);
  const geoJsonLayerRef = useRef(null);

  // CARTO basemap API configuration from environment
  const cartoApiKey = (import.meta.env.VITE_CARTO_API_KEY || '').trim();
  const cartoTileUrl = cartoApiKey
    ? `https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png?api_key=${cartoApiKey}`
    : 'https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png';

  // Load Tamil Nadu Districts GeoJSON from public directory
  useEffect(() => {
    fetch('/data/tamil-nadu-districts.geojson')
      .then((res) => {
        if (!res.ok) throw new Error('Failed to load GeoJSON');
        return res.json();
      })
      .then((data) => {
        setGeoData(data);
      })
      .catch((err) => {
        console.warn('GeoJSON boundary load error:', err);
        setGeoError(true);
      });
  }, []);

  // Compute hotspot metrics map by district name
  const hotspotMap = React.useMemo(() => {
    const map = {};
    if (overviewData?.districtHotspots) {
      for (const h of overviewData.districtHotspots) {
        map[h.districtName?.toLowerCase()] = h;
        if (h.districtCode) map[h.districtCode.toLowerCase()] = h;
      }
    }
    return map;
  }, [overviewData]);

  // Color mapping based on caseload volume & density
  const getFeatureColor = (districtName) => {
    const h = hotspotMap[districtName?.toLowerCase()];
    if (!h) return '#1e293b'; // Default muted border color

    if (mapLayerMode === 'density') {
      const density = h.densityPer100SqKm || 0;
      if (density > 150) return '#ef4444';
      if (density > 80) return '#f97316';
      if (density > 40) return '#f59e0b';
      if (density > 15) return '#00f0ff';
      return '#00e5a3';
    } else {
      // Default Choropleth based on total cases
      const cases = h.totalCases || 0;
      if (cases > 2500) return '#ef4444';
      if (cases > 1800) return '#f97316';
      if (cases > 1200) return '#f59e0b';
      if (cases > 600) return '#00f0ff';
      return '#00e5a3';
    }
  };

  // GeoJSON styling
  const styleFeature = (feature) => {
    const distName = feature.properties?.district || feature.properties?.name;
    const isSelected = selectedDistrict && selectedDistrict.name.toLowerCase() === distName?.toLowerCase();
    const color = getFeatureColor(distName);

    return {
      fillColor: color,
      weight: isSelected ? 3 : 1.2,
      opacity: 0.9,
      color: isSelected ? '#ffffff' : '#00f0ff',
      dashArray: isSelected ? '' : '2',
      fillOpacity: isSelected ? 0.75 : 0.45,
    };
  };

  const openStreetView = (lat, lng, name) => {
    setExplorerCoords({
      latitude: lat,
      longitude: lng,
      locationName: name,
    });
    setStreetExplorerOpen(true);
  };

  // Interactive callbacks on each polygon
  const onEachFeature = (feature, layer) => {
    const distName = feature.properties?.district || feature.properties?.name;
    const distMatch = districts.find(
      (d) => d.name.toLowerCase() === distName?.toLowerCase()
    );

    layer.on({
      mouseover: (e) => {
        const target = e.target;
        target.setStyle({
          weight: 3,
          color: '#ffffff',
          fillOpacity: 0.75,
        });
        target.bringToFront();
      },
      mouseout: (e) => {
        if (geoJsonLayerRef.current) {
          geoJsonLayerRef.current.resetStyle(e.target);
        }
      },
      click: () => {
        if (distMatch) {
          setSelectedDistrict(distMatch);
        }
      },
    });

    const h = hotspotMap[distName?.toLowerCase()];
    const cases = h ? h.totalCases : 'Calculating...';
    const threat = h ? h.threatLabel : 'MONITORED';

    layer.bindPopup(`
      <div style="font-family: 'Segoe UI', sans-serif; font-size: 12px; color: #f1f5f9; padding: 4px; min-width: 180px;">
        <div style="font-size: 14px; font-weight: bold; color: #00f0ff; margin-bottom: 2px;">
          ${distName}
        </div>
        <div style="color: #94a3b8; font-size: 11px; margin-bottom: 6px;">
          ${distMatch ? distMatch.headquarters + ' • ' + distMatch.zone + ' Zone' : 'Tamil Nadu District'}
        </div>
        <div style="background: rgba(16,25,46,0.9); padding: 6px; border-radius: 4px; border: 1px solid #1e293b; margin-bottom: 6px;">
          <div style="display: flex; justify-content: space-between; margin-bottom: 4px;">
            <span style="color: #94a3b8;">Recorded Cases:</span>
            <span style="font-family: monospace; font-weight: bold; color: #ffffff;">${cases}</span>
          </div>
          <div style="display: flex; justify-content: space-between;">
            <span style="color: #94a3b8;">Threat Status:</span>
            <span style="font-family: monospace; font-weight: bold; color: #00e5a3;">${threat}</span>
          </div>
        </div>
        <div style="font-size: 10px; color: #00f0ff; text-align: center; cursor: pointer;">
          ▶ CLICK POLYGON TO FOCUS JURISDICTION
        </div>
      </div>
    `);
  };

  // Determine current map center
  const mapCenter = selectedDistrict && selectedDistrict.centerLat
    ? [parseFloat(selectedDistrict.centerLat), parseFloat(selectedDistrict.centerLng)]
    : [11.1271, 78.6569];

  const mapZoom = selectedDistrict ? 9 : 7;

  return (
    <div className="cyber-panel relative w-full h-full min-h-[480px] flex flex-col overflow-hidden">
      
      {/* Map Control HUD Header */}
      <div className="absolute top-3 left-3 z-[1000] flex items-center gap-2 bg-[#0a0e1c]/90 backdrop-blur-md px-3 py-1.5 rounded border border-[#1e293b]">
        <Crosshair className="w-4 h-4 text-[#00f0ff] animate-pulse" />
        <span className="text-xs font-display font-bold text-white tracking-wider">
          GEOSPATIAL COMMAND GRID
        </span>
        <span className="text-[10px] font-mono text-[#00e5a3] px-1.5 py-0.5 rounded bg-[#00e5a3]/10 border border-[#00e5a3]/30">
          MODE: {mapLayerMode.toUpperCase()}
        </span>
        <span className={`text-[10px] font-mono px-1.5 py-0.5 rounded border hidden sm:inline-block ${
          cartoApiKey
            ? 'text-[#00f0ff] bg-[#00f0ff]/10 border-[#00f0ff]/30'
            : 'text-gray-400 bg-gray-800/40 border-gray-700'
        }`}>
          CARTO: {cartoApiKey ? 'API KEY ACTIVE' : 'DARK MATTER'}
        </span>
      </div>

      {/* Mapillary Street Explorer & Recenter Buttons in Top Right */}
      <div className="absolute top-3 right-3 z-[1000] flex items-center gap-2">
        {selectedDistrict && selectedDistrict.centerLat && (
          <button
            onClick={() => openStreetView(
              parseFloat(selectedDistrict.centerLat),
              parseFloat(selectedDistrict.centerLng),
              `${selectedDistrict.name} HQ (${selectedDistrict.headquarters})`
            )}
            className="cyber-btn text-[11px] py-1 px-2.5 bg-[#0a0e1c]/90 backdrop-blur-md flex items-center gap-1.5 text-[#00e5a3] border-[#00e5a3]/40"
            title="Explore Mapillary Street-Level Imagery near this district"
          >
            <Camera className="w-3.5 h-3.5" />
            STREET SURVEY
          </button>
        )}

        {selectedDistrict && (
          <button
            onClick={() => setSelectedDistrict(null)}
            className="cyber-btn text-[11px] py-1 px-2.5 bg-[#0a0e1c]/90 backdrop-blur-md"
          >
            RESET TO STATEWIDE VIEW
          </button>
        )}
      </div>

      {/* Leaflet Map Container */}
      <div className="flex-1 w-full h-full">
        <MapContainer
          center={mapCenter}
          zoom={mapZoom}
          scrollWheelZoom={true}
          style={{ width: '100%', height: '100%' }}
        >
          <MapRecenter center={mapCenter} zoom={mapZoom} />

          {/* Dark Cyber Map Tiles (CartoDB Dark Matter with optional CARTO API key) */}
          <TileLayer
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> &copy; <a href="https://carto.com/">CARTO</a>'
            url={cartoTileUrl}
          />

          {/* GeoJSON District Boundaries Layer */}
          {showBoundaries && geoData && (
            <GeoJSON
              ref={geoJsonLayerRef}
              data={geoData}
              style={styleFeature}
              onEachFeature={onEachFeature}
            />
          )}

          {/* Incident / Hotspot Circles for Verified Coordinates */}
          {mapLayerMode === 'markers' && overviewData?.districtHotspots && (
            <>
              {overviewData.districtHotspots.map((h) => {
                if (!h.centerLat || !h.centerLng) return null;
                const lat = parseFloat(h.centerLat);
                const lng = parseFloat(h.centerLng);
                const color = h.threatColor || '#00f0ff';

                return (
                  <CircleMarker
                    key={h.districtId}
                    center={[lat, lng]}
                    radius={Math.min(22, Math.max(6, Math.sqrt(h.totalCases || 10) * 0.35))}
                    pathOptions={{
                      color: color,
                      fillColor: color,
                      fillOpacity: 0.6,
                      weight: 2,
                    }}
                  >
                    <Popup>
                      <div className="p-1 text-xs min-w-[170px]">
                        <strong className="text-[#00f0ff]">{h.districtName}</strong>
                        <p className="text-gray-300">Total Cases: {h.totalCases}</p>
                        <p className="text-emerald-400">Threat: {h.threatLabel}</p>
                        
                        <div className="pt-2 mt-2 border-t border-gray-700">
                          <button
                            onClick={() => openStreetView(lat, lng, `${h.districtName} (${h.districtCode})`)}
                            className="w-full text-[10px] font-mono py-1 px-2 rounded bg-[#00e5a3]/20 hover:bg-[#00e5a3]/30 text-[#00e5a3] border border-[#00e5a3]/40 flex items-center justify-center gap-1 transition-all"
                          >
                            <Camera className="w-3 h-3" />
                            VIEW STREET SURVEY (MAPILLARY)
                          </button>
                        </div>
                      </div>
                    </Popup>
                  </CircleMarker>
                );
              })}
            </>
          )}
        </MapContainer>
      </div>

      {/* Floating Mapillary Street View Inspector Overlay */}
      {streetExplorerOpen && explorerCoords && (
        <div className="absolute bottom-4 right-4 z-[1100] w-full max-w-md animate-fadeIn">
          <div className="relative">
            <button
              onClick={() => setStreetExplorerOpen(false)}
              className="absolute -top-3 -right-3 z-20 p-1 rounded-full bg-[#ef4444] text-white hover:scale-110 shadow-lg transition-transform"
              title="Close Street Explorer"
            >
              <X className="w-4 h-4" />
            </button>
            <MapillaryViewer
              latitude={explorerCoords.latitude}
              longitude={explorerCoords.longitude}
              locationName={explorerCoords.locationName}
            />
          </div>
        </div>
      )}

      {/* Map Legend Overlay */}
      <div className="absolute bottom-3 left-3 z-[1000] bg-[#0a0e1c]/90 backdrop-blur-md p-2.5 rounded border border-[#1e293b] text-[10px] font-mono space-y-1">
        <div className="text-[10px] font-display font-bold text-[#94a3b8] mb-1">
          {mapLayerMode === 'density' ? 'CASE DENSITY (PER 100 SQ KM)' : 'RECORDED CASE VOLUME'}
        </div>
        <div className="flex items-center gap-2">
          <div className="w-2.5 h-2.5 rounded-sm bg-[#00e5a3]" />
          <span className="text-gray-300">&lt; 600 Cases (Nominal Activity)</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-2.5 h-2.5 rounded-sm bg-[#00f0ff]" />
          <span className="text-gray-300">600 - 1,200 Cases (Moderate Watch)</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-2.5 h-2.5 rounded-sm bg-[#f59e0b]" />
          <span className="text-gray-300">1,200 - 1,800 Cases (Elevated Caution)</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-2.5 h-2.5 rounded-sm bg-[#ef4444]" />
          <span className="text-gray-300">&gt; 1,800 Cases (Critical Response)</span>
        </div>
      </div>

      {/* GeoJSON Fallback Notice */}
      {geoError && (
        <div className="absolute top-12 left-3 right-3 z-[1000] bg-[#ef4444]/20 border border-[#ef4444] p-2 rounded flex items-center gap-2 text-xs text-[#ef4444]">
          <AlertTriangle className="w-4 h-4 shrink-0" />
          <span>Notice: Vector district boundaries loading fallback. Standard interactive district selection active.</span>
        </div>
      )}

    </div>
  );
}
