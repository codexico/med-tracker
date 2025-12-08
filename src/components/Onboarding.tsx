import React, { useState } from 'react';
import { Container, Typography, Box, Button, Switch, Paper, Divider, Popover, IconButton, Dialog, DialogTitle, DialogContent, DialogActions, TextField } from '@mui/material';
import { Medication } from '@mui/icons-material';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import 'dayjs/locale/pt-br';
import { DigitalClock } from '@mui/x-date-pickers/DigitalClock';
import dayjs, { Dayjs } from 'dayjs';
import { MedEvent } from '../types';
import { iconMap } from '../utils/iconMap';

interface OnboardingProps {
    events: MedEvent[];
    onToggleEnabled: (id: string) => void;
    onComplete: () => void;
    onUpdateTime: (id: string, newTime: string) => void;
    onAddMedication: (id: string, medication: string) => void;
}

export const Onboarding: React.FC<OnboardingProps> = ({ events, onToggleEnabled, onComplete, onUpdateTime, onAddMedication }) => {
    dayjs.locale('pt-br')
    const [anchorEl, setAnchorEl] = useState<HTMLButtonElement | null>(null);
    const [editingEventId, setEditingEventId] = useState<string | null>(null);
    const [editingValue, setEditingValue] = useState<Dayjs>(dayjs());
    const [openDialog, setOpenDialog] = useState(false);
    const [currentEventId, setCurrentEventId] = useState<string | null>(null);
    const [medicationName, setMedicationName] = useState('');

    const handleOpenDialog = (eventId: string) => {
        setCurrentEventId(eventId);
        setMedicationName('');
        setOpenDialog(true);
    };

    const handleCloseDialog = () => {
        setOpenDialog(false);
        setCurrentEventId(null);
        setMedicationName('');
    };

    const handleSaveMedication = () => {
        if (currentEventId && medicationName.trim()) {
            onAddMedication(currentEventId, medicationName.trim());
            handleCloseDialog();
        }
    };

    function handleTimeClick(event: React.MouseEvent<HTMLButtonElement>, eventId: string, eventTime: string) {
        setAnchorEl(event.currentTarget);
        setEditingEventId(eventId);
        setEditingValue(dayjs(eventTime, 'HH:mm'));
    }

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
        <LocalizationProvider dateAdapter={AdapterDayjs} adapterLocale='pt-br'>
            <Container maxWidth="sm" sx={{ py: 4, px: 0 }}>
                <Box sx={{ mx: 2, my: 0 }}>

                    <Typography variant="h1" component="h1" gutterBottom align="center">
                        Configuração Inicial
                    </Typography>

                    <Typography variant="body1" sx={{ mb: 4, textAlign: 'center' }}>
                        Selecione quais horários de medicamentos você utiliza diariamente. Clique no horário para alterar.
                    </Typography>
                </Box>

                <Paper elevation={3} sx={{ p: 2, mb: 4, mx: 0 }}>
                    {events.map((event, index) => {
                        const IconComponent = iconMap[event.icon] || Medication;
                        return (
                            <React.Fragment key={event.id}>
                                <Box sx={{ py: 2, px: 0, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                    <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                        <Box sx={{ mr: 2, display: 'flex', alignItems: 'center', color: 'primary.main' }}>
                                            <IconComponent fontSize="large" />
                                        </Box>
                                        <Box>
                                            <Button
                                                variant="contained"
                                                size="small"
                                                onClick={(e) => handleTimeClick(e, event.id, event.time)}
                                                sx={{
                                                    minWidth: '80px', mr: 2, py: 0, px: 1
                                                }}
                                            >
                                                <Typography variant="body1">{event.time}</Typography>
                                            </Button>
                                        </Box>
                                        <Box>
                                            <Box>
                                                <Typography variant="body1">{event.label}</Typography>
                                            </Box>
                                            <Box>
                                                {/* aqui vai a lista de remédios */}
                                                <Typography variant="body1">
                                                    {event.medications && event.medications.length > 0
                                                        ? event.medications.join(', ')
                                                        : ''}
                                                </Typography>
                                            </Box>
                                        </Box>
                                    </Box>
                                    <Box sx={{ py: 2, px: 0, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                        <IconButton
                                            color="primary"
                                            aria-label="adicionar medicamento"
                                            onClick={() => handleOpenDialog(event.id)}
                                        >
                                            <Medication fontSize="large" />
                                        </IconButton>

                                        <Switch
                                            checked={event.enabled}
                                            onChange={() => onToggleEnabled(event.id)}
                                            inputProps={{ 'aria-label': `Habilitar ${event.label}` }}
                                        />
                                    </Box>
                                </Box>
                                {index < events.length - 1 && <Divider />}
                            </React.Fragment>
                        );
                    })}
                </Paper>

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
                        value={editingValue}
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

                <Dialog open={openDialog} onClose={handleCloseDialog}>
                    <DialogTitle>Adicionar Medicamento</DialogTitle>
                    <DialogContent>
                        <TextField
                            autoFocus
                            margin="dense"
                            label="Nome do Medicamento"
                            fullWidth
                            variant="standard"
                            value={medicationName}
                            onChange={(e) => setMedicationName(e.target.value)}
                        />
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={handleCloseDialog}>Cancelar</Button>
                        <Button onClick={handleSaveMedication}>Adicionar</Button>
                    </DialogActions>
                </Dialog>
            </Container>
        </LocalizationProvider>
    );
};
