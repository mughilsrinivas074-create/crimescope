import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { CrimeProvider } from './context/CrimeContext';
import TopNavigation from './components/layout/TopNavigation';
import CommandCenterPage from './pages/CommandCenterPage';
import DistrictAnalyticsPage from './pages/DistrictAnalyticsPage';
import CrimeRecordsPage from './pages/CrimeRecordsPage';
import AiIntelligencePage from './pages/AiIntelligencePage';
import DataManagementPage from './pages/DataManagementPage';
import SystemStatusPage from './pages/SystemStatusPage';

export default function App() {
  return (
    <CrimeProvider>
      <BrowserRouter>
        <div className="min-h-screen flex flex-col bg-[#060911] text-[#f1f5f9]">
          <TopNavigation />
          <main className="flex-1 flex flex-col">
            <Routes>
              <Route path="/" element={<CommandCenterPage />} />
              <Route path="/district-analytics" element={<DistrictAnalyticsPage />} />
              <Route path="/crime-records" element={<CrimeRecordsPage />} />
              <Route path="/ai-intelligence" element={<AiIntelligencePage />} />
              <Route path="/data-management" element={<DataManagementPage />} />
              <Route path="/system-status" element={<SystemStatusPage />} />
            </Routes>
          </main>
        </div>
      </BrowserRouter>
    </CrimeProvider>
  );
}
