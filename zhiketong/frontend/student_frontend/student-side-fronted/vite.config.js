import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0',
    port: 5174,
    proxy: {
      // ---- 队友后端 (8082) ----
      '/api/user':        { target: 'http://localhost:8082', changeOrigin: true },
      '/api/class':       { target: 'http://localhost:8082', changeOrigin: true },
      '/api/teacher':     { target: 'http://localhost:8082', changeOrigin: true },
      '/api/leaderboard': { target: 'http://localhost:8082', changeOrigin: true },
      '/api/knowledge':   { target: 'http://localhost:8082', changeOrigin: true },
      '/api/report':      { target: 'http://localhost:8082', changeOrigin: true },
      '/api/ai':          { target: 'http://localhost:8082', changeOrigin: true },
      '/api/mistakes':    { target: 'http://localhost:8082', changeOrigin: true },
      '/api/answer':      { target: 'http://localhost:8082', changeOrigin: true },
      '/api/favorites':   { target: 'http://localhost:8082', changeOrigin: true },
      '/api/history':     { target: 'http://localhost:8082', changeOrigin: true },

      // ---- 你的后端 (8080) ----
      '/api/knowledge-points':   { target: 'http://localhost:8080', changeOrigin: true },
      '/api/exercises':          { target: 'http://localhost:8080', changeOrigin: true },
      '/api/homeworks':          { target: 'http://localhost:8080', changeOrigin: true },
      '/api/teaching-reports':   { target: 'http://localhost:8080', changeOrigin: true },
      '/api/questions/random':   { target: 'http://localhost:8082', changeOrigin: true },
      '/api/questions':          { target: 'http://localhost:8080', changeOrigin: true },

      // ---- 静态资源 ----
      '/uploads':                { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
})
