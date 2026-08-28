import axios from 'axios';
import { HealthResponse } from '../types/api';

const BACKEND_BASE_URL = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080';
const AI_SERVICE_BASE_URL = import.meta.env.VITE_AI_SERVICE_URL || 'http://localhost:8000';

export const backendClient = axios.create({
  baseURL: BACKEND_BASE_URL,
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const aiServiceClient = axios.create({
  baseURL: AI_SERVICE_BASE_URL,
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const checkBackendHealth = async (): Promise<HealthResponse> => {
  const response = await backendClient.get<HealthResponse>('/api/health');
  return response.data;
};

export const checkAiServiceHealth = async (): Promise<HealthResponse> => {
  const response = await aiServiceClient.get<HealthResponse>('/health');
  return response.data;
};

export const getReconciliationReport = async (): Promise<any> => {
  const response = await backendClient.get('/api/reconciliation/report');
  return response.data;
};

export interface PaginatedResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
}

export const getReconciliationResults = async (status?: string, exceptionType?: string, page = 0, size = 20): Promise<PaginatedResponse<any>> => {
  const params: any = { page, size };
  if (status && status !== 'ALL') params.status = status;
  if (exceptionType && exceptionType !== 'ALL') params.exceptionType = exceptionType;

  const response = await backendClient.get('/api/reconciliation/results', { params });
  return response.data;
};

export const getReconciliationResult = async (paymentId: string): Promise<any> => {
  const response = await backendClient.get(`/api/reconciliation/results/${paymentId}`);
  return response.data;
};

export const getAiExplanation = async (paymentId: string): Promise<any> => {
  const response = await backendClient.get(`/api/reconciliation/results/${paymentId}/explanation`);
  return response.data;
};
