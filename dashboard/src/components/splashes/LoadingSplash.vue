<template>
  <div class="absolute top-0 left-0 w-full h-full bg-ct-light-primary dark:bg-ct-dark-primary flex items-center justify-center text-black dark:text-white z-50">
    <div class="text-center">
      <AppLogo />
      <br /><br />
      <h4 data-text="Loading..." class="uppercase loading-animation font-extrabold">Loading...</h4>
      <br />
    </div>
  </div>
</template>

<script setup lang="ts">
import {onMounted} from "vue";
import {useAppState} from "../../stores/appstate.ts";

onMounted(() => {
  setTimeout(() => {
    useAppState().setSplashScreen(false);
  }, 1000);
})
</script>

<style lang="css" scoped>
.loading-animation {
  position: relative;
  font-size: 3em;
}

.loading-animation:before {
  content: attr(data-text);
  position: absolute;
  overflow: hidden;
  max-width: 7em;
  white-space: nowrap;
  animation: loading 5s linear;
  color: gray;
}
@keyframes loading {
  0% {
    max-width: 0;
  }
}
</style>
