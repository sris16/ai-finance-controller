import React, { useState, useEffect, useCallback } from 'react';
import { IconButton, Tooltip } from '@mui/material';
import { Volume2, Square } from 'lucide-react';

interface AudioReadButtonProps {
  text: string;
}

export const AudioReadButton: React.FC<AudioReadButtonProps> = ({ text }) => {
  const [isPlaying, setIsPlaying] = useState(false);
  const [isSupported, setIsSupported] = useState(true);

  useEffect(() => {
    if (typeof window === 'undefined' || !('speechSynthesis' in window)) {
      setIsSupported(false);
    }
    
    return () => {
      // We only want to cancel on unmount if this component is currently playing
      // to avoid interrupting other speech when navigating away from a non-playing component.
      // But standard cancel() cancels globally. Let's just cancel globally on unmount
      // if this instance was playing.
      if (isPlaying) {
        window.speechSynthesis.cancel();
      }
    };
  }, [isPlaying]);

  const togglePlay = useCallback(() => {
    if (!isSupported) return;

    if (isPlaying) {
      window.speechSynthesis.cancel();
      setIsPlaying(false);
    } else {
      window.speechSynthesis.cancel(); // Stop anything else

      const utterance = new SpeechSynthesisUtterance(text);
      
      // Try to find a good English voice
      const voices = window.speechSynthesis.getVoices();
      const enVoice = voices.find(v => v.lang.startsWith('en-US') || v.lang.startsWith('en-GB') || v.lang.startsWith('en'));
      if (enVoice) {
        utterance.voice = enVoice;
      }

      utterance.onend = () => setIsPlaying(false);
      utterance.onerror = () => setIsPlaying(false);
      
      window.speechSynthesis.speak(utterance);
      setIsPlaying(true);
    }
  }, [isPlaying, isSupported, text]);

  // Handle edge case where another audio button starts playing
  // We can listen to window blur or just accept that local state might get out of sync if multiple buttons exist,
  // but since we only have a few, it's ok. When they click the other, this one's onend will fire.

  if (!isSupported || !text) {
    return null;
  }

  return (
    <Tooltip title={isPlaying ? "Stop reading" : "Read explanation aloud"}>
      <IconButton
        size="small"
        onClick={togglePlay}
        color={isPlaying ? "primary" : "default"}
        sx={{
          ml: 1,
          opacity: isPlaying ? 1 : 0.6,
          '&:hover': { opacity: 1, bgcolor: 'action.hover' },
          transition: 'all 0.2s',
          ...(isPlaying && {
            animation: 'pulse-audio 2s infinite',
            '@keyframes pulse-audio': {
              '0%': { boxShadow: '0 0 0 0 rgba(14, 165, 233, 0.4)' },
              '70%': { boxShadow: '0 0 0 6px rgba(14, 165, 233, 0)' },
              '100%': { boxShadow: '0 0 0 0 rgba(14, 165, 233, 0)' },
            }
          })
        }}
        aria-label={isPlaying ? "Stop reading" : "Read explanation aloud"}
      >
        {isPlaying ? <Square size={14} fill="currentColor" /> : <Volume2 size={16} />}
      </IconButton>
    </Tooltip>
  );
};
