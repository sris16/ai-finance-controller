import React, { useEffect, useState } from 'react';
import { Box, Typography, Card, CardContent, Grid, CircularProgress, Chip, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Button, Stack } from '@mui/material';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { ArrowLeft, CreditCard, Receipt, Building2, AlertCircle, CheckCircle2 } from 'lucide-react';
import { getReconciliationResult } from '../services/api';
import { ReconciliationResult } from '../types/api';
import { AiExplanationPanel } from '../components/reconciliation/AiExplanationPanel';

export const ResultDetailsPage: React.FC = () => {
  const { paymentId } = useParams<{ paymentId: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const runId = queryParams.get('runId');

  const [result, setResult] = useState<ReconciliationResult | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchDetails = async () => {
      setLoading(true);
      try {
        const data = await getReconciliationResult(paymentId!, runId || undefined);
        setResult(data);
      } catch (err: any) {
        setError(err.response?.data?.message || err.message || 'Failed to fetch details');
      } finally {
        setLoading(false);
      }
    };
    if (paymentId) fetchDetails();
  }, [paymentId, runId]);

  if (loading) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', p: 10 }}><CircularProgress /></Box>;
  }

  if (error || !result) {
    return (
      <Box sx={{ p: 4, textAlign: 'center' }}>
        <Typography color="error" sx={{ mb: 2 }}>{error || 'Record not found'}</Typography>
        <Button variant="outlined" onClick={() => navigate('/reconciliation')}>Back to Results</Button>
      </Box>
    );
  }

  const formatCurrency = (amount: number | null | undefined) => {
    if (amount == null) return '-';
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(amount);
  };

  const isMatch = result.overallStatus === 'MATCH';

  return (
    <Box sx={{ maxWidth: 1200, mx: 'auto' }}>
      <Box sx={{ mb: 4 }}>
        <Button
          startIcon={<ArrowLeft size={16} />}
          onClick={() => navigate('/reconciliation')}
          sx={{ mb: 2, color: 'text.secondary', '&:hover': { color: 'text.primary', bgcolor: 'transparent' } }}
        >
          Back to Records
        </Button>
        <Typography variant="h3" sx={{ fontWeight: 700, mb: 1, display: 'flex', alignItems: 'center', gap: 2 }}>
          {result.paymentId}
          <Chip
            label={result.overallStatus}
            sx={{
              bgcolor: isMatch ? 'rgba(16, 185, 129, 0.1)' : 'rgba(244, 63, 94, 0.1)',
              color: isMatch ? '#10b981' : '#f43f5e',
              border: `1px solid ${isMatch ? 'rgba(16, 185, 129, 0.2)' : 'rgba(244, 63, 94, 0.2)'}`,
              fontWeight: 700,
              borderRadius: '6px'
            }}
          />
          {!isMatch && result.exceptionType !== 'NONE' && (
            <Chip
              label={result.exceptionType.replace(/_/g, ' ')}
              sx={{
                bgcolor: 'rgba(245, 158, 11, 0.1)',
                color: '#fbbf24',
                border: '1px solid rgba(245, 158, 11, 0.2)',
                fontWeight: 600,
                borderRadius: '6px'
              }}
            />
          )}
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Order: {result.orderId} • Date: {new Date(result.paymentDate).toLocaleString()}
        </Typography>
      </Box>

      {/* Deterministic Timeline */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h6" sx={{ mb: 2, color: 'text.primary' }}>Financial Lifecycle</Typography>
        <Grid container spacing={2}>
          {/* Payment */}
          <Grid item xs={12} md={4}>
            <Card sx={{ height: '100%', position: 'relative' }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 3 }}>
                  <Box sx={{ p: 1, bgcolor: 'rgba(255,255,255,0.05)', borderRadius: 2 }}><CreditCard size={20} color="#a1a1aa" /></Box>
                  <Typography variant="subtitle1">Payment Gateway</Typography>
                </Box>
                <Stack spacing={1.5}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}><Typography color="text.secondary" variant="body2">Status</Typography><Typography variant="body2" sx={{ fontWeight: 500 }}>{result.paymentStatus}</Typography></Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}><Typography color="text.secondary" variant="body2">Amount</Typography><Typography variant="body2" sx={{ fontWeight: 600 }}>{formatCurrency(result.paymentAmount)}</Typography></Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}><Typography color="text.secondary" variant="body2">Order Amount</Typography><Typography variant="body2">{formatCurrency(result.orderAmount)}</Typography></Box>
                </Stack>
              </CardContent>
            </Card>
          </Grid>

          {/* Settlement */}
          <Grid item xs={12} md={4}>
            <Card sx={{ height: '100%' }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 3 }}>
                  <Box sx={{ p: 1, bgcolor: 'rgba(255,255,255,0.05)', borderRadius: 2 }}><Receipt size={20} color="#a1a1aa" /></Box>
                  <Typography variant="subtitle1">Settlement</Typography>
                </Box>
                <Stack spacing={1.5}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}><Typography color="text.secondary" variant="body2">Status</Typography><Typography variant="body2" sx={{ fontWeight: 500 }}>{result.settlementPresent ? result.settlementStatus : 'MISSING'}</Typography></Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}><Typography color="text.secondary" variant="body2">Gross Amount</Typography><Typography variant="body2" sx={{ fontWeight: 600 }}>{formatCurrency(result.settlementGrossAmount)}</Typography></Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}><Typography color="text.secondary" variant="body2">Net Amount</Typography><Typography variant="body2">{formatCurrency(result.settlementNetAmount)}</Typography></Box>
                </Stack>
              </CardContent>
            </Card>
          </Grid>

          {/* Bank */}
          <Grid item xs={12} md={4}>
            <Card sx={{ height: '100%' }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 3 }}>
                  <Box sx={{ p: 1, bgcolor: 'rgba(255,255,255,0.05)', borderRadius: 2 }}><Building2 size={20} color="#a1a1aa" /></Box>
                  <Typography variant="subtitle1">Bank Statement</Typography>
                </Box>
                <Stack spacing={1.5}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}><Typography color="text.secondary" variant="body2">Transactions Found</Typography><Typography variant="body2" sx={{ fontWeight: 500, color: result.bankTransactionCount > 1 ? '#f59e0b' : 'inherit' }}>{result.bankTransactionCount}</Typography></Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}><Typography color="text.secondary" variant="body2">Total Credited</Typography><Typography variant="body2" sx={{ fontWeight: 600 }}>{formatCurrency(result.bankTransactionAmount)}</Typography></Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}><Typography color="text.secondary" variant="body2">Reconciliation</Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                      {isMatch ? <CheckCircle2 size={14} color="#10b981" /> : <AlertCircle size={14} color="#f43f5e" />}
                      <Typography variant="body2" sx={{ color: isMatch ? '#10b981' : '#f43f5e', fontWeight: 600 }}>{isMatch ? 'MATCHED' : 'EXCEPTION'}</Typography>
                    </Box>
                  </Box>
                </Stack>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </Box>

      {/* Bank Transactions Table (if any) */}
      {result.bankTransactions && result.bankTransactions.length > 0 && (
        <Card sx={{ mb: 4 }}>
          <CardContent sx={{ p: 0 }}>
            <Box sx={{ px: 3, py: 2, borderBottom: '1px solid rgba(255,255,255,0.08)' }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Bank Transaction Entries</Typography>
            </Box>
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow>
                    <TableCell>Date</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell align="right">Amount</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {result.bankTransactions.map((tx, idx) => (
                    <TableRow key={idx}>
                      <TableCell sx={{ color: 'text.secondary' }}>{new Date(tx.date).toLocaleString()}</TableCell>
                      <TableCell>
                        <Typography variant="body2" sx={{ color: tx.status === 'SUCCESS' ? '#10b981' : 'text.secondary', fontWeight: 500 }}>
                          {tx.status}
                        </Typography>
                      </TableCell>
                      <TableCell align="right" sx={{ fontWeight: 500 }}>{formatCurrency(tx.amount)}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </CardContent>
        </Card>
      )}

      {/* AI Explanation Panel */}
      <AiExplanationPanel paymentId={result.paymentId} runId={runId || undefined} deterministicStatus={result.overallStatus} deterministicException={result.exceptionType} />

    </Box>
  );
};
