/**
 * Helper to get next half hour (e.g. 10:12 -> 10:30, 10:45 -> 11:00)
 */
export const getNextHalfHour = () => {
    const date = new Date();
    const minutes = date.getMinutes();
    if (minutes < 30) {
        date.setMinutes(30, 0, 0);
    } else {
        date.setHours(date.getHours() + 1, 0, 0, 0);
    }
    return date;
};
