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
  const protocol = forwardedProto || 'https';
  const baseUrl = `${protocol}://${finalHost}`;

  if (!token || !refreshToken) {
    return NextResponse.redirect(new URL('/login?error=missing_tokens', baseUrl));
  }

  const isProduction = process.env.NODE_ENV === 'production';

  // Build Set-Cookie headers manually for more control
  const cookieOptions = [
    `HttpOnly`,
    `Path=/`,
    `SameSite=Lax`,
    `Max-Age=${60 * 60 * 24 * 7}`, // 7 days
    isProduction ? `Secure` : '',
  ].filter(Boolean).join('; ');

  const accessCookie = `access_token=${token}; ${cookieOptions.replace('Max-Age=604800', 'Max-Age=900')}`;
  const refreshCookie = `refresh_token=${refreshToken}; ${cookieOptions}`;

  // Use 302 redirect with manually set cookies
  const response = NextResponse.redirect(new URL('/dashboard', baseUrl), 302);

  response.headers.append('Set-Cookie', accessCookie);
  response.headers.append('Set-Cookie', refreshCookie);

  return response;
}
