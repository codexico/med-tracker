import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.tsx'
import { CssBaseline, ThemeProvider } from '@mui/material'
import { registerSW } from 'virtual:pwa-register'
import theme from './theme'

registerSW({
    immediate: true,
    onRegistered(r) {
        console.log('SW Registered:', r);
    },
    onRegisterError(error) {
        console.error('SW Registration error:', error);
    }
})

ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <ThemeProvider theme={theme}>
            <CssBaseline />
            <App />
        </ThemeProvider>
    </React.StrictMode>,
)
