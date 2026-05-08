import React from 'react';
import type { WidgetTaskHandlerProps } from 'react-native-android-widget';
import { requestWidgetUpdate } from 'react-native-android-widget';

import { DailyListWidget } from './DailyListWidget';
import { NextEventWidget } from './NextEventWidget';
import { getEvents, toggleEventCompletion } from '@/services/Database';

const nameToWidget = {
    DailyList: DailyListWidget,
    NextEvent: NextEventWidget,
};

export async function widgetTaskHandler(props: WidgetTaskHandlerProps) {
    const widgetInfo = props.widgetInfo;
    const Widget =
        nameToWidget[widgetInfo.widgetName as keyof typeof nameToWidget];
    console.log({ props });

    try {
        switch (props.widgetAction) {
            case 'WIDGET_ADDED':
            case 'WIDGET_UPDATE':
            case 'WIDGET_RESIZED': {
                const events = await getEvents();
                props.renderWidget(<Widget events={events} widgetInfo={props.widgetInfo} />);
                break;
            }

            case 'WIDGET_DELETED':
                // Not needed for now
                break;

            case 'WIDGET_CLICK': {
                const id = props.clickActionData?.id as string;
                
                if (props.clickAction === 'TOGGLE_EVENT' && id) {
                    const events = await getEvents();
                    const eventToToggle = events.find(e => e.id === id);
                    if (eventToToggle) {
                        await toggleEventCompletion(id, !eventToToggle.completedToday);
                        
                        const newEvents = await getEvents();
                        
                        // Update the clicked widget immediately
                        props.renderWidget(<Widget events={newEvents} widgetInfo={props.widgetInfo} />);
                        
                        // Update all instances of the other widget (and potentially other instances of the same widget)
                        await requestWidgetUpdate({
                            widgetName: 'DailyList',
                            renderWidget: (info) => <DailyListWidget events={newEvents} widgetInfo={info} />,
                        });
                        
                        await requestWidgetUpdate({
                            widgetName: 'NextEvent',
                            renderWidget: (info) => <NextEventWidget events={newEvents} widgetInfo={info} />,
                        });
                    }
                }
                break;
            }

            default:
                break;
        }
    } catch (error) {
        console.error('Widget task error:', error);
    }
}