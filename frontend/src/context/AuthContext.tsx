import React, { createContext, useContext, useState } from 'react';

export interface AuthUser {
  email: string;
  name: string;
  role: string;
}

interface AuthContextType {
  isAuthenticated: boolean;
  user: AuthUser | null;
  login: (email: string, password: string, rememberMe?: boolean) => Promise<{ success: boolean; error?: string }>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const AUTH_STORAGE_KEY = 'ai_finance_auth';

// Configurable credentials with demo defaults
const DEFAULT_ADMIN_EMAIL = import.meta.env.VITE_ADMIN_EMAIL || 'admin@aifinance.com';
const DEFAULT_ADMIN_PASSWORD = import.meta.env.VITE_ADMIN_PASSWORD || 'admin123';

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<AuthUser | null>(() => {
    try {
      const storedLocal = localStorage.getItem(AUTH_STORAGE_KEY);
      if (storedLocal) {
        return JSON.parse(storedLocal);
      }
      const storedSession = sessionStorage.getItem(AUTH_STORAGE_KEY);
      if (storedSession) {
        return JSON.parse(storedSession);
      }
    } catch {
      // Ignore parse errors
    }
    return null;
  });

  const isAuthenticated = !!user;

  const login = async (
    email: string,
    password: string,
    rememberMe: boolean = false
  ): Promise<{ success: boolean; error?: string }> => {
    // Artificial small delay for realistic UX and button state
    await new Promise((resolve) => setTimeout(resolve, 350));

    const trimmedEmail = email.trim().toLowerCase();
    const expectedEmail = DEFAULT_ADMIN_EMAIL.trim().toLowerCase();
    const expectedPassword = DEFAULT_ADMIN_PASSWORD;

    if (!trimmedEmail || !password) {
      return { success: false, error: 'Email and password are required.' };
    }

    if (trimmedEmail !== expectedEmail || password !== expectedPassword) {
      return { success: false, error: 'Invalid admin credentials. Please try again.' };
    }

    const authUser: AuthUser = {
      email: trimmedEmail,
      name: 'Administrator',
      role: 'Finance Operations Lead',
    };

    if (rememberMe) {
      localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(authUser));
      sessionStorage.removeItem(AUTH_STORAGE_KEY);
    } else {
      sessionStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(authUser));
      localStorage.removeItem(AUTH_STORAGE_KEY);
    }

    setUser(authUser);
    return { success: true };
  };

  const logout = () => {
    localStorage.removeItem(AUTH_STORAGE_KEY);
    sessionStorage.removeItem(AUTH_STORAGE_KEY);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
