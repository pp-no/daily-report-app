import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ReportListPage from './pages/ReportListPage';
import ReportFormPage from './pages/ReportFormPage';
import PublicReportsPage from './pages/PublicReportsPage';
import ProfilePage from './pages/ProfilePage';
import PrivateRoute from './components/PrivateRoute';
import { AuthProvider } from './context/AuthContext';

/**
 * アプリケーションのルーティング定義
 * AuthProvider で認証状態をグローバル管理し、PrivateRoute で認証必須ページを保護する
 */
function App() {
  return (
    <AuthProvider>
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
    </AuthProvider>
  );
}

export default App;
