import * as Notifications from 'expo-notifications';
import { MedEvent } from '@/types';
import { Alert, Platform } from 'react-native';

// Configure notification behavior
Notifications.setNotificationHandler({
    handleNotification: async () => ({
        shouldShowAlert: true,
        shouldPlaySound: true,
        shouldSetBadge: false,
        shouldShowBanner: true,
        shouldShowList: true,
    }),
});

// Create channel on Android
if (Platform.OS === 'android') {
    Notifications.setNotificationChannelAsync('default', {
        name: 'default',
        importance: Notifications.AndroidImportance.MAX,
        vibrationPattern: [0, 250, 250, 250],
        lightColor: '#FF231F7C',
    });
}

/**
 * Request permissions for notifications.
 * Should be called on app launch or onboarding.
 */
export const requestPermissions = async () => {
    const { status: existingStatus } = await Notifications.getPermissionsAsync();
    let finalStatus = existingStatus;

    if (existingStatus !== 'granted') {
        const { status } = await Notifications.requestPermissionsAsync();
        finalStatus = status;
    }

    if (finalStatus !== 'granted') {
        Alert.alert('Permissão necessária', 'Para receber lembretes dos remédios, precisamos da sua permissão para enviar notificações.');
        return false;
    }
    return true;
};

/**
 * Cancels a specific notification by ID.
 */
export const cancelNotification = async (notificationId: string) => {
    if (!notificationId) return;
    try {
        await Notifications.cancelScheduledNotificationAsync(notificationId);
        console.log(`Notification ${notificationId} cancelled.`);
    } catch (e) {
        console.warn(`Failed to cancel notification ${notificationId}:`, e);
    }
};

/**
 * Schedules a daily notification for a medication event.
 * Returns the new notification ID.
 */
export const scheduleEventNotification = async (event: MedEvent): Promise<string | null> => {
    if (!event.enabled) return null;

    // Ensure permissions
    const permission = await requestPermissions();
    if (!permission) return null;

    // Parse time HH:mm
    const [hourStr, minuteStr] = event.time.split(':');
    const hour = parseInt(hourStr, 10);
    const minute = parseInt(minuteStr, 10);

    // Create content
    let body = `Está na hora do: ${event.label}`;
    if (event.medications && event.medications.length > 0) {
        body += `\nTomar: ${event.medications.join(', ')}`;
    }

    try {
        const id = await Notifications.scheduleNotificationAsync({
            content: {
                title: 'Hora do Remedinho! 💊',
                body: body,
                sound: true,
                data: { eventId: event.id },
            },
            trigger: {
                type: Notifications.SchedulableTriggerInputTypes.DAILY,
                hour,
                minute,
                channelId: 'default',
            } as any,
        });

        console.log(`Scheduled notification ${id} for ${event.label} at ${event.time}`);
        return id;
    } catch (e) {
        console.error('Error scheduling notification:', e);
        return null;
    }
};
