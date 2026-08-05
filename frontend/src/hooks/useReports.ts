import { useState, useEffect } from 'react';
import apiClient from '../api/client';
import type { DailyReport, DailyReportRequest } from '../types/report';

/**
 * 日報のCRUD操作と状態管理をまとめたカスタムフック
 * このフックを使うコンポーネントは reports/loading/error を参照するだけでよい
 */
export const useReports = () => {
  // サーバーから取得した日報一覧
  const [reports, setReports] = useState<DailyReport[]>([]);
  // API通信中かどうか（ローディング表示の制御に使う）
  const [loading, setLoading] = useState(false);
  // エラーメッセージ（正常時はnull）
  const [error, setError] = useState<string | null>(null);

  /**
   * 自分の日報一覧を取得する
   * GET /api/reports → DailyReportController.getMyReports()
   * JWTトークンからログイン中のユーザーを特定してそのユーザーの日報だけ返ってくる
   */
  const fetchReports = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await apiClient.get<DailyReport[]>('/api/reports');
      setReports(response.data);
    } catch {
      setError('日報の取得に失敗しました');
    } finally {
      // 成功・失敗どちらでもローディングを終了する
      setLoading(false);
    }
  };

  /**
   * 日報を新規作成する
   * POST /api/reports → DailyReportController.create()
   * @param data 作成する日報の内容
   * @returns 作成された日報（サーバーが採番したidが含まれる）
   */
  const createReport = async (data: DailyReportRequest) => {
    const response = await apiClient.post<DailyReport>('/api/reports', data);
    return response.data;
  };

  /**
   * 日報を更新する
   * PUT /api/reports/:id → DailyReportController.update()
   * @param id 更新対象の日報ID
   * @param data 更新後の内容
   * @returns 更新後の日報
   */
  const updateReport = async (id: number, data: DailyReportRequest) => {
    const response = await apiClient.put<DailyReport>(`/api/reports/${id}`, data);
    return response.data;
  };

  /**
   * 日報を削除する
   * DELETE /api/reports/:id → DailyReportController.delete()
   * @param id 削除対象の日報ID
   */
  const deleteReport = async (id: number) => {
    await apiClient.delete(`/api/reports/${id}`);
  };

  /**
   * コンポーネントのマウント時に日報一覧を自動取得する
   * 依存配列が空なので初回レンダリング時に1度だけ実行される
   */
  useEffect(() => {
    // fetchReports は async 関数のため setState は非同期に呼ばれる。同期的カスケードの問題はない。
    // eslint-disable-next-line react-hooks/set-state-in-effect
    void fetchReports();
  }, []);

  return { reports, loading, error, fetchReports, createReport, updateReport, deleteReport };
};
