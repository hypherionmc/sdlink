<template>
  <UModal v-model:open="isOpen" :dismissible="false" :overlay="true">

    <template #content>
      <div class="flex flex-col items-center justify-center gap-4 p-4 min-h-70">
        <h2 class="font-bold text-xl">Welcome to Simple Discord Link</h2>

        <p class="text-sm text-center">
          It looks like this is the first time you are using the mod, or your config is not set up properly.
          <br /> <br />
          We will guide you through getting your bot up and running quickly.
          <br /> <br />
          If you have used the mod before, or you already have a bot token, click Next. Otherwise click Guide to open the Wiki/Video guide
        </p>
      </div>

      <div class="flex items-center justify-between gap-2 w-full p-4">
        <UButton variant="subtle" size="lg" color="info" label="Open Wiki" icon="i-lucide-book" />
        <UButton variant="subtle" size="lg" color="primary" label="Next Step" trailing-icon="i-lucide-arrow-right" />
      </div>
    </template>
  </UModal>
</template>

<script setup lang="ts">
import {ref} from "vue";

const isOpen = ref(false)
</script>
