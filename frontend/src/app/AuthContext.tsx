import { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import { api } from  '../services/api.client';

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

interface AuthState {
  isAuthenticated: boolean;
}

interface AuthContextType extends AuthState {
  login: (res: AuthResponse) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthState>({
    isAuthenticated: !!localStorage.getItem('jwt')
  });

  useEffect(() => {
    const handleAuthFailed = () => {
      setState({ isAuthenticated: false });
    };
    window.addEventListener('auth-failed', handleAuthFailed);
    return () => window.removeEventListener('auth-failed', handleAuthFailed);
  }, []);

  const login = (res: AuthResponse) => {
    localStorage.setItem('jwt', res.accessToken);
    localStorage.setItem('refresh_token', res.refreshToken);
    setState({ isAuthenticated: true });
  };

  const logout = async () => {
    try {
      const refreshToken = localStorage.getItem('refresh_token');
      await api.post('/auth/logout', { refreshToken });
    } catch (e) {
      console.warn('Logout request failed or unauthorized', e);
    } finally {
      localStorage.removeItem('jwt');
      localStorage.removeItem('refresh_token');
      setState({ isAuthenticated: false });
    }
  };

  return (
    <AuthContext.Provider value={{ ...state, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) throw new Error('useAuth must be used within AuthProvider');
  return context;
};
