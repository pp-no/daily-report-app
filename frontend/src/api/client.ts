import axios from 'axios';

/**
 * アプリ共通のaxiosインスタンス
 * baseURLは .env の VITE_API_URL（例: http://localhost:8080）を使用
 * withCredentials: true により HttpOnly Cookie が自動送信される
 */
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  withCredentials: true,
});

export default apiClient;
