/**
 * Below are the colors that are used in the app. The colors are defined in the light and dark mode.
 * There are many other ways to style your app. For example, [Nativewind](https://www.nativewind.dev/), [Tamagui](https://tamagui.dev/), [unistyles](https://reactnativeunistyles.vercel.app), etc.
 */

import { Platform } from 'react-native';

const primary = '#8b6f47';
const secondary = '#d4a574';
const background = '#f0d4bd';
const surface = '#ffffff';
const textPrimary = '#2d241b';
const textSecondary = '#6d5d4b';

export const Colors = {
  light: {
    text: textPrimary,
    textSecondary: textSecondary,
    background: background,
    surface: surface,
    tint: primary,
    icon: textSecondary,
    tabIconDefault: textSecondary,
    tabIconSelected: primary,
    primary: primary,
    secondary: secondary,
    error: '#d32f2f',
  },
  dark: {
    // For now, using same as light or similar, as PWA was only light.
    // We can adjust this later for true dark mode.
    text: textPrimary,
    textSecondary: textSecondary,
    background: background,
    surface: surface,
    tint: primary,
    icon: textSecondary,
    tabIconDefault: textSecondary,
    tabIconSelected: primary,
    primary: primary,
    secondary: secondary,
    error: '#ef5350',
  },
};

export const Fonts = Platform.select({
  ios: {
    /** iOS `UIFontDescriptorSystemDesignDefault` */
    sans: 'system-ui',
    /** iOS `UIFontDescriptorSystemDesignSerif` */
    serif: 'ui-serif',
    /** iOS `UIFontDescriptorSystemDesignRounded` */
    rounded: 'ui-rounded',
    /** iOS `UIFontDescriptorSystemDesignMonospaced` */
    mono: 'ui-monospace',
  },
  default: {
    sans: 'normal',
    serif: 'serif',
    rounded: 'normal',
    mono: 'monospace',
  },
  web: {
    sans: "system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif",
    serif: "Georgia, 'Times New Roman', serif",
    rounded: "'SF Pro Rounded', 'Hiragino Maru Gothic ProN', Meiryo, 'MS PGothic', sans-serif",
    mono: "SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace",
  },
});
