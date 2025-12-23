import React from 'react';
import { Pressable, Text, View } from 'react-native';
import { router } from 'expo-router';

import { COLORS } from '@/constants/theme';
import { commonStyles } from '@/constants/commonStyles';
import { saveSetting } from '@/services/Database';
import { requestPermissions } from '@/services/Notifications';
import i18n from '@/i18n';

import { EventConfigurationView } from '@/components/EventConfigurationView';

export default function InitialScreen() {
    const handleFinishOnboarding = async () => {
        console.log('Finishing onboarding...');
        await requestPermissions();
        await saveSetting('hasCompletedOnboarding', 'true');
        console.log('Navigating to tabs...');
        router.replace('/(tabs)/' as any);
    };

    return (
        <EventConfigurationView
            title={i18n.t('welcome')}
            subtitle={
                <View style={{ gap: 8, marginTop: 8 }}>
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
                </View>
            }
            footerContent={
                <Pressable
                    style={[commonStyles.button, { backgroundColor: COLORS.secondary, width: '100%', marginBottom: 24 }]}
                    onPress={handleFinishOnboarding}
                >
                    <Text style={commonStyles.textStyle}>{i18n.t('finishOnboarding')}</Text>
                </Pressable>
            }
        />
    );
}
