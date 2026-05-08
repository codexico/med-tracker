import React from 'react';
import { View, Text, StyleSheet, ScrollView, Image, TouchableOpacity, Linking, Platform } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { Ionicons } from '@expo/vector-icons';
import i18n from '@/i18n';
import { COLORS } from '@/constants/theme';
import Constants from 'expo-constants';

export default function AboutScreen() {
    const handleGithubPress = () => {
        Linking.openURL('https://github.com/codexico/med-tracker');
    };

    const appVersion = Constants.expoConfig?.version || '2.0.0';

    return (
        <SafeAreaView style={styles.container}>
            <ScrollView contentContainerStyle={styles.scrollContent}>
                {/* Header */}
                <View style={styles.header}>
                    <Ionicons name="medical" size={48} color={COLORS.primary} />
                    <Text style={styles.title}>{i18n.t('appName')}</Text>
                </View>

                {/* App Description */}
                <View style={styles.section}>
                    <Text style={styles.descriptionText}>
                        {i18n.t('appDescription')}
                    </Text>
                </View>

                {/* Widget Promotion Section */}
                {Platform.OS === 'android' && (
                    <View style={styles.widgetPromoContainer}>
                        <View style={styles.widgetPromoHeader}>
                            <Ionicons name="grid-outline" size={24} color={COLORS.primary} />
                            <Text style={styles.widgetPromoTitle}>{i18n.t('widgetPromoTitle')}</Text>
                        </View>
                        
                        <Text style={styles.widgetPromoDesc}>
                            {i18n.t('widgetPromoDesc')}
                        </Text>

                        <View style={styles.imageContainer}>
                            <Image 
                                source={require('@/assets/widget-preview/next-event-preview.png')} 
                                style={styles.widgetImageSmall} 
                                resizeMode="contain"
                            />
                            <Image 
                                source={require('@/assets/widget-preview/daily-list-preview.png')} 
                                style={styles.widgetImageLarge} 
                                resizeMode="contain"
                            />
                        </View>

                        <View style={styles.stepsContainer}>
                            <Text style={styles.stepText}>{i18n.t('widgetPromoStep1')}</Text>
                            <Text style={styles.stepText}>{i18n.t('widgetPromoStep2')}</Text>
                            <Text style={styles.stepText}>{i18n.t('widgetPromoStep3')}</Text>
                        </View>
                    </View>
                )}

                {/* Footer Links & Info */}
                <View style={styles.footer}>
                    <TouchableOpacity style={styles.linkButton} onPress={handleGithubPress}>
                        <Ionicons name="logo-github" size={24} color={COLORS.text} />
                        <Text style={styles.linkText}>{i18n.t('githubLink')}</Text>
                    </TouchableOpacity>

                    <Text style={styles.versionText}>
                        {i18n.t('version')} {appVersion}
                    </Text>
                </View>
            </ScrollView>
        </SafeAreaView>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: COLORS.background,
    },
    scrollContent: {
        padding: 24,
        paddingBottom: 40,
    },
    header: {
        alignItems: 'center',
        marginBottom: 24,
    },
    title: {
        fontSize: 28,
        fontWeight: 'bold',
        color: COLORS.text,
        marginTop: 12,
    },
    section: {
        marginBottom: 32,
    },
    descriptionText: {
        fontSize: 16,
        color: COLORS.textSecondary,
        textAlign: 'center',
        lineHeight: 24,
    },
    widgetPromoContainer: {
        backgroundColor: COLORS.surface,
        borderRadius: 16,
        padding: 20,
        marginBottom: 32,
        shadowColor: '#000',
        shadowOffset: {
            width: 0,
            height: 2,
        },
        shadowOpacity: 0.1,
        shadowRadius: 4,
        elevation: 3,
    },
    widgetPromoHeader: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 12,
    },
    widgetPromoTitle: {
        fontSize: 20,
        fontWeight: '600',
        color: COLORS.text,
        marginLeft: 8,
    },
    widgetPromoDesc: {
        fontSize: 15,
        color: COLORS.textSecondary,
        lineHeight: 22,
        marginBottom: 20,
    },
    imageContainer: {
        flexDirection: 'row',
        justifyContent: 'space-around',
        alignItems: 'center',
        marginBottom: 20,
        backgroundColor: COLORS.background,
        borderRadius: 12,
        padding: 12,
    },
    widgetImageSmall: {
        width: 120,
        height: 60,
        borderRadius: 8,
    },
    widgetImageLarge: {
        width: 140,
        height: 140,
        borderRadius: 8,
    },
    stepsContainer: {
        gap: 8,
    },
    stepText: {
        fontSize: 14,
        color: COLORS.text,
        lineHeight: 20,
    },
    footer: {
        alignItems: 'center',
        marginTop: 'auto',
        paddingTop: 24,
        borderTopWidth: 1,
        borderTopColor: COLORS.border,
    },
    linkButton: {
        flexDirection: 'row',
        alignItems: 'center',
        padding: 12,
        backgroundColor: COLORS.surface,
        borderRadius: 8,
        marginBottom: 16,
    },
    linkText: {
        fontSize: 16,
        fontWeight: '500',
        color: COLORS.text,
        marginLeft: 8,
    },
    versionText: {
        fontSize: 14,
        color: COLORS.textSecondary,
    },
});
