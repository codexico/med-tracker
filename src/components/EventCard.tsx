import React from 'react';
import { Card, CardContent, Typography, Checkbox, FormControlLabel, Box } from '@mui/material';
import { Medication } from '@mui/icons-material';
import { MedEvent } from '../types';
import { iconMap } from '../utils/iconMap';

interface EventCardProps {
    event: MedEvent;
    onToggle: (id: string) => void;
}

export const EventCard: React.FC<EventCardProps> = ({ event, onToggle }) => {
    const IconComponent = iconMap[event.icon] || Medication;

    return (
        <Card
            sx={{
                mb: 2,
                bgcolor: event.completedToday ? 'action.selected' : 'background.paper',
                borderLeft: event.completedToday ? '6px solid green' : '6px solid transparent',
                transition: 'all 0.3s ease'
            }}
        >
            <CardContent sx={{ '&:last-child': { pb: 2 } }}>
                <FormControlLabel
                    sx={{ width: '100%', m: 0 }}
                    control={
                        <Checkbox
                            checked={event.completedToday}
                            onChange={() => onToggle(event.id)}
                            sx={{ '& .MuiSvgIcon-root': { fontSize: 40 } }} // Large checkbox
                        />
                    }
                    label={
                        <Box sx={{ ml: 1, display: 'flex', alignItems: 'center', width: '100%' }}>
                            <Box sx={{ mr: 2, display: 'flex', alignItems: 'center', color: 'primary.main' }}>
                                <IconComponent fontSize="large" />
                            </Box>
                            <Box>
                                <Typography variant="h5" component="div" sx={{ fontWeight: 'bold' }}>
                                    {event.label}
                                </Typography>
                                <Typography variant="h6" color="text.secondary">
                                    {event.time}
                                </Typography>
                                {event.medications && event.medications.length > 0 && (
                                    <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                                        {event.medications.join(', ')}
                                    </Typography>
                                )}
                            </Box>
                        </Box>
                    }
                />
            </CardContent>
        </Card>
    );
};
