"use no memo"
import React from 'react';
import { FlexWidget, TextWidget, WidgetInfo } from 'react-native-android-widget';
import type { HexColor } from 'react-native-android-widget/src/widgets/utils/style.props';

import { MedEvent } from '@/types';
import { COLORS } from '@/constants/theme';

import { WidgetCheckbox } from './WidgetCheckbox';
import { EventCardIconWidget } from './EventCardIconWidget';

interface EventCardWidgetProps {
  event: MedEvent;
  widgetInfo?: WidgetInfo;
}

export function EventCardWidget({ event, widgetInfo }: EventCardWidgetProps) {
  // If we don't have widgetInfo, we assume wide by default unless specified
  const isNarrow = widgetInfo?.width ? widgetInfo.width <= 172 : false;

  if (__DEV__) {
    console.log({ widgetInfo });
    console.log({ isNarrow });
  }

  // WIDE LAYOUT
  return (
    // outer
    <FlexWidget
      style={{
        flexDirection: 'column',
        borderRadius: 16,
        padding: 8,
        alignItems: 'center',
        justifyContent: 'center',
        width: 'match_parent',
        flex: 1,
        height: 'match_parent',
        backgroundColor: COLORS.surface as HexColor,
      }}
    >

      {/* header */}
      <FlexWidget
        style={{
          flexDirection: 'row',
          alignItems: 'center',
          justifyContent: 'flex-start',
          width: 'match_parent',
        }}
      >

        {/* Column 1: Icon */}
        <FlexWidget
          style={{
            backgroundColor: COLORS.background as HexColor,
            padding: 6,
            borderRadius: 12,
            justifyContent: 'center',
            alignItems: 'center',
          }}
        >
          <EventCardIconWidget icon={event.icon} isNarrow={isNarrow} />
        </FlexWidget>

        {/* Column 2: text */}
        <FlexWidget
          style={{
            marginLeft: 8,
            flexDirection: 'column',
            justifyContent: 'flex-start',
            flex: 1,
          }}
        >
          {/* row 1: name */}
          <TextWidget
            text={event.label}
            style={{
              fontSize: 18,
              color: COLORS.text as HexColor,
              fontWeight: 'bold',
            }}
            maxLines={1}
            truncate="END"
          />
          {/* row 2: hour */}
          <TextWidget
            text={event.time}
            style={{
              fontSize: 14,
              color: COLORS.textSecondary as HexColor,
              fontWeight: 'bold',
              marginTop: 2,
            }}
          />
        </FlexWidget>

        {/* Column 3: checkbox */}
        {!isNarrow && (
          <FlexWidget
            style={{
              justifyContent: 'flex-end',
              alignItems: 'flex-end',
              paddingLeft: 4,
              paddingRight: 4,
              width: 'wrap_content',
            }}
          >
            <WidgetCheckbox
              checked={event.completedToday}
              color={COLORS.primary as HexColor}
            />
          </FlexWidget>
        )}

      </FlexWidget>
    </FlexWidget>
  )
}
