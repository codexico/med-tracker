'use no memo';
import { Tabs } from 'expo-router';
import React from 'react';
import { Fontisto, Ionicons } from '@expo/vector-icons';
import i18n from '@/i18n';
import { HapticTab } from '@/components/haptic-tab';
import { COLORS } from '@/constants/theme';


export default function TabLayout() {
  return (
    <Tabs
      initialRouteName={__DEV__ ? 'HelloWidgetPreviewScreen' : 'index'}
      screenOptions={{
        tabBarActiveTintColor: COLORS.tint,
        headerShown: false,
        tabBarButton: HapticTab,
      }}>
      <Tabs.Screen
        name="HelloWidgetPreviewScreen"
        options={{
          title: 'Development',
          tabBarIcon: ({ color }) => <Ionicons size={28} name="desktop-outline" color={color} />,
          href: __DEV__ ? '/(tabs)/HelloWidgetPreviewScreen' : null,
        }}
      />
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
      <Tabs.Screen
        name="about"
        options={{
          title: i18n.t('about'),
          tabBarIcon: ({ color }) => <Ionicons size={28} name="information-circle-outline" color={color} />,
        }}
      />
    </Tabs>
  );
}
