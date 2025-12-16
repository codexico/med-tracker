declare const self: ServiceWorkerGlobalScope;

interface PeriodicBackgroundSyncEvent extends ExtendableEvent {
	tag: string;
}

import { precacheAndRoute } from 'workbox-precaching';

import { UPDATE_CHECK } from '../constants.ts';
import { checkForUpdates } from './updates.ts';

precacheAndRoute(self.__WB_MANIFEST || []);

self.addEventListener('install', () => void self.skipWaiting());
self.addEventListener('activate', () => void self.clients.claim());

self.addEventListener('notificationclick', (event) => {
	event.notification.close();

	const urlToOpen = new URL('/', self.location.origin).href;

	const promiseChain = self.clients.matchAll({
		type: 'window',
		includeUncontrolled: true
	}).then((windowClients) => {
		let matchingClient = null;

		for (let i = 0; i < windowClients.length; i++) {
			const windowClient = windowClients[i];
			if (windowClient.url === urlToOpen) {
				matchingClient = windowClient;
				break;
			}
		}

		if (matchingClient) {
			return matchingClient.focus();
		} else {
			return self.clients.openWindow(urlToOpen);
		}
	});

	event.waitUntil(promiseChain);
});

// @ts-expect-error periodicsync is not included in the default SW interface.
self.addEventListener('periodicsync', (event: PeriodicBackgroundSyncEvent) => {
	if (event.tag === UPDATE_CHECK) {
		event.waitUntil(checkForUpdates());
	}
});

self.addEventListener('message', (event) => {
	if (event.data === UPDATE_CHECK) {
		event.waitUntil(checkForUpdates());
	}
});