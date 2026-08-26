import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueRouter from 'vue-router/vite'
import ui from '@nuxt/ui/vite'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vueRouter({
      dts: 'src/route-map.d.ts'
    }),
    vue(),
    ui({
      theme: {

      }
    })
  ],
  build: {
    outDir: '../Common/src/main/resources/assets/sdlink/frontend',
    emptyOutDir: true
  },
  base: ""
})
