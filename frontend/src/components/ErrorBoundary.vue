<template>
  <div>
    <div v-if="error" class="error-panel">
      <h3>页面暂时不可用</h3>
      <p>系统已捕获异常，你可以刷新或稍后重试。</p>
      <el-button type="primary" @click="reset">重新加载</el-button>
    </div>
    <slot v-else />
  </div>
</template>

<script setup>
import { ref, onErrorCaptured } from 'vue';

const error = ref(null);

onErrorCaptured((err) => {
  error.value = err;
  return false;
});

function reset() {
  error.value = null;
  window.location.reload();
}
</script>

<style scoped>
.error-panel {
  margin: 120px auto;
  max-width: 460px;
  padding: 36px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 18px 50px rgba(14, 36, 56, 0.18);
  text-align: center;
}
.error-panel h3 {
  margin: 0 0 10px;
}
.error-panel p {
  margin: 0 0 16px;
  color: #556;
}
</style>
