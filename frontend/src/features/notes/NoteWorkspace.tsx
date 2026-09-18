import { useState } from 'react';
import type { FormEvent } from 'react';
import { api } from '../../services/api.client';
import type { PageResponse } from '../../services/api.client';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Card, CardContent, CardHeader } from '../../components/ui/Card';
import { LoadingState, ErrorState, EmptyState } from '../../components/ui/Feedback';

interface Note {
  id: string;
  title: string;
  content: string;
  createdAt: string;
}

export function NoteWorkspace() {
  const [notes, setNotes] = useState<Note[]>([]);
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [searched, setSearched] = useState(false);

  const handleSearch = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setSearched(true);
    setError('');
    try {
      const res = await api.post<PageResponse<Note>>('/notes/search?query=' + encodeURIComponent(query));
      setNotes(res.items || res.content || []);
    } catch (err: any) {
      setError(err.message || 'Failed to search notes');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h2 style={{ marginBottom: 'var(--spacing-6)' }}>Notes System</h2>
      
      <Card style={{ marginBottom: 'var(--spacing-6)' }}>
        <CardContent>
          <form onSubmit={handleSearch} style={{ display: 'flex', gap: 'var(--spacing-2)', alignItems: 'flex-end' }}>
            <div style={{ flex: 1 }}>
              <Input 
                label="Search Notes" 
                type="text" 
                placeholder="Enter keywords..." 
                value={query} 
                onChange={e => setQuery(e.target.value)} 
                fullWidth
                style={{ marginBottom: 0 }}
              />
            </div>
            <Button type="submit" isLoading={loading}>Search</Button>
          </form>
        </CardContent>
      </Card>
      
      {error && <div style={{ marginBottom: 'var(--spacing-4)' }}><ErrorState message={error} /></div>}
      
      {loading && !error && <LoadingState message="Searching notes..." />}
      
      {!loading && searched && notes.length === 0 && !error && (
        <EmptyState message="No notes match your query." />
      )}
      
      {!loading && notes.length > 0 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-4)' }} aria-label="Note list">
          {notes.map(n => (
            <Card key={n.id}>
              <CardHeader>{n.title || 'Untitled Note'}</CardHeader>
              <CardContent>
                <div style={{ 
                  color: 'var(--color-text-secondary)', 
                  whiteSpace: 'pre-wrap',
                  maxHeight: '100px',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                  display: '-webkit-box',
                  WebkitLineClamp: 3,
                  WebkitBoxOrient: 'vertical'
                }}>
                  {n.content || 'No content.'}
                </div>
                <div style={{ marginTop: 'var(--spacing-4)', fontSize: 'var(--font-size-xs)', color: 'var(--color-text-muted)' }}>
                  Created: {new Date(n.createdAt).toLocaleDateString()}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
