import React, { useEffect, useState } from 'react';
import { Box, Typography, Card, CardContent, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, MenuItem, Select, FormControl, InputLabel, Chip, CircularProgress, Alert } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { getReconciliationResults } from '../services/api';
import { ReconciliationResult, ExceptionType } from '../types/api';

export const ResultsPage: React.FC = () => {
  const navigate = useNavigate();
  const [results, setResults] = useState<ReconciliationResult[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [exceptionFilter, setExceptionFilter] = useState<string>('ALL');

  const fetchResults = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getReconciliationResults(statusFilter, exceptionFilter);
      setResults(data);
    } catch (err: any) {
      setError(err.message || 'Failed to fetch results');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchResults();
  }, [statusFilter, exceptionFilter]);

  return (
    <Box>
      <Typography variant="h3" sx={{ fontWeight: 800, mb: 1, color: 'text.primary' }}>
        Reconciliation Results
      </Typography>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
        Deterministic matching engine results.
      </Typography>

      <Card sx={{ mb: 4, bgcolor: 'background.paper' }}>
        <CardContent sx={{ display: 'flex', gap: 3, alignItems: 'center' }}>
          <FormControl size="small" sx={{ minWidth: 200 }}>
            <InputLabel>Status</InputLabel>
            <Select
              value={statusFilter}
              label="Status"
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <MenuItem value="ALL">All Statuses</MenuItem>
              <MenuItem value="MATCH">MATCH</MenuItem>
              <MenuItem value="EXCEPTION">EXCEPTION</MenuItem>
            </Select>
          </FormControl>

          <FormControl size="small" sx={{ minWidth: 250 }}>
            <InputLabel>Exception Type</InputLabel>
            <Select
              value={exceptionFilter}
              label="Exception Type"
              onChange={(e) => setExceptionFilter(e.target.value)}
            >
              <MenuItem value="ALL">All Exceptions</MenuItem>
              {Object.keys(ExceptionType).map(key => (
                <MenuItem key={key} value={key}>{key}</MenuItem>
              ))}
            </Select>
          </FormControl>
        </CardContent>
      </Card>

      {error && <Alert severity="error" sx={{ mb: 4 }}>{error}</Alert>}

      <TableContainer component={Paper} sx={{ bgcolor: 'background.paper' }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell sx={{ fontWeight: 'bold' }}>Payment ID</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Overall Status</TableCell>
              <TableCell sx={{ fontWeight: 'bold' }}>Exception Type</TableCell>
              <TableCell align="right" sx={{ fontWeight: 'bold' }}>Payment Amount</TableCell>
              <TableCell align="right" sx={{ fontWeight: 'bold' }}>Settlement Amount</TableCell>
              <TableCell align="center" sx={{ fontWeight: 'bold' }}>Bank Txs</TableCell>
              <TableCell align="right" sx={{ fontWeight: 'bold' }}>Confidence</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={7} align="center" sx={{ py: 4 }}>
                  <CircularProgress />
                </TableCell>
              </TableRow>
            ) : results.length === 0 ? (
              <TableRow>
                <TableCell colSpan={7} align="center" sx={{ py: 4 }}>
                  No results found.
                </TableCell>
              </TableRow>
            ) : (
              results.map((row) => (
                <TableRow 
                  key={row.paymentId} 
                  hover 
                  sx={{ cursor: 'pointer' }}
                  onClick={() => navigate(`/reconciliation/${row.paymentId}`)}
                >
                  <TableCell>{row.paymentId}</TableCell>
                  <TableCell>
                    <Chip 
                      label={row.overallStatus} 
                      color={row.overallStatus === 'MATCH' ? 'success' : 'error'} 
                      size="small" 
                    />
                  </TableCell>
                  <TableCell>
                    {row.exceptionType !== 'NONE' && (
                      <Chip label={row.exceptionType} color="warning" size="small" variant="outlined" />
                    )}
                  </TableCell>
                  <TableCell align="right">{row.paymentAmount?.toFixed(2)}</TableCell>
                  <TableCell align="right">{row.settlementGrossAmount?.toFixed(2) || '-'}</TableCell>
                  <TableCell align="center">{row.bankTransactionCount}</TableCell>
                  <TableCell align="right">{(row.confidenceScore * 100).toFixed(0)}%</TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
};
