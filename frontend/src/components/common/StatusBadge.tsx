import React from 'react';
import { Chip, CircularProgress, Box, Typography } from '@mui/material';

interface StatusBadgeProps {
  label: string;
  isOnline: boolean;
  isLoading: boolean;
  error?: string | null;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({
  label,
  isOnline,
  isLoading,
  error,
}) => {
  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        <CircularProgress size={16} color="primary" />
        <Typography variant="body2" color="text.secondary">
          Checking {label}...
        </Typography>
      </Box>
    );
  }

  if (error || !isOnline) {
    return (
      <Chip
        size="small"
        label={`${label}: Offline`}
        color="error"
        variant="outlined"
        sx={{ fontWeight: 600 }}
      />
    );
  }

  return (
    <Chip
      size="small"
      label={`${label}: Operational`}
      color="success"
      sx={{ fontWeight: 600, backgroundColor: 'rgba(16, 185, 129, 0.15)', color: '#34d399', border: '1px solid rgba(52, 211, 153, 0.3)' }}
    />
  );
};
