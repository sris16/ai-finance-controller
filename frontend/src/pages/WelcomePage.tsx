import React from 'react';
import {
  Box,
  Container,
  Typography,
  Button,
  Stack,
  Chip,
  useTheme,
  IconButton,
  Tooltip,
} from '@mui/material';
import { alpha } from '@mui/material/styles';
import {
  ArrowRight,
  ShieldCheck,
  Sun,
  Moon,
  Lock,
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useSettings } from '../context/SettingsContext';

export const WelcomePage: React.FC = () => {
  const theme = useTheme();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const { theme: themeMode, setTheme } = useSettings();

  const handleCtaClick = () => {
    if (isAuthenticated) {
      navigate('/dashboard');
    } else {
      navigate('/login');
    }
  };

  const toggleTheme = () => {
    if (themeMode === 'dark') {
      setTheme('light');
    } else {
      setTheme('dark');
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        bgcolor: 'background.default',
        color: 'text.primary',
        display: 'flex',
        flexDirection: 'column',
        position: 'relative',
        overflowX: 'hidden',
      }}
    >
      {/* Subtle Background Glow */}
      <Box
        sx={{
          position: 'absolute',
          top: '-15%',
          left: '50%',
          transform: 'translateX(-50%)',
          width: { xs: 300, md: 700 },
          height: { xs: 300, md: 450 },
          background:
            theme.palette.mode === 'dark'
              ? 'radial-gradient(circle, rgba(59, 130, 246, 0.1) 0%, rgba(9, 9, 11, 0) 70%)'
              : 'radial-gradient(circle, rgba(59, 130, 246, 0.12) 0%, rgba(244, 244, 245, 0) 70%)',
          pointerEvents: 'none',
          zIndex: 0,
        }}
      />

      {/* Top Navigation Bar */}
      <Box
        component="header"
        sx={{
          borderBottom: '1px solid',
          borderColor: 'divider',
          bgcolor: (t) => alpha(t.palette.background.default, 0.85),
          backdropFilter: 'blur(16px)',
          position: 'sticky',
          top: 0,
          zIndex: 10,
        }}
      >
        <Container maxWidth="lg">
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              height: 64,
              px: { xs: 0.5, sm: 1 },
            }}
          >
            {/* Brand Logo */}
            <Box
              sx={{
                display: 'flex',
                alignItems: 'center',
                gap: 1.5,
                cursor: 'pointer',
              }}
              onClick={() => navigate('/')}
            >
              <Box
                sx={{
                  width: 34,
                  height: 34,
                  borderRadius: '9px',
                  background:
                    theme.palette.mode === 'dark'
                      ? 'linear-gradient(135deg, #e4e4e7 0%, #a1a1aa 100%)'
                      : 'linear-gradient(135deg, #18181b 0%, #3f3f46 100%)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  boxShadow:
                    theme.palette.mode === 'dark'
                      ? '0 2px 8px rgba(0,0,0,0.4)'
                      : '0 2px 8px rgba(0,0,0,0.15)',
                }}
              >
                <ShieldCheck
                  size={19}
                  color={theme.palette.mode === 'dark' ? '#09090b' : '#ffffff'}
                  strokeWidth={2.5}
                />
              </Box>
              <Box>
                <Typography
                  variant="h6"
                  sx={{
                    fontWeight: 700,
                    fontSize: '1rem',
                    letterSpacing: '-0.4px',
                    lineHeight: 1.1,
                  }}
                >
                  AI Finance Controller
                </Typography>
              </Box>
            </Box>

            {/* Right Header Actions */}
            <Stack direction="row" spacing={1.5} alignItems="center">
              <Tooltip title={`Switch to ${theme.palette.mode === 'dark' ? 'Light' : 'Dark'} Mode`}>
                <IconButton
                  size="small"
                  onClick={toggleTheme}
                  sx={{
                    color: 'text.secondary',
                    border: '1px solid',
                    borderColor: 'divider',
                    borderRadius: '8px',
                    p: 0.8,
                  }}
                  aria-label="toggle theme mode"
                >
                  {theme.palette.mode === 'dark' ? <Sun size={17} /> : <Moon size={17} />}
                </IconButton>
              </Tooltip>

              <Button
                variant={isAuthenticated ? 'contained' : 'outlined'}
                color="primary"
                size="small"
                onClick={handleCtaClick}
                startIcon={!isAuthenticated ? <Lock size={14} /> : undefined}
                endIcon={<ArrowRight size={14} />}
                sx={{
                  px: 1.8,
                  py: 0.6,
                  fontSize: '0.825rem',
                }}
              >
                {isAuthenticated ? 'Go to Dashboard' : 'Admin Sign In'}
              </Button>
            </Stack>
          </Box>
        </Container>
      </Box>

      {/* Main Hero Section */}
      <Container
        maxWidth="md"
        sx={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
          alignItems: 'center',
          py: { xs: 5, md: 7 },
          textAlign: 'center',
          zIndex: 1,
        }}
      >
        {/* Product Badge */}
        <Chip
          label="AI FINANCE CONTROLLER"
          size="small"
          sx={{
            mb: 2.5,
            px: 1.5,
            py: 1.8,
            borderRadius: '6px',
            bgcolor: (t) => alpha(t.palette.text.primary, 0.04),
            color: 'text.primary',
            border: '1px solid',
            borderColor: 'divider',
            fontWeight: 700,
            fontSize: '0.75rem',
            letterSpacing: '0.08em',
          }}
        />

        {/* Main Headline */}
        <Typography
          variant="h1"
          component="h1"
          sx={{
            fontWeight: 700,
            fontSize: { xs: '2rem', sm: '2.75rem', md: '3.25rem' },
            lineHeight: { xs: 1.2, md: 1.15 },
            letterSpacing: '-0.03em',
            mb: 2.5,
            maxWidth: 780,
          }}
        >
          Intelligent Financial Reconciliation
          <Box
            component="span"
            sx={{
              display: 'block',
              color: 'text.secondary',
              fontWeight: 600,
              fontSize: { xs: '1.6rem', sm: '2.25rem', md: '2.65rem' },
              mt: 0.5,
            }}
          >
            & Exception Intelligence
          </Box>
        </Typography>

        {/* Supporting Description */}
        <Typography
          variant="body1"
          color="text.secondary"
          sx={{
            fontSize: { xs: '0.95rem', sm: '1.05rem', md: '1.125rem' },
            lineHeight: 1.6,
            mb: 4,
            maxWidth: 620,
          }}
        >
          Reconcile orders, payments, settlements and bank transactions with deterministic accuracy
          and AI-assisted investigation.
        </Typography>

        {/* Primary CTA */}
        <Button
          variant="contained"
          color="primary"
          size="large"
          onClick={handleCtaClick}
          endIcon={<ArrowRight size={18} />}
          sx={{
            py: 1.4,
            px: 3.8,
            fontSize: '0.975rem',
            fontWeight: 600,
            borderRadius: '8px',
            boxShadow: (t) =>
              t.palette.mode === 'dark'
                ? '0 4px 14px rgba(0,0,0,0.5)'
                : '0 4px 14px rgba(0,0,0,0.15)',
          }}
        >
          Continue to Finance Controller
        </Button>
      </Container>

      {/* Minimal Footer */}
      <Box
        component="footer"
        sx={{
          borderTop: '1px solid',
          borderColor: 'divider',
          py: 2.5,
          bgcolor: (t) => alpha(t.palette.background.paper, 0.3),
        }}
      >
        <Container maxWidth="lg">
          <Box
            sx={{
              display: 'flex',
              flexDirection: { xs: 'column', sm: 'row' },
              alignItems: 'center',
              justifyContent: 'center',
              gap: 1.5,
              px: { xs: 0.5, sm: 1 },
            }}
          >
            <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 600, fontSize: '0.85rem' }}>
              AI Finance Controller
            </Typography>
          </Box>
        </Container>
      </Box>
    </Box>
  );
};
