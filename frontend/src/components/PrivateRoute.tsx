import { Navigate } from 'react-router-dom';

type Props = {
  children: React.ReactNode;
};

/**
 * 認証が必要なページを保護するラッパーコンポーネント
 * localStorageにJWTトークンがなければ /login へリダイレクトする
 * App.tsx で認証必須ルートをこのコンポーネントで囲って使う
 *
 * @param children 認証済みのときだけ表示するページコンポーネント
 */
const PrivateRoute = ({ children }: Props) => {
  // ログイン時に apiClient 経由で受け取ったトークンが保存されている
  const token = localStorage.getItem('token');

  // トークンがなければ未ログインとみなしてログインページへ追い返す
  // replace: true にすることでブラウザの戻るボタンで戻れなくする
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  // トークンがあれば子コンポーネント（実際のページ）をそのまま表示する
  return <>{children}</>;
};

export default PrivateRoute;
