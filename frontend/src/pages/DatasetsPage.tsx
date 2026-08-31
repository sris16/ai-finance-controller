import React, { useEffect, useState } from 'react';
import { Box, Typography, Card, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Button, CircularProgress, Chip } from '@mui/material';
import { Database, Plus, UploadCloud } from 'lucide-react';
import { getDatasets } from '../services/api';
import { DatasetResponse } from '../types/api';
import { DatasetUploadModal } from '../components/reconciliation/DatasetUploadModal';

export const DatasetsPage: React.FC = () => {
  const [datasets, setDatasets] = useState<DatasetResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [modalOpen, setModalOpen] = useState(false);

  const fetchDatasets = async () => {
    setLoading(true);
    try {
      const data = await getDatasets();
      setDatasets(data);
    } catch (err) {
      console.error('Failed to fetch datasets', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDatasets();
  }, []);

  const handleRunStarted = () => {
    fetchDatasets();
  };

  return (
    <Box>
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
        <Box>
          <Typography variant="h3" sx={{ mb: 1, display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Database size={28} />
            Data Sources
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Manage your uploaded payment and settlement datasets.
          </Typography>
        </Box>
        <Button
          variant="contained"
          color="primary"
          onClick={() => setModalOpen(true)}
          startIcon={<Plus size={16} />}
        >
          New Dataset
        </Button>
      </Box>

      <Card>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Dataset Name</TableCell>
                <TableCell>Dataset ID</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Uploaded At</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={4} align="center" sx={{ py: 6 }}>
                    <CircularProgress size={30} />
                  </TableCell>
                </TableRow>
              ) : datasets.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={4} align="center" sx={{ py: 8 }}>
                    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', color: 'text.secondary' }}>
                      <UploadCloud size={40} style={{ marginBottom: 16, opacity: 0.5 }} />
                      <Typography variant="h6">No datasets found</Typography>
                      <Typography variant="body2" sx={{ mb: 2 }}>Upload a dataset to begin reconciliation.</Typography>
                      <Button variant="outlined" onClick={() => setModalOpen(true)}>Upload Now</Button>
                    </Box>
                  </TableCell>
                </TableRow>
              ) : (
                datasets.map((ds) => (
                  <TableRow key={ds.id} hover>
                    <TableCell sx={{ fontWeight: 600 }}>{ds.name}</TableCell>
                    <TableCell sx={{ fontFamily: 'monospace', fontSize: '0.85rem', color: 'text.secondary' }}>{ds.id}</TableCell>
                    <TableCell>
                      <Chip label="AVAILABLE" size="small" sx={{ bgcolor: 'rgba(16, 185, 129, 0.1)', color: '#10b981', fontWeight: 600, border: '1px solid rgba(16, 185, 129, 0.2)' }} />
                    </TableCell>
                    <TableCell align="right" sx={{ color: 'text.secondary' }}>{new Date(ds.uploadedAt).toLocaleString()}</TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Card>

      <DatasetUploadModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        onRunStarted={handleRunStarted}
      />
    </Box>
  );
};
