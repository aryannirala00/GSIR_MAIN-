import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import { AuthProvider } from './AuthContext';

import { ChatWorkspace } from '../features/chat/ChatWorkspace';
import { ProjectWorkspace } from '../features/projects/ProjectWorkspace';

// Mock the API client
vi.mock('../services/api.client', () => ({
  api: {
    post: vi.fn((url, data) => {
      if (url === '/auth/login') {
        if (data.email === 'test@example.com' && data.password === 'password') {
          return Promise.resolve({ accessToken: 'mock-token', refreshToken: 'mock-refresh', expiresIn: 3600 });
        }
        return Promise.reject(new Error('Invalid credentials'));
      }
      return Promise.resolve({});
    }),
    get: vi.fn((url) => {
      if (url === '/conversations') {
        return Promise.resolve({ items: [{ id: '1', title: 'Test Chat', createdAt: new Date().toISOString() }] });
      }
      if (url === '/projects') {
        return Promise.resolve({ items: [{ id: 'p1', name: 'Test Project', status: 'ACTIVE' }] });
      }
      return Promise.reject(new Error('Auth missing'));
    }),
    setToken: vi.fn(),
    clearToken: vi.fn()
  }
}));

describe('GSIR Frontend Routing & Security', () => {
  it('renders login screen when unauthenticated and trying to access protected route', () => {
    window.history.pushState({}, 'Dashboard', '/');
    render(<App />);
    expect(screen.getByText(/Login to GSIR/i)).toBeDefined();
    expect(screen.queryByText(/Logout/i)).toBeNull();
  });
});

describe('Chat Workspace Integration', () => {
  it('loads and displays conversations', async () => {
    render(
      <AuthProvider>
        <BrowserRouter>
          <ChatWorkspace />
        </BrowserRouter>
      </AuthProvider>
    );
    expect(screen.getByText(/Loading workspace.../i)).toBeDefined();
    await waitFor(() => {
      expect(screen.getByText(/Test Chat/i)).toBeDefined();
    });
  });
});

describe('Projects Workspace Integration', () => {
  it('loads and displays projects', async () => {
    render(
      <AuthProvider>
        <BrowserRouter>
          <ProjectWorkspace />
        </BrowserRouter>
      </AuthProvider>
    );
    expect(screen.getByText(/Loading projects.../i)).toBeDefined();
    await waitFor(() => {
      expect(screen.getByText(/Test Project/i)).toBeDefined();
    });
  });
});
