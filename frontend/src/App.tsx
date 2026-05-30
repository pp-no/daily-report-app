import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ReportListPage from './pages/ReportListPage';
import ReportFormPage from './pages/ReportFormPage';
import PublicReportsPage from './pages/PublicReportsPage';
import ProfilePage from './pages/ProfilePage';
import PrivateRoute from './components/PrivateRoute';

/**
 * アプリケーションのルーティング定義
 * PrivateRouteで認証が必要なページをラップする
 */
function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* 認証不要ページ */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/public" element={<PrivateRoute><PublicReportsPage /></PrivateRoute>} />

        {/* 認証必要ページ */}
        <Route path="/reports" element={<PrivateRoute><ReportListPage /></PrivateRoute>} />
        <Route path="/reports/new" element={<PrivateRoute><ReportFormPage /></PrivateRoute>} />
        <Route path="/reports/:id/edit" element={<PrivateRoute><ReportFormPage /></PrivateRoute>} />
        <Route path="/settings" element={<PrivateRoute><ProfilePage /></PrivateRoute>} />

        {/* 未定義パスはログインへリダイレクト */}
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
