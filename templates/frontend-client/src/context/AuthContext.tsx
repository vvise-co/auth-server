import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User } from '@/lib/types';
import { getCurrentUser, logout as apiLogout, getAuthServerUrl } from '@/lib/api';

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (provider?: 'google' | 'github' | 'microsoft') => void;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
  isAdmin: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchUser = async () => {
    try {
      const userData = await getCurrentUser();
      setUser(userData);
    } catch {
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUser();
  }, []);

  const login = (provider: 'google' | 'github' | 'microsoft' = 'google') => {
    // Redirect to auth server's OAuth endpoint with callback to this app
    const callbackUrl = `${window.location.origin}/auth/callback`;
    const authServerUrl = getAuthServerUrl();
    window.location.href = `${authServerUrl}/oauth2/authorization/${provider}?redirect_uri=${encodeURIComponent(callbackUrl)}`;
  };

  const logout = async () => {
    try {
      await apiLogout();
    } finally {
      setUser(null);
      window.location.href = '/login';
    }
  };

  const refreshUser = async () => {
    setLoading(true);
    await fetchUser();
  };

  const isAdmin = user?.roles?.includes('ADMIN') ?? false;

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, refreshUser, isAdmin }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
