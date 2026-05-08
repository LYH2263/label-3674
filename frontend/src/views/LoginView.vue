<template>
  <div class="auth-layout">
    <section class="intro-card panel-card">
      <p class="eyebrow">Smart Community Service Platform</p>
      <h2>连接居民、物业与服务商的数字协作中枢</h2>
      <ul>
        <li>实名认证注册与角色权限控制</li>
        <li>报修全流程跟踪与服务评价</li>
        <li>活动组织、消息通知、个人中心一体化</li>
      </ul>
    </section>

    <section class="form-card panel-card">
      <div class="switcher">
        <button :class="{ active: mode === 'login' }" @click="mode = 'login'">登录</button>
        <button :class="{ active: mode === 'register' }" @click="mode = 'register'">注册</button>
      </div>

      <el-form ref="formRef" label-position="top" class="form-body">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>

        <template v-if="mode === 'register'">
          <el-form-item label="角色">
            <div class="role-picker">
              <button
                v-for="item in roleOptions"
                :key="item.value"
                type="button"
                :class="['role-item', { active: form.role === item.value }]"
                @click="form.role = item.value"
              >
                {{ item.label }}
              </button>
            </div>
          </el-form-item>

          <el-form-item label="姓名">
            <el-input v-model="form.fullName" placeholder="请输入真实姓名" />
          </el-form-item>

          <el-form-item label="手机号">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>

          <el-form-item label="邮箱">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
          </el-form-item>

          <el-form-item v-if="form.role === 'RESIDENT'" label="身份证号（实名认证）">
            <el-input v-model="form.idCardNo" placeholder="请输入18位身份证号" />
          </el-form-item>
        </template>

        <el-button :loading="submitting" class="submit-btn" type="primary" @click="handleSubmit">
          {{ mode === 'login' ? '进入平台' : '注册并进入' }}
        </el-button>
      </el-form>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { z } from 'zod';
import { useAuthStore } from '../stores/auth';

const router = useRouter();
const authStore = useAuthStore();
const mode = ref('login');
const submitting = ref(false);

const roleOptions = [
  { label: '居民', value: 'RESIDENT' },
  { label: '物业管理员', value: 'PROPERTY_ADMIN' },
  { label: '服务商', value: 'SERVICE_PROVIDER' },
];

const form = reactive({
  username: '',
  password: '',
  role: 'RESIDENT',
  fullName: '',
  phone: '',
  email: '',
  idCardNo: '',
});

const loginSchema = z.object({
  username: z.string().min(4, '用户名至少4位'),
  password: z.string().min(6, '密码至少6位'),
});

const registerSchema = loginSchema.extend({
  role: z.enum(['RESIDENT', 'PROPERTY_ADMIN', 'SERVICE_PROVIDER']),
  fullName: z.string().min(2, '姓名至少2位'),
  phone: z.string().regex(/^1\d{10}$/, '手机号格式错误').optional().or(z.literal('')),
  email: z.string().email('邮箱格式错误').optional().or(z.literal('')),
  idCardNo: z.string().optional(),
});

async function handleSubmit() {
  submitting.value = true;
  try {
    if (mode.value === 'login') {
      loginSchema.parse(form);
      await authStore.login({ username: form.username, password: form.password });
      ElMessage.success('登录成功');
    } else {
      registerSchema.parse(form);
      if (form.role === 'RESIDENT' && !/^\d{17}[\dXx]$/.test(form.idCardNo)) {
        throw new Error('居民需填写有效身份证号');
      }
      await authStore.register({
        username: form.username,
        password: form.password,
        role: form.role,
        fullName: form.fullName,
        phone: form.phone,
        email: form.email,
        idCardNo: form.idCardNo,
      });
      ElMessage.success('注册成功');
    }
    await router.push('/dashboard');
  } catch (error) {
    if (error?.issues?.[0]?.message) {
      ElMessage.error(error.issues[0].message);
    }
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.auth-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 20px;
  align-items: center;
  animation: fadeIn 0.5s ease;
}
.intro-card,
.form-card {
  padding: 34px;
}
.eyebrow {
  margin: 0;
  font-size: 0.82rem;
  color: #0f8b72;
  font-family: var(--font-title);
  letter-spacing: 0.06em;
  text-transform: uppercase;
}
.intro-card h2 {
  margin: 14px 0 18px;
  font-size: 1.9rem;
  line-height: 1.3;
}
.intro-card ul {
  margin: 0;
  padding-left: 18px;
  color: #536677;
  display: grid;
  gap: 10px;
}
.switcher {
  display: flex;
  padding: 4px;
  border-radius: 14px;
  background: rgba(27, 44, 64, 0.06);
  margin-bottom: 20px;
}
.switcher button {
  flex: 1;
  border: 0;
  background: transparent;
  padding: 11px;
  border-radius: 12px;
  font-weight: 700;
  color: #566878;
  cursor: pointer;
}
.switcher button.active {
  background: #fff;
  color: #122a3d;
  box-shadow: 0 8px 14px rgba(20, 52, 80, 0.15);
}
.form-body {
  margin-top: 10px;
}
.role-picker {
  display: flex;
  gap: 8px;
}
.role-item {
  border: 1px solid rgba(34, 65, 87, 0.18);
  background: #fff;
  border-radius: 12px;
  padding: 10px 12px;
  font-weight: 600;
  color: #314657;
  cursor: pointer;
  min-width: 90px;
}
.role-item.active {
  border-color: #0f9f85;
  background: rgba(15, 159, 133, 0.12);
  color: #0f7d69;
}
.submit-btn {
  width: 100%;
  height: 44px;
  margin-top: 8px;
  border: 0;
  background: linear-gradient(90deg, #0ea585, #3fbec2);
}
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
@media (max-width: 960px) {
  .auth-layout {
    grid-template-columns: 1fr;
    padding: 20px 0;
  }
}
</style>
