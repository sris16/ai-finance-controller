import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { WelcomePage } from '../pages/WelcomePage';
import { LoginPage } from '../pages/LoginPage';
import { HomePage } from '../pages/HomePage';
import { ResultsPage } from '../pages/ResultsPage';
import { ResultDetailsPage } from '../pages/ResultDetailsPage';
import { DatasetsPage } from '../pages/DatasetsPage';
import { RunsPage } from '../pages/RunsPage';
import { MainLayout } from '../layouts/MainLayout';
import { ProtectedRoute } from './ProtectedRoute';

export const AppRoutes: React.FC = () => {
  return (
    <Routes>
      {/* Public Routes */}
      <Route path="/" element={<WelcomePage />} />
      <Route path="/login" element={<LoginPage />} />

      {/* Protected Application Routes */}
      <Route element={<ProtectedRoute />}>
        <Route
          path="/dashboard"
          element={
            <MainLayout>
              <HomePage />
            </MainLayout>
          }
        />
        <Route
          path="/reconciliation"
          element={
            <MainLayout>
              <ResultsPage />
            </MainLayout>
          }
        />
        <Route
          path="/reconciliation/:paymentId"
          element={
            <MainLayout>
              <ResultDetailsPage />
            </MainLayout>
          }
        />
        <Route
          path="/datasets"
          element={
            <MainLayout>
              <DatasetsPage />
            </MainLayout>
          }
        />
        <Route
          path="/runs"
          element={
            <MainLayout>
              <RunsPage />
            </MainLayout>
          }
        />
      </Route>

      {/* Catch-all fallback */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};
