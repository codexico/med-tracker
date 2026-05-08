"use no memo"
import React from 'react';
import { SvgWidget, IconWidget } from 'react-native-android-widget';
import { COLORS } from '@/constants/theme';
import { CLOCK_ICONS } from '@/constants/IconAssets';
import type { HexColor } from 'react-native-android-widget/src/widgets/utils/style.props';

interface EventCardIconWidgetProps {
  icon: string;
  isNarrow: boolean
}

export function EventCardIconWidget({ icon, isNarrow }: EventCardIconWidgetProps) {
  const size = isNarrow ? 16 : 28;
  // If it's a clock icon (SVG)
  if (CLOCK_ICONS[icon]) {
    return (
      <SvgWidget
        svg={CLOCK_ICONS[icon]}
        style={{ height: size, width: size }}
      />
    );
  }

  // Fallback to Material Icons for the others
  const iconMap: Record<string, string> = {
    'wb_sunny': 'wb_sunny',
    'local_cafe': 'local_cafe',
    'work': 'work',
    'restaurant': 'restaurant',
    'wb_twilight': 'wb_twilight',
    'dinner_dining': 'dinner_dining',
    'bed': 'bed',
    'pills': 'medication',
  };

  const materialIconName = iconMap[icon] || 'medication';

  return (
    <IconWidget
      icon={materialIconName}
      size={size}
      style={{ color: COLORS.primary as HexColor }}
      font="material"
    />
  );
}
