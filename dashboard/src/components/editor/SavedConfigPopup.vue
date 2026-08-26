<template>
 <UModal v-model:open="isOpen" :title="useEditor().getEmbedEditor ? 'Your new Embed' : 'Your new config'" :transition="true" :overlay="true">
   <UTooltip text="View Config" v-if="!useEditor().getEmbedEditor">
     <UButton variant="ghost" color="neutral" target="_blank" icon="i-lucide-code" @click="saveConfigFile()" />
   </UTooltip>

   <template #description>
     <p class="text-xs text-muted">Here is your new {{ useEditor().getEmbedEditor ? "embed" : "config" }}. Ready to copy and paste</p>
   </template>

   <template #body>
     <highlightjs
       v-if="!useEditor().getEmbedEditor"
       language="toml"
       :code="useEditor().tomlConfig"
     />

     <highlightjs
       v-if="useEditor().getEmbedEditor"
       language="json"
       :code="useEditor().embedJson"
     />
   </template>

   <template #footer>
     <div class="flex items-center justify-end gap-2 w-full">
       <UButton
         variant="subtle"
         @click="copyToClipboard"
         size="lg"
         color="info"
         icon="i-lucide-copy"
         label="Copy to Clipboard" />

       <UButton
         variant="subtle"
         v-if="useEditor().isEmbedEditor"
         size="lg"
         @click="downloadFile(useEditor().embedJson, true)"
         label="Download"
         icon="i-lucide-download"
       />
     </div>
   </template>
 </UModal>
</template>
<script setup lang="ts">
import {useEditor} from "../../stores/editor.ts";
import {downloadFile, saveConfigFile} from "../../composables/EditorFunctions.ts";
import {useToast} from "@nuxt/ui/composables";
import {computed} from "vue";

const toast = useToast()

const isOpen = computed({
  get: () => useEditor().hasSavedConfig,

  set: (value) => {
    if (!value)
      useEditor().setTomlConfig(undefined)
  }
})

const copyToClipboard = async () => {
  await navigator.clipboard.writeText(useEditor().isEmbedEditor ? useEditor().embedJson : useEditor().getTomlConfig)
  toast.add({
    title: 'Copied to clipboard',
    color: 'info',
    duration: 2000
  })
}
</script>
