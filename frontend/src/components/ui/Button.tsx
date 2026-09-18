import type { ButtonHTMLAttributes, ReactNode } from 'react';
import './Button.css';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
  isLoading?: boolean;
  children: ReactNode;
}

export function Button({ 
  variant = 'primary', 
  size = 'md', 
  isLoading = false, 
  className = '', 
  style,
  children, 
  disabled, 
  ...props 
}: ButtonProps) {
  return (
    <button 
      className={'gsir-button gsir-button--' + variant + ' gsir-button--' + size + ' ' + className} 
      disabled={disabled || isLoading}
      style={style}
      {...props}
    >
      {isLoading ? <span className="gsir-button__spinner" aria-hidden="true" /> : null}
      <span className="gsir-button__content">{children}</span>
    </button>
  );
}
