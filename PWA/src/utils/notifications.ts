export const showMedicationNotification = async (eventName: string) => {
    if (!('Notification' in window)) return;

    if (Notification.permission === 'granted') {
        const title = 'Hora do Remédio!';
        const options = {
            body: `Já tomou o remédio: ${eventName}?`,
            icon: '/pwa-192x192.png',
            vibrate: [200, 100, 200],
            tag: `med-${eventName}-${Date.now()}`
        };

        if ('serviceWorker' in navigator && navigator.serviceWorker.ready) {
            try {
                const registration = await navigator.serviceWorker.ready;
                await registration.showNotification(title, options);
            } catch (err) {
                console.error('Service Worker notification failed, falling back to window.Notification:', err);
                new Notification(title, options);
            }
        } else {
            new Notification(title, options);
        }
    }
};

export const showServiceWorkerNotification = async (registration: ServiceWorkerRegistration, eventName: string) => {
    const title = 'Hora do Remédio!';
    const options = {
        body: `Já tomou o remédio: ${eventName}?`,
        icon: '/pwa-192x192.png',
        vibrate: [200, 100, 200],
        tag: `med-${eventName}-${Date.now()}`
    };
    await registration.showNotification(title, options);
}
