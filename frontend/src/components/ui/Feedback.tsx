import type { ReactNode } from 'react';
import './Feedback.css';

export function LoadingState({ message = 'Loading...' }: { message?: string }) {
  return (
    <div className="gsir-feedback gsir-feedback--loading" aria-live="polite">
      <div className="gsir-feedback__spinner" aria-hidden="true" />
      <p>{message}</p>
    </div>
  );
}

export function ErrorState({ message, retryAction }: { message: string, retryAction?: () => void }) {
  return (
    <div className="gsir-feedback gsir-feedback--error" role="alert">
      <p><strong>Error:</strong> {message}</p>
      {retryAction && <button className="gsir-button gsir-button--sm gsir-button--secondary" onClick={retryAction}>Retry</button>}
    </div>
  );
}

export function EmptyState({ message, children }: { message: string, children?: ReactNode }) {
  return (
    <div className="gsir-feedback gsir-feedback--empty">
      <p>{message}</p>
      {children && <div style={{ marginTop: '1rem' }}>{children}</div>}
    </div>
  );
}
