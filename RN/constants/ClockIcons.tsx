import { ClockIcon } from '@/components/ClockIcon';
import React from 'react';
import { Fontisto, MaterialIcons } from '@expo/vector-icons';

// Map of all clock icons
export const CLOCK_ICONS: { [key: string]: any } = {
    'clock-1': require('@/assets/images/icons/clock-time/clock-1.svg'),
    'clock-1-30': require('@/assets/images/icons/clock-time/clock-1-30.svg'),
    'clock-2': require('@/assets/images/icons/clock-time/clock-2.svg'),
    'clock-2-30': require('@/assets/images/icons/clock-time/clock-2-30.svg'),
    'clock-3': require('@/assets/images/icons/clock-time/clock-3.svg'),
    'clock-3-30': require('@/assets/images/icons/clock-time/clock-3-30.svg'),
    'clock-4': require('@/assets/images/icons/clock-time/clock-4.svg'),
    'clock-4-30': require('@/assets/images/icons/clock-time/clock-4-30.svg'),
    'clock-5': require('@/assets/images/icons/clock-time/clock-5.svg'),
    'clock-5-30': require('@/assets/images/icons/clock-time/clock-5-30.svg'),
    'clock-6': require('@/assets/images/icons/clock-time/clock-6.svg'),
    'clock-6-30': require('@/assets/images/icons/clock-time/clock-6-30.svg'),
    'clock-7': require('@/assets/images/icons/clock-time/clock-7.svg'),
    'clock-7-30': require('@/assets/images/icons/clock-time/clock-7-30.svg'),
    'clock-8': require('@/assets/images/icons/clock-time/clock-8.svg'),
    'clock-8-30': require('@/assets/images/icons/clock-time/clock-8-30.svg'),
    'clock-9': require('@/assets/images/icons/clock-time/clock-9.svg'),
    'clock-9-30': require('@/assets/images/icons/clock-time/clock-9-30.svg'),
    'clock-10': require('@/assets/images/icons/clock-time/clock-10.svg'),
    'clock-10-30': require('@/assets/images/icons/clock-time/clock-10-30.svg'),
    'clock-11': require('@/assets/images/icons/clock-time/clock-11.svg'),
    'clock-11-30': require('@/assets/images/icons/clock-time/clock-11-30.svg'),
    'clock-12': require('@/assets/images/icons/clock-time/clock-12.svg'),
    'clock-12-30': require('@/assets/images/icons/clock-time/clock-12-30.svg'),
};

export const DEFAULT_ICON_NAMES = [
    'wb_sunny',
    'local_cafe',
    'work',
    'restaurant',
    'wb_twilight',
    'dinner_dining',
    'bed',
    'pills'
];

/**
 * Converts a 24h time string (HH:mm) to the closest clock icon name.
 * e.g. "14:30" -> "clock-2-30"
 * e.g. "00:00" -> "clock-12"
 */
export const getClockIconName = (time: string): string => {
    if (!time) return 'pills';

    const [hStr, mStr] = time.split(':');
    let hours = parseInt(hStr, 10);
    const minutes = parseInt(mStr, 10);

    // Convert to 12h format
    // 0 -> 12, 13 -> 1, 24 -> 12 (if ever)
    hours = hours % 12;
    if (hours === 0) hours = 12;

    // Determine if it's closer to :00 or :30
    // The app only allows 30 min intervals, so it should be exact.
    // But just to be safe/consistent:
    const isHalfHour = minutes >= 15 && minutes < 45;
    // If > 45, it would technically be closer to next hour, but let's stick to the 30-min block logic users might expect.
    // Actually, standard rounding:
    // 0-14 -> 00
    // 15-44 -> 30
    // 45-59 -> next hour 00?

    // Since the picker limits to 30 mins, we only expect 00 or 30.
    const suffix = isHalfHour || minutes === 30 ? '-30' : '';

    return `clock-${hours}${suffix}`;
};



// Simple icon mapping fallback
export const getIcon = (iconName: string, color: string, size: number) => {
    // Map our generic material names to something in Ionicons or Fontisto if needed
    // For now simple pass through or basic mapping
    switch (iconName) {
        case 'wb_sunny': return <MaterialIcons name="wb-sunny" size={size} color={color} />;
        case 'local_cafe': return <MaterialIcons name="local-cafe" size={size} color={color} />;
        case 'work': return <MaterialIcons name="work" size={size} color={color} />;
        case 'restaurant': return <MaterialIcons name="restaurant" size={size} color={color} />;
        case 'wb_twilight': return <MaterialIcons name="wb-twilight" size={size} color={color} />; // Note: check validity
        case 'dinner_dining': return <MaterialIcons name="dinner-dining" size={size} color={color} />;
        case 'bed': return <MaterialIcons name="bed" size={size} color={color} />;
        default:
            if (CLOCK_ICONS[iconName]) {
                return <ClockIcon iconName={iconName} size={size} color={color} />;
            }
            return <Fontisto name="pills" size={size} color={color} />;
    }
};