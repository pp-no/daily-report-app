import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import apiClient from '../api/client';
import { useReports } from '../hooks/useReports';
import useIsMobile from '../hooks/useIsMobile';
import type { DailyReport, DailyReportRequest } from '../types/report';
import Layout from '../components/Layout';

/** 日報作成・編集画面（idパラメータがあれば編集、なければ作成） */
const ReportFormPage = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEdit = !!id;
  const isMobile = useIsMobile();
  const { createReport, updateReport } = useReports();

  const [form, setForm] = useState<DailyReportRequest>({
    title: '',
    todayTasks: '',
    tomorrowTasks: '',
    impression: '',
    summary: '',
    reportDate: new Date().toISOString().split('T')[0],
    isPublic: false,
  });
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  /**
   * 編集時：既存の日報データをAPIから取得してフォームに反映
   * idが変わるたびに再取得する
   */
  useEffect(() => {
    if (!isEdit) return;
    apiClient.get<DailyReport>(`/api/reports/${id}`).then((res) => {
      const r = res.data;
      setForm({
        title: r.title,
        todayTasks: r.todayTasks,
        tomorrowTasks: r.tomorrowTasks,
        impression: r.impression ?? '',
        summary: r.summary ?? '',
        reportDate: r.reportDate,
        isPublic: r.isPublic,
      });
    });
  }, [id, isEdit]);

  /**
   * フォーム送信：作成または更新を実行して一覧へ遷移
   */
  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
    setSaving(true);
    try {
      if (isEdit) {
        await updateReport(Number(id), form);
      } else {
        await createReport(form);
      }
      navigate('/reports');
    } catch {
      setError('保存に失敗しました');
    } finally {
      setSaving(false);
    }
  };

  // モバイル時は1カラム、デスクトップ時は2カラム
  const containerStyle: React.CSSProperties = {
    padding: isMobile ? '16px' : '32px 40px',
  };

  const formGridStyle: React.CSSProperties = {
    display: 'grid',
    gridTemplateColumns: isMobile ? '1fr' : '1fr 320px',
    gap: 24,
    alignItems: 'start',
  };

  return (
    <Layout>
      <div style={containerStyle}>
        {/* ページヘッダー */}
        <div style={S.header}>
          <div>
            <h1 style={S.title}>{isEdit ? '日報編集' : '日報作成'}</h1>
            <p style={S.subtitle}>
              {isEdit ? '日報の内容を更新しましょう' : '今日の業務内容を記録しましょう'}
            </p>
          </div>
          <div style={S.headerActions}>
            <button
              type="button"
              style={S.cancelButton}
              onClick={() => navigate('/reports')}
            >
              キャンセル
            </button>
            <button
              type="submit"
              form="report-form"
              style={S.saveButton}
              disabled={saving}
            >
              {isEdit ? '更新する' : '日報を保存'}
            </button>
          </div>
        </div>

        {error && <div style={S.error}>{error}</div>}

        {/* フォームグリッド：デスクトップ2カラム / モバイル1カラム */}
        <form id="report-form" onSubmit={handleSubmit} style={formGridStyle}>
          {/* 左カラム：メイン入力 */}
          <div style={S.leftCol}>
            <div style={S.field}>
              <label style={S.label}>タイトル</label>
              <input
                type="text"
                value={form.title}
                onChange={(e) => setForm({ ...form, title: e.target.value })}
                placeholder="例：2026年5月30日の作業"
                style={S.input}
                required
              />
            </div>
            <div style={S.field}>
              <label style={S.label}>日付</label>
              <input
                type="date"
                value={form.reportDate}
                onChange={(e) => setForm({ ...form, reportDate: e.target.value })}
                style={S.input}
                required
              />
            </div>
            <div style={S.field}>
              <label style={S.label}>今日やったこと <span style={S.required}>*</span></label>
              <textarea
                value={form.todayTasks}
                onChange={(e) => setForm({ ...form, todayTasks: e.target.value })}
                placeholder="今日取り組んだ作業内容を入力してください"
                rows={5}
                style={S.textarea}
                required
              />
            </div>
            <div style={S.field}>
              <label style={S.label}>明日やること <span style={S.required}>*</span></label>
              <textarea
                value={form.tomorrowTasks}
                onChange={(e) => setForm({ ...form, tomorrowTasks: e.target.value })}
                placeholder="明日の作業予定を入力してください"
                rows={5}
                style={S.textarea}
                required
              />
            </div>
            <div style={S.field}>
              <label style={S.label}>所感（任意）</label>
              <textarea
                value={form.impression}
                onChange={(e) => setForm({ ...form, impression: e.target.value })}
                placeholder="今日の気づきや感想を入力してください"
                rows={3}
                style={S.textarea}
              />
            </div>
          </div>

          {/* 右カラム：まとめ・公開設定 */}
          <div style={S.rightCol}>
            <div style={S.sideCard}>
              <label style={S.label}>まとめ欄</label>
              <p style={S.sideNote}>メール通知に使用されます</p>
              <textarea
                value={form.summary}
                onChange={(e) => setForm({ ...form, summary: e.target.value })}
                placeholder="業務のまとめを入力してください..."
                rows={6}
                style={S.textarea}
              />
            </div>
            <div style={S.sideCard}>
              <label style={S.label}>公開設定</label>
              <p style={S.sideNote}>他のユーザーに日報を公開します</p>
              <label style={S.toggleLabel}>
                <input
                  type="checkbox"
                  checked={form.isPublic}
                  onChange={(e) => setForm({ ...form, isPublic: e.target.checked })}
                  style={{ marginRight: 8 }}
                />
                他のユーザーに公開する
              </label>
            </div>
          </div>
        </form>
      </div>
    </Layout>
  );
};

