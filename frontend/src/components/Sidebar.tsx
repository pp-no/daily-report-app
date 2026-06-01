import { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import apiClient from '../api/client';

/** サイドバーのナビゲーション項目の型 */
interface NavItem {
  path: string;
  label: string;
  icon: string;
}

/** サイドバーのProps */
interface SidebarProps {
  /** モバイル表示モードかどうか */
  isMobile?: boolean;
  /** モバイル時のドロワー開閉状態 */
  isOpen?: boolean;
  /** モバイル時のドロワーを閉じるコールバック */
  onClose?: () => void;
}

const NAV_ITEMS: NavItem[] = [
  { path: '/reports', label: '日報一覧', icon: '📋' },
  { path: '/public', label: '公開日報', icon: '🌐' },
  { path: '/settings', label: 'プロフィール設定', icon: '⚙️' },
];

/**
 * サイドバーコンポーネント
 * デスクトップ：左固定表示。モバイル：スライドインドロワーとして表示。
 * @param isMobile モバイル表示かどうか
 * @param isOpen モバイル時のドロワー開閉状態
 * @param onClose ドロワーを閉じるコールバック
 */
const Sidebar = ({ isMobile = false, isOpen = false, onClose }: SidebarProps) => {
  const navigate = useNavigate();
  const location = useLocation();
  const [userName, setUserName] = useState('');

  useEffect(() => {
    apiClient.get<{ name: string }>('/api/users/me').then((res) => {
      setUserName(res.data.name);
    });
  }, []);

  /** ログアウト処理：トークンを削除してログインページへ遷移 */
  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  /**
   * ナビゲーション項目クリック：ページ遷移し、モバイル時はドロワーを閉じる
   * @param path 遷移先パス
   */
  const handleNavClick = (path: string) => {
    navigate(path);
    if (isMobile) onClose?.();
  };

  // モバイル時はposition:fixedでスライドインする
  const sidebarStyle: React.CSSProperties = isMobile
    ? {
        ...S.sidebar,
        position: 'fixed',
        top: 0,
        left: 0,
        height: '100vh',
        zIndex: 1001,
        transform: isOpen ? 'translateX(0)' : 'translateX(-100%)',
        transition: 'transform 0.25s ease',
      }
    : S.sidebar;

  return (
    <>
      {/* モバイル時：バックドロップ（クリックでドロワーを閉じる） */}
      {isMobile && isOpen && (
        <div style={S.backdrop} onClick={onClose} />
      )}

      <div style={sidebarStyle}>
        {/* モバイル時：閉じるボタン */}
        {isMobile && (
          <button style={S.closeButton} onClick={onClose} aria-label="メニューを閉じる">
            ✕
          </button>
        )}

        {/* ロゴエリア */}
        <div style={S.logo}>
          <div style={S.logoIcon}>✏️</div>
          <div>
            <div style={S.logoText}>DailyReport</div>
            <div style={S.logoSub}>日報管理システム</div>
          </div>
        </div>

        {/* ナビゲーション */}
        <nav style={S.nav}>
          {NAV_ITEMS.map((item) => (
            <div
              key={item.path}
              style={S.navItem(location.pathname === item.path)}
              onClick={() => handleNavClick(item.path)}
            >
              <span style={S.navIcon}>{item.icon}</span>
              <span>{item.label}</span>
            </div>
          ))}
        </nav>

        {/* ユーザーエリア */}
        <div style={S.userArea}>
          <div style={S.avatar}>{userName.charAt(0).toUpperCase() || 'U'}</div>
          <div>
            <div style={S.userName}>{userName || '...'}</div>
            <div
              style={{ ...S.userEmail, cursor: 'pointer' }}
              onClick={handleLogout}
            >
              ログアウト
            </div>
          </div>
        </div>
      </div>
    </>
  );
};

const S = {
  sidebar: {
    width: 220,
    minHeight: '100vh',
    background: '#1a2536',
    display: 'flex',
    flexDirection: 'column' as const,
    flexShrink: 0,
  },
  backdrop: {
    position: 'fixed' as const,
    inset: 0,
    background: 'rgba(0,0,0,0.45)',
    zIndex: 1000,
  },
  closeButton: {
    position: 'absolute' as const,
    top: 12,
    right: 12,
    background: 'transparent',
    border: 'none',
    color: '#94a3b8',
    fontSize: 16,
    cursor: 'pointer',
    padding: '4px 6px',
    lineHeight: 1,
  },
  logo: {
    padding: '24px 20px',
    borderBottom: '1px solid rgba(255,255,255,0.08)',
    display: 'flex',
    alignItems: 'center',
    gap: 10,
  },
  logoIcon: {
    width: 32,
    height: 32,
    background: '#3b82f6',
    borderRadius: 8,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: 16,
    flexShrink: 0,
  },
  logoText: {
    color: '#ffffff',
    fontWeight: 600,
    fontSize: 15,
    lineHeight: 1.2,
  },
  logoSub: {
    color: '#64748b',
    fontSize: 11,
    marginTop: 2,
  },
  nav: {
    flex: 1,
    padding: '16px 0',
  },
  navItem: (active: boolean): React.CSSProperties => ({
    display: 'flex',
    alignItems: 'center',
    gap: 10,
    padding: '10px 20px',
    cursor: 'pointer',
    color: active ? '#ffffff' : '#94a3b8',
    background: active ? 'rgba(59,130,246,0.15)' : 'transparent',
    borderLeft: active ? '3px solid #3b82f6' : '3px solid transparent',
    fontSize: 14,
    transition: 'all 0.15s',
  }),
  navIcon: {
    fontSize: 15,
    width: 20,
    textAlign: 'center' as const,
  },
  userArea: {
    padding: '16px 20px',
    borderTop: '1px solid rgba(255,255,255,0.08)',
    display: 'flex',
    alignItems: 'center',
    gap: 10,
  },
  avatar: {
    width: 32,
    height: 32,
    borderRadius: '50%',
    background: '#3b82f6',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    color: '#ffffff',
    fontSize: 13,
    fontWeight: 600,
    flexShrink: 0,
  },
  userName: {
    color: '#cbd5e1',
    fontSize: 13,
    fontWeight: 500,
  },
  userEmail: {
    color: '#64748b',
    fontSize: 11,
    marginTop: 1,
  },
};

export default Sidebar;
