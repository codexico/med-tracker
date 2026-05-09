'use no memo';
import React from 'react';
import { Text, View } from 'react-native';

import i18n from '@/i18n';
import { EventConfigurationView } from '@/components/EventConfigurationView';
import { COLORS } from '@/constants/theme';

export default function ConfigScreen() {
    return (

        <EventConfigurationView
            title={i18n.t('config')}
            subtitle={
                (<View style={{ gap: 8, marginTop: 8 }}>
                    <Text style={{ fontSize: 16, color: COLORS.textSecondary, marginBottom: 4 }}>
                        {i18n.t('initialScreenSubtitle')}
                    </Text>

                    <View style={{ gap: 4 }}>
                        <Text style={{ fontSize: 14, color: COLORS.text }}>{i18n.t('initialInstructionsStep1')}</Text>
                        <Text style={{ fontSize: 14, color: COLORS.text }}>{i18n.t('initialInstructionsStep2')}</Text>
                        <Text style={{ fontSize: 14, color: COLORS.text }}>{i18n.t('initialInstructionsStep3')}</Text>
                    </View>

                    <Text style={{ fontSize: 13, color: COLORS.primary, marginTop: 4, fontStyle: 'italic' }}>
                        {i18n.t('initialInstructionsNote')}
                    </Text>
                </View>)
            }
        />

    );
}
