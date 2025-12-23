import { DefaultTheme, ThemeProvider } from '@react-navigation/native';
import { router, Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import 'react-native-reanimated';
import { Suspense, useEffect } from 'react';
import { View, ActivityIndicator } from 'react-native';

import { SQLiteProvider } from 'expo-sqlite';
import { initializeDatabase, getSetting } from '@/services/Database';
import { EventProvider } from '@/context/EventContext';

export const unstable_settings = {
  anchor: '(tabs)',
};

const Fallback = () => {
  return (
    <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}>
      <ActivityIndicator />
    </View>
  );
};

const AppContent = () => {
  useEffect(() => {
    const checkOnboarding = async () => {
      const onboarded = await getSetting('hasCompletedOnboarding');
      console.log('Onboarded:', onboarded);
      if (onboarded !== 'true') {
        console.log('Redirecting to initial');
        router.replace('/initial' as any);
      }
    };
    checkOnboarding();
  }, []);


  return (
    <ThemeProvider value={DefaultTheme}>
      <Stack>
        <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
        <Stack.Screen name="initial" options={{ headerShown: false }} />
      </Stack>
      <StatusBar style="auto" />
    </ThemeProvider>
  );
};

export default function RootLayout() {
  return (
    <Suspense fallback={<Fallback />}>
      <SQLiteProvider databaseName="medtracker.db" onInit={initializeDatabase} useSuspense>
        <EventProvider>
          <AppContent />
        </EventProvider>
      </SQLiteProvider>
    </Suspense>
  );
}

