import type { InputHTMLAttributes } from 'react';
import './Input.css';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
  fullWidth?: boolean;
}

export function Input({ label, error, fullWidth = false, id, className = '', style, ...props }: InputProps) {
  const inputId = id || 'input-' + label.replace(/\s+/g, '-').toLowerCase();
  const errorId = inputId + '-error';
  
  return (
    <div className={'gsir-form-field ' + (fullWidth ? 'gsir-form-field--full' : '') + ' ' + className} style={style}>
      <label htmlFor={inputId} className="gsir-form-field__label">{label}</label>
      <input 
        id={inputId} 
        className={'gsir-input ' + (error ? 'gsir-input--error' : '')} 
        aria-invalid={!!error}
        aria-describedby={error ? errorId : undefined}
        {...props} 
      />
      {error && <span id={errorId} className="gsir-form-field__error" role="alert">{error}</span>}
    </div>
  );
}
