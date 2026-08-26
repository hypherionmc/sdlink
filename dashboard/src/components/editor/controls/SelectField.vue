<template>
  <div>
    <p class="text-sm mb-1 pl-1">{{ label }}</p>
    <USelect
      v-model="model"
      class="w-full"
      variant="subtle"
      size="lg"
      :items="items"
    />
  </div>
</template>

<script setup lang="ts">
const model = defineModel<any>()

defineProps<{
  label?: string
  items: any[]
}>()
</script>
