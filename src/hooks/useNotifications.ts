import { useEffect, useRef } from 'react';
import { MedEvent } from '../types';

export const useNotifications = (events: MedEvent[]) => {
    const lastCheck = useRef<number>(0);

    const showNotification = (eventName: string) => {
        if (!('Notification' in window)) return;

        if (Notification.permission === 'granted') {
            const title = 'Hora do Remédio!';
            const options = {
                body: `Já tomou o remédio: ${eventName}?`,
                icon: '/pwa-192x192.png',
                vibrate: [200, 100, 200], // Vibration pattern
                tag: `med-${eventName}-${Date.now()}` // Unique tag to prevent overwriting
            };

            // Try to use Service Worker for notifications (required for Android)
            if ('serviceWorker' in navigator) {
                navigator.serviceWorker.ready.then(registration => {
                    registration.showNotification(title, options);
                }).catch(err => {
                    console.error('Service Worker notification failed:', err);
                    // Fallback
                    new Notification(title, options);
                });
            } else {
                // Fallback for browsers without Service Worker support
                new Notification(title, options);
            }
        }
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



    const requestPermission = () => {
        if ('Notification' in window && Notification.permission === 'default') {
            Notification.requestPermission();
        }
    };

    return { requestPermission };
};
