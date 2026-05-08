<template>
  <div class="neighborhood-page">
    <section class="panel-card publish-card">
      <header>
        <h2 class="section-title">邻里互动</h2>
        <p class="section-subtitle">邻里圈、二手市场、技能交换、失物招领统一入口</p>
      </header>

      <el-form label-position="top" class="publish-form">
        <el-row :gutter="14">
          <el-col :md="6" :sm="24">
            <el-form-item label="互动分类">
              <el-select v-model="createForm.category" placeholder="请选择分类" style="width: 100%">
                <el-option v-for="item in categoryOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :md="8" :sm="24">
            <el-form-item label="标题">
              <el-input v-model="createForm.title" placeholder="请输入标题" />
            </el-form-item>
          </el-col>
          <el-col :md="10" :sm="24">
            <el-form-item label="联系方式">
              <el-input v-model="createForm.contact" placeholder="如手机号、微信号或用户名" />
            </el-form-item>
          </el-col>
          <el-col :md="24" :sm="24">
            <el-form-item label="内容">
              <el-input v-model="createForm.content" type="textarea" :rows="3" placeholder="请输入详细内容" />
            </el-form-item>
          </el-col>
          <el-col :md="6" :sm="24">
            <el-form-item label="价格（仅二手可填）">
              <el-input-number v-model="createForm.price" :min="0" :precision="2" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-button type="primary" :loading="submitting" @click="submitPost">发布互动</el-button>
      </el-form>
    </section>

    <section class="panel-card list-card">
      <header class="list-header">
        <h3 class="section-title">互动信息</h3>
        <el-tabs v-model="activeCategory" @tab-change="handleCategoryChange">
          <el-tab-pane label="全部" name="ALL" />
          <el-tab-pane label="邻里圈" name="NEIGHBOR_CIRCLE" />
          <el-tab-pane label="二手市场" name="SECOND_HAND" />
          <el-tab-pane label="技能交换" name="SKILL_SWAP" />
          <el-tab-pane label="失物招领" name="LOST_FOUND" />
        </el-tabs>
      </header>

      <el-table :data="posts" border empty-text="暂无数据" v-loading="loading">
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="categoryLabel" label="分类" width="110" />
        <el-table-column prop="content" label="内容" min-width="230" show-overflow-tooltip />
        <el-table-column label="价格" width="110">
          <template #default="{ row }">
            {{ row.price ? `¥ ${Number(row.price).toFixed(2)}` : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="contact" label="联系方式" min-width="130" />
        <el-table-column prop="publisherName" label="发布人" width="100" />
        <el-table-column prop="statusText" label="状态" width="90" />
        <el-table-column prop="createdAtText" label="发布时间" min-width="160" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button
              v-if="row.userId === authStore.user?.id && row.status === 'OPEN'"
              size="small"
              type="danger"
              plain
              @click="closePost(row.id)"
            >
              关闭
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager-wrap">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :current-page="page"
          :page-size="pageSize"
          :page-sizes="[5, 8, 10, 20]"
          :total="total"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { neighborhoodApi } from '../api';
import { useAuthStore } from '../stores/auth';

const authStore = useAuthStore();
const loading = ref(false);
const submitting = ref(false);
const posts = ref([]);
const total = ref(0);
const page = ref(1);
const pageSize = ref(8);
const activeCategory = ref('ALL');

const categoryMap = {
  NEIGHBOR_CIRCLE: '邻里圈',
  SECOND_HAND: '二手市场',
  SKILL_SWAP: '技能交换',
  LOST_FOUND: '失物招领',
};

const categoryOptions = Object.entries(categoryMap).map(([value, label]) => ({ value, label }));

const createForm = reactive({
  category: 'NEIGHBOR_CIRCLE',
  title: '',
  content: '',
  price: null,
  contact: '',
});

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

async function loadPosts() {
  loading.value = true;
  try {
    const data = await neighborhoodApi.list({
      category: activeCategory.value === 'ALL' ? undefined : activeCategory.value,
      page: page.value,
      pageSize: pageSize.value,
    });
    total.value = data.total;
    posts.value = (data.records || []).map((item) => ({
      ...item,
      categoryLabel: categoryMap[item.category] || item.category,
      statusText: item.status === 'OPEN' ? '进行中' : '已关闭',
      createdAtText: formatDateTime(item.createdAt),
    }));
  } finally {
    loading.value = false;
  }
}

async function submitPost() {
  submitting.value = true;
  try {
    await neighborhoodApi.create({
      category: createForm.category,
      title: createForm.title,
      content: createForm.content,
      price: createForm.category === 'SECOND_HAND' ? createForm.price : null,
      contact: createForm.contact,
    });
    ElMessage.success('发布成功');
    createForm.title = '';
    createForm.content = '';
    createForm.price = null;
    createForm.contact = '';
    page.value = 1;
    await loadPosts();
  } finally {
    submitting.value = false;
  }
}

async function closePost(id) {
  await neighborhoodApi.close(id);
  ElMessage.success('帖子已关闭');
  await loadPosts();
}

function handleCategoryChange() {
  page.value = 1;
  loadPosts();
}

function handlePageChange(current) {
  page.value = current;
  loadPosts();
}

function handleSizeChange(size) {
  pageSize.value = size;
  page.value = 1;
  loadPosts();
}

onMounted(loadPosts);
</script>

<style scoped>
.neighborhood-page {
  display: grid;
  gap: 12px;
}
.publish-card,
.list-card {
  padding: 20px;
}
.publish-form {
  margin-top: 12px;
}
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 10px;
  margin-bottom: 10px;
}
.pager-wrap {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}
@media (max-width: 900px) {
  .list-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
