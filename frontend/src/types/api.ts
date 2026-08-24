export interface HealthResponse {
  status: string;
  service: string;
  version: string;
  timestamp: string;
}

export interface ServiceHealthState {
  backend: HealthResponse | null;
  aiService: HealthResponse | null;
  backendLoading: boolean;
  aiLoading: boolean;
  backendError: string | null;
  aiError: string | null;
}
