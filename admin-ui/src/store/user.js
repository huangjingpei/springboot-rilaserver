import { defineStore } from 'pinia';
import { login } from '../api/login';

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
  }),
  actions: {
    async login(userInfo) {
      const { username, password } = userInfo;
      const response = await login({ email: username.trim(), password: password });
      // Assuming the token is in the headers
      const token = response.headers.authorization;
      if (token) {
        this.token = token;
        localStorage.setItem('token', token);
      }
    },
    logout() {
      this.token = '';
      localStorage.removeItem('token');
    },
  },
});