import { createTheme } from '@mui/material/styles';

export const darkTheme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#e4e4e7', // zinc-200, highly visible but professional
      light: '#ffffff',
      dark: '#a1a1aa',
      contrastText: '#09090b',
    },
    secondary: {
      main: '#3b82f6', // blue-500
      light: '#60a5fa',
      dark: '#2563eb',
      contrastText: '#ffffff',
    },
    success: {
      main: '#10b981', // emerald-500
      light: '#34d399',
      dark: '#059669',
    },
    error: {
      main: '#f43f5e', // rose-500
      light: '#fb7185',
      dark: '#e11d48',
    },
    warning: {
      main: '#f59e0b', // amber-500
      light: '#fbbf24',
      dark: '#d97706',
    },
    info: {
      main: '#0ea5e9', // sky-500
    },
    background: {
      default: '#09090b', // zinc-950, very deep black/gray
      paper: '#121214',   // slightly lighter for cards
    },
    text: {
      primary: '#fafafa', // zinc-50
      secondary: '#a1a1aa', // zinc-400
    },
    divider: 'rgba(255, 255, 255, 0.08)',
  },
  typography: {
    fontFamily: '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
    h1: { fontFamily: '"Outfit", sans-serif', fontWeight: 600, fontSize: '2.5rem', letterSpacing: '-0.02em' },
    h2: { fontFamily: '"Outfit", sans-serif', fontWeight: 600, fontSize: '2rem', letterSpacing: '-0.02em' },
    h3: { fontFamily: '"Outfit", sans-serif', fontWeight: 600, fontSize: '1.75rem', letterSpacing: '-0.02em' },
    h4: { fontFamily: '"Outfit", sans-serif', fontWeight: 600, fontSize: '1.5rem', letterSpacing: '-0.01em' },
    h5: { fontFamily: '"Outfit", sans-serif', fontWeight: 600, fontSize: '1.25rem' },
    h6: { fontFamily: '"Outfit", sans-serif', fontWeight: 600, fontSize: '1.125rem' },
    body1: { fontSize: '0.9375rem', lineHeight: 1.6 },
    body2: { fontSize: '0.875rem', lineHeight: 1.5 },
    subtitle1: { fontSize: '1rem', fontWeight: 500, letterSpacing: '-0.01em' },
    subtitle2: { fontSize: '0.875rem', fontWeight: 600, letterSpacing: '0.01em', textTransform: 'uppercase' },
    button: { textTransform: 'none', fontWeight: 600 },
  },
  shape: {
    borderRadius: 8,
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          backgroundColor: '#09090b',
          color: '#fafafa',
          WebkitFontSmoothing: 'antialiased',
          MozOsxFontSmoothing: 'grayscale',
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: '6px',
          padding: '8px 16px',
          transition: 'all 0.2s ease',
        },
        contained: {
          boxShadow: '0 1px 2px rgba(0, 0, 0, 0.4), inset 0 1px 0 rgba(255, 255, 255, 0.1)',
          '&:hover': {
            boxShadow: '0 2px 4px rgba(0, 0, 0, 0.5), inset 0 1px 0 rgba(255, 255, 255, 0.1)',
            transform: 'translateY(-1px)',
          },
        },
        outlined: {
          borderColor: 'rgba(255, 255, 255, 0.15)',
          '&:hover': {
            backgroundColor: 'rgba(255, 255, 255, 0.05)',
            borderColor: 'rgba(255, 255, 255, 0.3)',
          }
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
          backgroundColor: '#121214',
          border: '1px solid rgba(255, 255, 255, 0.08)',
          boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
          borderRadius: '10px',
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
        },
        elevation1: {
          boxShadow: '0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06)',
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: {
          borderBottom: '1px solid rgba(255, 255, 255, 0.06)',
          padding: '16px',
          color: '#e4e4e7',
          fontSize: '0.875rem',
        },
        head: {
          color: '#a1a1aa',
          fontWeight: 600,
          backgroundColor: '#09090b', // Match main bg to look sleek
          textTransform: 'uppercase',
          fontSize: '0.75rem',
          letterSpacing: '0.05em',
        },
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          '&:last-child td, &:last-child th': {
            borderBottom: 0,
          },
          '&.MuiTableRow-hover:hover': {
            backgroundColor: 'rgba(255, 255, 255, 0.02)',
          }
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          fontWeight: 600,
          borderRadius: '4px',
        },
      },
    },
  },
});
