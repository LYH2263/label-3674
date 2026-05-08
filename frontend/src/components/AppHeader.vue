<template>
  <header class="app-header">
    <div class="brand" @click="$router.push('/dashboard')">
      <span class="logo-dot"></span>
      <div>
        <h1>Smart Community</h1>
        <p>一站式智慧社区服务中心</p>
      </div>
    </div>

    <nav class="main-nav">
      <button
        v-for="item in menus"
        :key="item.path"
        :class="['nav-item', { active: $route.path === item.path }]"
        @click="$router.push(item.path)"
      >
        {{ item.label }}
      </button>
    </nav>

    <div class="user-area">
      <el-tag type="success" effect="plain" round>{{ roleLabel }}</el-tag>
      <div class="name">{{ authStore.user?.fullName || authStore.user?.username }}</div>
      <el-button size="small" type="danger" plain @click="handleLogout">退出</el-button>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../stores/auth';
import { authApi } from '../api';

const authStore = useAuthStore();

const roleMap = {
  RESIDENT: '居民',
  PROPERTY_ADMIN: '物业管理员',
  SERVICE_PROVIDER: '服务商',
};

const roleLabel = computed(() => roleMap[authStore.user?.role] || '访客');

const menus = computed(() => {
  const common = [
    { label: '总览', path: '/dashboard' },
    { label: '我的报修', path: '/repairs' },
    { label: '社区活动', path: '/activities' },
    { label: '邻里互动', path: '/neighborhood' },
    { label: '消息中心', path: '/messages' },
    { label: 'API文档', path: '/api-docs' },
    { label: '个人空间', path: '/profile' },
  ];
  if (authStore.user?.role === 'RESIDENT') {
    common.splice(4, 0, { label: '物业服务', path: '/property' });
  }
  return common;
});

async function handleLogout() {
  try {
    await authApi.logout();
  } catch {
    // Ignore API errors and clear local state.
  }
  authStore.logout();
  ElMessage.success('已安全退出');
  window.location.href = '/login';
}
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 10;
  display: grid;
  grid-template-columns: 320px 1fr 220px;
  align-items: center;
  gap: 14px;
  padding: 14px 22px;
  border-radius: 20px;
  backdrop-filter: blur(14px);
  background: rgba(253, 255, 255, 0.78);
  border: 1px solid rgba(22, 56, 79, 0.08);
  box-shadow: 0 12px 30px rgba(12, 35, 51, 0.1);
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}
.logo-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0da18c 0%, #45c8dc 100%);
  box-shadow: 0 0 0 6px rgba(13, 161, 140, 0.18);
}
.brand h1 {
  margin: 0;
  font-size: 1.08rem;
}
.brand p {
  margin: 2px 0 0;
  font-size: 0.73rem;
  color: #6f7e8b;
}
.main-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.nav-item {
  border: 0;
  border-radius: 12px;
  background: rgba(25, 41, 61, 0.06);
  color: #324558;
  font-weight: 600;
  padding: 9px 12px;
  cursor: pointer;
  transition: all 0.25s ease;
}
.nav-item:hover {
  transform: translateY(-1px);
  background: rgba(32, 58, 93, 0.12);
}
.nav-item.active {
  background: linear-gradient(90deg, #17a486, #4cc9bd);
  color: #fff;
}
.user-area {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
}
.name {
  font-size: 0.85rem;
  color: #304456;
}

@media (max-width: 1100px) {
  .app-header {
    grid-template-columns: 1fr;
    gap: 10px;
  }
  .main-nav,
  .user-area {
    justify-content: flex-start;
  }
}
</style>
