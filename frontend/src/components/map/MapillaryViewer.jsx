import React, { useState, useEffect } from 'react';
import { Camera, Compass, Calendar, MapPin, ExternalLink, RefreshCw, AlertTriangle, Key, Maximize2 } from 'lucide-react';
import api from '../../services/api';

export default function MapillaryViewer({ latitude, longitude, locationName, incidentTitle, onOpenKeyModal }) {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState(null);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [fullscreen, setFullscreen] = useState(false);

  const fetchImagery = async () => {
    if (!latitude || !longitude) return;
    setLoading(true);
    try {
      const res = await api.getMapillaryImageryNear(latitude, longitude, 1500);
      setData(res);
      setSelectedIndex(0);
    } catch (err) {
      setData({ status: 'ERROR', message: err.message, images: [] });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchImagery();
  }, [latitude, longitude]);

  if (!latitude || !longitude) {
    return (
      <div className="p-4 rounded bg-[#0a0e1c] border border-[#1e293b] text-center text-xs text-gray-400">
        <MapPin className="w-5 h-5 mx-auto mb-1 text-gray-500" />
        No geographic coordinates available for street-level exploration.
      </div>
    );
  }

  // 1. Token Missing State
  if (data?.status === 'TOKEN_MISSING') {
    return (
      <div className="p-4 rounded bg-[#0f172a] border border-[#f59e0b]/40 text-left space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2 text-amber-400 font-display font-bold text-xs tracking-wide">
            <Camera className="w-4 h-4" />
            <span>MAPILLARY STREET EXPLORER OFFLINE</span>
          </div>
          <span className="text-[10px] font-mono text-amber-400 px-1.5 py-0.5 rounded bg-amber-500/10 border border-amber-500/30">
            TOKEN REQUIRED
          </span>
        </div>

        <p className="text-xs text-gray-300">
          Street-level photographic surveying for <strong className="text-white">{locationName || 'Incident Coordinates'}</strong> ({parseFloat(latitude).toFixed(4)}, {parseFloat(longitude).toFixed(4)}) requires a Mapillary Access Token.
        </p>

        <div className="flex items-center justify-between pt-2 border-t border-[#1e293b]">
          <span className="text-[11px] text-gray-400 font-mono">
            Config: <code className="text-[#00e5a3]">MAPILLARY_CLIENT_TOKEN</code> in <code className="text-white">.env</code>
          </span>
          {onOpenKeyModal && (
            <button
              onClick={onOpenKeyModal}
              className="cyber-btn text-[11px] py-1 px-3 flex items-center gap-1.5"
            >
              <Key className="w-3.5 h-3.5" />
              CONFIGURE KEY
            </button>
          )}
        </div>
      </div>
    );
  }

  // 2. Token Invalid or Error State
  if (data?.status === 'TOKEN_INVALID' || data?.status === 'API_ERROR' || data?.status === 'ERROR') {
    return (
      <div className="p-4 rounded bg-[#ef4444]/10 border border-[#ef4444]/40 text-left space-y-2">
        <div className="flex items-center gap-2 text-red-400 font-display font-bold text-xs">
          <AlertTriangle className="w-4 h-4" />
          <span>MAPILLARY AUTHENTICATION ERROR</span>
        </div>
        <p className="text-xs text-red-300">
          {data.message || 'Authorization failed with Mapillary Graph API. Please verify your client token in .env.'}
        </p>
        {onOpenKeyModal && (
          <button
            onClick={onOpenKeyModal}
            className="cyber-btn text-[11px] py-1 px-2.5 mt-1"
          >
            UPDATE TOKEN
          </button>
        )}
      </div>
    );
  }

  // 3. Loading State
  if (loading) {
    return (
      <div className="p-6 rounded bg-[#0a0e1c] border border-[#1e293b] text-center text-xs text-gray-400 space-y-2">
        <RefreshCw className="w-5 h-5 mx-auto animate-spin text-[#00e5a3]" />
        <p>Scanning Mapillary street-level photographic coverage...</p>
      </div>
    );
  }

  const images = data?.images || [];
  const currentImage = images[selectedIndex];

  // 4. No Coverage Found State (Strict factual reporting - no hallucinated photos)
  if (images.length === 0) {
    return (
      <div className="p-4 rounded bg-[#0a0e1c] border border-[#1e293b] text-left space-y-2">
        <div className="flex items-center justify-between text-xs font-display font-bold text-gray-300">
          <div className="flex items-center gap-2 text-[#00f0ff]">
            <Camera className="w-4 h-4" />
            <span>MAPILLARY PHOTOGRAPHIC SURVEY</span>
          </div>
          <span className="text-[10px] font-mono text-gray-400">STATUS: NO SURVEYS IN RADIUS</span>
        </div>
        <p className="text-xs text-gray-400">
          No street-level sequence coverage is currently recorded by Mapillary contributors within 1,500m of coordinates:
          <span className="font-mono text-white ml-1">Lat: {parseFloat(latitude).toFixed(5)}, Lng: {parseFloat(longitude).toFixed(5)}</span>.
        </p>
        <p className="text-[11px] text-gray-500 italic">
          Location is verified in police jurisdiction records; photographic coverage relies on official or crowdsourced street surveys.
        </p>
      </div>
    );
  }

  // 5. Active Mapillary Street View Display
  return (
    <div className="rounded bg-[#0a0e1c] border border-[#00e5a3]/40 overflow-hidden shadow-lg">
      
      {/* Header bar */}
      <div className="px-3 py-2 bg-[#0f172a] border-b border-[#1e293b] flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Camera className="w-4 h-4 text-[#00e5a3]" />
          <span className="text-xs font-display font-bold text-white tracking-wide">
            MAPILLARY STREET-LEVEL SURVEY
          </span>
          <span className="text-[10px] font-mono text-emerald-400 px-1.5 py-0.2 rounded bg-emerald-500/10 border border-emerald-500/30">
            {images.length} SURVEY{images.length > 1 ? 'S' : ''} FOUND
          </span>
        </div>

        <div className="flex items-center gap-2">
          {currentImage?.viewerUrl && (
            <a
              href={currentImage.viewerUrl}
              target="_blank"
              rel="noreferrer"
              className="text-[11px] text-[#00e5a3] hover:underline flex items-center gap-1 font-mono"
            >
              Open 360 Viewer <ExternalLink className="w-3 h-3" />
            </a>
          )}
        </div>
      </div>

      {/* Main Street Image Frame */}
      <div className="relative aspect-video w-full bg-black flex items-center justify-center overflow-hidden group">
        {currentImage?.thumbUrl ? (
          <img
            src={currentImage.thumbUrl}
            alt={`Mapillary street view near ${locationName || 'Incident'}`}
            className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-[1.02]"
          />
        ) : (
          <div className="text-xs text-gray-500">Image thumbnail unavailable</div>
        )}

        {/* Proximity & Orientation HUD Overlay */}
        <div className="absolute top-2 left-2 z-10 flex items-center gap-2 bg-black/75 backdrop-blur-md px-2 py-1 rounded border border-white/10 text-[10px] font-mono text-white">
          <Compass className="w-3.5 h-3.5 text-[#00e5a3]" style={{ transform: `rotate(${currentImage?.compassAngle || 0}deg)` }} />
          <span>BEARING: {Math.round(currentImage?.compassAngle || 0)}°</span>
          <span className="text-gray-400">|</span>
          <span>DIST: ~{currentImage?.distanceMeters || 0}m</span>
        </div>

        {/* Capture Date HUD Overlay */}
        <div className="absolute top-2 right-2 z-10 flex items-center gap-1.5 bg-black/75 backdrop-blur-md px-2 py-1 rounded border border-white/10 text-[10px] font-mono text-gray-300">
          <Calendar className="w-3 h-3 text-[#00f0ff]" />
          <span>{currentImage?.capturedAt ? new Date(currentImage.capturedAt).toLocaleDateString() : 'Recorded Survey'}</span>
        </div>

        {/* Coordinate Subtitle */}
        <div className="absolute bottom-2 left-2 right-2 z-10 flex items-center justify-between bg-black/80 backdrop-blur-md px-2.5 py-1 rounded border border-white/10 text-[10px] font-mono">
          <span className="text-gray-300 truncate">
            {locationName || incidentTitle || 'Incident Jurisdiction'}
          </span>
          <span className="text-emerald-400 shrink-0">
            Lat: {currentImage?.latitude?.toFixed(5)}, Lng: {currentImage?.longitude?.toFixed(5)}
          </span>
        </div>
      </div>

      {/* Multi-Photo Sequence Filmstrip (if > 1 image found) */}
      {images.length > 1 && (
        <div className="p-2 bg-[#090d16] border-t border-[#1e293b] flex items-center gap-2 overflow-x-auto">
          <span className="text-[10px] font-mono text-gray-500 uppercase shrink-0">Photos:</span>
          {images.map((img, idx) => (
            <button
              key={img.id || idx}
              onClick={() => setSelectedIndex(idx)}
              className={`relative shrink-0 w-14 h-9 rounded overflow-hidden border transition-all ${
                idx === selectedIndex
                  ? 'border-[#00e5a3] ring-1 ring-[#00e5a3] scale-105'
                  : 'border-transparent opacity-60 hover:opacity-100'
              }`}
            >
              <img
                src={img.thumbSmallUrl || img.thumbUrl}
                alt={`Survey photo ${idx + 1}`}
                className="w-full h-full object-cover"
              />
              <span className="absolute bottom-0 right-0 bg-black/80 px-1 text-[8px] font-mono text-white">
                #{idx + 1}
              </span>
            </button>
          ))}
        </div>
      )}

      {/* Attribution footer */}
      <div className="px-3 py-1.5 bg-[#0a0e1c] text-[10px] font-mono text-gray-500 flex items-center justify-between border-t border-[#1e293b]/50">
        <span>Attribution: © Mapillary and contributors (ODbL)</span>
        <button
          onClick={fetchImagery}
          className="text-gray-400 hover:text-white flex items-center gap-1 transition-colors"
        >
          <RefreshCw className="w-2.5 h-2.5" /> Re-scan
        </button>
      </div>

    </div>
  );
}
