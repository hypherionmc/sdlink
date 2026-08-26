<template>
  <div class="grid grid-cols-3 w-full gap-2 items-center">
    <div>
      <p class="text-sm mb-1 pl-1">Status</p>
      <UInput
        variant="subtle"
        size="lg"
        class="w-full"
        v-model="model.status"
      />
    </div>

    <SelectField
      v-model="model.botStatusType"
      :items="items"
      label="Status Type"
    />

    <div>
      <p class="text-sm mb-1 pl-1">Bot Status Streaming URL</p>
      <div class="flex gap-1">
        <UInput
          variant="subtle"
          size="lg"
          v-model="model.botStatusStreamingURL"
          class="w-full"
        />

        <UButton
          @click="$emit('delete')"
          variant="ghost"
          color="error"
          icon="i-lucide-trash"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineEmits(['delete'])

const model = defineModel<Record<string, any>>({ required: true })

defineProps<{
  items: any[]
}>()
</script>
