import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // Skip middleware for API routes and static files
  if (pathname.startsWith('/api/') || pathname.startsWith('/_next/')) {
    return NextResponse.next();
  }

  // Get auth tokens from cookies
  const accessToken = request.cookies.get('access_token')?.value;
  const refreshToken = request.cookies.get('refresh_token')?.value;
  const isAuthenticated = !!(accessToken && refreshToken);

  // Protected routes - redirect to login if not authenticated
  if (pathname.startsWith('/dashboard')) {
    if (!isAuthenticated) {
      return NextResponse.redirect(new URL('/login', request.url));
    }
  }

  // Login page handling
  if (pathname === '/login') {
    // Redirect to dashboard if already authenticated
    if (isAuthenticated) {
      const redirectUri = request.nextUrl.searchParams.get('redirect_uri');
      if (redirectUri) {
        return NextResponse.redirect(new URL(redirectUri));
      }
      return NextResponse.redirect(new URL('/dashboard', request.url));
    }

    // Store redirect_uri in cookie for OAuth flow (if provided)
    const redirectUri = request.nextUrl.searchParams.get('redirect_uri');
    if (redirectUri) {
      const response = NextResponse.next();
      response.cookies.set('oauth2_redirect_uri', redirectUri, {
        httpOnly: true,
        secure: process.env.NODE_ENV === 'production',
        sameSite: 'lax',
        path: '/',
        maxAge: 60 * 10, // 10 minutes
      });
      return response;
    }
  }

  return NextResponse.next();
}

export const config = {
  matcher: ['/dashboard/:path*', '/login'],
};
