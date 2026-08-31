import React, { createContext, useContext, useState, useEffect } from 'react';

type ThemeMode = 'dark' | 'light' | 'system';

interface SettingsState {
  theme: ThemeMode;
  currency: string;
  pageSize: number;
  autoRefresh: boolean;
  aiEnabled: boolean;
}

interface SettingsContextType extends SettingsState {
  setTheme: (theme: ThemeMode) => void;
  setPageSize: (size: number) => void;
  setAutoRefresh: (refresh: boolean) => void;
  setAiEnabled: (enabled: boolean) => void;
}

const defaultSettings: SettingsState = {
  theme: 'dark',
  currency: 'INR',
  pageSize: 20,
  autoRefresh: true,
  aiEnabled: true,
};

const SettingsContext = createContext<SettingsContextType | undefined>(undefined);

export const SettingsProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [settings, setSettings] = useState<SettingsState>(() => {
    const saved = localStorage.getItem('aiFinanceSettings');
    if (saved) {
      try {
        return { ...defaultSettings, ...JSON.parse(saved) };
      } catch (e) {
        return defaultSettings;
      }
    }
    return defaultSettings;
  });

  useEffect(() => {
    localStorage.setItem('aiFinanceSettings', JSON.stringify(settings));
  }, [settings]);

  const setTheme = (theme: ThemeMode) => setSettings(s => ({ ...s, theme }));
  const setPageSize = (pageSize: number) => setSettings(s => ({ ...s, pageSize }));
  const setAutoRefresh = (autoRefresh: boolean) => setSettings(s => ({ ...s, autoRefresh }));
  const setAiEnabled = (aiEnabled: boolean) => setSettings(s => ({ ...s, aiEnabled }));

  return (
    <SettingsContext.Provider
      value={{
        ...settings,
        setTheme,
        setPageSize,
        setAutoRefresh,
        setAiEnabled,
      }}
    >
      {children}
    </SettingsContext.Provider>
  );
};

export const useSettings = () => {
  const context = useContext(SettingsContext);
  if (context === undefined) {
    throw new Error('useSettings must be used within a SettingsProvider');
  }
  return context;
};
