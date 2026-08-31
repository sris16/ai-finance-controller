import React, { useEffect, useState } from 'react';
import { Drawer, Box, Typography, IconButton, Divider, Switch, Select, MenuItem, FormControl, InputLabel, CircularProgress } from '@mui/material';
import { X, Palette, IndianRupee, Sparkles, LayoutDashboard, Server } from 'lucide-react';
import { useSettings } from '../../context/SettingsContext';
import { checkBackendHealth, checkAiServiceHealth } from '../../services/api';

interface SettingsDrawerProps {
  open: boolean;
  onClose: () => void;
}

export const SettingsDrawer: React.FC<SettingsDrawerProps> = ({ open, onClose }) => {
  const { theme, setTheme, pageSize, setPageSize, autoRefresh, setAutoRefresh, aiEnabled, setAiEnabled } = useSettings();
  
  const [backendStatus, setBackendStatus] = useState<'checking' | 'online' | 'offline'>('checking');
  const [aiStatus, setAiStatus] = useState<'checking' | 'online' | 'offline'>('checking');

  useEffect(() => {
    if (open) {
      setBackendStatus('checking');
      setAiStatus('checking');
      
      checkBackendHealth()
        .then(() => setBackendStatus('online'))
        .catch(() => setBackendStatus('offline'));
        
      checkAiServiceHealth()
        .then(() => setAiStatus('online'))
        .catch(() => setAiStatus('offline'));
    }
  }, [open]);

  return (
    <Drawer anchor="right" open={open} onClose={onClose} PaperProps={{ sx: { width: { xs: '100%', sm: 400 }, bgcolor: 'background.default', backgroundImage: 'none' } }}>
      <Box sx={{ p: 3, display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '1px solid', borderColor: 'divider' }}>
        <Typography variant="h6" sx={{ fontWeight: 600 }}>Settings</Typography>
        <IconButton onClick={onClose} size="small"><X size={20} /></IconButton>
      </Box>

      <Box sx={{ p: 3, display: 'flex', flexDirection: 'column', gap: 4 }}>
        
        {/* Appearance */}
        <Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2, color: 'text.secondary' }}>
            <Palette size={16} />
            <Typography variant="subtitle2">Appearance</Typography>
          </Box>
          <FormControl fullWidth size="small">
            <InputLabel>Theme</InputLabel>
            <Select value={theme} label="Theme" onChange={(e) => setTheme(e.target.value as any)}>
              <MenuItem value="system">System Default</MenuItem>
              <MenuItem value="dark">Dark Mode</MenuItem>
              <MenuItem value="light">Light Mode</MenuItem>
            </Select>
          </FormControl>
        </Box>

        {/* Finance */}
        <Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2, color: 'text.secondary' }}>
            <IndianRupee size={16} />
            <Typography variant="subtitle2">Finance</Typography>
          </Box>
          <FormControl fullWidth size="small">
            <InputLabel>Currency</InputLabel>
            <Select value="INR" label="Currency" disabled>
              <MenuItem value="INR">Indian Rupee (₹)</MenuItem>
            </Select>
          </FormControl>
        </Box>

        {/* AI Explanation */}
        <Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2, color: 'text.secondary' }}>
            <Sparkles size={16} />
            <Typography variant="subtitle2">AI Explanation</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
            <Typography variant="body2">AI Explanations</Typography>
            <Switch checked={aiEnabled} onChange={(e) => setAiEnabled(e.target.checked)} color="primary" />
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
            <Typography variant="body2" color="text.secondary">Show deterministic result as authoritative</Typography>
            <Switch checked={true} disabled />
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <Typography variant="body2" color="text.secondary">AI Timeout Limit</Typography>
            <Typography variant="body2" sx={{ fontFamily: 'monospace', color: 'text.secondary' }}>20s</Typography>
          </Box>
        </Box>

        {/* Dashboard */}
        <Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2, color: 'text.secondary' }}>
            <LayoutDashboard size={16} />
            <Typography variant="subtitle2">Dashboard</Typography>
          </Box>
          <FormControl fullWidth size="small" sx={{ mb: 2 }}>
            <InputLabel>Results per page</InputLabel>
            <Select value={pageSize} label="Results per page" onChange={(e) => setPageSize(Number(e.target.value))}>
              <MenuItem value={10}>10</MenuItem>
              <MenuItem value={20}>20</MenuItem>
              <MenuItem value={50}>50</MenuItem>
            </Select>
          </FormControl>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <Typography variant="body2">Auto-refresh active runs</Typography>
            <Switch checked={autoRefresh} onChange={(e) => setAutoRefresh(e.target.checked)} color="primary" />
          </Box>
        </Box>

        <Divider />

        {/* System */}
        <Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2, color: 'text.secondary' }}>
            <Server size={16} />
            <Typography variant="subtitle2">System Status</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1.5 }}>
            <Typography variant="body2">Backend Service</Typography>
            {backendStatus === 'checking' ? <CircularProgress size={14} /> : 
             <Typography variant="caption" sx={{ color: backendStatus === 'online' ? 'success.main' : 'error.main', fontWeight: 600, display: 'flex', alignItems: 'center', gap: 0.5 }}>
               <Box sx={{ width: 6, height: 6, borderRadius: '50%', bgcolor: backendStatus === 'online' ? 'success.main' : 'error.main' }} />
               {backendStatus.toUpperCase()}
             </Typography>
            }
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1.5 }}>
            <Typography variant="body2">AI Service</Typography>
            {aiStatus === 'checking' ? <CircularProgress size={14} /> : 
             <Typography variant="caption" sx={{ color: aiStatus === 'online' ? 'success.main' : 'error.main', fontWeight: 600, display: 'flex', alignItems: 'center', gap: 0.5 }}>
               <Box sx={{ width: 6, height: 6, borderRadius: '50%', bgcolor: aiStatus === 'online' ? 'success.main' : 'error.main' }} />
               {aiStatus.toUpperCase()}
             </Typography>
            }
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <Typography variant="body2" color="text.secondary">Version</Typography>
            <Typography variant="caption" color="text.secondary" sx={{ fontFamily: 'monospace' }}>1.0.0</Typography>
          </Box>
        </Box>

      </Box>
    </Drawer>
  );
};
