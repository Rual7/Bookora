import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [react()],
  // one .env in the project root for both parts; only variables that start with VITE_ reach the browser
  envDir: '..',
  // strictPort: fail loudly if 5173 is taken, instead of silently moving to another port the browser does not know
  server: { port: 5173, strictPort: true, proxy: { '/api': 'http://localhost:8080', '/sitemap.xml': 'http://localhost:8080', '/robots.txt': 'http://localhost:8080' } },
})