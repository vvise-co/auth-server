import { User } from './types';

// Auth server URL (for OAuth redirects and token validation)
const AUTH_SERVER_URL = import.meta.env.VITE_AUTH_SERVER_URL || '';

// Backend API URL (for this app's API calls - empty means same origin)
const API_URL = import.meta.env.VITE_API_URL || '';

// Token storage keys
const ACCESS_TOKEN_KEY = 'access_token';
const REFRESH_TOKEN_KEY = 'refresh_token';

// Get stored access token
export function getAccessToken(): string | null {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

// Get stored refresh token
export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}

// Store tokens (called from AuthCallbackPage)
export function setTokens(accessToken: string, refreshToken?: string): void {
  localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  if (refreshToken) {
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  }
}

// Clear tokens (called on logout)
export function clearTokens(): void {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
}

class ApiClient {
  private baseUrl: string;
  private useAuth: boolean;

  constructor(baseUrl: string = '', useAuth: boolean = true) {
    this.baseUrl = baseUrl;
    this.useAuth = useAuth;
  }

  async fetch<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    const url = `${this.baseUrl}${endpoint}`;
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(options.headers as Record<string, string>),
    };

    // Add Authorization header if we have a token
    if (this.useAuth) {
      const token = getAccessToken();
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
    }

    const response = await fetch(url, {
      ...options,
      credentials: 'include',
      headers,
    });

    // Handle 401 - try to refresh token
    if (response.status === 401 && this.useAuth) {
      const refreshed = await this.tryRefreshToken();
      if (refreshed) {
        // Retry the request with new token
        const newToken = getAccessToken();
        if (newToken) {
          headers['Authorization'] = `Bearer ${newToken}`;
        }
        const retryResponse = await fetch(url, {
          ...options,
          credentials: 'include',
          headers,
        });
        if (retryResponse.ok) {
          return retryResponse.json();
        }
      }
      // Refresh failed, clear tokens
      clearTokens();
      throw new Error('Session expired');
    }

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Request failed' }));
      throw new Error(error.message || `HTTP ${response.status}`);
    }

    return response.json();
  }

  private async tryRefreshToken(): Promise<boolean> {
    const refreshToken = getRefreshToken();
    if (!refreshToken) return false;

    try {
      const response = await fetch(`${AUTH_SERVER_URL}/api/auth/refresh`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken }),
      });

      if (response.ok) {
        const data = await response.json();
        setTokens(data.accessToken, data.refreshToken);
        return true;
      }
    } catch {
      // Refresh failed
    }
    return false;
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

  async put<T>(endpoint: string, data?: unknown): Promise<T> {
    return this.fetch<T>(endpoint, {
      method: 'PUT',
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
    // First check if we have a token
    const token = getAccessToken();
    if (!token) {
      return null;
    }
    // Validate token with auth server
    return await authApi.get<User>('/api/auth/me');
  } catch {
    return null;
  }
}

export async function logout(): Promise<void> {
  try {
    // Logout from auth server
    await authApi.post('/api/auth/logout');
  } finally {
    // Always clear local tokens
    clearTokens();
  }
}

export function getAuthServerUrl(): string {
  return AUTH_SERVER_URL;
}
