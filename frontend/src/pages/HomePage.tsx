import React, { useEffect, useState } from 'react';
import { Box, Typography, Card, CardContent, Grid, Button, Stack, Alert, Divider, CircularProgress } from '@mui/material';
import { Server, Cpu, Database, ArrowRight } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { checkBackendHealth, checkAiServiceHealth, getReconciliationReport } from '../services/api';
import { HealthResponse, ReconciliationReport } from '../types/api';
import { StatusBadge } from '../components/common/StatusBadge';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, Cell } from 'recharts';

export const HomePage: React.FC = () => {
  const navigate = useNavigate();
  const [backendHealth, setBackendHealth] = useState<HealthResponse | null>(null);
  const [aiHealth, setAiHealth] = useState<HealthResponse | null>(null);
  const [loadingBackend, setLoadingBackend] = useState<boolean>(true);
  const [loadingAi, setLoadingAi] = useState<boolean>(true);
  const [backendError, setBackendError] = useState<string | null>(null);
  const [aiError, setAiError] = useState<string | null>(null);

  const [report, setReport] = useState<ReconciliationReport | null>(null);
  const [loadingReport, setLoadingReport] = useState<boolean>(true);
  const [reportError, setReportError] = useState<string | null>(null);

  const fetchData = async () => {
    setLoadingBackend(true); setLoadingAi(true); setLoadingReport(true);
    setBackendError(null); setAiError(null); setReportError(null);

    try {
      const bRes = await checkBackendHealth();
      setBackendHealth(bRes);
    } catch (err: any) { setBackendError(err.message || 'Failed to reach backend service'); }
    finally { setLoadingBackend(false); }

    try {
      const aiRes = await checkAiServiceHealth();
      setAiHealth(aiRes);
    } catch (err: any) { setAiError(err.message || 'Failed to reach AI service'); }
    finally { setLoadingAi(false); }

    try {
      const rep = await getReconciliationReport();
      setReport(rep);
    } catch (err: any) { setReportError(err.message || 'Failed to fetch report'); }
    finally { setLoadingReport(false); }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const getExceptionChartData = () => {
    if (!report || !report.exceptionBreakdown) return [];
    return Object.entries(report.exceptionBreakdown).map(([key, value]) => ({
      name: key,
      count: value
    })).filter(item => item.count > 0);
  };

  const chartData = getExceptionChartData();

  return (
    <Box>
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <Box>
          <Typography variant="h3" sx={{ fontWeight: 800, mb: 1, color: 'text.primary' }}>
            Reconciliation Dashboard
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ maxWidth: 800, mb: 3 }}>
            Overview of deterministic reconciliation results and AI integration status.
          </Typography>
        </Box>
        <Button
          variant="contained"
          color="primary"
          endIcon={<ArrowRight size={18} />}
          onClick={() => navigate('/reconciliation')}
          sx={{ py: 1.5, px: 3, fontWeight: 'bold' }}
        >
          View All Results
        </Button>
      </Box>

      {/* Metrics Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} md={3}>
          <Card sx={{ height: '100%', bgcolor: 'background.paper' }}>
            <CardContent>
              <Typography variant="subtitle2" color="text.secondary" gutterBottom>Total Records</Typography>
              {loadingReport ? <CircularProgress size={24} /> :
                <Typography variant="h3" sx={{ fontWeight: 700 }}>{report?.totalRecords || 0}</Typography>}
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={3}>
          <Card sx={{ height: '100%', bgcolor: 'background.paper', borderColor: 'secondary.main', borderWidth: 1, borderStyle: 'solid' }}>
            <CardContent>
              <Typography variant="subtitle2" color="text.secondary" gutterBottom>Matches</Typography>
              {loadingReport ? <CircularProgress size={24} /> :
                <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 1 }}>
                  <Typography variant="h3" sx={{ fontWeight: 700, color: 'secondary.main' }}>{report?.matchedRecords || 0}</Typography>
                  <Typography variant="subtitle1" color="secondary.light">({report?.matchRate?.toFixed(2)}%)</Typography>
                </Box>}
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={3}>
          <Card sx={{ height: '100%', bgcolor: 'background.paper', borderColor: 'error.main', borderWidth: 1, borderStyle: 'solid' }}>
            <CardContent>
              <Typography variant="subtitle2" color="text.secondary" gutterBottom>Exceptions</Typography>
              {loadingReport ? <CircularProgress size={24} /> :
                <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 1 }}>
                  <Typography variant="h3" sx={{ fontWeight: 700, color: 'error.main' }}>{report?.exceptionRecords || 0}</Typography>
                  <Typography variant="subtitle1" color="error.light">({report?.exceptionRate?.toFixed(2)}%)</Typography>
                </Box>}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Exception Breakdown Chart */}
      <Grid container spacing={3} sx={{ mb: 5 }}>
        <Grid item xs={12} md={8}>
          <Card sx={{ height: '100%', bgcolor: 'background.paper' }}>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 3 }}>Exception Breakdown</Typography>
              {loadingReport ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}><CircularProgress /></Box>
              ) : reportError ? (
                <Alert severity="error">{reportError}</Alert>
              ) : chartData.length > 0 ? (
                <Box sx={{ height: 300 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={chartData} margin={{ top: 10, right: 30, left: 0, bottom: 40 }}>
                      <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" />
                      <XAxis dataKey="name" angle={-45} textAnchor="end" height={80} stroke="#9ca3af" tick={{ fontSize: 12 }} />
                      <YAxis stroke="#9ca3af" />
                      <Tooltip contentStyle={{ backgroundColor: '#1f2937', border: 'none', borderRadius: 8, color: '#fff' }} />
                      <Bar dataKey="count" radius={[4, 4, 0, 0]}>
                        {chartData.map((_entry, index) => (
                          <Cell key={`cell-${index}`} fill="#f43f5e" />
                        ))}
                      </Bar>
                    </BarChart>
                  </ResponsiveContainer>
                </Box>
              ) : (
                <Typography variant="body1" color="text.secondary">No exceptions found.</Typography>
              )}
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={4}>
          <Card sx={{ height: '100%', bgcolor: 'background.paper' }}>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 3 }}>Exception Summary</Typography>
              {loadingReport ? <CircularProgress /> : (
                <Stack spacing={2} divider={<Divider sx={{ borderColor: 'rgba(255,255,255,0.05)' }} />}>
                  {chartData.map((item) => (
                    <Box key={item.name} sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Typography variant="body2" sx={{ fontWeight: 500 }}>{item.name}</Typography>
                      <Typography variant="body2" color="error.main" sx={{ fontWeight: 700 }}>{item.count}</Typography>
                    </Box>
                  ))}
                  {chartData.length === 0 && <Typography color="text.secondary">All clear.</Typography>}
                </Stack>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Typography variant="h5" sx={{ fontWeight: 700, mb: 3 }}>
        System Microservices Verification
      </Typography>

      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} md={4}>
          <Card sx={{ height: '100%', backgroundColor: '#111827' }}>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                  <Box sx={{ p: 1, borderRadius: 2, backgroundColor: 'rgba(2, 132, 199, 0.15)', color: '#38bdf8' }}>
                    <Server size={22} />
                  </Box>
                  <Typography variant="h6" sx={{ fontWeight: 600 }}>Spring Boot Backend</Typography>
                </Box>
                <StatusBadge label="Backend" isOnline={!!backendHealth} isLoading={loadingBackend} error={backendError} />
              </Box>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={4}>
          <Card sx={{ height: '100%', backgroundColor: '#111827' }}>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                  <Box sx={{ p: 1, borderRadius: 2, backgroundColor: 'rgba(16, 185, 129, 0.15)', color: '#34d399' }}>
                    <Cpu size={22} />
                  </Box>
                  <Typography variant="h6" sx={{ fontWeight: 600 }}>FastAPI AI Service</Typography>
                </Box>
                <StatusBadge label="AI Service" isOnline={!!aiHealth} isLoading={loadingAi} error={aiError} />
              </Box>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={4}>
          <Card sx={{ height: '100%', backgroundColor: '#111827' }}>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                  <Box sx={{ p: 1, borderRadius: 2, backgroundColor: 'rgba(245, 158, 11, 0.15)', color: '#fbbf24' }}>
                    <Database size={22} />
                  </Box>
                  <Typography variant="h6" sx={{ fontWeight: 600 }}>PostgreSQL 16</Typography>
                </Box>
                <StatusBadge label="Postgres" isOnline={true} isLoading={false} />
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};
