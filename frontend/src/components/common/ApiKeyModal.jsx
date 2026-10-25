import React, { useState, useEffect } from 'react';
import { Key, ShieldCheck, AlertCircle, ExternalLink, CheckCircle2, RefreshCw, X, Map, Layers } from 'lucide-react';
import api from '../../services/api';

export default function ApiKeyModal({ isOpen, onClose, onRefreshStatus }) {
  const [keysStatus, setKeysStatus] = useState(null);
  const [loading, setLoading] = useState(false);

  // Gemini test state
  const [geminiInput, setGeminiInput] = useState('');
  const [geminiTesting, setGeminiTesting] = useState(false);
  const [geminiResult, setGeminiResult] = useState(null);

  // Mapillary test state
  const [mapillaryInput, setMapillaryInput] = useState('');
  const [mapillaryTesting, setMapillaryTesting] = useState(false);
  const [mapillaryResult, setMapillaryResult] = useState(null);

  const fetchStatus = async () => {
    setLoading(true);
    try {
      const data = await api.getApiKeysStatus();
      setKeysStatus(data);
    } catch (err) {
      console.warn('Failed to fetch keys status:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen) {
      fetchStatus();
      setGeminiResult(null);
      setMapillaryResult(null);
    }
  }, [isOpen]);

  const testGeminiKey = async () => {
    if (!geminiInput.trim()) return;
    setGeminiTesting(true);
    setGeminiResult(null);
    try {
      const res = await api.validateGeminiKey(geminiInput.trim());
      setGeminiResult(res);
    } catch (err) {
      setGeminiResult({ valid: false, message: err.message || 'Validation failed' });
    } finally {
      setGeminiTesting(false);
    }
  };

  const testMapillaryToken = async () => {
    if (!mapillaryInput.trim()) return;
    setMapillaryTesting(true);
    setMapillaryResult(null);
    try {
      const res = await api.validateMapillaryToken(mapillaryInput.trim());
      setMapillaryResult(res);
    } catch (err) {
      setMapillaryResult({ valid: false, message: err.message || 'Validation failed' });
    } finally {
      setMapillaryTesting(false);
    }
  };

  if (!isOpen) return null;

  const gemini = keysStatus?.gemini || {};
  const mapillary = keysStatus?.mapillary || {};
  const cartoApiKey = (import.meta.env.VITE_CARTO_API_KEY || '').trim();

  return (
    <div className="fixed inset-0 z-[2000] flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fadeIn">
      <div className="cyber-panel w-full max-w-2xl max-h-[90vh] overflow-y-auto p-6 bg-[#0a0e1c] border border-[#00f0ff]/40 shadow-[0_0_50px_rgba(0,240,255,0.15)] rounded-lg">
        
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-[#1e293b] mb-5">
          <div className="flex items-center gap-3">
            <div className="p-2 rounded bg-[#00f0ff]/10 border border-[#00f0ff]/30 text-[#00f0ff]">
              <Key className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-display font-bold text-white tracking-wide flex items-center gap-2">
                API CREDENTIALS & INTEGRATION COMMAND
              </h2>
              <p className="text-xs text-gray-400">
                Secure runtime configuration for Google Gemini AI, Mapillary Street Explorer & CARTO Basemap Grid
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded text-gray-400 hover:text-white hover:bg-white/10 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Security Assurance Banner */}
        <div className="p-3 mb-5 rounded bg-emerald-950/30 border border-emerald-500/30 flex items-start gap-2.5 text-xs text-emerald-300">
          <ShieldCheck className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
          <div>
            <strong className="text-emerald-300">Strict Secret Protection:</strong> Keys are maintained exclusively in your local root <code className="text-white bg-black/40 px-1 py-0.5 rounded">.env</code> file (isolated by <code className="text-white bg-black/40 px-1 py-0.5 rounded">.gitignore</code>). Gemini and Mapillary requests route safely and are never exposed in public repositories.
          </div>
        </div>

        {/* 1. Google Gemini API Status Card */}
        <div className="p-4 mb-4 rounded bg-[#0f172a]/70 border border-[#1e293b] space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full animate-pulse"
                style={{ backgroundColor: gemini.valid ? '#00e5a3' : (gemini.configured ? '#ef4444' : '#f59e0b') }}
              />
              <h3 className="text-sm font-display font-bold text-white">GOOGLE GEMINI API (AI ANALYSIS)</h3>
            </div>
            <span className={`text-[10px] font-mono font-bold px-2 py-0.5 rounded border ${
              gemini.valid ? 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30' :
              (gemini.configured ? 'text-red-400 bg-red-500/10 border-red-500/30' : 'text-amber-400 bg-amber-500/10 border-amber-500/30')
            }`}>
              {gemini.status || (gemini.valid ? 'OPERATIONAL' : 'KEY MISSING')}
            </span>
          </div>

          <div className="text-xs text-gray-300 space-y-1">
            <p className="text-[11px] text-gray-400">{gemini.message}</p>
            <div className="flex items-center justify-between text-[11px] font-mono pt-1 text-gray-400">
              <span>Model: <strong className="text-[#00f0ff]">{gemini.model || 'gemini-2.5-flash'}</strong></span>
              <span>Active Key: <strong className="text-white">{gemini.keyMasked || 'None Configured'}</strong></span>
            </div>
          </div>

          {/* Test Candidate Key */}
          <div className="pt-2 border-t border-[#1e293b]/60">
            <label className="block text-[11px] font-mono text-gray-400 mb-1">
              Verify Candidate Gemini Key:
            </label>
            <div className="flex gap-2">
              <input
                type="password"
                placeholder="AIzaSy..."
                value={geminiInput}
                onChange={(e) => setGeminiInput(e.target.value)}
                className="flex-1 bg-[#0a0e1c] border border-[#1e293b] focus:border-[#00f0ff] px-2.5 py-1 text-xs text-white rounded font-mono outline-none"
              />
              <button
                onClick={testGeminiKey}
                disabled={geminiTesting || !geminiInput.trim()}
                className="cyber-btn text-xs py-1 px-3 flex items-center gap-1.5 shrink-0"
              >
                {geminiTesting ? <RefreshCw className="w-3.5 h-3.5 animate-spin" /> : 'TEST KEY'}
              </button>
            </div>

            {geminiResult && (
              <div className={`mt-2 p-2 rounded text-[11px] flex items-center gap-2 ${
                geminiResult.valid ? 'bg-emerald-950/40 text-emerald-300 border border-emerald-500/30' : 'bg-red-950/40 text-red-300 border border-red-500/30'
              }`}>
                {geminiResult.valid ? <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" /> : <AlertCircle className="w-4 h-4 text-red-400 shrink-0" />}
                <span>{geminiResult.message}</span>
              </div>
            )}
          </div>
        </div>

        {/* 2. Mapillary API Status Card */}
        <div className="p-4 mb-4 rounded bg-[#0f172a]/70 border border-[#1e293b] space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full animate-pulse"
                style={{ backgroundColor: mapillary.valid ? '#00e5a3' : (mapillary.configured ? '#ef4444' : '#f59e0b') }}
              />
              <h3 className="text-sm font-display font-bold text-white">MAPILLARY GRAPH API (STREET EXPLORER)</h3>
            </div>
            <span className={`text-[10px] font-mono font-bold px-2 py-0.5 rounded border ${
              mapillary.valid ? 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30' :
              (mapillary.configured ? 'text-red-400 bg-red-500/10 border-red-500/30' : 'text-amber-400 bg-amber-500/10 border-amber-500/30')
            }`}>
              {mapillary.status || (mapillary.valid ? 'OPERATIONAL' : 'TOKEN MISSING')}
            </span>
          </div>

          <div className="text-xs text-gray-300 space-y-1">
            <p className="text-[11px] text-gray-400">{mapillary.message}</p>
            <div className="flex items-center justify-between text-[11px] font-mono pt-1 text-gray-400">
              <span>Features: <strong className="text-[#00e5a3]">Street Photos, Vector Survey Grid</strong></span>
              <span>Active Token: <strong className="text-white">{mapillary.tokenMasked || 'None Configured'}</strong></span>
            </div>
          </div>

          {/* Test Candidate Token */}
          <div className="pt-2 border-t border-[#1e293b]/60">
            <label className="block text-[11px] font-mono text-gray-400 mb-1">
              Verify Candidate Mapillary Token:
            </label>
            <div className="flex gap-2">
              <input
                type="password"
                placeholder="MLY|..."
                value={mapillaryInput}
                onChange={(e) => setMapillaryInput(e.target.value)}
                className="flex-1 bg-[#0a0e1c] border border-[#1e293b] focus:border-[#00e5a3] px-2.5 py-1 text-xs text-white rounded font-mono outline-none"
              />
              <button
                onClick={testMapillaryToken}
                disabled={mapillaryTesting || !mapillaryInput.trim()}
                className="cyber-btn text-xs py-1 px-3 flex items-center gap-1.5 shrink-0"
              >
                {mapillaryTesting ? <RefreshCw className="w-3.5 h-3.5 animate-spin" /> : 'TEST TOKEN'}
              </button>
            </div>

            {mapillaryResult && (
              <div className={`mt-2 p-2 rounded text-[11px] flex items-center gap-2 ${
                mapillaryResult.valid ? 'bg-emerald-950/40 text-emerald-300 border border-emerald-500/30' : 'bg-red-950/40 text-red-300 border border-red-500/30'
              }`}>
                {mapillaryResult.valid ? <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" /> : <AlertCircle className="w-4 h-4 text-red-400 shrink-0" />}
                <span>{mapillaryResult.message}</span>
              </div>
            )}
          </div>
        </div>

        {/* 3. CARTO Basemap API Status Card */}
        <div className="p-4 mb-5 rounded bg-[#0f172a]/70 border border-[#1e293b] space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full animate-pulse"
                style={{ backgroundColor: cartoApiKey ? '#00e5a3' : '#00f0ff' }}
              />
              <h3 className="text-sm font-display font-bold text-white">CARTO BASEMAP GRID (COMMAND CENTER MAP)</h3>
            </div>
            <span className={`text-[10px] font-mono font-bold px-2 py-0.5 rounded border ${
              cartoApiKey ? 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30' : 'text-[#00f0ff] bg-[#00f0ff]/10 border-[#00f0ff]/30'
            }`}>
              {cartoApiKey ? 'API KEY LINKED' : 'DARK MATTER (ACTIVE)'}
            </span>
          </div>

          <div className="text-xs text-gray-300 space-y-1">
            <p className="text-[11px] text-gray-400">
              {cartoApiKey
                ? 'High-speed authenticated CARTO tile server pipeline active for the interactive Tamil Nadu command center map view.'
                : 'Using standard CartoDB Dark Matter basemap tiles. Provide CARTO_API_KEY / VITE_CARTO_API_KEY in .env for custom basemap quotas and styles.'}
            </p>
            <div className="flex items-center justify-between text-[11px] font-mono pt-1 text-gray-400">
              <span>Theme: <strong className="text-[#00f0ff]">CartoDB Dark Matter Cyber Grid</strong></span>
              <span>Variable: <code className="text-[#00e5a3]">VITE_CARTO_API_KEY</code></span>
            </div>
          </div>
        </div>

        {/* Step-by-Step Configuration Guide */}
        <div className="p-3 rounded bg-blue-950/20 border border-blue-500/20 text-xs text-blue-300 space-y-2 mb-4">
          <div className="font-display font-bold text-white flex items-center gap-2">
            <span>HOW TO SET YOUR KEYS PERMANENTLY</span>
          </div>
          <ol className="list-decimal list-inside space-y-1 text-gray-300 text-[11px]">
            <li>Open the project root directory: <code className="text-[#00f0ff] bg-black/40 px-1 py-0.5 rounded">crimescope-ai</code></li>
            <li>Create or edit your local <code className="text-[#00f0ff] bg-black/40 px-1 py-0.5 rounded">.env</code> file (copied from <code className="text-gray-400">.env.example</code>).</li>
            <li>Paste your tokens:
              <pre className="bg-black/60 p-2 rounded mt-1 text-[11px] text-emerald-400 font-mono overflow-x-auto">
GEMINI_API_KEY=AIzaSyYourGeminiApiKeyHere
MAPILLARY_CLIENT_TOKEN=MLY|YourMapillaryClientTokenHere
CARTO_API_KEY=your_carto_basemap_api_key_here
VITE_CARTO_API_KEY=your_carto_basemap_api_key_here
              </pre>
            </li>
            <li>The Command Center map immediately connects your authenticated CARTO basemap tiles with 38-district polygon boundaries.</li>
          </ol>
        </div>

        {/* Footer Actions */}
        <div className="flex items-center justify-between pt-3 border-t border-[#1e293b]">
          <div className="flex items-center gap-3">
            <a
              href="https://aistudio.google.com/"
              target="_blank"
              rel="noreferrer"
              className="text-[11px] text-[#00f0ff] hover:underline flex items-center gap-1"
            >
              Gemini Key <ExternalLink className="w-3 h-3" />
            </a>
            <span className="text-gray-600">•</span>
            <a
              href="https://www.mapillary.com/dashboard/developers"
              target="_blank"
              rel="noreferrer"
              className="text-[11px] text-[#00e5a3] hover:underline flex items-center gap-1"
            >
              Mapillary Token <ExternalLink className="w-3 h-3" />
            </a>
            <span className="text-gray-600">•</span>
            <a
              href="https://carto.com/"
              target="_blank"
              rel="noreferrer"
              className="text-[11px] text-amber-400 hover:underline flex items-center gap-1"
            >
              CARTO Key <ExternalLink className="w-3 h-3" />
            </a>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={fetchStatus}
              disabled={loading}
              className="cyber-btn text-xs py-1.5 px-3 flex items-center gap-1 bg-[#1e293b]"
            >
              <RefreshCw className={`w-3 h-3 ${loading ? 'animate-spin' : ''}`} />
              REFRESH
            </button>
            <button
              onClick={onClose}
              className="cyber-btn text-xs py-1.5 px-4"
            >
              DISMISS
            </button>
          </div>
        </div>

      </div>
    </div>
  );
}
