<template>
  <UModal v-model:open="isOpen" title="Your new config" :transition="true" :overlay="true">
    <UTooltip text="Download Config">
      <UButton variant="ghost" color="neutral" target="_blank" icon="i-lucide-save" @click="saveConfigFile(true)" />
    </UTooltip>

    <template #body>
      <p class="text-sm">Your config is now ready to download. You can choose between downloading your file before you made changes, or downloading the modified config file</p>
    </template>

    <template #footer>
      <div class="flex items-center justify-end gap-2 w-full">
        <UButton label="Download Original" variant="subtle" color="warning" size="lg" @click="downloadFile(useEditor().hasDownloadConfig.original)" />
        <UButton label="Download Modified" variant="subtle" color="success" size="lg" @click="downloadFile(useEditor().hasDownloadConfig.modified)" />
      </div>
    </template>
  </UModal>
</template>
<script setup lang="ts">
import {downloadFile, saveConfigFile} from "../../composables/EditorFunctions.ts";
import {computed} from "vue";
import {useEditor} from "../../stores/editor.ts";

const isOpen = computed({
  get: () => useEditor().hasDownloadConfig.isSaved,

  set: (value) => {
    if (!value)
      useEditor().setDownloadConfig(false, '')
  }
})
</script>
