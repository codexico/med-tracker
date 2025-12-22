import React from 'react';
import { View, Text, Pressable, StyleSheet } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

import { COLORS } from '@/constants/theme';
import i18n from '@/i18n';
import { MedEvent } from '@/types';

interface MedicationListProps {
    medications: MedEvent['medications'];
    onRemove?: (index: number) => void;
    onAdd?: () => void;
}

export const MedicationList: React.FC<MedicationListProps> = ({ medications, onRemove, onAdd }) => {
    // If no medications and no add capability, render nothing
    if ((!medications || medications.length === 0) && !onAdd) {
        return null;
    }

    return (
        <View style={styles.medicationSection}>
            <View style={styles.medicationList}>
                {medications && medications.map((med, idx) => (
                    <View key={idx} style={styles.chip}>
                        <Text style={styles.chipText}>{med}</Text>
                        {onRemove && (
                            <Pressable onPress={() => onRemove(idx)}>
                                <Ionicons name="close-circle" size={16} color={COLORS.textSecondary} />
                            </Pressable>
                        )}
                    </View>
                ))}
            </View>
            {onAdd && (
                <Pressable
                    style={[styles.addMedButton, { borderColor: COLORS.primary }]}
                    onPress={onAdd}
                >
                    <View style={styles.addMedButtonContent}>
                        <Ionicons name="add-circle-outline" size={24} color={COLORS.primary} />
                        <Text style={styles.addMedButtonText}>{i18n.t('addMedication')}</Text>
                    </View>
                </Pressable>
            )}
        </View>
    );
};

const styles = StyleSheet.create({
    chip: {
        flexDirection: 'row',
        alignItems: 'center',
        paddingHorizontal: 10,
        paddingVertical: 6,
        borderRadius: 20,
        borderWidth: 1,
        justifyContent: 'center',
        backgroundColor: COLORS.background,
        borderColor: COLORS.icon
    },
    chipText: {
        color: COLORS.text,
        marginRight: 6,
    },
    medicationSection: {
        borderTopWidth: 1,
        borderTopColor: COLORS.separator,
        paddingTop: 12,
    },
    medicationList: {
        flexDirection: 'row',
        flexWrap: 'wrap',
        gap: 8,
        marginBottom: 12,
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
    addMedButtonContent: {
        flexDirection: 'row',
        alignItems: 'center',
    },
    addMedButtonText: {
        marginLeft: 8,
        color: COLORS.primary,
        fontWeight: '600',
    },
});
