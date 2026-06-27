import React, { useState, useEffect, useRef } from 'react'
import { 
  Sparkles, 
  RefreshCw, 
  Database, 
  Send, 
  User, 
  CheckCircle, 
  Layers, 
  Users, 
  AlertTriangle,
  Mail,
  Briefcase
} from 'lucide-react'

export default function App() {
  const [leads, setLeads] = useState([])
  const [goldenLeads, setGoldenLeads] = useState([])
  const [messages, setMessages] = useState([
    { sender: 'agent', text: "Hello! I am SmartLead AI, your sales operations steward. I can help you enrich raw details and merge duplicates. Try asking me to 'enrich all raw leads' or 'find duplicates'." }
  ])
  const [inputText, setInputText] = useState('')
  const [isTyping, setIsTyping] = useState(false)
  const [isSeeding, setIsSeeding] = useState(false)
  const [notification, setNotification] = useState(null)
  
  const messagesEndRef = useRef(null)

  useEffect(() => {
    fetchLeads()
    fetchGoldenLeads()

    // 1. Establish Server-Sent Events (SSE) stream for real-time updates
    const eventSource = new EventSource('/api/events')
    
    eventSource.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        
        if (data.mergedRecordIds) {
          // Received a GoldenLead update
          setGoldenLeads(prev => {
            if (prev.some(g => g.id === data.id)) return prev
            return [...prev, data]
          })
          showToast(`✨ Golden Record created: ${data.name} @ ${data.company}`)
          // Re-fetch raw leads since their status would have updated to 'MERGED'
          fetchLeads()
        } else {
          // Received a Lead update (e.g. enrichment)
          setLeads(prev => prev.map(l => l.id === data.id ? data : l))
          showToast(`⚡ Lead Enriched: ${data.name} (${data.enrichedIndustry})`)
        }
      } catch (err) {
        console.error("Error parsing SSE event data", err)
      }
    }

    eventSource.onerror = (err) => {
      console.warn("SSE connection error, attempting to reconnect...", err)
    }

    return () => {
      eventSource.close()
    }
  }, [])

  useEffect(() => {
    scrollToBottom()
  }, [messages, isTyping])

  const fetchLeads = async () => {
    try {
      const res = await fetch('/api/leads')
      const data = await res.json()
      // Sort leads by creation time
      setLeads(data.sort((a, b) => b.createdAt - a.createdAt))
    } catch (err) {
      console.error("Error fetching leads", err)
    }
  }

  const fetchGoldenLeads = async () => {
    try {
      const res = await fetch('/api/leads/golden')
      const data = await res.json()
      setGoldenLeads(data)
    } catch (err) {
      console.error("Error fetching golden leads", err)
    }
  }

  const showToast = (msg) => {
    setNotification(msg)
    setTimeout(() => {
      setNotification(null)
    }, 4500)
  }

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  const handleSeedData = async () => {
    setIsSeeding(true)
    try {
      const res = await fetch('/api/leads/seed', { method: 'POST' })
      const text = await res.text()
      showToast(text)
      fetchLeads()
      fetchGoldenLeads()
    } catch (err) {
      console.error("Error seeding data", err)
    } finally {
      setIsSeeding(false)
    }
  }

  const handleSendMessage = async (textToSend) => {
    const prompt = textToSend || inputText
    if (!prompt.trim()) return

    if (!textToSend) setInputText('')
    
    // Add user message to UI
    setMessages(prev => [...prev, { sender: 'user', text: prompt }])
    setIsTyping(true)

    try {
      const res = await fetch('/api/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: prompt })
      })
      const reply = await res.text()
      setMessages(prev => [...prev, { sender: 'agent', text: reply }])
      // Trigger updates in case the agent called tools
      fetchLeads()
      fetchGoldenLeads()
    } catch (err) {
      setMessages(prev => [...prev, { sender: 'agent', text: "Sorry, I ran into an error communicating with the agent server." }])
    } finally {
      setIsTyping(false)
    }
  }

  // Calculate metrics
  const totalRawLeads = leads.filter(l => l.status === 'RAW').length
  const totalEnrichedLeads = leads.filter(l => l.status === 'ENRICHED').length
  const totalMergedLeads = leads.filter(l => l.status === 'MERGED').length

  return (
    <div className="app-container">
      {/* 1. Main Workspace Panel (Left) */}
      <div className="workspace">
        <header className="app-header">
          <div className="brand">
            <span className="brand-icon">⚡</span>
            <h1 className="brand-title">SmartLead AI</h1>
            <span className="brand-subtitle">GCP Reference Arch</span>
          </div>
          <button 
            className="action-btn secondary" 
            onClick={handleSeedData}
            disabled={isSeeding}
          >
            <RefreshCw className={isSeeding ? "animate-spin" : ""} size={14} />
            {isSeeding ? "Resetting..." : "Reset & Seed Demo Data"}
          </button>
        </header>

        {/* 2. Metrics Row */}
        <section className="metrics-row">
          <div className="metric-card">
            <div className="metric-label">Unprocessed Leads</div>
            <div className="metric-value text-amber-500" style={{ color: 'var(--amber)' }}>{totalRawLeads}</div>
          </div>
          <div className="metric-card">
            <div className="metric-label">AI Enriched</div>
            <div className="metric-value" style={{ color: 'var(--accent)' }}>{totalEnrichedLeads}</div>
          </div>
          <div className="metric-card">
            <div className="metric-label">Duplicates Merged</div>
            <div className="metric-value" style={{ color: 'var(--green)' }}>{totalMergedLeads}</div>
          </div>
          <div className="metric-card">
            <div className="metric-label">Golden Master Records</div>
            <div className="metric-value" style={{ fontFamily: 'JetBrains Mono' }}>{goldenLeads.length}</div>
          </div>
        </section>

        {/* 3. Lead Tables */}
        <div className="board-sections" style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          
          {/* Raw / Enriched Leads list */}
          <div className="card-wrap">
            <div className="card-title">
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Database size={16} color="var(--accent)" />
                Incoming Lead Sheet
              </span>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>
                Total: {leads.length}
              </span>
            </div>
            <div className="data-table-container">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Company</th>
                    <th>Email</th>
                    <th>Status</th>
                    <th>Enriched Industry</th>
                    <th>Stack</th>
                    <th>Outreach Pitch</th>
                  </tr>
                </thead>
                <tbody>
                  {leads.length === 0 ? (
                    <tr>
                      <td colSpan="7" style={{ textAlign: 'center', color: 'var(--text-dim)', padding: '2rem' }}>
                        No leads found. Click 'Reset & Seed Demo Data' to populate.
                      </td>
                    </tr>
                  ) : (
                    leads.map(lead => (
                      <tr key={lead.id} className={lead.status === 'MERGED' ? 'merged' : ''}>
                        <td style={{ fontWeight: 600 }}>{lead.name}</td>
                        <td>{lead.company}</td>
                        <td style={{ fontFamily: 'JetBrains Mono', fontSize: '0.75rem' }}>
                          <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                            <Mail size={12} color="var(--text-dim)" />
                            {lead.email}
                          </span>
                        </td>
                        <td>
                          <span className={`badge badge-${lead.status.toLowerCase()}`}>
                            {lead.status}
                          </span>
                        </td>
                        <td>{lead.enrichedIndustry || '—'}</td>
                        <td>
                          {lead.enrichedTechStack ? (
                            <code style={{ fontSize: '0.7rem', color: 'var(--accent)', background: 'rgba(37,99,235,0.05)', padding: '2px 4px', borderRadius: '4px' }}>
                              {lead.enrichedTechStack}
                            </code>
                          ) : '—'}
                        </td>
                        <td style={{ maxWidth: '200px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }} title={lead.outreachMessage}>
                          {lead.outreachMessage || '—'}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>

          {/* Golden Records list */}
          <div className="card-wrap">
            <div className="card-title">
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Layers size={16} color="var(--green)" />
                Golden Master Records (Deduplicated Truth)
              </span>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>
                Master Count: {goldenLeads.length}
              </span>
            </div>
            <div className="data-table-container">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Master ID</th>
                    <th>Resolved Name</th>
                    <th>Resolved Company</th>
                    <th>Resolved Email</th>
                    <th>Consolidated Raw IDs</th>
                    <th>Last Updated</th>
                  </tr>
                </thead>
                <tbody>
                  {goldenLeads.length === 0 ? (
                    <tr>
                      <td colSpan="6" style={{ textAlign: 'center', color: 'var(--text-dim)', padding: '2rem' }}>
                        No Golden Records created yet. Tell the AI agent to "find and merge duplicates" to create one.
                      </td>
                    </tr>
                  ) : (
                    goldenLeads.map(golden => (
                      <tr key={golden.id}>
                        <td style={{ fontFamily: 'JetBrains Mono', fontSize: '0.7rem', color: 'var(--text-dim)' }}>
                          {golden.id.substring(0, 8)}...
                        </td>
                        <td style={{ fontWeight: 600, color: 'var(--green)' }}>{golden.name}</td>
                        <td>{golden.company}</td>
                        <td style={{ fontFamily: 'JetBrains Mono', fontSize: '0.75rem' }}>{golden.email}</td>
                        <td>
                          <span style={{ display: 'flex', gap: '4px', flexWrap: 'wrap' }}>
                            {golden.mergedRecordIds.map(rid => (
                              <span key={rid} style={{ fontSize: '0.65rem', background: 'rgba(255,255,255,0.05)', border: '1px solid var(--border)', padding: '1px 4px', borderRadius: '3px', color: 'var(--text-muted)' }}>
                                {rid.substring(0, 4)}...
                              </span>
                            ))}
                          </span>
                        </td>
                        <td style={{ fontSize: '0.7rem', color: 'var(--text-dim)' }}>
                          {new Date(golden.updatedAt).toLocaleTimeString()}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>

        </div>
      </div>

      {/* 4. Chat Sidebar Panel (Right) */}
      <div className="chat-sidebar">
        <div className="chat-header">
          <div className="chat-header-status"></div>
          <span className="chat-header-title">SmartLead AI Steward</span>
        </div>

        {/* Chat message history */}
        <div className="chat-messages">
          {messages.map((msg, index) => (
            <div key={index} className={`message ${msg.sender}`}>
              {msg.text}
            </div>
          ))}
          {isTyping && (
            <div className="typing-indicator">
              <div className="typing-dot"></div>
              <div className="typing-dot"></div>
              <div className="typing-dot"></div>
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>

        {/* Suggestion Chips */}
        <div style={{ padding: '0.5rem 1rem', display: 'flex', flexDirection: 'column', gap: '4px', borderTop: '1px solid var(--border)' }}>
          <span style={{ fontSize: '0.65rem', color: 'var(--text-dim)', fontWeight: 600, textTransform: 'uppercase' }}>Quick Commands</span>
          <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
            <button 
              onClick={() => handleSendMessage("enrich all raw leads")} 
              style={{ fontSize: '0.7rem', background: 'var(--card)', border: '1px solid var(--border)', borderRadius: '15px', padding: '4px 10px', color: 'var(--text-muted)', cursor: 'pointer', outline: 'none' }}
              disabled={isTyping}
            >
              🪄 Enrich Leads
            </button>
            <button 
              onClick={() => handleSendMessage("find and merge duplicates")} 
              style={{ fontSize: '0.7rem', background: 'var(--card)', border: '1px solid var(--border)', borderRadius: '15px', padding: '4px 10px', color: 'var(--text-muted)', cursor: 'pointer', outline: 'none' }}
              disabled={isTyping}
            >
              🔄 Merge Duplicates
            </button>
          </div>
        </div>

        {/* Input box */}
        <form 
          className="chat-input-form" 
          onSubmit={(e) => { e.preventDefault(); handleSendMessage(); }}
        >
          <input 
            type="text" 
            className="chat-input" 
            placeholder="Instruct the Data Steward..."
            value={inputText}
            onChange={(e) => setInputText(e.target.value)}
            disabled={isTyping}
          />
          <button 
            type="submit" 
            className="action-btn" 
            style={{ padding: '0.6rem' }}
            disabled={isTyping}
          >
            <Send size={14} />
          </button>
        </form>
      </div>

      {/* 5. Live Notification Toast for SSE Event Sync */}
      {notification && (
        <div className="live-alert">
          <Sparkles size={14} />
          <span>{notification}</span>
        </div>
      )}
    </div>
  )
}
