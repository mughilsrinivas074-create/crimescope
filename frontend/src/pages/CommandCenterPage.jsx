import React from 'react';
import LeftFilterPanel from '../components/layout/LeftFilterPanel';
import TamilNaduMap from '../components/map/TamilNaduMap';
import RightIntelPanel from '../components/analytics/RightIntelPanel';
import BottomTimeline from '../components/timeline/BottomTimeline';

export default function CommandCenterPage() {
  return (
    <div className="flex-1 max-w-[1920px] w-full mx-auto p-2 sm:p-3 flex flex-col gap-3 min-h-[calc(100vh-60px)]">
      
      {/* Upper 3-Panel Geospatial Layout */}
      <div className="flex-1 grid grid-cols-1 lg:grid-cols-12 gap-3 min-h-[520px]">
        
        {/* Left Filter Panel (Col 1 to 3) */}
        <div className="lg:col-span-3 h-[420px] lg:h-auto">
          <LeftFilterPanel />
        </div>

        {/* Center Tamil Nadu Interactive Map (Col 4 to 9) */}
        <div className="lg:col-span-6 h-[460px] lg:h-auto">
          <TamilNaduMap />
        </div>

        {/* Right District Intelligence & AI Panel (Col 10 to 12) */}
        <div className="lg:col-span-3 h-[420px] lg:h-auto">
          <RightIntelPanel />
        </div>
      </div>

      {/* Bottom Historical Timeline & Aggregates */}
      <div className="w-full shrink-0">
        <BottomTimeline />
      </div>

    </div>
  );
}
