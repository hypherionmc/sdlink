<template>
  <div class="flex flex-col w-full gap-2 items-center">
    <div class="flex items-end gap-2 w-full justify-between">
      <div class="w-full">
        <p class="text-sm mb-1 pl-1">Search</p>
        <UInput
          variant="subtle"
          size="lg"
          v-model="model.search"
          class="w-full"
        />
      </div>

      <div class="w-full">
        <p class="text-sm mb-1 pl-1">Replace</p>
        <UInput
          variant="subtle"
          size="lg"
          v-model="model.replace"
          class="w-full"
        />
      </div>

      <UButton
        variant="ghost"
        color="error"
        icon="i-lucide-trash"
        @click="$emit('delete')"
      />
    </div>

    <div class="flex gap-2 w-full justify-between">
      <SelectField
        v-model="model.target"
        label="Target Mode"
        class="w-full"
        :items="filterItems"
        v-if="useEditor().getConfig.config.general.configVersion > 26"
      />

      <SelectField
        v-model="model.searchMode"
        label="Search Mode"
        class="w-full"
        :items="searchItems"
      />

      <SelectField
        v-model="model.appliesTo"
        label="Applies To"
        class="w-full"
        :items="appliesItems"
        v-if="useEditor().getConfig.config.general.configVersion > 26"
      />

      <SelectField
        v-model="model.action"
        label="Action"
        class="w-full"
        :items="replaceItems"
      />

      <div v-if="useEditor().getConfig.config.general.configVersion > 26" class="w-full">
        <p class="text-sm mb-1 pl-1">Ignore Console</p>
        <USwitch v-model="model.ignoreConsole" size="lg" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {useEditor} from "../../../stores/editor.ts";

defineEmits(['delete'])

const model = defineModel<Record<string, any>>({ required: true })

defineProps<{
  filterItems: any[]
  searchItems: any[]
  appliesItems: any[]
  replaceItems: any[]
}>()
</script>
