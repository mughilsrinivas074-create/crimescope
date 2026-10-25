import React, { useState, useEffect } from 'react';
import { Cpu, Sparkles, Send, BrainCircuit, ArrowRightLeft, TrendingUp, HelpCircle, CheckCircle2, AlertTriangle, Key, ShieldCheck, Database } from 'lucide-react';
import { useCrime } from '../context/CrimeContext';
import api from '../services/api';
import ApiKeyModal from '../components/common/ApiKeyModal';

export default function AiIntelligencePage() {
  const { districts } = useCrime();

  const [activeTab, setActiveTab] = useState('insights'); // 'insights' | 'compare' | 'trends' | 'ask'
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [geminiStatus, setGeminiStatus] = useState(null);
  const [keyModalOpen, setKeyModalOpen] = useState(false);

  // Form inputs
  const [targetDistrictId, setTargetDistrictId] = useState(1); // Chennai default
  const [startYear, setStartYear] = useState(2018);
  const [endYear, setEndYear] = useState(2023);

  const [distCodeA, setDistCodeA] = useState('CHN');
  const [distCodeB, setDistCodeB] = useState('CBE');
  const [compareYear, setCompareYear] = useState(2023);

  const [userQuery, setUserQuery] = useState('Which Tamil Nadu districts experienced the highest increase in Cyber Crimes between 2018 and 2023?');

  const checkGeminiStatus = async () => {
    try {
      const res = await api.getGeminiStatus();
      setGeminiStatus(res);
    } catch (err) {
      console.warn('Failed to check Gemini status:', err);
    }
  };

  useEffect(() => {
    checkGeminiStatus();
  }, []);

  const handleGenerate = async () => {
    setLoading(true);
    setResult(null);
    try {
      let res;
      if (activeTab === 'insights') {
        res = await api.generateAiInsights(targetDistrictId, startYear, endYear);
      } else if (activeTab === 'compare') {
        res = await api.compareAiDistricts(distCodeA, distCodeB, compareYear);
      } else if (activeTab === 'trends') {
        res = await api.explainAiTrends(targetDistrictId, startYear, endYear);
      } else if (activeTab === 'ask') {
        res = await api.askAiQuestion(userQuery, targetDistrictId, endYear);
      }
      setResult(res);
    } catch (err) {
      setResult({
        status: 'NETWORK_ERROR',
        modelUsed: 'DETERMINISTIC_STATISTICAL_SYNTHESIS',
        analysis: 'Communication error: ' + err.message + '\nDeterministic statistical analysis engine active.',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-[1600px] mx-auto p-4 space-y-4">
      
      {/* Page Header */}
      <div className="cyber-panel p-4 flex flex-col md:flex-row md:items-center justify-between gap-3">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <Cpu className="w-5 h-5 text-[#a855f7]" />
            <h1 className="text-lg font-display font-bold text-white tracking-wider">
              GOOGLE GEMINI STRATEGIC CRIME INTELLIGENCE ENGINE
            </h1>
          </div>
          <p className="text-xs font-mono text-[#94a3b8]">
            Evidence-bounded generative AI analyzing historical Tamil Nadu crime statistics without hallucination.
          </p>
        </div>

        {/* Gemini Key Status & Configuration Button */}
        <div className="flex items-center gap-2">
          <button
            onClick={() => setKeyModalOpen(true)}
            className={`flex items-center gap-2 px-3 py-1.5 rounded border text-xs font-mono transition-all ${
              geminiStatus?.valid
                ? 'bg-emerald-950/40 text-emerald-300 border-emerald-500/40'
                : 'bg-amber-950/40 text-amber-300 border-amber-500/40 hover:bg-amber-950/60'
            }`}
          >
            <Key className="w-3.5 h-3.5" />
            <span>
              {geminiStatus?.valid
                ? `GEMINI 2.5 FLASH ACTIVE (${geminiStatus.keyMasked || 'LIVE'})`
                : 'CONFIGURE GEMINI KEY (.ENV)'}
            </span>
          </button>
        </div>
      </div>

      {/* Notice Banner when Key is Missing */}
      {geminiStatus && !geminiStatus.valid && (
        <div className="p-3 rounded bg-amber-950/30 border border-amber-500/40 flex items-start justify-between gap-3 text-xs text-amber-300">
          <div className="flex items-start gap-2.5">
            <AlertTriangle className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
            <div>
              <strong className="text-white">API Key Notice: </strong>
              Google Gemini API key is not configured in <code className="text-[#00f0ff] bg-black/40 px-1 py-0.5 rounded">.env</code>.
              The application is currently operating in <strong className="text-amber-200">Deterministic Statistical Synthesis</strong> mode: calculating 100% verified mathematical metrics in Java/SQL without hallucinated AI output.
            </div>
          </div>
          <button
            onClick={() => setKeyModalOpen(true)}
            className="cyber-btn text-[11px] py-1 px-3 shrink-0"
          >
            ENTER KEY
          </button>
        </div>
      )}

      {/* Mode Selector Tabs */}
      <div className="flex flex-wrap gap-2">
        {[
          { id: 'insights', label: 'STRATEGIC DISTRICT INSIGHTS', icon: Sparkles },
          { id: 'compare', label: 'COMPARATIVE JURISDICTION ANALYSIS', icon: ArrowRightLeft },
          { id: 'trends', label: 'HISTORICAL TREND TRAJECTORY', icon: TrendingUp },
          { id: 'ask', label: 'NATURAL LANGUAGE Q&A CONSOLE', icon: HelpCircle },
        ].map((tab) => {
          const Icon = tab.icon;
          return (
            <button
              key={tab.id}
              onClick={() => { setActiveTab(tab.id); setResult(null); }}
              className={`cyber-btn text-xs py-2 px-3.5 flex items-center gap-2 ${
                activeTab === tab.id
                  ? 'bg-[#a855f7]/20 text-[#a855f7] border-[#a855f7] shadow-[0_0_12px_rgba(168,85,247,0.3)]'
                  : 'cyber-btn-muted'
              }`}
            >
              <Icon className="w-3.5 h-3.5" />
              <span>{tab.label}</span>
            </button>
          );
        })}
      </div>

      {/* Input Parameters Panel */}
      <div className="cyber-panel p-4 space-y-4">
        {activeTab === 'insights' && (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div>
              <label className="text-[11px] font-mono text-[#94a3b8] block mb-1">Target District Jurisdiction</label>
              <select
                value={targetDistrictId}
                onChange={(e) => setTargetDistrictId(parseInt(e.target.value))}
                className="cyber-input text-xs"
              >
                {districts.map((d) => (
                  <option key={d.id} value={d.id}>{d.name} ({d.code} - {d.zone} Zone)</option>
                ))}
              </select>
            </div>
            <div>
              <label className="text-[11px] font-mono text-[#94a3b8] block mb-1">Historical Start Year</label>
              <select
                value={startYear}
                onChange={(e) => setStartYear(parseInt(e.target.value))}
                className="cyber-input text-xs"
              >
                {[2018, 2019, 2020, 2021, 2022].map((y) => (
                  <option key={y} value={y}>{y}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="text-[11px] font-mono text-[#94a3b8] block mb-1">Historical End Year</label>
              <select
                value={endYear}
                onChange={(e) => setEndYear(parseInt(e.target.value))}
                className="cyber-input text-xs"
              >
                {[2019, 2020, 2021, 2022, 2023].map((y) => (
                  <option key={y} value={y}>{y}</option>
                ))}
              </select>
            </div>
          </div>
        )}

        {activeTab === 'compare' && (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div>
              <label className="text-[11px] font-mono text-[#94a3b8] block mb-1">Primary District</label>
              <select
                value={distCodeA}
                onChange={(e) => setDistCodeA(e.target.value)}
                className="cyber-input text-xs"
              >
                {districts.map((d) => (
                  <option key={d.id} value={d.code}>{d.name} ({d.code})</option>
                ))}
              </select>
            </div>
            <div>
              <label className="text-[11px] font-mono text-[#94a3b8] block mb-1">Comparative District</label>
              <select
                value={distCodeB}
                onChange={(e) => setDistCodeB(e.target.value)}
                className="cyber-input text-xs"
              >
                {districts.map((d) => (
                  <option key={d.id} value={d.code}>{d.name} ({d.code})</option>
                ))}
              </select>
            </div>
            <div>
              <label className="text-[11px] font-mono text-[#94a3b8] block mb-1">Evaluation Year</label>
              <select
                value={compareYear}
                onChange={(e) => setCompareYear(parseInt(e.target.value))}
                className="cyber-input text-xs"
              >
                {[2018, 2019, 2020, 2021, 2022, 2023].map((y) => (
                  <option key={y} value={y}>{y}</option>
                ))}
              </select>
            </div>
          </div>
        )}

        {activeTab === 'trends' && (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div>
              <label className="text-[11px] font-mono text-[#94a3b8] block mb-1">Target District</label>
              <select
                value={targetDistrictId}
                onChange={(e) => setTargetDistrictId(parseInt(e.target.value))}
                className="cyber-input text-xs"
              >
                {districts.map((d) => (
                  <option key={d.id} value={d.id}>{d.name} ({d.code})</option>
                ))}
              </select>
            </div>
            <div>
              <label className="text-[11px] font-mono text-[#94a3b8] block mb-1">Baseline Year</label>
              <input type="text" value={startYear} disabled className="cyber-input text-xs opacity-60" />
            </div>
            <div>
              <label className="text-[11px] font-mono text-[#94a3b8] block mb-1">Evaluation Year</label>
              <input type="text" value={endYear} disabled className="cyber-input text-xs opacity-60" />
            </div>
          </div>
        )}

        {activeTab === 'ask' && (
          <div className="space-y-2">
            <label className="text-[11px] font-mono text-[#94a3b8] block">
              Inquire About Historical Tamil Nadu Crime Data (Natural Language Query)
            </label>
            <div className="flex gap-2">
              <input
                type="text"
                value={userQuery}
                onChange={(e) => setUserQuery(e.target.value)}
                placeholder="Ask about district comparisons, cyber crime trends, clearance rates..."
                className="cyber-input text-xs"
              />
            </div>
            <div className="flex flex-wrap gap-1.5 pt-1">
              <span className="text-[10px] font-mono text-[#64748b]">Suggested Questions:</span>
              {[
                'How did cyber crime case counts evolve between 2018 and 2023 in Chennai?',
                'Which category represented the largest share of offences in Coimbatore?',
                'What was the chargesheet clearance percentage in Madurai during 2023?',
              ].map((q) => (
                <button
                  key={q}
                  onClick={() => setUserQuery(q)}
                  className="text-[10px] font-mono text-[#00f0ff] hover:underline"
                >
                  &quot;{q.slice(0, 45)}...&quot;
                </button>
              ))}
            </div>
          </div>
        )}

        <div className="flex items-center justify-between border-t border-[#1e293b] pt-3">
          <div className="text-[10px] font-mono text-[#64748b]">
            Backend computes mathematical totals before feeding evidence to model.
          </div>
          <button
            onClick={handleGenerate}
            disabled={loading}
            className="cyber-btn cyber-btn-teal text-xs py-1.5 px-4 flex items-center gap-1.5 disabled:opacity-40"
          >
            <Send className="w-3.5 h-3.5" />
            <span>{loading ? 'SYNTHESIZING...' : 'EVALUATE INTELLIGENCE PROMPT'}</span>
          </button>
        </div>
      </div>

      {/* Intelligence Response Container */}
      <div className="cyber-panel p-5 min-h-[360px]">
        {loading ? (
          <div className="h-64 flex flex-col items-center justify-center gap-3">
            <BrainCircuit className="w-10 h-10 text-[#00f0ff] animate-pulse" />
            <p className="text-xs font-mono text-[#00f0ff] tracking-wider animate-pulse">
              RETRIEVING BOUNDED RECORDS & CALCULATING FACTUAL EVIDENCE...
            </p>
          </div>
        ) : result ? (
          <div className="space-y-4">
            
            {/* Header info */}
            <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-3 border-b border-[#1e293b] gap-2">
              <div className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-[#00e5a3]" />
                <span className="text-xs font-display font-bold text-white tracking-wider">
                  STRATEGIC ASSESSMENT DOSSIER
                </span>
              </div>
              <div className="flex items-center gap-2 text-[10px] font-mono">
                <span className="px-2 py-0.5 rounded bg-[#10192e] text-[#00f0ff] border border-[#1e293b]">
                  {result.modelUsed || 'Gemini 2.5 Flash'}
                </span>
                <span className={`px-2 py-0.5 rounded border ${
                  result.isAiGenerated
                    ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30'
                    : 'bg-amber-500/10 text-amber-400 border-amber-500/30'
                }`}>
                  {result.isAiGenerated ? 'LIVE GEMINI AI' : 'VERIFIED STATISTICAL SYNTHESIS'}
                </span>
              </div>
            </div>

            {/* Factual Evidence Footprint (Java / SQL calculated) */}
            {(result.factualEvidence || result.evidenceSummary) && (
              <div className="p-3.5 rounded bg-[#090e1a] border border-[#00f0ff]/30 text-[11px] font-mono space-y-2">
                <div className="flex items-center justify-between border-b border-[#1e293b] pb-1.5">
                  <div className="flex items-center gap-2 text-[#00f0ff] font-bold">
                    <Database className="w-3.5 h-3.5" />
                    <span>BOUNDED FACTUAL EVIDENCE (JAVA/SQL COMPUTED)</span>
                  </div>
                  <span className="text-[10px] text-gray-400">Official Police SCRB Register Metrics</span>
                </div>

                {(() => {
                  const ev = result.factualEvidence || result.evidenceSummary || {};
                  return (
                    <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-gray-300 pt-1">
                      <div>Jurisdiction: <span className="text-white font-bold">{ev.context || 'Selected Scope'}</span></div>
                      <div>Registered FIRs: <span className="text-[#00f0ff] font-bold">{ev.totalRegisteredCases?.toLocaleString()}</span></div>
                      <div>Court Convictions: <span className="text-[#f59e0b] font-bold">{ev.totalConvictions?.toLocaleString()}</span></div>
                      <div>Clearance Ratio: <span className="text-[#00e5a3] font-bold">{ev.overallClearanceRatePct}%</span></div>
                    </div>
                  );
                })()}
              </div>
            )}

            {/* Analysis Text Body */}
            <div className="p-4 rounded bg-[#0a0e1c] border border-[#1e293b] space-y-2">
              <div className="flex items-center gap-2 text-xs font-display font-bold text-gray-300">
                <Sparkles className="w-3.5 h-3.5 text-[#00e5a3]" />
                <span>
                  {result.isAiGenerated ? 'GEMINI 2.5 FLASH AUTHORITATIVE ANALYSIS' : 'DETERMINISTIC STATISTICAL REPORT'}
                </span>
              </div>
              <div className="text-xs font-mono text-[#cbd5e1] leading-relaxed whitespace-pre-line">
                {result.analysis || result.response}
              </div>
            </div>

            {/* Ethical & Grounding Disclaimer */}
            {result.disclaimer && (
              <div className="p-2.5 rounded bg-blue-950/20 border border-blue-500/20 text-[10px] font-mono text-blue-300">
                {result.disclaimer}
              </div>
            )}
          </div>
        ) : (
          <div className="h-64 flex flex-col items-center justify-center text-center p-6 text-[#64748b] space-y-2">
            <Cpu className="w-12 h-12 text-[#1e293b]" />
            <p className="text-xs font-mono">
              Ready for intelligence query execution. Choose a mode and click &quot;Evaluate Intelligence Prompt&quot; above.
            </p>
          </div>
        )}
      </div>

      {/* Global API Key Configuration Modal */}
      <ApiKeyModal
        isOpen={keyModalOpen}
        onClose={() => { setKeyModalOpen(false); checkGeminiStatus(); }}
        onRefreshStatus={checkGeminiStatus}
      />

    </div>
  );
}
