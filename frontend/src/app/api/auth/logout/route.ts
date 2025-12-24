import { cookies } from 'next/headers';
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
        await apiClient.logout(refreshToken, accessToken);
      } catch (error) {
        // Continue with logout even if backend call fails
        console.error('Backend logout error:', error);
      }
    }

    // Clear cookies
    cookieStore.delete('access_token');
    cookieStore.delete('refresh_token');

    return NextResponse.json({ success: true });
  } catch (error) {
    console.error('Logout error:', error);
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    );
  }
}
