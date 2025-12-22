import React, { useState, useRef, useEffect } from 'react';
import { View, Text, Modal, TextInput, Pressable, StyleSheet, Platform } from 'react-native';
import DateTimePicker from '@react-native-community/datetimepicker';

import i18n from '@/i18n';
import { COLORS } from '@/constants/theme';
import { commonStyles } from '@/constants/commonStyles';

import { getNextHalfHour } from '@/utils/date';

interface AddEventModalProps {
    visible: boolean;
    onClose: () => void;
    onAdd: (label: string, time: Date) => void;
}

export const AddEventModal: React.FC<AddEventModalProps> = ({ visible, onClose, onAdd }) => {
    const [newEventLabel, setNewEventLabel] = useState('');
    const [newEventTime, setNewEventTime] = useState(getNextHalfHour());
    const [showCreateTimePicker, setShowCreateTimePicker] = useState(false);
    const [labelError, setLabelError] = useState(false);
    const labelInputRef = useRef<TextInput>(null);

    // Reset state when modal opens
    useEffect(() => {
        if (visible) {
            setNewEventLabel('');
            setNewEventTime(getNextHalfHour());
            setLabelError(false);
        }
    }, [visible]);

    const handleCreateEvent = () => {
        if (!newEventLabel.trim()) {
            setLabelError(true);
            labelInputRef.current?.focus();
            return;
        }
        onAdd(newEventLabel.trim(), newEventTime);
        onClose();
    };

    return (
        <Modal
            animationType="slide"
            transparent={true}
            visible={visible}
            onRequestClose={onClose}
        >
            <View style={commonStyles.modalOverlay}>
                <View style={[commonStyles.modalView, { backgroundColor: COLORS.surface }]}>
                    <Text style={commonStyles.modalTitle}>{i18n.t('newTime')}</Text>

                    <Text style={[styles.label, { color: COLORS.textSecondary }]}>{i18n.t('timeNameLabel')}</Text>
                    <TextInput
                        ref={labelInputRef}
                        style={[
                            commonStyles.input,
                            {
                                color: COLORS.text,
                                borderColor: labelError ? 'red' : COLORS.icon,
                                backgroundColor: COLORS.background,
                                borderWidth: labelError ? 2 : 1
                            }
                        ]}
                        placeholder={labelError ? i18n.t('nameRequired') : i18n.t('timeNamePlaceholder')}
                        placeholderTextColor={labelError ? 'red' : COLORS.textSecondary}
                        value={newEventLabel}
                        onChangeText={(text) => {
                            setNewEventLabel(text);
                            if (text.trim()) setLabelError(false);
                        }}
                    />
                    {labelError && (
                        <Text style={styles.errorText}>{i18n.t('nameRequiredHint')}</Text>
                    )}

                    <Text style={[styles.timeLabel, { color: COLORS.textSecondary }]}>{i18n.t('timeLabel')}</Text>

                    <View style={[styles.cardHeader, styles.timePickerContainer]}>
                        <View style={styles.timeDisplayContainer}>
                            <Text style={styles.timeDisplay}>
                                {newEventTime.getHours().toString().padStart(2, '0')}:{newEventTime.getMinutes().toString().padStart(2, '0')}
                            </Text>
                        </View>

                        <Pressable
                            style={[styles.timeButton, { backgroundColor: COLORS.background, borderWidth: 1, borderColor: COLORS.icon }]}
                            onPress={() => setShowCreateTimePicker(true)}
                        >
                            <Text style={[styles.timeText, { color: COLORS.text }]}>{i18n.t('editTime')}</Text>
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

                    <View style={commonStyles.modalButtons}>
                        <Pressable
                            style={[commonStyles.button, commonStyles.buttonClose]}
                            onPress={onClose}
                        >
                            <Text style={[commonStyles.textStyle, { color: COLORS.textSecondary }]}>{i18n.t('cancel')}</Text>
                        </Pressable>
                        <Pressable style={[commonStyles.button, { backgroundColor: COLORS.primary }]} onPress={handleCreateEvent}>
                            <Text style={commonStyles.textStyle}>{i18n.t('create')}</Text>
                        </Pressable>
                    </View>
                </View>
            </View>
        </Modal>
    );
};

const styles = StyleSheet.create({
    cardHeader: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        marginBottom: 12,
    },
    cardTitle: {
        fontSize: 18,
        fontWeight: '600',
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

    label: {
        alignSelf: 'flex-start',
        marginLeft: 12,
        marginBottom: 4,
    },
    errorText: {
        color: 'red',
        alignSelf: 'flex-start',
        marginLeft: 12,
        marginBottom: 4,
    },
    timeLabel: {
        alignSelf: 'flex-start',
        marginLeft: 12,
        marginBottom: 4,
        marginTop: 10,
    },
    timePickerContainer: {
        width: '100%',
        paddingHorizontal: 12,
        marginBottom: 20,
    },
    timeDisplay: {
        fontSize: 22,
        fontWeight: '600',
        color: COLORS.text,
    },
    timeDisplayContainer: {
        flex: 1,
    },
});
