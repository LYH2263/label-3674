<template>
  <div class="activities-page">
    <section class="panel-card create-card">
      <header>
        <h2 class="section-title">社区活动</h2>
        <p class="section-subtitle">活动发布、报名、扫码签到、回顾分享一体化</p>
      </header>

      <el-form label-position="top" class="activity-form">
        <el-row :gutter="14">
          <el-col :md="8" :sm="24">
            <el-form-item label="活动标题">
              <el-input v-model="createForm.title" placeholder="请输入活动标题" />
            </el-form-item>
          </el-col>
          <el-col :md="8" :sm="24">
            <el-form-item label="活动地点">
              <el-input v-model="createForm.location" placeholder="请输入活动地点" />
            </el-form-item>
          </el-col>
          <el-col :md="8" :sm="24">
            <el-form-item label="开始时间">
              <el-date-picker
                v-model="createForm.startTime"
                type="datetime"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
                placeholder="请选择开始时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :md="24" :sm="24">
            <el-form-item label="活动描述">
              <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请输入活动描述" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-button type="primary" :loading="creating" @click="createActivity">发布活动</el-button>
      </el-form>
    </section>

    <section class="panel-card list-card">
      <header>
        <h3 class="section-title">活动列表</h3>
      </header>
      <el-skeleton v-if="loading" :rows="5" animated />
      <div v-else class="activity-grid">
        <article v-for="item in activities" :key="item.id" class="activity-item">
          <h4>{{ item.title }}</h4>
          <p>{{ item.description }}</p>
          <div class="meta">
            <span>📍 {{ item.location }}</span>
            <span>🕒 {{ item.startTimeText }}</span>
          </div>
          <div class="actions">
            <el-tag v-if="item.joined" type="success">已报名</el-tag>
            <el-button v-else size="small" type="primary" plain @click="signup(item.id)">报名参加</el-button>
            <el-button v-if="item.joined" size="small" @click="openCheckin(item)">签到二维码</el-button>
            <el-button v-if="item.joined" size="small" type="success" plain @click="openReview(item)">活动回顾</el-button>
          </div>
        </article>
        <el-empty v-if="activities.length === 0" description="暂无活动" />
      </div>
    </section>

    <section class="panel-card my-card">
      <header>
        <h3 class="section-title">我的活动</h3>
      </header>
      <el-table :data="pagedMyActivities" border empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="location" label="地点" min-width="120" />
        <el-table-column prop="startTimeText" label="开始时间" min-width="170" />
      </el-table>
      <div class="pager-wrap">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :current-page="myPage"
          :page-size="myPageSize"
          :page-sizes="[5, 8, 10, 20]"
          :total="myActivities.length"
          @current-change="(val) => (myPage = val)"
          @size-change="handleMySizeChange"
        />
      </div>
    </section>

    <el-dialog v-model="checkinVisible" title="活动签到二维码" width="460px">
      <div class="checkin-box" v-loading="checkinLoading">
        <img v-if="checkinQrDataUrl" :src="checkinQrDataUrl" alt="签到二维码" />
        <p>请在活动现场出示二维码完成签到，系统会自动校验当日有效性。</p>
        <el-tag :type="checkinStatus === 'SIGNED_IN' ? 'success' : 'warning'">
          {{ checkinStatus === 'SIGNED_IN' ? '已签到' : '待签到' }}
        </el-tag>
      </div>
      <template #footer>
        <el-button @click="checkinVisible = false">关闭</el-button>
        <el-button type="primary" @click="confirmCheckin" :disabled="checkinStatus === 'SIGNED_IN'">确认签到</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewVisible" title="活动回顾" width="780px">
      <el-row :gutter="14">
        <el-col :md="11" :sm="24">
          <el-form label-position="top">
            <el-form-item label="评分">
              <el-rate v-model="reviewForm.rating" :max="5" />
            </el-form-item>
            <el-form-item label="回顾内容">
              <el-input v-model="reviewForm.content" type="textarea" :rows="4" placeholder="分享你的活动体验" />
            </el-form-item>
            <el-form-item label="回顾照片（可选）">
              <el-upload
                :show-file-list="false"
                :auto-upload="true"
                :before-upload="beforeReviewImageUpload"
                :http-request="handleReviewImageUpload"
                accept="image/*"
              >
                <el-button>上传照片</el-button>
              </el-upload>
              <div v-if="reviewForm.photoUrl" class="photo-preview">
                <img :src="reviewForm.photoUrl" alt="回顾照片" />
              </div>
            </el-form-item>
            <el-button type="primary" :loading="reviewSubmitting" @click="submitReview">发布回顾</el-button>
          </el-form>
        </el-col>
        <el-col :md="13" :sm="24">
          <div class="review-list">
            <article v-for="item in reviews" :key="item.id" :class="['review-item', { featured: item.photoUrl }]">
              <div v-if="item.photoUrl" class="review-photo-shell">
                <img :src="item.photoUrl" alt="活动回顾照片" />
              </div>
              <div class="review-body">
                <div class="review-top">
                  <strong>{{ item.reviewerName }}</strong>
                  <el-rate :model-value="item.rating" disabled :max="5" size="small" />
                </div>
                <p>{{ item.content }}</p>
                <span>{{ item.createdAtText }}</span>
              </div>
            </article>
            <el-empty v-if="reviews.length === 0" description="暂无活动回顾" />
          </div>
        </el-col>
      </el-row>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import QRCode from 'qrcode';
