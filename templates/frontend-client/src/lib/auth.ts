import { cookies } from 'next/headers';
import { User } from './types';

const AUTH_SERVER_URL = process.env.AUTH_SERVER_URL || 'http://localhost:8081';
const ACCESS_TOKEN_COOKIE = 'access_token';
const REFRESH_TOKEN_COOKIE = 'refresh_token';

/**
 * Get the current user from the auth server using stored tokens.
 * Use this in Server Components and API routes.
 */
export async function getCurrentUser(): Promise<User | null> {
  const cookieStore = await cookies();
  const accessToken = cookieStore.get(ACCESS_TOKEN_COOKIE)?.value;

  if (!accessToken) {
    return null;
  }

  try {
    const response = await fetch(`${AUTH_SERVER_URL}/api/auth/me`, {
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
      cache: 'no-store',
    });

    if (response.ok) {
      return response.json();
    }

    // Try to refresh the token
    const refreshToken = cookieStore.get(REFRESH_TOKEN_COOKIE)?.value;
    if (refreshToken) {
      const refreshed = await refreshAccessToken(refreshToken);
      if (refreshed) {
        return refreshed.user;
      }
    }

    return null;
  } catch (error) {
    console.error('Failed to get current user:', error);
    return null;
  }
}

/**
 * Refresh the access token using the refresh token.
 */
async function refreshAccessToken(
  refreshToken: string
): Promise<{ user: User; accessToken: string } | null> {
  try {
    const response = await fetch(`${AUTH_SERVER_URL}/api/auth/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ refreshToken }),
    });

    if (response.ok) {
      return response.json();
    }

    return null;
  } catch (error) {
    console.error('Failed to refresh token:', error);
    return null;
  }
}

/**
 * Check if user has a specific role.
 */
export function hasRole(user: User | null, role: string): boolean {
  return user?.roles?.includes(role.toUpperCase()) ?? false;
}

/**
 * Check if user is an admin.
 */
export function isAdmin(user: User | null): boolean {
  return hasRole(user, 'ADMIN');
}

/**
 * Get the OAuth2 login URL for a provider.
 */
export function getLoginUrl(provider: string, callbackUrl?: string): string {
  const callback = callbackUrl || `${process.env.NEXT_PUBLIC_APP_URL}/auth/callback`;
  // The auth server will redirect back with tokens
  return `${AUTH_SERVER_URL}/oauth2/authorization/${provider}?redirect_uri=${encodeURIComponent(callback)}`;
}

/**
 * Cookie options for auth tokens.
 */
export const cookieOptions = {
  httpOnly: true,
  secure: process.env.NODE_ENV === 'production',
  sameSite: 'lax' as const,
  path: '/',
};
