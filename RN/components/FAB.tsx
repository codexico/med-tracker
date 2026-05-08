import React from 'react';
import { StyleSheet, Pressable, ViewStyle, StyleProp } from 'react-native';
import { MaterialIcons } from '@expo/vector-icons';
import { COLORS } from '@/constants/theme';

interface FABProps {
  onPress: () => void;
  style?: StyleProp<ViewStyle>;
  icon?: keyof typeof MaterialIcons.glyphMap;
}

export function FAB({ onPress, style, icon = 'add' }: FABProps) {
  return (
    <Pressable
      style={({ pressed }) => [
        styles.fab,
        { opacity: pressed ? 0.8 : 1 },
        style,
      ]}
      onPress={onPress}
    >
      <MaterialIcons name={icon} size={28} color={COLORS.white} />
    </Pressable>
  );
}

const styles = StyleSheet.create({
  fab: {
    position: 'absolute',
    bottom: 36,
    right: 24,
    width: 60,
    height: 60,
    borderRadius: 30,
    backgroundColor: COLORS.primary,
    justifyContent: 'center',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 4,
    },
    shadowOpacity: 0.3,
    shadowRadius: 4.65,
    elevation: 8,
    zIndex: 1000,
  },
});