import { activityApi, repairApi } from '../api';
import { formatDateTime } from '../utils/datetime';

const loading = ref(true);
const creating = ref(false);
const activities = ref([]);
const myActivities = ref([]);
const myPage = ref(1);
const myPageSize = ref(8);

const checkinVisible = ref(false);
const checkinLoading = ref(false);
const checkinActivityId = ref(null);
const checkinToken = ref('');
const checkinQrDataUrl = ref('');
const checkinStatus = ref('REGISTERED');

const reviewVisible = ref(false);
const reviewSubmitting = ref(false);
const reviewActivityId = ref(null);
const reviews = ref([]);

const reviewForm = reactive({
  rating: 5,
  content: '',
  photoUrl: '',
});

const createForm = reactive({
  title: '',
  description: '',
  location: '',
  startTime: '',
});

const pagedMyActivities = computed(() => {
  const start = (myPage.value - 1) * myPageSize.value;
  return myActivities.value.slice(start, start + myPageSize.value);
});

function handleMySizeChange(size) {
  myPageSize.value = size;
  myPage.value = 1;
}

async function loadData() {
  loading.value = true;
  try {
    const [activityList, mine] = await Promise.all([activityApi.list(), activityApi.mine()]);
    activities.value = activityList.map((item) => ({
      ...item,
      startTimeText: formatDateTime(item.startTime),
    }));
    myActivities.value = mine.map((item) => ({
      ...item,
      startTimeText: formatDateTime(item.startTime),
    }));
    if (myPage.value > Math.ceil(Math.max(myActivities.value.length, 1) / myPageSize.value)) {
      myPage.value = 1;
    }
  } finally {
    loading.value = false;
  }
}

async function createActivity() {
  const title = createForm.title?.trim() || '';
  const description = createForm.description?.trim() || '';
  const location = createForm.location?.trim() || '';

  if (title.length < 3) {
    ElMessage.warning('活动标题至少3个字符');
    return;
  }
  if (description.length < 8) {
    ElMessage.warning('活动描述至少8个字符');
    return;
  }
  if (location.length < 2) {
    ElMessage.warning('活动地点至少2个字符');
    return;
  }
  if (!createForm.startTime) {
    ElMessage.warning('请选择活动开始时间');
    return;
  }

  creating.value = true;
  try {
    await activityApi.create({
      title,
      description,
      location,
      startTime: createForm.startTime,
    });
    ElMessage.success('活动发布成功');
    createForm.title = '';
    createForm.description = '';
    createForm.location = '';
    createForm.startTime = '';
    await loadData();
  } finally {
    creating.value = false;
  }
}

async function signup(id) {
  await activityApi.signup(id);
  ElMessage.success('报名成功');
  await loadData();
}

async function openCheckin(item) {
  checkinVisible.value = true;
  checkinLoading.value = true;
  checkinQrDataUrl.value = '';
  try {
    const data = await activityApi.checkinQr(item.id);
    checkinActivityId.value = item.id;
    checkinToken.value = data.token;
    checkinStatus.value = data.signStatus;
    checkinQrDataUrl.value = await QRCode.toDataURL(data.checkinPayload, {
      width: 220,
      margin: 1,
    });
  } finally {
    checkinLoading.value = false;
  }
}

