/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import apiClient from '../api/client';

type AuthState = 'loading' | 'authenticated' | 'unauthenticated';

interface AuthContextValue {
  authState: AuthState;
  setAuthenticated: () => void;
  setUnauthenticated: () => void;
}

const AuthContext = createContext<AuthContextValue>({
  authState: 'loading',
  setAuthenticated: () => {},
  setUnauthenticated: () => {},
});

/**
 * 認証状態をアプリ全体で共有するプロバイダー。
 * マウント時に /api/users/me を呼び出してCookieが有効かチェックする。
 * HttpOnly Cookie はJSから読めないため、APIで認証状態を確認する必要がある。
 */
export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [authState, setAuthState] = useState<AuthState>('loading');

  useEffect(() => {
    apiClient.get('/api/users/me')
      .then(() => setAuthState('authenticated'))
      .catch(() => setAuthState('unauthenticated'));
  }, []);

  return (
    <AuthContext.Provider value={{
      authState,
      setAuthenticated: () => setAuthState('authenticated'),
      setUnauthenticated: () => setAuthState('unauthenticated'),
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
