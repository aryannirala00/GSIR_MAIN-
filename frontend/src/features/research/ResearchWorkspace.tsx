import { useState } from 'react';
import type { FormEvent } from 'react';
import { api } from '../../services/api.client';
import type { PageResponse } from '../../services/api.client';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Card, CardHeader, CardContent } from '../../components/ui/Card';
import { LoadingState, ErrorState, EmptyState } from '../../components/ui/Feedback';

interface Workflow {
  id: string;
  name: string;
  status: string;
  objective: string;
}

export function ResearchWorkspace() {
  const [projectId, setProjectId] = useState('');
  const [workflows, setWorkflows] = useState<Workflow[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [fetched, setFetched] = useState(false);

  const fetchWorkflows = async (e: FormEvent) => {
    e.preventDefault();
    if (!projectId) return;
    
    setLoading(true);
    setFetched(true);
    setError('');
    
    try {
      const res = await api.get<PageResponse<Workflow>>('/projects/' + encodeURIComponent(projectId) + '/workflows');
      setWorkflows(res.items || res.content || []);
    } catch (err: any) {
      setError(err.message || 'Failed to load workflows for this project');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h2 style={{ marginBottom: 'var(--spacing-6)' }}>Research Workspace</h2>
      
      <Card style={{ marginBottom: 'var(--spacing-6)' }}>
        <CardContent>
          <form onSubmit={fetchWorkflows} style={{ display: 'flex', gap: 'var(--spacing-2)', alignItems: 'flex-end' }}>
            <div style={{ flex: 1 }}>
              <Input 
                label="Target Project ID" 
                type="text" 
                value={projectId} 
                onChange={e => setProjectId(e.target.value)} 
                placeholder="Enter Project UUID"
                required 
                fullWidth
                style={{ marginBottom: 0 }}
              />
            </div>
            <Button type="submit" isLoading={loading}>Load Workflows</Button>
          </form>
        </CardContent>
      </Card>

      {error && <div style={{ marginBottom: 'var(--spacing-4)' }}><ErrorState message={error} /></div>}
      
      {loading && !error && <LoadingState message="Loading research workflows..." />}
      
      {!loading && fetched && workflows.length === 0 && !error && (
        <EmptyState message="No active research workflows for this project." />
      )}
      
      {!loading && workflows.length > 0 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-4)' }} aria-label="Workflow list">
          {workflows.map(w => (
            <Card key={w.id}>
              <CardHeader>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span>{w.name || 'Unknown Workflow'}</span>
                  <span style={{ 
                    fontSize: 'var(--font-size-xs)', 
                    padding: 'var(--spacing-1) var(--spacing-2)', 
                    backgroundColor: w.status === 'COMPLETED' ? 'var(--color-success-bg)' : 'var(--color-info-bg)',
                    color: w.status === 'COMPLETED' ? 'var(--color-success-text)' : 'var(--color-info-text)',
                    borderRadius: 'var(--radius-full)'
                  }}>
                    {w.status}
                  </span>
                </div>
              </CardHeader>
              <CardContent>
                <p style={{ margin: 0, color: 'var(--color-text-secondary)', fontSize: 'var(--font-size-sm)' }}>
                  {w.objective || 'No objective specified.'}
                </p>
                <div style={{ marginTop: 'var(--spacing-4)' }}>
                  <Button variant="secondary" size="sm">View Details</Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
