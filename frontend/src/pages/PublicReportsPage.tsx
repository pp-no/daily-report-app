import { useEffect, useState } from 'react';
import apiClient from '../api/client';
import type { DailyReport } from '../types/report';
import Layout from '../components/Layout';
import useIsMobile from '../hooks/useIsMobile';

/** 公開日報一覧画面 */
const PublicReportsPage = () => {
  const [reports, setReports] = useState<DailyReport[]>([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState<DailyReport | null>(null);
  const isMobile = useIsMobile();

  useEffect(() => {
    apiClient
      .get<DailyReport[]>('/api/reports/public')
      .then((res) => setReports(res.data))
      .finally(() => setLoading(false));
  }, []);

  const gridStyle: React.CSSProperties = {
    display: 'grid',
    gridTemplateColumns: isMobile ? '1fr' : 'repeat(3, 1fr)',
    gap: 16,
  };

  const containerStyle: React.CSSProperties = {
    padding: isMobile ? '16px' : '32px 40px',
  };

  return (
    <Layout>
      <div style={containerStyle}>
        <div style={S.header}>
          <div>
            <h1 style={S.title}>公開日報一覧</h1>
            <p style={S.subtitle}>みんなの日報をチェックしましょう</p>
          </div>
        </div>

        {loading && <p style={S.message}>読み込み中...</p>}
        {!loading && reports.length === 0 && (
          <p style={S.message}>公開日報がまだありません</p>
        )}

        <div style={gridStyle}>
          {reports.map((report) => (
            <PublicReportCard key={report.id} report={report} onDetail={() => setSelected(report)} />
          ))}
        </div>
      </div>

      {selected && <DetailModal report={selected} onClose={() => setSelected(null)} />}
    </Layout>
  );
};

/** 公開日報カード */
const PublicReportCard = ({
  report,
  onDetail,
}: {
  report: DailyReport;
  onDetail: () => void;
}) => (
  <div style={S.card}>
    <div style={S.cardHeader}>
      <span style={S.userName}>{report.userName}</span>
      <span style={S.cardDate}>{report.reportDate}</span>
    </div>
    <h3 style={S.cardTitle}>{report.title}</h3>
    <p style={S.cardBody}>{report.todayTasks}</p>
    <button style={S.detailButton} onClick={onDetail}>
      詳細を見る →
    </button>
  </div>
);

/** 詳細モーダル */
const DetailModal = ({ report, onClose }: { report: DailyReport; onClose: () => void }) => (
  <div style={S.overlay} onClick={onClose}>
    <div style={S.modal} onClick={(e) => e.stopPropagation()}>
      <div style={S.modalHeader}>
        <div>
          <p style={S.modalMeta}>
            {report.userName} · {report.reportDate}
          </p>
          <h2 style={S.modalTitle}>{report.title}</h2>
        </div>
        <button style={S.closeButton} onClick={onClose}>✕</button>
      </div>
      <div style={S.modalBody}>
        <Section label="今日やったこと" content={report.todayTasks} />
        <Section label="明日やること" content={report.tomorrowTasks} />
        {report.impression && <Section label="所感" content={report.impression} />}
      </div>
    </div>
  </div>
);

const Section = ({ label, content }: { label: string; content: string }) => (
  <div style={S.section}>
    <p style={S.sectionLabel}>{label}</p>
    <p style={S.sectionContent}>{content}</p>
  </div>
);

const S = {
  header: { marginBottom: 28 },
  title: { fontSize: 22, fontWeight: 700, color: '#1e293b', margin: 0 },
  subtitle: { fontSize: 13, color: '#64748b', marginTop: 4 },
  message: { fontSize: 14, color: '#94a3b8', padding: '40px 0' },
  card: {
    background: '#ffffff',
    borderRadius: 12,
    padding: 20,
    display: 'flex',
    flexDirection: 'column' as const,
    gap: 12,
    boxShadow: '0 1px 4px rgba(0,0,0,0.06)',
  },
  cardHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  userName: {
    fontSize: 12,
    fontWeight: 600,
    color: '#374151',
  },
  cardDate: { fontSize: 11, color: '#94a3b8' },
  cardTitle: {
    fontSize: 15,
    fontWeight: 700,
    color: '#0f172a',
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
    margin: 0,
  },
  detailButton: {
    background: '#EFF6FF',
    color: '#3b82f6',
    border: 'none',
    borderRadius: 8,
    padding: '6px 14px',
    fontSize: 12,
    fontWeight: 600,
    cursor: 'pointer',
    alignSelf: 'flex-start' as const,
  },
  overlay: {
    position: 'fixed' as const,
    inset: 0,
    background: 'rgba(0,0,0,0.4)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 1000,
    padding: 16,
  },
  modal: {
    background: '#ffffff',
    borderRadius: 12,
    width: '100%',
    maxWidth: 560,
    maxHeight: '80vh',
    overflow: 'auto',
    padding: 28,
  },
  modalHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 20,
    gap: 12,
  },
  modalMeta: { fontSize: 12, color: '#94a3b8', margin: '0 0 4px 0' },
  modalTitle: { fontSize: 18, fontWeight: 700, color: '#0f172a', margin: 0 },
  closeButton: {
    background: 'transparent',
    border: 'none',
    fontSize: 16,
    color: '#94a3b8',
    cursor: 'pointer',
    flexShrink: 0,
  },
  modalBody: { display: 'flex', flexDirection: 'column' as const, gap: 16 },
  section: {},
  sectionLabel: {
    fontSize: 12,
    fontWeight: 600,
    color: '#64748b',
    marginBottom: 4,
    margin: '0 0 4px 0',
  },
  sectionContent: {
    fontSize: 14,
    color: '#374151',
    lineHeight: 1.7,
    whiteSpace: 'pre-wrap' as const,
    margin: 0,
  },
};

export default PublicReportsPage;
