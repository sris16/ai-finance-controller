import React, { useEffect, useState } from 'react';
import { Box, Typography, Card, CardContent, Grid, CircularProgress, Alert, Chip, Divider, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, Button } from '@mui/material';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import { getReconciliationResult } from '../services/api';
import { ReconciliationResult } from '../types/api';
import { AiExplanationPanel } from '../components/reconciliation/AiExplanationPanel';

export const ResultDetailsPage: React.FC = () => {
  const { paymentId } = useParams<{ paymentId: string }>();
  const navigate = useNavigate();
  const [result, setResult] = useState<ReconciliationResult | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDetails = async () => {
      setLoading(true);
      try {
        const data = await getReconciliationResult(paymentId!);
        setResult(data);
      } catch (err: any) {
        setError(err.response?.data?.message || err.message || 'Failed to fetch details');
      } finally {
        setLoading(false);
      }
    };
    if (paymentId) fetchDetails();
  }, [paymentId]);

  if (loading) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', p: 10 }}><CircularProgress /></Box>;
  }

  if (error || !result) {
    return <Box><Alert severity="error">{error || 'Not found'}</Alert><Button onClick={() => navigate('/reconciliation')} sx={{ mt: 2 }}>Back</Button></Box>;
  }

  return (
    <Box>
      <Box sx={{ mb: 3 }}>
        <Button startIcon={<ArrowLeft size={16} />} onClick={() => navigate('/reconciliation')} sx={{ mb: 2, color: 'text.secondary' }}>
          Back to Results
        </Button>
        <Typography variant="h4" sx={{ fontWeight: 800, mb: 1, color: 'text.primary' }}>
          Reconciliation Detail: {result.paymentId}
        </Typography>
      </Box>

      {/* DETERMINISTIC RESULT HEADER */}
      <Card sx={{ mb: 4, bgcolor: '#1e293b', border: '1px solid', borderColor: result.overallStatus === 'MATCH' ? 'success.dark' : 'error.dark' }}>
        <CardContent>
          <Typography variant="overline" color="text.secondary" sx={{ fontWeight: 700, letterSpacing: 1.5 }}>
            DETERMINISTIC RESULT
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, mt: 1, alignItems: 'center' }}>
            <Typography variant="h6" color="text.primary">Status:</Typography>
            <Chip label={result.overallStatus} color={result.overallStatus === 'MATCH' ? 'success' : 'error'} sx={{ fontWeight: 700 }} />
            
            {result.exceptionType !== 'NONE' && (
              <>
                <Typography variant="h6" color="text.primary" sx={{ ml: 2 }}>Exception:</Typography>
                <Chip label={result.exceptionType} color="warning" sx={{ fontWeight: 700 }} />
              </>
            )}
          </Box>
        </CardContent>
      </Card>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Card sx={{ height: '100%', bgcolor: 'background.paper' }}>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2 }}>Payment Gateway Evidence</Typography>
              <Divider sx={{ mb: 2, borderColor: 'rgba(255,255,255,0.05)' }} />
              <Grid container spacing={2}>
                <Grid item xs={6}><Typography color="text.secondary">Order ID</Typography><Typography>{result.orderId}</Typography></Grid>
                <Grid item xs={6}><Typography color="text.secondary">Order Amount</Typography><Typography>{result.orderAmount.toFixed(2)}</Typography></Grid>
                <Grid item xs={6}><Typography color="text.secondary">Payment Amount</Typography><Typography>{result.paymentAmount.toFixed(2)}</Typography></Grid>
                <Grid item xs={6}><Typography color="text.secondary">Payment Status</Typography><Typography>{result.paymentStatus}</Typography></Grid>
                <Grid item xs={12}><Typography color="text.secondary">Payment Date</Typography><Typography>{result.paymentDate}</Typography></Grid>
              </Grid>

              <Typography variant="h6" sx={{ mb: 2, mt: 4 }}>Settlement Evidence</Typography>
              <Divider sx={{ mb: 2, borderColor: 'rgba(255,255,255,0.05)' }} />
              <Grid container spacing={2}>
                <Grid item xs={6}>
                  <Typography color="text.secondary">Settlement Present</Typography>
                  <Typography color={result.settlementPresent ? 'success.main' : 'error.main'} sx={{ fontWeight: 'bold' }}>
                    {result.settlementPresent ? 'Yes' : 'No'}
                  </Typography>
                </Grid>
                <Grid item xs={6}><Typography color="text.secondary">Settlement Status</Typography><Typography>{result.settlementStatus || '-'}</Typography></Grid>
                <Grid item xs={6}><Typography color="text.secondary">Gross Amount</Typography><Typography>{result.settlementGrossAmount?.toFixed(2) || '-'}</Typography></Grid>
                <Grid item xs={6}><Typography color="text.secondary">Net Amount</Typography><Typography>{result.settlementNetAmount?.toFixed(2) || '-'}</Typography></Grid>
                <Grid item xs={12}><Typography color="text.secondary">Settlement Date</Typography><Typography>{result.settlementDate || '-'}</Typography></Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card sx={{ height: '100%', bgcolor: 'background.paper' }}>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2 }}>Bank Bank Statement Evidence</Typography>
              <Divider sx={{ mb: 2, borderColor: 'rgba(255,255,255,0.05)' }} />
              <Box sx={{ display: 'flex', gap: 4, mb: 3 }}>
                <Box>
                  <Typography color="text.secondary">Transaction Count</Typography>
                  <Typography variant="h6">{result.bankTransactionCount}</Typography>
                </Box>
                <Box>
                  <Typography color="text.secondary">Total Credited Amount</Typography>
                  <Typography variant="h6">{result.bankTransactionAmount?.toFixed(2) || '-'}</Typography>
                </Box>
              </Box>

              {result.bankTransactions && result.bankTransactions.length > 0 ? (
                <TableContainer component={Paper} elevation={0} sx={{ bgcolor: 'rgba(255,255,255,0.02)' }}>
                  <Table size="small">
                    <TableHead>
                      <TableRow>
                        <TableCell>Amount</TableCell>
                        <TableCell>Status</TableCell>
                        <TableCell>Date</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {result.bankTransactions.map((tx, idx) => (
                        <TableRow key={idx}>
                          <TableCell>{tx.amount?.toFixed(2)}</TableCell>
                          <TableCell><Chip label={tx.status} size="small" color={tx.status === 'SUCCESS' ? 'success' : 'default'} /></TableCell>
                          <TableCell>{tx.date}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              ) : (
                <Typography color="text.secondary">No bank transactions found.</Typography>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* AI EXPLANATION COMPONENT */}
      <AiExplanationPanel paymentId={result.paymentId} />

    </Box>
  );
};
