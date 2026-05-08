<template>
  <div class="app-bg"></div>
  <div class="grain"></div>
  <main class="page-shell">
    <AppHeader v-if="showHeader" />
    <section class="route-container" :class="{ login: !showHeader }">
      <router-view />
    </section>
  </main>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { ElNotification } from 'element-plus';
import { useAuthStore } from './stores/auth';
import AppHeader from './components/AppHeader.vue';
import { shouldMuteRealtimeNotification } from './utils/realtimeNotification';

const route = useRoute();
const authStore = useAuthStore();
const socketRef = ref(null);
const reconnectTimer = ref(null);

const showHeader = computed(() => route.path !== '/login' && !!authStore.token);

function socketUrl(token) {
  const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
  return `${protocol}://${window.location.host}/ws/notifications?token=${encodeURIComponent(token)}`;
}

function closeSocket() {
  if (reconnectTimer.value) {
    clearTimeout(reconnectTimer.value);
    reconnectTimer.value = null;
  }
  if (socketRef.value) {
    socketRef.value.close();
    socketRef.value = null;
  }
}

function connectSocket() {
  if (!authStore.token || socketRef.value) {
    return;
  }
  const socket = new WebSocket(socketUrl(authStore.token));
  socketRef.value = socket;

  socket.onmessage = (event) => {
    try {
      const payload = JSON.parse(event.data);
      if (payload.event === 'message_created') {
        if (shouldMuteRealtimeNotification(payload.title)) {
          return;
        }
        ElNotification({
          title: '实时通知',
          message: payload.title || '您收到一条新消息',
          type: 'info',
          duration: 2600,
        });
      }
    } catch {
      // Ignore malformed payloads.
    }
  };

  socket.onclose = () => {
    socketRef.value = null;
    if (authStore.token) {
      reconnectTimer.value = setTimeout(() => connectSocket(), 3000);
    }
  };

  socket.onerror = () => {
    socket.close();
  };
}

onMounted(async () => {
  if (authStore.token) {
    await authStore.fetchMe();
  }
});

watch(
  () => authStore.token,
  (token) => {
    if (!token) {
      closeSocket();
      return;
    }
    connectSocket();
  },
  { immediate: true },
);

onBeforeUnmount(() => {
  closeSocket();
});
</script>

<style scoped>
.app-bg {
  position: fixed;
  inset: 0;
  background:
    radial-gradient(1400px 700px at 12% 18%, rgba(126, 233, 205, 0.35), transparent 64%),
    radial-gradient(1000px 600px at 84% 30%, rgba(124, 182, 233, 0.3), transparent 68%),
    linear-gradient(140deg, #eef8fb 0%, #f7fcff 42%, #edf7f1 100%);
  z-index: -2;
}
.grain {
  position: fixed;
  inset: 0;
  background-image: radial-gradient(rgba(10, 24, 34, 0.06) 1px, transparent 1px);
  background-size: 4px 4px;
  opacity: 0.15;
  z-index: -1;
}
.page-shell {
  max-width: 1300px;
  margin: 0 auto;
  padding: 20px 16px 28px;
}
.route-container {
  margin-top: 14px;
  min-height: calc(100vh - 120px);
}
.route-container.login {
  margin-top: 0;
}
</style>
