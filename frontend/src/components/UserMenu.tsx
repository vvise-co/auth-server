'use client';

import { useState } from 'react';
import { User } from '@/lib/types';
import { LogOut, User as UserIcon, ChevronDown } from 'lucide-react';

interface UserMenuProps {
  user: User;
}

export default function UserMenu({ user }: UserMenuProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  const handleLogout = async () => {
    setIsLoggingOut(true);
    try {
      await fetch('/api/auth/logout', {
        method: 'POST',
        credentials: 'include',
      });
      // Force full page navigation to ensure cookies are cleared
      window.location.replace('/login');
    } catch {
      setIsLoggingOut(false);
    }
  };

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center space-x-3 p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
      >
        {user.imageUrl ? (
          <img
            src={user.imageUrl}
            alt={user.name}
            className="h-8 w-8 rounded-full"
          />
        ) : (
          <div className="h-8 w-8 rounded-full bg-primary-100 dark:bg-primary-900 flex items-center justify-center">
            <UserIcon className="h-4 w-4 text-primary-600 dark:text-primary-400" />
          </div>
        )}
        <span className="hidden md:block text-sm font-medium text-gray-700 dark:text-gray-200">
          {user.name}
        </span>
        <ChevronDown className="h-4 w-4 text-gray-500" />
      </button>

      {isOpen && (
        <>
          {/* Backdrop */}
          <div
            className="fixed inset-0 z-10"
            onClick={() => setIsOpen(false)}
          />

          {/* Dropdown */}
          <div className="absolute right-0 mt-2 w-56 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 z-20">
            <div className="p-3 border-b border-gray-200 dark:border-gray-700">
              <p className="text-sm font-medium text-gray-900 dark:text-white">
                {user.name}
              </p>
              <p className="text-xs text-gray-500 dark:text-gray-400 truncate">
                {user.email}
              </p>
            </div>

            <div className="p-1">
              <button
                onClick={handleLogout}
                disabled={isLoggingOut}
                className="w-full flex items-center space-x-3 px-3 py-2 text-sm text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-md transition-colors disabled:opacity-50"
              >
                <LogOut className="h-4 w-4" />
                <span>{isLoggingOut ? 'Signing out...' : 'Sign out'}</span>
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
