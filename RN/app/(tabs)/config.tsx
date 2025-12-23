import React from 'react';
import i18n from '@/i18n';
import { EventConfigurationView } from '@/components/EventConfigurationView';

export default function ConfigScreen() {
    return (
        <EventConfigurationView
            title={i18n.t('config')}
            subtitle={i18n.t('configSubtitle')}
        />
    );
}
