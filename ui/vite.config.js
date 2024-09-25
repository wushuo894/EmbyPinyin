import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
    base: './',
    server: {
        proxy: {
            '/api': {
                target: 'http://127.0.0.1:9198',
                changeOrigin: true,
            }
        }
    },
    plugins: [vue()],
})
