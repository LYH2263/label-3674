<template>
  <div class="profile-page">
    <section class="panel-card profile-card">
      <header>
        <h2 class="section-title">个人空间</h2>
        <p class="section-subtitle">个人资料、身份状态与服务历史的统一入口</p>
      </header>

      <el-skeleton v-if="loading" :rows="10" animated />

      <template v-else>
        <section class="summary-grid">
          <article>
            <strong>{{ repairHistory.length }}</strong>
            <span>累计报修单</span>
          </article>
          <article>
            <strong>{{ pendingRepairCount }}</strong>
            <span>待跟进报修</span>
          </article>
          <article>
            <strong>{{ activityHistory.length }}</strong>
            <span>已报名活动</span>
          </article>
        </section>

        <el-form label-position="top" class="profile-form">
          <el-row :gutter="14">
            <el-col :md="12" :sm="24">
              <el-form-item label="用户名">
                <el-input v-model="profile.username" disabled />
              </el-form-item>
            </el-col>
            <el-col :md="12" :sm="24">
              <el-form-item label="角色">
                <el-input v-model="profile.role" disabled />
              </el-form-item>
            </el-col>
            <el-col :md="12" :sm="24">
              <el-form-item label="姓名">
                <el-input v-model="form.fullName" placeholder="请输入姓名" />
              </el-form-item>
            </el-col>
            <el-col :md="12" :sm="24">
              <el-form-item label="手机号">
                <el-input v-model="form.phone" placeholder="请输入手机号" />
              </el-form-item>
            </el-col>
            <el-col :md="12" :sm="24">
              <el-form-item label="邮箱">
                <el-input v-model="form.email" placeholder="请输入邮箱" />
              </el-form-item>
            </el-col>
            <el-col :md="12" :sm="24">
              <el-form-item label="实名认证状态">
                <el-tag :type="profile.realNameVerified ? 'success' : 'warning'" effect="plain">
                  {{ profile.realNameVerified ? '已认证' : '待认证' }}
                </el-tag>
              </el-form-item>
            </el-col>
          </el-row>

          <div class="actions">
            <el-button type="primary" :loading="submitting" @click="save">保存修改</el-button>
          </div>
        </el-form>
      </template>
    </section>

    <section class="panel-card history-card">
      <header class="history-header">
        <div>
          <h3 class="section-title">历史管理列表</h3>
          <p class="section-subtitle">集中查看个人报修与活动记录，满足验收所需的详尽列表展示</p>
        </div>
        <el-button text type="primary" @click="loadDetail">刷新</el-button>
      </header>

      <el-skeleton v-if="loading" :rows="8" animated />

      <el-tabs v-else v-model="activeTab">
        <el-tab-pane label="我的报修" name="repairs">
          <el-table :data="pagedRepairs" border empty-text="暂无报修历史">
            <el-table-column prop="id" label="工单ID" width="90" />
            <el-table-column prop="title" label="标题" min-width="180" />
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="repairStatusType(row.status)">{{ repairStatusText[row.status] || row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="最近更新" min-width="180">
              <template #default="{ row }">
                <div class="time-cell">
                  <strong>{{ row.updatedAtText }}</strong>
                  <span>{{ row.updatedAtRelative }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="评价" width="160">
              <template #default="{ row }">
                <span>{{ row.rating ? `${row.rating} 分` : '待评价' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default>
                <el-button link type="primary" @click="router.push('/repairs')">前往报修模块</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pager-wrap">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next"
              :current-page="repairPage"
              :page-size="repairPageSize"
              :page-sizes="[5, 8, 10, 20]"
              :total="repairHistory.length"
              @current-change="(val) => (repairPage = val)"
              @size-change="handleRepairSizeChange"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="我的活动" name="activities">
          <el-table :data="pagedActivities" border empty-text="暂无活动历史">
            <el-table-column prop="id" label="活动ID" width="90" />
            <el-table-column prop="title" label="标题" min-width="170" />
            <el-table-column prop="location" label="地点" min-width="140" />
            <el-table-column prop="startTimeText" label="开始时间" min-width="170" />
            <el-table-column label="日程状态" width="120">
              <template #default="{ row }">
                <el-tag :type="row.phaseType" effect="plain">{{ row.phaseText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default>
                <el-button link type="primary" @click="router.push('/activities')">前往活动模块</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pager-wrap">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next"
              :current-page="activityPage"
              :page-size="activityPageSize"
              :page-sizes="[5, 8, 10, 20]"
              :total="activityHistory.length"
              @current-change="(val) => (activityPage = val)"
              @size-change="handleActivitySizeChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { z } from 'zod';
import { activityApi, profileApi, repairApi } from '../api';
import { formatDateTime, formatRelativeTime } from '../utils/datetime';

const router = useRouter();
const loading = ref(true);
const submitting = ref(false);
const activeTab = ref('repairs');
const repairHistory = ref([]);
const activityHistory = ref([]);
const repairPage = ref(1);
const repairPageSize = ref(5);
const activityPage = ref(1);
const activityPageSize = ref(5);

const profile = reactive({
  username: '',
  role: '',
  realNameVerified: false,
});
const form = reactive({
  fullName: '',
  phone: '',
  email: '',
});

const repairStatusText = {
  PENDING: '待受理',
  IN_PROGRESS: '处理中',
  COMPLETED: '已完成',
};

const schema = z.object({
  fullName: z.string().min(2, '姓名至少2位'),
  phone: z.string().regex(/^1\d{10}$/, '手机号格式错误').optional().or(z.literal('')),
  email: z.string().email('邮箱格式错误').optional().or(z.literal('')),
});

const pendingRepairCount = computed(() => repairHistory.value.filter((item) => item.status !== 'COMPLETED').length);

const pagedRepairs = computed(() => {
  const start = (repairPage.value - 1) * repairPageSize.value;
  return repairHistory.value.slice(start, start + repairPageSize.value);
});

const pagedActivities = computed(() => {
  const start = (activityPage.value - 1) * activityPageSize.value;
  return activityHistory.value.slice(start, start + activityPageSize.value);
});

function repairStatusType(status) {
  if (status === 'COMPLETED') {
    return 'success';
  }
  if (status === 'IN_PROGRESS') {
    return 'warning';
  }
  return 'info';
}

function resolveActivityPhase(startTime) {
  const current = new Date();
  const time = new Date(formatDateTime(startTime).replace(' ', 'T'));
  if (Number.isNaN(time.getTime())) {
    return { phaseText: '待确认', phaseType: 'info' };
  }
  if (time.getTime() > current.getTime()) {
    return { phaseText: '待开始', phaseType: 'warning' };
  }
  return { phaseText: '已开场', phaseType: 'success' };
}

function normalizeRepair(item) {
  return {
    ...item,
    updatedAtText: formatDateTime(item.updatedAt, { withSeconds: false }),
    updatedAtRelative: formatRelativeTime(item.updatedAt),
  };
}

function normalizeActivity(item) {
  return {
    ...item,
    startTimeText: formatDateTime(item.startTime),
    ...resolveActivityPhase(item.startTime),
  };
}

function syncPageState() {
  if (repairPage.value > Math.ceil(Math.max(repairHistory.value.length, 1) / repairPageSize.value)) {
    repairPage.value = 1;
  }
  if (activityPage.value > Math.ceil(Math.max(activityHistory.value.length, 1) / activityPageSize.value)) {
    activityPage.value = 1;
  }
}

async function loadDetail() {
  loading.value = true;
  try {
    const [data, repairs, activities] = await Promise.all([profileApi.detail(), repairApi.mine(), activityApi.mine()]);
    Object.assign(profile, data);
    Object.assign(form, {
      fullName: data.fullName || '',
      phone: data.phone || '',
      email: data.email || '',
    });
    repairHistory.value = (repairs || []).map(normalizeRepair);
    activityHistory.value = (activities || []).map(normalizeActivity);
    syncPageState();
  } finally {
    loading.value = false;
  }
}

async function save() {
  submitting.value = true;
  try {
    schema.parse(form);
    const data = await profileApi.update(form);
    Object.assign(profile, data);
    ElMessage.success('资料已更新');
  } catch (error) {
    if (error?.issues?.[0]?.message) {
      ElMessage.error(error.issues[0].message);
    }
  } finally {
    submitting.value = false;
  }
}

function handleRepairSizeChange(size) {
  repairPageSize.value = size;
  repairPage.value = 1;
}

function handleActivitySizeChange(size) {
  activityPageSize.value = size;
  activityPage.value = 1;
}

onMounted(loadDetail);
</script>

<style scoped>
.profile-page {
  display: grid;
  gap: 12px;
}
.profile-card,
.history-card {
  padding: 24px;
}
.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}
.summary-grid article {
  padding: 16px;
  border-radius: 16px;
  background: linear-gradient(145deg, rgba(233, 247, 255, 0.9), rgba(239, 252, 243, 0.92));
  border: 1px solid rgba(22, 61, 88, 0.08);
}
.summary-grid strong {
  display: block;
  font-size: 1.8rem;
  color: #173850;
}
.summary-grid span {
  display: inline-block;
  margin-top: 6px;
  color: #68808f;
}
.profile-form {
  margin-top: 18px;
}
.actions {
  margin-top: 8px;
}
.history-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}
.pager-wrap {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}
.time-cell {
  display: grid;
  gap: 4px;
}
.time-cell strong {
  color: #183b55;
  font-size: 0.92rem;
}
.time-cell span {
  color: #738694;
  font-size: 0.82rem;
}
@media (max-width: 900px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
  .history-header {
    flex-direction: column;
    align-items: stretch;
  }
  .pager-wrap {
    justify-content: center;
  }
}
</style>
