import React, { useEffect, useState } from 'react';
import { Box, Typography, Card, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, MenuItem, Select, FormControl, InputLabel, CircularProgress, Alert, TablePagination, Button, Chip } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { getReconciliationResults, getReconciliationRuns } from '../services/api';
import { ReconciliationResult, ExceptionType, ReconciliationRun } from '../types/api';
import { DatasetUploadModal } from '../components/reconciliation/DatasetUploadModal';
import { Play } from 'lucide-react';
import { formatINR } from '../utils/currency';
export const ResultsPage: React.FC = () => {
  const navigate = useNavigate();
  const [modalOpen, setModalOpen] = useState(false);
  const [results, setResults] = useState<ReconciliationResult[]>([]);
  const [runs, setRuns] = useState<ReconciliationRun[]>([]);
  const [selectedRunId, setSelectedRunId] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [exceptionFilter, setExceptionFilter] = useState<string>('ALL');

  const [currentPage, setCurrentPage] = useState<number>(0);
  const [pageSize, setPageSize] = useState<number>(10);
  const [totalElements, setTotalElements] = useState<number>(0);

  const fetchRuns = async () => {
    try {
      const data = await getReconciliationRuns();
      setRuns(data);
      if (data.length > 0 && !selectedRunId) {
        const completedRun = data.find((r: ReconciliationRun) => r.status === 'COMPLETED');
        if (completedRun) setSelectedRunId(completedRun.id);
      }
    } catch (err) {
      console.error('Failed to fetch runs', err);
    }
  };

  const fetchResults = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getReconciliationResults(statusFilter, exceptionFilter, currentPage, pageSize, selectedRunId || undefined);
      setResults(data.content);
      setTotalElements(data.totalElements);
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Failed to fetch results');
    } finally {
      setLoading(false);
    }
  };

  const handleRunStarted = async (newRunId: string) => {
    await fetchRuns();
    setSelectedRunId(newRunId);
  };

  useEffect(() => {
    fetchRuns();
  }, []);

  const selectedRunStatus = runs.find(r => r.id === selectedRunId)?.status;

  useEffect(() => {
    if (selectedRunStatus !== 'IN_PROGRESS') {
      fetchResults();
    }
  }, [statusFilter, exceptionFilter, currentPage, pageSize, selectedRunId, selectedRunStatus]);

  useEffect(() => {
    let interval: NodeJS.Timeout;
    if (runs.some(r => r.status === 'IN_PROGRESS')) {
      interval = setInterval(() => {
        fetchRuns();
      }, 2000);
    }
    return () => {
      if (interval) clearInterval(interval);
    };
  }, [runs]);

  useEffect(() => {
    setCurrentPage(0);
  }, [statusFilter, exceptionFilter, selectedRunId]);

  const getStatusColor = (status: string) => {
    return status === 'MATCH' ? 'success' : 'error';
  };



  return (
    <Box>
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
        <Box>
          <Typography variant="h3" sx={{ mb: 1 }}>Reconciliation Records</Typography>
          <Typography variant="body1" color="text.secondary">
            Deterministic results for all transactions in the current run.
          </Typography>
        </Box>
        <Button
          variant="contained"
          color="primary"
          onClick={() => setModalOpen(true)}
          disabled={runs.some(r => r.status === 'IN_PROGRESS')}
          startIcon={runs.some(r => r.status === 'IN_PROGRESS') ? <CircularProgress size={16} /> : <Play size={16} />}
          sx={{ bgcolor: '#fafafa', color: '#09090b', '&:hover': { bgcolor: '#e4e4e7' } }}
        >
          {runs.some(r => r.status === 'IN_PROGRESS') ? 'Running...' : 'New Run'}
        </Button>
      </Box>

      {/* Filters Bar */}
      <Box sx={{ display: 'flex', gap: 2, mb: 4, flexWrap: 'wrap' }}>
        <FormControl size="small" sx={{ minWidth: 240, '& .MuiOutlinedInput-root': { bgcolor: '#121214' } }}>
          <InputLabel>Run ID</InputLabel>
          <Select value={selectedRunId} label="Run ID" onChange={(e) => setSelectedRunId(e.target.value)}>
            {runs.map(run => (
              <MenuItem key={run.id} value={run.id}>
                {new Date(run.executionTime).toLocaleString()} ({run.status})
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl size="small" sx={{ minWidth: 160, '& .MuiOutlinedInput-root': { bgcolor: '#121214' } }}>
          <InputLabel>Status</InputLabel>
          <Select value={statusFilter} label="Status" onChange={(e) => setStatusFilter(e.target.value)}>
            <MenuItem value="ALL">All Statuses</MenuItem>
            <MenuItem value="MATCH">Matched</MenuItem>
            <MenuItem value="EXCEPTION">Exception</MenuItem>
          </Select>
        </FormControl>

        <FormControl size="small" sx={{ minWidth: 240, '& .MuiOutlinedInput-root': { bgcolor: '#121214' } }}>
          <InputLabel>Exception Type</InputLabel>
          <Select value={exceptionFilter} label="Exception Type" onChange={(e) => setExceptionFilter(e.target.value)}>
            <MenuItem value="ALL">All Exceptions</MenuItem>
            {Object.keys(ExceptionType).map(key => (
              <MenuItem key={key} value={key}>{key.replace(/_/g, ' ')}</MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>

      {error && <Alert severity="error" sx={{ mb: 4 }}>{error}</Alert>}

      <Card>
        <TableContainer sx={{ maxHeight: 600 }}>
          <Table stickyHeader>
            <TableHead>
              <TableRow>
                <TableCell>Payment ID</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Exception</TableCell>
                <TableCell align="right">Payment Amt</TableCell>
                <TableCell align="right">Settlement Amt</TableCell>
                <TableCell align="center">Bank Txs</TableCell>
                <TableCell align="right">Confidence</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={7} align="center" sx={{ py: 6 }}>
                    <CircularProgress size={30} />
                  </TableCell>
                </TableRow>
              ) : selectedRunStatus === 'IN_PROGRESS' ? (
                <TableRow>
                  <TableCell colSpan={7} align="center" sx={{ py: 6 }}>
                    <CircularProgress sx={{ mb: 2 }} size={30} /><br/>
                    <Typography color="text.secondary">Processing reconciliation batch...</Typography>
                  </TableCell>
                </TableRow>
              ) : results.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} align="center" sx={{ py: 6, color: 'text.secondary' }}>
                    No records match the current filters.
                  </TableCell>
                </TableRow>
              ) : (
                results.map((row) => (
                  <TableRow
                    key={row.paymentId}
                    hover
                    sx={{ cursor: 'pointer' }}
                    onClick={() => navigate(`/reconciliation/${row.paymentId}?runId=${selectedRunId}`)}
                  >
                    <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.85rem' }}>{row.paymentId}</TableCell>
                    <TableCell>
                      <Chip
                        label={row.overallStatus}
                        color={getStatusColor(row.overallStatus)}
                        size="small"
                        sx={{ borderRadius: '4px', height: 24, fontSize: '0.75rem', fontWeight: 700 }}
                      />
                    </TableCell>
                    <TableCell>
                      {row.exceptionType !== 'NONE' ? (
                        <Chip
                          label={row.exceptionType.replace(/_/g, ' ')}
                          size="small"
                          sx={{ borderRadius: '4px', height: 24, fontSize: '0.7rem', bgcolor: 'rgba(245, 158, 11, 0.1)', color: '#fbbf24', border: '1px solid rgba(245, 158, 11, 0.2)' }}
                        />
                      ) : (
                        <Typography variant="body2" sx={{ color: 'text.secondary', fontSize: '0.8rem' }}>-</Typography>
                      )}
                    </TableCell>
                    <TableCell align="right" sx={{ fontWeight: 500 }}>{formatINR(row.paymentAmount)}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 500 }}>{formatINR(row.settlementGrossAmount)}</TableCell>
                    <TableCell align="center">{row.bankTransactionCount}</TableCell>
                    <TableCell align="right" sx={{ color: 'text.secondary' }}>{(row.confidenceScore * 100).toFixed(0)}%</TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
        <TablePagination
          component="div"
          count={totalElements}
          page={currentPage}
          onPageChange={(_, newPage) => setCurrentPage(newPage)}
          rowsPerPage={pageSize}
          onRowsPerPageChange={(e) => {
            setPageSize(parseInt(e.target.value, 10));
            setCurrentPage(0);
          }}
          rowsPerPageOptions={[10, 25, 50, 100]}
          sx={{ borderTop: '1px solid rgba(255,255,255,0.06)' }}
        />
      </Card>

      <DatasetUploadModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        onRunStarted={handleRunStarted}
      />
    </Box>
  );
};
