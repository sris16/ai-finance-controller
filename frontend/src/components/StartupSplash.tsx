import React, { useEffect, useState } from 'react';
import { Box, Fade, CircularProgress, useTheme } from '@mui/material';

interface StartupSplashProps {
  onComplete: () => void;
}

export const StartupSplash: React.FC<StartupSplashProps> = ({ onComplete }) => {
  const theme = useTheme();
  const [show, setShow] = useState(true);

  useEffect(() => {
    // Start fade out slightly before 2 seconds to complete within the target time
    const fadeOutTimer = setTimeout(() => {
      setShow(false);
    }, 2600);

    // Call onComplete after the fade transition finishes
    const completeTimer = setTimeout(() => {
      onComplete();
    }, 3000);

    return () => {
      clearTimeout(fadeOutTimer);
      clearTimeout(completeTimer);
    };
  }, [onComplete]);

  return (
    <Fade in={show} timeout={400}>
      <Box
        sx={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          bgcolor: 'background.default',
          zIndex: 9999, // Ensure it sits on top of everything
        }}
      >
        <Box
          component="img"
          src="https://upload.wikimedia.org/wikipedia/commons/8/89/Razorpay_logo.svg"
          alt="Razorpay Logo"
          sx={{
            width: { xs: 150, sm: 200 },
            mb: 4,
            // Add a subtle brightness adjustment for dark mode if needed
            filter: theme.palette.mode === 'dark' ? 'brightness(1.5)' : 'none',
          }}
        />
        <CircularProgress
          size={24}
          thickness={4}
          sx={{
            color: theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.2)' : 'rgba(0,0,0,0.1)',
          }}
        />
      </Box>
    </Fade>
  );
};
