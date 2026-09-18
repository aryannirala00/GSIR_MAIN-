import { BrowserRouter } from 'react-router-dom';
import { AuthProvider } from './AuthContext';
import { AppRoutes } from '../routes/app.routes';

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </AuthProvider>
  );
}
