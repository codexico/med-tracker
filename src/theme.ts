import { createTheme, responsiveFontSizes } from '@mui/material/styles';

let theme = createTheme({
    palette: {
        mode: 'light',
        background: {
            default: '#f0d4bd',
            paper: '#ffffff',
        },
        primary: {
            main: '#8b6f47',
        },
        secondary: {
            main: '#d4a574',
        },
    },
    typography: {
        fontFamily: 'Seravek, \'Gill Sans Nova\', Ubuntu, Calibri, \'DejaVu Sans\', source-sans-pro, sans-serif',
        h1: {
            fontSize: '2rem',
            '@media (min-width:600px)': {
                fontSize: '3rem',
            },
        },
        h3: {
            fontSize: '1.75rem',
            '@media (min-width:600px)': {
                fontSize: '2.5rem',
            },
        },
        h5: {
            fontSize: '1.25rem',
            '@media (min-width:600px)': {
                fontSize: '1.5rem',
            },
        },
        h6: {
            fontSize: '1.1rem',
            '@media (min-width:600px)': {
                fontSize: '1.25rem',
            },
        },
    },
});

export default theme;
