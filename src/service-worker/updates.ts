import { getEventsFromDB } from '../utils/db';
import { showServiceWorkerNotification } from '../utils/notifications';

declare const self: ServiceWorkerGlobalScope;

export async function checkForUpdates() {
    console.log('Checking for medication updates...');
    const events = await getEventsFromDB();
    const now = new Date();
    const currentMinutes = now.getHours() * 60 + now.getMinutes();

    console.log(`Current minutes: ${currentMinutes}. Found ${events.length} events in DB.`);

    for (const event of events) {
        if (event.enabled && !event.completedToday) {
            const [eventHour, eventMinute] = event.time.split(':').map(Number);
            const eventMinutes = eventHour * 60 + eventMinute;

            // Check if current time is within +/- 1 hour of event time
            const diff = currentMinutes - eventMinutes;

            if (Math.abs(diff) <= 60) {
                console.log(`Time match (within +/- 1h range) for ${event.label}. Showing notification.`);
                await showServiceWorkerNotification(self.registration, event.label);
            }
        }
    }
}