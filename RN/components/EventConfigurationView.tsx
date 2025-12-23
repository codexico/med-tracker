import React, { useState } from 'react';
import { View, Text, StyleSheet, ScrollView, Platform, Pressable, Switch } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import DateTimePicker from '@react-native-community/datetimepicker';

import { COLORS } from '@/constants/theme';
import { commonStyles } from '@/constants/commonStyles';
import { getIcon } from '@/constants/ClockIcons';
import i18n from '@/i18n';

import { AddEventModal } from '@/components/AddEventModal';
import { AddMedicationModal } from '@/components/AddMedicationModal';
import { MedicationList } from '@/components/MedicationList';
import { useEventManagement } from '@/hooks/useEventManagement';


interface EventConfigurationViewProps {
    title: string;
    subtitle: string | React.ReactNode;
    footerContent?: React.ReactNode;
}

export const EventConfigurationView: React.FC<EventConfigurationViewProps> = ({
    title,
    subtitle,
    footerContent
}) => {
    const insets = useSafeAreaInsets();
    const {
        events,
        updateEventTime,
        toggleEvent,
        addMedication,
        removeMedication,
        createEvent
    } = useEventManagement();

    const [showPicker, setShowPicker] = useState<string | null>(null);
    const [modalVisible, setModalVisible] = useState(false);
    const [currentEventId, setCurrentEventId] = useState<string | null>(null);
    const [createModalVisible, setCreateModalVisible] = useState(false);

    const handleTimeChange = (selectedDate: Date, id: string) => {
        if (Platform.OS === 'android') {
            setShowPicker(null);
        }
        if (selectedDate) {
            updateEventTime(id, selectedDate);
        }
    };

    const openAddMedicationModal = (eventId: string) => {
        setCurrentEventId(eventId);
        setModalVisible(true);
    };

    const handleAddMedicationSubmit = (medName: string) => {
        if (currentEventId && medName) {
            addMedication(currentEventId, medName);
        }
    };

    return (
        <ScrollView style={[commonStyles.container, { backgroundColor: COLORS.background, paddingTop: insets.top + 20 }]}>
            <View style={commonStyles.content}>
                <View style={commonStyles.header}>
                    <Text style={[commonStyles.title, { color: COLORS.text }]}>{title}</Text>
                    <Text style={[styles.subtitle, { color: COLORS.textSecondary }]}>
                        {subtitle}
                    </Text>
                </View>

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
                                onChange={(e, date?: Date) => date && handleTimeChange(date, event.id)}
                            />
                        )}
                    </View>
                ))}

                <View style={styles.footerButtonContainer}>
                    <Pressable
                        style={[commonStyles.button, { backgroundColor: COLORS.primary, width: '100%', marginBottom: 12 }]}
                        onPress={() => setCreateModalVisible(true)}
                    >
                        <Text style={commonStyles.textStyle}>{i18n.t('addNewTime')}</Text>
                    </Pressable>

                    {footerContent}
                </View>

                <AddMedicationModal
                    visible={modalVisible}
                    onClose={() => setModalVisible(false)}
                    onAdd={handleAddMedicationSubmit}
                />

                <AddEventModal
                    visible={createModalVisible}
                    onClose={() => setCreateModalVisible(false)}
                    onAdd={async (label, time) => {
                        await createEvent(label, time);
                        setCreateModalVisible(false);
                    }}
                />
            </View>
        </ScrollView>
    );
};

const styles = StyleSheet.create({
    subtitle: {
        fontSize: 20,
    },
    iconContainer: {
        padding: 6,
        borderRadius: 12,
        marginRight: 12
    },
    eventInfo: {
        flex: 1,
    },
    footerButtonContainer: {
        paddingHorizontal: 24,
        paddingBottom: 4,
        paddingTop: 8,
        marginBottom: 36,
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
});
