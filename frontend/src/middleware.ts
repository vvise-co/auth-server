import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

// Paths that don't require authentication
const publicPaths = ['/login', '/auth/callback', '/api/auth/callback', '/api/auth', '/api/'];

// Paths that require authentication
const protectedPaths = ['/dashboard'];

function getBaseUrl(request: NextRequest): string {
  const host = request.headers.get('x-forwarded-host') || request.headers.get('host') || 'localhost:3000';
  const protocol = request.headers.get('x-forwarded-proto') || 'http';
  return `${protocol}://${host}`;
}

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // Skip middleware for API routes entirely - they handle their own auth
  if (pathname.startsWith('/api/')) {
    return NextResponse.next();
  }

  const baseUrl = getBaseUrl(request);

  // Check if the path is public
  const isPublicPath = publicPaths.some(
    (path) => pathname === path || pathname.startsWith(path + '/')
  );

  // Check if the path is protected
  const isProtectedPath = protectedPaths.some(
    (path) => pathname === path || pathname.startsWith(path + '/')
  );

  // Get auth tokens from cookies
  const accessToken = request.cookies.get('access_token')?.value;
  const refreshToken = request.cookies.get('refresh_token')?.value;

  const isAuthenticated = accessToken && refreshToken;

  // Redirect authenticated users away from login page
  if (isPublicPath && isAuthenticated && pathname === '/login') {
    return NextResponse.redirect(new URL('/dashboard', baseUrl));
  }

  // Redirect unauthenticated users to login
  if (isProtectedPath && !isAuthenticated) {
    const loginUrl = new URL('/login', baseUrl);
    loginUrl.searchParams.set('redirect', pathname);
    return NextResponse.redirect(loginUrl);
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     * - public folder
     */
    '/((?!_next/static|_next/image|favicon.ico|public).*)',
  ],
};
