import Layout from '../components/Layout';
import useIsMobile from '../hooks/useIsMobile';

/** プロフィール設定画面（Phase 8でAPI実装予定） */
const ProfilePage = () => {
  const isMobile = useIsMobile();

  // モバイル：1カラム / デスクトップ：2カラム
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
          {/* 左カラム：プロフィール情報 */}
          <div>
            <div style={S.card}>
              <h2 style={S.cardTitle}>プロフィール情報</h2>
              <div style={S.avatarArea}>
                <div style={S.avatar}>U</div>
              </div>
              <div style={S.field}>
                <label style={S.label}>名前</label>
                <input type="text" style={S.input} placeholder="山田 太郎" />
              </div>
              <div style={S.field}>
                <label style={S.label}>メールアドレス</label>
                <input type="email" style={S.input} placeholder="yamada@example.com" />
              </div>
              <button style={S.primaryButton}>プロフィールを更新</button>
            </div>

            <div style={{ ...S.card, marginTop: 16 }}>
              <h2 style={S.cardTitle}>パスワード変更</h2>
              <div style={S.field}>
                <label style={S.label}>現在のパスワード</label>
                <input type="password" style={S.input} placeholder="••••••••" />
              </div>
              <div style={S.field}>
                <label style={S.label}>新しいパスワード</label>
                <input type="password" style={S.input} placeholder="••••••••" />
              </div>
              <div style={S.field}>
                <label style={S.label}>パスワード確認</label>
                <input type="password" style={S.input} placeholder="••••••••" />
              </div>
              <button style={S.primaryButton}>パスワードを変更</button>
            </div>
          </div>

          {/* 右カラム：通知設定 */}
          <div>
            <div style={S.card}>
              <h2 style={S.cardTitle}>通知設定</h2>
              <div style={S.field}>
                <label style={S.label}>業務開始時刻</label>
                <input type="time" style={S.input} defaultValue="09:00" />
              </div>
              <div style={S.field}>
                <label style={S.label}>メール通知</label>
                <label style={S.toggleLabel}>
                  <input type="checkbox" defaultChecked style={{ marginRight: 8 }} />
                  業務開始30分前に通知する
                </label>
              </div>
              <button style={S.primaryButton}>通知設定を保存</button>
            </div>

            <div style={{ ...S.card, marginTop: 16 }}>
              <h2 style={{ ...S.cardTitle, color: '#ef4444' }}>アカウント操作</h2>
              <button style={S.logoutButton}>ログアウト</button>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
};

/** スタイル定義 */
const S = {
  header: {
    marginBottom: 28,
  },
  title: {
    fontSize: 22,
    fontWeight: 700,
    color: '#1e293b',
    margin: 0,
  },
  subtitle: {
    fontSize: 13,
    color: '#64748b',
    marginTop: 4,
  },
  card: {
    background: '#ffffff',
    border: '1px solid #e2e8f0',
    borderRadius: 8,
    padding: 24,
  },
  cardTitle: {
    fontSize: 15,
    fontWeight: 600,
    color: '#1e293b',
    marginBottom: 20,
  },
  avatarArea: {
    display: 'flex',
    justifyContent: 'center',
    marginBottom: 20,
  },
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
};

export default ProfilePage;
