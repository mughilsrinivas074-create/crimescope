import React from 'react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import { Calendar, TrendingUp, ShieldCheck, UserCheck, Gavel } from 'lucide-react';
import { useCrime } from '../../context/CrimeContext';

export default function BottomTimeline() {
  const { overviewData, yearRange, setYearRange } = useCrime();

  const yearlyTrends = overviewData?.yearlyTrends || [
    { year: 2018, totalCases: 84000, arrests: 65000, convictions: 35000 },
    { year: 2019, totalCases: 86500, arrests: 68000, convictions: 36000 },
    { year: 2020, totalCases: 79200, arrests: 61000, convictions: 31000 },
    { year: 2021, totalCases: 88100, arrests: 70000, convictions: 37500 },
    { year: 2022, totalCases: 93400, arrests: 74500, convictions: 39000 },
    { year: 2023, totalCases: 98200, arrests: 78000, convictions: 41200 },
  ];

  // Filter trends to selected range
  const filteredTrends = yearlyTrends.filter(
    (t) => t.year >= yearRange[0] && t.year <= yearRange[1]
  );

  const totalPeriodCases = filteredTrends.reduce((acc, curr) => acc + (curr.totalCases || 0), 0);
  const totalPeriodArrests = filteredTrends.reduce((acc, curr) => acc + (curr.arrests || 0), 0);
  const totalPeriodConvictions = filteredTrends.reduce((acc, curr) => acc + (curr.convictions || 0), 0);
  const avgClearance = totalPeriodCases > 0
    ? ((totalPeriodArrests / totalPeriodCases) * 100).toFixed(1)
    : '0.0';

  return (
    <div className="cyber-panel p-3 flex flex-col lg:flex-row items-center gap-4 w-full">
      
      {/* 1. Timeline Controls & Year Buttons */}
      <div className="flex flex-col gap-1.5 shrink-0 border-b lg:border-b-0 lg:border-r border-[#1e293b] pb-2 lg:pb-0 lg:pr-4 w-full lg:w-auto">
        <div className="flex items-center gap-2">
          <Calendar className="w-4 h-4 text-[#f59e0b]" />
          <span className="text-xs font-display font-bold text-white tracking-wider">
            HISTORICAL TIMELINE
          </span>
        </div>

        <div className="flex items-center gap-1 mt-1">
          {[2018, 2019, 2020, 2021, 2022, 2023].map((yr) => {
            const isSelected = yr >= yearRange[0] && yr <= yearRange[1];
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
                className={`text-[10px] font-mono px-2 py-1 rounded transition-all ${
                  isSelected
                    ? 'bg-[#00f0ff] text-black font-bold shadow-[0_0_10px_rgba(0,240,255,0.4)]'
                    : 'bg-[#10192e] text-[#94a3b8] hover:text-white border border-[#1e293b]'
                }`}
              >
                {yr}
              </button>
            );
          })}
        </div>
        <span className="text-[10px] font-mono text-[#64748b]">
          Viewing: {yearRange[0]} - {yearRange[1]} (NCRB/SCRB)
        </span>
      </div>

      {/* 2. Responsive Chart Area */}
      <div className="flex-1 h-20 w-full min-w-[280px]">
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={filteredTrends} margin={{ top: 5, right: 10, left: -20, bottom: 0 }}>
            <defs>
              <linearGradient id="cyberCyanGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#00f0ff" stopOpacity={0.4} />
                <stop offset="95%" stopColor="#00f0ff" stopOpacity={0.0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
            <XAxis dataKey="year" stroke="#64748b" tick={{ fontSize: 10, fontFamily: 'monospace' }} />
            <YAxis stroke="#64748b" tick={{ fontSize: 9, fontFamily: 'monospace' }} />
            <Tooltip
              content={({ active, payload, label }) => {
                if (active && payload && payload.length) {
                  return (
                    <div className="bg-[#10192e] border border-[#00f0ff] p-2 rounded text-xs font-mono">
                      <p className="text-[#00f0ff] font-bold">Year {label}</p>
                      <p className="text-white">FIR Cases: {payload[0].value?.toLocaleString()}</p>
                    </div>
                  );
                }
                return null;
              }}
            />
            <Area
              type="monotone"
              dataKey="totalCases"
              stroke="#00f0ff"
              strokeWidth={2}
              fillOpacity={1}
              fill="url(#cyberCyanGrad)"
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>

      {/* 3. Selected Period Summary Telemetry */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5 shrink-0 border-t lg:border-t-0 lg:border-l border-[#1e293b] pt-2 lg:pt-0 lg:pl-4 w-full lg:w-auto">
        <div className="p-2 rounded bg-[#0b1120] border border-[#1e293b]">
          <span className="text-[9px] font-mono text-[#64748b] flex items-center gap-1">
            <TrendingUp className="w-3 h-3 text-[#00f0ff]" /> CASES
          </span>
          <div className="text-sm font-mono font-bold text-white">
            {totalPeriodCases.toLocaleString()}
          </div>
        </div>

        <div className="p-2 rounded bg-[#0b1120] border border-[#1e293b]">
          <span className="text-[9px] font-mono text-[#64748b] flex items-center gap-1">
            <UserCheck className="w-3 h-3 text-[#00e5a3]" /> ARRESTS
          </span>
          <div className="text-sm font-mono font-bold text-[#00e5a3]">
            {totalPeriodArrests.toLocaleString()}
          </div>
        </div>

        <div className="p-2 rounded bg-[#0b1120] border border-[#1e293b]">
          <span className="text-[9px] font-mono text-[#64748b] flex items-center gap-1">
            <Gavel className="w-3 h-3 text-[#f59e0b]" /> CONVICTIONS
          </span>
          <div className="text-sm font-mono font-bold text-[#f59e0b]">
            {totalPeriodConvictions.toLocaleString()}
          </div>
        </div>

        <div className="p-2 rounded bg-[#0b1120] border border-[#1e293b]">
          <span className="text-[9px] font-mono text-[#64748b] flex items-center gap-1">
            <ShieldCheck className="w-3 h-3 text-[#a855f7]" /> CLEARANCE
          </span>
          <div className="text-sm font-mono font-bold text-[#00f0ff]">
            {avgClearance}%
          </div>
        </div>
      </div>

    </div>
  );
}
