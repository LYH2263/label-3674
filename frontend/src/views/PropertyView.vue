<template>
  <div>
    <section v-if="authStore.user?.role !== 'RESIDENT'" class="panel-card no-access">
      <h2>仅居民可访问物业服务模块</h2>
      <p>当前账号角色可使用报修、活动和消息中心等功能。</p>
    </section>

    <div v-else class="property-page">
      <section class="panel-card summary-card">
        <article>
          <h4>{{ unpaidCount }}</h4>
          <p>待缴物业费</p>
        </article>
        <article>
          <h4>{{ overview.parkingSlots.filter((item) => item.status === 'FREE').length }}</h4>
          <p>空闲车位</p>
        </article>
        <article>
          <h4>{{ overview.visitors.length }}</h4>
          <p>访客记录</p>
        </article>
      </section>

      <section class="panel-card visitor-card">
        <header>
          <h2 class="section-title">物业服务中心</h2>
          <p class="section-subtitle">物业费、停车位、访客登记统一办理</p>
        </header>
        <el-form label-position="top" class="visitor-form">
          <el-row :gutter="12">
            <el-col :md="12" :sm="24">
              <el-form-item label="访客姓名">
                <el-input v-model="visitorForm.visitorName" placeholder="请输入访客姓名" />
              </el-form-item>
            </el-col>
            <el-col :md="12" :sm="24">
              <el-form-item label="到访时间">
                <el-date-picker
                  v-model="visitorForm.visitTime"
                  type="datetime"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                  placeholder="请选择到访时间"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <div class="visitor-actions">
            <p>登记完成后会同步写入访客记录，便于物业核验和后续查询。</p>
            <el-button type="primary" :loading="submittingVisitor" @click="submitVisitor">登记访客</el-button>
          </div>
        </el-form>
      </section>

      <section class="panel-card">
        <h3 class="section-title">物业费记录</h3>
        <el-table :data="pagedPayments" border empty-text="暂无数据">
          <el-table-column prop="id" label="编号" width="80" />
          <el-table-column label="金额" width="120">
            <template #default="{ row }">¥ {{ Number(row.amount).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.payStatus === 'PAID' ? 'success' : 'warning'">
                {{ row.payStatus === 'PAID' ? '已缴费' : '待缴费' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="paidAtText" label="支付时间" min-width="160" />
          <el-table-column label="操作" width="140">
            <template #default="{ row }">
              <el-button v-if="row.payStatus !== 'PAID'" size="small" type="primary" @click="openPayDialog(row)">在线缴纳</el-button>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>
        <div class="pager-wrap">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next"
            :current-page="paymentPage"
            :page-size="paymentPageSize"
            :page-sizes="[5, 8, 10, 20]"
            :total="overview.payments.length"
            @current-change="(val) => (paymentPage = val)"
            @size-change="handlePaymentSizeChange"
          />
        </div>
      </section>

      <section class="panel-card">
        <h3 class="section-title">在线缴费流水</h3>
        <el-table :data="pagedTransactions" border empty-text="暂无数据">
          <el-table-column prop="payNo" label="支付流水号" min-width="190" />
          <el-table-column prop="channelText" label="渠道" width="120" />
          <el-table-column label="金额" width="120">
            <template #default="{ row }">¥ {{ Number(row.amount).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'info'">{{ row.statusText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAtText" label="创建时间" min-width="160" />
          <el-table-column prop="paidAtText" label="支付时间" min-width="160" />
        </el-table>
        <div class="pager-wrap">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next"
            :current-page="txPage"
            :page-size="txPageSize"
            :page-sizes="[5, 8, 10, 20]"
            :total="overview.paymentTransactions.length"
            @current-change="(val) => (txPage = val)"
            @size-change="handleTxSizeChange"
          />
        </div>
      </section>

      <section class="panel-card">
        <h3 class="section-title">停车位管理</h3>
        <div class="parking-grid">
          <div v-for="slot in overview.parkingSlots" :key="slot.slotNo" :class="['slot', slot.status === 'FREE' ? 'free' : 'used']">
            <strong>{{ slot.slotNo }}</strong>
            <span>{{ slot.status === 'FREE' ? '空闲' : '已占用' }}</span>
          </div>
        </div>
      </section>

      <section class="panel-card">
        <h3 class="section-title">访客记录</h3>
        <el-table :data="pagedVisitors" border empty-text="暂无数据">
          <el-table-column prop="id" label="编号" width="80" />
          <el-table-column prop="visitorName" label="访客" min-width="120" />
          <el-table-column prop="visitTimeText" label="到访时间" min-width="170" />
          <el-table-column prop="status" label="状态" width="120" />
        </el-table>
        <div class="pager-wrap">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next"
            :current-page="visitorPage"
            :page-size="visitorPageSize"
            :page-sizes="[5, 8, 10, 20]"
            :total="overview.visitors.length"
            @current-change="(val) => (visitorPage = val)"
            @size-change="handleVisitorSizeChange"
          />
        </div>
      </section>
    </div>

    <el-dialog v-model="payVisible" title="物业费在线缴纳" width="520px">
      <el-form label-position="top">
        <el-form-item label="支付渠道">
          <el-radio-group v-model="payForm.channel">
            <el-radio label="ALIPAY">支付宝</el-radio>
            <el-radio label="WECHAT">微信支付</el-radio>
            <el-radio label="BANK_CARD">银行卡</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <div v-if="payIntent" class="pay-intent-box">
        <img :src="payQrDataUrl" alt="支付二维码" />
        <div class="pay-intent-meta">
          <span>金额：¥ {{ Number(payIntent.amount).toFixed(2) }}</span>
          <span>流水号：{{ payIntent.payNo }}</span>
        </div>
      </div>

      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button :loading="payIntentLoading" @click="createPayIntent">生成支付单</el-button>
        <el-button type="primary" :disabled="!payIntent" :loading="confirmLoading" @click="confirmPayment">确认已支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import QRCode from 'qrcode';
import { ElMessage } from 'element-plus';
import { propertyApi } from '../api';
import { useAuthStore } from '../stores/auth';
import { formatDateTime } from '../utils/datetime';
import { muteRealtimeNotification } from '../utils/realtimeNotification';

const authStore = useAuthStore();
const submittingVisitor = ref(false);
const payVisible = ref(false);
const payIntentLoading = ref(false);
const confirmLoading = ref(false);
const currentPayment = ref(null);
const payIntent = ref(null);
const payQrDataUrl = ref('');

const paymentPage = ref(1);
const paymentPageSize = ref(8);
const txPage = ref(1);
const txPageSize = ref(8);
const visitorPage = ref(1);
const visitorPageSize = ref(8);

const payForm = reactive({
  channel: 'ALIPAY',
});

const overview = reactive({
  payments: [],
  paymentTransactions: [],
  parkingSlots: [],
  visitors: [],
});

const visitorForm = reactive({
  visitorName: '',
  visitTime: '',
});

const channelMap = {
  ALIPAY: '支付宝',
  WECHAT: '微信支付',
  BANK_CARD: '银行卡',
};

const unpaidCount = computed(() => overview.payments.filter((item) => item.payStatus !== 'PAID').length);

const pagedPayments = computed(() => {
  const start = (paymentPage.value - 1) * paymentPageSize.value;
  return overview.payments.slice(start, start + paymentPageSize.value);
});

const pagedTransactions = computed(() => {
  const start = (txPage.value - 1) * txPageSize.value;
  return overview.paymentTransactions.slice(start, start + txPageSize.value);
});

const pagedVisitors = computed(() => {
  const start = (visitorPage.value - 1) * visitorPageSize.value;
  return overview.visitors.slice(start, start + visitorPageSize.value);
});

function normalizeOverview(data) {
  overview.payments = (data.payments || []).map((item) => ({
    ...item,
    paidAtText: formatDateTime(item.paidAt),
  }));
  overview.paymentTransactions = (data.paymentTransactions || []).map((item) => ({
    ...item,
    channelText: channelMap[item.channel] || item.channel,
    statusText: item.status === 'SUCCESS' ? '支付成功' : '待支付',
    createdAtText: formatDateTime(item.createdAt),
    paidAtText: formatDateTime(item.paidAt),
  }));
  overview.parkingSlots = data.parkingSlots || [];
  overview.visitors = (data.visitors || []).map((item) => ({
    ...item,
    visitTimeText: formatDateTime(item.visitTime),
  }));
}

async function loadOverview() {
  if (authStore.user?.role !== 'RESIDENT') return;
  const data = await propertyApi.overview();
  normalizeOverview(data);
}

async function submitVisitor() {
  if (!visitorForm.visitorName?.trim()) {
    ElMessage.warning('请输入访客姓名');
    return;
  }
  if (!visitorForm.visitTime) {
    ElMessage.warning('请选择到访时间');
    return;
  }
  submittingVisitor.value = true;
  try {
    muteRealtimeNotification('访客登记完成');
    await propertyApi.createVisitor(visitorForm);
    visitorForm.visitorName = '';
    visitorForm.visitTime = '';
    ElMessage.success('访客登记成功');
    await loadOverview();
  } finally {
    submittingVisitor.value = false;
  }
}

function openPayDialog(payment) {
  currentPayment.value = payment;
  payIntent.value = null;
  payQrDataUrl.value = '';
  payVisible.value = true;
}

async function createPayIntent() {
  if (!currentPayment.value) return;
  payIntentLoading.value = true;
  try {
    const intent = await propertyApi.createPayIntent(currentPayment.value.id, {
      channel: payForm.channel,
    });
    payIntent.value = intent;
    payQrDataUrl.value = await QRCode.toDataURL(intent.qrPayload, { width: 200, margin: 1 });
    ElMessage.success('支付单已生成');
  } finally {
    payIntentLoading.value = false;
  }
}

async function confirmPayment() {
  if (!currentPayment.value || !payIntent.value) {
    return;
  }
  confirmLoading.value = true;
  try {
    await propertyApi.confirmPayment(currentPayment.value.id, {
      payNo: payIntent.value.payNo,
    });
    ElMessage.success('在线缴费成功');
    payVisible.value = false;
    await loadOverview();
  } finally {
    confirmLoading.value = false;
  }
}

function handlePaymentSizeChange(size) {
  paymentPageSize.value = size;
  paymentPage.value = 1;
}

function handleTxSizeChange(size) {
  txPageSize.value = size;
  txPage.value = 1;
}

function handleVisitorSizeChange(size) {
  visitorPageSize.value = size;
  visitorPage.value = 1;
}

onMounted(loadOverview);
</script>

<style scoped>
.no-access {
  padding: 28px;
  text-align: center;
}
.no-access h2 {
  margin: 0;
}
.no-access p {
  margin: 10px 0 0;
  color: #687e8c;
}
.property-page {
  display: grid;
  gap: 12px;
}
.property-page section {
  padding: 20px;
}
.summary-card {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}
.summary-card article {
  border: 1px solid rgba(22, 58, 83, 0.1);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.74);
  padding: 14px;
}
.summary-card h4 {
  margin: 0;
  font-size: 1.6rem;
  color: #173850;
}
.summary-card p {
  margin: 4px 0 0;
  color: #6a7f8e;
}
.visitor-form {
  margin-top: 12px;
}
.visitor-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 6px;
}
.visitor-actions p {
  margin: 0;
  color: #688090;
  line-height: 1.5;
}
.parking-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(4, minmax(90px, 1fr));
  gap: 10px;
}
.slot {
  border-radius: 12px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.slot.free {
  background: rgba(15, 164, 130, 0.12);
  color: #0f6e59;
}
.slot.used {
  background: rgba(236, 147, 72, 0.16);
  color: #8f4b16;
}
.pager-wrap {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}
.pay-intent-box {
  margin-top: 12px;
  border: 1px solid rgba(26, 63, 90, 0.12);
  border-radius: 12px;
  padding: 12px;
  display: grid;
  justify-items: center;
  gap: 8px;
}
.pay-intent-box img {
  width: 200px;
  height: 200px;
}
.pay-intent-meta {
  width: 100%;
  display: grid;
  gap: 6px;
}
.pay-intent-meta span {
  display: block;
  padding: 9px 12px;
  border-radius: 10px;
  background: rgba(21, 60, 86, 0.06);
  color: #5f7483;
  text-align: center;
  word-break: break-all;
}
@media (max-width: 900px) {
  .summary-card {
    grid-template-columns: 1fr;
  }
  .visitor-actions {
    flex-direction: column;
    align-items: stretch;
  }
  .visitor-actions .el-button {
    width: 100%;
  }
  .parking-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .pager-wrap {
    justify-content: center;
  }
}
</style>
