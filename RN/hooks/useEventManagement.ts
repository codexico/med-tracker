import { useState, useCallback, useEffect } from 'react';

import { MedEvent } from '@/types';
import { getClockIconName, DEFAULT_ICON_NAMES } from '@/constants/ClockIcons';

import { getEvents, saveEvent, toggleEventCompletion as dbToggleEventCompletion } from '@/services/Database';
import { scheduleEventNotification, cancelNotification } from '@/services/Notifications';


export const useEventManagement = () => {
    const [events, setEvents] = useState<MedEvent[]>([]);

    const loadEvents = useCallback(async () => {
        const loadedEvents = await getEvents();
        setEvents(loadedEvents);
    }, []);

    useEffect(() => {
        loadEvents();
    }, [loadEvents]);

    const updateEvent = async (targetEvent: MedEvent) => {
        const oldEvent = events.find(e => e.id === targetEvent.id);
        let finalEvent = { ...targetEvent };

        const timeChanged = !oldEvent || oldEvent.time !== finalEvent.time;
        const enabledChanged = !oldEvent || oldEvent.enabled !== finalEvent.enabled;
        const medsChanged = !oldEvent || JSON.stringify(oldEvent.medications) !== JSON.stringify(finalEvent.medications);
        const labelChanged = !oldEvent || oldEvent.label !== finalEvent.label;

        if (timeChanged || enabledChanged || medsChanged || labelChanged) {
            if (oldEvent?.notificationId) {
                await cancelNotification(oldEvent.notificationId);
                finalEvent.notificationId = undefined;
            }

            if (finalEvent.enabled) {
                const newId = await scheduleEventNotification(finalEvent);
                if (newId) {
                    finalEvent.notificationId = newId;
                }
            }
        } else {
            finalEvent.notificationId = oldEvent?.notificationId;
        }

        setEvents(prev => {
            const updated = prev.map(e => e.id === finalEvent.id ? finalEvent : e);
            return updated.sort((a, b) => a.time.localeCompare(b.time));
        });

        await saveEvent(finalEvent);
    };

    const updateEventTime = (id: string, selectedDate: Date) => {
        const hours = selectedDate.getHours().toString().padStart(2, '0');
        const minutes = selectedDate.getMinutes().toString().padStart(2, '0');
        const timeString = `${hours}:${minutes}`;

        const currentEvent = events.find(e => e.id === id);
        if (currentEvent) {
            let newIcon = currentEvent.icon;
            if (!DEFAULT_ICON_NAMES.includes(currentEvent.icon)) {
                newIcon = getClockIconName(timeString);
            }
            updateEvent({ ...currentEvent, time: timeString, icon: newIcon });
        }
    };

    const toggleEvent = (id: string) => {
        const currentEvent = events.find(e => e.id === id);
        if (currentEvent) {
            updateEvent({ ...currentEvent, enabled: !currentEvent.enabled });
        }
    };

    const addMedication = (id: string, medName: string) => {
        const currentEvent = events.find(e => e.id === id);
        if (currentEvent) {
            const updatedMeds = [...(currentEvent.medications || []), medName];
            updateEvent({ ...currentEvent, medications: updatedMeds });
        }
    };

    const removeMedication = (id: string, index: number) => {
        const currentEvent = events.find(e => e.id === id);
        if (currentEvent && currentEvent.medications) {
            const updatedMeds = currentEvent.medications.filter((_, i) => i !== index);
            updateEvent({ ...currentEvent, medications: updatedMeds });
        }
    };

    const createEvent = async (label: string, time: Date) => {
        const hours = time.getHours().toString().padStart(2, '0');
        const minutes = time.getMinutes().toString().padStart(2, '0');
        const timeString = `${hours}:${minutes}`;
        const newId = `event-${Date.now()}`;

        let newEvent: MedEvent = {
            id: newId,
            label: label,
            time: timeString,
            icon: getClockIconName(timeString),
            enabled: true,
            medications: [],
            completedToday: false
        };

        const notifId = await scheduleEventNotification(newEvent);
        if (notifId) {
            newEvent.notificationId = notifId;
        }

        setEvents(prev => {
            const updated = [...prev, newEvent];
            return updated.sort((a, b) => a.time.localeCompare(b.time));
        });
        await saveEvent(newEvent);
    };

    const toggleEventCompletion = async (id: string, completed: boolean) => {
        // Optimistic update
        setEvents(prev => prev.map(e =>
            e.id === id ? { ...e, completedToday: completed } : e
        ));

        await dbToggleEventCompletion(id, completed);
    };

    return {
        events,
        loadEvents,
        updateEventTime,
        toggleEvent,
        toggleEventCompletion,
        addMedication,
        removeMedication,
        createEvent
    };
};
