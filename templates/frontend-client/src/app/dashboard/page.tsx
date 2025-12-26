import { redirect } from 'next/navigation';
import { getCurrentUser, isAdmin } from '@/lib/auth';
import UserMenu from '@/components/UserMenu';

export default async function DashboardPage() {
  const user = await getCurrentUser();

  if (!user) {
    redirect('/login');
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <h1 className="text-xl font-semibold text-gray-900">
              Your Project Dashboard
            </h1>
            <UserMenu user={user} />
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-medium text-gray-900 mb-4">
            Welcome, {user.name}!
          </h2>
          <p className="text-gray-600 mb-4">
            You are authenticated via the central auth server.
          </p>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="border rounded-lg p-4">
              <h3 className="font-medium text-gray-900">User Info</h3>
              <dl className="mt-2 text-sm">
                <div className="flex justify-between py-1">
                  <dt className="text-gray-500">Email:</dt>
                  <dd className="text-gray-900">{user.email}</dd>
                </div>
                <div className="flex justify-between py-1">
                  <dt className="text-gray-500">Provider:</dt>
                  <dd className="text-gray-900">{user.provider}</dd>
                </div>
                <div className="flex justify-between py-1">
                  <dt className="text-gray-500">Roles:</dt>
                  <dd className="text-gray-900">{user.roles.join(', ')}</dd>
                </div>
              </dl>
            </div>

            {isAdmin(user) && (
              <div className="border rounded-lg p-4 bg-blue-50 border-blue-200">
                <h3 className="font-medium text-blue-900">Admin Panel</h3>
                <p className="mt-2 text-sm text-blue-700">
                  You have admin privileges. Add your admin features here.
                </p>
              </div>
            )}
          </div>
        </div>

        {/* Add your project-specific content below */}
        <div className="mt-8 bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-medium text-gray-900 mb-4">
            Your Project Features
          </h2>
          <p className="text-gray-500">
            Add your project-specific components and features here.
          </p>
        </div>
      </main>
    </div>
  );
}
