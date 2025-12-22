import React, { useCallback, useState } from 'react';
import { StyleSheet, View, Text, ScrollView, Pressable } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useFocusEffect } from 'expo-router';
import { Checkbox } from 'expo-checkbox';

import { MedEvent } from '@/types';
import { COLORS } from '@/constants/theme';
import { getIcon } from '@/constants/ClockIcons';
import { MedicationList } from '@/components/MedicationList';
import i18n from '@/i18n';

import { getEvents, toggleEventCompletion } from '@/services/Database';
import { commonStyles } from '@/constants/commonStyles';


export default function DashboardScreen() {
  const insets = useSafeAreaInsets();
  const [events, setEvents] = useState<MedEvent[]>([]);

  const loadEvents = async () => {
    const loaded = await getEvents();
    // Sort by time
    loaded.sort((a, b) => a.time.localeCompare(b.time));
    setEvents(loaded);
  };

  useFocusEffect(
    useCallback(() => {
      loadEvents();
    }, [])
  );

  const handleToggle = async (id: string, currentStatus: boolean) => {
    try {
      // Optimistic update
      setEvents(prev => prev.map(e =>
        e.id === id ? { ...e, completedToday: !currentStatus } : e
      ));
      await toggleEventCompletion(id, !currentStatus);
    } catch (e) {
      console.error(e);
      // Revert on error
      loadEvents();
    }
  };

  return (
    <View style={[commonStyles.container, { backgroundColor: COLORS.background, paddingTop: insets.top + 20 }]}>
      <View style={commonStyles.header}>
        <Text style={[commonStyles.title, { color: COLORS.text }]}>{i18n.t('appName')}</Text>
      </View>

      <ScrollView
        contentContainerStyle={commonStyles.content}
      >
        {events.filter(e => e.enabled).map(event => (
          <Pressable
            key={event.id}
            style={[
              commonStyles.card,
              styles.card,
              { backgroundColor: COLORS.surface },
              event.completedToday && { opacity: 0.7 }
            ]}
            onPress={() => handleToggle(event.id, event.completedToday)}
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
                      onValueChange={() => handleToggle(event.id, event.completedToday)}
                      color={event.completedToday ? COLORS.primary : COLORS.textSecondary}
                      style={styles.checkbox}

                    />
                  </View>
                </View>

                <MedicationList medications={event.medications} />
              </View>
            </View>
          </Pressable>
        ))}

        {events.filter(e => e.enabled).length === 0 && (
          <Text style={styles.emptyText}>
            {i18n.t('noEvents')}
          </Text>
        )}
      </ScrollView>
    </View >
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
  emptyText: {
    textAlign: 'center',
    marginTop: 40,
    color: COLORS.textSecondary,
  },
});
