<template>
  <img :src="themeColor == 'dark' ? LOGO : LOGO_LIGHT" class="block w-full" alt="logo"  />
</template>

<script setup lang="ts">
import LOGO from '../assets/img/sdl_logo.svg'
import LOGO_LIGHT from '../assets/img/sdl_light.svg'
import {useColorMode} from "@vueuse/core";
import {computed} from "vue";

const colorMode = useColorMode()
const themeColor = computed(() => colorMode.value)
</script>
