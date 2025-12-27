import { cookies, headers } from 'next/headers';
import { NextRequest, NextResponse } from 'next/server';
import { apiClient } from '@/lib/api';

export async function POST(request: NextRequest) {
  try {
    const cookieStore = await cookies();
    const accessToken = cookieStore.get('access_token')?.value;
    const refreshToken = cookieStore.get('refresh_token')?.value;

    // Call backend to invalidate tokens
    if (accessToken || refreshToken) {
      try {
        // Get base URL for server-side request
        const headersList = await headers();
        const host = headersList.get('x-forwarded-host') || headersList.get('host') || 'localhost:3000';
        const protocol = headersList.get('x-forwarded-proto') || 'http';
        const baseUrl = `${protocol}://${host}`;

        const client = apiClient.withBaseUrl(baseUrl);
        await client.logout(refreshToken, accessToken);
      } catch {
        // Continue with logout even if backend call fails
      }
    }

    // Create response with cookies cleared
    const response = NextResponse.json({ success: true });

    // Clear cookies by setting them with immediate expiration
    response.cookies.set('access_token', '', {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'lax',
      maxAge: 0,
      path: '/',
    });

    response.cookies.set('refresh_token', '', {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'lax',
      maxAge: 0,
      path: '/',
    });

    return response;
  } catch {
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    );
  }
}
