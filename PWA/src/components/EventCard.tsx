import React from 'react';
import { Card, CardContent, Typography, Checkbox, FormControlLabel, Box, useTheme, alpha } from '@mui/material';
import { Medication, CheckCircle, RadioButtonUnchecked } from '@mui/icons-material';
import { MedEvent } from '../types';
import { iconMap } from '../utils/iconMap';

interface EventCardProps {
    event: MedEvent;
    onToggle: (id: string) => void;
}

export const EventCard: React.FC<EventCardProps> = ({ event, onToggle }) => {
    const theme = useTheme();
    const IconComponent = iconMap[event.icon] || Medication;

    return (
        <Card
            elevation={event.completedToday ? 0 : 3}
            sx={{
                mb: 2,
                position: 'relative',
                overflow: 'visible',
                bgcolor: event.completedToday
                    ? alpha(theme.palette.success.light, 0.1)
                    : 'background.paper',
                border: event.completedToday
                    ? `1px solid ${theme.palette.success.main}`
                    : '1px solid transparent',
                borderRadius: 3,
                transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                transform: event.completedToday ? 'scale(0.98)' : 'scale(1)',
                opacity: event.completedToday ? 0.8 : 1,
                '&:hover': {
                    transform: event.completedToday ? 'scale(0.98)' : 'translateY(-2px)',
                    boxShadow: event.completedToday ? 0 : theme.shadows[6]
                }
            }}
        >
            <CardContent sx={{ '&:last-child': { pb: 2 }, p: 2 }}>
                <FormControlLabel
                    sx={{ width: '100%', m: 0 }}
                    control={
                        <Checkbox
                            checked={event.completedToday}
                            onChange={() => onToggle(event.id)}
                            icon={<RadioButtonUnchecked sx={{ fontSize: 32, color: 'text.disabled' }} />}
                            checkedIcon={<CheckCircle sx={{ fontSize: 32, color: 'success.main' }} />}
                            sx={{ p: 1 }}
                        />
                    }
                    label={
                        <Box sx={{ ml: 1, display: 'flex', alignItems: 'center', width: '100%' }}>
                            <Box
                                sx={{
                                    mr: 2,
                                    display: 'flex',
                                    alignItems: 'center',
                                    color: event.completedToday ? 'success.main' : 'primary.main',
                                    p: 1,
                                    borderRadius: '50%',
                                    bgcolor: event.completedToday ? 'transparent' : alpha(theme.palette.primary.main, 0.1)
                                }}
                            >
                                <IconComponent fontSize="medium" />
                            </Box>
                            <Box sx={{ flexGrow: 1 }}>
                                <Typography
                                    variant="h6"
                                    component="div"
                                    sx={{
                                        fontWeight: 'bold',
                                        textDecoration: event.completedToday ? 'line-through' : 'none',
                                        color: event.completedToday ? 'text.secondary' : 'text.primary'
                                    }}
                                >
                                    {event.label}
                                </Typography>
                                <Typography variant="body1" color="text.secondary" sx={{ fontWeight: 500 }}>
                                    {event.time}
                                </Typography>
                                {event.medications && event.medications.length > 0 && (
                                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5, mt: 0.5 }}>
                                        {event.medications.map((med, i) => (
                                            <Typography
                                                key={i}
                                                variant="caption"
                                                sx={{
                                                    bgcolor: 'background.default',
                                                    px: 1,
                                                    py: 0.2,
                                                    borderRadius: 1,
                                                    color: 'text.secondary'
                                                }}
                                            >
                                                {med}
                                            </Typography>
                                        ))}
                                    </Box>
                                )}
                            </Box>
                        </Box>
                    }
                />
            </CardContent>
        </Card>
    );
};
