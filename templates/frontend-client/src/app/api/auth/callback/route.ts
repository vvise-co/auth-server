import { NextRequest, NextResponse } from 'next/server';
import { cookieOptions } from '@/lib/auth-utils';

/**
 * API route to set auth cookies after OAuth callback.
 * This runs on the server and sets HTTP-only cookies.
 */
export async function POST(request: NextRequest) {
  try {
    const { token, refreshToken } = await request.json();

    if (!token || !refreshToken) {
      return NextResponse.json(
        { error: 'Missing tokens' },
        { status: 400 }
      );
    }

    const response = NextResponse.json({ success: true });

    // Set HTTP-only cookies
    response.cookies.set('access_token', token, {
      ...cookieOptions,
      maxAge: 60 * 15, // 15 minutes
    });

    response.cookies.set('refresh_token', refreshToken, {
      ...cookieOptions,
      maxAge: 60 * 60 * 24 * 7, // 7 days
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
