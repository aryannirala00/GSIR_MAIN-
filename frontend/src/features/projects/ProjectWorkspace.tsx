import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { api } from '../../services/api.client';
import type { PageResponse } from '../../services/api.client';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Card, CardHeader, CardContent } from '../../components/ui/Card';
import { LoadingState, ErrorState, EmptyState } from '../../components/ui/Feedback';

interface Project {
  id: string;
  name: string;
  description: string;
  status: string;
}

export function ProjectWorkspace() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const [showCreate, setShowCreate] = useState(false);
  const [newName, setNewName] = useState('');
  const [newDesc, setNewDesc] = useState('');
  const [creating, setCreating] = useState(false);

  const fetchProjects = async () => {
    try {
      setLoading(true);
      const res = await api.get<PageResponse<Project>>('/projects');
      setProjects(res.items || res.content || []);
    } catch (err: any) {
      setError(err.message || 'Failed to load projects');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProjects();
  }, []);

  const handleCreate = async (e: FormEvent) => {
    e.preventDefault();
    setCreating(true);
    try {
      await api.post('/projects', { name: newName, description: newDesc, workspaceId: null }); // Depends on backend constraints, workspaceId may be optional or inferred.
      setNewName('');
      setNewDesc('');
      setShowCreate(false);
      await fetchProjects();
    } catch (err: any) {
      setError(err.message || 'Failed to create project');
    } finally {
      setCreating(false);
    }
  };

  if (loading) return <LoadingState message="Loading projects..." />;
  
  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 'var(--spacing-6)' }}>
        <h2 style={{ margin: 0 }}>Projects Workspace</h2>
        <Button onClick={() => setShowCreate(!showCreate)}>{showCreate ? 'Cancel' : 'New Project'}</Button>
      </div>

      {error && <div style={{ marginBottom: 'var(--spacing-4)' }}><ErrorState message={error} /></div>}

      {showCreate && (
        <Card className="mb-6" style={{ marginBottom: 'var(--spacing-6)' }}>
          <CardHeader>Create New Project</CardHeader>
          <CardContent>
            <form onSubmit={handleCreate} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--spacing-2)' }}>
              <Input label="Project Name" value={newName} onChange={e => setNewName(e.target.value)} required />
              <Input label="Description" value={newDesc} onChange={e => setNewDesc(e.target.value)} />
              <div style={{ marginTop: 'var(--spacing-2)' }}>
                <Button type="submit" isLoading={creating}>Create Project</Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}

      {projects.length === 0 && !error ? (
        <EmptyState message="No projects found. Create your first research project." />
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: 'var(--spacing-4)' }} aria-label="Project list">
          {projects.map(p => (
            <Card key={p.id}>
              <CardHeader>{p.name}</CardHeader>
              <CardContent>
                <p style={{ color: 'var(--color-text-secondary)', fontSize: 'var(--font-size-sm)' }}>
                  {p.description || 'No description provided.'}
                </p>
                <div style={{ marginTop: 'var(--spacing-4)' }}>
                  <span style={{ 
                    display: 'inline-block', 
                    padding: 'var(--spacing-1) var(--spacing-2)', 
                    backgroundColor: 'var(--color-bg-secondary)',
                    borderRadius: 'var(--radius-full)',
                    fontSize: 'var(--font-size-xs)'
                  }}>
                    {p.status || 'ACTIVE'}
                  </span>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
