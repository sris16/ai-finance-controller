import React, { useEffect, useState } from 'react';
import { Box, Typography, Card, CardContent, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, MenuItem, Select, FormControl, InputLabel, Chip, CircularProgress, Alert, TablePagination, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { getReconciliationResults, getReconciliationRuns } from '../services/api';
import { ReconciliationResult, ExceptionType, ReconciliationRun } from '../types/api';
import { DatasetUploadModal } from '../components/reconciliation/DatasetUploadModal';

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
  const [pageSize, setPageSize] = useState<number>(20);
  const [totalElements, setTotalElements] = useState<number>(0);

  const fetchRuns = async () => {
    try {
      const data = await getReconciliationRuns();
      setRuns(data);
      if (data.length > 0 && !selectedRunId) {
        // default to latest completed
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

  // Reset to page 0 when filters change
  useEffect(() => {
    setCurrentPage(0);
  }, [statusFilter, exceptionFilter, selectedRunId]);

  return (
    <Box>
      <Typography variant="h3" sx={{ fontWeight: 800, mb: 1, color: 'text.primary' }}>
        Reconciliation Results
      </Typography>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Typography variant="body1" color="text.secondary">
          Deterministic matching engine results.
        </Typography>
        <Button
          variant="contained"
          color="primary"
          onClick={() => setModalOpen(true)}
          disabled={runs.some(r => r.status === 'IN_PROGRESS')}
        >
          {runs.some(r => r.status === 'IN_PROGRESS') ? <CircularProgress size={24} /> : 'Trigger New Batch'}
        </Button>
      </Box>

      <Card sx={{ mb: 4, bgcolor: 'background.paper' }}>
        <CardContent sx={{ display: 'flex', gap: 3, alignItems: 'center', flexWrap: 'wrap' }}>
          <FormControl size="small" sx={{ minWidth: 250 }}>
            <InputLabel>Reconciliation Run</InputLabel>
            <Select
              value={selectedRunId}
              label="Reconciliation Run"
              onChange={(e) => setSelectedRunId(e.target.value)}
            >
              {runs.map(run => (
                <MenuItem key={run.id} value={run.id}>
                  {new Date(run.executionTime).toLocaleString()} ({run.status})
                </MenuItem>
              ))}
            </Select>
          </FormControl>

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
            ) : selectedRunStatus === 'IN_PROGRESS' ? (
              <TableRow>
                <TableCell colSpan={7} align="center" sx={{ py: 4 }}>
                  <CircularProgress sx={{ mb: 2 }} /><br/>
                  <Typography color="text.secondary">Processing reconciliation batch... Please wait.</Typography>
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
                  onClick={() => navigate(`/reconciliation/${row.paymentId}?runId=${selectedRunId}`)}
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
          rowsPerPageOptions={[5, 10, 20, 50, 100]}
        />
      </TableContainer>

      <DatasetUploadModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        onRunStarted={handleRunStarted}
      />
    </Box>
  );
};
