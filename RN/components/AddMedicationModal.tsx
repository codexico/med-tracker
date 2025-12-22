import React, { useState, useEffect } from 'react';
import { View, Text, Modal, TextInput, Pressable } from 'react-native';

import { COLORS } from '@/constants/theme';
import { commonStyles } from '@/constants/commonStyles';

import i18n from '@/i18n';


interface AddMedicationModalProps {
    visible: boolean;
    onClose: () => void;
    onAdd: (medicationName: string) => void;
}

export const AddMedicationModal: React.FC<AddMedicationModalProps> = ({ visible, onClose, onAdd }) => {
    const [newMedication, setNewMedication] = useState('');

    useEffect(() => {
        if (visible) {
            setNewMedication('');
        }
    }, [visible]);

    const handleAdd = () => {
        if (newMedication.trim()) {
            onAdd(newMedication.trim());
            onClose();
        }
    };

    return (
        <Modal
            animationType="slide"
            transparent={true}
            visible={visible}
            onRequestClose={onClose}
        >
            <View style={commonStyles.modalOverlay}>
                <View style={commonStyles.modalView}>
                    <Text style={commonStyles.modalTitle}>{i18n.t('newMedication')}</Text>
                    <TextInput
                        style={[commonStyles.input, { borderColor: COLORS.icon, backgroundColor: COLORS.background, color: COLORS.text }]}
                        placeholder={i18n.t('medNamePlaceholder')}
                        placeholderTextColor={COLORS.textSecondary}
                        value={newMedication}
                        onChangeText={setNewMedication}
                        autoFocus
                    />
                    <View style={commonStyles.modalButtons}>
                        <Pressable style={[commonStyles.button, commonStyles.buttonClose]}
                            onPress={onClose}>
                            <Text style={[commonStyles.textStyle, { color: COLORS.textSecondary }]}>{i18n.t('cancel')}</Text>
                        </Pressable>
                        <Pressable style={[commonStyles.button, { backgroundColor: COLORS.primary }]} onPress={handleAdd}>
                            <Text style={commonStyles.textStyle}>{i18n.t('add')}</Text>
                        </Pressable>
                    </View>
                </View>
            </View>
        </Modal>
    );
};



