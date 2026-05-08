import React, { useState } from 'react';
import { StyleSheet, View, Text, ScrollView, Pressable, Image } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { COLORS } from '@/constants/theme';
import { EventCard } from '@/components/EventCard';
import i18n from '@/i18n';
import { FAB } from '@/components/FAB';
import { AddEventModal } from '@/components/AddEventModal';
import { commonStyles } from '@/constants/commonStyles';
import { useEventContext } from '@/context/EventContext';


export default function DashboardScreen() {
  const insets = useSafeAreaInsets();
  const { events, toggleEventCompletion, createEvent } = useEventContext();
  const [createModalVisible, setCreateModalVisible] = useState(false);

  const handleToggle = async (id: string, currentStatus: boolean) => {
    await toggleEventCompletion(id, !currentStatus);
  };

  return (
    <View style={[commonStyles.container, { backgroundColor: COLORS.background, paddingTop: insets.top + 20 }]}>
      <View style={[commonStyles.header, commonStyles.headerFixed]}>
        <Image
          accessibilityLabel={i18n.t('appName')}
          source={i18n.locale.includes('pt')
            ? require('@/assets/images/med-logo-header-pt.png')
            : require('@/assets/images/med-logo-header.png')}
          style={commonStyles.headerLogo}
        />
      </View>

      <ScrollView
        contentContainerStyle={commonStyles.content}
      >
        {events.filter(e => e.enabled).map(event => (
          <EventCard key={event.id} event={event} onToggle={handleToggle} />
        ))}

        {events.filter(e => e.enabled).length === 0 && (
          <Text style={styles.emptyText}>
            {i18n.t('noEvents')}
          </Text>
        )}
      </ScrollView>

      <FAB onPress={() => setCreateModalVisible(true)} />

      <AddEventModal
        visible={createModalVisible}
        onClose={() => setCreateModalVisible(false)}
        onAdd={async (label, time) => {
          await createEvent(label, time);
          setCreateModalVisible(false);
        }}
      />
    </View >
  );
}

const styles = StyleSheet.create({
  emptyText: {
    textAlign: 'center',
    marginTop: 40,
    color: COLORS.textSecondary,
  },
});
