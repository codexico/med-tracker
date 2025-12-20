import React, { useCallback, useState } from 'react';
import { StyleSheet, View, Text, ScrollView, Pressable } from 'react-native';
import { useFocusEffect } from 'expo-router';
import { Colors } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { MedEvent } from '@/types';
import { getEvents, toggleEventCompletion } from '@/services/Database';
import { Ionicons } from '@expo/vector-icons';
import { getIcon } from '@/constants/ClockIcons';


export default function DashboardScreen() {
  const colorScheme = useColorScheme();
  const theme = Colors[colorScheme ?? 'light'];
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
    <View style={[styles.container, { backgroundColor: theme.background }]}>
      <View style={styles.header}>
        <Text style={[styles.title, { color: theme.text }]}>Meus Remedinhos</Text>
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
            <View style={styles.cardLeft}>
              <View style={[styles.iconContainer, { backgroundColor: theme.background }]}>
                {getIcon(event.icon, theme.primary, 24)}
              </View>
              <View style={{ flex: 1 }}>
                <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }}>
                  <Text style={[
                    styles.cardTitle,
                    { color: theme.text },
                    event.completedToday && { textDecorationLine: 'line-through' }
                  ]}>
                    {event.label}
                  </Text>
                  <Text style={[styles.cardTime, { color: theme.textSecondary }]}>{event.time}</Text>
                </View>

                {/* Medication List */}
                {event.medications && event.medications.length > 0 && (
                  <View style={styles.medicationList}>
                    {event.medications.map((med, idx) => (
                      <Text key={idx} style={[styles.medtext, { color: theme.textSecondary }]}>• {med}</Text>
                    ))}
                  </View>
                )}
              </View>
            </View>

            <Ionicons
              name={event.completedToday ? "checkmark-circle" : "ellipse-outline"}
              size={32}
              color={event.completedToday ? theme.primary : theme.textSecondary}
              style={{ paddingLeft: 8 }}
            />
          </Pressable>
        ))}

        {events.filter(e => e.enabled).length === 0 && (
          <Text style={{ textAlign: 'center', marginTop: 40, color: theme.textSecondary }}>
            Nenhum evento configurado.
          </Text>
        )}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingTop: 60,
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
    alignItems: 'flex-start', // Top align for list
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
