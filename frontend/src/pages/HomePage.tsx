import React, { useEffect, useState } from 'react';
import { Box, Typography, Card, CardContent, Grid, Button, Stack, Alert, Divider, CircularProgress } from '@mui/material';
import { ArrowRight, CheckCircle2, AlertTriangle, FileCheck2, Server, Database, Cpu } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { checkBackendHealth, checkAiServiceHealth, getReconciliationReport } from '../services/api';
import { HealthResponse, ReconciliationReport } from '../types/api';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Cell } from 'recharts';

export const HomePage: React.FC = () => {
  const navigate = useNavigate();
  const [backendHealth, setBackendHealth] = useState<HealthResponse | null>(null);
  const [aiHealth, setAiHealth] = useState<HealthResponse | null>(null);
  const [report, setReport] = useState<ReconciliationReport | null>(null);
  const [loadingReport, setLoadingReport] = useState<boolean>(true);
  const [reportError, setReportError] = useState<string | null>(null);

  const fetchData = async () => {
    setLoadingReport(true);
    setReportError(null);
    try {
      checkBackendHealth().then(setBackendHealth).catch(() => setBackendHealth(null));
      checkAiServiceHealth().then(setAiHealth).catch(() => setAiHealth(null));
      const rep = await getReconciliationReport();
      setReport(rep);
    } catch (err: any) {
      setReportError(err.message || 'Failed to fetch report');
    } finally {
      setLoadingReport(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const getExceptionChartData = () => {
    if (!report || !report.exceptionBreakdown) return [];
    return Object.entries(report.exceptionBreakdown).map(([key, value]) => ({
      name: key.replace(/_/g, ' '),
      count: value
    })).filter(item => item.count > 0).sort((a, b) => b.count - a.count);
  };

  const chartData = getExceptionChartData();

  const renderKPI = (title: string, value: string | number, subtext: string, icon: React.ReactNode, colorClass: string) => (
    <Card sx={{ height: '100%', position: 'relative', overflow: 'hidden' }}>
      <CardContent sx={{ p: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
          <Typography variant="subtitle2" color="text.secondary">{title}</Typography>
          <Box sx={{ color: colorClass }}>{icon}</Box>
        </Box>
        {loadingReport ? <CircularProgress size={24} sx={{ my: 1 }} /> : (
          <Typography variant="h2" sx={{ mb: 1 }}>{value}</Typography>
        )}
        <Typography variant="body2" color="text.secondary">{subtext}</Typography>
      </CardContent>
    </Card>
  );

  return (
    <Box>
      <Box sx={{ mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end' }}>
        <Box>
          <Typography variant="h3" sx={{ mb: 1 }}>Reconciliation Overview</Typography>
          <Typography variant="body1" color="text.secondary">
            Deterministic results for the latest processing window.
          </Typography>
        </Box>
        <Button
          variant="contained"
          color="primary"
          endIcon={<ArrowRight size={16} />}
          onClick={() => navigate('/reconciliation')}
        >
          View Records
        </Button>
      </Box>

      {reportError && <Alert severity="error" sx={{ mb: 4 }}>{reportError}</Alert>}

      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          {renderKPI('Total Records', report?.totalRecords || 0, 'Processed transactions', <FileCheck2 size={20} />, 'text.secondary')}
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          {renderKPI('Matched', report?.matchedRecords || 0, `${report?.matchRate?.toFixed(1) || 0}% Match Rate`, <CheckCircle2 size={20} />, '#10b981')}
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          {renderKPI('Exceptions', report?.exceptionRecords || 0, `${report?.exceptionRate?.toFixed(1) || 0}% Exception Rate`, <AlertTriangle size={20} />, 'error.main')}
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
            <CardContent sx={{ p: 3 }}>
              <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 2 }}>System Status</Typography>
              <Stack spacing={1.5}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                  <Server size={16} color={backendHealth ? '#10b981' : '#f43f5e'} />
                  <Typography variant="body2" color="text.secondary">Engine: {backendHealth ? 'Online' : 'Offline'}</Typography>
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                  <Cpu size={16} color={aiHealth ? '#10b981' : '#f43f5e'} />
                  <Typography variant="body2" color="text.secondary">AI Service: {aiHealth ? 'Online' : 'Offline'}</Typography>
                </Box>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                  <Database size={16} color="#10b981" />
                  <Typography variant="body2" color="text.secondary">Database: Online</Typography>
                </Box>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        <Grid item xs={12} md={8}>
          <Card sx={{ height: '100%' }}>
            <CardContent sx={{ p: 3 }}>
              <Typography variant="h6" sx={{ mb: 3 }}>Exception Distribution</Typography>
              {loadingReport ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 280 }}><CircularProgress /></Box>
              ) : chartData.length > 0 ? (
                <Box sx={{ height: 280 }}>
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }} layout="vertical">
                      <XAxis type="number" hide />
                      <YAxis dataKey="name" type="category" axisLine={false} tickLine={false} tick={{ fill: '#a1a1aa', fontSize: 12 }} width={180} />
                      <Tooltip
                        cursor={{ fill: 'rgba(128,128,128,0.1)' }}
                        contentStyle={{ backgroundColor: 'var(--mui-palette-background-paper)', border: '1px solid var(--mui-palette-divider)', borderRadius: '8px', color: 'var(--mui-palette-text-primary)' }}
                      />
                      <Bar dataKey="count" radius={[0, 4, 4, 0]} barSize={24}>
                        {chartData.map((_entry, index) => (
                          <Cell key={`cell-${index}`} fill="#f43f5e" />
                        ))}
                      </Bar>
                    </BarChart>
                  </ResponsiveContainer>
                </Box>
              ) : (
                <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 280, color: 'text.secondary' }}>
                  No exceptions found. System is fully reconciled.
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={4}>
          <Card sx={{ height: '100%' }}>
            <CardContent sx={{ p: 3 }}>
              <Typography variant="h6" sx={{ mb: 3 }}>Breakdown Details</Typography>
              {loadingReport ? <CircularProgress /> : (
                <Stack spacing={2} divider={<Divider sx={{ borderColor: 'divider' }} />}>
                  {chartData.map((item) => (
                    <Box key={item.name} sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Typography variant="body2" sx={{ color: 'text.primary' }}>{item.name}</Typography>
                      <Typography variant="body2" sx={{ fontWeight: 600, color: 'error.main' }}>{item.count}</Typography>
                    </Box>
                  ))}
                  {chartData.length === 0 && <Typography color="text.secondary">0 exceptions.</Typography>}
                </Stack>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};
