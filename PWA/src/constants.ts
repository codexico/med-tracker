import { MedEvent } from './types';
import { v4 as uuidv4 } from 'uuid';

export const UPDATE_CHECK = 'update-check';

export const DEFAULT_EVENTS: Omit<MedEvent, 'id' | 'completedToday'>[] = [
    { label: 'Ao acordar', time: '07:00', icon: 'wb_sunny', enabled: true },
    { label: 'Café da manhã', time: '08:00', icon: 'local_cafe', enabled: true },
    { label: 'Manhã', time: '10:00', icon: 'work', enabled: true },
    { label: 'Almoço', time: '12:00', icon: 'restaurant', enabled: true },
    { label: 'Tarde', time: '16:00', icon: 'wb_twilight', enabled: true },
    { label: 'Janta', time: '20:00', icon: 'dinner_dining', enabled: true },
    { label: 'Antes de dormir', time: '22:00', icon: 'bed', enabled: true },
];

export const createInitialEvents = (): MedEvent[] => {
    return DEFAULT_EVENTS.map(event => ({
        ...event,
        id: uuidv4(),
        completedToday: false,
    }));
};
