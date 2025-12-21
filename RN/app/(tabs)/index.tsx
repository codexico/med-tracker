import React, { useCallback, useState } from 'react';
import { StyleSheet, View, Text, ScrollView, Pressable } from 'react-native';
import { useFocusEffect } from 'expo-router';
import { Colors } from '@/constants/theme';
import { MedEvent } from '@/types';
import { getEvents, toggleEventCompletion } from '@/services/Database';
import { getIcon } from '@/constants/ClockIcons';
import { Checkbox } from 'expo-checkbox';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import i18n from '@/i18n';


export default function DashboardScreen() {
  const insets = useSafeAreaInsets();
  const theme = Colors.light;
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
    <View style={[styles.container, { backgroundColor: theme.background, paddingTop: insets.top + 20 }]}>
      <View style={styles.header}>
        <Text style={[styles.title, { color: theme.text }]}>{i18n.t('appName')}</Text>
      </View>

      <ScrollView
        contentContainerStyle={styles.content}
      >
        {events.filter(e => e.enabled).map(event => (
          <Pressable
            key={event.id}
            style={[
              styles.card,
              { backgroundColor: theme.surface },
              event.completedToday && { opacity: 0.7 }
            ]}
            onPress={() => handleToggle(event.id, event.completedToday)}
          >

            <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }}>

              <View style={[styles.iconContainer, { backgroundColor: theme.background, alignSelf: 'flex-start' }]}>
                {getIcon(event.icon, theme.primary, 24)}
              </View>
              <View style={{ flex: 1, flexShrink: 1 }}>

                <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', flexShrink: 1 }}>

                  <Text style={[
                    styles.cardTitle,
                    { color: theme.text, flexShrink: 1, paddingHorizontal: 8 },
                    event.completedToday && { textDecorationLine: 'line-through' }
                  ]}>
                    {event.label} {event.label}
                  </Text>
                  <View style={{ flexDirection: 'row', alignItems: 'center' }}>

                    <Text style={[styles.cardTime, { color: theme.textSecondary }]}>{event.time}</Text>


                    <Checkbox
                      value={event.completedToday}
                      onValueChange={() => handleToggle(event.id, event.completedToday)}
                      color={event.completedToday ? theme.primary : theme.textSecondary}
                      style={[{ marginLeft: 10 }]}

                    />
                  </View>
                </View>

                {/* Medication List */}
                {event.medications && event.medications.length > 0 && (
                  <View style={{ flex: 1, flexShrink: 1 }}>
                    <View style={styles.medicationList}>
                      {event.medications.map((med, idx) => (
                        <Text key={idx} style={[styles.medtext, { color: theme.textSecondary, flexShrink: 1, paddingHorizontal: 8 }]}>• {med}</Text>
                      ))}
                    </View>
                  </View>
                )}
              </View>
            </View>
          </Pressable>
        ))}

        {events.filter(e => e.enabled).length === 0 && (
          <Text style={{ textAlign: 'center', marginTop: 40, color: theme.textSecondary }}>
            {i18n.t('noEvents')}
          </Text>
        )}
      </ScrollView>
    </View >
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  header: {
    paddingHorizontal: 24,
    marginBottom: 20,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
  },
  content: {
    paddingHorizontal: 24,
    paddingBottom: 40,
  },
  card: {
    padding: 16,
    borderRadius: 16,
    marginBottom: 12,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  cardLeft: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: 16,
    flex: 1,
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
  medicationList: {
    marginTop: 4,
  },
  medtext: {
    fontSize: 14,
    lineHeight: 20,
  }
});
