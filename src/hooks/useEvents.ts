import { useState, useEffect } from 'react';
import { MedEvent, UserSettings } from '../types';
import { createInitialEvents } from '../constants';
import { format } from 'date-fns';

const STORAGE_KEY_EVENTS = 'med_tracker_events';
const STORAGE_KEY_SETTINGS = 'med_tracker_settings';

export const useEvents = () => {
    const [events, setEvents] = useState<MedEvent[]>([]);
    const [settings, setSettings] = useState<UserSettings>({ hasCompletedOnboarding: false });
    const [loading, setLoading] = useState(true);

    // Load data on mount
    useEffect(() => {
        const loadData = () => {
            const storedEvents = localStorage.getItem(STORAGE_KEY_EVENTS);
            const storedSettings = localStorage.getItem(STORAGE_KEY_SETTINGS);

            if (storedEvents) {
                let parsedEvents: MedEvent[] = JSON.parse(storedEvents);

                // Check if we need to reset daily completion
                const today = format(new Date(), 'yyyy-MM-dd');
                parsedEvents = parsedEvents.map(event => {
                    if (event.lastCompletedDate !== today) {
                        return { ...event, completedToday: false };
                    }
                    return event;
                });

                setEvents(parsedEvents);
            } else {
                setEvents(createInitialEvents());
            }

            if (storedSettings) {
                setSettings(JSON.parse(storedSettings));
            }

            setLoading(false);
        };

        loadData();
    }, []);

    // Save events whenever they change
    useEffect(() => {
        if (!loading) {
            localStorage.setItem(STORAGE_KEY_EVENTS, JSON.stringify(events));
        }
    }, [events, loading]);

    // Save settings whenever they change
    useEffect(() => {
        if (!loading) {
            localStorage.setItem(STORAGE_KEY_SETTINGS, JSON.stringify(settings));
        }
    }, [settings, loading]);

    const toggleEventCompletion = (id: string) => {
        setEvents(prev => prev.map(event => {
            if (event.id === id) {
                const newStatus = !event.completedToday;
                return {
                    ...event,
                    completedToday: newStatus,
                    lastCompletedDate: newStatus ? format(new Date(), 'yyyy-MM-dd') : event.lastCompletedDate
                };
            }
            return event;
        }));
    };

    const toggleEventEnabled = (id: string) => {
        setEvents(prev => prev.map(event => {
            if (event.id === id) {
                return { ...event, enabled: !event.enabled };
            }
            return event;
        }));
    };

    const completeOnboarding = () => {
        setSettings({ hasCompletedOnboarding: true });
    };

    const undoOnboarding = () => {
        setSettings({ hasCompletedOnboarding: false });
    };

    const updateEventTime = (id: string, newTime: string) => {
        setEvents(prev => prev.map(event => {
            if (event.id === id) {
                return { ...event, time: newTime };
            }
            return event;
        }));
    };

    return {
        events,
        settings,
        loading,
        toggleEventCompletion,
        toggleEventEnabled,
        completeOnboarding,
        undoOnboarding,
        updateEventTime
    };
};
