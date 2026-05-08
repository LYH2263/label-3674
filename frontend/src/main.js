import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import { ElMessage } from 'element-plus';
import zhCn from 'element-plus/es/locale/lang/zh-cn';
import 'element-plus/dist/index.css';
import App from './App.vue';
import router from './router';
import './styles/theme.css';

const app = createApp(App);

app.config.errorHandler = (error) => {
  console.error(error);
  if (error?.__handledByInterceptor) {
    return;
  }
  ElMessage.error('操作失败，请稍后重试');
};

window.addEventListener('unhandledrejection', (event) => {
  if (event.reason?.__handledByInterceptor) {
    event.preventDefault();
    return;
  }
  event.preventDefault();
  ElMessage.error('请求异常，请稍后重试');
});

app.use(createPinia());
app.use(router);
app.use(ElementPlus, { locale: zhCn });
app.mount('#app');
