import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { VitePWA } from 'vite-plugin-pwa'
import basicSsl from '@vitejs/plugin-basic-ssl'

// https://vitejs.dev/config/
export default defineConfig({
    base: './',
    plugins: [
        react(),
        basicSsl(),
        VitePWA({
            devOptions: {
                enabled: true
            },
            registerType: 'autoUpdate',
            includeAssets: ['favicon.ico', 'apple-touch-icon.png', 'mask-icon.svg'],
            manifest: {
                lang: 'pt-BR',
                name: 'MedTracker',
                short_name: 'MedTracker',
                description: 'Acompanhe seus medicamentos diários',
                theme_color: '#cdab8f',
                background_color: '#ffffff',
                display: 'standalone',
                start_url: '/git/medtracker/dist/index.html',
                orientation: 'portrait',
                icons: [
                    {
                        src: 'pwa-192x192.png',
                        sizes: '192x192',
                        type: 'image/png',
                        purpose: 'any'
                    },
                    {
                        src: 'pwa-512x512.png',
                        sizes: '512x512',
                        type: 'image/png',
                        purpose: 'any'
                    },
                    {
                        src: 'pwa-512x512.png',
                        sizes: '512x512',
                        type: 'image/png',
                        purpose: 'maskable'
                    }
                ]
            }
        })
    ],
    server: {
        host: true,
        port: 5173,
        https: true,
        watch: {
            usePolling: true
        }
    }
})
