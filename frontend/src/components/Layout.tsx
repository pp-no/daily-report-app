import { useState } from 'react';
import Sidebar from './Sidebar';
import useIsMobile from '../hooks/useIsMobile';

/** 認証済みページ共通レイアウト（サイドバー + メインコンテンツ） */
interface Props {
  children: React.ReactNode;
}

/**
 * 認証済みページの共通レイアウトコンポーネント
 * デスクトップ：サイドバー左固定 + メインコンテンツ右側
 * モバイル：上部ヘッダーバー（ハンバーガーメニュー） + サイドバードロワー
 */
const Layout = ({ children }: Props) => {
  const isMobile = useIsMobile();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      <Sidebar
        isMobile={isMobile}
        isOpen={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
      />

      {/* サイドバー右側のコンテンツ領域 */}
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>
        {/* モバイル用ヘッダーバー：ハンバーガーボタン + ロゴ */}
        {isMobile && (
          <header style={S.mobileHeader}>
            <button
              style={S.hamburger}
              onClick={() => setSidebarOpen(true)}
              aria-label="メニューを開く"
            >
              ☰
            </button>
            <div style={S.mobileLogo}>
              <span style={S.mobileLogoIcon}>✏️</span>
              <span style={S.mobileLogoText}>DailyReport</span>
            </div>
          </header>
        )}

        <main style={{ flex: 1, background: '#f8fafc', overflowY: 'auto' }}>
          {children}
        </main>
      </div>
    </div>
  );
};

const S = {
  mobileHeader: {
    background: '#1a2536',
    padding: '12px 16px',
    display: 'flex',
    alignItems: 'center',
    gap: 12,
    flexShrink: 0,
    position: 'relative' as const,
  },
  hamburger: {
    background: 'transparent',
    border: 'none',
    color: '#ffffff',
    fontSize: 22,
    cursor: 'pointer',
    padding: '2px 4px',
    lineHeight: 1,
  },
  mobileLogo: {
    display: 'flex',
    alignItems: 'center',
    gap: 8,
  },
  mobileLogoIcon: {
    fontSize: 16,
  },
  mobileLogoText: {
    color: '#ffffff',
    fontWeight: 600,
    fontSize: 15,
  },
};

export default Layout;
