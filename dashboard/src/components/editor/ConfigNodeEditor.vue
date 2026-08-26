<template>
  <div class="flex flex-col gap-2">
    <template v-for="(val, key) in value" :key="String(key)">
      <!-- Primitive Values -->
      <EditorField
        v-if="isPrimitive(val) || isStringArray(key)"
        :target="value"
        :identifier="key"
        :commentkey="currentPath(key)"
        :value="val"
      />

      <!-- Arrays -->
      <UPageCard v-else-if="Array.isArray(val)" variant="soft">
        <template #title>
          <div class="flex items-center justify-between gap-2">
            <h1 class="text-base font-semibold text-highlighted">
              {{ headerToDisplay(String(key)) }}
            </h1>

            <UButton
              variant="ghost"
              color="neutral"
              icon="i-lucide-plus"
              @click="addToArray(val, String(key))"
            />
          </div>
        </template>

        <template #description>
          <p class="text-xs text-muted">
            {{ comments?.[currentPath(key)] }}
          </p>
        </template>

        <!-- Special Arrays -->
        <div v-if="isSpecialArray(key)" class="flex flex-col gap-2">
          <div v-for="(entry, index) in val" :key="index">
            <EditorField
              :target="val"
              :identifier="index"
              :value="entry"
              :commentkey="currentPath(key)"
            />

            <UButton
              variant="ghost"
              v-if="!isSpecialArray(key)"
              color="error"
              icon="i-lucide-trash"
              @click="deleteArrayEntry(val, index)"
            />
          </div>
        </div>

        <!-- Object Array -->
        <div v-else class="flex flex-col gap-2">
          <UPageCard v-for="(entry, index) in val" :key="index" variant="subtle">
            <template #title>
              <div class="flex items-center justify-between gap-2">
                <h1 class="text-sm font-semibold">
                  {{ headerToDisplay(String(key)) }} #{{ index + 1 }}
                </h1>

                <UButton
                  variant="ghost"
                  color="error"
                  icon="i-lucide-trash"
                  @click="deleteArrayEntry(val, index)"
                />
              </div>
            </template>

            <ConfigNodeEditor
              :value="entry"
              :comments="comments"
              :path="[...path, String(key), String(index)]"
            />
          </UPageCard>
        </div>
      </UPageCard>

      <!-- Objects -->
      <UPageCard v-else-if="isObject(val)" variant="soft">
        <template #title>
          <div class="flex items-center justify-between gap-2">
            <h1 class="text-base font-semibold text-highlighted">
              {{ headerToDisplay(String(key)) }}
            </h1>
          </div>
        </template>

        <template #description>
          <p class="text-xs text-muted">
            {{ comments?.[currentPath(key)] }}
          </p>
        </template>

        <ConfigNodeEditor
          :value="val"
          :comments="comments"
          :path="[...path, String(key)]"
        />
      </UPageCard>
    </template>
  </div>
</template>

<script setup lang="ts">
import {addToArray, headerToDisplay, isStringArray} from "../../composables/FieldUtils.ts";

interface Props {
  value: Record<string, any>
  comments?: Record<string, string>
  path?: string[]
}

const props = withDefaults(defineProps<Props>(), {
  path: () => []
})

function isPrimitive(value: unknown): boolean {
  return (typeof value !== "object" || value === null)
}

function isSpecialArray(value: unknown) {
  if (!value)
    return false;

  const special = ['botStatus', 'permissions', 'entries', 'roleAdded', 'roleRemoved', 'syncs']
  return special.includes(String(value));
}

function isObject(value: unknown): value is Record<string, any> {
  return (typeof value === "object" && value !== null && !Array.isArray(value))
}

function currentPath(key: string | number) {
  return [...props.path, String(key)].join(".")
}

function deleteArrayEntry(array: any[], index: number) {
  array.splice(index, 1)
}
</script>
