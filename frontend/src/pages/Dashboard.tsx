import { Card, CardHeader, CardContent } from '../components/ui/Card';

export function Dashboard() {
  return (
    <div>
      <h1 style={{ marginBottom: 'var(--spacing-6)' }}>Dashboard</h1>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: 'var(--spacing-6)' }}>
        <Card>
          <CardHeader>Welcome to GSIR</CardHeader>
          <CardContent>
            <p>Select a workspace from the navigation above to get started.</p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
