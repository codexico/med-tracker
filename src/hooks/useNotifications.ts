import { useEffect, useRef } from 'react';
import { MedEvent } from '../types';

export const useNotifications = (events: MedEvent[]) => {
    const lastCheck = useRef<number>(Date.now());

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

    const showNotification = (eventName: string) => {
        if (!('Notification' in window)) return;

        if (Notification.permission === 'granted') {
            new Notification('Hora do Remédio!', {
                body: `Já tomou o remédio: ${eventName}?`,
                icon: '/pwa-192x192.png', // We need to ensure this exists or use a placeholder
            });
        }
    };

    const requestPermission = () => {
        if ('Notification' in window && Notification.permission === 'default') {
            Notification.requestPermission();
        }
    };

    return { requestPermission };
};
