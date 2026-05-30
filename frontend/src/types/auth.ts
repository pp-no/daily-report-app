/**
 * ログインAPIへ送るデータの型
 * LoginRequest.java の内容と対応している
 */
export interface LoginRequest {
  email: string;
  password: string;
}

/**
 * ユーザー登録APIへ送るデータの型
 * RegisterRequest.java の内容と対応している
 */
export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  /** 業務開始時刻（例: "09:00"）。登録後、開始30分前にメール通知が届く */
  workStartTime: string;
}

/**
 * ログイン・登録APIが返すレスポンスの型
 * AuthResponse.java の内容と対応している
 * tokenをlocalStorageに保存してその後のAPIリクエストに使う
 */
export interface AuthResponse {
  token: string;
}
