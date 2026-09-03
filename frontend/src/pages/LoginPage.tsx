import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  TextField,
  Button,
  FormControlLabel,
  Checkbox,
  IconButton,
  InputAdornment,
  Alert,
  CircularProgress,
  Stack,
  useTheme,
  Tooltip,
} from '@mui/material';
import { alpha } from '@mui/material/styles';
import {
  ShieldCheck,
  Lock,
  Mail,
  Eye,
  EyeOff,
  ArrowRight,
  Sun,
  Moon,
  ArrowLeft,
  Info,
} from 'lucide-react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useSettings } from '../context/SettingsContext';

export const LoginPage: React.FC = () => {
  const theme = useTheme();
  const navigate = useNavigate();
  const location = useLocation();
  const { login, isAuthenticated } = useAuth();
  const { theme: themeMode, setTheme } = useSettings();

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(true);
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // If already authenticated, redirect to /dashboard
  useEffect(() => {
    if (isAuthenticated) {
      const from = (location.state as any)?.from?.pathname || '/dashboard';
      navigate(from, { replace: true });
    }
  }, [isAuthenticated, navigate, location]);

  const toggleTheme = () => {
    if (themeMode === 'dark') {
      setTheme('light');
    } else {
      setTheme('dark');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const result = await login(email, password, rememberMe);
      if (result.success) {
        const from = (location.state as any)?.from?.pathname || '/dashboard';
        navigate(from, { replace: true });
      } else {
        setError(result.error || 'Authentication failed. Please check your credentials.');
      }
    } catch {
      setError('An unexpected error occurred. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleFillDemoCredentials = () => {
    setEmail('admin@aifinance.com');
    setPassword('admin123');
    setError(null);
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
        overflow: 'hidden',
      }}
    >
      {/* Background Subtle Gradient Glow */}
      <Box
        sx={{
          position: 'absolute',
          top: '-10%',
          left: '50%',
          transform: 'translateX(-50%)',
          width: { xs: 300, md: 600 },
          height: { xs: 300, md: 450 },
          background:
            theme.palette.mode === 'dark'
              ? 'radial-gradient(circle, rgba(59, 130, 246, 0.12) 0%, rgba(9, 9, 11, 0) 70%)'
              : 'radial-gradient(circle, rgba(59, 130, 246, 0.12) 0%, rgba(244, 244, 245, 0) 70%)',
          pointerEvents: 'none',
          zIndex: 0,
        }}
      />

      {/* Top Header Controls */}
      <Box
        sx={{
          p: 3,
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          zIndex: 1,
        }}
      >
        <Button
          startIcon={<ArrowLeft size={16} />}
          onClick={() => navigate('/')}
          sx={{
            color: 'text.secondary',
            textTransform: 'none',
            fontSize: '0.875rem',
            '&:hover': { color: 'text.primary', bgcolor: 'transparent' },
          }}
        >
          Back to Welcome Page
        </Button>

        <Tooltip title={`Switch to ${theme.palette.mode === 'dark' ? 'Light' : 'Dark'} Mode`}>
          <IconButton
            size="small"
            onClick={toggleTheme}
            sx={{
              color: 'text.secondary',
              border: '1px solid',
              borderColor: 'divider',
              borderRadius: '8px',
              p: 1,
            }}
            aria-label="toggle theme mode"
          >
            {theme.palette.mode === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
          </IconButton>
        </Tooltip>
      </Box>

      {/* Center Login Container */}
      <Box
        sx={{
          flex: 1,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          p: 2.5,
          zIndex: 1,
        }}
      >
        <Card
          sx={{
            width: '100%',
            maxWidth: 440,
            p: { xs: 2.5, sm: 4 },
            border: '1px solid',
            borderColor: 'divider',
            borderRadius: '12px',
            bgcolor: 'background.paper',
            boxShadow:
              theme.palette.mode === 'dark'
                ? '0 12px 36px rgba(0, 0, 0, 0.6)'
                : '0 12px 36px rgba(0, 0, 0, 0.08)',
          }}
        >
          <CardContent sx={{ p: 0 }}>
            {/* Header Icon & Title */}
            <Box sx={{ textAlign: 'center', mb: 3.5 }}>
              <Box
                sx={{
                  width: 48,
                  height: 48,
                  borderRadius: '12px',
                  background:
                    theme.palette.mode === 'dark'
                      ? 'linear-gradient(135deg, #e4e4e7 0%, #a1a1aa 100%)'
                      : 'linear-gradient(135deg, #18181b 0%, #3f3f46 100%)',
                  display: 'inline-flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  mb: 2,
                  boxShadow:
                    theme.palette.mode === 'dark'
                      ? '0 4px 12px rgba(0,0,0,0.5)'
                      : '0 4px 12px rgba(0,0,0,0.15)',
                }}
              >
                <ShieldCheck
                  size={26}
                  color={theme.palette.mode === 'dark' ? '#09090b' : '#ffffff'}
                  strokeWidth={2.5}
                />
              </Box>

              <Typography
                variant="h4"
                component="h1"
                sx={{
                  fontWeight: 700,
                  fontSize: '1.5rem',
                  letterSpacing: '-0.5px',
                  mb: 0.5,
                }}
              >
                Admin Sign In
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Sign in to access the AI Finance Controller.
              </Typography>
            </Box>

            {/* Error Message */}
            {error && (
              <Alert
                severity="error"
                variant="outlined"
                sx={{
                  mb: 3,
                  borderRadius: '8px',
                  fontSize: '0.85rem',
                  borderColor: 'error.main',
                }}
              >
                {error}
              </Alert>
            )}

            {/* Form */}
            <Box component="form" onSubmit={handleSubmit} noValidate>
              <Stack spacing={2.5}>
                {/* Email Field */}
                <Box>
                  <Typography
                    variant="body2"
                    sx={{ fontWeight: 600, mb: 0.8, fontSize: '0.85rem' }}
                  >
                    Admin Email
                  </Typography>
                  <TextField
                    fullWidth
                    size="small"
                    id="admin-email"
                    name="email"
                    type="email"
                    placeholder="admin@aifinance.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    autoFocus
                    disabled={loading}
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <Mail size={16} color={theme.palette.text.secondary} />
                        </InputAdornment>
                      ),
                    }}
                    sx={{
                      '& .MuiOutlinedInput-root': {
                        bgcolor: 'background.default',
                        borderRadius: '8px',
                      },
                    }}
                  />
                </Box>

                {/* Password Field */}
                <Box>
                  <Typography
                    variant="body2"
                    sx={{ fontWeight: 600, mb: 0.8, fontSize: '0.85rem' }}
                  >
                    Password
                  </Typography>
                  <TextField
                    fullWidth
                    size="small"
                    id="admin-password"
                    name="password"
                    type={showPassword ? 'text' : 'password'}
                    placeholder="••••••••"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    disabled={loading}
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">
                          <Lock size={16} color={theme.palette.text.secondary} />
                        </InputAdornment>
                      ),
                      endAdornment: (
                        <InputAdornment position="end">
                          <IconButton
                            size="small"
                            onClick={() => setShowPassword(!showPassword)}
                            edge="end"
                            aria-label={showPassword ? 'hide password' : 'show password'}
                          >
                            {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                          </IconButton>
                        </InputAdornment>
                      ),
                    }}
                    sx={{
                      '& .MuiOutlinedInput-root': {
                        bgcolor: 'background.default',
                        borderRadius: '8px',
                      },
                    }}
                  />
                </Box>

                {/* Remember Me */}
                <Box
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                  }}
                >
                  <FormControlLabel
                    control={
                      <Checkbox
                        size="small"
                        checked={rememberMe}
                        onChange={(e) => setRememberMe(e.target.checked)}
                        color="primary"
                        sx={{ p: 0.5 }}
                      />
                    }
                    label={
                      <Typography variant="body2" color="text.secondary" sx={{ fontSize: '0.85rem' }}>
                        Remember me
                      </Typography>
                    }
                  />

                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, color: 'text.secondary' }}>
                    <Lock size={12} />
                    <Typography variant="caption" sx={{ fontSize: '0.75rem', fontWeight: 500 }}>
                      Admin access only
                    </Typography>
                  </Box>
                </Box>

                {/* Submit Button */}
                <Button
                  type="submit"
                  variant="contained"
                  color="primary"
                  fullWidth
                  size="large"
                  disabled={loading}
                  endIcon={!loading ? <ArrowRight size={16} /> : undefined}
                  sx={{
                    py: 1.2,
                    fontWeight: 600,
                    borderRadius: '8px',
                    fontSize: '0.95rem',
                  }}
                >
                  {loading ? <CircularProgress size={22} color="inherit" /> : 'Sign In'}
                </Button>
              </Stack>
            </Box>

            {/* Quick Demo Fill Helper */}
            <Box
              sx={{
                mt: 3,
                p: 2,
                borderRadius: '8px',
                bgcolor: (t) => alpha(t.palette.primary.main, 0.04),
                border: '1px dashed',
                borderColor: 'divider',
                display: 'flex',
                alignItems: 'flex-start',
                gap: 1.5,
              }}
            >
              <Info size={16} color={theme.palette.primary.main} style={{ marginTop: 2, flexShrink: 0 }} />
              <Box sx={{ flex: 1 }}>
                <Typography variant="caption" sx={{ display: 'block', fontWeight: 600, color: 'text.primary', mb: 0.3 }}>
                  Demo Admin Credentials
                </Typography>
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block', fontFamily: 'monospace', mb: 1 }}>
                  admin@aifinance.com / admin123
                </Typography>
                <Button
                  size="small"
                  variant="text"
                  onClick={handleFillDemoCredentials}
                  sx={{
                    p: 0,
                    fontSize: '0.75rem',
                    textTransform: 'none',
                    fontWeight: 600,
                    color: 'primary.main',
                    minWidth: 0,
                    '&:hover': { bgcolor: 'transparent', textDecoration: 'underline' },
                  }}
                >
                  Auto-fill demo credentials →
                </Button>
              </Box>
            </Box>
          </CardContent>
        </Card>
      </Box>

      {/* Footer info */}
      <Box sx={{ p: 2, textAlign: 'center', zIndex: 1 }}>
        <Typography variant="caption" color="text.secondary">
          AI Finance Controller • Razorpay AI Buildathon 2026
        </Typography>
      </Box>
    </Box>
  );
};
