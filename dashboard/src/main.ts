import router from './router'
import { createApp } from 'vue'
import { handleHotUpdate } from 'vue-router/auto-routes'
import { createHead } from '@unhead/vue/client'
import ui from '@nuxt/ui/vue-plugin'
import { install as DiscordMessageComponents } from '@discord-message-components/vue'

import 'highlight.js/styles/stackoverflow-dark.css'
import '@discord-message-components/vue/dist/style.css'
import './assets/css/main.css'

import hljs from 'highlight.js/lib/core';
import yaml from 'highlight.js/lib/languages/yaml';
import hljsVuePlugin from "@highlightjs/vue-plugin";

hljs.registerLanguage('toml', yaml);

import App from './App.vue'
import {createPinia} from "pinia";

const app = createApp(App)

const head = createHead()
const pinia = createPinia()

app.use(head)
app.use(router)
app.use(ui)
app.use(pinia)
app.use(hljsVuePlugin)
app.use(DiscordMessageComponents, {
  defaultTheme: 'light'
})

app.mount('#app')

if (import.meta.hot) {
  handleHotUpdate(router)
}
