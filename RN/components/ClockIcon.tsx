import { Image } from 'expo-image';
import React from 'react';
import { CLOCK_ICONS } from '@/constants/IconAssets';

interface ClockIconProps {
    iconName: string;
    size: number;
    color: string;
}

export const ClockIcon: React.FC<ClockIconProps> = ({ iconName, size, color }) => {
    const source = CLOCK_ICONS[iconName];
    if (!source) return null;

    return (
        <Image
            source={source}
            style={{ width: size, height: size }}
            tintColor={color}
            contentFit="contain"
        />
    );
};
