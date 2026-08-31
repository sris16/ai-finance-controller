import React, { useMemo } from 'react';
import { ThemeProvider, CssBaseline, useMediaQuery } from '@mui/material';
import { BrowserRouter } from 'react-router-dom';
import { darkTheme, lightTheme } from './theme/theme';
import { AppRoutes } from './routes/AppRoutes';
import { SettingsProvider, useSettings } from './context/SettingsContext';

const AppContent: React.FC = () => {
  const { theme } = useSettings();
  const prefersDarkMode = useMediaQuery('(prefers-color-scheme: dark)');

  const currentTheme = useMemo(() => {
    if (theme === 'system') {
      return prefersDarkMode ? darkTheme : lightTheme;
    }
    return theme === 'light' ? lightTheme : darkTheme;
  }, [theme, prefersDarkMode]);

  return (
    <ThemeProvider theme={currentTheme}>
      <CssBaseline />
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </ThemeProvider>
  );
};

export const App: React.FC = () => {
  return (
    <SettingsProvider>
      <AppContent />
    </SettingsProvider>
  );
};

export default App;
