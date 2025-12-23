import { Tabs } from 'expo-router';
import React from 'react';
import { Fontisto, Ionicons } from '@expo/vector-icons';
import i18n from '@/i18n';
import { HapticTab } from '@/components/haptic-tab';
import { COLORS } from '@/constants/theme';


export default function TabLayout() {
  return (
    <Tabs
      screenOptions={{
        tabBarActiveTintColor: COLORS.tint,
        headerShown: false,
        tabBarButton: HapticTab,
      }}>
      <Tabs.Screen
        name="index"
        options={{
          title: i18n.t('appName'),
          tabBarIcon: ({ color }) => <Fontisto size={24} name="pills" color={color} />,
        }}
      />
      <Tabs.Screen
        name="config"
        options={{
          title: i18n.t('config'),
          tabBarIcon: ({ color }) => <Ionicons size={28} name="settings-sharp" color={color} />,
        }}
      />

    </Tabs>
  );
}
