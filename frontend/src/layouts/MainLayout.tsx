import React, { useState } from 'react';
import {
  Box,
  AppBar,
  Toolbar,
  Typography,
  Container,
  Drawer,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  IconButton,
  useMediaQuery,
  useTheme,
  Tooltip,
  Divider,
} from '@mui/material';
import { alpha } from '@mui/material/styles';
import {
  Activity,
  LayoutDashboard,
  FileText,
  Database,
  Settings,
  Menu,
  Server,
  LogOut,
  UserCheck,
} from 'lucide-react';
import { useNavigate, useLocation } from 'react-router-dom';
import { SettingsDrawer } from '../components/common/SettingsDrawer';
import { useAuth } from '../context/AuthContext';

const DRAWER_WIDTH = 260;

interface MainLayoutProps {
  children: React.ReactNode;
}

export const MainLayout: React.FC<MainLayoutProps> = ({ children }) => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [mobileOpen, setMobileOpen] = useState(false);
  const [settingsOpen, setSettingsOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();

  const menuItems = [
    { text: 'Dashboard', icon: <LayoutDashboard size={20} />, path: '/dashboard' },
    { text: 'Reconciliation', icon: <FileText size={20} />, path: '/reconciliation' },
    { text: 'Datasets', icon: <Database size={20} />, path: '/datasets' },
    { text: 'Runs', icon: <Activity size={20} />, path: '/runs' },
  ];

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const navigateTo = (path: string) => {
    navigate(path);
    if (isMobile) setMobileOpen(false);
  };

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  const drawer = (
    <Box
      sx={{
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        bgcolor: 'background.paper',
        borderRight: '1px solid',
        borderColor: 'divider',
      }}
    >
      {/* Brand Header */}
      <Box
        sx={{
          p: 3,
          display: 'flex',
          alignItems: 'center',
          gap: 1.5,
          cursor: 'pointer',
        }}
        onClick={() => navigateTo('/dashboard')}
      >
        <Box
          sx={{
            width: 32,
            height: 32,
            borderRadius: '8px',
            background:
              theme.palette.mode === 'dark'
                ? 'linear-gradient(135deg, #e4e4e7 0%, #a1a1aa 100%)'
                : 'linear-gradient(135deg, #18181b 0%, #3f3f46 100%)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <Activity
            size={18}
            color={theme.palette.mode === 'dark' ? '#09090b' : '#ffffff'}
            strokeWidth={2.5}
          />
        </Box>
        <Box>
          <Typography variant="h6" sx={{ fontWeight: 700, letterSpacing: '-0.5px', lineHeight: 1.1 }}>
            AI Finance
          </Typography>
          <Typography variant="caption" color="text.secondary" sx={{ fontSize: '0.7rem' }}>
            Controller Hub
          </Typography>
        </Box>
      </Box>

      {/* Navigation List */}
      <List sx={{ px: 2, flex: 1 }}>
        {menuItems.map((item) => {
          const isActive =
            location.pathname === item.path ||
            (item.path !== '/dashboard' && location.pathname.startsWith(item.path));
          return (
            <ListItem
              button
              key={item.text}
              onClick={() => navigateTo(item.path)}
              sx={{
                mb: 0.5,
                borderRadius: '6px',
                bgcolor: isActive ? 'action.selected' : 'transparent',
                color: isActive ? 'text.primary' : 'text.secondary',
                '&:hover': {
                  bgcolor: 'action.hover',
                  color: 'text.primary',
                },
              }}
            >
              <ListItemIcon sx={{ minWidth: 36, color: 'inherit' }}>{item.icon}</ListItemIcon>
              <ListItemText
                primary={item.text}
                primaryTypographyProps={{ fontSize: '0.9rem', fontWeight: isActive ? 600 : 500 }}
              />
            </ListItem>
          );
        })}
      </List>

      {/* Drawer Footer: System Status & User Profile */}
      <Box sx={{ p: 2.5, borderTop: '1px solid', borderColor: 'divider' }}>
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            gap: 1,
            color: 'text.secondary',
            mb: 2,
            px: 0.5,
          }}
        >
          <Server size={14} color="#10b981" />
          <Typography variant="caption" sx={{ fontWeight: 500 }}>
            System: Online
          </Typography>
        </Box>

        <Divider sx={{ mb: 2 }} />

        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            p: 1,
            borderRadius: '8px',
            bgcolor: (t) => alpha(t.palette.text.primary, 0.03),
            border: '1px solid',
            borderColor: 'divider',
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.2, minWidth: 0 }}>
            <Box
              sx={{
                width: 32,
                height: 32,
                borderRadius: '50%',
                bgcolor: (t) => alpha(t.palette.primary.main, 0.1),
                color: 'primary.main',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexShrink: 0,
              }}
            >
              <UserCheck size={16} />
            </Box>
            <Box sx={{ minWidth: 0 }}>
              <Typography
                variant="body2"
                sx={{
                  fontWeight: 600,
                  fontSize: '0.825rem',
                  whiteSpace: 'nowrap',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                }}
              >
                {user?.name || 'Administrator'}
              </Typography>
              <Typography
                variant="caption"
                color="text.secondary"
                sx={{
                  display: 'block',
                  fontSize: '0.7rem',
                  whiteSpace: 'nowrap',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                }}
              >
                {user?.role || 'Finance Operations'}
              </Typography>
            </Box>
          </Box>

          <Tooltip title="Sign Out">
            <IconButton
              size="small"
              onClick={handleLogout}
              sx={{
                color: 'text.secondary',
                '&:hover': { color: 'error.main', bgcolor: (t) => alpha(t.palette.error.main, 0.1) },
              }}
              aria-label="sign out"
            >
              <LogOut size={16} />
            </IconButton>
          </Tooltip>
        </Box>
      </Box>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', backgroundColor: 'background.default' }}>
      <Box component="nav" sx={{ width: { md: DRAWER_WIDTH }, flexShrink: { md: 0 } }}>
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{ keepMounted: true }}
          sx={{
            display: { xs: 'block', md: 'none' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: DRAWER_WIDTH, border: 'none' },
          }}
        >
          {drawer}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: 'none', md: 'block' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: DRAWER_WIDTH, border: 'none' },
          }}
          open
        >
          {drawer}
        </Drawer>
      </Box>

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          display: 'flex',
          flexDirection: 'column',
          width: { md: `calc(100% - ${DRAWER_WIDTH}px)` },
        }}
      >
        <AppBar
          position="sticky"
          elevation={0}
          sx={{
            bgcolor: (t) => alpha(t.palette.background.default, 0.8),
            backdropFilter: 'blur(12px)',
            borderBottom: '1px solid',
            borderColor: 'divider',
          }}
        >
          <Toolbar sx={{ minHeight: '64px !important', px: { xs: 2, sm: 4 } }}>
            {isMobile && (
              <IconButton
                color="inherit"
                aria-label="open drawer"
                edge="start"
                onClick={handleDrawerToggle}
                sx={{ mr: 2, color: 'text.secondary' }}
              >
                <Menu />
              </IconButton>
            )}
            <Box sx={{ flexGrow: 1 }} />
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <Tooltip title="Settings">
                <IconButton
                  size="small"
                  sx={{ color: 'text.secondary' }}
                  onClick={() => setSettingsOpen(true)}
                  aria-label="open settings"
                >
                  <Settings size={20} />
                </IconButton>
              </Tooltip>
              <Tooltip title="Sign Out">
                <IconButton
                  size="small"
                  sx={{
                    color: 'text.secondary',
                    '&:hover': { color: 'error.main', bgcolor: (t) => alpha(t.palette.error.main, 0.1) },
                  }}
                  onClick={handleLogout}
                  aria-label="sign out"
                >
                  <LogOut size={20} />
                </IconButton>
              </Tooltip>
            </Box>
          </Toolbar>
        </AppBar>

        <Container maxWidth="xl" sx={{ flexGrow: 1, py: { xs: 3, md: 5 }, px: { xs: 2, sm: 4 } }}>
          {children}
        </Container>
      </Box>
      <SettingsDrawer open={settingsOpen} onClose={() => setSettingsOpen(false)} />
    </Box>
  );
};
