import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [react()],
  // strictPort: fail loudly if 5173 is taken, instead of silently moving to another port the browser does not know
  server: { port: 5173, strictPort: true, proxy: { '/api': 'http://localhost:8080' } },
})