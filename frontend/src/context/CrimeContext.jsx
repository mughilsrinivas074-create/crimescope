import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import api from '../services/api';

const CrimeContext = createContext(null);

export function CrimeProvider({ children }) {
  const [districts, setDistricts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [selectedDistrict, setSelectedDistrict] = useState(null); // null = Statewide
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [yearRange, setYearRange] = useState([2018, 2023]);
  const [includeDemo, setIncludeDemo] = useState(false);
  const [datasetSource, setDatasetSource] = useState('ALL'); // 'ALL' | 'OFFICIAL' | 'DEMO'

  // Map display controls
  const [mapLayerMode, setMapLayerMode] = useState('choropleth'); // 'choropleth' | 'density' | 'markers'
  const [showBoundaries, setShowBoundaries] = useState(true);

  // Telemetry & Overview
  const [overviewData, setOverviewData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [backendOnline, setBackendOnline] = useState(false);

  // User auth state
  const [user, setUser] = useState(null);

  // Fetch metadata on mount
  const refreshMetadata = useCallback(async () => {
    try {
      const [distList, catList] = await Promise.all([
        api.getDistricts(),
        api.getCategories(),
      ]);
      setDistricts(Array.isArray(distList) ? distList : []);
      setCategories(Array.isArray(catList) ? catList : []);
      setBackendOnline(true);
    } catch (err) {
      console.warn('Initial metadata load fallback:', err.message);
      setBackendOnline(false);
    }
  }, []);

  // Fetch command overview data
  const refreshOverview = useCallback(async () => {
    setLoading(true);
    try {
      const data = await api.getOverview(includeDemo);
      setOverviewData(data);
      setBackendOnline(true);
    } catch (err) {
      console.warn('Overview fetch error:', err.message);
      setBackendOnline(false);
    } finally {
      setLoading(false);
    }
  }, [includeDemo]);

  useEffect(() => {
    refreshMetadata();
    refreshOverview();
  }, [refreshMetadata, refreshOverview]);

  // Auth actions
  const login = async (username, password) => {
    const res = await api.login(username, password);
    setUser({
      username: res.username,
      email: res.email,
      fullName: res.fullName,
      roles: res.roles,
    });
    return res;
  };

  const logout = () => {
    api.logout();
    setUser(null);
  };

  const value = {
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
    datasetSource,
    setDatasetSource,
    mapLayerMode,
    setMapLayerMode,
    showBoundaries,
    setShowBoundaries,
    overviewData,
    loading,
    backendOnline,
    refreshOverview,
    user,
    login,
    logout,
  };

  return <CrimeContext.Provider value={value}>{children}</CrimeContext.Provider>;
}

export function useCrime() {
  const context = useContext(CrimeContext);
  if (!context) {
    throw new Error('useCrime must be used within a CrimeProvider');
  }
  return context;
}
