'use client';

import { Suspense } from 'react';
import { useSearchParams } from 'next/navigation';
import OAuthButtons from '@/components/OAuthButtons';

/**
 * Login page content component.
 */
function LoginContent() {
  const searchParams = useSearchParams();
  const callbackUrl = searchParams.get('callbackUrl') || '/dashboard';
  const error = searchParams.get('error');

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            Sign in to Your Project
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            Use your account from the central auth server
          </p>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded-md">
            {error === 'auth_failed'
              ? 'Authentication failed. Please try again.'
              : error}
          </div>
        )}

        <OAuthButtons callbackUrl={callbackUrl} />

        <p className="mt-4 text-center text-xs text-gray-500">
          By signing in, you agree to our Terms of Service and Privacy Policy.
        </p>
      </div>
    </div>
  );
}

/**
 * Login page with OAuth buttons.
 */
export default function LoginPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen flex items-center justify-center bg-gray-50">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900"></div>
        </div>
      }
    >
      <LoginContent />
    </Suspense>
  );
}
