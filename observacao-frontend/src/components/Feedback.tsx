import type { ReactNode } from 'react';

interface MessageProps {
  children: ReactNode;
}

export function Loading({ children = 'Carregando…' }: { children?: ReactNode }) {
  return (
    <div className="feedback feedback-loading">
      <span className="spinner" aria-hidden="true" />
      <span>{children}</span>
    </div>
  );
}

export function ErrorMessage({ children }: MessageProps) {
  return <div className="feedback feedback-error">⚠ {children}</div>;
}

export function EmptyState({ children }: MessageProps) {
  return <div className="feedback feedback-empty">{children}</div>;
}
