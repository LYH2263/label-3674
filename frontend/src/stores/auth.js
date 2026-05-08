import { defineStore } from 'pinia';
import { ref } from 'vue';
import { authApi } from '../api';

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '');
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'));

  function setAuth(payload) {
    token.value = payload.token;
    user.value = payload.user;
    localStorage.setItem('token', token.value);
    localStorage.setItem('user', JSON.stringify(user.value));
  }

  async function login(form) {
    const data = await authApi.login(form);
    setAuth(data);
  }

  async function register(form) {
    const data = await authApi.register(form);
    setAuth(data);
  }

  async function fetchMe() {
    if (!token.value) return;
    const profile = await authApi.me();
    user.value = {
      ...user.value,
      ...profile,
    };
    localStorage.setItem('user', JSON.stringify(user.value));
  }

  function logout() {
    token.value = '';
    user.value = null;
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }

  return {
    token,
    user,
    setAuth,
    login,
    register,
    fetchMe,
    logout,
  };
});
