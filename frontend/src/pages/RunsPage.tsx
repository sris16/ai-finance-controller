import React, { useEffect, useState } from 'react';
import { Box, Typography, Card, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, CircularProgress, Chip, Button } from '@mui/material';
import { Activity, Play } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { getReconciliationRuns } from '../services/api';
import { ReconciliationRun } from '../types/api';
import { DatasetUploadModal } from '../components/reconciliation/DatasetUploadModal';

export const RunsPage: React.FC = () => {
  const navigate = useNavigate();
  const [runs, setRuns] = useState<ReconciliationRun[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [modalOpen, setModalOpen] = useState(false);

  const fetchRuns = async () => {
    setLoading(true);
    try {
      const data = await getReconciliationRuns();
      setRuns(data.sort((a: ReconciliationRun, b: ReconciliationRun) => new Date(b.executionTime).getTime() - new Date(a.executionTime).getTime()));
    } catch (err) {
      console.error('Failed to fetch runs', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRuns();
  }, []);

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED': return { color: '#10b981', bg: 'rgba(16, 185, 129, 0.1)', border: 'rgba(16, 185, 129, 0.2)' };
      case 'IN_PROGRESS': return { color: '#0ea5e9', bg: 'rgba(14, 165, 233, 0.1)', border: 'rgba(14, 165, 233, 0.2)' };
      case 'FAILED': return { color: '#f43f5e', bg: 'rgba(244, 63, 94, 0.1)', border: 'rgba(244, 63, 94, 0.2)' };
      default: return { color: '#a1a1aa', bg: 'rgba(255,255,255,0.05)', border: 'rgba(255,255,255,0.1)' };
    }
  };

  return (
    <Box>
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
        <Box>
          <Typography variant="h3" sx={{ mb: 1, display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Activity size={28} />
            Reconciliation Runs
          </Typography>
          <Typography variant="body1" color="text.secondary">
            History of deterministic reconciliation engine executions.
          </Typography>
        </Box>
        <Button
          variant="contained"
          onClick={() => setModalOpen(true)}
          startIcon={<Play size={16} />}
          sx={{ bgcolor: '#fafafa', color: '#09090b', '&:hover': { bgcolor: '#e4e4e7' } }}
        >
          New Run
        </Button>
      </Box>

      <Card>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Run ID</TableCell>
                <TableCell>Execution Time</TableCell>
                <TableCell>Dataset ID</TableCell>
                <TableCell align="right">Total Records</TableCell>
                <TableCell align="center">Status</TableCell>
                <TableCell align="right">Action</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 6 }}>
                    <CircularProgress size={30} />
                  </TableCell>
                </TableRow>
              ) : runs.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 6, color: 'text.secondary' }}>
                    No runs found. Start a new reconciliation run.
                  </TableCell>
                </TableRow>
              ) : (
                runs.map((run) => {
                  const style = getStatusColor(run.status);
                  return (
                    <TableRow key={run.id} hover>
                      <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.85rem' }}>{run.id}</TableCell>
                      <TableCell>{new Date(run.executionTime).toLocaleString()}</TableCell>
                      <TableCell sx={{ color: 'text.secondary', fontSize: '0.85rem' }}>{run.datasetId || '-'}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 500 }}>{run.totalRecords}</TableCell>
                      <TableCell align="center">
                        <Chip
                          label={run.status}
                          size="small"
                          sx={{
                            bgcolor: style.bg,
                            color: style.color,
                            border: `1px solid ${style.border}`,
                            fontWeight: 700
                          }}
                        />
                      </TableCell>
                      <TableCell align="right">
                        <Button
                          size="small"
                          variant="outlined"
                          disabled={run.status !== 'COMPLETED'}
                          onClick={() => navigate(`/reconciliation?runId=${run.id}`)}
                          sx={{ py: 0.5 }}
                        >
                          View Results
                        </Button>
                      </TableCell>
                    </TableRow>
                  );
                })
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Card>

      <DatasetUploadModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        onRunStarted={fetchRuns}
      />
    </Box>
  );
};
