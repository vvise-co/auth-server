import { cookies, headers } from 'next/headers';
import { apiClient } from './api';
import { User, AuthTokens } from './types';

const ACCESS_TOKEN_COOKIE = 'access_token';
const REFRESH_TOKEN_COOKIE = 'refresh_token';

// Get base URL for server-side API requests
async function getServerApiClient() {
  const API_URL = process.env.NEXT_PUBLIC_API_URL;
  if (API_URL) {
    return apiClient;
  }

  // For unified deployment, construct URL from headers
  const headersList = await headers();
  const host = headersList.get('x-forwarded-host') || headersList.get('host') || 'localhost:3000';
  const protocol = headersList.get('x-forwarded-proto') || 'http';
  const baseUrl = `${protocol}://${host}`;

  return apiClient.withBaseUrl(baseUrl);
}

export async function getTokens(): Promise<AuthTokens | null> {
  const cookieStore = await cookies();
  const accessToken = cookieStore.get(ACCESS_TOKEN_COOKIE)?.value;
  const refreshToken = cookieStore.get(REFRESH_TOKEN_COOKIE)?.value;

  if (!accessToken || !refreshToken) {
    return null;
  }

  return { accessToken, refreshToken };
}

export async function getCurrentUser(): Promise<User | null> {
  const tokens = await getTokens();

  if (!tokens) {
    return null;
  }

  try {
    const client = await getServerApiClient();
    const user = await client.getCurrentUser(tokens.accessToken);
    return user;
  } catch (error) {
    // Try to refresh the token
    try {
      const client = await getServerApiClient();
      const authResponse = await client.refreshToken(tokens.refreshToken);

      // Set new cookies (this will be done via API route)
      const user = await client.getCurrentUser(authResponse.accessToken);
      return user;
    } catch {
      return null;
    }
  }
}

export async function setAuthCookies(accessToken: string, refreshToken: string): Promise<void> {
  const cookieStore = await cookies();

  // Access token - shorter lived
  cookieStore.set(ACCESS_TOKEN_COOKIE, accessToken, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax',
    maxAge: 60 * 15, // 15 minutes
    path: '/',
  });

  // Refresh token - longer lived
  cookieStore.set(REFRESH_TOKEN_COOKIE, refreshToken, {
    httpOnly: true,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'lax',
    maxAge: 60 * 60 * 24 * 7, // 7 days
    path: '/',
  });
}

export async function clearAuthCookies(): Promise<void> {
  const cookieStore = await cookies();
  cookieStore.delete(ACCESS_TOKEN_COOKIE);
  cookieStore.delete(REFRESH_TOKEN_COOKIE);
}

export function isAdmin(user: User): boolean {
  return user.roles.includes('ADMIN');
}
