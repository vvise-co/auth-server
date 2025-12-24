import { redirect } from 'next/navigation';
import { getCurrentUser } from '@/lib/auth';
import OAuthButtons from '@/components/OAuthButtons';

export default async function LoginPage() {
  const user = await getCurrentUser();

  if (user) {
    redirect('/dashboard');
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
