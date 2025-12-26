/**
 * User data from the auth server.
 */
export interface User {
  id: number;
  email: string;
  name: string;
  avatarUrl: string | null;
  roles: string[];
  provider: string;
}

/**
 * Authentication response from the auth server.
 */
export interface AuthResponse {
  user: User;
  accessToken: string;
  refreshToken: string;
}

/**
 * API error response.
 */
export interface ApiError {
  message: string;
  status: number;
}
