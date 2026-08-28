import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { HomePage } from '../pages/HomePage';
import { ResultsPage } from '../pages/ResultsPage';
import { ResultDetailsPage } from '../pages/ResultDetailsPage';
import { MainLayout } from '../layouts/MainLayout';

export const AppRoutes: React.FC = () => {
  return (
    <MainLayout>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/reconciliation" element={<ResultsPage />} />
        <Route path="/reconciliation/:paymentId" element={<ResultDetailsPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </MainLayout>
  );
};
