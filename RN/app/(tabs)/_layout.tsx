import { Tabs } from 'expo-router';
import React from 'react';
import { Fontisto, Ionicons } from '@expo/vector-icons';

import { HapticTab } from '@/components/haptic-tab';
import { Colors } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';

export default function TabLayout() {
  const colorScheme = useColorScheme();

  return (
    <Tabs
      screenOptions={{
        tabBarActiveTintColor: Colors[colorScheme ?? 'light'].tint,
        headerShown: false,
        tabBarButton: HapticTab,
      }}>
      <Tabs.Screen
        name="index"
        options={{
          title: 'Meus Remedinhos',
          tabBarIcon: ({ color }) => <Fontisto size={24} name="pills" color={color} />,
        }}
      />
      <Tabs.Screen
        name="onboarding"
        options={{
          title: 'Configuração',
          tabBarIcon: ({ color }) => <Ionicons size={28} name="settings-sharp" color={color} />,
        }}
      />

    </Tabs>
  );
}
