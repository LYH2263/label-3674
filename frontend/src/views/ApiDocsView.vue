<template>
  <div class="api-docs-page panel-card">
    <header>
      <h2 class="section-title">API 在线文档</h2>
      <p class="section-subtitle">Swagger UI（支持在线调试）</p>
    </header>
    <div id="swagger-ui" class="swagger-wrap"></div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import SwaggerUI from 'swagger-ui-dist/swagger-ui-es-bundle';
import 'swagger-ui-dist/swagger-ui.css';

onMounted(() => {
  SwaggerUI({
    dom_id: '#swagger-ui',
    url: '/api/openapi.json',
    docExpansion: 'list',
    defaultModelsExpandDepth: 1,
    deepLinking: true,
    persistAuthorization: true,
  });
});
</script>

<style scoped>
.api-docs-page {
  padding: 20px;
}
.swagger-wrap {
  margin-top: 12px;
  min-height: calc(100vh - 240px);
}
:deep(.swagger-ui .topbar) {
  display: none;
}
</style>
