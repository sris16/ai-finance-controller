import React, { useMemo } from 'react';
import { ThemeProvider, CssBaseline, useMediaQuery, Box } from '@mui/material';
import { BrowserRouter } from 'react-router-dom';
import { darkTheme, lightTheme } from './theme/theme';
import { AppRoutes } from './routes/AppRoutes';
import { SettingsProvider, useSettings } from './context/SettingsContext';
import { AuthProvider } from './context/AuthContext';

import { StartupSplash } from './components/StartupSplash';

const AppContent: React.FC = () => {
  const { theme } = useSettings();
  const prefersDarkMode = useMediaQuery('(prefers-color-scheme: dark)');
  const [showSplash, setShowSplash] = React.useState(true);

  const currentTheme = useMemo(() => {
    if (theme === 'system') {
      return prefersDarkMode ? darkTheme : lightTheme;
    }
    return theme === 'light' ? lightTheme : darkTheme;
  }, [theme, prefersDarkMode]);

  return (
    <ThemeProvider theme={currentTheme}>
      <CssBaseline />
      {showSplash && <StartupSplash onComplete={() => setShowSplash(false)} />}
      <Box sx={{ display: showSplash ? 'none' : 'block', height: '100vh' }}>
        <BrowserRouter>
          <AppRoutes />
        </BrowserRouter>
      </Box>
    </ThemeProvider>
  );
};

export const App: React.FC = () => {
  return (
    <SettingsProvider>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </SettingsProvider>
  );
};

export default App;