async function confirmCheckin() {
  if (!checkinActivityId.value || !checkinToken.value) {
    return;
  }
  await activityApi.checkin(checkinActivityId.value, { token: checkinToken.value });
  ElMessage.success('签到成功');
  checkinStatus.value = 'SIGNED_IN';
}

async function openReview(item) {
  reviewVisible.value = true;
  reviewActivityId.value = item.id;
  reviewForm.rating = 5;
  reviewForm.content = '';
  reviewForm.photoUrl = '';
  await loadReviews(item.id);
}

async function loadReviews(activityId) {
  const list = await activityApi.reviews(activityId);
  reviews.value = (list || []).map((item) => ({
    ...item,
    createdAtText: formatDateTime(item.createdAt),
  }));
}

function beforeReviewImageUpload(file) {
  const isImage = file.type.startsWith('image/');
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isImage) {
    ElMessage.warning('仅支持图片格式');
    return false;
  }
  if (!isLt5M) {
    ElMessage.warning('图片不能超过5MB');
    return false;
  }
  return true;
}

async function handleReviewImageUpload(options) {
  try {
    const data = await repairApi.uploadImage(options.file);
    reviewForm.photoUrl = data.imageUrl;
    options.onSuccess?.(data);
    ElMessage.success('图片上传成功');
  } catch (error) {
    options.onError?.(error);
  }
}

async function submitReview() {
  if (!reviewActivityId.value) {
    return;
  }
  reviewSubmitting.value = true;
  try {
    await activityApi.createReview(reviewActivityId.value, {
      rating: reviewForm.rating,
      content: reviewForm.content,
      photoUrl: reviewForm.photoUrl || null,
    });
    ElMessage.success('活动回顾发布成功');
    reviewForm.content = '';
    reviewForm.photoUrl = '';
    reviewForm.rating = 5;
    await loadReviews(reviewActivityId.value);
  } finally {
    reviewSubmitting.value = false;
  }
}

onMounted(loadData);
</script>

<style scoped>
.activities-page {
  display: grid;
  gap: 12px;
}
.create-card,
.list-card,
.my-card {
  padding: 20px;
}
.activity-form {
  margin-top: 12px;
}
.activity-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}
.activity-item {
  border: 1px solid rgba(31, 67, 95, 0.13);
  border-radius: 14px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.75);
}
.activity-item h4 {
  margin: 0;
}
.activity-item p {
  margin: 8px 0 12px;
  color: #5e7484;
  line-height: 1.5;
}
.meta {
  display: grid;
  gap: 4px;
  color: #607688;
  font-size: 0.86rem;
}
.actions {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}
.pager-wrap {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}
.checkin-box {
  display: grid;
  justify-items: center;
  gap: 12px;
  text-align: center;
}
.checkin-box img {
  width: 220px;
  height: 220px;
  border-radius: 10px;
  border: 1px solid rgba(22, 54, 74, 0.12);
}
.checkin-box p {
  margin: 0;
  color: #5b7180;
  line-height: 1.6;
}
.review-list {
  max-height: 420px;
  overflow: auto;
  display: grid;
  gap: 12px;
}
.review-item {
  border: 1px solid rgba(28, 63, 90, 0.13);
  border-radius: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.8);
}
.review-item.featured {
  display: grid;
  grid-template-columns: 180px minmax(0, 1fr);
  gap: 14px;
  align-items: stretch;
}
.review-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.review-body {
  display: grid;
  align-content: start;
}
.review-item p {
  margin: 8px 0;
  color: #5b7181;
  line-height: 1.7;
}
.review-photo-shell {
  overflow: hidden;
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(228, 245, 255, 0.9), rgba(239, 250, 242, 0.9));
}
.review-item img {
  width: 100%;
  height: 100%;
  min-height: 150px;
  object-fit: cover;
  display: block;
}
.review-item span {
  display: inline-block;
  margin-top: 8px;
  font-size: 0.82rem;
  color: #718695;
}
.photo-preview {
  margin-top: 10px;
}
.photo-preview img {
  width: 160px;
  height: 110px;
  border-radius: 12px;
  object-fit: cover;
  border: 1px solid rgba(30, 73, 103, 0.12);
}
@media (max-width: 900px) {
  .activity-grid {
    grid-template-columns: 1fr;
  }
  .review-item.featured {
    grid-template-columns: 1fr;
  }
  .pager-wrap {
    justify-content: center;
  }
}
</style>
