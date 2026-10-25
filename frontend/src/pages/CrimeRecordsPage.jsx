import React, { useState, useEffect, useCallback } from 'react';
import { Database, Search, Filter, Download, Plus, Trash2, Eye, Edit2, AlertCircle } from 'lucide-react';
import { useCrime } from '../context/CrimeContext';
import api from '../services/api';
import MapillaryViewer from '../components/map/MapillaryViewer';

export default function CrimeRecordsPage() {
  const { districts, categories, user } = useCrime();

  // Filters & Pagination
  const [records, setRecords] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [size] = useState(15);
  const [selectedDistrictId, setSelectedDistrictId] = useState('');
  const [selectedCategoryId, setSelectedCategoryId] = useState('');
  const [selectedYear, setSelectedYear] = useState('');
  const [includeDemo, setIncludeDemo] = useState(true);
  const [loading, setLoading] = useState(false);

  // Modals state
  const [inspectModalRecord, setInspectModalRecord] = useState(null);
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [editModalRecord, setEditModalRecord] = useState(null);

  // Form State for Create/Edit
  const [formData, setFormData] = useState({
    districtId: '',
    categoryId: '',
    reportingYear: 2023,
    reportingMonth: '',
    caseCount: 10,
    arrestCount: 8,
    convictedCount: 3,
    chargesheetedCount: 8,
    locationDescription: '',
    reportingUnit: '',
    isDemonstration: false,
  });

  const loadRecords = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.getRecords({
        districtId: selectedDistrictId || null,
        categoryId: selectedCategoryId || null,
        startYear: selectedYear || null,
        endYear: selectedYear || null,
        includeDemo: includeDemo,
        page: page,
        size: size,
      });
      if (res && res.content) {
        setRecords(res.content);
        setTotalPages(res.totalPages);
        setTotalElements(res.totalElements);
      }
    } catch (err) {
      console.warn('Error fetching crime records:', err);
    } finally {
      setLoading(false);
    }
  }, [selectedDistrictId, selectedCategoryId, selectedYear, includeDemo, page, size]);

  useEffect(() => {
    loadRecords();
  }, [loadRecords]);

  // Handle Create Record
  const handleCreateRecord = async (e) => {
    e.preventDefault();
    try {
      await api.createRecord({
        ...formData,
        districtId: parseInt(formData.districtId),
        categoryId: parseInt(formData.categoryId),
        reportingYear: parseInt(formData.reportingYear),
        reportingMonth: formData.reportingMonth ? parseInt(formData.reportingMonth) : null,
        caseCount: parseInt(formData.caseCount),
        arrestCount: formData.arrestCount ? parseInt(formData.arrestCount) : 0,
        convictedCount: formData.convictedCount ? parseInt(formData.convictedCount) : 0,
        chargesheetedCount: formData.chargesheetedCount ? parseInt(formData.chargesheetedCount) : 0,
      });
      setCreateModalOpen(false);
      loadRecords();
    } catch (err) {
      alert('Error creating record: ' + err.message);
    }
  };

  // Handle Delete Record
  const handleDeleteRecord = async (id) => {
    if (!window.confirm(`Are you sure you want to permanently delete crime record #${id}? This action is audit-logged.`)) {
      return;
    }
    try {
      await api.deleteRecord(id);
      loadRecords();
    } catch (err) {
      alert('Delete failed: ' + err.message);
    }
  };

  return (
    <div className="max-w-[1720px] mx-auto p-4 space-y-4">
      
      {/* Page Header */}
      <div className="cyber-panel p-4 flex flex-col md:flex-row md:items-center justify-between gap-3">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <Database className="w-5 h-5 text-[#00f0ff]" />
            <h1 className="text-lg font-display font-bold text-white tracking-wider">
              CRIME RECORD MANAGEMENT REPOSITORY
            </h1>
          </div>
          <p className="text-xs font-mono text-[#94a3b8]">
            Authoritative registry of historical crime records for Tamil Nadu districts ({totalElements.toLocaleString()} entries).
          </p>
        </div>

        {/* Toolbar Buttons */}
        <div className="flex items-center gap-2">
          <a
            href={api.getExportUrl(selectedDistrictId, selectedCategoryId, selectedYear, selectedYear, includeDemo)}
            className="cyber-btn text-xs py-1.5 px-3 flex items-center gap-1.5 text-decoration-none"
            download
          >
            <Download className="w-3.5 h-3.5" />
            <span>EXPORT CSV</span>
          </a>

          <button
            onClick={() => {
              setFormData({
                districtId: districts[0]?.id || '',
                categoryId: categories[0]?.id || '',
                reportingYear: 2023,
                reportingMonth: '',
                caseCount: 25,
                arrestCount: 18,
                convictedCount: 6,
                chargesheetedCount: 20,
                locationDescription: 'Zonal Police Station Jurisdiction',
                reportingUnit: 'District Crime Branch',
                isDemonstration: false,
              });
              setCreateModalOpen(true);
            }}
            className="cyber-btn cyber-btn-teal text-xs py-1.5 px-3 flex items-center gap-1.5"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>ADD RECORD</span>
          </button>
        </div>
      </div>

      {/* Filter Toolbar */}
      <div className="cyber-panel p-3 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-2.5 items-center">
        <div>
          <label className="text-[10px] font-mono text-[#64748b] block mb-1">DISTRICT FILTER</label>
          <select
            value={selectedDistrictId}
            onChange={(e) => { setSelectedDistrictId(e.target.value); setPage(0); }}
            className="cyber-input text-xs py-1 px-2"
          >
            <option value="">All 38 Districts</option>
            {districts.map((d) => (
              <option key={d.id} value={d.id}>{d.code} - {d.name}</option>
            ))}
          </select>
        </div>

        <div>
          <label className="text-[10px] font-mono text-[#64748b] block mb-1">CATEGORY FILTER</label>
          <select
            value={selectedCategoryId}
            onChange={(e) => { setSelectedCategoryId(e.target.value); setPage(0); }}
            className="cyber-input text-xs py-1 px-2"
          >
            <option value="">All Categories</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>
        </div>

        <div>
          <label className="text-[10px] font-mono text-[#64748b] block mb-1">YEAR FILTER</label>
          <select
            value={selectedYear}
            onChange={(e) => { setSelectedYear(e.target.value); setPage(0); }}
            className="cyber-input text-xs py-1 px-2"
          >
            <option value="">All Years (2018-2023)</option>
            {[2023, 2022, 2021, 2020, 2019, 2018].map((yr) => (
              <option key={yr} value={yr}>{yr}</option>
            ))}
          </select>
        </div>

        <div className="flex items-center gap-2 pt-4">
          <input
            type="checkbox"
            id="demoToggle"
            checked={includeDemo}
            onChange={(e) => setIncludeDemo(e.target.checked)}
            className="cursor-pointer accent-[#f59e0b]"
          />
          <label htmlFor="demoToggle" className="text-xs font-mono text-[#94a3b8] cursor-pointer">
            Include Demo Sandbox Records
          </label>
        </div>

        <div className="text-right pt-4">
          <button
            onClick={() => {
              setSelectedDistrictId('');
              setSelectedCategoryId('');
              setSelectedYear('');
              setPage(0);
            }}
            className="text-xs font-mono text-[#00f0ff] hover:underline"
          >
            Clear Filters
          </button>
        </div>
      </div>

      {/* Main Records Table */}
      <div className="cyber-panel overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs font-mono">
            <thead>
              <tr className="bg-[#0b1120] text-[#00f0ff] border-b border-[#1e293b]">
                <th className="p-3">ID</th>
                <th className="p-3">DISTRICT</th>
                <th className="p-3">CLASSIFICATION</th>
                <th className="p-3">YEAR</th>
                <th className="p-3 text-right">CASES</th>
                <th className="p-3 text-right">ARRESTS</th>
                <th className="p-3 text-right">CONVICTED</th>
                <th className="p-3 text-right">CLEARANCE</th>
                <th className="p-3 text-center">STATUS</th>
                <th className="p-3 text-center">ACTIONS</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={10} className="p-8 text-center text-[#64748b]">
                    Accessing crime registry database...
                  </td>
                </tr>
              ) : records.length === 0 ? (
                <tr>
                  <td colSpan={10} className="p-8 text-center text-[#64748b]">
                    No crime records matching selected criteria.
                  </td>
                </tr>
              ) : (
                records.map((r, idx) => {
                  const clearance = r.caseCount > 0 && r.chargesheetedCount != null
                    ? ((r.chargesheetedCount / r.caseCount) * 100).toFixed(1)
                    : '---';

                  return (
                    <tr
                      key={r.id}
                      className={`border-b border-[#1e293b]/60 hover:bg-[#10192e] transition-colors ${
                        idx % 2 === 0 ? 'bg-[#060911]/60' : 'bg-[#080d1a]/60'
                      }`}
                    >
                      <td className="p-3 text-[#64748b]">#{r.id}</td>
                      <td className="p-3 font-bold text-white">
                        {r.district?.name} <span className="text-[#64748b] text-[10px]">({r.district?.code})</span>
                      </td>
                      <td className="p-3 text-[#cbd5e1]">{r.category?.name}</td>
                      <td className="p-3 text-[#00f0ff]">{r.reportingYear}</td>
                      <td className="p-3 text-right font-bold text-white">{r.caseCount?.toLocaleString()}</td>
                      <td className="p-3 text-right text-[#00e5a3]">{r.arrestCount?.toLocaleString() || '-'}</td>
                      <td className="p-3 text-right text-[#f59e0b]">{r.convictedCount?.toLocaleString() || '-'}</td>
                      <td className="p-3 text-right text-[#00f0ff]">{clearance !== '---' ? `${clearance}%` : '-'}</td>
                      <td className="p-3 text-center">
                        <span
                          className={`text-[9px] px-2 py-0.5 rounded font-mono ${
                            r.isDemonstration
                              ? 'bg-[#f59e0b]/15 text-[#f59e0b] border border-[#f59e0b]/40'
                              : 'bg-[#00e5a3]/15 text-[#00e5a3] border border-[#00e5a3]/40'
                          }`}
                        >
                          {r.isDemonstration ? 'DEMO' : 'VERIFIED'}
                        </span>
                      </td>
                      <td className="p-3 text-center">
                        <div className="flex items-center justify-center gap-1.5">
                          <button
                            onClick={() => setInspectModalRecord(r)}
                            className="p-1 rounded bg-[#10192e] text-[#00f0ff] hover:bg-[#00f0ff]/20"
                            title="Inspect Dossier"
                          >
                            <Eye className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={() => handleDeleteRecord(r.id)}
                            className="p-1 rounded bg-[#10192e] text-[#ef4444] hover:bg-[#ef4444]/20"
                            title="Delete Record"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Bar */}
        <div className="p-3 bg-[#0b1120] border-t border-[#1e293b] flex items-center justify-between text-xs font-mono">
          <span className="text-[#94a3b8]">
            Showing page {page + 1} of {Math.max(1, totalPages)} ({totalElements.toLocaleString()} records)
          </span>
          <div className="flex items-center gap-2">
            <button
              onClick={() => setPage(Math.max(0, page - 1))}
              disabled={page === 0}
              className="cyber-btn text-xs py-1 px-3 disabled:opacity-40"
            >
              PREV
            </button>
            <button
              onClick={() => setPage(page + 1)}
              disabled={page + 1 >= totalPages}
              className="cyber-btn text-xs py-1 px-3 disabled:opacity-40"
            >
              NEXT
            </button>
          </div>
        </div>
      </div>

      {/* Inspect Dossier Modal */}
      {inspectModalRecord && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-4 backdrop-blur-sm">
          <div className="cyber-panel p-5 max-w-lg w-full space-y-4">
            <div className="flex items-center justify-between border-b border-[#1e293b] pb-2">
              <h2 className="text-sm font-display font-bold text-[#00f0ff]">
                CRIME RECORD DOSSIER #{inspectModalRecord.id}
              </h2>
              <button
                onClick={() => setInspectModalRecord(null)}
                className="text-[#94a3b8] hover:text-white text-xs font-mono"
              >
                ✕ CLOSE
              </button>
            </div>

            <div className="space-y-2 text-xs font-mono text-[#cbd5e1]">
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <span className="text-[#64748b]">District: </span>
                  <span className="text-white font-bold">{inspectModalRecord.district?.name}</span>
                </div>
                <div>
                  <span className="text-[#64748b]">Classification: </span>
                  <span className="text-white font-bold">{inspectModalRecord.category?.name}</span>
                </div>
                <div>
                  <span className="text-[#64748b]">Reporting Year: </span>
                  <span className="text-white font-bold">{inspectModalRecord.reportingYear}</span>
                </div>
                <div>
                  <span className="text-[#64748b]">Month: </span>
                  <span className="text-white">{inspectModalRecord.reportingMonth || 'Annual Summary'}</span>
                </div>
                <div>
                  <span className="text-[#64748b]">FIR Cases: </span>
                  <span className="text-[#00f0ff] font-bold">{inspectModalRecord.caseCount?.toLocaleString()}</span>
                </div>
                <div>
                  <span className="text-[#64748b]">Arrests: </span>
                  <span className="text-[#00e5a3]">{inspectModalRecord.arrestCount?.toLocaleString() || '-'}</span>
                </div>
                <div>
                  <span className="text-[#64748b]">Convictions: </span>
                  <span className="text-[#f59e0b]">{inspectModalRecord.convictedCount?.toLocaleString() || '-'}</span>
                </div>
                <div>
                  <span className="text-[#64748b]">Chargesheeted: </span>
                  <span className="text-white">{inspectModalRecord.chargesheetedCount?.toLocaleString() || '-'}</span>
                </div>
              </div>

              <div className="border-t border-[#1e293b] pt-2 space-y-1">
                <div>
                  <span className="text-[#64748b]">Police Unit: </span>
                  <span>{inspectModalRecord.reportingUnit || 'District Police Office'}</span>
                </div>
                <div>
                  <span className="text-[#64748b]">Location Description: </span>
                  <span>{inspectModalRecord.locationDescription || 'Jurisdictional Headquarters'}</span>
                </div>
                <div>
                  <span className="text-[#64748b]">Source Reference: </span>
                  <span className="text-[#00f0ff]">{inspectModalRecord.sourceReference || 'SCRB Compendium'}</span>
                </div>
                <div>
                  <span className="text-[#64748b]">Data Integrity Hash: </span>
                  <span className="text-[10px] text-[#64748b] break-all">{inspectModalRecord.dataHash || 'N/A'}</span>
                </div>
              </div>

              {/* Mapillary Street-Level Imagery Section */}
              <div className="pt-2">
                <MapillaryViewer
                  latitude={inspectModalRecord.latitude || inspectModalRecord.district?.centerLat}
                  longitude={inspectModalRecord.longitude || inspectModalRecord.district?.centerLng}
                  locationName={inspectModalRecord.locationDescription || (inspectModalRecord.district?.name + ' District Jurisdiction')}
                  incidentTitle={`${inspectModalRecord.category?.name || 'Crime Incident'} (${inspectModalRecord.reportingYear})`}
                />
              </div>
            </div>

            <div className="text-right border-t border-[#1e293b] pt-3">
              <button
                onClick={() => setInspectModalRecord(null)}
                className="cyber-btn text-xs py-1 px-4"
              >
                DISMISS
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Create Record Modal */}
      {createModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/75 p-4 backdrop-blur-sm">
          <form onSubmit={handleCreateRecord} className="cyber-panel p-5 max-w-md w-full space-y-3">
            <div className="flex items-center justify-between border-b border-[#1e293b] pb-2">
              <h2 className="text-sm font-display font-bold text-[#00e5a3]">
                CREATE VERIFIED CRIME RECORD
              </h2>
              <button
                type="button"
                onClick={() => setCreateModalOpen(false)}
                className="text-[#94a3b8] hover:text-white text-xs font-mono"
              >
                ✕
              </button>
            </div>

            <div className="space-y-2 text-xs font-mono">
              <div>
                <label className="text-[#94a3b8] block mb-1">Target District</label>
                <select
                  value={formData.districtId}
                  onChange={(e) => setFormData({ ...formData, districtId: e.target.value })}
                  className="cyber-input text-xs"
                  required
                >
                  {districts.map((d) => (
                    <option key={d.id} value={d.id}>{d.name} ({d.code})</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="text-[#94a3b8] block mb-1">Crime Classification</label>
                <select
                  value={formData.categoryId}
                  onChange={(e) => setFormData({ ...formData, categoryId: e.target.value })}
                  className="cyber-input text-xs"
                  required
                >
                  {categories.map((c) => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="text-[#94a3b8] block mb-1">Year</label>
                  <input
                    type="number"
                    min="2000"
                    max="2030"
                    value={formData.reportingYear}
                    onChange={(e) => setFormData({ ...formData, reportingYear: e.target.value })}
                    className="cyber-input text-xs"
                    required
                  />
                </div>
                <div>
                  <label className="text-[#94a3b8] block mb-1">Case Count (FIRs)</label>
                  <input
                    type="number"
                    min="0"
                    value={formData.caseCount}
                    onChange={(e) => setFormData({ ...formData, caseCount: e.target.value })}
                    className="cyber-input text-xs"
                    required
                  />
                </div>
              </div>

              <div className="grid grid-cols-3 gap-2">
                <div>
                  <label className="text-[#94a3b8] block mb-1">Arrests</label>
                  <input
                    type="number"
                    min="0"
                    value={formData.arrestCount}
                    onChange={(e) => setFormData({ ...formData, arrestCount: e.target.value })}
                    className="cyber-input text-xs"
                  />
                </div>
                <div>
                  <label className="text-[#94a3b8] block mb-1">Convictions</label>
                  <input
                    type="number"
                    min="0"
                    value={formData.convictedCount}
                    onChange={(e) => setFormData({ ...formData, convictedCount: e.target.value })}
                    className="cyber-input text-xs"
                  />
                </div>
                <div>
                  <label className="text-[#94a3b8] block mb-1">Chargesheeted</label>
                  <input
                    type="number"
                    min="0"
                    value={formData.chargesheetedCount}
                    onChange={(e) => setFormData({ ...formData, chargesheetedCount: e.target.value })}
                    className="cyber-input text-xs"
                  />
                </div>
              </div>

              <div>
                <label className="text-[#94a3b8] block mb-1">Location Details / Sub-division</label>
                <input
                  type="text"
                  value={formData.locationDescription}
                  onChange={(e) => setFormData({ ...formData, locationDescription: e.target.value })}
                  placeholder="e.g. Central Crime Branch, Vepery"
                  className="cyber-input text-xs"
                />
              </div>
            </div>

            <div className="flex items-center justify-end gap-2 border-t border-[#1e293b] pt-3">
              <button
                type="button"
                onClick={() => setCreateModalOpen(false)}
                className="cyber-btn cyber-btn-muted text-xs py-1 px-3"
              >
                CANCEL
              </button>
              <button
                type="submit"
                className="cyber-btn cyber-btn-teal text-xs py-1 px-4"
              >
                SAVE RECORD
              </button>
            </div>
          </form>
        </div>
      )}

    </div>
  );
}
