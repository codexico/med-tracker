import { useEffect, useRef } from 'react';
import { MedEvent } from '../types';
import { showMedicationNotification } from '../utils/notifications';
import { UPDATE_CHECK } from '../constants';

export const useNotifications = (events: MedEvent[]) => {
    const lastCheck = useRef<number>(0);

    const showNotification = (eventName: string) => {
        showMedicationNotification(eventName);
    };

    useEffect(() => {
        // Check every minute
        const interval = setInterval(() => {
            const now = new Date();
            const currentTime = now.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });

            // Prevent double notifications in the same minute
            if (Date.now() - lastCheck.current < 50000) return;
            lastCheck.current = Date.now();

            events.forEach(event => {
                if (event.enabled && !event.completedToday && event.time === currentTime) {
                    showNotification(event.label);
                }
            });

        }, 30000); // Check every 30 seconds to be safe

        return () => clearInterval(interval);
    }, [events]);

    useEffect(() => {
        if ('serviceWorker' in navigator && 'periodicSync' in navigator.serviceWorker) {
            navigator.serviceWorker.ready.then(async (registration) => {
                try {
                    // @ts-expect-error periodicSync is not in the default types
                    await registration.periodicSync.register(UPDATE_CHECK, {
                        minInterval: 60 * 60 * 1000, // 1 hour (minimum allowed usually)
                    });
                    console.log('Periodic sync registered:', UPDATE_CHECK);
                } catch (error) {
                    console.error('Periodic sync registration failed:', error);
                }
            });
        }
    }, []);



    const requestPermission = () => {
        if ('Notification' in window && Notification.permission === 'default') {
            Notification.requestPermission();
        }
    };

    return { requestPermission };
};
