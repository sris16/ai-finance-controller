import React, { useEffect, useState } from 'react';
import { Box, Typography, Card, CardContent, Grid, CircularProgress, Chip, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Button, Stack, Divider } from '@mui/material';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { ArrowLeft, CreditCard, Receipt, Building2, AlertCircle, CheckCircle2, AlertTriangle, CalendarDays } from 'lucide-react';
import { getReconciliationResult } from '../services/api';
import { ReconciliationResult } from '../types/api';
import { AiExplanationPanel } from '../components/reconciliation/AiExplanationPanel';
import { formatINR } from '../utils/currency';

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

  const isMatch = result.overallStatus === 'MATCH';

  const renderExceptionImpact = () => {
    if (isMatch || result.exceptionType === 'NONE') return null;

    let content = null;

    switch (result.exceptionType) {
      case 'DUPLICATE_TRANSACTION':
        const expected = result.settlementGrossAmount || result.paymentAmount;
        const excess = result.bankTransactionAmount - expected;
        const individualTxAmount = result.bankTransactionCount > 0 ? (result.bankTransactionAmount / result.bankTransactionCount) : 0;

        content = (
          <Grid container spacing={3}>
            <Grid item xs={12} sm={4}>
              <Typography color="text.secondary" variant="body2" sx={{ mb: 0.5, fontWeight: 600 }}>Expected Settlement</Typography>
              <Typography variant="h5" sx={{ fontFamily: 'monospace' }}>{formatINR(expected)}</Typography>
            </Grid>
            <Grid item xs={12} sm={4}>
              <Typography color="text.secondary" variant="body2" sx={{ mb: 0.5, fontWeight: 600 }}>Total Credited ({result.bankTransactionCount} txs)</Typography>
              <Typography variant="h5" color="error.main" sx={{ fontFamily: 'monospace' }}>{formatINR(result.bankTransactionAmount)}</Typography>
              {result.bankTransactionCount > 1 && (
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 0.5 }}>
                  ({formatINR(individualTxAmount)} × {result.bankTransactionCount})
                </Typography>
              )}
            </Grid>
            <Grid item xs={12} sm={4}>
              <Typography color="text.secondary" variant="body2" sx={{ mb: 0.5, fontWeight: 600 }}>Detected Excess</Typography>
              <Typography variant="h5" color="warning.main" sx={{ fontFamily: 'monospace' }}>+{formatINR(excess)}</Typography>
            </Grid>
          </Grid>
        );
        break;

      case 'AMOUNT_MISMATCH':
        const diff = (result.settlementGrossAmount || 0) - result.paymentAmount;
        content = (
          <Grid container spacing={3}>
            <Grid item xs={12} sm={4}>
              <Typography color="text.secondary" variant="body2" sx={{ mb: 0.5, fontWeight: 600 }}>Payment Amount</Typography>
              <Typography variant="h5" sx={{ fontFamily: 'monospace' }}>{formatINR(result.paymentAmount)}</Typography>
            </Grid>
            <Grid item xs={12} sm={4}>
              <Typography color="text.secondary" variant="body2" sx={{ mb: 0.5, fontWeight: 600 }}>Settlement Amount</Typography>
              <Typography variant="h5" sx={{ fontFamily: 'monospace', color: diff < 0 ? 'error.main' : 'success.main' }}>
                {formatINR(result.settlementGrossAmount)}
              </Typography>
            </Grid>
            <Grid item xs={12} sm={4}>
              <Typography color="text.secondary" variant="body2" sx={{ mb: 0.5, fontWeight: 600 }}>Difference</Typography>
              <Typography variant="h5" color={diff < 0 ? 'error.main' : 'warning.main'} sx={{ fontFamily: 'monospace' }}>
                {diff > 0 ? '+' : ''}{formatINR(diff)}
              </Typography>
            </Grid>
          </Grid>
        );
        break;

      case 'MISSING_SETTLEMENT':
        content = (
          <Grid container spacing={3}>
            <Grid item xs={12} sm={6}>
              <Typography color="text.secondary" variant="body2" sx={{ mb: 0.5, fontWeight: 600 }}>Payment Amount</Typography>
              <Typography variant="h5" sx={{ fontFamily: 'monospace' }}>{formatINR(result.paymentAmount)}</Typography>
            </Grid>
            <Grid item xs={12} sm={6}>
              <Typography color="text.secondary" variant="body2" sx={{ mb: 0.5, fontWeight: 600 }}>Settlement Status</Typography>
              <Typography variant="h5" color="error.main" sx={{ fontWeight: 600 }}>NOT FOUND</Typography>
            </Grid>
          </Grid>
        );
        break;

      case 'DATE_ANOMALY':
        content = (
          <Grid container spacing={3}>
            <Grid item xs={12} sm={6}>
              <Typography color="text.secondary" variant="body2" sx={{ mb: 0.5, fontWeight: 600 }}>Payment Date</Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <CalendarDays size={20} color="#a1a1aa" />
                <Typography variant="h6">{new Date(result.paymentDate).toLocaleDateString()}</Typography>
              </Box>
            </Grid>
            <Grid item xs={12} sm={6}>
              <Typography color="text.secondary" variant="body2" sx={{ mb: 0.5, fontWeight: 600 }}>Settlement Date</Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <CalendarDays size={20} color="#f43f5e" />
                <Typography variant="h6" color="error.main">{result.settlementDate ? new Date(result.settlementDate).toLocaleDateString() : 'Missing'}</Typography>
              </Box>
            </Grid>
          </Grid>
        );
        break;

      case 'STATUS_MISMATCH':
        content = (
          <Grid container spacing={3}>
            <Grid item xs={12} sm={4}>
              <Typography color="text.secondary" variant="body2" sx={{ mb: 0.5, fontWeight: 600 }}>Payment Status</Typography>
              <Typography variant="h6">{result.paymentStatus}</Typography>
            </Grid>
            <Grid item xs={12} sm={4}>
              <Typography color="text.secondary" variant="body2" sx={{ mb: 0.5, fontWeight: 600 }}>Settlement Status</Typography>
              <Typography variant="h6" color="warning.main">{result.settlementStatus || 'MISSING'}</Typography>
            </Grid>
            <Grid item xs={12} sm={4}>
              <Typography color="text.secondary" variant="body2" sx={{ mb: 0.5, fontWeight: 600 }}>Bank Status</Typography>
              <Typography variant="h6" color={result.bankTransactionStatus !== result.paymentStatus ? 'error.main' : 'inherit'}>
                {result.bankTransactionStatus || '-'}
              </Typography>
            </Grid>
          </Grid>
        );
        break;
    }

    return (
      <Card sx={{ mb: 4, bgcolor: 'rgba(244, 63, 94, 0.03)', border: '1px solid rgba(244, 63, 94, 0.2)' }}>
        <CardContent sx={{ p: 4 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 3 }}>
            <AlertTriangle size={24} color="#f43f5e" />
            <Typography variant="h6" color="error.main" sx={{ fontWeight: 700 }}>Exception Impact: {result.exceptionType.replace(/_/g, ' ')}</Typography>
          </Box>
          {content}
        </CardContent>
      </Card>
    );
  };

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

      {/* Exception Impact (If any) */}
      {renderExceptionImpact()}

      {/* Deterministic Timeline */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h6" sx={{ mb: 2, color: 'text.primary' }}>Financial Lifecycle</Typography>
        <Grid container spacing={2}>
          {/* Payment */}
          <Grid item xs={12} md={4}>
            <Card sx={{ height: '100%', position: 'relative' }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 3 }}>
                  <Box sx={{ p: 1, bgcolor: 'action.hover', borderRadius: 2 }}><CreditCard size={20} color="inherit" /></Box>
                  <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Payment Gateway</Typography>
                </Box>
                <Stack spacing={2}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Typography color="text.secondary" variant="body2">Amount</Typography>
                    <Typography variant="h6" sx={{ fontFamily: 'monospace' }}>{formatINR(result.paymentAmount)}</Typography>
                  </Box>
                  <Divider sx={{ borderColor: 'divider' }} />
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography color="text.secondary" variant="body2">Status</Typography>
                    <Typography variant="body2" sx={{ fontWeight: 500 }}>{result.paymentStatus}</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography color="text.secondary" variant="body2">Order Amount</Typography>
                    <Typography variant="body2" sx={{ fontFamily: 'monospace', color: 'text.secondary' }}>{formatINR(result.orderAmount)}</Typography>
                  </Box>
                </Stack>
              </CardContent>
            </Card>
          </Grid>

          {/* Settlement */}
          <Grid item xs={12} md={4}>
            <Card sx={{ height: '100%' }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 3 }}>
                  <Box sx={{ p: 1, bgcolor: 'action.hover', borderRadius: 2 }}><Receipt size={20} color="inherit" /></Box>
                  <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Settlement</Typography>
                </Box>
                <Stack spacing={2}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Typography color="text.secondary" variant="body2">Gross Amount</Typography>
                    <Typography variant="h6" sx={{ fontFamily: 'monospace', color: result.settlementPresent ? 'inherit' : 'text.disabled' }}>
                      {formatINR(result.settlementGrossAmount)}
                    </Typography>
                  </Box>
                  <Divider sx={{ borderColor: 'divider' }} />
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography color="text.secondary" variant="body2">Status</Typography>
                    <Typography variant="body2" sx={{ fontWeight: 500, color: result.settlementPresent ? 'inherit' : 'error.main' }}>
                      {result.settlementPresent ? result.settlementStatus : 'MISSING'}
                    </Typography>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography color="text.secondary" variant="body2">Net Amount (Less Fees)</Typography>
                    <Typography variant="body2" sx={{ fontFamily: 'monospace', color: 'text.secondary' }}>{formatINR(result.settlementNetAmount)}</Typography>
                  </Box>
                </Stack>
              </CardContent>
            </Card>
          </Grid>

          {/* Bank */}
          <Grid item xs={12} md={4}>
            <Card sx={{ height: '100%' }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 3 }}>
                  <Box sx={{ p: 1, bgcolor: 'action.hover', borderRadius: 2 }}><Building2 size={20} color="inherit" /></Box>
                  <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Bank Statement</Typography>
                </Box>
                <Stack spacing={2}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Typography color="text.secondary" variant="body2">Total Credited</Typography>
                    <Typography variant="h6" sx={{ fontFamily: 'monospace', color: result.bankTransactionCount > 0 ? 'inherit' : 'text.disabled' }}>
                      {formatINR(result.bankTransactionAmount)}
                    </Typography>
                  </Box>
                  <Divider sx={{ borderColor: 'divider' }} />
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography color="text.secondary" variant="body2">Transactions Found</Typography>
                    <Typography variant="body2" sx={{ fontWeight: 500, color: result.bankTransactionCount > 1 ? '#f59e0b' : 'inherit' }}>
                      {result.bankTransactionCount}
                    </Typography>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography color="text.secondary" variant="body2">Reconciliation</Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                      {isMatch ? <CheckCircle2 size={14} color="#10b981" /> : <AlertCircle size={14} color="#f43f5e" />}
                      <Typography variant="body2" sx={{ color: isMatch ? '#10b981' : '#f43f5e', fontWeight: 600 }}>
                        {isMatch ? 'MATCHED' : 'EXCEPTION'}
                      </Typography>
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
            <Box sx={{ px: 3, py: 2, borderBottom: '1px solid', borderColor: 'divider' }}>
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
                      <TableCell align="right" sx={{ fontWeight: 500, fontFamily: 'monospace' }}>{formatINR(tx.amount)}</TableCell>
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
