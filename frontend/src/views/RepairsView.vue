<template>
  <div class="repairs-page">
    <section class="panel-card submit-card">
      <header>
        <h2 class="section-title">智能报修系统</h2>
        <p class="section-subtitle">支持图文提交、状态跟踪、服务评价和在线协同处理</p>
      </header>

      <el-form label-position="top" class="repair-form">
        <el-row :gutter="14">
          <el-col :md="10" :sm="24">
            <el-form-item label="报修标题">
              <el-input v-model="createForm.title" placeholder="请输入故障标题" />
            </el-form-item>
          </el-col>
          <el-col :md="10" :sm="24">
            <el-form-item label="上传图片">
              <el-upload
                ref="uploaderRef"
                class="image-uploader"
                drag
                :show-file-list="false"
                :auto-upload="true"
                :before-upload="beforeImageUpload"
                :http-request="handleImageUpload"
                accept="image/*"
              >
                <div v-if="uploadedImageUrl" class="upload-preview">
                  <img :src="uploadedImageUrl" alt="上传图片预览" />
                  <div class="upload-overlay">
                    <span>{{ uploadedFileName || '已上传图片' }}</span>
                    <el-button size="small" type="danger" plain @click.stop="removeUploadedImage">删除图片</el-button>
                  </div>
                </div>
                <div v-else class="upload-placeholder">
                  <p>拖拽图片到此处，或点击上传</p>
                  <p class="sub">支持 jpg/png/webp，单张不超过 5MB</p>
                </div>
              </el-upload>
            </el-form-item>
          </el-col>
          <el-col :md="24" :sm="24">
            <el-form-item label="问题描述">
              <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请详细描述报修情况" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-button type="primary" :loading="submitting" :disabled="uploadingImage" @click="submitRepair">提交报修</el-button>
      </el-form>
    </section>

    <section class="panel-card list-card">
      <header>
        <h3 class="section-title">我的报修</h3>
      </header>

      <el-skeleton v-if="loadingMine" :rows="5" animated />
      <template v-else>
        <el-table :data="pagedMyRepairs" border empty-text="暂无数据">
          <el-table-column prop="id" label="工单ID" width="90" />
          <el-table-column prop="title" label="标题" min-width="180" />
          <el-table-column label="状态" width="130">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)">{{ statusTextMap[row.status] || row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="最近更新" min-width="190">
            <template #default="{ row }">
              <div class="time-cell">
                <strong>{{ row.updatedAtText }}</strong>
                <span>{{ row.updatedAtRelative }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="评价" width="190">
            <template #default="{ row }">
              <el-button v-if="row.status === 'COMPLETED' && !row.rating" size="small" @click="openRate(row)">评价</el-button>
              <span v-else>{{ row.rating ? `${row.rating} 分` : '-' }}</span>
            </template>
          </el-table-column>
        </el-table>
        <div class="pager-wrap">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next"
            :current-page="myPage"
            :page-size="myPageSize"
            :page-sizes="[5, 8, 10, 20]"
            :total="myRepairs.length"
            @current-change="(val) => (myPage = val)"
            @size-change="handleMySizeChange"
          />
        </div>
      </template>
    </section>

    <section v-if="canManageRepairs" class="panel-card board-card">
      <header>
        <h3 class="section-title">服务工单看板</h3>
        <p class="section-subtitle">物业/服务商可在此受理工单并更新进度</p>
      </header>
      <el-table :data="pagedBoardRepairs" border empty-text="暂无数据">
        <el-table-column prop="id" label="工单" width="80" />
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="status" label="状态" width="130" />
        <el-table-column prop="assignedProviderId" label="服务商" width="120" />
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <div class="status-actions">
              <button class="mini-chip" @click="updateStatus(row, 'IN_PROGRESS')">处理中</button>
              <button class="mini-chip" @click="updateStatus(row, 'COMPLETED')">完成</button>
              <button class="mini-chip" @click="openAssign(row)">指派</button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager-wrap">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :current-page="boardPage"
          :page-size="boardPageSize"
          :page-sizes="[5, 8, 10, 20]"
          :total="boardRepairs.length"
          @current-change="(val) => (boardPage = val)"
          @size-change="handleBoardSizeChange"
        />
      </div>
    </section>

    <el-dialog v-model="rateVisible" title="服务评价" width="420px">
      <el-form label-position="top">
        <el-form-item label="评分（1-5）">
          <el-rate v-model="rateForm.rating" :max="5" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="rateForm.comment" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rateVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRate">提交评价</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignVisible" title="选择服务商" width="540px">
      <div class="provider-grid">
        <button
          v-for="item in providers"
          :key="item.id"
          :class="['provider-item', { active: assignProviderId === item.id }]"
          @click="assignProviderId = item.id"
        >
          <strong>{{ item.fullName }}</strong>
          <span>{{ item.username }}</span>
        </button>
      </div>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAssign">确认指派</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { repairApi, systemApi } from '../api';
import { useAuthStore } from '../stores/auth';
import { formatDateTime, formatRelativeTime } from '../utils/datetime';

const authStore = useAuthStore();
const loadingMine = ref(true);
const submitting = ref(false);
const uploadingImage = ref(false);
const myRepairs = ref([]);
const boardRepairs = ref([]);
const myPage = ref(1);
const myPageSize = ref(8);
const boardPage = ref(1);
const boardPageSize = ref(8);
const providers = ref([]);
const uploaderRef = ref(null);
const uploadedImageUrl = ref('');
const uploadedFileName = ref('');

const createForm = reactive({
  title: '',
  description: '',
});

const canManageRepairs = computed(() => ['PROPERTY_ADMIN', 'SERVICE_PROVIDER'].includes(authStore.user?.role));
const pagedMyRepairs = computed(() => {
  const start = (myPage.value - 1) * myPageSize.value;
  return myRepairs.value.slice(start, start + myPageSize.value);
});
const pagedBoardRepairs = computed(() => {
  const start = (boardPage.value - 1) * boardPageSize.value;
  return boardRepairs.value.slice(start, start + boardPageSize.value);
});

const rateVisible = ref(false);
const currentRateRepairId = ref(null);
const rateForm = reactive({
  rating: 5,
  comment: '',
});

const assignVisible = ref(false);
const assignTarget = ref(null);
const assignProviderId = ref(null);

const statusTextMap = {
  PENDING: '待受理',
  IN_PROGRESS: '处理中',
  COMPLETED: '已完成',
};

function normalizeRepair(item) {
  return {
    ...item,
    updatedAtText: formatDateTime(item.updatedAt, { withSeconds: false }),
    updatedAtRelative: formatRelativeTime(item.updatedAt),
  };
}

async function loadMine() {
  loadingMine.value = true;
  try {
    myRepairs.value = (await repairApi.mine()).map(normalizeRepair);
    if (myPage.value > Math.ceil(Math.max(myRepairs.value.length, 1) / myPageSize.value)) {
      myPage.value = 1;
    }
  } finally {
    loadingMine.value = false;
  }
}

async function loadBoard() {
  if (!canManageRepairs.value) return;
  const [board, providerList] = await Promise.all([repairApi.board(), systemApi.providers()]);
  boardRepairs.value = board.map(normalizeRepair);
  providers.value = providerList;
  if (boardPage.value > Math.ceil(Math.max(boardRepairs.value.length, 1) / boardPageSize.value)) {
    boardPage.value = 1;
  }
}

function statusTagType(status) {
  if (status === 'COMPLETED') {
    return 'success';
  }
  if (status === 'IN_PROGRESS') {
    return 'warning';
  }
  return 'info';
}

function beforeImageUpload(rawFile) {
  const isImage = rawFile.type.startsWith('image/');
  const isLt5M = rawFile.size / 1024 / 1024 < 5;
  if (!isImage) {
    ElMessage.error('仅支持上传图片文件');
    return false;
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB');
    return false;
  }
  return true;
}

async function handleImageUpload(options) {
  uploadingImage.value = true;
  try {
    const data = await repairApi.uploadImage(options.file);
    uploadedImageUrl.value = data.imageUrl;
    uploadedFileName.value = options.file.name;
    ElMessage.success('图片上传成功');
    options.onSuccess?.(data);
  } catch (error) {
    options.onError?.(error);
  } finally {
    uploadingImage.value = false;
  }
}

function removeUploadedImage() {
  uploadedImageUrl.value = '';
  uploadedFileName.value = '';
  uploaderRef.value?.clearFiles?.();
}

async function submitRepair() {
  const title = createForm.title?.trim() || '';
  const description = createForm.description?.trim() || '';
  if (title.length < 4) {
    ElMessage.warning('报修标题至少4个字符');
    return;
  }
  if (description.length < 8) {
    ElMessage.warning('报修描述至少8个字符');
    return;
  }
  submitting.value = true;
  try {
    await repairApi.create({
      title,
      description,
      imageUrl: uploadedImageUrl.value || null,
    });
    ElMessage.success('报修提交成功');
    createForm.title = '';
    createForm.description = '';
    removeUploadedImage();
    await loadMine();
    await loadBoard();
  } finally {
    submitting.value = false;
  }
}

async function updateStatus(row, status) {
  await repairApi.updateStatus(row.id, {
    status,
    assignedProviderId: row.assignedProviderId,
  });
  ElMessage.success('状态已更新');
  await loadBoard();
  await loadMine();
}

function openRate(row) {
  currentRateRepairId.value = row.id;
  rateForm.rating = 5;
  rateForm.comment = '';
  rateVisible.value = true;
}

async function submitRate() {
  await repairApi.rate(currentRateRepairId.value, {
    rating: rateForm.rating,
    comment: rateForm.comment,
  });
  ElMessage.success('评价已提交');
  rateVisible.value = false;
  await loadMine();
}

function openAssign(row) {
  assignTarget.value = row;
  assignProviderId.value = row.assignedProviderId;
  assignVisible.value = true;
}

async function confirmAssign() {
  if (!assignProviderId.value) {
    ElMessage.warning('请选择服务商');
    return;
  }
  await repairApi.updateStatus(assignTarget.value.id, {
    status: assignTarget.value.status,
    assignedProviderId: assignProviderId.value,
  });
  ElMessage.success('指派成功');
  assignVisible.value = false;
  await loadBoard();
}

function handleMySizeChange(size) {
  myPageSize.value = size;
  myPage.value = 1;
}

function handleBoardSizeChange(size) {
  boardPageSize.value = size;
  boardPage.value = 1;
}

onMounted(async () => {
  await loadMine();
  await loadBoard();
});
</script>

<style scoped>
.repairs-page {
  display: grid;
  gap: 12px;
}
.submit-card,
.list-card,
.board-card {
  padding: 20px;
}
.submit-card {
  background: linear-gradient(165deg, rgba(255, 255, 255, 0.92), rgba(232, 248, 255, 0.88));
}
.repair-form {
  margin-top: 12px;
}

.image-uploader {
  width: 100%;
}
.image-uploader :deep(.el-upload) {
  width: 100%;
}
.image-uploader :deep(.el-upload-dragger) {
  border: 0;
  padding: 0;
  width: 100%;
  background: transparent;
}
.upload-placeholder,
.upload-preview {
  width: 100%;
  min-height: 132px;
  border-radius: 14px;
  overflow: hidden;
}
.upload-placeholder {
  border: 1px dashed rgba(31, 74, 112, 0.32);
  background: linear-gradient(135deg, rgba(230, 246, 255, 0.88), rgba(233, 251, 243, 0.9));
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #2b4f68;
  font-weight: 700;
}
.upload-placeholder .sub {
  margin: 0;
  font-size: 12px;
  font-weight: 500;
  color: #638094;
}
.upload-preview {
  position: relative;
  border: 1px solid rgba(32, 64, 96, 0.14);
}
.upload-preview img {
  width: 100%;
  height: 132px;
  object-fit: cover;
  display: block;
}
.upload-overlay {
  position: absolute;
  inset: auto 0 0 0;
  padding: 10px 12px;
  background: linear-gradient(to top, rgba(9, 28, 44, 0.75), rgba(9, 28, 44, 0.2));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.status-actions {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.mini-chip {
  border: 0;
  border-radius: 999px;
  padding: 5px 10px;
  cursor: pointer;
  font-weight: 700;
  color: #20445e;
  background: rgba(25, 88, 140, 0.14);
}
.provider-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}
.provider-item {
  border: 1px solid rgba(32, 67, 96, 0.16);
  border-radius: 12px;
  background: #fff;
  padding: 10px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  cursor: pointer;
}
.provider-item strong {
  color: #1e3f56;
}
.provider-item span {
  color: #637987;
  font-size: 0.83rem;
}
.provider-item.active {
  border-color: #0f9f84;
  background: rgba(15, 159, 132, 0.1);
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
  color: #1a3d57;
  font-size: 0.92rem;
}
.time-cell span {
  color: #738897;
  font-size: 0.82rem;
}
@media (max-width: 760px) {
  .provider-grid {
    grid-template-columns: 1fr;
  }
  .pager-wrap {
    justify-content: center;
  }
}
</style>
