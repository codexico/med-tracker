import { StyleSheet } from 'react-native';
import { COLORS } from './theme';

export const commonStyles = StyleSheet.create({
    container: {
        flex: 1,
    },
    headerFixed: {
        marginHorizontal: 24,
    },
    header: {
        paddingHorizontal: 8,
        marginBottom: 10,
        flexDirection: 'column',
    },
    title: {
        fontSize: 28,
        fontWeight: 'bold',
    },
    headerLogo: {
        width: 150,
        height: 40,
    },
    content: {
        paddingHorizontal: 24,
    },
    card: {
        paddingHorizontal: 12,
        paddingTop: 12,
        paddingBottom: 8,
        borderRadius: 16,
        marginBottom: 8,
        shadowColor: COLORS.shadow,
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.05,
        shadowRadius: 4,
        elevation: 2,
    },
    cardHeader: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        minHeight: 40,
        marginBottom: 8,
    },
    modalOverlay: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: COLORS.overlay,
    },
    modalView: {
        margin: 20,
        borderRadius: 20,
        padding: 35,
        alignItems: 'center',
        shadowColor: COLORS.shadow,
        shadowOffset: {
            width: 0,
            height: 2,
        },
        shadowOpacity: 0.25,
        shadowRadius: 4,
        elevation: 5,
        width: '80%',
        backgroundColor: COLORS.surface,
    },
    modalTitle: {
        marginBottom: 15,
        textAlign: 'center',
        fontSize: 18,
        fontWeight: 'bold',
    },
    input: {
        height: 40,
        width: '100%',
        margin: 12,
        borderWidth: 1,
        borderRadius: 8,
        padding: 10,
    },
    modalButtons: {
        flexDirection: 'row',
        gap: 12,
        width: '100%',
        justifyContent: 'center',
        marginTop: 10,
    },
    button: {
        borderRadius: 10,
        padding: 10,
        elevation: 2,
        minWidth: 100,
        alignItems: 'center',
    },
    buttonClose: {
        backgroundColor: COLORS.white,
        borderWidth: 1,
        borderColor: COLORS.textSecondary,
    },
    textStyle: {
        color: COLORS.white,
        fontWeight: 'bold',
        textAlign: 'center',
    },

});
