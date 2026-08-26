<template>
  <div class="grid grid-cols-1 w-full gap-2 items-center">
    <div class="flex items-center gap-2">
      <div class="w-full">
        <p class="text-sm mb-1 pl-1">Rank/Group</p>
        <UInput
          variant="subtle"
          size="lg"
          v-model="model.rank"
          class="w-full"
        />
      </div>

      <div class="w-full">
        <p class="text-sm mb-1 pl-1">Discord Role</p>
        <UInput
          variant="subtle"
          size="lg"
          v-model="model.role"
          class="w-full"
        />
      </div>

      <div>
        <p>&nbsp;</p>
        <UButton
          variant="ghost"
          color="error"
          icon="i-lucide-trash"
          @click="$emit('delete')"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineEmits(['delete'])

const model = defineModel<Record<string, any>>({ required: true })
</script>
