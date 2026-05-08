"use no memo"
import React from 'react';
import { ListWidget, FlexWidget, TextWidget, WidgetInfo } from 'react-native-android-widget';
import { MedEvent } from '@/types';
import { EventCardWidget } from './EventCardWidget';
import { COLORS } from '@/constants/theme';
import type { HexColor } from 'react-native-android-widget/src/widgets/utils/style.props';
import i18n from '@/i18n';

interface DailyListWidgetProps {
    events: MedEvent[];
    widgetInfo?: WidgetInfo;
}

export function DailyListWidget({ events = [], widgetInfo }: DailyListWidgetProps) {
    const enabledEvents = events.filter(e => e.enabled);
    const sortedEvents = [...enabledEvents].sort((a, b) => a.time.localeCompare(b.time));
    const isNarrow = widgetInfo?.width ? widgetInfo.width <= 172 : false;

    if (sortedEvents.length === 0) {
        return (
            <FlexWidget
                style={{
                    height: 'match_parent',
                    width: 'match_parent',
                    justifyContent: 'center',
                    alignItems: 'center',
                    backgroundColor: COLORS.background as HexColor,
                    borderRadius: 16,
                }}
            >
                <TextWidget
                    text={i18n.t('noEventsToday')}
                    style={{ color: COLORS.textSecondary as HexColor, fontSize: 16 }}
                />
            </FlexWidget>
        );
    }

    return (
        <ListWidget
            style={{
                height: 'match_parent',
                width: 'match_parent',
                backgroundColor: COLORS.background as HexColor,
            }}
        >
            {sortedEvents.map((event) => (
                <FlexWidget
                    key={event.id}
                    style={{
                        width: 'match_parent',
                        paddingHorizontal: 8,
                        paddingVertical: 4,
                    }}
                    clickAction={isNarrow ? "OPEN_URI" : "TOGGLE_EVENT"}
                    clickActionData={isNarrow ? { uri: 'meusremedinhos://' } : { id: event.id }}
                >
                    <EventCardWidget event={event} widgetInfo={widgetInfo} />
                </FlexWidget>
            ))}
        </ListWidget>
    );
}