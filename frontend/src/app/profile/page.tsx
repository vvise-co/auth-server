import { redirect } from 'next/navigation';
import { getCurrentUser } from '@/lib/auth';
import UserMenu from '@/components/UserMenu';
import { User, Mail, Shield, Calendar, ExternalLink } from 'lucide-react';
import Link from 'next/link';

export default async function ProfilePage() {
  const user = await getCurrentUser();

  if (!user) {
    redirect('/login');
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      {/* Header */}
      <header className="bg-white dark:bg-gray-800 shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
          <div className="flex items-center space-x-4">
            <Link href="/dashboard" className="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200">
              Dashboard
            </Link>
            <span className="text-gray-300 dark:text-gray-600">/</span>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
              Profile
            </h1>
          </div>
          <UserMenu user={user} />
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Profile Header */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6 mb-6">
          <div className="flex items-center space-x-6">
            {user.imageUrl ? (
              <img
                src={user.imageUrl}
                alt={user.name}
                className="h-24 w-24 rounded-full"
              />
            ) : (
              <div className="h-24 w-24 rounded-full bg-primary-100 dark:bg-primary-900 flex items-center justify-center">
                <User className="h-12 w-12 text-primary-600 dark:text-primary-400" />
              </div>
            )}
            <div>
              <h2 className="text-2xl font-bold text-gray-900 dark:text-white">
                {user.name}
              </h2>
              <p className="text-gray-500 dark:text-gray-400 flex items-center mt-1">
                <Mail className="w-4 h-4 mr-2" />
                {user.email}
              </p>
              <div className="flex items-center mt-2 space-x-2">
                {user.roles.map((role) => (
                  <span
                    key={role}
                    className={`px-3 py-1 rounded-full text-sm font-medium ${
                      role === 'ADMIN'
                        ? 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
                        : 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
                    }`}
                  >
                    {role}
                  </span>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Account Details */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow overflow-hidden">
          <div className="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
              Account Details
            </h3>
          </div>
          <div className="divide-y divide-gray-200 dark:divide-gray-700">
            {/* Email */}
            <div className="px-6 py-4 flex justify-between items-center">
              <div>
                <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Email</p>
                <p className="text-gray-900 dark:text-white">{user.email}</p>
              </div>
              <Mail className="w-5 h-5 text-gray-400" />
            </div>

            {/* Provider */}
            <div className="px-6 py-4 flex justify-between items-center">
              <div>
                <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Sign-in Provider</p>
                <p className="text-gray-900 dark:text-white capitalize">{user.provider}</p>
              </div>
              <ExternalLink className="w-5 h-5 text-gray-400" />
            </div>

            {/* Roles */}
            <div className="px-6 py-4 flex justify-between items-center">
              <div>
                <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Roles</p>
                <div className="flex flex-wrap gap-2 mt-1">
                  {user.roles.map((role) => (
                    <span
                      key={role}
                      className={`px-2 py-1 rounded text-xs font-medium ${
                        role === 'ADMIN'
                          ? 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200'
                          : 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
                      }`}
                    >
                      {role}
                    </span>
                  ))}
                </div>
              </div>
              <Shield className="w-5 h-5 text-gray-400" />
            </div>

            {/* Member Since */}
            <div className="px-6 py-4 flex justify-between items-center">
              <div>
                <p className="text-sm font-medium text-gray-500 dark:text-gray-400">Member Since</p>
                <p className="text-gray-900 dark:text-white">
                  {new Date(user.createdAt).toLocaleDateString('en-US', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric',
                  })}
                </p>
              </div>
              <Calendar className="w-5 h-5 text-gray-400" />
            </div>
          </div>
        </div>

        {/* Info Note */}
        <div className="mt-6 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
          <p className="text-sm text-blue-700 dark:text-blue-300">
            Your profile information is managed by your OAuth provider ({user.provider}).
            To update your name or profile picture, please update it in your {user.provider} account.
          </p>
        </div>
      </main>
    </div>
  );
}
