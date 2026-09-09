import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

type Props = {
  children: React.ReactNode;
};

/**
 * 認証が必要なページを保護するラッパーコンポーネント。
 * AuthContext の認証状態を見てアクセス可否を判定する。
 * loading 中は何も描画しない（アプリ初期化時の一瞬だけ）。
 */
const PrivateRoute = ({ children }: Props) => {
  const { authState } = useAuth();

  if (authState === 'loading') return null;
  if (authState === 'unauthenticated') return <Navigate to="/login" replace />;
  return <>{children}</>;
};

export default PrivateRoute;
