import React, { useState, useEffect } from 'react';
import { Server, ShieldCheck, Terminal, Cpu, Clock, CheckCircle2, AlertCircle, Key, Camera, Sparkles } from 'lucide-react';
import api from '../services/api';
import ApiKeyModal from '../components/common/ApiKeyModal';

export default function SystemStatusPage() {
  const [status, setStatus] = useState(null);
  const [keysStatus, setKeysStatus] = useState(null);
  const [auditLogs, setAuditLogs] = useState([]);
  const [jdbcResult, setJdbcResult] = useState(null);
  const [jdbcDistCode, setJdbcDistCode] = useState('CHN');
  const [jdbcYear, setJdbcYear] = useState(2023);
  const [loadingJdbc, setLoadingJdbc] = useState(false);
  const [keyModalOpen, setKeyModalOpen] = useState(false);

  const loadAllStatus = async () => {
    try {
      const [stat, keys, logs] = await Promise.all([
        api.getSystemStatus(),
        api.getApiKeysStatus(),
        api.getAuditLogs(0, 10),
      ]);
      setStatus(stat);
      setKeysStatus(keys);
      if (logs && logs.content) {
        setAuditLogs(logs.content);
      }
    } catch (err) {
      console.warn('Status load error:', err);
    }
  };

  useEffect(() => {
    loadAllStatus();
  }, []);

  const runJdbcDemonstration = async () => {
    setLoadingJdbc(true);
    try {
      const res = await api.runJdbcDemo(jdbcDistCode, jdbcYear);
      setJdbcResult(res);
    } catch (err) {
      console.warn('JDBC demo error:', err);
    } finally {
      setLoadingJdbc(false);
    }
  };

  const gemini = keysStatus?.gemini || {};
  const mapillary = keysStatus?.mapillary || {};

  return (
    <div className="max-w-[1600px] mx-auto p-4 space-y-4">
      
      {/* Header */}
      <div className="cyber-panel p-4 flex flex-col md:flex-row md:items-center justify-between gap-3">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <Server className="w-5 h-5 text-[#00f0ff]" />
            <h1 className="text-lg font-display font-bold text-white tracking-wider">
              SYSTEM STATUS, TELEMETRY & ACADEMIC AUDIT
            </h1>
          </div>
          <p className="text-xs font-mono text-[#94a3b8]">
            Spring Boot 3.3 runtime metrics, Flyway migration registry, API key health diagnostics, and direct JDBC demonstration.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => setKeyModalOpen(true)}
            className="cyber-btn text-xs py-1.5 px-3 flex items-center gap-1.5"
          >
            <Key className="w-3.5 h-3.5" />
            <span>CONFIGURE API KEYS</span>
          </button>
        </div>
      </div>

      {/* API Key Health Diagnostics Card */}
      <div className="cyber-panel p-4 space-y-3">
        <div className="flex items-center justify-between border-b border-[#1e293b] pb-2">
          <div className="flex items-center gap-2">
            <Key className="w-4 h-4 text-[#00f0ff]" />
            <span className="text-xs font-display font-bold text-white tracking-wider">
              INTEGRATION API KEYS & CREDENTIALS RUNTIME STATUS
            </span>
          </div>
          <span className="text-[10px] font-mono text-gray-400">Environment Variables / .env</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          
          {/* Gemini API Status */}
          <div className="p-3.5 rounded bg-[#0b1120] border border-[#1e293b] space-y-2">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Sparkles className="w-4 h-4 text-[#00f0ff]" />
                <span className="text-xs font-display font-bold text-white">GOOGLE GEMINI 2.5 FLASH</span>
              </div>
              <span className={`text-[10px] font-mono font-bold px-2 py-0.5 rounded border ${
                gemini.valid ? 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30' :
                (gemini.configured ? 'text-red-400 bg-red-500/10 border-red-500/30' : 'text-amber-400 bg-amber-500/10 border-amber-500/30')
              }`}>
                {gemini.status || 'UNCONFIGURED'}
              </span>
            </div>
            <p className="text-[11px] font-mono text-gray-300">{gemini.message}</p>
            <div className="flex items-center justify-between text-[10px] font-mono text-gray-400 pt-1 border-t border-[#1e293b]/50">
              <span>Variable: <code className="text-[#00f0ff]">GEMINI_API_KEY</code></span>
              <span>Key: <strong className="text-white">{gemini.keyMasked || 'None'}</strong></span>
            </div>
          </div>

          {/* Mapillary API Status */}
          <div className="p-3.5 rounded bg-[#0b1120] border border-[#1e293b] space-y-2">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <Camera className="w-4 h-4 text-[#00e5a3]" />
                <span className="text-xs font-display font-bold text-white">MAPILLARY STREET EXPLORER</span>
              </div>
              <span className={`text-[10px] font-mono font-bold px-2 py-0.5 rounded border ${
                mapillary.valid ? 'text-emerald-400 bg-emerald-500/10 border-emerald-500/30' :
                (mapillary.configured ? 'text-red-400 bg-red-500/10 border-red-500/30' : 'text-amber-400 bg-amber-500/10 border-amber-500/30')
              }`}>
                {mapillary.status || 'UNCONFIGURED'}
              </span>
            </div>
            <p className="text-[11px] font-mono text-gray-300">{mapillary.message}</p>
            <div className="flex items-center justify-between text-[10px] font-mono text-gray-400 pt-1 border-t border-[#1e293b]/50">
              <span>Variable: <code className="text-[#00e5a3]">MAPILLARY_CLIENT_TOKEN</code></span>
              <span>Token: <strong className="text-white">{mapillary.tokenMasked || 'None'}</strong></span>
            </div>
          </div>

        </div>
      </div>

      {/* Telemetry Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
        <div className="cyber-panel p-3.5 space-y-1">
          <span className="text-[10px] font-mono text-[#64748b]">DATABASE ENGINE</span>
          <div className="text-sm font-mono font-bold text-white truncate">
            {status?.databaseProductName || 'MySQL / H2 (MySQL Mode)'}
          </div>
          <p className="text-[10px] font-mono text-[#94a3b8] truncate">
            Version: {status?.databaseVersion || '8.0+ Compatible'}
          </p>
        </div>

        <div className="cyber-panel p-3.5 space-y-1">
          <span className="text-[10px] font-mono text-[#64748b]">JAVA RUNTIME (LTS)</span>
          <div className="text-sm font-mono font-bold text-[#00f0ff] truncate">
            {status?.javaVersion || 'Java 21 OpenJDK'}
          </div>
          <p className="text-[10px] font-mono text-[#94a3b8]">
            Spring Boot 3.3.5 • Flyway Migrations V1-V3
          </p>
        </div>

        <div className="cyber-panel p-3.5 space-y-1">
          <span className="text-[10px] font-mono text-[#64748b]">STORED CRIME RECORDS</span>
          <div className="text-sm font-mono font-bold text-[#00e5a3]">
            {status?.totalCrimeRecords?.toLocaleString() || '1,600+'}
          </div>
          <p className="text-[10px] font-mono text-[#94a3b8]">
            38 Tamil Nadu Administrative Districts
          </p>
        </div>

        <div className="cyber-panel p-3.5 space-y-1">
          <span className="text-[10px] font-mono text-[#64748b]">AI INTEGRATION MODEL</span>
          <div className="text-sm font-mono font-bold text-[#a855f7]">
            {status?.geminiModel || 'Gemini 2.5 Flash'}
          </div>
          <p className="text-[10px] font-mono text-[#94a3b8]">
            Status: {status?.geminiKeyConfigured ? 'Live AI Key Active' : 'Deterministic Grounded Fallback'}
          </p>
        </div>
      </div>

      {/* Academic OOPS Architecture Visualizer */}
      <div className="cyber-panel p-4 space-y-3">
        <div className="flex items-center justify-between">
          <span className="text-xs font-display font-bold text-white flex items-center gap-1.5">
            <Cpu className="w-4 h-4 text-[#00f0ff]" />
            ACADEMIC OOPS DESIGN CRITERIA IMPLEMENTATION
          </span>
          <span className="text-[10px] font-mono text-[#00e5a3]">Java 21 Architecture</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-4 gap-3 text-xs font-mono">
          <div className="p-3 rounded bg-[#0b1120] border border-[#1e293b] space-y-1">
            <span className="text-[10px] text-[#00f0ff] font-bold block">1. ENCAPSULATION</span>
            <h4 className="text-white font-bold">CrimeRecord & User</h4>
            <p className="text-[#94a3b8] text-[11px] leading-relaxed">
              Private fields, validated mutators, zero-division protected clearance rate calculation, and defensive copies.
            </p>
          </div>

          <div className="p-3 rounded bg-[#0b1120] border border-[#1e293b] space-y-1">
            <span className="text-[10px] text-[#00e5a3] font-bold block">2. INHERITANCE</span>
            <h4 className="text-white font-bold">BaseEntity Hierarchy</h4>
            <p className="text-[#94a3b8] text-[11px] leading-relaxed">
              Mapped superclass provides unified primary keys, automated lifecycle callbacks (onCreate, onUpdate) and timestamping.
            </p>
          </div>

          <div className="p-3 rounded bg-[#0b1120] border border-[#1e293b] space-y-1">
            <span className="text-[10px] text-[#f59e0b] font-bold block">3. ABSTRACTION</span>
            <h4 className="text-white font-bold">Service & Strategy Interfaces</h4>
            <p className="text-[#94a3b8] text-[11px] leading-relaxed">
              CrimeRecordRepository and CrimeAnalyticsStrategy decouple storage logic and mathematical evaluations.
            </p>
          </div>

          <div className="p-3 rounded bg-[#0b1120] border border-[#1e293b] space-y-1">
            <span className="text-[10px] text-[#a855f7] font-bold block">4. POLYMORPHISM</span>
            <h4 className="text-white font-bold">Strategy Pattern (4 Types)</h4>
            <p className="text-[#94a3b8] text-[11px] leading-relaxed">
              Dynamic execution: DistrictComparison, TrendForecasting, HotspotDensity, and CategoryDistribution strategies.
            </p>
          </div>
        </div>
      </div>

      {/* Live Direct JDBC Demonstration Runner */}
      <div className="cyber-panel p-4 space-y-3">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-[#1e293b] pb-2">
          <div className="flex items-center gap-2">
            <Terminal className="w-4 h-4 text-[#00e5a3]" />
            <span className="text-xs font-display font-bold text-white tracking-wider">
              ACADEMIC LIVE JDBC DEMONSTRATION RUNNER
            </span>
          </div>
          <span className="text-[10px] font-mono text-[#94a3b8]">
            Direct java.sql.Connection & PreparedStatement Execution
          </span>
        </div>

        <p className="text-xs font-mono text-[#94a3b8]">
          Executes EducationalJdbcCrimeDao bypassing Hibernate/JPA to satisfy academic raw JDBC requirements.
        </p>

        <div className="flex flex-wrap items-center gap-3">
          <div className="flex items-center gap-2">
            <label className="text-xs font-mono text-gray-400">District Code:</label>
            <input
              type="text"
              value={jdbcDistCode}
              onChange={(e) => setJdbcDistCode(e.target.value.toUpperCase())}
              className="cyber-input text-xs w-20 py-1"
              maxLength={4}
            />
          </div>

          <div className="flex items-center gap-2">
            <label className="text-xs font-mono text-gray-400">Year:</label>
            <input
              type="number"
              value={jdbcYear}
              onChange={(e) => setJdbcYear(parseInt(e.target.value))}
              className="cyber-input text-xs w-24 py-1"
              min={2018}
              max={2023}
            />
          </div>

          <button
            onClick={runJdbcDemonstration}
            disabled={loadingJdbc}
            className="cyber-btn cyber-btn-teal text-xs py-1.5 px-3 flex items-center gap-1.5 disabled:opacity-40"
          >
            <span>{loadingJdbc ? 'EXECUTING QUERY...' : 'EXECUTE DIRECT JDBC QUERY'}</span>
          </button>
        </div>

        {/* JDBC Telemetry Results */}
        {jdbcResult && (
          <div className="p-3 rounded bg-[#0a0f1d] border border-[#1e293b] space-y-2 text-xs font-mono">
            <div className="flex flex-wrap items-center justify-between gap-2 border-b border-[#1e293b]/60 pb-1.5 text-[11px]">
              <span className="text-[#00e5a3]">Driver: {jdbcResult.driverName} v{jdbcResult.driverVersion}</span>
              <span className="text-white">Latency: <strong className="text-[#00f0ff]">{jdbcResult.executionTimeMs} ms</strong></span>
              <span className="text-gray-400">Isolation: {jdbcResult.transactionIsolation}</span>
              <span className="text-emerald-400 font-bold">Rows: {jdbcResult.rowCount}</span>
            </div>

            <div className="text-[10px] text-[#64748b] bg-black/40 p-2 rounded break-all">
              <strong className="text-gray-400 block mb-0.5">Parameterized PreparedStatement SQL:</strong>
              {jdbcResult.executedSql}
            </div>

            {jdbcResult.records && jdbcResult.records.length > 0 && (
              <div className="overflow-x-auto pt-1">
                <table className="w-full text-left text-[11px]">
                  <thead>
                    <tr className="text-[#64748b] border-b border-[#1e293b]">
                      <th className="py-1">ID</th>
                      <th className="py-1">Category</th>
                      <th className="py-1">Cases</th>
                      <th className="py-1">Arrests</th>
                      <th className="py-1">Chargesheeted</th>
                    </tr>
                  </thead>
                  <tbody>
                    {jdbcResult.records.map((r) => (
                      <tr key={r.recordId} className="border-b border-[#1e293b]/40 text-gray-300">
                        <td className="py-1 text-[#64748b]">#{r.recordId}</td>
                        <td className="py-1 text-white font-bold">{r.categoryName}</td>
                        <td className="py-1 text-[#00f0ff] font-bold">{r.caseCount}</td>
                        <td className="py-1 text-[#00e5a3]">{r.arrestCount || 0}</td>
                        <td className="py-1">{r.chargesheetedCount || 0}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Audit Log Table */}
      <div className="cyber-panel p-4 space-y-3">
        <span className="text-xs font-display font-bold text-white flex items-center gap-1.5">
          <Clock className="w-4 h-4 text-[#00f0ff]" />
          IMMUTABLE SYSTEM AUDIT TRAIL LOGS
        </span>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs font-mono">
            <thead>
              <tr className="border-b border-[#1e293b] text-[#64748b] text-[10px]">
                <th className="py-1.5">ID</th>
                <th className="py-1.5">TIMESTAMP</th>
                <th className="py-1.5">OPERATOR</th>
                <th className="py-1.5">ACTION</th>
                <th className="py-1.5">TARGET</th>
                <th className="py-1.5">IP ADDRESS</th>
                <th className="py-1.5">DETAILS</th>
              </tr>
            </thead>
            <tbody>
              {auditLogs.map((log) => (
                <tr key={log.id} className="border-b border-[#1e293b]/40 hover:bg-[#10192e] text-[11px]">
                  <td className="py-1.5 text-[#64748b]">#{log.id}</td>
                  <td className="py-1.5 text-gray-400">{new Date(log.createdAt).toLocaleString()}</td>
                  <td className="py-1.5 text-[#00f0ff] font-bold">{log.username || 'SYSTEM'}</td>
                  <td className="py-1.5">
                    <span className="px-1.5 py-0.5 rounded text-[9px] bg-[#00e5a3]/10 text-[#00e5a3] border border-[#00e5a3]/30">
                      {log.action}
                    </span>
                  </td>
                  <td className="py-1.5 text-white">{log.entityName} #{log.entityId}</td>
                  <td className="py-1.5 text-[#64748b]">{log.ipAddress || '127.0.0.1'}</td>
                  <td className="py-1.5 text-gray-400 truncate max-w-xs">{log.details}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Global API Key Configuration Modal */}
      <ApiKeyModal
        isOpen={keyModalOpen}
        onClose={() => { setKeyModalOpen(false); loadAllStatus(); }}
        onRefreshStatus={loadAllStatus}
      />

    </div>
  );
}
