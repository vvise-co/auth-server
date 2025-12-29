import { Link } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import UserMenu from '@/components/UserMenu';
import { useAuth } from '@/context/AuthContext';

export default function ProfilePage() {
  const { user } = useAuth();

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      {/* Header */}
      <header className="bg-white dark:bg-gray-800 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
          <div className="flex items-center gap-4">
            <Link
              to="/dashboard"
              className="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors"
            >
              <ArrowLeft className="w-5 h-5 text-gray-600 dark:text-gray-400" />
            </Link>
            <h1 className="text-xl font-semibold text-gray-900 dark:text-gray-100">Profile</h1>
          </div>
          <UserMenu />
        </div>
      </header>

      {/* Main content */}
      <main className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden">
          {/* Profile header */}
          <div className="bg-gradient-to-r from-primary-500 to-primary-600 px-6 py-8">
            <div className="flex items-center gap-4">
              {user?.imageUrl ? (
                <img
                  src={user.imageUrl}
                  alt={user.name}
                  className="w-20 h-20 rounded-full border-4 border-white shadow-lg"
                />
              ) : (
                <div className="w-20 h-20 rounded-full bg-white flex items-center justify-center text-primary-600 text-2xl font-bold shadow-lg">
                  {user?.name?.charAt(0).toUpperCase() || 'U'}
                </div>
              )}
              <div className="text-white">
                <h2 className="text-2xl font-bold">{user?.name}</h2>
                <p className="opacity-90">{user?.email}</p>
              </div>
            </div>
          </div>

          {/* Profile details */}
          <div className="p-6 space-y-6">
            <div>
              <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
                Account Details
              </h3>
              <dl className="grid gap-4 sm:grid-cols-2">
                <div className="bg-gray-50 dark:bg-gray-700/50 rounded-lg p-4">
                  <dt className="text-sm text-gray-500 dark:text-gray-400">Full Name</dt>
                  <dd className="mt-1 text-gray-900 dark:text-gray-100 font-medium">{user?.name}</dd>
                </div>
                <div className="bg-gray-50 dark:bg-gray-700/50 rounded-lg p-4">
                  <dt className="text-sm text-gray-500 dark:text-gray-400">Email Address</dt>
                  <dd className="mt-1 text-gray-900 dark:text-gray-100 font-medium">{user?.email}</dd>
                </div>
                <div className="bg-gray-50 dark:bg-gray-700/50 rounded-lg p-4">
                  <dt className="text-sm text-gray-500 dark:text-gray-400">Auth Provider</dt>
                  <dd className="mt-1 text-gray-900 dark:text-gray-100 font-medium capitalize">
                    {user?.provider.toLowerCase()}
                  </dd>
                </div>
                <div className="bg-gray-50 dark:bg-gray-700/50 rounded-lg p-4">
                  <dt className="text-sm text-gray-500 dark:text-gray-400">Account Created</dt>
                  <dd className="mt-1 text-gray-900 dark:text-gray-100 font-medium">
                    {user?.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'N/A'}
                  </dd>
                </div>
              </dl>
            </div>

            <div>
              <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">Roles</h3>
              <div className="flex flex-wrap gap-2">
                {user?.roles.map((role) => (
                  <span
                    key={role}
                    className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${
                      role === 'ADMIN'
                        ? 'bg-purple-100 dark:bg-purple-900/30 text-purple-800 dark:text-purple-300'
                        : 'bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-300'
                    }`}
                  >
                    {role}
                  </span>
                ))}
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
