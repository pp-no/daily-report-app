import { useEffect, useState } from 'react';
import apiClient from '../api/client';
import type { DailyReport } from '../types/report';
import Layout from '../components/Layout';
import useIsMobile from '../hooks/useIsMobile';

/** 公開日報一覧画面（他ユーザーの公開日報をカード形式で表示） */
const PublicReportsPage = () => {
  const [reports, setReports] = useState<DailyReport[]>([]);
  const [loading, setLoading] = useState(true);
  const isMobile = useIsMobile();

  /** ページ読み込み時に公開日報を取得（認証不要エンドポイント） */
  useEffect(() => {
    apiClient.get<DailyReport[]>('/api/reports/public')
      .then((res) => setReports(res.data))
      .finally(() => setLoading(false));
  }, []);

  // モバイル：1カラム / タブレット：2カラム / デスクトップ：3カラム
  const gridStyle: React.CSSProperties = {
    display: 'grid',
    gridTemplateColumns: isMobile ? '1fr' : 'repeat(3, 1fr)',
    gap: 20,
  };

  const containerStyle: React.CSSProperties = {
    padding: isMobile ? '16px' : '32px 40px',
  };

  return (
    <Layout>
      <div style={containerStyle}>
        {/* ページヘッダー */}
        <div style={S.header}>
          <div>
            <h1 style={S.title}>公開日報一覧</h1>
            <p style={S.subtitle}>みんなの日報をチェックしましょう</p>
          </div>
        </div>

        {loading && <p style={S.message}>読み込み中...</p>}

        {/* カードグリッド */}
        {!loading && reports.length === 0 && (
          <p style={S.message}>公開日報がまだありません</p>
        )}

        <div style={gridStyle}>
          {reports.map((report) => (
            <PublicReportCard key={report.id} report={report} />
          ))}
        </div>
      </div>
    </Layout>
  );
};

/**
 * 公開日報カードコンポーネント
 * @param report 表示する公開日報データ
 */
const PublicReportCard = ({ report }: { report: DailyReport }) => (
  <div style={S.card}>
    <div style={S.cardHeader}>
      <span style={S.cardDate}>{report.reportDate}</span>
    </div>
    <h3 style={S.cardTitle}>{report.title}</h3>
    <p style={S.cardBody}>{report.todayTasks}</p>
    <div style={S.cardFooter}>
      <span style={S.readMore}>続きを読む →</span>
    </div>
  </div>
);

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
  message: {
    fontSize: 14,
    color: '#94a3b8',
    padding: '40px 0',
  },
  card: {
    background: '#ffffff',
    border: '1px solid #e2e8f0',
    borderRadius: 8,
    padding: 20,
    display: 'flex',
    flexDirection: 'column' as const,
    gap: 8,
  },
  cardHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  cardDate: {
    fontSize: 12,
    color: '#94a3b8',
  },
  cardTitle: {
    fontSize: 15,
    fontWeight: 600,
    color: '#1e293b',
    margin: 0,
    lineHeight: 1.4,
  },
  cardBody: {
    fontSize: 13,
    color: '#64748b',
    lineHeight: 1.6,
    overflow: 'hidden',
    display: '-webkit-box',
    WebkitLineClamp: 3,
    WebkitBoxOrient: 'vertical' as const,
    flex: 1,
  },
  cardFooter: {
    marginTop: 4,
  },
  readMore: {
    fontSize: 12,
    color: '#3b82f6',
    cursor: 'pointer',
  },
};

export default PublicReportsPage;
