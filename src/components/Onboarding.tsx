import React, { useState } from 'react';
import { Container, Typography, Box, Button, Switch, Divider, Popover, List, ListItem, ListItemText, ListItemIcon } from '@mui/material';
import { Medication } from '@mui/icons-material';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { DigitalClock } from '@mui/x-date-pickers/DigitalClock';
import dayjs from 'dayjs';
import { MedEvent } from '../types';
import { iconMap } from '../utils/iconMap';

interface OnboardingProps {
    events: MedEvent[];
    onToggleEnabled: (id: string) => void;
    onComplete: () => void;
    onUpdateTime: (id: string, newTime: string) => void;
}

export const Onboarding: React.FC<OnboardingProps> = ({ events, onToggleEnabled, onComplete, onUpdateTime }) => {
    const [anchorEl, setAnchorEl] = useState<HTMLButtonElement | null>(null);
    const [editingEventId, setEditingEventId] = useState<string | null>(null);

    const handleTimeClick = (event: React.MouseEvent<HTMLButtonElement>, eventId: string) => {
        setAnchorEl(event.currentTarget);
        setEditingEventId(eventId);
    };

    const handleClose = () => {
        setAnchorEl(null);
        setEditingEventId(null);
    };

    const handleTimeChange = (newValue: dayjs.Dayjs | null) => {
        if (newValue && editingEventId) {
            onUpdateTime(editingEventId, newValue.format('HH:mm'));
            handleClose();
        }
    };

    const open = Boolean(anchorEl);

    return (
        <LocalizationProvider dateAdapter={AdapterDayjs}>
            <Container maxWidth="sm" sx={{ py: 4, px: 0 }}>
                <Box sx={{ mx: 2, my: 0 }}>

                    <Typography variant="h1" component="h1" gutterBottom align="center">
                        Configuração Inicial
                    </Typography>

                    <Typography variant="body1" sx={{ mb: 4, textAlign: 'center' }}>
                        Selecione quais horários de medicamentos você utiliza diariamente. Clique no horário para alterar.
                    </Typography>
                </Box>

                <List sx={{ mb: 4, bgcolor: 'background.paper', borderRadius: 1 }}>
                    {events.map((event, index) => {
                        const IconComponent = iconMap[event.icon] || Medication;
                        return (
                            <React.Fragment key={event.id}>
                                <ListItem>
                                    <ListItemIcon>
                                        <IconComponent color="primary" fontSize="large" />
                                    </ListItemIcon>
                                    <ListItemText
                                        primary={
                                            <Button
                                                size='small'
                                                variant="outlined"
                                                onClick={(e) => handleTimeClick(e, event.id)}
                                                sx={{
                                                    minWidth: '40px', mr: 2, py: 0, px: 1
                                                }}
                                            >
                                                <Typography variant="body1">{event.time}</Typography>

                                            </Button>
                                        }
                                        sx={{
                                            flexGrow: 0,
                                        }}
                                    />
                                    <ListItemText primary={event.label}
                                        sx={{
                                            alignSelf: "start",

                                        }} />
                                    <Switch
                                        edge="end"
                                        checked={event.enabled}
                                        onChange={() => onToggleEnabled(event.id)}
                                        inputProps={{ 'aria-label': `Habilitar ${event.label}` }}
                                    />
                                </ListItem>
                                {index < events.length - 1 && <Divider variant="middle" component="li" sx={{
                                    'border-bottom-width': 'medium'
                                }} />}
                            </React.Fragment>
                        );
                    })}
                </List>

                <Popover
                    open={open}
                    anchorEl={anchorEl}
                    onClose={handleClose}
                    anchorOrigin={{
                        vertical: 'bottom',
                        horizontal: 'left',
                    }}
                >
                    <DigitalClock
                        timeStep={30}
                        onChange={handleTimeChange}
                    />
                </Popover>

                <Button
                    variant="contained"
                    color="primary"

                    size="large"
                    onClick={onComplete}
                    sx={{ m: '0 auto', display: 'block' }}
                >
                    Concluir e Começar
                </Button>
            </Container>
        </LocalizationProvider>
    );
};