/** スタイル定義 */
const S = {
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 28,
    flexWrap: 'wrap' as const,
    gap: 12,
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
  headerActions: {
    display: 'flex',
    gap: 10,
  },
  cancelButton: {
    background: 'transparent',
    color: '#64748b',
    border: '1px solid #e2e8f0',
    borderRadius: 6,
    padding: '9px 18px',
    fontSize: 13,
    cursor: 'pointer',
  },
  saveButton: {
    background: '#3b82f6',
    color: '#ffffff',
    border: 'none',
    borderRadius: 6,
    padding: '9px 18px',
    fontSize: 13,
    fontWeight: 600,
    cursor: 'pointer',
  },
  error: {
    background: '#fef2f2',
    color: '#ef4444',
    border: '1px solid #fecaca',
    borderRadius: 6,
    padding: '10px 12px',
    fontSize: 13,
    marginBottom: 20,
  },
  leftCol: {},
  rightCol: {
    display: 'flex',
    flexDirection: 'column' as const,
    gap: 16,
  },
  sideCard: {
    background: '#ffffff',
    border: '1px solid #e2e8f0',
    borderRadius: 8,
    padding: 16,
  },
  sideNote: {
    fontSize: 11,
    color: '#94a3b8',
    marginBottom: 8,
    marginTop: 2,
  },
  field: {
    marginBottom: 20,
  },
  label: {
    display: 'block',
    fontSize: 13,
    fontWeight: 600,
    color: '#374151',
    marginBottom: 6,
  },
  required: {
    color: '#ef4444',
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
  textarea: {
    width: '100%',
    padding: '9px 12px',
    border: '1px solid #e2e8f0',
    borderRadius: 6,
    fontSize: 14,
    color: '#1e293b',
    outline: 'none',
    resize: 'vertical' as const,
    lineHeight: 1.6,
    boxSizing: 'border-box' as const,
  },
  toggleLabel: {
    display: 'flex',
    alignItems: 'center',
    fontSize: 13,
    color: '#374151',
    cursor: 'pointer',
  },
};

export default ReportFormPage;
