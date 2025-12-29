import { User } from './types';

// Auth server URL (for OAuth redirects and token validation)
const AUTH_SERVER_URL = import.meta.env.VITE_AUTH_SERVER_URL || '';

// Backend API URL (for this app's API calls - empty means same origin)
const API_URL = import.meta.env.VITE_API_URL || '';

class ApiClient {
  private baseUrl: string;

  constructor(baseUrl: string = '') {
    this.baseUrl = baseUrl;
  }

  async fetch<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    const url = `${this.baseUrl}${endpoint}`;
    const response = await fetch(url, {
      ...options,
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Request failed' }));
      throw new Error(error.message || `HTTP ${response.status}`);
    }

    return response.json();
  }

  async get<T>(endpoint: string): Promise<T> {
    return this.fetch<T>(endpoint, { method: 'GET' });
  }

  async post<T>(endpoint: string, data?: unknown): Promise<T> {
    return this.fetch<T>(endpoint, {
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  async delete<T>(endpoint: string): Promise<T> {
    return this.fetch<T>(endpoint, { method: 'DELETE' });
  }
}

// API client for this app's backend
export const api = new ApiClient(API_URL);

// API client for auth server
export const authApi = new ApiClient(AUTH_SERVER_URL);

export async function getCurrentUser(): Promise<User | null> {
  try {
    // Validate token with auth server's introspection endpoint
    return await authApi.get<User>('/api/auth/me');
  } catch {
    return null;
  }
}

export async function logout(): Promise<void> {
  // Logout from auth server (clears cookies)
  await authApi.post('/api/auth/logout');
}

export function getAuthServerUrl(): string {
  return AUTH_SERVER_URL;
}
