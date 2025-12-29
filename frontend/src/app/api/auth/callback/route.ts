import { NextRequest, NextResponse } from 'next/server';

export async function POST(request: NextRequest) {
  try {
    const { token, refreshToken } = await request.json();

    if (!token || !refreshToken) {
      return NextResponse.json(
        { error: 'Missing tokens' },
        { status: 400 }
      );
    }

    const isProduction = process.env.NODE_ENV === 'production';
    const response = NextResponse.json({ success: true });

    // Set access token cookie
    response.cookies.set('access_token', token, {
      httpOnly: true,
      secure: isProduction,
      sameSite: 'lax',
      maxAge: 60 * 15, // 15 minutes
      path: '/',
    });

    // Set refresh token cookie
    response.cookies.set('refresh_token', refreshToken, {
      httpOnly: true,
      secure: isProduction,
      sameSite: 'lax',
      maxAge: 60 * 60 * 24 * 7, // 7 days
      path: '/',
    });

    return response;
  } catch (error) {
    console.error('Auth callback error:', error);
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    );
  }
}

// GET handler that sets cookies and redirects
export async function GET(request: NextRequest) {
  const searchParams = request.nextUrl.searchParams;
  const token = searchParams.get('token');
  const refreshToken = searchParams.get('refreshToken');

  // Get the host from headers (set by nginx proxy)
  const forwardedHost = request.headers.get('x-forwarded-host');
  const host = request.headers.get('host');
  const forwardedProto = request.headers.get('x-forwarded-proto');

  const finalHost = forwardedHost || host || 'localhost:3000';
  // Use forwarded proto if available, otherwise check if request looks like HTTPS
  const protocol = forwardedProto || (finalHost.includes('koyeb') || finalHost.includes('railway') ? 'https' : 'http');
  const baseUrl = `${protocol}://${finalHost}`;

  console.log('[Auth Callback] Headers:', { forwardedHost, host, forwardedProto, finalHost, baseUrl });
  console.log('[Auth Callback] Tokens present:', { token: !!token, refreshToken: !!refreshToken });

  if (!token || !refreshToken) {
    console.log('[Auth Callback] Missing tokens, redirecting to login');
    return NextResponse.redirect(new URL('/login?error=missing_tokens', baseUrl));
  }

  // For Secure cookies: use true if forwarded proto is https or if we're on a cloud platform
  const useSecureCookies = forwardedProto === 'https' || protocol === 'https';
  console.log('[Auth Callback] Cookie settings:', { useSecureCookies, protocol });

  // Create redirect response
  const response = NextResponse.redirect(new URL('/dashboard', baseUrl), 302);

  // Set cookies using the NextResponse cookies API
  response.cookies.set('access_token', token, {
    httpOnly: true,
    secure: useSecureCookies,
    sameSite: 'lax',
    maxAge: 60 * 15, // 15 minutes
    path: '/',
  });

  response.cookies.set('refresh_token', refreshToken, {
    httpOnly: true,
    secure: useSecureCookies,
    sameSite: 'lax',
    maxAge: 60 * 60 * 24 * 7, // 7 days
    path: '/',
  });

  console.log('[Auth Callback] Cookies set, redirecting to dashboard');
  return response;
}
