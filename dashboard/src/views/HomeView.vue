<template>
  <div class="flex flex-1">
    <USidebar variant="sidebar" v-if="useEditor().isConfigLoaded">
      <template #title>
       <div class="flex items-center gap-2">
         <AppLogo />
         <UBadge variant="subtle" size="sm" color="warning" label="Alpha" />
       </div>
      </template>

      <div class="flex flex-col cursor-pointer overflow-x-auto" v-if="useEditor().isConfigLoaded">
        <SidebarNode
          v-if="useEditor().getConfig?.config"
          :node="useEditor().getConfig.config"
          :path="[]"
        />
      </div>
    </USidebar>

    <div class="flex-1 flex flex-col w-full">
      <EditorHeader :title="sectionTitle" v-if="useEditor().isConfigLoaded || useEditor().getEmbedEditor" class="w-full" />

      <div class="content-container" v-if="useEditor().isConfigLoaded">
        <div class="rounded-lg relative">
          <div class="editor-body mt-5 flex gap-2 flex-col">
            <ConfigNodeEditor
              :value="getByPath(useEditor().getConfig.config, useEditor().currentSection)"
              :comments="getByPath(useEditor().getConfig.comments, useEditor().currentSection)"
              :path="useEditor().currentSection"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {useEditor} from "../stores/editor.ts";
import {headerToDisplay} from "../composables/FieldUtils.ts";
import SidebarNode from "../components/editor/SidebarNode.vue";
import {useWebsocketClient} from "../composables/websocketClient.ts";

const props = defineProps(['id'])
const websocket = useWebsocketClient()

function getByPath(obj: any, path: string[]) {
  return path.reduce((acc, key) => {
    if (!acc) return undefined
    return acc[key]
  }, obj)
}

if (props.id) {
  websocket.connect(props.id)
}

const sectionTitle = computed(() => {
  return headerToDisplay(useEditor().currentSection.join(' >> '))
})
</script>
