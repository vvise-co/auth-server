import { User } from './types';

/**
 * Cookie options for auth tokens.
 * Can be imported in both client and server code.
 */
export const cookieOptions = {
  httpOnly: true,
  secure: process.env.NODE_ENV === 'production',
  sameSite: 'lax' as const,
  path: '/',
};

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
