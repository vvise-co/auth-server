import { Link } from 'react-router-dom';
import { Users, User, Shield } from 'lucide-react';
import UserMenu from '@/components/UserMenu';
import { useAuth } from '@/context/AuthContext';

export default function DashboardPage() {
  const { user, isAdmin } = useAuth();

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      {/* Header */}
      <header className="bg-white dark:bg-gray-800 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
          <h1 className="text-xl font-semibold text-gray-900 dark:text-gray-100">Auth Server</h1>
          <UserMenu />
        </div>
      </header>

      {/* Main content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h2 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Welcome, {user?.name}!
          </h2>
          <p className="text-gray-600 dark:text-gray-400">
            Manage your authentication and account settings.
          </p>
        </div>

        {/* User info card */}
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 mb-8">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
            Account Information
          </h3>
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <p className="text-sm text-gray-500 dark:text-gray-400">Email</p>
              <p className="text-gray-900 dark:text-gray-100">{user?.email}</p>
            </div>
            <div>
              <p className="text-sm text-gray-500 dark:text-gray-400">Provider</p>
              <p className="text-gray-900 dark:text-gray-100 capitalize">{user?.provider.toLowerCase()}</p>
            </div>
            <div>
              <p className="text-sm text-gray-500 dark:text-gray-400">Roles</p>
              <div className="flex gap-2 mt-1">
                {user?.roles.map((role) => (
                  <span
                    key={role}
                    className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
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
            <div>
              <p className="text-sm text-gray-500 dark:text-gray-400">Member since</p>
              <p className="text-gray-900 dark:text-gray-100">
                {user?.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'N/A'}
              </p>
            </div>
          </div>
        </div>

        {/* Quick actions */}
        <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
          Quick Actions
        </h3>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <Link
            to="/profile"
            className="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 hover:shadow-md transition-shadow flex items-center gap-4"
          >
            <div className="bg-primary-100 dark:bg-primary-900/30 p-3 rounded-lg">
              <User className="w-6 h-6 text-primary-600 dark:text-primary-400" />
            </div>
            <div>
              <h4 className="font-medium text-gray-900 dark:text-gray-100">Profile</h4>
              <p className="text-sm text-gray-500 dark:text-gray-400">View and edit your profile</p>
            </div>
          </Link>

          {isAdmin && (
            <Link
              to="/users"
              className="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 hover:shadow-md transition-shadow flex items-center gap-4"
            >
              <div className="bg-purple-100 dark:bg-purple-900/30 p-3 rounded-lg">
                <Users className="w-6 h-6 text-purple-600 dark:text-purple-400" />
              </div>
              <div>
                <h4 className="font-medium text-gray-900 dark:text-gray-100">User Management</h4>
                <p className="text-sm text-gray-500 dark:text-gray-400">Manage users and roles</p>
              </div>
            </Link>
          )}

          {isAdmin && (
            <div className="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-6 flex items-center gap-4 opacity-75">
              <div className="bg-green-100 dark:bg-green-900/30 p-3 rounded-lg">
                <Shield className="w-6 h-6 text-green-600 dark:text-green-400" />
              </div>
              <div>
                <h4 className="font-medium text-gray-900 dark:text-gray-100">Admin Panel</h4>
                <p className="text-sm text-gray-500 dark:text-gray-400">You have admin access</p>
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
