import { AuthResponse, User } from './types';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

class ApiClient {
  private baseUrl: string;

  constructor(baseUrl: string) {
    this.baseUrl = baseUrl;
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${this.baseUrl}${endpoint}`;

    const response = await fetch(url, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      credentials: 'include',
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ error: 'Request failed' }));
      throw new Error(error.error || 'Request failed');
    }

    return response.json();
  }

  async getCurrentUser(accessToken: string): Promise<User> {
    return this.request<User>('/api/auth/me', {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    });
  }

  async refreshToken(refreshToken: string): Promise<AuthResponse> {
    return this.request<AuthResponse>('/api/auth/refresh', {
      method: 'POST',
      body: JSON.stringify({ refreshToken }),
    });
  }

  async logout(refreshToken?: string, accessToken?: string): Promise<void> {
    await this.request('/api/auth/logout', {
      method: 'POST',
      headers: accessToken
        ? { Authorization: `Bearer ${accessToken}` }
        : {},
      body: refreshToken ? JSON.stringify({ refreshToken }) : '{}',
    });
  }

  getOAuthUrl(provider: 'google' | 'github' | 'microsoft'): string {
    return `${this.baseUrl}/oauth2/authorization/${provider}`;
  }
}

export const apiClient = new ApiClient(API_URL);
export { API_URL };
