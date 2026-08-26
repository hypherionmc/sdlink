<script setup lang="ts">
import {computed, ref} from 'vue'
import {useEditor} from "../../stores/editor.ts";
import {headerToDisplay} from "../../composables/FieldUtils.ts";

const props = defineProps<{
  node: any
  path: string[]
}>()

const isOpen = ref(false)

const keys = computed(() => {
  if (!props.node) return []
  if (typeof props.node !== 'object') return []
  return Object.keys(props.node).filter(k => k && k.length > 0)
})

function isExpandNode(node: any) {
  const expandNodes = ["channels"]
  return expandNodes.includes(node)
}

function isObject(v: any) {
  return v && typeof v === 'object' && !Array.isArray(v)
}

function open(key: string) {
  useEditor().setCurrentSection([...props.path, key])
}

const isActive = (path: string[]) => {
  const current = useEditor().currentSection
  return path.every((p, i) => current[i] === p)
}
</script>

<template>
  <div v-if="node" class="flex flex-col gap-1">
    <div v-for="key in keys" :key="key" class="w-full">
      <UCollapsible v-model:open="isOpen" v-if="isObject(node[key]) && isExpandNode(key)">
        <UButton variant="ghost" size="md" :color="isActive([...props.path, key]) ? 'info' : 'neutral'" class="w-full" :trailing-icon="isOpen ? 'i-lucide-chevron-up' : 'i-lucide-chevron-down'" block>
          {{ headerToDisplay(key) }}
        </UButton>

        <template #content>
          <SidebarNode
            class="ml-3"
            :node="node[key]"
            :path="[...path, key]"
          />
        </template>
      </UCollapsible>

      <UButton v-else variant="ghost" size="md" :color="isActive([...props.path, key]) ? 'info' : 'neutral'" @click="open(key)" class="w-full">
        {{ headerToDisplay(key) }}
      </UButton>

    </div>
  </div>
</template>
