import React from 'react';
import { StyleSheet, View, Text, Pressable } from 'react-native';
import { Checkbox } from 'expo-checkbox';

import { COLORS } from '@/constants/theme';
import { getIcon } from '@/constants/ClockIcons';
import { MedicationList } from '@/components/MedicationList';
import { commonStyles } from '@/constants/commonStyles';
import { MedEvent } from '@/types';

interface EventCardProps {
  event: MedEvent;
  onToggle: (id: string, currentStatus: boolean) => void;
}

export function EventCard({ event, onToggle }: EventCardProps) {
  return (
    <Pressable
      style={[
        commonStyles.card,
        styles.card,
        { backgroundColor: COLORS.surface },
        event.completedToday && { opacity: 0.7 }
      ]}
      onPress={() => onToggle(event.id, event.completedToday)}
    >
      <View style={styles.cardContent}>
        <View style={[styles.iconContainer, { backgroundColor: COLORS.background, alignSelf: 'flex-start' }]}>
          {getIcon(event.icon, COLORS.primary, 24)}
        </View>
        <View style={styles.cardBody}>
          <View style={commonStyles.cardHeader}>
            <Text style={[
              styles.cardTitle,
              { color: COLORS.text, flexShrink: 1, paddingHorizontal: 8 },
              event.completedToday && { textDecorationLine: 'line-through' }
            ]}>
              {event.label}
            </Text>
            <View style={styles.cardActions}>
              <Text style={[styles.cardTime, { color: COLORS.textSecondary }]}>{event.time}</Text>
              <Checkbox
                value={event.completedToday}
                onValueChange={() => onToggle(event.id, event.completedToday)}
                color={event.completedToday ? COLORS.primary : COLORS.textSecondary}
                style={styles.checkbox}
              />
            </View>
          </View>
          <MedicationList medications={event.medications} />
        </View>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  iconContainer: {
    padding: 10,
    borderRadius: 12,
  },
  cardTitle: {
    fontSize: 18,
    fontWeight: '600',
  },
  cardTime: {
    fontSize: 16,
    fontWeight: 'bold',
  },
  cardContent: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  cardBody: {
    flex: 1,
  },
  cardActions: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  checkbox: {
    marginLeft: 10,
  },
});
