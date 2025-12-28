import { redirect } from 'next/navigation';
import { getCurrentUser, isAdmin } from '@/lib/auth';
import UserMenu from '@/components/UserMenu';
import { Shield, User, Calendar, Mail, Users, UserCog } from 'lucide-react';
import Link from 'next/link';

export default async function DashboardPage() {
  const user = await getCurrentUser();

  if (!user) {
    redirect('/login');
  }

  const userIsAdmin = isAdmin(user);

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      {/* Header */}
      <header className="bg-white dark:bg-gray-800 shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
            Dashboard
          </h1>
          <UserMenu user={user} />
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Welcome Section */}
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6 mb-8">
          <div className="flex items-center space-x-4">
            {user.imageUrl ? (
              <img
                src={user.imageUrl}
                alt={user.name}
                className="h-16 w-16 rounded-full"
              />
            ) : (
              <div className="h-16 w-16 rounded-full bg-primary-100 dark:bg-primary-900 flex items-center justify-center">
                <User className="h-8 w-8 text-primary-600 dark:text-primary-400" />
              </div>
            )}
            <div>
              <h2 className="text-xl font-semibold text-gray-900 dark:text-white">
                Welcome back, {user.name}!
              </h2>
              <p className="text-gray-500 dark:text-gray-400">
                Signed in with {user.provider}
              </p>
            </div>
          </div>
        </div>

        {/* User Info Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {/* Email Card */}
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center space-x-3 mb-4">
              <Mail className="h-6 w-6 text-primary-600" />
              <h3 className="text-lg font-medium text-gray-900 dark:text-white">
                Email
              </h3>
            </div>
            <p className="text-gray-600 dark:text-gray-300">{user.email}</p>
          </div>

          {/* Roles Card */}
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center space-x-3 mb-4">
              <Shield className="h-6 w-6 text-primary-600" />
              <h3 className="text-lg font-medium text-gray-900 dark:text-white">
                Roles
              </h3>
            </div>
            <div className="flex flex-wrap gap-2">
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

          {/* Member Since Card */}
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
            <div className="flex items-center space-x-3 mb-4">
              <Calendar className="h-6 w-6 text-primary-600" />
              <h3 className="text-lg font-medium text-gray-900 dark:text-white">
                Member Since
              </h3>
            </div>
            <p className="text-gray-600 dark:text-gray-300">
              {new Date(user.createdAt).toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
              })}
            </p>
          </div>
        </div>

        {/* Quick Actions */}
        <div className="mt-8">
          <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-4">
            Quick Actions
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {/* Profile Link */}
            <Link
              href="/profile"
              className="flex items-center p-4 bg-white dark:bg-gray-800 rounded-lg shadow hover:shadow-md transition-shadow"
            >
              <UserCog className="h-8 w-8 text-primary-600 mr-4" />
              <div>
                <h4 className="font-medium text-gray-900 dark:text-white">Profile Settings</h4>
                <p className="text-sm text-gray-500 dark:text-gray-400">Manage your account</p>
              </div>
            </Link>

            {/* User Management (Admin only) */}
            {userIsAdmin && (
              <Link
                href="/users"
                className="flex items-center p-4 bg-white dark:bg-gray-800 rounded-lg shadow hover:shadow-md transition-shadow border-2 border-yellow-200 dark:border-yellow-800"
              >
                <Users className="h-8 w-8 text-yellow-600 mr-4" />
                <div>
                  <h4 className="font-medium text-gray-900 dark:text-white">User Management</h4>
                  <p className="text-sm text-gray-500 dark:text-gray-400">Manage all users</p>
                </div>
              </Link>
            )}
          </div>
        </div>

        {/* Admin Section */}
        {userIsAdmin && (
          <div className="mt-8">
            <div className="bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-lg p-6">
              <div className="flex items-center space-x-3 mb-4">
                <Shield className="h-6 w-6 text-yellow-600" />
                <h3 className="text-lg font-medium text-yellow-800 dark:text-yellow-200">
                  Admin Access
                </h3>
              </div>
              <p className="text-yellow-700 dark:text-yellow-300">
                You have administrator privileges. You can manage users and
                system settings.
              </p>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
