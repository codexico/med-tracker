import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { VitePWA } from 'vite-plugin-pwa'

import packageJson from './package.json'

// https://vitejs.dev/config/
export default defineConfig({
    base: './',
    define: {
        __APP_VERSION__: JSON.stringify(packageJson.version)
    },
    plugins: [
        react(),
        VitePWA({
            strategies: 'injectManifest',
            srcDir: 'src/service-worker',
            filename: 'sw.ts',
            devOptions: {
                enabled: true,
                type: 'module'
            },
            registerType: 'autoUpdate',
            includeAssets: ['favicon.ico', 'apple-touch-icon.png', 'mask-icon.svg'],
            manifest: {
                lang: 'pt-BR',
                name: 'Meus Remedinhos',
                short_name: 'Meus Remedinhos',
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
                ],
                screenshots: [
                    {
                        src: 'screenshot-mobile.png',
                        sizes: '1024x1024',
                        type: 'image/png'
                    },
                    {
                        src: 'screenshot-desktop.png',
                        sizes: '1024x1024',
                        type: 'image/png',
                        form_factor: 'wide'
                    }
                ]
            }
        })
    ],
    server: {
        host: true,
        port: 5173,
        watch: {
            usePolling: true
        }
    }
})
