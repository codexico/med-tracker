import React, { useEffect, useState, useRef } from 'react';
import { View, Text, StyleSheet, ScrollView, Platform, Pressable, Modal, TextInput, Switch } from 'react-native';
import { Colors } from '@/constants/theme';
import { MedEvent } from '@/types';
// createInitialEvents used internally by Database service now
import { saveEvent, saveSetting, loadOrInitializeEvents } from '@/services/Database';
import DateTimePicker from '@react-native-community/datetimepicker';
import { Ionicons } from '@expo/vector-icons';
import { scheduleEventNotification, cancelNotification, requestPermissions } from '@/services/Notifications';
import { getClockIconName, DEFAULT_ICON_NAMES, getIcon } from '@/constants/ClockIcons';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import i18n from '@/i18n';

// Helper to get next half hour (e.g. 10:12 -> 10:30, 10:45 -> 11:00)
const getNextHalfHour = () => {
    const date = new Date();
    const minutes = date.getMinutes();
    if (minutes < 30) {
        date.setMinutes(30, 0, 0);
    } else {
        date.setHours(date.getHours() + 1, 0, 0, 0);
    }
    return date;
};

export default function OnboardingScreen() {
    const insets = useSafeAreaInsets();
    const theme = Colors.light;
    const [events, setEvents] = useState<MedEvent[]>([]);
    const [showPicker, setShowPicker] = useState<string | null>(null);

    // Modal state for adding medication
    const [modalVisible, setModalVisible] = useState(false);
    const [currentEventId, setCurrentEventId] = useState<string | null>(null);
    const [newMedication, setNewMedication] = useState('');

    // Modal state for creating new event
    const [createModalVisible, setCreateModalVisible] = useState(false);
    const [newEventLabel, setNewEventLabel] = useState('');
    const [newEventTime, setNewEventTime] = useState(getNextHalfHour());
    const [showCreateTimePicker, setShowCreateTimePicker] = useState(false);

    // Validation
    const [labelError, setLabelError] = useState(false);
    const labelInputRef = useRef<TextInput>(null);

    // Load on mount
    useEffect(() => {
        const load = async () => {
            await requestPermissions();
            // Try getting existing events (or initializing if empty) in a safe singleton way
            const loadedEvents = await loadOrInitializeEvents();
            setEvents(loadedEvents);

            // Mark onboarding as done immediately to ensure next launch goes to Dashboard
            saveSetting('hasCompletedOnboarding', 'true');
        };
        load();
    }, []);

    const updateEvent = async (targetEvent: MedEvent) => {
        const oldEvent = events.find(e => e.id === targetEvent.id);
        let finalEvent = { ...targetEvent };

        // Logic to determine if we need to reschedule notifications
        const timeChanged = !oldEvent || oldEvent.time !== finalEvent.time;
        const enabledChanged = !oldEvent || oldEvent.enabled !== finalEvent.enabled;
        const medsChanged = !oldEvent || JSON.stringify(oldEvent.medications) !== JSON.stringify(finalEvent.medications);
        const labelChanged = !oldEvent || oldEvent.label !== finalEvent.label;

        // If any relevant property changed, we refresh the notification
        if (timeChanged || enabledChanged || medsChanged || labelChanged) {
            // Always try to cancel the old one if it exists
            if (oldEvent?.notificationId) {
                await cancelNotification(oldEvent.notificationId);
                // Clear ID from temp object just in case
                finalEvent.notificationId = undefined;
            }

            // If the event should be active, schedule a new one
            if (finalEvent.enabled) {
                const newId = await scheduleEventNotification(finalEvent);
                if (newId) {
                    finalEvent.notificationId = newId;
                }
            }
        } else {
            // Keep existing notification ID if no changes
            finalEvent.notificationId = oldEvent?.notificationId;
        }

        // Optimistic Update
        setEvents(prev => {
            const updated = prev.map(e => e.id === finalEvent.id ? finalEvent : e);
            return updated.sort((a, b) => a.time.localeCompare(b.time));
        });
        console.log('Updated event:', finalEvent.label);
        // Save to DB
        await saveEvent(finalEvent);
    };

    const handleTimeChange = (event: any, selectedDate?: Date, id?: string) => {
        if (Platform.OS === 'android') {
            setShowPicker(null);
        }

        if (selectedDate && id) {
            const hours = selectedDate.getHours().toString().padStart(2, '0');
            const minutes = selectedDate.getMinutes().toString().padStart(2, '0');
            const timeString = `${hours}:${minutes}`;

            const currentEvent = events.find(e => e.id === id);
            if (currentEvent) {
                // If the current icon is NOT in the default list, update it to match the time
                let newIcon = currentEvent.icon;
                if (!DEFAULT_ICON_NAMES.includes(currentEvent.icon)) {
                    newIcon = getClockIconName(timeString);
                }
                updateEvent({ ...currentEvent, time: timeString, icon: newIcon });
            }
        }
    };

    const toggleEvent = (id: string) => {
        const currentEvent = events.find(e => e.id === id);
        if (currentEvent) {
            updateEvent({ ...currentEvent, enabled: !currentEvent.enabled });
        }
    };

    const openAddMedicationModal = (eventId: string) => {
        setCurrentEventId(eventId);
        setNewMedication('');
        setModalVisible(true);
    };

    const handleAddMedication = () => {
        if (currentEventId && newMedication.trim()) {
            const currentEvent = events.find(e => e.id === currentEventId);
            if (currentEvent) {
                const updatedMeds = [...(currentEvent.medications || []), newMedication.trim()];
                updateEvent({ ...currentEvent, medications: updatedMeds });
            }
            setModalVisible(false);
        }
    };

    const removeMedication = (eventId: string, index: number) => {
        const currentEvent = events.find(e => e.id === eventId);
        if (currentEvent && currentEvent.medications) {
            const updatedMeds = currentEvent.medications.filter((_, i) => i !== index);
            updateEvent({ ...currentEvent, medications: updatedMeds });
        }
    };

    const handleCreateEvent = async () => {
        if (!newEventLabel.trim()) {
            setLabelError(true);
            labelInputRef.current?.focus();
            return;
        }

        const hours = newEventTime.getHours().toString().padStart(2, '0');
        const minutes = newEventTime.getMinutes().toString().padStart(2, '0');
        const timeString = `${hours}:${minutes}`;

        // Generate a text ID based on timestamp
        const newId = `event-${Date.now()}`;

        let newEvent: MedEvent = {
            id: newId,
            label: newEventLabel.trim(),
            time: timeString,
            icon: getClockIconName(timeString), // Default icon based on time
            enabled: true,
            medications: [],
            completedToday: false
        };

        // Schedule Notification
        const notifId = await scheduleEventNotification(newEvent);
        if (notifId) {
            newEvent.notificationId = notifId;
        }

        // Update State & DB
        setEvents(prev => {
            const updated = [...prev, newEvent];
            return updated.sort((a, b) => a.time.localeCompare(b.time));
        });
        console.log('New event created:', newEvent.label);
        await saveEvent(newEvent);

        setCreateModalVisible(false);
        setNewEventLabel('');
        setNewEventTime(getNextHalfHour());
        setLabelError(false);
    };

    return (
        <View style={[styles.container, { backgroundColor: theme.background, paddingTop: insets.top + 20 }]}>
            <View style={styles.header}>
                <Text style={[styles.title, { color: theme.text }]}>{i18n.t('config')}</Text>
                <Text style={[styles.subtitle, { color: theme.textSecondary }]}>
                    {i18n.t('configSubtitle')}
                </Text>
            </View>

            <ScrollView style={styles.content}>
                {events.map(event => (
                    <View key={event.id} style={[styles.card, { backgroundColor: event.enabled ? theme.surface : theme.off }]}>
                        <View style={styles.cardHeader}>

                            <View style={[styles.iconContainer, { backgroundColor: theme.background }]}>
                                {getIcon(event.icon, theme.primary, 20)}
                            </View>

                            <View style={{ flex: 1 }}>
                                <Text style={[styles.cardTitle, { color: theme.text }]}>{event.label}</Text>
                                <Text style={[styles.cardTimeDisplay, { color: theme.textSecondary }]}>{event.time}</Text>
                            </View>

                            <Pressable
                                style={[styles.timeButton, { backgroundColor: theme.background }]}
                                onPress={() => setShowPicker(event.id)}
                            >
                                <Text style={[styles.timeText, { color: theme.text }]}>{i18n.t('editTime')}</Text>
                            </Pressable>
                            <Switch
                                style={styles.switch}
                                value={event.enabled}
                                onValueChange={() => toggleEvent(event.id)}
                                trackColor={{ false: theme.backgroundOff, true: theme.primary }}
                                thumbColor={event.enabled ? '#fff' : theme.off}
                            />
                        </View>

                        <View style={styles.medicationSection}>
                            <View style={styles.medicationList}>
                                {event.medications?.map((med, idx) => (
                                    <View key={idx} style={[styles.chip, { backgroundColor: theme.background, borderColor: theme.icon }]}>
                                        <Text style={{ color: theme.text, marginRight: 6 }}>{med}</Text>
                                        <Pressable onPress={() => removeMedication(event.id, idx)}>
                                            <Ionicons name="close-circle" size={16} color={theme.textSecondary} />
                                        </Pressable>
                                    </View>
                                ))}
                            </View>
                            <Pressable
                                style={[styles.addMedButton, { borderColor: theme.primary }]}
                                onPress={() => openAddMedicationModal(event.id)}
                            >
                                <View style={{ flexDirection: 'row', alignItems: 'center' }}>
                                    <Ionicons name="add-circle-outline" size={24} color={theme.primary} />
                                    <Text style={{ marginLeft: 8, color: theme.primary, fontWeight: '600' }}>{i18n.t('addMedication')}</Text>
                                </View>
                            </Pressable>
                        </View>


                        {showPicker === event.id && (
                            <DateTimePicker
                                value={new Date(new Date().setHours(parseInt(event.time.split(':')[0]), parseInt(event.time.split(':')[1])))}
                                mode="time"
                                display="spinner"
                                minuteInterval={30}
                                onChange={(e, date) => handleTimeChange(e, date, event.id)}
                            />
                        )}
                    </View>
                ))}
                <View style={{ paddingHorizontal: 24, paddingBottom: 4, paddingTop: 8 }}>
                    <Pressable
                        style={[styles.button, { backgroundColor: theme.primary, width: '100%' }]}
                        onPress={() => {
                            setNewEventTime(getNextHalfHour());
                            setCreateModalVisible(true);
                            setLabelError(false);
                        }}
                    >
                        <Text style={styles.textStyle}>{i18n.t('addNewTime')}</Text>
                    </Pressable>
                </View>
            </ScrollView>


            {/* Modal for adding medication */}
            <Modal
                animationType="slide"
                transparent={true}
                visible={modalVisible}
                onRequestClose={() => setModalVisible(false)}
            >
                <View style={[styles.modalOverlay, { backgroundColor: 'rgba(0,0,0,0.5)' }]}>
                    <View style={[styles.modalView, { backgroundColor: theme.surface }]}>
                        <Text style={styles.modalTitle}>{i18n.t('newMedication')}</Text>
                        <TextInput
                            style={[styles.input, { borderColor: theme.icon, backgroundColor: theme.background, color: theme.text }]}
                            placeholder={i18n.t('medNamePlaceholder')}
                            placeholderTextColor={theme.textSecondary}
                            value={newMedication}
                            onChangeText={setNewMedication}
                            autoFocus
                        />
                        <View style={styles.modalButtons}>
                            <Pressable style={[styles.button, styles.buttonClose]}
                                onPress={() => setModalVisible(false)}>
                                <Text style={[styles.textStyle, { color: theme.textSecondary }]}>{i18n.t('cancel')}</Text>
                            </Pressable>
                            <Pressable style={[styles.button, { backgroundColor: theme.primary }]} onPress={handleAddMedication}>
                                <Text style={styles.textStyle}>{i18n.t('add')}</Text>
                            </Pressable>
                        </View>
                    </View>
                </View>
            </Modal>

            {/* Modal for creating new event */}
            <Modal
                animationType="slide"
                transparent={true}
                visible={createModalVisible}
                onRequestClose={() => setCreateModalVisible(false)}
            >
                <View style={[styles.modalOverlay, { backgroundColor: 'rgba(0,0,0,0.5)' }]}>
                    <View style={[styles.modalView, { backgroundColor: theme.surface }]}>
                        <Text style={styles.modalTitle}>{i18n.t('newTime')}</Text>

                        <Text style={{ color: theme.textSecondary, alignSelf: 'flex-start', marginLeft: 12, marginBottom: 4 }}>{i18n.t('timeNameLabel')}</Text>
                        <TextInput
                            ref={labelInputRef}
                            style={[
                                styles.input,
                                {
                                    color: theme.text,
                                    borderColor: labelError ? 'red' : theme.icon,
                                    backgroundColor: theme.background,
                                    borderWidth: labelError ? 2 : 1
                                }
                            ]}
                            placeholder={labelError ? i18n.t('nameRequired') : i18n.t('timeNamePlaceholder')}
                            placeholderTextColor={labelError ? 'red' : theme.textSecondary}
                            value={newEventLabel}
                            onChangeText={(text) => {
                                setNewEventLabel(text);
                                if (text.trim()) setLabelError(false);
                            }}
                        />
                        {labelError && (
                            <Text style={{ color: 'red', alignSelf: 'flex-start', marginLeft: 12, marginBottom: 4 }}>{i18n.t('nameRequiredHint')}</Text>
                        )}

                        <Text style={{ color: theme.textSecondary, alignSelf: 'flex-start', marginLeft: 12, marginBottom: 4, marginTop: 10 }}>{i18n.t('timeLabel')}</Text>

                        <View style={[styles.cardHeader, { width: '100%', paddingHorizontal: 12, marginBottom: 20 }]}>
                            <View style={{ flex: 1 }}>
                                <Text style={[styles.cardTitle, { color: theme.text, fontSize: 22 }]}>
                                    {newEventTime.getHours().toString().padStart(2, '0')}:{newEventTime.getMinutes().toString().padStart(2, '0')}
                                </Text>
                            </View>

                            <Pressable
                                style={[styles.timeButton, { backgroundColor: theme.background, borderWidth: 1, borderColor: theme.icon }]}
                                onPress={() => setShowCreateTimePicker(true)}
                            >
                                <Text style={[styles.timeText, { color: theme.text }]}>{i18n.t('editTime')}</Text>
                            </Pressable>
                        </View>

                        {showCreateTimePicker && (
                            <DateTimePicker
                                value={newEventTime}
                                mode="time"
                                display="spinner"
                                minuteInterval={30}
                                onChange={(e, date) => {
                                    if (Platform.OS === 'android') setShowCreateTimePicker(false);
                                    if (date) setNewEventTime(date);
                                }}
                            />
                        )}

                        <View style={styles.modalButtons}>
                            <Pressable
                                style={[styles.button, styles.buttonClose]}
                                onPress={() => setCreateModalVisible(false)}
                            >
                                <Text style={[styles.textStyle, { color: theme.textSecondary }]}>{i18n.t('cancel')}</Text>
                            </Pressable>
                            <Pressable style={[styles.button, { backgroundColor: theme.primary }]} onPress={handleCreateEvent}>
                                <Text style={styles.textStyle}>{i18n.t('create')}</Text>
                            </Pressable>
                        </View>
                    </View>
                </View>
            </Modal>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        // paddingTop handled dynamically
        paddingBottom: 20
    },
    header: {
        paddingHorizontal: 24,
        marginBottom: 20,
    },
    title: {
        fontSize: 32,
        fontWeight: 'bold',
        marginBottom: 8,
    },
    subtitle: {
        fontSize: 20,
    },
    content: {
        paddingHorizontal: 24,
    },
    card: {
        padding: 16,
        borderRadius: 16,
        marginBottom: 12,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.05,
        shadowRadius: 4,
        elevation: 2,
    },
    cardHeader: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        marginBottom: 12,
    },
    iconContainer: {
        padding: 6,
        borderRadius: 12,
        marginRight: 12
    },
    cardTitle: {
        fontSize: 18,
        fontWeight: '600',
    },
    cardTimeDisplay: {
        fontSize: 14,
    },
    timeButton: {
        paddingHorizontal: 12,
        paddingVertical: 6,
        borderRadius: 8
    },
    timeText: {
        fontSize: 14,
        fontWeight: '600',
    },
    switch: {
        marginLeft: 8,
    },
    medicationSection: {
        borderTopWidth: 1,
        borderTopColor: 'rgba(0,0,0,0.05)',
        paddingTop: 12,
    },
    medicationList: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        gap: 8,
        marginBottom: 12,
    },
    chip: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingHorizontal: 10,
        paddingVertical: 6,
        borderRadius: 20,
        borderWidth: 1,
        borderColor: '#ddd',
        justifyContent: 'center',
    },
    addMedButton: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        paddingVertical: 8,
        borderRadius: 8,
        borderWidth: 1,
        borderStyle: 'dashed',
    },
    modalOverlay: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
    modalView: {
        margin: 20,
        borderRadius: 20,
        padding: 35,
        alignItems: 'center',
        shadowColor: '#000',
        shadowOffset: {
            width: 0,
            height: 2,
        },
        shadowOpacity: 0.25,
        shadowRadius: 4,
        elevation: 5,
        width: '80%',
    },
    modalTitle: {
        marginBottom: 15,
        textAlign: 'center',
        fontSize: 18,
        fontWeight: 'bold',
    },
    input: {
        height: 40,
        width: '100%',
        margin: 12,
        borderWidth: 1,
        borderRadius: 8,
        padding: 10,
    },
    modalButtons: {
        flexDirection: 'row',
        gap: 12,
        width: '100%',
        justifyContent: 'center',
        marginTop: 10,
    },
    button: {
        borderRadius: 10,
        padding: 10,
        elevation: 2,
        minWidth: 100,
        alignItems: 'center',
    },
    buttonClose: {
        backgroundColor: 'white',
        borderWidth: 1,
        borderColor: '#6d5d4b',
    },
    textStyle: {
        color: 'white',
        fontWeight: 'bold',
        textAlign: 'center',
    },
});
