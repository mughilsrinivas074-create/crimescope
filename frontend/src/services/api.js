// CrimeScope AI - Centralized Backend API Client
const API_BASE = 'http://localhost:8080/api';

class ApiService {
  constructor() {
    this.token = localStorage.getItem('crimescope_token') || null;
  }

  setToken(token) {
    this.token = token;
    if (token) {
      localStorage.setItem('crimescope_token', token);
    } else {
      localStorage.removeItem('crimescope_token');
    }
  }

  getToken() {
    return this.token;
  }

  getHeaders(isMultipart = false) {
    const headers = {};
    if (!isMultipart) {
      headers['Content-Type'] = 'application/json';
    }
    if (this.token) {
      headers['Authorization'] = `Bearer ${this.token}`;
    }
    return headers;
  }

  async request(endpoint, options = {}) {
    const url = `${API_BASE}${endpoint}`;
    const isMultipart = options.body instanceof FormData;
    const config = {
      ...options,
      headers: {
        ...this.getHeaders(isMultipart),
        ...options.headers,
      },
    };

    try {
      const response = await fetch(url, config);
      if (response.status === 204) return null;

      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        const data = await response.json();
        if (!response.ok) {
          throw new Error(data.message || data.error || `HTTP error ${response.status}`);
        }
        return data;
      } else {
        const text = await response.text();
        if (!response.ok) throw new Error(text || `HTTP error ${response.status}`);
        return text;
      }
    } catch (err) {
      console.warn(`[API] Request failed for ${endpoint}:`, err.message);
      throw err;
    }
  }

  // Auth
  async login(username, password) {
    const data = await this.request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    });
    if (data.token) {
      this.setToken(data.token);
    }
    return data;
  }

  async register(userData) {
    return this.request('/auth/register', {
      method: 'POST',
      body: JSON.stringify(userData),
    });
  }

  logout() {
    this.setToken(null);
  }

  // Districts & Categories
  async getDistricts() {
    return this.request('/districts');
  }

  async getCategories() {
    return this.request('/crime-categories');
  }

  // Analytics
  async getOverview(includeDemo = false) {
    return this.request(`/analytics/overview?includeDemo=${includeDemo}`);
  }

  async compareDistricts(districtIds = [], year = null, includeDemo = false) {
    let url = `/analytics/district-compare?includeDemo=${includeDemo}`;
    if (districtIds.length > 0) {
      url += `&districtIds=${districtIds.join(',')}`;
    }
    if (year) {
      url += `&year=${year}`;
    }
    return this.request(url);
  }

  async getTrends(districtId = null, categoryId = null, includeDemo = false) {
    let url = `/analytics/trends?includeDemo=${includeDemo}`;
    if (districtId) url += `&districtId=${districtId}`;
    if (categoryId) url += `&categoryId=${categoryId}`;
    return this.request(url);
  }

  async getCategoriesDistribution(districtId = null, year = null, includeDemo = false) {
    let url = `/analytics/categories?includeDemo=${includeDemo}`;
    if (districtId) url += `&districtId=${districtId}`;
    if (year) url += `&year=${year}`;
    return this.request(url);
  }

  // Records CRUD
  async getRecords(params = {}) {
    const query = new URLSearchParams();
    if (params.districtId) query.append('districtId', params.districtId);
    if (params.categoryId) query.append('categoryId', params.categoryId);
    if (params.startYear) query.append('startYear', params.startYear);
    if (params.endYear) query.append('endYear', params.endYear);
    if (params.includeDemo !== undefined) query.append('includeDemo', params.includeDemo);
    query.append('page', params.page || 0);
    query.append('size', params.size || 20);
    query.append('sortBy', params.sortBy || 'reportingYear');
    query.append('direction', params.direction || 'desc');

    return this.request(`/crime-records?${query.toString()}`);
  }

  async createRecord(recordDto) {
    return this.request('/crime-records', {
      method: 'POST',
      body: JSON.stringify(recordDto),
    });
  }

  async updateRecord(id, recordDto) {
    return this.request(`/crime-records/${id}`, {
      method: 'PUT',
      body: JSON.stringify(recordDto),
    });
  }

  async deleteRecord(id) {
    return this.request(`/crime-records/${id}`, {
      method: 'DELETE',
    });
  }

  // AI Intelligence
  async getGeminiStatus() {
    return this.request('/ai/status');
  }

  async validateGeminiKey(key) {
    return this.request('/ai/validate', {
      method: 'POST',
      body: JSON.stringify({ key }),
    });
  }

  async generateAiInsights(districtId = null, startYear = 2018, endYear = 2023) {
    return this.request('/ai/insights', {
      method: 'POST',
      body: JSON.stringify({ districtId, startYear, endYear }),
    });
  }

  async compareAiDistricts(districtCodeA, districtCodeB, year = 2023) {
    return this.request('/ai/compare', {
      method: 'POST',
      body: JSON.stringify({ districtCodeA, districtCodeB, year }),
    });
  }

  async explainAiTrends(districtId = null, startYear = 2018, endYear = 2023) {
    return this.request('/ai/trends', {
      method: 'POST',
      body: JSON.stringify({ districtId, startYear, endYear }),
    });
  }

  async askAiQuestion(question, districtId = null, year = null) {
    return this.request('/ai/ask', {
      method: 'POST',
      body: JSON.stringify({ question, districtId, year }),
    });
  }

  // Data Management & Import/Export
  async importCsv(file, sourceId = null) {
    const formData = new FormData();
    formData.append('file', file);
    if (sourceId) formData.append('sourceId', sourceId);

    return this.request('/data/import/csv', {
      method: 'POST',
      body: formData,
    });
  }

  getExportUrl(districtId = null, categoryId = null, startYear = null, endYear = null, includeDemo = false) {
    const query = new URLSearchParams();
    if (districtId) query.append('districtId', districtId);
    if (categoryId) query.append('categoryId', categoryId);
    if (startYear) query.append('startYear', startYear);
    if (endYear) query.append('endYear', endYear);
    query.append('includeDemo', includeDemo);
    return `${API_BASE}/data/export/csv?${query.toString()}`;
  }

  async downloadTemplate() {
    return this.request('/data/template');
  }

  // System & Telemetry
  async getSystemStatus() {
    return this.request('/system/status');
  }

  async getApiKeysStatus() {
    return this.request('/system/keys-status');
  }

  // Mapillary Street-Level Imagery & Coverage
  async getMapillaryStatus() {
    return this.request('/mapillary/status');
  }

  async getMapillaryConfig() {
    return this.request('/mapillary/config');
  }

  async getMapillaryImageryNear(lat, lng, radius = 1000) {
    return this.request(`/mapillary/imagery-near?lat=${lat}&lng=${lng}&radius=${radius}`);
  }

  async getMapillaryDistrictCoverage(districtCode) {
    return this.request(`/mapillary/district-coverage?districtCode=${districtCode}`);
  }

  async validateMapillaryToken(token) {
    return this.request('/mapillary/validate', {
      method: 'POST',
      body: JSON.stringify({ token }),
    });
  }

  async getAuditLogs(page = 0, size = 20) {
    return this.request(`/system/audit-logs?page=${page}&size=${size}`);
  }

  async runJdbcDemo(districtCode = 'CHN', year = 2023) {
    return this.request(`/system/jdbc-demo?districtCode=${districtCode}&year=${year}`);
  }
}

export const api = new ApiService();
export default api;
