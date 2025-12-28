'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { User, LogOut, Settings, ChevronDown } from 'lucide-react';
import type { User as UserType } from '@/lib/types';

interface UserMenuProps {
  user: UserType;
}

const AUTH_SERVER_URL = process.env.NEXT_PUBLIC_AUTH_SERVER_URL || 'http://localhost:8080';

export default function UserMenu({ user }: UserMenuProps) {
  const [isOpen, setIsOpen] = useState(false);
  const router = useRouter();

  const handleLogout = async () => {
    try {
      // Clear local cookies first
      await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' });

      // Redirect to auth server logout, which will redirect back to our login page
      const returnUrl = `${window.location.origin}/login`;
      window.location.href = `${AUTH_SERVER_URL}/api/auth/logout?redirect_uri=${encodeURIComponent(returnUrl)}`;
    } catch {
      // If local logout fails, still redirect to auth server logout
      const returnUrl = `${window.location.origin}/login`;
      window.location.href = `${AUTH_SERVER_URL}/api/auth/logout?redirect_uri=${encodeURIComponent(returnUrl)}`;
    }
  };

  const handleProfile = () => {
    setIsOpen(false);
    // Redirect to auth server profile management
    window.location.href = `${AUTH_SERVER_URL}/profile`;
  };

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center space-x-2 p-2 rounded-lg hover:bg-gray-100 transition-colors"
      >
        {user.avatarUrl ? (
          <img
            src={user.avatarUrl}
            alt={user.name}
            className="w-8 h-8 rounded-full"
          />
        ) : (
          <div className="w-8 h-8 rounded-full bg-gray-300 flex items-center justify-center">
            <User className="w-4 h-4 text-gray-600" />
          </div>
        )}
        <span className="text-sm font-medium text-gray-700">{user.name}</span>
        <ChevronDown className="w-4 h-4 text-gray-500" />
      </button>

      {isOpen && (
        <>
          <div
            className="fixed inset-0 z-10"
            onClick={() => setIsOpen(false)}
          />
          <div className="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg z-20 py-1 border">
            <div className="px-4 py-2 border-b">
              <p className="text-sm font-medium text-gray-900">{user.name}</p>
              <p className="text-xs text-gray-500">{user.email}</p>
            </div>

            <button
              onClick={handleProfile}
              className="w-full flex items-center px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
            >
              <Settings className="w-4 h-4 mr-2" />
              Profile
            </button>

            <button
              onClick={handleLogout}
              className="w-full flex items-center px-4 py-2 text-sm text-red-600 hover:bg-red-50"
            >
              <LogOut className="w-4 h-4 mr-2" />
              Sign out
            </button>
          </div>
        </>
      )}
    </div>
  );
}
