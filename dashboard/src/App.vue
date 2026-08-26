<script setup lang="ts">
import {computed } from 'vue'
import { useHead } from '@unhead/vue'
import { useColorMode } from '@vueuse/core'
import {useAppState} from "./stores/appstate.ts";
import {useEditor} from "./stores/editor.ts";

const colorMode = useColorMode()
const themeColor = computed(() => colorMode.value === 'dark' ? '#04002A' : '#ECEDF0')

useHead({
  meta: [
    { name: 'theme-color', content: themeColor }
  ]
})
</script>

<template>
  <Suspense>
    <UApp>
      <!-- Splash Screen -->
      <LoadingSplash v-if="useAppState().getSplashScreen && !useEditor().getEmbedEditor" />
      <NoConfigSplash v-if="!useEditor().isConfigLoaded && !useAppState().getSplashScreen && !useEditor().getEmbedEditor" />
      <FirstRunModal />

      <RouterView />

      <UColorModeButton
        class="fixed z-10 right-4 bottom-4 rounded-full"
        variant="soft"
        :color="colorMode == 'dark' ? 'warning' : 'info'"
        size="xl"
      />
    </UApp>
  </Suspense>
</template>
