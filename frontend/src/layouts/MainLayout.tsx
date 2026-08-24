import React from 'react';
import { Box, AppBar, Toolbar, Typography, Container, Button } from '@mui/material';
import { Activity, Terminal } from 'lucide-react';

interface MainLayoutProps {
  children: React.ReactNode;
}

export const MainLayout: React.FC<MainLayoutProps> = ({ children }) => {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', backgroundColor: 'background.default' }}>
      <AppBar position="static" elevation={0} sx={{ borderBottom: '1px solid rgba(255, 255, 255, 0.08)', backgroundColor: '#111827' }}>
        <Toolbar sx={{ justifyContent: 'space-between', px: { xs: 2, sm: 4 } }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Box
              sx={{
                width: 36,
                height: 36,
                borderRadius: 2,
                backgroundColor: 'primary.main',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                boxShadow: '0 0 12px rgba(2, 132, 199, 0.5)',
              }}
            >
              <Activity size={20} color="#ffffff" />
            </Box>
            <Box>
              <Typography variant="h6" sx={{ color: 'text.primary', lineHeight: 1.2, fontWeight: 700 }}>
                AI Finance Controller
              </Typography>
              <Typography variant="caption" sx={{ color: 'primary.light', fontWeight: 600 }}>
                Razorpay Buildathon 2026 — Track 04
              </Typography>
            </Box>
          </Box>

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Button
              variant="outlined"
              size="small"
              startIcon={<Terminal size={16} />}
              sx={{ borderColor: 'rgba(255, 255, 255, 0.15)', color: 'text.secondary' }}
              href="file:///home/srisakthi/Projects/ai-finance-controller/docs/architecture.md"
              target="_blank"
            >
              Architecture Docs
            </Button>
          </Box>
        </Toolbar>
      </AppBar>

      <Container maxWidth="xl" sx={{ flexGrow: 1, py: 4 }}>
        {children}
      </Container>

      <Box
        component="footer"
        sx={{
          py: 2.5,
          px: 3,
          mt: 'auto',
          borderTop: '1px solid rgba(255, 255, 255, 0.08)',
          backgroundColor: '#0b0f19',
          textAlign: 'center',
        }}
      >
        <Typography variant="body2" color="text.secondary">
          AI Finance Controller — Track 04: Multi-Source Reconciliation Agent | Phase 1 Foundation Active
        </Typography>
      </Box>
    </Box>
  );
};
