import { useNavigate } from 'react-router-dom';
import { useReports } from '../hooks/useReports';
import Layout from '../components/Layout';
import useIsMobile from '../hooks/useIsMobile';
import type { DailyReport } from '../types/report';

/** 日報一覧画面 */
const ReportListPage = () => {
  const navigate = useNavigate();
  const isMobile = useIsMobile();
  const { reports, loading, error, deleteReport, fetchReports } = useReports();

  /**
   * 日報削除
   * @param id 削除する日報のID
   */
  const handleDelete = async (id: number) => {
    if (!confirm('この日報を削除しますか？')) return;
    await deleteReport(id);
    fetchReports();
  };

  const containerStyle: React.CSSProperties = {
    padding: isMobile ? '16px' : '32px 40px',
    maxWidth: 900,
  };

  return (
    <Layout>
      <div style={containerStyle}>
        {/* ページヘッダー */}
        <div style={S.header}>
          <div>
            <h1 style={S.title}>日報一覧</h1>
            <p style={S.subtitle}>あなたの日報を管理しましょう</p>
          </div>
          <button style={S.primaryButton} onClick={() => navigate('/reports/new')}>
            ＋ 新規作成
          </button>
        </div>

        {/* ローディング・エラー */}
        {loading && <p style={S.message}>読み込み中...</p>}
        {error && <p style={{ ...S.message, color: '#ef4444' }}>{error}</p>}

        {/* 日報リスト */}
        {!loading && reports.length === 0 && (
          <div style={S.empty}>
            <p>日報がまだありません</p>
            <button style={S.primaryButton} onClick={() => navigate('/reports/new')}>
              最初の日報を作成する
            </button>
          </div>
        )}

        {reports.map((report) => (
          <ReportItem
            key={report.id}
            report={report}
            isMobile={isMobile}
            onEdit={() => navigate(`/reports/${report.id}/edit`)}
            onDelete={() => handleDelete(report.id)}
          />
        ))}
      </div>
    </Layout>
  );
};

/**
 * 日報リストの1件分コンポーネント
 * @param report 表示する日報データ
 * @param isMobile モバイル表示かどうか（レイアウト切り替えに使用）
 * @param onEdit 編集ボタン押下時のコールバック
 * @param onDelete 削除ボタン押下時のコールバック
 */
const ReportItem = ({
  report,
  isMobile,
  onEdit,
  onDelete,
}: {
  report: DailyReport;
  isMobile: boolean;
  onEdit: () => void;
  onDelete: () => void;
}) => {
  const cardRowStyle: React.CSSProperties = isMobile
    ? { display: 'flex', flexDirection: 'column', gap: 10 }
    : { display: 'flex', justifyContent: 'space-between', alignItems: 'center' };

  return (
    <div style={S.card}>
      <div style={cardRowStyle}>
        <div style={S.cardLeft}>
          <span style={S.date}>{report.reportDate}</span>
          <span style={S.cardTitle}>{report.title}</span>
          <span style={report.isPublic ? S.badgePublic : S.badgePrivate}>
            {report.isPublic ? '公開' : '非公開'}
          </span>
        </div>
        <div style={S.cardActions}>
          <button style={S.editButton} onClick={onEdit}>編集</button>
          <button style={S.deleteButton} onClick={onDelete}>削除</button>
        </div>
      </div>
      <p style={S.todayTasks}>{report.todayTasks}</p>
    </div>
  );
};

/** スタイル定義 */
const S = {
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
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
  primaryButton: {
    background: '#3b82f6',
    color: '#ffffff',
    border: 'none',
    borderRadius: 6,
    padding: '9px 18px',
    fontSize: 13,
    fontWeight: 600,
    cursor: 'pointer',
    flexShrink: 0,
  },
  message: {
    fontSize: 14,
    color: '#64748b',
    padding: '20px 0',
  },
  empty: {
    textAlign: 'center' as const,
    padding: '60px 0',
    color: '#94a3b8',
    display: 'flex',
    flexDirection: 'column' as const,
    alignItems: 'center',
    gap: 16,
  },
  card: {
    background: '#ffffff',
    border: '1px solid #e2e8f0',
    borderRadius: 8,
    padding: '16px 20px',
    marginBottom: 12,
  },
  cardLeft: {
    display: 'flex',
    alignItems: 'center',
    gap: 10,
    flexWrap: 'wrap' as const,
  },
  date: {
    fontSize: 12,
    color: '#94a3b8',
  },
  cardTitle: {
    fontSize: 14,
    fontWeight: 600,
    color: '#1e293b',
  },
  badgePublic: {
    fontSize: 11,
    background: '#d1fae5',
    color: '#059669',
    padding: '2px 8px',
    borderRadius: 999,
    fontWeight: 500,
  },
  badgePrivate: {
    fontSize: 11,
    background: '#f1f5f9',
    color: '#94a3b8',
    padding: '2px 8px',
    borderRadius: 999,
    fontWeight: 500,
  },
  cardActions: {
    display: 'flex',
    gap: 8,
    flexShrink: 0,
  },
  editButton: {
    background: 'transparent',
    color: '#3b82f6',
    border: '1px solid #3b82f6',
    borderRadius: 5,
    padding: '4px 12px',
    fontSize: 12,
    cursor: 'pointer',
  },
  deleteButton: {
    background: 'transparent',
    color: '#ef4444',
    border: '1px solid #ef4444',
    borderRadius: 5,
    padding: '4px 12px',
    fontSize: 12,
    cursor: 'pointer',
  },
  todayTasks: {
    fontSize: 13,
    color: '#64748b',
    marginTop: 10,
    whiteSpace: 'pre-wrap' as const,
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    display: '-webkit-box',
    WebkitLineClamp: 2,
    WebkitBoxOrient: 'vertical' as const,
  },
};

export default ReportListPage;
