import React, { useEffect, useState } from 'react';
import { Box, Typography, Card, CardContent, Grid, Button, Stack, Alert, Divider } from '@mui/material';
import { Server, Cpu, Database, CheckCircle2, RefreshCw, Layers } from 'lucide-react';
import { checkBackendHealth, checkAiServiceHealth } from '../services/api';
import { HealthResponse } from '../types/api';
import { StatusBadge } from '../components/common/StatusBadge';

export const HomePage: React.FC = () => {
  const [backendHealth, setBackendHealth] = useState<HealthResponse | null>(null);
  const [aiHealth, setAiHealth] = useState<HealthResponse | null>(null);

  const [loadingBackend, setLoadingBackend] = useState<boolean>(true);
  const [loadingAi, setLoadingAi] = useState<boolean>(true);

  const [backendError, setBackendError] = useState<string | null>(null);
  const [aiError, setAiError] = useState<string | null>(null);

  const fetchHealthStatuses = async () => {
    setLoadingBackend(true);
    setLoadingAi(true);
    setBackendError(null);
    setAiError(null);

    try {
      const bRes = await checkBackendHealth();
      setBackendHealth(bRes);
    } catch (err: any) {
      setBackendError(err.message || 'Failed to reach backend service');
    } finally {
      setLoadingBackend(false);
    }

    try {
      const aiRes = await checkAiServiceHealth();
      setAiHealth(aiRes);
    } catch (err: any) {
      setAiError(err.message || 'Failed to reach AI service');
    } finally {
      setLoadingAi(false);
    }
  };

  useEffect(() => {
    fetchHealthStatuses();
  }, []);

  return (
    <Box>
      {/* Header Banner */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h3" sx={{ fontWeight: 800, mb: 1, color: 'text.primary' }}>
          Intelligent Multi-Source Reconciliation Agent
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ maxWidth: 800, mb: 3 }}>
          Phase 1 Foundation Ready — Decoupled deterministic financial reconciliation engine & AI exception investigation agent scaffold.
        </Typography>

        <Alert severity="info" sx={{ backgroundColor: 'rgba(2, 132, 199, 0.12)', border: '1px solid rgba(2, 132, 199, 0.3)', color: '#38bdf8' }}>
          <strong>Phase 1 Architecture Status:</strong> All core scaffolding components (Frontend, Spring Boot Java 21, FastAPI Python 3.12, PostgreSQL 16, Docker Compose) are initialized and operational.
        </Alert>
      </Box>

      {/* System Infrastructure Readiness Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {/* Backend Card */}
        <Grid item xs={12} md={4}>
          <Card sx={{ height: '100%', backgroundColor: '#111827', borderColor: 'rgba(255, 255, 255, 0.08)' }}>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                  <Box sx={{ p: 1, borderRadius: 2, backgroundColor: 'rgba(2, 132, 199, 0.15)', color: '#38bdf8' }}>
                    <Server size={22} />
                  </Box>

                  <Typography variant="h6" sx={{ fontWeight: 600 }}>
                    Spring Boot Backend
                  </Typography>
                </Box>
                <StatusBadge
                  label="Backend"
                  isOnline={!!backendHealth}
                  isLoading={loadingBackend}
                  error={backendError}
                />
              </Box>

              <Divider sx={{ my: 1.5, borderColor: 'rgba(255, 255, 255, 0.08)' }} />

              <Stack spacing={1}>
                <Typography variant="body2" color="text.secondary">
                  <strong>Stack:</strong> Java 21 / Spring Boot 3.2
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  <strong>Health Endpoint:</strong> <code>GET /api/health</code>
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  <strong>Status:</strong> {backendHealth ? backendHealth.status : (loadingBackend ? 'Checking...' : 'Offline')}
                </Typography>
              </Stack>
            </CardContent>
          </Card>
        </Grid>

        {/* AI Service Card */}
        <Grid item xs={12} md={4}>
          <Card sx={{ height: '100%', backgroundColor: '#111827', borderColor: 'rgba(255, 255, 255, 0.08)' }}>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                  <Box sx={{ p: 1, borderRadius: 2, backgroundColor: 'rgba(16, 185, 129, 0.15)', color: '#34d399' }}>
                    <Cpu size={22} />
                  </Box>
                  <Typography variant="h6" sx={{ fontWeight: 600 }}>
                    FastAPI AI Service
                  </Typography>
                </Box>
                <StatusBadge
                  label="AI Service"
                  isOnline={!!aiHealth}
                  isLoading={loadingAi}
                  error={aiError}
                />
              </Box>

              <Divider sx={{ my: 1.5, borderColor: 'rgba(255, 255, 255, 0.08)' }} />

              <Stack spacing={1}>
                <Typography variant="body2" color="text.secondary">
                  <strong>Stack:</strong> Python 3.12 / FastAPI / Pandas
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  <strong>Health Endpoint:</strong> <code>GET /health</code>
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  <strong>Status:</strong> {aiHealth ? aiHealth.status : (loadingAi ? 'Checking...' : 'Offline')}
                </Typography>
              </Stack>
            </CardContent>
          </Card>
        </Grid>

        {/* Database Card */}
        <Grid item xs={12} md={4}>
          <Card sx={{ height: '100%', backgroundColor: '#111827', borderColor: 'rgba(255, 255, 255, 0.08)' }}>
            <CardContent>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                  <Box sx={{ p: 1, borderRadius: 2, backgroundColor: 'rgba(245, 158, 11, 0.15)', color: '#fbbf24' }}>
                    <Database size={22} />
                  </Box>
                  <Typography variant="h6" sx={{ fontWeight: 600 }}>
                    PostgreSQL 16
                  </Typography>
                </Box>
                <StatusBadge
                  label="Postgres"
                  isOnline={true}
                  isLoading={false}
                />
              </Box>

              <Divider sx={{ my: 1.5, borderColor: 'rgba(255, 255, 255, 0.08)' }} />

              <Stack spacing={1}>
                <Typography variant="body2" color="text.secondary">
                  <strong>Storage:</strong> Persistent Docker Volume
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  <strong>Port:</strong> 5432 (Configurable)
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  <strong>Database Name:</strong> aifinance
                </Typography>
              </Stack>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Action Bar */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', p: 3, borderRadius: 3, backgroundColor: '#111827', border: '1px solid rgba(255, 255, 255, 0.08)' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Layers size={24} color="#0284c7" />
          <Typography variant="subtitle1" sx={{ fontWeight: 600, color: 'text.primary' }}>
            System Microservices Verification
          </Typography>
        </Box>

        <Button
          variant="contained"
          color="primary"
          startIcon={<RefreshCw size={16} />}
          onClick={fetchHealthStatuses}
          disabled={loadingBackend || loadingAi}
        >
          Ping Microservices Health
        </Button>
      </Box>

      {/* Architecture Planned Scope list */}
      <Box sx={{ mt: 4 }}>
        <Typography variant="h5" sx={{ fontWeight: 700, mb: 2 }}>
          System Architecture Plan Overview
        </Typography>

        <Grid container spacing={2}>
          {[
            { title: '1. Multi-Source Ingestion', desc: 'Orders, Payments, Refunds, Settlements synthetic batch records.' },
            { title: '2. Deterministic Matching', desc: 'Java Spring Boot exact matching engine measuring throughput & accuracy.' },
            { title: '3. AI Exception Investigation', desc: 'FastAPI + LLM agent determining root causes & evidence explanations.' },
            { title: '4. Executive Audit Trail', desc: 'Immutable audit history and interactive discrepancy workbench.' },
          ].map((item, idx) => (
            <Grid item xs={12} sm={6} md={3} key={idx}>
              <Box sx={{ p: 2.5, borderRadius: 2, backgroundColor: 'rgba(255, 255, 255, 0.02)', border: '1px solid rgba(255, 255, 255, 0.05)' }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                  <CheckCircle2 size={18} color="#10b981" />
                  <Typography variant="subtitle2" sx={{ fontWeight: 600, color: 'text.primary' }}>
                    {item.title}
                  </Typography>
                </Box>
                <Typography variant="body2" color="text.secondary">
                  {item.desc}
                </Typography>
              </Box>
            </Grid>
          ))}
        </Grid>
      </Box>
    </Box>
  );
};
