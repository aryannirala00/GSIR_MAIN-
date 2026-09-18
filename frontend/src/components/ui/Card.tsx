import type { ReactNode, CSSProperties } from 'react';
import './Card.css';

export function Card({ children, className = '', style }: { children: ReactNode, className?: string, style?: CSSProperties }) {
  return <div className={'gsir-card ' + className} style={style}>{children}</div>;
}

export function CardHeader({ children, className = '', style }: { children: ReactNode, className?: string, style?: CSSProperties }) {
  return <div className={'gsir-card__header ' + className} style={style}>{children}</div>;
}

export function CardContent({ children, className = '', style }: { children: ReactNode, className?: string, style?: CSSProperties }) {
  return <div className={'gsir-card__content ' + className} style={style}>{children}</div>;
}
