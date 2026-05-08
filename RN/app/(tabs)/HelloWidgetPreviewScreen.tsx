import * as React from 'react';
import { StyleSheet, View } from 'react-native';
import { WidgetPreview } from 'react-native-android-widget';

import { NextEventWidget } from '../../widgets/NextEventWidget';
import { useEventContext } from '@/context/EventContext';

export default function HelloWidgetPreviewScreen() {
    const { events } = useEventContext();
    return (
        <View style={styles.container}>
            <WidgetPreview
                renderWidget={() => <NextEventWidget events={events} />}
                width={320}
                height={200}
            />
            <WidgetPreview
                renderWidget={() => <NextEventWidget events={events} widgetInfo={
                    { width: 160, height: 160 } as any
                } />} //
                width={160}
                height={160}

            />
            <WidgetPreview
                renderWidget={() => <NextEventWidget events={events}
                    widgetInfo={{ "height": 104, "screenInfo": { "density": 2.625, "densityDpi": 420, "screenHeightDp": 923, "screenWidthDp": 411 }, "widgetId": 3, "widgetName": "NextEvent", "width": 172 }} />} //
                width={172}
                height={104}

            />
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
    },
});