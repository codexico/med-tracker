import React from 'react';
import { Fontisto, MaterialIcons } from '@expo/vector-icons';
import { CLOCK_ICONS } from '@/constants/IconAssets';
import { ClockIcon } from '@/components/ClockIcon';

export { getClockIconName, DEFAULT_ICON_NAMES } from '@/constants/IconAssets';
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