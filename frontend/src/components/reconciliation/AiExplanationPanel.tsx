import React, { useState } from 'react';
import { Box, Typography, Card, CardContent, Button, CircularProgress, Alert, Divider, Stack, Grid } from '@mui/material';
import { Sparkles, ShieldCheck, AlertCircle } from 'lucide-react';
import { getAiExplanation } from '../../services/api';
import { AiExplanationResponse } from '../../types/api';
import { useSettings } from '../../context/SettingsContext';

interface AiExplanationPanelProps {
  paymentId: string;
  runId?: string;
  deterministicStatus: string;
  deterministicException: string;
}

export const AiExplanationPanel: React.FC<AiExplanationPanelProps> = ({ paymentId, runId, deterministicStatus, deterministicException }) => {
  const { aiEnabled } = useSettings();
  const [explanation, setExplanation] = useState<AiExplanationResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [requested, setRequested] = useState<boolean>(false);

  const fetchExplanation = async () => {
    setLoading(true);
    setError(null);
    setRequested(true);
    try {
      const data = await getAiExplanation(paymentId, runId);
      setExplanation(data);
    } catch (err: any) {
      setError(err.response?.data?.detail || err.message || 'AI explanation is currently unavailable. The deterministic result remains authoritative.');
    } finally {
      setLoading(false);
    }
  };

  if (!aiEnabled) {
    return (
      <Card sx={{ mt: 4, border: '1px solid', borderColor: 'divider', bgcolor: 'background.paper' }}>
        <CardContent sx={{ p: 4, display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center' }}>
          <ShieldCheck size={32} color="inherit" style={{ marginBottom: '16px', color: 'var(--mui-palette-text-secondary)' }} />
          <Typography variant="h6" sx={{ color: 'text.primary', mb: 1 }}>AI Explanations Disabled</Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary', maxWidth: 400 }}>
            AI-assisted analysis is currently disabled in your Settings.
            The deterministic reconciliation engine remains the authoritative source of truth.
          </Typography>
        </CardContent>
      </Card>
    );
  }

  if (!requested) {
    return (
      <Box sx={{ mt: 4, display: 'flex', flexDirection: 'column', alignItems: 'center', p: 4, border: '1px dashed', borderColor: 'divider', borderRadius: '12px', bgcolor: 'background.paper' }}>
        <Typography variant="h6" sx={{ mb: 1, fontWeight: 600 }}>Need deeper insight?</Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3, textAlign: 'center', maxWidth: 400 }}>
          Generate an AI-assisted analysis of this transaction's lifecycle to understand exactly why it was classified as {deterministicException !== 'NONE' ? deterministicException.replace(/_/g, ' ') : deterministicStatus}.
        </Typography>
        <Button
          variant="contained"
          color="info"
          onClick={fetchExplanation}
          startIcon={<Sparkles size={18} />}
        >
          Analyze with AI
        </Button>
      </Box>
    );
  }

  return (
    <Card sx={{ mt: 4, border: '1px solid rgba(14, 165, 233, 0.3)', bgcolor: 'rgba(14, 165, 233, 0.03)', boxShadow: '0 0 40px rgba(14, 165, 233, 0.05)' }}>
      <CardContent sx={{ p: { xs: 3, md: 4 } }}>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 3, flexWrap: 'wrap', gap: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Box sx={{ p: 1, bgcolor: 'rgba(14, 165, 233, 0.1)', borderRadius: '8px' }}>
              <Sparkles size={24} color="#38bdf8" />
            </Box>
            <Box>
              <Typography variant="h5" sx={{ color: 'text.primary', fontWeight: 600, letterSpacing: '-0.5px' }}>AI-Assisted Analysis</Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 500 }}>Powered by Groq Integration</Typography>
            </Box>
          </Box>

          {/* Absolute Trust Indicator */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, px: 2, py: 1, bgcolor: 'action.hover', borderRadius: '6px', border: '1px solid', borderColor: 'divider' }}>
            <ShieldCheck size={16} color="#10b981" />
            <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 500 }}>
              Deterministic Rule: <Box component="span" sx={{ color: 'text.primary' }}>{deterministicException !== 'NONE' ? deterministicException : deterministicStatus}</Box>
            </Typography>
          </Box>
        </Box>

        <Divider sx={{ mb: 3, borderColor: 'rgba(14, 165, 233, 0.15)' }} />

        {loading ? (
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', py: 4 }}>
            <CircularProgress size={32} color="info" sx={{ mb: 2 }} />
            <Typography sx={{ color: 'text.secondary' }}>Analyzing financial evidence...</Typography>
          </Box>
        ) : error ? (
          <Alert severity="warning" variant="outlined" sx={{ borderColor: 'rgba(245, 158, 11, 0.3)', color: '#fbbf24', '& .MuiAlert-icon': { color: '#fbbf24' } }}>
            {error}
          </Alert>
        ) : explanation ? (
          <Grid container spacing={4}>
            <Grid item xs={12} md={8}>
              <Stack spacing={3}>
                <Box>
                  <Typography variant="overline" sx={{ color: 'info.main', fontWeight: 700, letterSpacing: '1px' }}>Summary</Typography>
                  <Typography variant="body1" sx={{ color: 'text.primary', mt: 1, fontSize: '1.05rem' }}>
                    {explanation.explanation.summary}
                  </Typography>
                </Box>
                <Box>
                  <Typography variant="overline" sx={{ color: 'info.main', fontWeight: 700, letterSpacing: '1px' }}>Reasoning</Typography>
                  <Typography variant="body2" sx={{ color: 'text.secondary', mt: 1, lineHeight: 1.6 }}>
                    {explanation.explanation.reasoning}
                  </Typography>
                </Box>
              </Stack>
            </Grid>
            <Grid item xs={12} md={4}>
              <Box sx={{ p: 3, bgcolor: 'action.hover', borderRadius: '8px', border: '1px solid', borderColor: 'divider', height: '100%' }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                  <AlertCircle size={18} color="#fbbf24" />
                  <Typography variant="subtitle2" sx={{ color: 'warning.main', fontWeight: 700 }}>RECOMMENDED ACTION</Typography>
                </Box>
                <Typography variant="body2" sx={{ color: 'text.primary', lineHeight: 1.6 }}>
                  {explanation.explanation.recommendedAction}
                </Typography>
              </Box>
            </Grid>
          </Grid>
        ) : null}
      </CardContent>
    </Card>
  );
};
