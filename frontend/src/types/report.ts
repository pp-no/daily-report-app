/**
 * APIから返ってくる日報データの型（レスポンス用）
 * DailyReportResponse.java の内容と対応している
 */
export interface DailyReport {
  id: number;
  title: string;
  todayTasks: string;
  tomorrowTasks: string;
  impression: string | null;  // 任意項目のため null になりうる
  summary: string | null;     // 任意項目のため null になりうる
  reportDate: string;         // "2026-05-30" 形式の日付文字列
  isPublic: boolean;
  createdAt: string;          // ISO 8601形式のタイムスタンプ
  updatedAt: string;
}

/**
 * 日報を作成・更新するときにAPIへ送るデータの型（リクエスト用）
 * DailyReportRequest.java の内容と対応している
 */
export interface DailyReportRequest {
  title: string;
  todayTasks: string;
  tomorrowTasks: string;
  impression?: string;    // 省略可能（送らない場合はバックエンドでnull扱い）
  summary?: string;       // 省略可能（メール通知のまとめ欄）
  reportDate: string;     // "2026-05-30" 形式で送る
  isPublic: boolean;
}
