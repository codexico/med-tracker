import { get, set } from 'idb-keyval';
import { MedEvent } from '../types';

const STORE_KEY = 'med_tracker_events';

export const saveEventsToDB = async (events: MedEvent[]) => {
    try {
        await set(STORE_KEY, events);
    } catch (error) {
        console.error('Error saving events to IDB:', error);
    }
};

export const getEventsFromDB = async (): Promise<MedEvent[]> => {
    try {
        return (await get(STORE_KEY)) || [];
    } catch (error) {
        console.error('Error getting events from IDB:', error);
        return [];
    }
};
