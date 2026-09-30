import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      '/api/products': {
        target: 'http://localhost:8081',
        changeOrigin: true
      },
      '/api/inventory': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/api/orders': {
        target: 'http://localhost:8083',
        changeOrigin: true
      },
      '/api/payment': {
        target: 'http://localhost:8084',
        changeOrigin: true
      },
      '/api/notifications': {
        target: 'http://localhost:8085',
        changeOrigin: true
      }
    }
  }
})
