import axios from 'axios';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../stores/auth';

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 10000,
});

http.interceptors.request.use((config) => {
  const authStore = useAuthStore();
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => {
    const payload = response.data;
    if (payload?.code !== 0) {
      const handledError = new Error(payload?.message || '请求失败');
      handledError.__handledByInterceptor = true;
      ElMessage.error(handledError.message);
      return Promise.reject(handledError);
    }
    return payload.data;
  },
  (error) => {
    const authStore = useAuthStore();
    const message = error?.response?.data?.message || error.message || '网络异常';
    if (error?.__handledByInterceptor) {
      return Promise.reject(error);
    }
    if (error?.response?.status === 401) {
      authStore.logout();
      // Keep current page and only notify user; no exception-driven page redirect.
    }
    ElMessage.error(message);
    error.__handledByInterceptor = true;
    return Promise.reject(error);
  },
);

export default http;
