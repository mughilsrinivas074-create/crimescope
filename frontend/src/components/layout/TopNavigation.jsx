import React, { useState, useEffect } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { ShieldAlert, Activity, BarChart3, Database, Cpu, FileSpreadsheet, Server, Menu, X, User, LogOut, Key, Camera, Sparkles } from 'lucide-react';
import { useCrime } from '../../context/CrimeContext';
import api from '../../services/api';
import ApiKeyModal from '../common/ApiKeyModal';

export default function TopNavigation() {
  const { backendOnline, user, logout } = useCrime();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [keyModalOpen, setKeyModalOpen] = useState(false);
  const [keysStatus, setKeysStatus] = useState(null);
  const navigate = useNavigate();

  const fetchKeysStatus = async () => {
    try {
      const data = await api.getApiKeysStatus();
      setKeysStatus(data);
    } catch (err) {
      // Backend might be starting
    }
  };

  useEffect(() => {
    fetchKeysStatus();
    const interval = setInterval(fetchKeysStatus, 30000);
    return () => clearInterval(interval);
  }, []);

  const navLinks = [
    { to: '/', label: 'COMMAND CENTER', icon: Activity },
    { to: '/district-analytics', label: 'DISTRICT ANALYTICS', icon: BarChart3 },
    { to: '/crime-records', label: 'CRIME RECORDS', icon: Database },
    { to: '/ai-intelligence', label: 'AI INTELLIGENCE', icon: Cpu },
    { to: '/data-management', label: 'DATA MANAGEMENT', icon: FileSpreadsheet },
    { to: '/system-status', label: 'SYSTEM STATUS', icon: Server },
  ];

  const geminiValid = keysStatus?.gemini?.valid;
  const geminiConfigured = keysStatus?.gemini?.configured;

  const mapillaryValid = keysStatus?.mapillary?.valid;
  const mapillaryConfigured = keysStatus?.mapillary?.configured;

  return (
    <>
      <header className="sticky top-0 z-50 bg-[#080d1a]/95 backdrop-blur-md border-b border-[#1e293b]">
        <div className="max-w-[1920px] mx-auto px-4 py-2.5 flex items-center justify-between">
          
          {/* Brand & Logo */}
          <div className="flex items-center gap-3">
            <NavLink to="/" className="flex items-center gap-2.5 text-decoration-none group">
              <div className="relative flex items-center justify-center w-9 h-9 rounded bg-[#00f0ff]/10 border border-[#00f0ff] group-hover:shadow-[0_0_15px_rgba(0,240,255,0.4)] transition-all">
                <ShieldAlert className="w-5 h-5 text-[#00f0ff]" />
                <div className="absolute -top-0.5 -right-0.5 w-2 h-2 rounded-full bg-[#00e5a3] animate-ping" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <span className="font-display font-black text-lg tracking-wider text-white">
                    CRIMESCOPE <span className="text-[#00f0ff]">AI</span>
                  </span>
                  <span className="text-[10px] font-mono uppercase px-1.5 py-0.5 rounded bg-[#00f0ff]/15 text-[#00f0ff] border border-[#00f0ff]/30">
                    TAMIL NADU
                  </span>
                </div>
                <p className="text-[10px] font-mono text-[#94a3b8] tracking-widest hidden sm:block">
                  CRIME INTELLIGENCE COMMAND CENTER
                </p>
              </div>
            </NavLink>
          </div>

          {/* Desktop Navigation Links */}
          <nav className="hidden lg:flex items-center gap-1 xl:gap-2">
            {navLinks.map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  end={item.to === '/'}
                  className={({ isActive }) =>
                    `flex items-center gap-1.5 px-3 py-1.5 rounded text-xs font-display font-bold tracking-wider transition-all text-decoration-none ${
                      isActive
                        ? 'bg-[#00f0ff]/15 text-[#00f0ff] border border-[#00f0ff]/50 shadow-[0_0_10px_rgba(0,240,255,0.2)]'
                        : 'text-[#94a3b8] hover:text-white hover:bg-[#1e293b]/50 border border-transparent'
                    }`
                  }
                >
                  <Icon className="w-3.5 h-3.5" />
                  <span>{item.label}</span>
                </NavLink>
              );
            })}
          </nav>

          {/* Right Status & Integrations */}
          <div className="flex items-center gap-2 sm:gap-3">
            
            {/* Gemini Status Pill */}
            <button
              onClick={() => setKeyModalOpen(true)}
              className="flex items-center gap-1.5 px-2 py-1 rounded bg-[#10192e] hover:bg-[#1e293b] border border-[#1e293b] text-[10px] font-mono transition-all"
              title="Click to configure or test Google Gemini API key"
            >
              <Sparkles className="w-3 h-3 text-[#00f0ff]" />
              <span className="hidden xl:inline text-gray-300">GEMINI:</span>
              <span className={`w-1.5 h-1.5 rounded-full ${
                geminiValid ? 'bg-[#00e5a3]' : (geminiConfigured ? 'bg-[#ef4444]' : 'bg-[#f59e0b]')
              }`} />
              <span className={geminiValid ? 'text-[#00e5a3]' : (geminiConfigured ? 'text-[#ef4444]' : 'text-amber-400')}>
                {geminiValid ? 'ONLINE' : (geminiConfigured ? 'ERROR' : 'KEY REQUIRED')}
              </span>
            </button>

            {/* Mapillary Status Pill */}
            <button
              onClick={() => setKeyModalOpen(true)}
              className="flex items-center gap-1.5 px-2 py-1 rounded bg-[#10192e] hover:bg-[#1e293b] border border-[#1e293b] text-[10px] font-mono transition-all"
              title="Click to configure or test Mapillary Street-Level Imagery token"
            >
              <Camera className="w-3 h-3 text-[#00e5a3]" />
              <span className="hidden xl:inline text-gray-300">MAPILLARY:</span>
              <span className={`w-1.5 h-1.5 rounded-full ${
                mapillaryValid ? 'bg-[#00e5a3]' : (mapillaryConfigured ? 'bg-[#ef4444]' : 'bg-[#f59e0b]')
              }`} />
              <span className={mapillaryValid ? 'text-[#00e5a3]' : (mapillaryConfigured ? 'text-[#ef4444]' : 'text-amber-400')}>
                {mapillaryValid ? 'ONLINE' : (mapillaryConfigured ? 'ERROR' : 'KEY REQUIRED')}
              </span>
            </button>

            {/* API Keys Central Dialog Button */}
            <button
              onClick={() => setKeyModalOpen(true)}
              className="p-1.5 rounded bg-[#10192e] hover:bg-[#00f0ff]/15 text-gray-400 hover:text-[#00f0ff] border border-[#1e293b] transition-colors"
              title="API Credentials & Integration Command"
            >
              <Key className="w-4 h-4" />
            </button>

            {/* User Button */}
            {user ? (
              <div className="flex items-center gap-2">
                <div className="hidden md:block text-right">
                  <p className="text-xs font-display font-bold text-white">{user.fullName || user.username}</p>
                  <p className="text-[10px] font-mono text-[#00e5a3]">{user.roles?.[0] || 'ROLE_ANALYST'}</p>
                </div>
                <button
                  onClick={logout}
                  className="p-1.5 rounded bg-[#1e293b] hover:bg-[#ef4444]/20 text-[#94a3b8] hover:text-[#ef4444] border border-[#334155] transition-colors"
                  title="Sign Out"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            ) : (
              <button
                onClick={() => setKeyModalOpen(true)}
                className="cyber-btn text-xs py-1 px-2.5 flex items-center gap-1"
              >
                <Key className="w-3.5 h-3.5" />
                <span>KEYS</span>
              </button>
            )}

            {/* Mobile Menu Button */}
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="lg:hidden p-1.5 rounded bg-[#10192e] text-[#94a3b8] hover:text-white border border-[#1e293b]"
            >
              {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
            </button>
          </div>
        </div>

        {/* Mobile Drawer Menu */}
        {mobileMenuOpen && (
          <div className="lg:hidden bg-[#0a0f1d] border-b border-[#1e293b] px-4 py-3 space-y-1">
            {navLinks.map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  end={item.to === '/'}
                  onClick={() => setMobileMenuOpen(false)}
                  className={({ isActive }) =>
                    `flex items-center gap-2.5 px-3 py-2 rounded text-xs font-display font-bold tracking-wider ${
                      isActive
                        ? 'bg-[#00f0ff]/15 text-[#00f0ff] border border-[#00f0ff]/40'
                        : 'text-[#94a3b8] hover:text-white hover:bg-[#1e293b]'
                    }`
                  }
                >
                  <Icon className="w-4 h-4" />
                  <span>{item.label}</span>
                </NavLink>
              );
            })}
          </div>
        )}
      </header>

      {/* Global API Key Configuration & Test Modal */}
      <ApiKeyModal
        isOpen={keyModalOpen}
        onClose={() => setKeyModalOpen(false)}
        onRefreshStatus={fetchKeysStatus}
      />
    </>
  );
}
