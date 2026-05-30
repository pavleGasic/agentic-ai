import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/invoices': 'http://localhost:8080',
      '/logs': 'http://localhost:8080',
    },
  },
})
