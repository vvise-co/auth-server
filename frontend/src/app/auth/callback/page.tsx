import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';
import Link from 'next/link';

interface PageProps {
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
}

export default async function AuthCallbackPage({ searchParams }: PageProps) {
  const params = await searchParams;
  const token = params.token as string | undefined;
  const refreshToken = params.refreshToken as string | undefined;
  const error = params.error as string | undefined;

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

  if (!token || !refreshToken) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900">
        <div className="max-w-md w-full space-y-8 p-8">
          <div className="text-center">
            <h2 className="text-2xl font-bold text-red-600">
              Authentication Error
            </h2>
            <p className="mt-2 text-gray-600 dark:text-gray-400">
              Missing authentication tokens
            </p>
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

  // Set cookies on the server
  const cookieStore = await cookies();
  const isProduction = process.env.NODE_ENV === 'production';

  cookieStore.set('access_token', token, {
    httpOnly: true,
    secure: isProduction,
    sameSite: 'lax',
    maxAge: 60 * 15, // 15 minutes
    path: '/',
  });

  cookieStore.set('refresh_token', refreshToken, {
    httpOnly: true,
    secure: isProduction,
    sameSite: 'lax',
    maxAge: 60 * 60 * 24 * 7, // 7 days
    path: '/',
  });

  // Redirect to dashboard
  redirect('/dashboard');
}
