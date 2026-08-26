<template>
  <div class="custom-collapse">
    <div class="flex items-center justify-between gap-2 border-b border-muted pb-2">
      <div @click="toggle" class="flex items-center justify-between w-full">
        <p class="w-full font-semibold">{{ props.collapseTitle }}</p>
        <UIcon :name="isOpen ? 'i-lucide-chevron-up' : 'i-lucide-chevron-down'" />
      </div>
      <slot name="button" class="w-full" />
    </div>

    <slot v-if="isOpen" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps(['collapseTitle'])

const isOpen = ref(false)

const toggle = () => {
  isOpen.value = !isOpen.value
}
</script>
