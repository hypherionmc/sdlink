<template>
  <div class="grid grid-cols-3 w-full gap-2 items-center">
    <div>
      <p class="text-sm mb-1 pl-1">Role</p>
      <UInput
        type="text"
        variant="subtle"
        size="lg"
        class="w-full"
        v-model="model.role"
      />
    </div>

    <div>
      <p class="text-sm mb-1 pl-1">Commands (Press enter to save)</p>
      <UInputTags
        variant="subtle"
        size="lg"
        class="w-full"
        v-model="model.commands"
      ></UInputTags>
    </div>

    <div>
      <p class="text-sm mb-1 pl-1">Permission Level</p>
      <div class="flex gap-1">
        <UInputNumber
          v-model="model.permissionLevel"
          :max="4"
          :min="1"
          variant="subtle"
          size="lg"
          class="w-full"
          @change="limitLevel"
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

const limitLevel = () => {
  if (model.value.permissionLevel > 4) {
    model.value.permissionLevel = 4;
  }

  if (model.value.permissionLevel < 1) {
    model.value.permissionLevel = 1;
  }
}
</script>
