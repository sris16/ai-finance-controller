import React, { useState } from 'react';
import { Box, Typography, Card, CardContent, Button, CircularProgress, Alert, Divider } from '@mui/material';
import { Bot, RefreshCw } from 'lucide-react';
import { getAiExplanation } from '../../services/api';
import { AiExplanationResponse } from '../../types/api';

interface AiExplanationPanelProps {
  paymentId: string;
  runId?: string;
}

export const AiExplanationPanel: React.FC<AiExplanationPanelProps> = ({ paymentId, runId }) => {
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
      setError(err.response?.data?.detail || err.message || 'AI explanation is currently unavailable. The reconciliation result remains available.');
    } finally {
      setLoading(false);
    }
  };

  if (!requested) {
    return (
      <Box sx={{ mt: 3, display: 'flex', justifyContent: 'center' }}>
        <Button
          variant="outlined"
          color="secondary"
          startIcon={<Bot size={20} />}
          onClick={fetchExplanation}
          sx={{ py: 1.5, px: 4, borderRadius: 2 }}
        >
          Explain with AI
        </Button>
      </Box>
    );
  }

  return (
    <Card sx={{ mt: 4, bgcolor: '#0f172a', borderColor: 'rgba(56, 189, 248, 0.3)', borderWidth: 1, borderStyle: 'solid' }}>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
          <Bot size={24} color="#38bdf8" />
          <Typography variant="h6" sx={{ color: '#38bdf8', fontWeight: 700 }}>AI EXPLANATION</Typography>
        </Box>
        <Divider sx={{ mb: 2, borderColor: 'rgba(56, 189, 248, 0.1)' }} />

        {loading ? (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, p: 2 }}>
            <CircularProgress size={20} color="secondary" />
            <Typography variant="body2" color="text.secondary">Generating explanation...</Typography>
          </Box>
        ) : error ? (
          <Box>
            <Alert severity="warning" sx={{ mb: 2 }}>
              {error}
            </Alert>
            <Button variant="text" size="small" startIcon={<RefreshCw size={16} />} onClick={fetchExplanation}>
              Retry
            </Button>
          </Box>
        ) : explanation ? (
          <Box>
            <Typography variant="subtitle2" sx={{ color: 'text.secondary', mb: 0.5, fontWeight: 700 }}>Summary:</Typography>
            <Typography variant="body1" sx={{ mb: 2 }}>{explanation.explanation.summary}</Typography>

            <Typography variant="subtitle2" sx={{ color: 'text.secondary', mb: 0.5, fontWeight: 700 }}>Reasoning:</Typography>
            <Typography variant="body2" sx={{ mb: 2, color: 'text.secondary' }}>{explanation.explanation.reasoning}</Typography>

            <Typography variant="subtitle2" sx={{ color: 'text.secondary', mb: 0.5, fontWeight: 700 }}>Recommended Action:</Typography>
            <Typography variant="body2" sx={{ color: '#fbbf24', fontWeight: 600 }}>{explanation.explanation.recommendedAction}</Typography>
          </Box>
        ) : null}
      </CardContent>
    </Card>
  );
};
