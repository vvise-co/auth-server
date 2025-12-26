import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

/**
 * Public paths that don't require authentication.
 * Customize this list for your project.
 */
const PUBLIC_PATHS = [
  '/',
  '/login',
  '/auth/callback',
  '/api/auth/callback',
  '/api/auth/logout',
];

/**
 * Paths that authenticated users should be redirected away from.
 */
const AUTH_REDIRECT_PATHS = ['/login'];

/**
 * Middleware to protect routes and handle authentication redirects.
 */
export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  const accessToken = request.cookies.get('access_token')?.value;

  // Check if the path is public
  const isPublicPath = PUBLIC_PATHS.some(
    (path) => pathname === path || pathname.startsWith('/api/public')
  );

  // Check if this is an auth redirect path
  const isAuthRedirectPath = AUTH_REDIRECT_PATHS.some((path) => pathname === path);

  // Redirect authenticated users away from login page
  if (accessToken && isAuthRedirectPath) {
    return NextResponse.redirect(new URL('/dashboard', request.url));
  }

  // Allow public paths
  if (isPublicPath) {
    return NextResponse.next();
  }

  // Redirect unauthenticated users to login
  if (!accessToken) {
    const loginUrl = new URL('/login', request.url);
    loginUrl.searchParams.set('callbackUrl', pathname);
    return NextResponse.redirect(loginUrl);
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * Match all request paths except:
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     * - public folder
     */
    '/((?!_next/static|_next/image|favicon.ico|public/).*)',
  ],
};
