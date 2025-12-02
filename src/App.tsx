import { CircularProgress, Box } from '@mui/material';
import { Dashboard } from './components/Dashboard';
import { Onboarding } from './components/Onboarding';
import { useEvents } from './hooks/useEvents';
import { useNotifications } from './hooks/useNotifications';

function App() {
    const {
        events,
        settings,
        loading,
        toggleEventCompletion,
        toggleEventEnabled,
        completeOnboarding,
        undoOnboarding,
        updateEventTime
    } = useEvents();

    useNotifications(events);

    if (loading) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
                <CircularProgress size={60} />
            </Box>
        );
    }

    if (!settings.hasCompletedOnboarding) {
        return (
            <Onboarding
                events={events}
                onToggleEnabled={toggleEventEnabled}
                onComplete={completeOnboarding}
                onUpdateTime={updateEventTime}
            />
        );
    }

    return (
        <Dashboard
            events={events}
            onToggleEvent={toggleEventCompletion}
            onUndoOnboarding={undoOnboarding}
        />
    );
}

export default App;
