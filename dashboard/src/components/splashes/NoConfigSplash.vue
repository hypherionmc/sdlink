<script setup lang="ts">
import {useWebsocketClient} from "../../composables/websocketClient.ts";
const socketClient = useWebsocketClient()

</script>
<template>
  <div class="absolute top-0 left-0 w-full h-full flex items-center justify-center text-black dark:text-white">

    <div class="text-center">
      <AppLogo class="mb-5" />

      <UAlert
        v-if="!socketClient.isConnected && !socketClient.isWaiting"
        title="You have no active editor sessions."
        description="Use the sdconfigeditor command in your server to open a new editor session"
        variant="subtle"
        color="error"
      />

      <UAlert
        v-if="socketClient.isWaiting"
        title="Waiting for data"
        description="Waiting for config to be sent from Simple Discord Link. You will be redirected shortly"
        variant="subtle"
        color="warning"
      />
    </div>

  </div>
</template>
