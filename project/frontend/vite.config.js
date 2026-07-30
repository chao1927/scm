import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  build: {
    chunkSizeWarningLimit: 900,
  },
  server: {
    port: 5173,
    proxy: {
      '/api/iam': {
        target: process.env.IAM_API || 'http://127.0.0.1:8097',
        changeOrigin: true,
      },
      '/openapi/iam': {
        target: process.env.IAM_API || 'http://127.0.0.1:8097',
        changeOrigin: true,
      },
      '/api/mdm': {
        target: process.env.MDM_API || 'http://127.0.0.1:8098',
        changeOrigin: true,
      },
      '/api/bms': {
        target: process.env.BMS_API || 'http://127.0.0.1:8110',
        changeOrigin: true,
      },
      '/api/tms': {
        target: process.env.TMS_API || 'http://127.0.0.1:8100',
        changeOrigin: true,
      },
      '/api/oms': {
        target: process.env.OMS_API || 'http://127.0.0.1:8099',
        changeOrigin: true,
      },
      '/api/inventory': {
        target: process.env.INVENTORY_API || 'http://127.0.0.1:8104',
        changeOrigin: true,
      },
      '/api/wms': {
        target: process.env.WMS_API || 'http://127.0.0.1:8103',
        changeOrigin: true,
      },
      '/api/purchase': {
        target: process.env.PURCHASE_API || 'http://127.0.0.1:8102',
        changeOrigin: true,
      },
      '/api/supplier': {
        target: process.env.SUPPLIER_API || 'http://127.0.0.1:8101',
        changeOrigin: true,
      },
    },
  },
})
