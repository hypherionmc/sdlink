<template>
  <div class="grid grid-cols-2 w-full gap-2 items-center">
    <div>
      <p class="text-sm mb-1 pl-1">Role</p>
      <UInput
        variant="subtle"
        size="lg"
        v-model="model.discordRole"
        class="w-full"
      />
    </div>

    <div>
      <p class="text-sm mb-1 pl-1">Commands (Press enter to save)</p>
      <div class="flex gap-1">
        <UInputTags
          variant="subtle"
          size="lg"
          v-model="model.minecraftCommand"
          class="w-full"
        />

        <UButton
          @click="$emit('delete')"
          icon="i-lucide-trash"
          variant="ghost"
          color="error"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineEmits(['delete'])

const model = defineModel<Record<string, any>>({ required: true })
</script>
