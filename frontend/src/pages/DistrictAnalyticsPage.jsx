import React, { useState, useEffect } from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Legend, CartesianGrid } from 'recharts';
import { BarChart3, ArrowRightLeft, Layers, ShieldCheck } from 'lucide-react';
import { useCrime } from '../context/CrimeContext';
import api from '../services/api';

export default function DistrictAnalyticsPage() {
  const { districts, includeDemo } = useCrime();
  const [selectedIds, setSelectedIds] = useState([1, 2, 3]); // Chennai, Coimbatore, Madurai default
  const [targetYear, setTargetYear] = useState(2023);
  const [comparisonResult, setComparisonResult] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    async function fetchComparison() {
      setLoading(true);
      try {
        const res = await api.compareDistricts(selectedIds, targetYear, includeDemo);
        setComparisonResult(res);
      } catch (err) {
        console.warn('Comparison error:', err);
      } finally {
        setLoading(false);
      }
    }
    fetchComparison();
  }, [selectedIds, targetYear, includeDemo]);

  const toggleDistrict = (id) => {
    if (selectedIds.includes(id)) {
      if (selectedIds.length > 1) {
        setSelectedIds(selectedIds.filter((item) => item !== id));
      }
    } else {
      if (selectedIds.length < 5) {
        setSelectedIds([...selectedIds, id]);
      }
    }
  };

  const chartData = comparisonResult?.visualizationData?.comparisonTable || [];

  return (
    <div className="max-w-[1600px] mx-auto p-4 space-y-4">
      
      {/* Page Header */}
      <div className="cyber-panel p-4 flex flex-col md:flex-row md:items-center justify-between gap-3">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <BarChart3 className="w-5 h-5 text-[#00f0ff]" />
            <h1 className="text-lg font-display font-bold text-white tracking-wider">
              DISTRICT COMPARATIVE ANALYTICS ENGINE
            </h1>
          </div>
          <p className="text-xs font-mono text-[#94a3b8]">
            Multi-jurisdiction statistical evaluation across Tamil Nadu administrative districts.
          </p>
        </div>

        {/* Year Selector */}
        <div className="flex items-center gap-2">
          <span className="text-xs font-mono text-[#94a3b8]">Benchmark Year:</span>
          <select
            value={targetYear}
            onChange={(e) => setTargetYear(parseInt(e.target.value))}
            className="cyber-input text-xs py-1 px-2 w-auto"
          >
            {[2023, 2022, 2021, 2020, 2019, 2018].map((yr) => (
              <option key={yr} value={yr}>{yr}</option>
            ))}
          </select>
        </div>
      </div>

      {/* District Selection Chips */}
      <div className="cyber-panel p-3 space-y-2">
        <div className="flex items-center justify-between">
          <span className="text-xs font-display font-bold text-[#94a3b8]">
            SELECT UP TO 5 DISTRICTS TO CONTRAST ({selectedIds.length}/5 SELECTED)
          </span>
          <span className="text-[10px] font-mono text-[#00e5a3]">
            Click to toggle jurisdiction
          </span>
        </div>

        <div className="flex flex-wrap gap-1.5 max-h-28 overflow-y-auto pr-1">
          {districts.map((d) => {
            const isSelected = selectedIds.includes(d.id);
            return (
              <button
                key={d.id}
                onClick={() => toggleDistrict(d.id)}
                className={`text-xs font-mono px-2.5 py-1 rounded transition-all ${
                  isSelected
                    ? 'bg-[#00f0ff] text-black font-bold shadow-[0_0_10px_rgba(0,240,255,0.4)]'
                    : 'bg-[#10192e] text-[#94a3b8] hover:text-white border border-[#1e293b]'
                }`}
              >
                {d.code} - {d.name}
              </button>
            );
          })}
        </div>
      </div>

      {/* Comparison Visualizations */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-4">
        
        {/* Main Comparison Bar Chart (Col 1 to 8) */}
        <div className="lg:col-span-8 cyber-panel p-4 space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-xs font-display font-bold text-white flex items-center gap-1.5">
              <ArrowRightLeft className="w-4 h-4 text-[#00f0ff]" />
              CASE VOLUME & ENFORCEMENT RESOLUTION
            </span>
            <span className="text-[10px] font-mono text-[#94a3b8]">Year {targetYear}</span>
          </div>

          <div className="h-80 w-full">
            {loading ? (
              <div className="h-full flex items-center justify-center text-xs font-mono text-[#64748b]">
                Computing comparative statistics...
              </div>
            ) : chartData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData} margin={{ top: 10, right: 20, left: 0, bottom: 20 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                  <XAxis dataKey="districtName" stroke="#94a3b8" tick={{ fontSize: 11, fontFamily: 'monospace' }} />
                  <YAxis stroke="#94a3b8" tick={{ fontSize: 10, fontFamily: 'monospace' }} />
                  <Tooltip
                    contentStyle={{ backgroundColor: '#10192e', borderColor: '#00f0ff', color: '#fff', fontSize: '12px' }}
                  />
                  <Legend wrapperStyle={{ fontSize: '11px', fontFamily: 'monospace' }} />
                  <Bar dataKey="totalCases" name="Registered FIRs" fill="#00f0ff" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="totalArrests" name="Arrests" fill="#00e5a3" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="totalConvictions" name="Convictions" fill="#f59e0b" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center text-xs font-mono text-[#64748b]">
                No comparative data points available.
              </div>
            )}
          </div>
        </div>

        {/* Clearance Efficiency & Metrics Table (Col 9 to 12) */}
        <div className="lg:col-span-4 cyber-panel p-4 flex flex-col justify-between">
          <div className="space-y-3">
            <span className="text-xs font-display font-bold text-white flex items-center gap-1.5">
              <ShieldCheck className="w-4 h-4 text-[#00e5a3]" />
              CLEARANCE & CONVICTION EFFICIENCY
            </span>

            <div className="space-y-2">
              {chartData.map((d) => (
                <div key={d.districtName} className="p-2.5 rounded bg-[#0b1120] border border-[#1e293b] space-y-1">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-mono font-bold text-white">{d.districtName}</span>
                    <span className="text-[11px] font-mono text-[#00f0ff]">{d.totalCases?.toLocaleString()} Cases</span>
                  </div>
                  <div className="grid grid-cols-2 gap-2 pt-1 text-[10px] font-mono">
                    <div>
                      <span className="text-[#64748b]">Chargesheeted: </span>
                      <span className="text-[#00e5a3] font-bold">{d.clearanceRatePct}%</span>
                    </div>
                    <div>
                      <span className="text-[#64748b]">Conviction Rate: </span>
                      <span className="text-[#f59e0b] font-bold">{d.convictionRatePct}%</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="p-2.5 rounded bg-[#0d1424] border border-[#1e293b] text-[10px] font-mono text-[#64748b] mt-3">
            Mathematical Rule: Comparative clearance percentages are computed with strict non-zero denominator guards to prevent false mathematical representations.
          </div>
        </div>

      </div>

    </div>
  );
}
