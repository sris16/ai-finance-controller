import React, { useState, useEffect } from 'react';
import { 
  Dialog, DialogTitle, DialogContent, DialogActions, Button, 
  Tabs, Tab, Box, Typography, TextField, CircularProgress, Alert, 
  List, ListItem, ListItemText, ListItemButton, Chip
} from '@mui/material';
import { CloudUpload } from 'lucide-react';
import { getDatasets, uploadDataset, triggerReconciliationRun } from '../../services/api';
import { DatasetResponse } from '../../types/api';

interface DatasetUploadModalProps {
  open: boolean;
  onClose: () => void;
  onRunStarted: (runId: string) => void;
}

export const DatasetUploadModal: React.FC<DatasetUploadModalProps> = ({ open, onClose, onRunStarted }) => {
  const [tab, setTab] = useState(0);
  const [datasets, setDatasets] = useState<DatasetResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Upload Form State
  const [name, setName] = useState('');
  const [orders, setOrders] = useState<File | null>(null);
  const [payments, setPayments] = useState<File | null>(null);
  const [settlements, setSettlements] = useState<File | null>(null);
  const [bankTxs, setBankTxs] = useState<File | null>(null);

  useEffect(() => {
    if (open && tab === 0) {
      fetchDatasets();
    }
  }, [open, tab]);

  const fetchDatasets = async () => {
    setLoading(true);
    try {
      const data = await getDatasets();
      setDatasets(data);
    } catch (err: any) {
      setError('Failed to fetch datasets: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const handleSelectAndRun = async (datasetId: string) => {
    setLoading(true);
    setError(null);
    try {
      const newRun = await triggerReconciliationRun(datasetId);
      onRunStarted(newRun.id);
      onClose();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to start run.');
    } finally {
      setLoading(false);
    }
  };

  const handleLegacyRun = async () => {
    setLoading(true);
    setError(null);
    try {
      const newRun = await triggerReconciliationRun();
      onRunStarted(newRun.id);
      onClose();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to start run.');
    } finally {
      setLoading(false);
    }
  };

  const handleUploadAndRun = async () => {
    if (!name || !orders || !payments) {
      setError('Name, Orders CSV, and Payments CSV are required.');
      return;
    }
    
    setLoading(true);
    setError(null);
    try {
      const uploadedDataset = await uploadDataset(name, orders, payments, settlements || undefined, bankTxs || undefined);
      const newRun = await triggerReconciliationRun(uploadedDataset.id);
      onRunStarted(newRun.id);
      
      // Reset form
      setName('');
      setOrders(null);
      setPayments(null);
      setSettlements(null);
      setBankTxs(null);
      onClose();
    } catch (err: any) {
      setError(err.response?.data?.message || err.message || 'Upload failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onClose={!loading ? onClose : undefined} maxWidth="sm" fullWidth>
      <DialogTitle sx={{ fontWeight: 'bold' }}>Trigger Reconciliation Batch</DialogTitle>
      
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={tab} onChange={(_, v) => setTab(v)}>
          <Tab label="Select Existing Dataset" />
          <Tab label="Upload New Dataset" />
        </Tabs>
      </Box>

      <DialogContent sx={{ minHeight: 300 }}>
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        
        {loading && (
          <Box display="flex" justifyContent="center" alignItems="center" height={200}>
            <CircularProgress />
          </Box>
        )}

        {!loading && tab === 0 && (
          <Box>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Select a previously uploaded dataset to run reconciliation against, or use the legacy static dataset.
            </Typography>
            
            <Button variant="outlined" fullWidth sx={{ mb: 3 }} onClick={handleLegacyRun}>
              Run Legacy Sandbox Data (Default)
            </Button>
            
            <Typography variant="subtitle2" sx={{ mb: 1 }}>Available Datasets:</Typography>
            {datasets.length === 0 ? (
              <Typography color="text.secondary" variant="body2">No custom datasets uploaded yet.</Typography>
            ) : (
              <List sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 1 }}>
                {datasets.map(d => (
                  <ListItem disablePadding key={d.id} divider>
                    <ListItemButton onClick={() => handleSelectAndRun(d.id)}>
                      <ListItemText 
                        primary={d.name} 
                        secondary={`Uploaded: ${new Date(d.uploadedAt).toLocaleString()}`} 
                      />
                      <Chip size="small" label="Select & Run" color="primary" variant="outlined" />
                    </ListItemButton>
                  </ListItem>
                ))}
              </List>
            )}
          </Box>
        )}

        {!loading && tab === 1 && (
          <Box display="flex" flexDirection="column" gap={2}>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
              Upload new CSV files. Orders and Payments are strictly required.
            </Typography>
            
            <TextField 
              label="Dataset Name" 
              size="small" 
              fullWidth 
              value={name} 
              onChange={e => setName(e.target.value)} 
              required
            />
            
            <Box>
              <Typography variant="body2" sx={{ fontWeight: 'bold', mb: 0.5 }}>Orders CSV (Required)</Typography>
              <input type="file" accept=".csv" onChange={e => setOrders(e.target.files?.[0] || null)} />
            </Box>
            
            <Box>
              <Typography variant="body2" sx={{ fontWeight: 'bold', mb: 0.5 }}>Payments CSV (Required)</Typography>
              <input type="file" accept=".csv" onChange={e => setPayments(e.target.files?.[0] || null)} />
            </Box>
            
            <Box>
              <Typography variant="body2" sx={{ fontWeight: 'bold', mb: 0.5 }}>Settlements CSV (Optional)</Typography>
              <input type="file" accept=".csv" onChange={e => setSettlements(e.target.files?.[0] || null)} />
            </Box>

            <Box>
              <Typography variant="body2" sx={{ fontWeight: 'bold', mb: 0.5 }}>Bank Transactions CSV (Optional)</Typography>
              <input type="file" accept=".csv" onChange={e => setBankTxs(e.target.files?.[0] || null)} />
            </Box>
            
            <Button 
              variant="contained" 
              startIcon={<CloudUpload size={18} />} 
              fullWidth 
              sx={{ mt: 2 }}
              onClick={handleUploadAndRun}
              disabled={!name || !orders || !payments}
            >
              Upload & Start Run
            </Button>
          </Box>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={loading}>Cancel</Button>
      </DialogActions>
    </Dialog>
  );
};
