import { redirect } from 'next/navigation';
import { getCurrentUser, isAdmin } from '@/lib/auth';
import UserMenu from '@/components/UserMenu';
import UserManagement from '@/components/UserManagement';
import { Users, Shield } from 'lucide-react';
import Link from 'next/link';

export default async function UsersPage() {
  const user = await getCurrentUser();

  if (!user) {
    redirect('/login');
  }

  if (!isAdmin(user)) {
    redirect('/dashboard');
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
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white flex items-center">
              <Users className="w-6 h-6 mr-2" />
              User Management
            </h1>
          </div>
          <UserMenu user={user} />
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Admin Notice */}
        <div className="bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800 rounded-lg p-4 mb-6">
          <div className="flex items-center">
            <Shield className="h-5 w-5 text-yellow-600 mr-2" />
            <p className="text-sm text-yellow-700 dark:text-yellow-300">
              Admin access required. You can manage user roles from this page.
            </p>
          </div>
        </div>

        {/* User Management Component */}
        <UserManagement currentUserId={user.id} />
      </main>
    </div>
  );
}
