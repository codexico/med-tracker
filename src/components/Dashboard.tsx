import React, { useEffect } from 'react';
import { Container, Typography, Box, Alert } from '@mui/material';
import { MedEvent } from '../types';
import { EventCard } from './EventCard';

interface DashboardProps {
    events: MedEvent[];
    onToggleEvent: (id: string) => void;
    onUndoOnboarding: () => void;
}

export const Dashboard: React.FC<DashboardProps> = ({ events, onToggleEvent, onUndoOnboarding }) => {
    const enabledEvents = events.filter(e => e.enabled);
    const sortedEvents = [...enabledEvents].sort((a, b) => a.time.localeCompare(b.time));

    const allCompleted = enabledEvents.length > 0 && enabledEvents.every(e => e.completedToday);

    useEffect(() => {
        // Request notification permission on mount
        if ('Notification' in window && Notification.permission === 'default') {
            Notification.requestPermission();
        }
    }, []);

    return (
        <Container maxWidth="sm" sx={{ py: 4 }}>
            <Typography variant="h4" component="h1" gutterBottom align="center" sx={{ mb: 4 }}>
                Hoje
            </Typography>

            {allCompleted && (
                <Alert severity="success" sx={{ mb: 3, fontSize: '1.1rem' }}>
                    Parabéns! Você completou todos os medicamentos de hoje.
                </Alert>
            )}

            <Box>
                {sortedEvents.map(event => (
                    <EventCard
                        key={event.id}
                        event={event}
                        onToggle={onToggleEvent}
                    />
                ))}
            </Box>

            {enabledEvents.length === 0 && (
                <Typography align="center" color="text.secondary">
                    Nenhum evento configurado.
                </Typography>
            )}

            <Box sx={{ mt: 4, display: 'flex', justifyContent: 'center' }}>
                <button
                    onClick={onUndoOnboarding}
                    style={{
                        background: 'none',
                        border: 'none',
                        color: '#666',
                        textDecoration: 'underline',
                        cursor: 'pointer',
                        fontSize: '0.9rem'
                    }}
                >
                    Voltar ao Início
                </button>
            </Box>
        </Container>
    );
};
