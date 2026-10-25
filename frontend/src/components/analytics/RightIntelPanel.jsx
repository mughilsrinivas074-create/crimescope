import React, { useState, useEffect } from 'react';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer } from 'recharts';
import { Shield, BrainCircuit, Info, Sparkles, CheckCircle2, ChevronRight, TrendingUp } from 'lucide-react';
import { useCrime } from '../../context/CrimeContext';
import api from '../../services/api';

const CYBER_COLORS = ['#00f0ff', '#00e5a3', '#f59e0b', '#ef4444', '#a855f7', '#38bdf8', '#ec4899'];

export default function RightIntelPanel() {
  const { selectedDistrict, overviewData, includeDemo, yearRange } = useCrime();

  const [aiReport, setAiReport] = useState(null);
  const [aiLoading, setAiLoading] = useState(false);
  const [categoryData, setCategoryData] = useState([]);

  // Fetch category distribution whenever selectedDistrict changes
  useEffect(() => {
    async function loadCategoryDistribution() {
      try {
        const res = await api.getCategoriesDistribution(
          selectedDistrict?.id || null,
          null,
          includeDemo
        );
        if (res && res.visualizationData && res.visualizationData.distribution) {
          setCategoryData(res.visualizationData.distribution);
        } else if (overviewData?.categoryBreakdown) {
          setCategoryData(
            overviewData.categoryBreakdown.map((c) => ({
              categoryName: c.categoryName,
              caseCount: c.totalCases,
            }))
          );
        }
      } catch (err) {
        console.warn('Category distribution load error:', err);
      }
    }
    loadCategoryDistribution();
  }, [selectedDistrict, overviewData, includeDemo]);

  // Request Grounded AI Assessment
  const requestAiAnalysis = async () => {
    setAiLoading(true);
    try {
      const res = await api.generateAiInsights(
        selectedDistrict?.id || null,
        yearRange[0],
        yearRange[1]
      );
      setAiReport(res);
    } catch (err) {
      console.warn('AI Insights error:', err);
      setAiReport({
        modelUsed: 'DETERMINISTIC_FALLBACK',
        response: 'AI Strategic Assessment offline. All numerical metrics remain verified from local SCRB records.',
      });
    } finally {
      setAiLoading(false);
    }
  };

  // Find hotspot details for selected district
  const districtHotspot = React.useMemo(() => {
    if (!selectedDistrict || !overviewData?.districtHotspots) return null;
    return overviewData.districtHotspots.find(
      (h) => h.districtId === selectedDistrict.id ||
             h.districtName?.toLowerCase() === selectedDistrict.name.toLowerCase()
    );
  }, [selectedDistrict, overviewData]);

  return (
    <aside className="cyber-panel p-3.5 flex flex-col gap-3.5 h-full overflow-y-auto">
      
      {/* 1. Selected District Dossier Header */}
      <div className="border-b border-[#1e293b] pb-3">
        <div className="flex items-center justify-between mb-1.5">
          <div className="flex items-center gap-1.5">
            <Shield className="w-4 h-4 text-[#00f0ff]" />
            <span className="text-[10px] font-mono text-[#00f0ff] uppercase tracking-wider">
              {selectedDistrict ? 'JURISDICTION DOSSIER' : 'STATEWIDE CONSOLIDATED'}
            </span>
          </div>
          <span
            className={`text-[9px] font-mono font-bold px-2 py-0.5 rounded uppercase ${
              districtHotspot ? 'badge-' + districtHotspot.threatLevel?.toLowerCase() : 'badge-moderate'
            }`}
          >
            {districtHotspot ? districtHotspot.threatLabel : 'MONITORED'}
          </span>
        </div>

        <h3 className="text-base font-display font-bold text-white tracking-wide">
          {selectedDistrict ? selectedDistrict.name : 'Tamil Nadu State Command'}
        </h3>
        <p className="text-xs text-[#94a3b8] font-mono">
          {selectedDistrict
            ? `HQ: ${selectedDistrict.headquarters} • Zone: ${selectedDistrict.zone}`
            : 'Consolidated State Crime Records Bureau (SCRB)'}
        </p>

        {/* Quick Metrics Bar */}
        <div className="grid grid-cols-2 gap-2 mt-2.5">
          <div className="p-2 rounded bg-[#0b1120] border border-[#1e293b]">
            <span className="text-[10px] font-mono text-[#64748b]">RECORDED CASES</span>
            <div className="text-sm font-mono font-bold text-[#00f0ff]">
              {districtHotspot ? districtHotspot.totalCases?.toLocaleString() : overviewData?.totalRegisteredCases?.toLocaleString() || '---'}
            </div>
          </div>
          <div className="p-2 rounded bg-[#0b1120] border border-[#1e293b]">
            <span className="text-[10px] font-mono text-[#64748b]">EST. POPULATION</span>
            <div className="text-sm font-mono font-bold text-[#00e5a3]">
              {selectedDistrict?.population2021Est
                ? (selectedDistrict.population2021Est / 1000000).toFixed(2) + ' M'
                : '76.8 M (State)'}
            </div>
          </div>
        </div>
      </div>

      {/* 2. Crime Category Breakdown Chart */}
      <div className="space-y-1.5">
        <div className="flex items-center justify-between">
          <span className="text-xs font-display font-bold text-[#94a3b8]">CATEGORY DISTRIBUTION</span>
          <span className="text-[10px] font-mono text-[#00e5a3]">FIR VOLUME</span>
        </div>

        <div className="h-44 w-full bg-[#0a0e1c] rounded border border-[#1e293b] flex items-center justify-center p-1">
          {categoryData.length > 0 ? (
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={categoryData}
                  dataKey="caseCount"
                  nameKey="categoryName"
                  cx="50%"
                  cy="50%"
                  innerRadius={36}
                  outerRadius={62}
                  paddingAngle={3}
                >
                  {categoryData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={CYBER_COLORS[index % CYBER_COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip
                  content={({ active, payload }) => {
                    if (active && payload && payload.length) {
                      const data = payload[0].payload;
                      return (
                        <div className="bg-[#10192e] border border-[#00f0ff] p-2 rounded text-xs font-mono">
                          <p className="text-[#00f0ff] font-bold">{data.categoryName}</p>
                          <p className="text-white">Cases: {data.caseCount?.toLocaleString()}</p>
                          {data.sharePercentage && (
                            <p className="text-[#00e5a3]">Share: {data.sharePercentage}%</p>
                          )}
                        </div>
                      );
                    }
                    return null;
                  }}
                />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <div className="text-xs font-mono text-[#64748b]">Loading distribution...</div>
          )}
        </div>
      </div>

      {/* 3. Grounded AI Intelligence Observations */}
      <div className="space-y-2 border-t border-[#1e293b] pt-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-1.5">
            <BrainCircuit className="w-4 h-4 text-[#a855f7]" />
            <span className="text-xs font-display font-bold text-white">AI STRATEGIC INSIGHTS</span>
          </div>
          <button
            onClick={requestAiAnalysis}
            disabled={aiLoading}
            className="cyber-btn text-[10px] py-1 px-2 flex items-center gap-1"
          >
            <Sparkles className="w-3 h-3 text-[#00f0ff]" />
            <span>{aiLoading ? 'ANALYZING...' : 'GENERATE AI INSIGHT'}</span>
          </button>
        </div>

        <div className="p-2.5 rounded bg-[#0a0e1c] border border-[#1e293b] text-xs font-mono space-y-2 max-h-48 overflow-y-auto">
          {aiReport ? (
            <div>
              <div className="flex items-center justify-between text-[10px] text-[#00f0ff] border-b border-[#1e293b] pb-1 mb-1.5">
                <span>MODEL: {aiReport.modelUsed || 'Gemini 2.5 Flash'}</span>
                <span className="text-[#00e5a3]">GROUNDED</span>
              </div>
              <div className="text-[#cbd5e1] leading-relaxed whitespace-pre-line text-[11px]">
                {aiReport.response}
              </div>
            </div>
          ) : (
            <p className="text-[#64748b] text-[11px] leading-relaxed">
              Click &quot;Generate AI Insight&quot; above to request an evidence-grounded assessment synthesized exclusively from recorded SCRB/NCRB statistics.
            </p>
          )}
        </div>
      </div>

      {/* 4. Data Quality & Source Provenance Notice */}
      <div className="mt-auto border-t border-[#1e293b] pt-2.5 space-y-1 text-[10px] font-mono text-[#64748b]">
        <div className="flex items-center justify-between text-[#94a3b8]">
          <span className="flex items-center gap-1">
            <Info className="w-3 h-3 text-[#00f0ff]" />
            <span>DATA PROVENANCE</span>
          </span>
          <span className="text-[#00e5a3] flex items-center gap-1">
            <CheckCircle2 className="w-3 h-3" />
            <span>VERIFIED</span>
          </span>
        </div>
        <p className="leading-tight">
          Source: Tamil Nadu SCRB Crime Review Compendium (2020-2023) & NCRB.
        </p>
        <p className="text-[9px] text-[#475569]">
          * Historical crime records reflect registered FIR cases, not live monitoring.
        </p>
      </div>

    </aside>
  );
}
