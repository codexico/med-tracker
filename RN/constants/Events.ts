import { MedEvent } from '@/types';
import * as Crypto from 'expo-crypto';
import i18n from '@/i18n';

export const DEFAULT_EVENTS: Omit<MedEvent, 'id' | 'completedToday'>[] = [
    { label: i18n.t('wakeUp'), time: '07:00', icon: 'wb_sunny', enabled: true },
    { label: i18n.t('breakfast'), time: '08:00', icon: 'local_cafe', enabled: true },
    { label: i18n.t('morning'), time: '10:00', icon: 'work', enabled: true },
    { label: i18n.t('lunch'), time: '12:00', icon: 'restaurant', enabled: true },
    { label: i18n.t('afternoon'), time: '16:00', icon: 'wb_twilight', enabled: true },
    { label: i18n.t('dinner'), time: '20:00', icon: 'dinner_dining', enabled: true },
    { label: i18n.t('sleep'), time: '22:00', icon: 'bed', enabled: true },
];

export const createInitialEvents = (): MedEvent[] => {
    return DEFAULT_EVENTS.map(event => ({
        ...event,
        id: Crypto.randomUUID(),
        completedToday: false,
    }));
};
