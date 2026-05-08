import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const routes = [
  { path: '/login', component: () => import('../views/LoginView.vue') },
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', component: () => import('../views/DashboardView.vue'), meta: { requiresAuth: true } },
  { path: '/profile', component: () => import('../views/ProfileView.vue'), meta: { requiresAuth: true } },
  { path: '/repairs', component: () => import('../views/RepairsView.vue'), meta: { requiresAuth: true } },
  { path: '/activities', component: () => import('../views/ActivitiesView.vue'), meta: { requiresAuth: true } },
  { path: '/neighborhood', component: () => import('../views/NeighborhoodView.vue'), meta: { requiresAuth: true } },
  { path: '/messages', component: () => import('../views/MessagesView.vue'), meta: { requiresAuth: true } },
  { path: '/property', component: () => import('../views/PropertyView.vue'), meta: { requiresAuth: true } },
  { path: '/api-docs', component: () => import('../views/ApiDocsView.vue') },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to) => {
  const authStore = useAuthStore();
  if (to.meta.requiresAuth && !authStore.token) {
    return '/login';
  }
  if (to.path === '/login' && authStore.token) {
    return '/dashboard';
  }
  return true;
});

export default router;
