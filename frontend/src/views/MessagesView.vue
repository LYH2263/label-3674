<template>
  <div class="messages-page panel-card">
    <header>
      <h2 class="section-title">消息中心</h2>
      <p class="section-subtitle">系统通知与私信统一管理，支持状态追踪</p>
    </header>

    <el-skeleton v-if="loading" :rows="5" animated />
    <div v-else class="message-list">
      <article v-for="item in messages" :key="item.id" :class="['message-item', { unread: !item.read }]">
        <div class="top-line">
          <el-tag :type="item.messageType === 'SYSTEM' ? 'primary' : 'success'" size="small">{{ item.messageType }}</el-tag>
          <span class="time">{{ item.createdAtText }}</span>
        </div>
        <h4>{{ item.title }}</h4>
        <p>{{ item.content }}</p>
        <div class="foot">
          <el-button v-if="!item.read" size="small" type="primary" plain @click="markRead(item.id)">标记已读</el-button>
          <span v-else class="read-mark">已读</span>
        </div>
      </article>
      <el-empty v-if="messages.length === 0" description="暂无消息" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { messageApi } from '../api';

const loading = ref(true);
const messages = ref([]);

function formatDateTime(value) {
  if (Array.isArray(value)) {
    const [y, m, d, hh = 0, mm = 0, ss = 0] = value;
    const pad = (num) => String(num).padStart(2, '0');
    return `${y}-${pad(m)}-${pad(d)} ${pad(hh)}:${pad(mm)}:${pad(ss)}`;
  }
  if (typeof value === 'string') {
    return value.replace('T', ' ');
  }
  return '-';
}

async function loadData() {
  loading.value = true;
  const list = await messageApi.list();
  messages.value = list.map((item) => ({
    ...item,
    createdAtText: formatDateTime(item.createdAt),
  }));
  loading.value = false;
}

async function markRead(id) {
  await messageApi.read(id);
  await loadData();
}

onMounted(loadData);
</script>

<style scoped>
.messages-page {
  padding: 20px;
}
.message-list {
  margin-top: 12px;
  display: grid;
  gap: 10px;
}
.message-item {
  border: 1px solid rgba(35, 67, 89, 0.12);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.8);
  padding: 14px;
}
.message-item.unread {
  border-color: rgba(15, 145, 123, 0.32);
  box-shadow: 0 10px 22px rgba(12, 63, 92, 0.1);
}
.top-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.time {
  color: #688092;
  font-size: 0.82rem;
}
.message-item h4 {
  margin: 10px 0 6px;
}
.message-item p {
  margin: 0;
  color: #5f7585;
}
.foot {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}
.read-mark {
  color: #6c8190;
  font-size: 0.85rem;
}
</style>
