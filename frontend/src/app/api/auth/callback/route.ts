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
  const dashboardUrl = `${baseUrl}/dashboard`;

  // Return HTML page that will redirect after cookies are set
  const html = `
    <!DOCTYPE html>
    <html>
      <head>
        <meta http-equiv="refresh" content="0;url=${dashboardUrl}">
        <title>Redirecting...</title>
      </head>
      <body>
        <p>Redirecting to dashboard...</p>
        <script>window.location.href = "${dashboardUrl}";</script>
      </body>
    </html>
  `;

  const response = new NextResponse(html, {
    status: 200,
    headers: {
      'Content-Type': 'text/html',
    },
  });

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
}
