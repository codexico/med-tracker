"use no memo"
import React from 'react';
import { FlexWidget, IconWidget } from 'react-native-android-widget';
import type { HexColor } from 'react-native-android-widget/src/widgets/utils/style.props';

export function WidgetCheckbox({ checked, color }: { checked: boolean, color: string }) {
  return (
    <FlexWidget
      style={{
        height: 24,
        width: 24,
        borderRadius: 4,
        borderWidth: 2,
        borderColor: (checked ? color : '#657786') as HexColor,
        backgroundColor: (checked ? color : 'transparent') as HexColor,
        justifyContent: 'center',
        alignItems: 'center',
      }}
    >
      {checked && (
        <IconWidget
          icon="check"
          size={16}
          style={{ color: '#FFFFFF' as HexColor }}
          font="material"
        />
      )}
    </FlexWidget>
  );
}
