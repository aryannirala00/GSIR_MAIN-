import type { ReactNode } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from '../app/AuthContext';
import { AppLayout } from '../layouts/AppLayout';
import { Login } from '../features/auth/Login';
import { Dashboard } from '../pages/Dashboard';
import { ChatWorkspace } from '../features/chat/ChatWorkspace';
import { ProjectWorkspace } from '../features/projects/ProjectWorkspace';
import { NoteWorkspace } from '../features/notes/NoteWorkspace';
import { ResearchWorkspace } from '../features/research/ResearchWorkspace';

function ProtectedRoute({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <>{children}</>;
}

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/" element={
        <ProtectedRoute>
          <AppLayout />
        </ProtectedRoute>
      }>
        <Route index element={<Dashboard />} />
        <Route path="chat" element={<ChatWorkspace />} />
        <Route path="projects" element={<ProjectWorkspace />} />
        <Route path="notes" element={<NoteWorkspace />} />
        <Route path="research" element={<ResearchWorkspace />} />
      </Route>
    </Routes>
  );
}
