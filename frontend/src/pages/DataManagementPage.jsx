import React, { useState } from 'react';
import { FileSpreadsheet, Upload, Download, CheckCircle2, AlertTriangle, Clock, ShieldAlert } from 'lucide-react';
import api from '../services/api';

export default function DataManagementPage() {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [importSummary, setImportSummary] = useState(null);

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setFile(e.target.files[0]);
    }
  };

  const handleImportSubmit = async (e) => {
    e.preventDefault();
    if (!file) {
      alert('Please select a CSV file first.');
      return;
    }

    setUploading(true);
    setImportSummary(null);
    try {
      const summary = await api.importCsv(file);
      setImportSummary(summary);
    } catch (err) {
      alert('Import failed: ' + err.message);
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="max-w-[1600px] mx-auto p-4 space-y-4">
      
      {/* Header */}
      <div className="cyber-panel p-4 flex flex-col md:flex-row md:items-center justify-between gap-3">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <FileSpreadsheet className="w-5 h-5 text-[#00f0ff]" />
            <h1 className="text-lg font-display font-bold text-white tracking-wider">
              DATA MANAGEMENT & FILE IMPORT SUBSYSTEM
            </h1>
          </div>
          <p className="text-xs font-mono text-[#94a3b8]">
            RFC 4180 compliant CSV parser, duplicate hash verification, and provenance tracker.
          </p>
        </div>

        {/* Template Download Button */}
        <a
          href="http://localhost:8080/api/data/template"
          className="cyber-btn text-xs py-1.5 px-3 flex items-center gap-1.5 text-decoration-none"
          download="crimescope_template.csv"
        >
          <Download className="w-3.5 h-3.5" />
          <span>DOWNLOAD SAMPLE CSV TEMPLATE</span>
        </a>
      </div>

      {/* Upload and Importer Section */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-4">
        
        {/* Upload Form (Col 1 to 5) */}
        <div className="lg:col-span-5 cyber-panel p-4 space-y-4">
          <span className="text-xs font-display font-bold text-white block">
            BATCH CSV IMPORT CONSOLE
          </span>

          <form onSubmit={handleImportSubmit} className="space-y-4">
            <div className="border-2 border-dashed border-[#1e293b] hover:border-[#00f0ff] rounded-lg p-6 text-center transition-colors bg-[#080d1a]/60">
              <Upload className="w-8 h-8 text-[#00f0ff] mx-auto mb-2" />
              <p className="text-xs font-mono text-white mb-1">
                {file ? file.name : 'Select or drop CSV file for validation'}
              </p>
              <p className="text-[10px] font-mono text-[#64748b] mb-3">
                UTF-8 encoded • Escaped quotes supported • Max file size: 10MB
              </p>
              <label className="cyber-btn text-xs py-1 px-3 cursor-pointer inline-flex">
                <span>BROWSE LOCAL DISK</span>
                <input
                  type="file"
                  accept=".csv"
                  onChange={handleFileChange}
                  className="hidden"
                />
              </label>
            </div>

            <div className="p-3 rounded bg-[#0b1120] border border-[#1e293b] text-[10px] font-mono space-y-1 text-[#64748b]">
              <span className="text-[#94a3b8] font-bold block">VERIFICATION CHECKS ACTIVE:</span>
              <p>✓ RFC 4180 State Machine Parsing (handles embedded commas and multiline quotes)</p>
              <p>✓ District code & Category foreign key verification</p>
              <p>✓ SHA-256 Record Hash duplicate prevention</p>
              <p>✓ Non-destructive malformed row isolation</p>
            </div>

            <button
              type="submit"
              disabled={!file || uploading}
              className="w-full cyber-btn cyber-btn-teal text-xs py-2 justify-center disabled:opacity-40"
            >
              {uploading ? 'PROCESSING FILE STREAM...' : 'RUN INGESTION & AUDIT'}
            </button>
          </form>
        </div>

        {/* Import Results & Diagnostics (Col 6 to 12) */}
        <div className="lg:col-span-7 cyber-panel p-4 space-y-4">
          <span className="text-xs font-display font-bold text-white block">
            INGESTION DIAGNOSTICS & AUDIT RESULTS
          </span>

          {importSummary ? (
            <div className="space-y-4">
              {/* Telemetry Chips */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-xs font-mono">
                <div className="p-2.5 rounded bg-[#0b1120] border border-[#1e293b]">
                  <span className="text-[10px] text-[#64748b] block">TOTAL ROWS</span>
                  <span className="text-sm font-bold text-white">{importSummary.totalRows}</span>
                </div>
                <div className="p-2.5 rounded bg-[#0b1120] border border-[#00e5a3]/40">
                  <span className="text-[10px] text-[#00e5a3] block">VALID IMPORTED</span>
                  <span className="text-sm font-bold text-[#00e5a3]">{importSummary.validRecordsImported}</span>
                </div>
                <div className="p-2.5 rounded bg-[#0b1120] border border-[#f59e0b]/40">
                  <span className="text-[10px] text-[#f59e0b] block">DUPLICATES SKIPPED</span>
                  <span className="text-sm font-bold text-[#f59e0b]">{importSummary.duplicateRowsSkipped}</span>
                </div>
                <div className="p-2.5 rounded bg-[#0b1120] border border-[#ef4444]/40">
                  <span className="text-[10px] text-[#ef4444] block">MALFORMED ROWS</span>
                  <span className="text-sm font-bold text-[#ef4444]">{importSummary.malformedRows}</span>
                </div>
              </div>

              {/* Malformed Row Details List */}
              {importSummary.malformedRowDetails && importSummary.malformedRowDetails.length > 0 ? (
                <div className="space-y-2">
                  <div className="flex items-center gap-1.5 text-xs font-mono text-[#ef4444]">
                    <AlertTriangle className="w-4 h-4" />
                    <span>MALFORMED ROWS IDENTIFIED ({importSummary.malformedRowDetails.length})</span>
                  </div>
                  <div className="max-h-52 overflow-y-auto space-y-1 p-2 rounded bg-[#080d1a] border border-[#ef4444]/30 text-[11px] font-mono text-[#ef4444]">
                    {importSummary.malformedRowDetails.map((detail, idx) => (
                      <div key={idx} className="p-1 rounded bg-[#ef4444]/10 border border-[#ef4444]/20">
                        {detail}
                      </div>
                    ))}
                  </div>
                </div>
              ) : (
                <div className="p-4 rounded bg-[#00e5a3]/10 border border-[#00e5a3]/30 text-xs font-mono text-[#00e5a3] flex items-center gap-2">
                  <CheckCircle2 className="w-5 h-5 shrink-0" />
                  <span>Zero malformed rows detected. All dataset rows validated successfully.</span>
                </div>
              )}
            </div>
          ) : (
            <div className="h-64 flex flex-col items-center justify-center text-center p-6 text-[#64748b] space-y-2">
              <Clock className="w-10 h-10 text-[#1e293b]" />
              <p className="text-xs font-mono">
                No active import execution. Select a CSV file and run ingestion to view diagnostics.
              </p>
            </div>
          )}
        </div>

      </div>

      {/* Dataset Sources & Provenance Metadata */}
      <div className="cyber-panel p-4 space-y-3">
        <span className="text-xs font-display font-bold text-white block">
          OFFICIAL DATASET SOURCES & PROVENANCE METADATA
        </span>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-3 text-xs font-mono">
          <div className="p-3 rounded bg-[#0b1120] border border-[#1e293b] space-y-1">
            <span className="text-[10px] text-[#00f0ff] font-bold block">SOURCE #1 (PRIMARY)</span>
            <h4 className="text-white font-bold">Tamil Nadu Police Annual Crime Review</h4>
            <p className="text-[#94a3b8] text-[11px]">SCRB Compendium of Statistics 2020-2023</p>
            <p className="text-[#64748b] text-[10px]">License: Government Open Data License - India (GODL)</p>
          </div>

          <div className="p-3 rounded bg-[#0b1120] border border-[#1e293b] space-y-1">
            <span className="text-[10px] text-[#00e5a3] font-bold block">SOURCE #2 (BENCHMARK)</span>
            <h4 className="text-white font-bold">NCRB Crime in India Series</h4>
            <p className="text-[#94a3b8] text-[11px]">Tamil Nadu District Chapters 2018-2022</p>
            <p className="text-[#64748b] text-[10px]">Publisher: Ministry of Home Affairs, New Delhi</p>
          </div>

          <div className="p-3 rounded bg-[#0b1120] border border-[#1e293b] space-y-1">
            <span className="text-[10px] text-[#f59e0b] font-bold block">SOURCE #3 (TEST SANDBOX)</span>
            <h4 className="text-white font-bold">CrimeScope Demonstration Baseline</h4>
            <p className="text-[#94a3b8] text-[11px]">Synthetic Sandbox for Edge-Case Testing</p>
            <p className="text-[#f59e0b] text-[10px]">Clearly tagged is_demonstration = true</p>
          </div>
        </div>
      </div>

    </div>
  );
}
