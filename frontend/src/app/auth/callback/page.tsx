'use client';

import { useEffect, useState } from 'react';
import { useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { Suspense } from 'react';

function AuthCallbackContent() {
  const searchParams = useSearchParams();
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = searchParams.get('token');
    const refreshToken = searchParams.get('refreshToken');
    const errorParam = searchParams.get('error');

    console.log('[Auth Callback Page] Params:', {
      hasToken: !!token,
      hasRefreshToken: !!refreshToken,
      error: errorParam
    });

    if (errorParam) {
      setError(errorParam);
      return;
    }

    if (!token || !refreshToken) {
      console.log('[Auth Callback Page] Missing tokens');
      setError('Missing authentication tokens');
      return;
    }

    console.log('[Auth Callback Page] Calling API to set cookies...');

    // Use the API route to set cookies, then redirect
    fetch('/api/auth/callback', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ token, refreshToken }),
      credentials: 'include',
    })
      .then((res) => {
        console.log('[Auth Callback Page] API response:', res.status, res.ok);
        if (res.ok) {
          console.log('[Auth Callback Page] Success! Redirecting to dashboard...');
          // Force full page reload to ensure cookies are sent
          window.location.replace('/dashboard');
        } else {
          console.log('[Auth Callback Page] API returned error');
          setError('Failed to authenticate');
        }
      })
      .catch((err) => {
        console.error('[Auth Callback Page] Fetch error:', err);
        setError('Failed to authenticate');
      });
  }, [searchParams]);

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
        <div className="max-w-md w-full space-y-8 p-8">
          <div className="text-center">
            <h2 className="text-2xl font-bold text-red-600">
              Authentication Error
            </h2>
            <p className="mt-2 text-gray-600 dark:text-gray-400">{error}</p>
            <Link
              href="/login"
              className="mt-4 inline-block px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700"
            >
              Back to Login
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary-600 mx-auto"></div>
        <p className="mt-4 text-gray-600 dark:text-gray-400">
          Completing sign in...
        </p>
      </div>
    </div>
  );
}

export default function AuthCallbackPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary-600 mx-auto"></div>
            <p className="mt-4 text-gray-600 dark:text-gray-400">Loading...</p>
          </div>
        </div>
      }
    >
      <AuthCallbackContent />
    </Suspense>
  );
}
