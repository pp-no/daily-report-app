import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import Layout from '../components/Layout';
import useIsMobile from '../hooks/useIsMobile';
import apiClient from '../api/client';
import type { ApiErrorResponse } from '../types/api';

/** GET /api/users/me のレスポンス型 */
type UserProfile = {
  id: number;
  name: string;
  email: string;
  workStartTime: string; // "HH:mm:ss" 形式で返ってくる
  notificationEnabled: boolean;
};

const ProfilePage = () => {
  const isMobile = useIsMobile();
  const navigate = useNavigate();

  // プロフィール情報フォームの状態
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');

  // 通知設定フォームの状態
  const [workStartTime, setWorkStartTime] = useState('09:00');
  const [notificationEnabled, setNotificationEnabled] = useState(true);

  const [profileMessage, setProfileMessage] = useState('');
  const [notifMessage, setNotifMessage] = useState('');
  const [testMailMessage, setTestMailMessage] = useState('');
  const [testMailSending, setTestMailSending] = useState(false);

  // 画面表示時にプロフィール情報を取得する
  useEffect(() => {
    apiClient.get<UserProfile>('/api/users/me').then((res) => {
      setName(res.data.name);
      setEmail(res.data.email);
      // "HH:mm:ss" → "HH:mm" に変換して <input type="time"> に渡す
      setWorkStartTime(res.data.workStartTime.slice(0, 5));
      setNotificationEnabled(res.data.notificationEnabled);
    });
  }, []);

  // プロフィール情報（名前・メール）を更新する
  const handleProfileSave = async () => {
    try {
      await apiClient.put('/api/users/me', {
        name,
        email,
        workStartTime: `${workStartTime}:00`, // "HH:mm" → "HH:mm:ss" に戻す
        notificationEnabled,
      });
      setProfileMessage('プロフィールを更新しました');
      setTimeout(() => setProfileMessage(''), 3000);
    } catch {
      setProfileMessage('更新に失敗しました');
    }
  };

  // パスワード変更フォームの状態
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [passwordMessage, setPasswordMessage] = useState('');
  const [passwordError, setPasswordError] = useState('');

  // パスワードを変更する
  const handlePasswordChange = async () => {
    setPasswordMessage('');
    setPasswordError('');

    if (newPassword !== confirmPassword) {
      setPasswordError('新しいパスワードと確認用パスワードが一致しません');
      return;
    }
    if (newPassword.length < 8) {
      setPasswordError('パスワードは8文字以上で入力してください');
      return;
    }

    try {
      await apiClient.put('/api/users/me/password', {
        currentPassword,
        newPassword,
        confirmPassword,
      });
      setPasswordMessage('パスワードを変更しました');
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      setTimeout(() => setPasswordMessage(''), 3000);
    } catch (err) {
      const msg = axios.isAxiosError<ApiErrorResponse>(err)
        ? err.response?.data?.message
        : undefined;
      setPasswordError(msg ?? 'パスワードの変更に失敗しました');
    }
  };

  // 通知設定（業務開始時刻・通知ON/OFF）を更新する
  const handleNotifSave = async () => {
    try {
      await apiClient.put('/api/users/me', {
        name,
        email,
        workStartTime: `${workStartTime}:00`,
        notificationEnabled,
      });
      setNotifMessage('通知設定を保存しました');
      setTimeout(() => setNotifMessage(''), 3000);
    } catch {
      setNotifMessage('保存に失敗しました');
    }
  };

  // テストメールを即時送信する
  const handleTestMail = async () => {
    setTestMailSending(true);
    setTestMailMessage('');
    try {
      await apiClient.post('/api/notifications/send');
      setTestMailMessage('送信しました。受信ボックスを確認してください。');
    } catch {
      setTestMailMessage('送信に失敗しました。');
    } finally {
      setTestMailSending(false);
      setTimeout(() => setTestMailMessage(''), 5000);
    }
  };

  // ログアウト：トークンを削除して /login へ遷移
  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login', { replace: true });
  };

  const gridStyle: React.CSSProperties = {
    display: 'grid',
    gridTemplateColumns: isMobile ? '1fr' : '1fr 1fr',
    gap: 24,
    alignItems: 'start',
  };

  const containerStyle: React.CSSProperties = {
    padding: isMobile ? '16px' : '32px 40px',
  };

  return (
    <Layout>
      <div style={containerStyle}>
        <div style={S.header}>
          <h1 style={S.title}>プロフィール設定</h1>
          <p style={S.subtitle}>プロフィール情報と通知設定を管理しましょう</p>
        </div>

        <div style={gridStyle}>
          {/* 左カラム：プロフィール情報 + パスワード変更 */}
          <div>
            <div style={S.card}>
              <h2 style={S.cardTitle}>プロフィール情報</h2>
              <div style={S.avatarArea}>
                <div style={S.avatar}>{name.charAt(0) || 'U'}</div>
              </div>
              <div style={S.field}>
                <label style={S.label}>名前</label>
                <input
                  type="text"
                  style={S.input}
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                />
              </div>
              <div style={S.field}>
                <label style={S.label}>メールアドレス</label>
                <input
                  type="email"
                  style={S.input}
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>
              {profileMessage && <p style={S.message}>{profileMessage}</p>}
              <button style={S.primaryButton} onClick={handleProfileSave}>
                プロフィールを更新
              </button>
            </div>

            <div style={{ ...S.card, marginTop: 16 }}>
              <h2 style={S.cardTitle}>パスワード変更</h2>
              <div style={S.field}>
                <label style={S.label}>現在のパスワード</label>
                <input
                  type="password"
                  style={S.input}
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  placeholder="現在のパスワード"
                />
              </div>
              <div style={S.field}>
                <label style={S.label}>新しいパスワード</label>
                <input
                  type="password"
                  style={S.input}
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="8文字以上"
                />
              </div>
              <div style={S.field}>
                <label style={S.label}>新しいパスワード（確認）</label>
                <input
                  type="password"
                  style={S.input}
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="もう一度入力"
                />
              </div>
              {passwordError && <p style={S.errorMessage}>{passwordError}</p>}
              {passwordMessage && <p style={S.message}>{passwordMessage}</p>}
              <button style={S.primaryButton} onClick={handlePasswordChange}>
                パスワードを変更
              </button>
            </div>
          </div>

          {/* 右カラム：通知設定 */}
          <div>
            <div style={S.card}>
              <h2 style={S.cardTitle}>通知設定</h2>
              <div style={S.field}>
                <label style={S.label}>業務開始時刻</label>
                <input
                  type="time"
                  style={S.input}
                  value={workStartTime}
                  onChange={(e) => setWorkStartTime(e.target.value)}
                />
              </div>
              <div style={S.field}>
                <label style={S.label}>メール通知</label>
                <label style={S.toggleLabel}>
                  <input
                    type="checkbox"
                    checked={notificationEnabled}
                    onChange={(e) => setNotificationEnabled(e.target.checked)}
                    style={{ marginRight: 8 }}
                  />
                  業務開始30分前に通知する
                </label>
              </div>
              {notifMessage && <p style={S.message}>{notifMessage}</p>}
              <button style={S.primaryButton} onClick={handleNotifSave}>
                通知設定を保存
              </button>
              <hr style={{ border: 'none', borderTop: '1px solid #e2e8f0', margin: '16px 0' }} />
              <p style={S.testMailDesc}>
                昨日の日報サマリーを今すぐ自分宛に送信します。
              </p>
              {testMailMessage && <p style={S.message}>{testMailMessage}</p>}
              <button
                style={S.secondaryButton}
                onClick={handleTestMail}
                disabled={testMailSending}
              >
                {testMailSending ? '送信中...' : '今すぐ送信'}
              </button>
            </div>

            <div style={{ ...S.card, marginTop: 16 }}>
              <h2 style={{ ...S.cardTitle, color: '#ef4444' }}>アカウント操作</h2>
              <button style={S.logoutButton} onClick={handleLogout}>
                ログアウト
              </button>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
};

