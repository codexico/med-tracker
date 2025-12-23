import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, ScrollView, Platform, Pressable, Switch } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import DateTimePicker from '@react-native-community/datetimepicker';

import { router } from 'expo-router';
import { getClockIconName, DEFAULT_ICON_NAMES, getIcon } from '@/constants/ClockIcons';
import { COLORS } from '@/constants/theme';
import { commonStyles } from '@/constants/commonStyles';
import { MedEvent } from '@/types';

import { saveEvent, saveSetting, getSetting, loadOrInitializeEvents } from '@/services/Database';
import { scheduleEventNotification, cancelNotification, requestPermissions } from '@/services/Notifications';
import i18n from '@/i18n';

import { AddEventModal } from '@/components/AddEventModal';
import { AddMedicationModal } from '@/components/AddMedicationModal';
import { MedicationList } from '@/components/MedicationList';


export default function OnboardingScreen() {
    const insets = useSafeAreaInsets();
    const [events, setEvents] = useState<MedEvent[]>([]);
    const [showPicker, setShowPicker] = useState<string | null>(null);

    // Modal state for adding medication
    const [modalVisible, setModalVisible] = useState(false);
    const [currentEventId, setCurrentEventId] = useState<string | null>(null);

    // Modal state for creating new event
    const [createModalVisible, setCreateModalVisible] = useState(false);
    const [isFirstLaunch, setIsFirstLaunch] = useState(false);

    // Load on mount
    useEffect(() => {
        const load = async () => {
            // Check if this is the first launch (onboarding flow)
            const onboarded = await getSetting('hasCompletedOnboarding');
            if (onboarded !== 'true') {
                setIsFirstLaunch(true);
            }

            // Try getting existing events (or initializing if empty) in a safe singleton way
            const loadedEvents = await loadOrInitializeEvents();
            setEvents(loadedEvents);
        };
        load();
    }, []);

    const handleFinishOnboarding = async () => {
        // Request permissions only when finishing
        await requestPermissions();

        // Mark as done
        await saveSetting('hasCompletedOnboarding', 'true');
        setIsFirstLaunch(false);

        // Navigate to dashboard
        router.replace('/(tabs)/index' as any);
    };

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

    const handleTimeChange = (selectedDate?: Date, id?: string) => {
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
        setModalVisible(true);
    };

    const handleAddMedication = (medName: string) => {
        if (currentEventId && medName) {
            const currentEvent = events.find(e => e.id === currentEventId);
            if (currentEvent) {
                const updatedMeds = [...(currentEvent.medications || []), medName];
                updateEvent({ ...currentEvent, medications: updatedMeds });
            }
        }
    };

    const removeMedication = (eventId: string, index: number) => {
        const currentEvent = events.find(e => e.id === eventId);
        if (currentEvent && currentEvent.medications) {
            const updatedMeds = currentEvent.medications.filter((_, i) => i !== index);
            updateEvent({ ...currentEvent, medications: updatedMeds });
        }
    };

    const handleCreateEvent = async (label: string, time: Date) => {
        const hours = time.getHours().toString().padStart(2, '0');
        const minutes = time.getMinutes().toString().padStart(2, '0');
        const timeString = `${hours}:${minutes}`;

        // Generate a text ID based on timestamp
        const newId = `event-${Date.now()}`;

        let newEvent: MedEvent = {
            id: newId,
            label: label,
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
    };

    return (
        <View style={[commonStyles.container, { backgroundColor: COLORS.background, paddingTop: insets.top + 20 }]}>
            <View style={commonStyles.header}>
                <Text style={[commonStyles.title, { color: COLORS.text }]}>{i18n.t('config')}</Text>
                <Text style={[styles.subtitle, { color: COLORS.textSecondary }]}>
                    {i18n.t('configSubtitle')}
                </Text>
            </View>

            <ScrollView style={commonStyles.content}>
                {events.map(event => (
                    <View key={event.id} style={[commonStyles.card, { backgroundColor: event.enabled ? COLORS.surface : COLORS.off }]}>
                        <View style={commonStyles.cardHeader}>

                            <View style={[styles.iconContainer, { backgroundColor: COLORS.background }]}>
                                {getIcon(event.icon, COLORS.primary, 20)}
                            </View>

                            <View style={styles.eventInfo}>
                                <Text style={[styles.cardTitle, { color: COLORS.text }]}>{event.label}</Text>
                                <Text style={[styles.cardTimeDisplay, { color: COLORS.textSecondary }]}>{event.time}</Text>
                            </View>

                            <Pressable
                                style={[styles.timeButton, { backgroundColor: COLORS.background }]}
                                onPress={() => setShowPicker(event.id)}
                            >
                                <Text style={[styles.timeText, { color: COLORS.text }]}>{i18n.t('editTime')}</Text>
                            </Pressable>
                            <Switch
                                style={styles.switch}
                                value={event.enabled}
                                onValueChange={() => toggleEvent(event.id)}
                                trackColor={{ false: COLORS.backgroundOff, true: COLORS.primary }}
                                thumbColor={event.enabled ? COLORS.white : COLORS.off}
                            />
                        </View>

                        <MedicationList
                            medications={event.medications || []}
                            onRemove={(idx) => removeMedication(event.id, idx)}
                            onAdd={() => openAddMedicationModal(event.id)}
                        />


                        {showPicker === event.id && (
                            <DateTimePicker
                                value={new Date(new Date().setHours(parseInt(event.time.split(':')[0]), parseInt(event.time.split(':')[1])))}
                                mode="time"
                                display="spinner"
                                minuteInterval={30}
                                onChange={(e, date) => handleTimeChange(date, event.id)}
                            />
                        )}
                    </View>
                ))}
                <View style={styles.footerButtonContainer}>
                    <Pressable
                        style={[commonStyles.button, { backgroundColor: COLORS.primary, width: '100%', marginBottom: 12 }]}
                        onPress={() => {
                            setCreateModalVisible(true);
                        }}
                    >
                        <Text style={commonStyles.textStyle}>{i18n.t('addNewTime')}</Text>
                    </Pressable>

                    {isFirstLaunch && (
                        <Pressable
                            style={[commonStyles.button, { backgroundColor: COLORS.secondary, width: '100%', marginBottom: 24 }]}
                            onPress={handleFinishOnboarding}
                        >
                            <Text style={commonStyles.textStyle}>{i18n.t('finishOnboarding')}</Text>
                        </Pressable>
                    )}
                </View>
            </ScrollView>


            {/* Modal for adding medication */}
            <AddMedicationModal
                visible={modalVisible}
                onClose={() => setModalVisible(false)}
                onAdd={handleAddMedication}
            />

            {/* Modal for creating new event */}
            <AddEventModal
                visible={createModalVisible}
                onClose={() => setCreateModalVisible(false)}
                onAdd={handleCreateEvent}
            />
        </View>
    );
}

const styles = StyleSheet.create({
    subtitle: {
        fontSize: 20,
    },
    iconContainer: {
        padding: 6,
        borderRadius: 12,
        marginRight: 12
    },
    addMedButtonText: {
        marginLeft: 8,
        color: COLORS.primary,
        fontWeight: '600',
    },
    eventInfo: {
        flex: 1,
    },
    addMedButtonContent: {
        flexDirection: 'row',
        alignItems: 'center',
    },
    footerButtonContainer: {
        paddingHorizontal: 24,
        paddingBottom: 4,
        paddingTop: 8,
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
        marginLeft: 4,
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


});
