import { User } from './types';

/**
 * API client for making authenticated requests.
 * Works on both client and server side.
 */
class ApiClient {
  private backendUrl: string;
  private authServerUrl: string;

  constructor() {
    // Your project's backend API
    this.backendUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
    // Central auth server
    this.authServerUrl = process.env.NEXT_PUBLIC_AUTH_SERVER_URL || 'http://localhost:8081';
  }

  /**
   * Get headers for authenticated requests.
   */
  private getHeaders(accessToken?: string): HeadersInit {
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    };

    if (accessToken) {
      headers['Authorization'] = `Bearer ${accessToken}`;
    }

    return headers;
  }

  /**
   * Make a request to your project's backend.
   */
  async backendFetch<T>(
    endpoint: string,
    options: RequestInit & { accessToken?: string } = {}
  ): Promise<T> {
    const { accessToken, ...fetchOptions } = options;

    const response = await fetch(`${this.backendUrl}${endpoint}`, {
      ...fetchOptions,
      headers: {
        ...this.getHeaders(accessToken),
        ...fetchOptions.headers,
      },
      credentials: 'include',
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Request failed' }));
      throw new Error(error.message || `HTTP error ${response.status}`);
    }

    return response.json();
  }

  /**
   * Make a request to the auth server.
   */
  async authFetch<T>(
    endpoint: string,
    options: RequestInit & { accessToken?: string } = {}
  ): Promise<T> {
    const { accessToken, ...fetchOptions } = options;

    const response = await fetch(`${this.authServerUrl}${endpoint}`, {
      ...fetchOptions,
      headers: {
        ...this.getHeaders(accessToken),
        ...fetchOptions.headers,
      },
      credentials: 'include',
    });

    if (!response.ok) {
      const error = await response.json().catch(() => ({ message: 'Request failed' }));
      throw new Error(error.message || `HTTP error ${response.status}`);
    }

    return response.json();
  }

  /**
   * Get the current user from the auth server.
   */
  async getCurrentUser(accessToken: string): Promise<User> {
    return this.authFetch<User>('/api/auth/me', { accessToken });
  }

  /**
   * Get user by ID from the auth server.
   */
  async getUserById(userId: number, accessToken: string): Promise<User> {
    return this.authFetch<User>(`/api/users/${userId}`, { accessToken });
  }
}

// Export singleton instance
export const api = new ApiClient();
