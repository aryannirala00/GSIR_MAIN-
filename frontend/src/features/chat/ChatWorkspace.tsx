import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { api } from '../../services/api.client';
import type { PageResponse } from '../../services/api.client';
import { Button } from '../../components/ui/Button';
import { LoadingState, ErrorState, EmptyState } from '../../components/ui/Feedback';
import './ChatWorkspace.css';

interface Conversation {
  id: string;
  title: string;
  createdAt: string;
}

interface MessagePart {
  partType: string;
  content: string;
  sequenceNumber: number;
}

interface Message {
  id: string;
  role: string;
  createdAt: string;
  parts: MessagePart[];
}

export function ChatWorkspace() {
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [activeConvId, setActiveConvId] = useState<string | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  
  const [loadingList, setLoadingList] = useState(true);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [error, setError] = useState('');
  const [creating, setCreating] = useState(false);
  const [sending, setSending] = useState(false);
  
  const [draft, setDraft] = useState('');

  const fetchConversations = async () => {
    try {
      setLoadingList(true);
      const res = await api.get<PageResponse<Conversation>>('/conversations');
      setConversations(res.items || res.content || []);
    } catch (err: any) {
      setError(err.message || 'Failed to load conversations');
    } finally {
      setLoadingList(false);
    }
  };

  useEffect(() => {
    fetchConversations();
  }, []);

  useEffect(() => {
    if (!activeConvId) return;
    const fetchMessages = async () => {
      try {
        setLoadingMessages(true);
        const res = await api.get<PageResponse<Message>>('/conversations/' + activeConvId + '/messages');
        setMessages((res.items || res.content || []).reverse()); // Assume API returns latest first if paginated, or just use as is. Adjust as needed. Let's assume standard order.
      } catch (err: any) {
        console.error(err);
      } finally {
        setLoadingMessages(false);
      }
    };
    fetchMessages();
  }, [activeConvId]);

  const handleCreate = async () => {
    setCreating(true);
    setError('');
    try {
      const res = await api.post<Conversation>('/conversations', { title: 'New Conversation' });
      await fetchConversations();
      setActiveConvId(res.id);
    } catch (err: any) {
      setError(err.message || 'Failed to create conversation');
    } finally {
      setCreating(false);
    }
  };

  const handleSend = async (e: FormEvent) => {
    e.preventDefault();
    if (!draft.trim() || !activeConvId) return;
    
    setSending(true);
    try {
      const req = {
        role: 'user',
        parts: [{ partType: 'text', content: draft, sequenceNumber: 0 }]
      };
      await api.post('/conversations/' + activeConvId + '/messages', req);
      setDraft('');
      // Reload messages
      const res = await api.get<PageResponse<Message>>('/conversations/' + activeConvId + '/messages');
      setMessages((res.items || res.content || []).reverse());
    } catch (err: any) {
      console.error(err);
    } finally {
      setSending(false);
    }
  };

  if (loadingList && conversations.length === 0) return <LoadingState message="Loading workspace..." />;

  return (
    <div className="gsir-chat-layout">
      <aside className="gsir-chat-sidebar">
        <div className="gsir-chat-sidebar__header">
          <Button onClick={handleCreate} isLoading={creating} className="w-full" style={{width: '100%'}}>
            New Chat
          </Button>
        </div>
        {error && <div style={{padding: '1rem'}}><ErrorState message={error} /></div>}
        <ul className="gsir-chat-list" aria-label="Conversation list">
          {conversations.map(c => (
            <li 
              key={c.id} 
              className={'gsir-chat-item ' + (activeConvId === c.id ? 'gsir-chat-item--active' : '')}
              onClick={() => setActiveConvId(c.id)}
              role="button"
              tabIndex={0}
              onKeyDown={e => e.key === 'Enter' && setActiveConvId(c.id)}
            >
              <div className="gsir-chat-item__title">{c.title || 'Untitled'}</div>
              <div className="gsir-chat-item__date">{new Date(c.createdAt).toLocaleDateString()}</div>
            </li>
          ))}
          {conversations.length === 0 && !error && (
            <div style={{padding: '1rem'}}><EmptyState message="No conversations yet." /></div>
          )}
        </ul>
      </aside>
      
      <section className="gsir-chat-main">
        {!activeConvId ? (
          <div style={{ margin: 'auto' }}>
            <EmptyState message="Select a conversation to start chatting" />
          </div>
        ) : (
          <>
            <div className="gsir-chat-messages" aria-live="polite">
              {loadingMessages && <LoadingState message="Loading messages..." />}
              {!loadingMessages && messages.length === 0 && (
                <EmptyState message="Say hello to begin the research session." />
              )}
              {messages.map(m => (
                <div key={m.id} className={'gsir-message ' + (m.role === 'user' ? 'gsir-message--user' : 'gsir-message--assistant')}>
                  {m.parts?.map((p, i) => (
                    <div key={i}>{p.content}</div>
                  ))}
                </div>
              ))}
            </div>
            <div className="gsir-chat-input-area">
              <form onSubmit={handleSend} className="gsir-chat-input-form">
                <input 
                  type="text" 
                  value={draft} 
                  onChange={e => setDraft(e.target.value)}
                  className="gsir-chat-input"
                  placeholder="Message GSIR..."
                  aria-label="Message text"
                  disabled={sending}
                />
                <Button type="submit" isLoading={sending} disabled={!draft.trim()}>Send</Button>
              </form>
            </div>
          </>
        )}
      </section>
    </div>
  );
}
