import React, { useState } from 'react';
import { Search, MapPin, Filter, Calendar, Layers, RotateCcw, AlertTriangle } from 'lucide-react';
import { useCrime } from '../../context/CrimeContext';

export default function LeftFilterPanel() {
  const {
    districts,
    categories,
    selectedDistrict,
    setSelectedDistrict,
    selectedCategory,
    setSelectedCategory,
    yearRange,
    setYearRange,
    includeDemo,
    setIncludeDemo,
    mapLayerMode,
    setMapLayerMode,
    showBoundaries,
    setShowBoundaries,
  } = useCrime();

  const [searchTerm, setSearchTerm] = useState('');
  const [selectedZone, setSelectedZone] = useState('ALL');

  const zones = ['ALL', 'NORTH', 'CENTRAL', 'SOUTH', 'WEST'];

  const filteredDistricts = districts.filter((d) => {
    const matchesSearch = d.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          d.code.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesZone = selectedZone === 'ALL' || (d.zone && d.zone.toUpperCase() === selectedZone);
    return matchesSearch && matchesZone;
  });

  const handleReset = () => {
    setSelectedDistrict(null);
    setSelectedCategory(null);
    setYearRange([2018, 2023]);
    setIncludeDemo(false);
    setSearchTerm('');
    setSelectedZone('ALL');
  };

  return (
    <aside className="cyber-panel p-3.5 flex flex-col gap-4 h-full overflow-y-auto">
      
      {/* Panel Header */}
      <div className="flex items-center justify-between border-b border-[#1e293b] pb-2.5">
        <div className="flex items-center gap-2">
          <Filter className="w-4 h-4 text-[#00f0ff]" />
          <h2 className="text-xs font-display font-bold text-white tracking-wider">COMMAND FILTERS</h2>
        </div>
        <button
          onClick={handleReset}
          className="flex items-center gap-1 text-[10px] font-mono text-[#94a3b8] hover:text-[#00f0ff] transition-colors"
          title="Reset All Filters"
        >
          <RotateCcw className="w-3 h-3" />
          <span>RESET</span>
        </button>
      </div>

      {/* 1. District Selector with Search & Zone Tabs */}
      <div className="space-y-2">
        <label className="text-[11px] font-display font-bold text-[#94a3b8] flex items-center justify-between">
          <span>JURISDICTION / DISTRICT</span>
          <span className="text-[10px] font-mono text-[#00f0ff]">
            {selectedDistrict ? selectedDistrict.name : 'ALL (STATEWIDE)'}
          </span>
        </label>

        {/* Search input */}
        <div className="relative">
          <Search className="w-3.5 h-3.5 absolute left-2.5 top-2.5 text-[#64748b]" />
          <input
            type="text"
            placeholder="Search 38 TN Districts..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="cyber-input pl-8 text-xs py-1.5"
          />
        </div>

        {/* Zone Filter Chips */}
        <div className="flex gap-1 overflow-x-auto py-1">
          {zones.map((zone) => (
            <button
              key={zone}
              onClick={() => setSelectedZone(zone)}
              className={`text-[9px] font-mono px-2 py-0.5 rounded transition-all whitespace-nowrap ${
                selectedZone === zone
                  ? 'bg-[#00f0ff]/20 text-[#00f0ff] border border-[#00f0ff]/50'
                  : 'bg-[#10192e] text-[#64748b] hover:text-[#94a3b8] border border-[#1e293b]'
              }`}
            >
              {zone}
            </button>
          ))}
        </div>

        {/* District Selection Scroll Box */}
        <div className="max-h-40 overflow-y-auto space-y-1 pr-1 border border-[#1e293b] rounded p-1 bg-[#090d1a]">
          <button
            onClick={() => setSelectedDistrict(null)}
            className={`w-full text-left px-2 py-1.5 rounded text-xs font-mono flex items-center justify-between transition-all ${
              selectedDistrict === null
                ? 'bg-[#00f0ff]/20 text-[#00f0ff] border border-[#00f0ff]/40'
                : 'text-[#94a3b8] hover:bg-[#10192e]'
            }`}
          >
            <span>[ALL] Statewide Tamil Nadu</span>
            <span className="text-[10px] text-[#00e5a3]">38 Districts</span>
          </button>

          {filteredDistricts.map((d) => {
            const isSelected = selectedDistrict?.id === d.id;
            return (
              <button
                key={d.id}
                onClick={() => setSelectedDistrict(d)}
                className={`w-full text-left px-2 py-1.5 rounded text-xs font-mono flex items-center justify-between transition-all ${
                  isSelected
                    ? 'bg-[#00f0ff]/20 text-[#00f0ff] border border-[#00f0ff]/40 font-bold'
                    : 'text-[#94a3b8] hover:bg-[#10192e] hover:text-white'
                }`}
              >
                <div className="flex items-center gap-1.5 truncate">
                  <MapPin className="w-3 h-3 text-[#64748b] shrink-0" />
                  <span className="truncate">{d.name}</span>
                </div>
                <span className="text-[10px] text-[#64748b] font-mono">{d.code}</span>
              </button>
            );
          })}
        </div>
      </div>

      {/* 2. Crime Category Filters */}
      <div className="space-y-2 border-t border-[#1e293b] pt-3">
        <label className="text-[11px] font-display font-bold text-[#94a3b8] flex items-center justify-between">
          <span>CRIME CLASSIFICATION</span>
          <span className="text-[10px] font-mono text-[#00e5a3]">
            {selectedCategory ? selectedCategory.name.slice(0, 15) + '...' : 'ALL CATEGORIES'}
          </span>
        </label>

        <div className="space-y-1">
          <button
            onClick={() => setSelectedCategory(null)}
            className={`w-full text-left px-2.5 py-1.5 rounded text-xs font-mono transition-all ${
              selectedCategory === null
                ? 'bg-[#00e5a3]/15 text-[#00e5a3] border border-[#00e5a3]/40'
                : 'text-[#94a3b8] hover:bg-[#10192e]'
            }`}
          >
            All Crime Classifications
          </button>

          {categories.map((cat) => {
            const isSelected = selectedCategory?.id === cat.id;
            const isCritical = cat.severityLevel === 'CRITICAL';
            const isHigh = cat.severityLevel === 'HIGH';

            return (
              <button
                key={cat.id}
                onClick={() => setSelectedCategory(cat)}
                className={`w-full text-left px-2.5 py-1.5 rounded text-xs font-mono flex items-center justify-between transition-all ${
                  isSelected
                    ? 'bg-[#00f0ff]/15 text-[#00f0ff] border border-[#00f0ff]/40 font-bold'
                    : 'text-[#94a3b8] hover:bg-[#10192e] hover:text-white'
                }`}
              >
                <span className="truncate">{cat.name}</span>
                <span
                  className={`text-[9px] px-1.5 py-0.2 rounded font-mono ${
                    isCritical
                      ? 'bg-[#ef4444]/20 text-[#ef4444]'
                      : isHigh
                      ? 'bg-[#f59e0b]/20 text-[#f59e0b]'
                      : 'bg-[#64748b]/20 text-[#94a3b8]'
                  }`}
                >
                  {cat.severityLevel}
                </span>
              </button>
            );
          })}
        </div>
      </div>

      {/* 3. Temporal Range Controls */}
      <div className="space-y-2 border-t border-[#1e293b] pt-3">
        <label className="text-[11px] font-display font-bold text-[#94a3b8] flex items-center justify-between">
          <span className="flex items-center gap-1.5">
            <Calendar className="w-3.5 h-3.5 text-[#f59e0b]" />
            <span>REPORTING PERIOD</span>
          </span>
          <span className="text-[10px] font-mono text-[#f59e0b]">
            {yearRange[0]} - {yearRange[1]}
          </span>
        </label>

        <div className="grid grid-cols-6 gap-1">
          {[2018, 2019, 2020, 2021, 2022, 2023].map((yr) => {
            const inRange = yr >= yearRange[0] && yr <= yearRange[1];
            return (
              <button
                key={yr}
                onClick={() => {
                  if (yearRange[0] === yr && yearRange[1] === yr) {
                    setYearRange([2018, 2023]);
                  } else {
                    setYearRange([yr, yr]);
                  }
                }}
                className={`text-[10px] font-mono py-1 rounded transition-all ${
                  inRange
                    ? 'bg-[#00f0ff]/20 text-[#00f0ff] border border-[#00f0ff]/40 font-bold'
                    : 'bg-[#10192e] text-[#64748b] hover:text-[#94a3b8] border border-[#1e293b]'
                }`}
              >
                {yr}
              </button>
            );
          })}
        </div>
      </div>

      {/* 4. Map Display Layer Modes */}
      <div className="space-y-2 border-t border-[#1e293b] pt-3">
        <label className="text-[11px] font-display font-bold text-[#94a3b8] flex items-center gap-1.5">
          <Layers className="w-3.5 h-3.5 text-[#00f0ff]" />
          <span>MAP VISUALIZATION LAYER</span>
        </label>

        <div className="grid grid-cols-3 gap-1">
          {[
            { id: 'choropleth', label: 'CHOROPLETH' },
            { id: 'density', label: 'DENSITY' },
            { id: 'markers', label: 'INCIDENTS' },
          ].map((mode) => (
            <button
              key={mode.id}
              onClick={() => setMapLayerMode(mode.id)}
              className={`text-[10px] font-mono py-1 rounded text-center transition-all ${
                mapLayerMode === mode.id
                  ? 'bg-[#00f0ff]/20 text-[#00f0ff] border border-[#00f0ff]/50 font-bold'
                  : 'bg-[#10192e] text-[#64748b] hover:text-white border border-[#1e293b]'
              }`}
            >
              {mode.label}
            </button>
          ))}
        </div>

        <div className="flex items-center justify-between pt-1">
          <span className="text-[10px] font-mono text-[#94a3b8]">Show District Boundaries</span>
          <input
            type="checkbox"
            checked={showBoundaries}
            onChange={(e) => setShowBoundaries(e.target.checked)}
            className="cursor-pointer accent-[#00f0ff]"
          />
        </div>
      </div>

      {/* 5. Dataset Source Selector & Demonstration Sandbox Toggle */}
      <div className="space-y-2 border-t border-[#1e293b] pt-3 mt-auto">
        <div className="p-2.5 rounded bg-[#0d1424] border border-[#1e293b] space-y-1.5">
          <div className="flex items-center justify-between">
            <span className="text-[10px] font-display font-bold text-[#94a3b8]">INCLUDE DEMO SANDBOX</span>
            <input
              type="checkbox"
              checked={includeDemo}
              onChange={(e) => setIncludeDemo(e.target.checked)}
              className="cursor-pointer accent-[#f59e0b]"
            />
          </div>
          <p className="text-[9px] font-mono text-[#64748b] leading-tight">
            Separates genuine SCRB police records from synthetic baseline simulation records.
          </p>
          {includeDemo && (
            <div className="flex items-center gap-1.5 text-[9px] font-mono text-[#f59e0b] bg-[#f59e0b]/10 p-1 rounded border border-[#f59e0b]/30">
              <AlertTriangle className="w-3 h-3 shrink-0" />
              <span>Simulated baseline records active</span>
            </div>
          )}
        </div>
      </div>

    </aside>
  );
}
