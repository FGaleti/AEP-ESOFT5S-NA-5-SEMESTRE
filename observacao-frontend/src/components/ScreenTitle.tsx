import type { ReactNode } from 'react';

interface ScreenTitleProps {
  children: ReactNode;
}

/** Título de seção no estilo do wireframe: "— Título —". */
export default function ScreenTitle({ children }: ScreenTitleProps) {
  return (
    <h2 className="screen-title">
      <span className="screen-title-dash" />
      <span>{children}</span>
      <span className="screen-title-dash" />
    </h2>
  );
}
