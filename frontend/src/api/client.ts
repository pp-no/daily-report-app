import axios from 'axios';

/**
 * アプリ共通のaxiosインスタンス
 * baseURLは .env の VITE_API_URL（例: http://localhost:8080）を使用
 * このインスタンスを通じてすべてのAPIリクエストを送る
 */
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
});

/**
 * リクエストインターセプター
 * apiClient経由のリクエストが送られる直前に毎回実行される
 * localStorageにJWTトークンがあれば Authorization ヘッダーに自動付与する
 * これにより各ページで手動でヘッダーをセットする必要がなくなる
 */
apiClient.interceptors.request.use((config) => {
  // ログイン時に保存したトークンを取得
  const token = localStorage.getItem('token');
  if (token) {
    // Spring Boot の JwtFilter が "Bearer " プレフィックスを期待するため付ける
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default apiClient;
