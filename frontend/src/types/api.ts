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

export enum ReconciliationStatus {
  MATCH = 'MATCH',
  EXCEPTION = 'EXCEPTION',
}

export enum ExceptionType {
  NONE = 'NONE',
  AMOUNT_MISMATCH = 'AMOUNT_MISMATCH',
  MISSING_SETTLEMENT = 'MISSING_SETTLEMENT',
  DUPLICATE_TRANSACTION = 'DUPLICATE_TRANSACTION',
  DATE_ANOMALY = 'DATE_ANOMALY',
  STATUS_MISMATCH = 'STATUS_MISMATCH',
}

export interface ExceptionBreakdown {
  AMOUNT_MISMATCH?: number;
  MISSING_SETTLEMENT?: number;
  DUPLICATE_TRANSACTION?: number;
  DATE_ANOMALY?: number;
  STATUS_MISMATCH?: number;
}

export interface ReconciliationReport {
  totalRecords: number;
  matchedRecords: number;
  exceptionRecords: number;
  matchRate: number;
  exceptionRate: number;
  exceptionBreakdown: ExceptionBreakdown;
}

export interface BankTransactionDetail {
  amount: number;
  status: string;
  date: string;
}

export interface AiExplanation {
  summary: string;
  reasoning: string;
  recommendedAction: string;
}

export interface ReconciliationResult {
  paymentId: string;
  orderId: string;
  orderAmount: number;
  orderStatus: string;
  paymentAmount: number;
  paymentStatus: string;
  paymentDate: string;
  settlementPresent: boolean;
  settlementGrossAmount: number;
  settlementFee: number;
  settlementNetAmount: number;
  settlementStatus: string;
  settlementDate: string;
  bankTransactionCount: number;
  bankTransactionAmount: number;
  bankTransactionStatus: string;
  bankTransactionDate: string;
  bankTransactions: BankTransactionDetail[];
  overallStatus: ReconciliationStatus;
  exceptionType: ExceptionType;
  expectedResult: string | null;
  explanation?: string | null; // From backend's deterministic layer if present
  aiExplanation?: AiExplanation | null; // Populated asynchronously on frontend
  confidenceScore: number;
}

export interface AiExplanationResponse {
  paymentId: string;
  overallStatus: ReconciliationStatus;
  exceptionType: ExceptionType;
  explanation: AiExplanation;
}
