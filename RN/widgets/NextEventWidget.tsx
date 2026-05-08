"use no memo"
import React from 'react';
import { FlexWidget, TextWidget, ListWidget, WidgetInfo } from 'react-native-android-widget';
import { MedEvent } from '@/types';
import { EventCardWidget } from './EventCardWidget';
import { COLORS } from '@/constants/theme';
import type { HexColor } from 'react-native-android-widget/src/widgets/utils/style.props';
import i18n from '@/i18n';

interface NextEventWidgetProps {
    events: MedEvent[];
    widgetInfo?: WidgetInfo;
}

export function NextEventWidget({ events = [], widgetInfo }: NextEventWidgetProps) {
    const enabledEvents = events.filter(e => e.enabled).sort((a, b) => a.time.localeCompare(b.time));
    const isNarrow = widgetInfo?.width ? widgetInfo.width <= 172 : false;
    console.log(widgetInfo);

    if (enabledEvents.length === 0) {
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
                    text={i18n.t('noEvents')}
                    style={{ color: COLORS.textSecondary as HexColor, fontSize: 16 }}
                />
            </FlexWidget>
        );
    }

    const pendingEvent = enabledEvents.find(e => !e.completedToday);
    const nextEvent = pendingEvent || enabledEvents[0];

    return (
        <ListWidget
            style={{
                height: 'match_parent',
                width: 'match_parent',

            }}
        >
            <FlexWidget
                key={1}
                style={{
                    backgroundColor: COLORS.background as HexColor,
                    borderRadius: 16,
                    width: 'match_parent',
                    paddingHorizontal: 8,
                    paddingVertical: 4,
                    marginHorizontal: 8,
                    marginVertical: 4,
                    justifyContent: `center`,
                    alignItems: `center`,
                    height: `wrap_content`,
                }}
                clickAction={isNarrow ? "OPEN_URI" : "TOGGLE_EVENT"}
                clickActionData={isNarrow ? { uri: 'meusremedinhos://' } : { id: nextEvent.id }}
            >
                <EventCardWidget
                    event={nextEvent}
                    widgetInfo={widgetInfo}
                />
            </FlexWidget>
        </ListWidget>
    );
}
