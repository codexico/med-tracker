import React, { useEffect, useState } from 'react';
import { Container, Typography, Box, Alert, Grow, Fade, Paper, Avatar } from '@mui/material';
import { MedEvent } from '../types';
import { EventCard } from './EventCard';
import { CheckCircleOutline, SentimentSatisfiedAlt } from '@mui/icons-material';

interface DashboardProps {
    events: MedEvent[];
    onToggleEvent: (id: string) => void;
    onUndoOnboarding: () => void;
}

export const Dashboard: React.FC<DashboardProps> = ({ events, onToggleEvent, onUndoOnboarding }) => {
    const enabledEvents = events.filter(e => e.enabled);
    const sortedEvents = [...enabledEvents].sort((a, b) => a.time.localeCompare(b.time));

    const allCompleted = enabledEvents.length > 0 && enabledEvents.every(e => e.completedToday);

    const [greeting] = useState(() => {
        const hour = new Date().getHours();
        if (hour < 12) return 'Bom dia';
        else if (hour < 18) return 'Boa tarde';
        return 'Boa noite';
    });

    useEffect(() => {
        // Request notification permission on mount
        if ('Notification' in window && Notification.permission === 'default') {
            Notification.requestPermission();
        }
    }, []);

    return (
        <Container maxWidth="sm" sx={{ py: 4 }}>
            <Fade in={true} timeout={800}>
                <Box sx={{ mb: 4, textAlign: 'center' }}>
                    <Typography variant="h4" component="h1" gutterBottom align="center" color="primary" sx={{ fontWeight: 'bold' }}>
                        {greeting}!
                    </Typography>
                    <Typography variant="body1" color="text.secondary">
                        Aqui estão seus medicamentos de hoje.
                    </Typography>
                </Box>
            </Fade>

            {allCompleted && (
                <Grow in={true}>
                    <Alert icon={<CheckCircleOutline fontSize="inherit" />} severity="success" sx={{ mb: 3, fontSize: '1.1rem', borderRadius: 2 }}>
                        Parabéns! Tudo certo por hoje.
                    </Alert>
                </Grow>
            )}

            <Box>
                {sortedEvents.map((event, index) => (
                    <Grow in={true} timeout={(index + 1) * 300} key={event.id}>
                        <Box>
                            <EventCard
                                event={event}
                                onToggle={onToggleEvent}
                            />
                        </Box>
                    </Grow>
                ))}
            </Box>

            {enabledEvents.length === 0 && (
                <Fade in={true} timeout={1000}>
                    <Paper
                        elevation={0}
                        sx={{
                            p: 4,
                            textAlign: 'center',
                            bgcolor: 'transparent',
                            display: 'flex',
                            flexDirection: 'column',
                            alignItems: 'center'
                        }}
                    >
                        <Avatar sx={{ width: 60, height: 60, bgcolor: 'secondary.light', mb: 2 }}>
                            <SentimentSatisfiedAlt sx={{ color: 'white', fontSize: 40 }} />
                        </Avatar>
                        <Typography variant="h6" color="text.secondary" gutterBottom>
                            Nenhum medicamento configurado
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                            Vá para as configurações se precisar adicionar algo.
                        </Typography>
                    </Paper>
                </Fade>
            )}

            <Box sx={{ mt: 4, display: 'flex', justifyContent: 'center' }}>
                <button
                    onClick={onUndoOnboarding}
                    style={{
                        background: 'none',
                        border: 'none',
                        color: 'rgba(0, 0, 0, 0.4)',
                        textDecoration: 'underline',
                        cursor: 'pointer',
                        fontSize: '0.85rem',
                        transition: 'color 0.2s'
                    }}
                    onMouseEnter={(e) => e.currentTarget.style.color = 'var(--mui-palette-primary-main)'}
                    onMouseLeave={(e) => e.currentTarget.style.color = 'rgba(0, 0, 0, 0.4)'}
                >
                    Refazer configurações
                </button>
            </Box>
        </Container>
    );
};
