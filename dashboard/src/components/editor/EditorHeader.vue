<template>
  <div class="bg-default/75 backdrop-blur border-b border-default h-(--ui-header-height) sticky top-0 z-50 w-full">
    <div class="flex items-center justify-between px-4 h-full">
      <div>
        <div class="flex items-center w-full" v-if="props.title == ''">
          <AppLogo class="h-6 w-auto" />
          <UBadge label="Alpha" color="success" variant="subtle" class="ml-2" />
        </div>

        <h2 class="font-bold text-xl">{{ props.title }}</h2>
      </div>

      <div>
        <UTooltip text="Report Issue on GitHub">
          <UButton variant="ghost" color="neutral" to="https://github.com/hypherionmc/fdd-editor" target="_blank" icon="i-lucide-github" />
        </UTooltip>

        <DownloadConfigModal v-if="!useEditor().getEmbedEditor"  />

        <UTooltip text="Close Editor">
          <UButton variant="ghost" color="neutral" icon="i-lucide-power" @click="pushReload()" />
        </UTooltip>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {useEditor} from "../../stores/editor.ts";
import DownloadConfigModal from "./DownloadConfigModal.vue";

interface Props {
  title?: string
}

const props = withDefaults(defineProps<Props>() ,{
  title: ''
})

const pushReload = () => {
  window.location.href = '/';
}
</script>
