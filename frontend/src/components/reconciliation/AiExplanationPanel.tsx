import React, { useState } from 'react';
import { Box, Typography, Card, CardContent, Button, CircularProgress, Alert, Divider, Stack, Grid } from '@mui/material';
import { Sparkles, ShieldCheck, AlertCircle } from 'lucide-react';
import { getAiExplanation } from '../../services/api';
import { AiExplanationResponse } from '../../types/api';

interface AiExplanationPanelProps {
  paymentId: string;
  runId?: string;
  deterministicStatus: string;
  deterministicException: string;
}

export const AiExplanationPanel: React.FC<AiExplanationPanelProps> = ({ paymentId, runId, deterministicStatus, deterministicException }) => {
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

  if (!requested) {
    return (
      <Box sx={{ mt: 4, display: 'flex', flexDirection: 'column', alignItems: 'center', p: 4, border: '1px dashed rgba(255,255,255,0.15)', borderRadius: '12px', bgcolor: 'rgba(255,255,255,0.02)' }}>
        <Typography variant="h6" sx={{ mb: 1, fontWeight: 600 }}>Need deeper insight?</Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3, textAlign: 'center', maxWidth: 400 }}>
          Generate an AI-assisted analysis of this transaction's lifecycle to understand exactly why it was classified as {deterministicException !== 'NONE' ? deterministicException.replace(/_/g, ' ') : deterministicStatus}.
        </Typography>
        <Button
          variant="contained"
          onClick={fetchExplanation}
          startIcon={<Sparkles size={18} />}
          sx={{
            bgcolor: '#0ea5e9',
            color: '#fff',
            fontWeight: 600,
            '&:hover': { bgcolor: '#0284c7' }
          }}
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
              <Typography variant="h5" sx={{ color: '#e0f2fe', fontWeight: 600, letterSpacing: '-0.5px' }}>AI-Assisted Analysis</Typography>
              <Typography variant="caption" sx={{ color: '#7dd3fc', fontWeight: 500 }}>Powered by Groq Integration</Typography>
            </Box>
          </Box>

          {/* Absolute Trust Indicator */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, px: 2, py: 1, bgcolor: 'rgba(0,0,0,0.4)', borderRadius: '6px', border: '1px solid rgba(255,255,255,0.05)' }}>
            <ShieldCheck size={16} color="#10b981" />
            <Typography variant="caption" sx={{ color: '#a1a1aa', fontWeight: 500 }}>
              Deterministic Rule: <span style={{ color: '#fff' }}>{deterministicException !== 'NONE' ? deterministicException : deterministicStatus}</span>
            </Typography>
          </Box>
        </Box>

        <Divider sx={{ mb: 3, borderColor: 'rgba(14, 165, 233, 0.15)' }} />

        {loading ? (
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', py: 4 }}>
            <CircularProgress size={32} sx={{ color: '#0ea5e9', mb: 2 }} />
            <Typography sx={{ color: '#bae6fd' }}>Analyzing financial evidence...</Typography>
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
                  <Typography variant="overline" sx={{ color: '#7dd3fc', fontWeight: 700, letterSpacing: '1px' }}>Summary</Typography>
                  <Typography variant="body1" sx={{ color: '#f0f9ff', mt: 1, fontSize: '1.05rem' }}>
                    {explanation.explanation.summary}
                  </Typography>
                </Box>
                <Box>
                  <Typography variant="overline" sx={{ color: '#7dd3fc', fontWeight: 700, letterSpacing: '1px' }}>Reasoning</Typography>
                  <Typography variant="body2" sx={{ color: '#bae6fd', mt: 1, lineHeight: 1.6 }}>
                    {explanation.explanation.reasoning}
                  </Typography>
                </Box>
              </Stack>
            </Grid>
            <Grid item xs={12} md={4}>
              <Box sx={{ p: 3, bgcolor: 'rgba(0,0,0,0.3)', borderRadius: '8px', border: '1px solid rgba(255,255,255,0.05)', height: '100%' }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
                  <AlertCircle size={18} color="#fbbf24" />
                  <Typography variant="subtitle2" sx={{ color: '#fde68a', fontWeight: 700 }}>RECOMMENDED ACTION</Typography>
                </Box>
                <Typography variant="body2" sx={{ color: '#fef3c7', lineHeight: 1.6 }}>
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
