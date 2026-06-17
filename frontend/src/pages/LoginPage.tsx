import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import apiClient from '../api/client';
import type { AuthResponse, LoginRequest } from '../types/auth';

/** ログイン画面 */
const LoginPage = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState<LoginRequest>({ email: '', password: '' });
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  /**
   * ログインフォーム送信
   * 成功時はJWTトークンをlocalStorageに保存して日報一覧へ遷移
   */
  const handleSubmit = async (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const response = await apiClient.post<AuthResponse>('/api/auth/login', form);
      localStorage.setItem('token', response.data.token);
      navigate('/reports');
    } catch {
      setError('メールアドレスまたはパスワードが正しくありません');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={S.page}>
      <div style={S.card}>
        {/* ロゴ */}
        <div style={S.logoArea}>
          <div style={S.logoIcon}>✏️</div>
          <div style={S.logoTitle}>DailyReport</div>
          <div style={S.logoSub}>日報管理システム</div>
        </div>

        {/* エラーメッセージ */}
        {error && <div style={S.error}>{error}</div>}

        {/* ログインフォーム */}
        <form onSubmit={handleSubmit}>
          <div style={S.field}>
            <label style={S.label}>メールアドレス</label>
            <input
              type="email"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              placeholder="example@example.com"
              style={S.input}
              required
            />
          </div>
          <div style={S.field}>
            <label style={S.label}>パスワード</label>
            <input
              type="password"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              placeholder="••••••••"
              style={S.input}
              required
            />
          </div>
          <button type="submit" style={S.button} disabled={loading}>
            {loading ? 'ログイン中...' : 'ログイン'}
          </button>
        </form>

        <p style={S.linkText}>
          アカウントをお持ちでない方は <Link to="/register">ユーザー登録</Link>
        </p>
      </div>
    </div>
  );
};

/** スタイル定義 */
const S = {
  page: {
    minHeight: '100vh',
    background: '#f1f5f9',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 16,
  },
  card: {
    background: '#ffffff',
    borderRadius: 12,
    padding: '40px 36px',
    width: '100%',
    maxWidth: 380,
    boxShadow: '0 4px 24px rgba(0,0,0,0.08)',
  },
  logoArea: {
    textAlign: 'center' as const,
    marginBottom: 28,
  },
  logoIcon: {
    fontSize: 28,
    marginBottom: 8,
  },
  logoTitle: {
    fontSize: 20,
    fontWeight: 700,
    color: '#1e293b',
    letterSpacing: '-0.3px',
  },
  logoSub: {
    fontSize: 12,
    color: '#94a3b8',
    marginTop: 4,
  },
  error: {
    background: '#fef2f2',
    color: '#ef4444',
    border: '1px solid #fecaca',
    borderRadius: 6,
    padding: '10px 12px',
    fontSize: 13,
    marginBottom: 16,
  },
  field: {
    marginBottom: 16,
  },
  label: {
    display: 'block',
    fontSize: 13,
    fontWeight: 500,
    color: '#374151',
    marginBottom: 6,
  },
  input: {
    width: '100%',
    padding: '9px 12px',
    border: '1px solid #e2e8f0',
    borderRadius: 6,
    fontSize: 14,
    color: '#1e293b',
    outline: 'none',
    transition: 'border-color 0.15s',
  },
  button: {
    width: '100%',
    padding: '10px 0',
    background: '#3b82f6',
    color: '#ffffff',
    border: 'none',
    borderRadius: 6,
    fontSize: 14,
    fontWeight: 600,
    cursor: 'pointer',
    marginTop: 8,
  },
  linkText: {
    textAlign: 'center' as const,
    fontSize: 13,
    color: '#64748b',
    marginTop: 20,
  },
};

export default LoginPage;
