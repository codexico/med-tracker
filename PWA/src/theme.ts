import { createTheme, responsiveFontSizes } from '@mui/material/styles';

let theme = createTheme({
    shape: {
        borderRadius: 16,
    },
    palette: {
        mode: 'light',
        background: {
            default: '#f0d4bd',
            paper: '#ffffff',
        },
        primary: {
            main: '#8b6f47',
            light: '#d4a574',
        },
        secondary: {
            main: '#d4a574',
        },
        text: {
            primary: '#2d241b',
            secondary: '#6d5d4b',
        }
    },
    typography: {
        fontFamily: "'Outfit', 'Seravek', 'Gill Sans Nova', Ubuntu, Calibri, sans-serif",
        h1: {
            fontSize: '2rem',
            fontWeight: 700,
            '@media (min-width:600px)': {
                fontSize: '3rem',
            },
        },
        h3: {
            fontSize: '1.75rem',
            fontWeight: 600,
            '@media (min-width:600px)': {
                fontSize: '2.5rem',
            },
        },
        h4: {
            fontSize: '1.5rem',
            fontWeight: 600,
            '@media (min-width:600px)': {
                fontSize: '2rem',
            },
        },
        h5: {
            fontSize: '1.25rem',
            fontWeight: 600,
            '@media (min-width:600px)': {
                fontSize: '1.5rem',
            },
        },
        h6: {
            fontSize: '1.1rem',
            fontWeight: 500,
            '@media (min-width:600px)': {
                fontSize: '1.25rem',
            },
        },
        button: {
            textTransform: 'none',
            fontWeight: 600,
        }
    },
    components: {
        MuiButton: {
            styleOverrides: {
                root: {
                    borderRadius: 12,
                    boxShadow: 'none',
                    '&:hover': {
                        boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
                    }
                }
            }
        },
        MuiPaper: {
            styleOverrides: {
                rounded: {
                    borderRadius: 16,
                }
            }
        }
    }
});

theme = responsiveFontSizes(theme);
export default theme;
