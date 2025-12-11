import React, { useState } from 'react';
import { Container, Typography, Box, Button, Switch, Paper, Divider, Popover, Dialog, DialogTitle, DialogContent, DialogActions, TextField, Chip, Stack, Avatar } from '@mui/material';
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
    onRemoveMedication: (id: string, index: number) => void;
    onAddEvent: (label: string, time: string) => void;
}

export const Onboarding: React.FC<OnboardingProps> = ({ events, onToggleEnabled, onComplete, onUpdateTime, onAddMedication, onRemoveMedication, onAddEvent }) => {
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

    const [isAddEventOpen, setIsAddEventOpen] = useState(false);
    const [newEventLabel, setNewEventLabel] = useState('');
    const [newEventTime, setNewEventTime] = useState<Dayjs | null>(dayjs());

    const handleAddEvent = () => {
        if (newEventLabel && newEventTime) {
            onAddEvent(newEventLabel, newEventTime.format('HH:mm'));
            setIsAddEventOpen(false);
            setNewEventLabel('');
            setNewEventTime(dayjs());
        }
    };

    return (
        <LocalizationProvider dateAdapter={AdapterDayjs} adapterLocale='pt-br'>
            <Container maxWidth="sm" sx={{ py: 4, px: 0 }}>

                <Box sx={{ mb: 4, textAlign: 'center' }}>
                    <Typography variant="h4" component="h1" gutterBottom color="primary" sx={{ fontWeight: 'bold' }}>
                        Meus Remedinhos
                    </Typography>
                    <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
                        Configure sua rotina em 3 passos simples:
                    </Typography>

                    <Stack spacing={2}>
                        <Paper variant="outlined" sx={{ p: 2, display: 'flex', alignItems: 'center', borderColor: 'primary.light' }}>
                            <Avatar sx={{ bgcolor: 'primary.main', mr: 2, width: 32, height: 32, fontSize: '0.9rem' }}>1</Avatar>
                            <Typography variant="body1">Defina os horários da sua rotina</Typography>
                        </Paper>
                        <Paper variant="outlined" sx={{ p: 2, display: 'flex', alignItems: 'center', borderColor: 'primary.light' }}>
                            <Avatar sx={{ bgcolor: 'primary.main', mr: 2, width: 32, height: 32, fontSize: '0.9rem' }}>2</Avatar>
                            <Typography variant="body1">Adicione os remédios de cada horário</Typography>
                        </Paper>
                        <Paper variant="outlined" sx={{ p: 2, display: 'flex', alignItems: 'center', borderColor: 'primary.light' }}>
                            <Avatar sx={{ bgcolor: 'primary.main', mr: 2, width: 32, height: 32, fontSize: '0.9rem' }}>3</Avatar>
                            <Typography variant="body1">Ative as notificações diárias</Typography>
                        </Paper>
                    </Stack>
                </Box>

                <Paper elevation={3} sx={{ px: 2, mb: 4, mx: 0 }}>

                    {events.map((event, index) => {
                        const IconComponent = iconMap[event.icon] || Medication;
                        return (
                            <React.Fragment key={event.id}>
                                <Box sx={{ py: 2 }} >
                                    <Box sx={{ pt: 0, pb: 0, px: 0, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
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
                                                        width: '9ch',
                                                        mr: 2, py: 0, px: 1
                                                    }}
                                                >
                                                    {event.time}
                                                </Button>
                                            </Box>
                                            <Box>

                                                <Button
                                                    variant="contained"
                                                    size="small"
                                                    sx={{
                                                        textTransform: 'none',
                                                        width: '14ch',
                                                        minWidth: '80px', mx: 0, py: 0.2, px: 1
                                                    }}
                                                    onClick={() => handleOpenDialog(event.id)}
                                                >
                                                    <Typography variant="body2">Adicionar medicamento</Typography>
                                                </Button>

                                            </Box>
                                        </Box>
                                        <Box sx={{ py: 0, px: 0, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>

                                            <Switch
                                                checked={event.enabled}
                                                onChange={() => onToggleEnabled(event.id)}
                                                inputProps={{ 'aria-label': `Habilitar ${event.label}` }}
                                            />
                                        </Box>
                                    </Box>

                                    {event.medications &&
                                        <Box >
                                            <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap sx={{ mt: 1 }}>
                                                {event.medications?.map((med, idx) => (
                                                    <Chip
                                                        key={`${event.id}-med-${idx}`}
                                                        label={med}
                                                        onDelete={() => onRemoveMedication(event.id, idx)}
                                                        color="default"
                                                        variant="outlined"
                                                        size="small"
                                                    />
                                                ))}
                                            </Stack>
                                        </Box>
                                    }
                                </Box>
                                {index < events.length - 1 && <Divider />}
                            </React.Fragment>
                        );
                    })}
                </Paper>

                <Box sx={{ display: 'flex', justifyContent: 'center', mb: 4 }}>
                    <Button variant="outlined" onClick={() => setIsAddEventOpen(true)} startIcon={<Medication />}>
                        Adicionar Novo Horário
                    </Button>
                </Box>

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

                <Dialog open={isAddEventOpen} onClose={() => setIsAddEventOpen(false)}>
                    <DialogTitle>Novo Evento</DialogTitle>
                    <DialogContent sx={{ display: 'flex', flexDirection: 'column', gap: 2, pt: 2, minWidth: 300 }}>
                        <TextField
                            autoFocus
                            label="Nome do Evento (ex: Lanche)"
                            fullWidth
                            value={newEventLabel}
                            onChange={(e) => setNewEventLabel(e.target.value)}
                        />
                        <Typography variant="body2" color="text.secondary">Horário</Typography>
                        <Box sx={{ maxHeight: 300, overflow: 'auto' }}>
                            <DigitalClock
                                timeStep={30}
                                value={newEventTime}
                                onChange={(newValue) => setNewEventTime(newValue)}
                                sx={{ width: '100%' }}
                            />
                        </Box>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setIsAddEventOpen(false)}>Cancelar</Button>
                        <Button onClick={handleAddEvent} variant="contained" disabled={!newEventLabel}>
                            Criar
                        </Button>
                    </DialogActions>
                </Dialog>

                <Box sx={{ mt: 4, textAlign: 'center', opacity: 0.5 }}>
                    <Typography variant="caption">
                        v{__APP_VERSION__}
                    </Typography>
                </Box>
            </Container>
        </LocalizationProvider>
    );
};
