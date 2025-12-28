'use client';

interface OAuthButtonsProps {
  callbackUrl?: string;
}

const AUTH_SERVER_URL = process.env.NEXT_PUBLIC_AUTH_SERVER_URL || 'http://localhost:8080';

/**
 * Login button that redirects to the central auth server's login page.
 * After authentication, the auth server redirects back to this app's callback URL.
 */
export default function OAuthButtons({ callbackUrl = '/dashboard' }: OAuthButtonsProps) {
  const handleLogin = () => {
    // Construct the callback URL for this app (where auth server redirects after login)
    const appCallbackUrl = `${window.location.origin}/auth/callback?callbackUrl=${encodeURIComponent(callbackUrl)}`;

    // Redirect to auth server's login page with redirect_uri
    window.location.href = `${AUTH_SERVER_URL}/login?redirect_uri=${encodeURIComponent(appCallbackUrl)}`;
  };

  return (
    <div className="space-y-3">
      <button
        onClick={handleLogin}
        className="w-full flex items-center justify-center px-4 py-3 border border-gray-300 rounded-md shadow-sm bg-primary-600 text-white hover:bg-primary-700 transition-colors font-medium"
      >
        <svg className="w-5 h-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1" />
        </svg>
        Sign in with Auth Server
      </button>
      <p className="text-center text-sm text-gray-500">
        You will be redirected to sign in with Google, GitHub, or Microsoft
      </p>
    </div>
  );
}
