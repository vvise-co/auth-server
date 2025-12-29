import { User } from './types';

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

export const api = new ApiClient();

export async function getCurrentUser(): Promise<User | null> {
  try {
    return await api.get<User>('/api/auth/me');
  } catch {
    return null;
  }
}

export async function getUsers(): Promise<User[]> {
  return api.get<User[]>('/api/users');
}

export async function addAdminRole(userId: number): Promise<User> {
  return api.post<User>(`/api/users/${userId}/admin`);
}

export async function removeAdminRole(userId: number): Promise<User> {
  return api.delete<User>(`/api/users/${userId}/admin`);
}

export async function logout(): Promise<void> {
  await api.post('/api/auth/logout');
}
