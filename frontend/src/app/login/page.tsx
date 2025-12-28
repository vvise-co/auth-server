import { redirect } from 'next/navigation';
import { cookies } from 'next/headers';
import { getCurrentUser } from '@/lib/auth';
import OAuthButtons from '@/components/OAuthButtons';

interface LoginPageProps {
  searchParams: Promise<{ redirect_uri?: string }>;
}

export default async function LoginPage({ searchParams }: LoginPageProps) {
  const user = await getCurrentUser();
  const params = await searchParams;
  const redirectUri = params.redirect_uri;

  if (user) {
    // If there's a redirect_uri, redirect there with a message to complete auth
    if (redirectUri) {
      redirect(redirectUri);
    }
    redirect('/dashboard');
  }

  // Store redirect_uri in cookie for the OAuth flow
  if (redirectUri) {
    const cookieStore = await cookies();
    cookieStore.set('oauth2_redirect_uri', redirectUri, {
      httpOnly: true,
      secure: process.env.NODE_ENV === 'production',
      sameSite: 'lax',
      path: '/',
      maxAge: 60 * 10, // 10 minutes
    });
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900 dark:text-white">
            Sign in to your account
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600 dark:text-gray-400">
            Choose your preferred sign-in method
          </p>
          {redirectUri && (
            <p className="mt-2 text-center text-xs text-gray-500 dark:text-gray-500">
              You will be redirected back after signing in
            </p>
          )}
        </div>

        <div className="mt-8 space-y-6">
          <OAuthButtons />
        </div>

        <div className="mt-6">
          <p className="text-center text-xs text-gray-500 dark:text-gray-400">
            By signing in, you agree to our Terms of Service and Privacy Policy
          </p>
        </div>
      </div>
    </div>
  );
}
