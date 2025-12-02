export interface MedEvent {
    id: string;
    label: string;
    time: string; // Format "HH:mm"
    icon: string;
    enabled: boolean;
    completedToday: boolean;
    lastCompletedDate?: string; // ISO Date string YYYY-MM-DD
}

export interface UserSettings {
    hasCompletedOnboarding: boolean;
}
