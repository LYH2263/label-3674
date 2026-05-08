<template>
  <div class="dashboard">
    <section class="hero panel-card">
      <h2>社区数字驾驶舱</h2>
      <p>把报修、活动、通知和在线状态放在同一视图，提升服务响应速度。</p>
      <div class="chips">
        <span>实名认证</span>
        <span>JWT 安全链路</span>
        <span>RBAC 权限</span>
        <span>全容器化部署</span>
      </div>
    </section>

    <el-skeleton v-if="loading" animated :rows="6" class="panel-card skeleton" />

    <template v-else>
      <section class="metric-grid">
        <article class="metric panel-card">
          <h3>{{ overview.pendingRepairCount }}</h3>
          <p>进行中报修</p>
        </article>
        <article class="metric panel-card">
          <h3>{{ overview.joinedActivityCount }}</h3>
          <p>已报名活动</p>
        </article>
        <article class="metric panel-card">
          <h3>{{ overview.unreadMessageCount }}</h3>
          <p>未读消息</p>
        </article>
        <article class="metric panel-card">
          <h3>{{ overview.onlineUsers }}</h3>
          <p>当前在线用户</p>
        </article>
      </section>

      <section class="panel-card smart-card">
        <header>
          <h3 class="section-title">智能通知</h3>
          <p class="section-subtitle">个性化推荐与天气提醒</p>
        </header>
        <div class="smart-body">
          <article class="weather-tip">
            <h4>天气提醒</h4>
            <p>{{ smartNotice.weatherReminder?.summary || '-' }}</p>
            <span>{{ smartNotice.weatherReminder?.suggestion || '-' }}</span>
          </article>
          <div class="recommend-list">
            <article v-for="item in smartNotice.personalizedRecommendations" :key="item.title" class="recommend-item">
              <h5>{{ item.title }}</h5>
              <p>{{ item.content }}</p>
            </article>
            <el-empty v-if="!smartNotice.personalizedRecommendations?.length" description="暂无推荐" />
          </div>
        </div>
      </section>

      <section class="main-grid">
        <article class="notices panel-card">
          <header>
            <h3 class="section-title">社区公告</h3>
            <p class="section-subtitle">最近通知与紧急广播</p>
          </header>
          <div class="notice-list">
            <div v-for="item in overview.notices" :key="item.id" class="notice-item">
              <el-tag v-if="item.emergency" type="danger" size="small">紧急</el-tag>
              <h4>{{ item.title }}</h4>
              <p>{{ item.content }}</p>
            </div>
            <el-empty v-if="!overview.notices?.length" description="暂无公告" />
          </div>
        </article>

        <article class="quick-actions panel-card">
          <header>
            <h3 class="section-title">快速操作</h3>
            <p class="section-subtitle">常用模块一键进入</p>
          </header>
          <div class="action-grid">
            <button @click="$router.push('/repairs')">提交报修</button>
            <button @click="$router.push('/activities')">报名活动</button>
            <button @click="$router.push('/neighborhood')">邻里互动</button>
            <button @click="$router.push('/messages')">查看消息</button>
            <button @click="$router.push('/profile')">编辑资料</button>
            <button v-if="authStore.user?.role === 'RESIDENT'" @click="$router.push('/property')">物业服务</button>
          </div>
          <div class="uptime">
            系统稳定运行 {{ overview.uptimeMinutes }} 分钟
          </div>
        </article>
      </section>
    </template>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { systemApi } from '../api';
import { useAuthStore } from '../stores/auth';

const authStore = useAuthStore();
const loading = ref(true);
const overview = reactive({
  pendingRepairCount: 0,
  joinedActivityCount: 0,
  unreadMessageCount: 0,
  onlineUsers: 0,
  uptimeMinutes: 0,
  notices: [],
});
const smartNotice = reactive({
  personalizedRecommendations: [],
  weatherReminder: null,
});

async function loadOverview() {
  loading.value = true;
  try {
    const [data, intelligent] = await Promise.all([systemApi.overview(), systemApi.intelligentNotices()]);
    Object.assign(overview, data);
    Object.assign(smartNotice, intelligent || {});
  } finally {
    loading.value = false;
  }
}

onMounted(loadOverview);
</script>

<style scoped>
.dashboard {
  display: grid;
  gap: 14px;
}
.hero {
  padding: 24px;
}
.hero h2 {
  margin: 0;
  font-size: 1.55rem;
}
.hero p {
  margin: 10px 0 14px;
  color: #5d7182;
}
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.chips span {
  background: rgba(20, 147, 123, 0.12);
  color: #126f5f;
  border-radius: 999px;
  padding: 7px 12px;
  font-weight: 600;
  font-size: 0.83rem;
}
.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(140px, 1fr));
  gap: 12px;
}
.metric {
  padding: 20px;
}
.metric h3 {
  margin: 0;
  font-size: 2rem;
  color: #143651;
}
.metric p {
  margin: 6px 0 0;
  color: #678091;
}
.main-grid {
  display: grid;
  grid-template-columns: 1.25fr 1fr;
  gap: 12px;
}
.smart-card {
  padding: 20px;
}
.smart-body {
  margin-top: 12px;
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 12px;
}
.weather-tip {
  border: 1px solid rgba(29, 70, 108, 0.14);
  border-radius: 14px;
  padding: 14px;
  background: linear-gradient(160deg, rgba(227, 246, 255, 0.75), rgba(235, 252, 243, 0.75));
}
.weather-tip h4 {
  margin: 0 0 8px;
}
.weather-tip p {
  margin: 0;
  font-weight: 700;
  color: #173e5e;
}
.weather-tip span {
  display: inline-block;
  margin-top: 6px;
  color: #5f7688;
}
.recommend-list {
  display: grid;
  gap: 10px;
}
.recommend-item {
  border: 1px solid rgba(24, 59, 84, 0.1);
  border-radius: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.76);
}
.recommend-item h5 {
  margin: 0 0 6px;
  font-size: 0.95rem;
}
.recommend-item p {
  margin: 0;
  color: #5f7586;
}
.notices,
.quick-actions {
  padding: 20px;
}
.notice-list {
  margin-top: 12px;
  display: grid;
  gap: 12px;
}
.notice-item {
  border: 1px solid rgba(27, 48, 68, 0.1);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.72);
  padding: 14px;
}
.notice-item h4 {
  margin: 8px 0 6px;
  font-size: 1rem;
}
.notice-item p {
  margin: 0;
  color: #586c7d;
  line-height: 1.5;
}
.action-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(2, minmax(100px, 1fr));
  gap: 10px;
}
.action-grid button {
  border: 0;
  border-radius: 13px;
  background: linear-gradient(120deg, #f0f9f8, #eaf4ff);
  color: #22435c;
  font-weight: 700;
  padding: 12px;
  cursor: pointer;
  transition: all 0.25s ease;
}
.action-grid button:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(17, 49, 74, 0.14);
}
.uptime {
  margin-top: 12px;
  color: #6b7f91;
  font-size: 0.84rem;
}
.skeleton {
  padding: 16px;
}
@media (max-width: 1024px) {
  .metric-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .main-grid {
    grid-template-columns: 1fr;
  }
  .smart-body {
    grid-template-columns: 1fr;
  }
}
</style>
