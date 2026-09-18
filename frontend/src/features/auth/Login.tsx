import { useState } from 'react';
import type { FormEvent } from 'react';
import { useAuth } from '../../app/AuthContext';
import type { AuthResponse } from '../../app/AuthContext';
import { api } from '../../services/api.client';
import { useNavigate } from 'react-router-dom';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Card, CardHeader, CardContent } from '../../components/ui/Card';
import { ErrorState } from '../../components/ui/Feedback';

export function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleLogin = async (e: FormEvent) => {
    e.preventDefault();
    if (!email || !password) return;
    
    setLoading(true);
    try {
      setError('');
      const res = await api.post<AuthResponse>('/auth/login', { email, password });
      login(res);
      navigate('/');
    } catch (err: any) {
      setError(err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ width: '100%', maxWidth: '400px' }}>
      <Card>
        <CardHeader>
          <h2 style={{ margin: 0, textAlign: 'center' }}>Login to GSIR</h2>
        </CardHeader>
        <CardContent>
          {error && <div style={{ marginBottom: 'var(--spacing-4)' }}><ErrorState message={error} /></div>}
          
          <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column' }}>
            <Input 
              label="Email Address" 
              type="email" 
              value={email} 
              onChange={e => setEmail(e.target.value)} 
              required 
              fullWidth
              autoComplete="username"
            />
            <Input 
              label="Password" 
              type="password" 
              value={password} 
              onChange={e => setPassword(e.target.value)} 
              required 
              fullWidth
              autoComplete="current-password"
            />
            <div style={{ marginTop: 'var(--spacing-2)' }}>
              <Button type="submit" isLoading={loading} className="w-full" style={{ width: '100%' }}>
                Sign In
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
