import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// In dev, calls to /api are forwarded to the Spring backend (no CORS needed).
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: { '/api': 'http://localhost:8080' },
  },
})
