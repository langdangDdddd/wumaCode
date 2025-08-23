<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
import {healthCheck} from "@/api/healthController.ts";
healthCheck().then((res) => {
  console.log(res)
})

</script>

<template>
  <BasicLayout />
</template>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  line-height: 1.6;
  color: #333;
}

#app {
  min-height: 100vh;
}
</style>
