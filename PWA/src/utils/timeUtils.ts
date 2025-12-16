export const getIconForTime = (time: string): string => {
    const hour = parseInt(time.split(':')[0], 10);

    if (hour >= 5 && hour < 10) return 'wb_sunny'; // Morning
    if (hour >= 10 && hour < 15) return 'restaurant'; // Lunch/Mid-day
    if (hour >= 15 && hour < 18) return 'local_cafe'; // Afternoon
    if (hour >= 18 && hour < 22) return 'dinner_dining'; // Dinner
    return 'bed'; // Night/Sleep
};