const S = {
  header: { marginBottom: 28 },
  title: { fontSize: 22, fontWeight: 700, color: '#1e293b', margin: 0 },
  subtitle: { fontSize: 13, color: '#64748b', marginTop: 4 },
  card: {
    background: '#ffffff',
    border: '1px solid #e2e8f0',
    borderRadius: 8,
    padding: 24,
  },
  cardTitle: { fontSize: 15, fontWeight: 600, color: '#1e293b', marginBottom: 20 },
  avatarArea: { display: 'flex', justifyContent: 'center', marginBottom: 20 },
  avatar: {
    width: 64,
    height: 64,
    borderRadius: '50%',
    background: '#3b82f6',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    color: '#ffffff',
    fontSize: 24,
    fontWeight: 700,
  },
  field: { marginBottom: 16 },
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
    boxSizing: 'border-box' as const,
  },
  primaryButton: {
    background: '#3b82f6',
    color: '#ffffff',
    border: 'none',
    borderRadius: 6,
    padding: '9px 18px',
    fontSize: 13,
    fontWeight: 600,
    cursor: 'pointer',
    width: '100%',
    marginTop: 4,
  },
  toggleLabel: {
    display: 'flex',
    alignItems: 'center',
    fontSize: 13,
    color: '#374151',
    cursor: 'pointer',
  },
  logoutButton: {
    background: 'transparent',
    color: '#ef4444',
    border: '1px solid #ef4444',
    borderRadius: 6,
    padding: '9px 18px',
    fontSize: 13,
    cursor: 'pointer',
    width: '100%',
  },
  message: {
    fontSize: 12,
    color: '#3b82f6',
    marginBottom: 8,
    marginTop: 0,
  },
  errorMessage: {
    fontSize: 12,
    color: '#ef4444',
    marginBottom: 8,
    marginTop: 0,
  },
  testMailDesc: {
    fontSize: 12,
    color: '#64748b',
    marginBottom: 10,
    marginTop: 0,
  },
  secondaryButton: {
    background: 'transparent',
    color: '#3b82f6',
    border: '1px solid #3b82f6',
    borderRadius: 6,
    padding: '9px 18px',
    fontSize: 13,
    fontWeight: 600,
    cursor: 'pointer',
    width: '100%',
  },
};

export default ProfilePage;
