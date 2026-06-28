import React, { useState, useEffect, useRef } from 'react'
import { 
  Sparkles, 
  RefreshCw, 
  Database, 
  Send, 
  Layers, 
  Mail,
  Briefcase,
  Sun,
  Moon,
  LayoutGrid,
  Trello,
  Table,
  X,
  Cpu
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
  
  // New UI/UX states
  const [activeNav, setActiveNav] = useState('dashboard') // 'dashboard', 'leads', 'golden'
  const [activeTab, setActiveTab] = useState('kanban') // 'kanban', 'table' (within dashboard)
  const [selectedLead, setSelectedLead] = useState(null)
  const [darkMode, setDarkMode] = useState(() => {
    return localStorage.getItem('theme') === 'dark'
  })

  const messagesEndRef = useRef(null)

  // Sync theme to root HTML element
  useEffect(() => {
    if (darkMode) {
      document.documentElement.classList.add('dark')
      localStorage.setItem('theme', 'dark')
    } else {
      document.documentElement.classList.remove('dark')
      localStorage.setItem('theme', 'light')
    }
  }, [darkMode])

  useEffect(() => {
    fetchLeads()
    fetchGoldenLeads()

    // Establish Server-Sent Events (SSE) stream for real-time updates
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
    <div className="app-layout">
      {/* 1. Left Navigation Sidebar */}
      <aside className="nav-sidebar">
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
          <div className="nav-logo" onClick={() => setActiveNav('dashboard')}>⚡</div>
          <nav className="nav-links">
            <div 
              className={`nav-item ${activeNav === 'dashboard' ? 'active' : ''}`} 
              onClick={() => setActiveNav('dashboard')}
              title="Dashboard Overview"
            >
              <LayoutGrid size={20} />
            </div>
            <div 
              className={`nav-item ${activeNav === 'leads' ? 'active' : ''}`} 
              onClick={() => setActiveNav('leads')}
              title="Lead Database"
            >
              <Database size={20} />
            </div>
            <div 
              className={`nav-item ${activeNav === 'golden' ? 'active' : ''}`} 
              onClick={() => setActiveNav('golden')}
              title="Golden Master Records"
            >
              <Layers size={20} />
            </div>
          </nav>
        </div>

        <button 
          className="nav-item" 
          onClick={() => setDarkMode(!darkMode)}
          title={darkMode ? "Switch to Light Mode" : "Switch to Dark Mode"}
          style={{ background: 'transparent', border: 'none' }}
        >
          {darkMode ? <Sun size={20} /> : <Moon size={20} />}
        </button>
      </aside>

      {/* 2. Main Workspace (Center Panel) */}
      <main className="workspace">
        <header className="app-header">
          <div>
            <h1 className="brand-title">SmartLead AI</h1>
            <p className="brand-subtitle">Google Cloud Enterprise Sales Ops Workspace</p>
          </div>
          <div className="header-actions">
            <button 
              className="action-btn secondary" 
              onClick={handleSeedData}
              disabled={isSeeding}
            >
              <RefreshCw className={isSeeding ? "animate-spin" : ""} size={14} />
              {isSeeding ? "Resetting..." : "Reset Demo Data"}
            </button>
          </div>
        </header>

        {/* Metrics Overview Row */}
        <section className="metrics-row">
          <div className="metric-card">
            <div className="metric-label">Unprocessed Leads</div>
            <div className="metric-value" style={{ color: 'var(--amber)' }}>{totalRawLeads}</div>
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
            <div className="metric-label">Golden Masters</div>
            <div className="metric-value">{goldenLeads.length}</div>
          </div>
        </section>

        {/* Tab Selection (only visible when in general dashboard view) */}
        {activeNav === 'dashboard' && (
          <div className="tab-bar">
            <button 
              className={`tab-btn ${activeTab === 'kanban' ? 'active' : ''}`}
              onClick={() => setActiveTab('kanban')}
            >
              <Trello size={14} />
              Pipeline Board
            </button>
            <button 
              className={`tab-btn ${activeTab === 'table' ? 'active' : ''}`}
              onClick={() => setActiveTab('table')}
            >
              <Table size={14} />
              Table View
            </button>
          </div>
        )}

        {/* Dynamic Nav View Rendering */}
        <div style={{ flexGrow: 1, display: 'flex', flexDirection: 'column' }}>
          
          {/* VIEW: Dashboard - Kanban Board */}
          {activeNav === 'dashboard' && activeTab === 'kanban' && (
            <div className="kanban-board">
              {/* Column 1: Incoming Raw */}
              <div className="kanban-column">
                <div className="column-header">
                  <span className="column-title" style={{ color: 'var(--amber)' }}>
                    <Database size={16} />
                    Raw Incoming
                  </span>
                  <span className="column-count">{totalRawLeads}</span>
                </div>
                <div className="kanban-cards">
                  {leads.filter(l => l.status === 'RAW').map(lead => (
                    <div key={lead.id} className="lead-card" onClick={() => setSelectedLead(lead)}>
                      <h4 className="card-lead-name">{lead.name}</h4>
                      <p className="card-lead-company">{lead.company}</p>
                      <div className="card-lead-meta">
                        <span className="badge badge-raw">{lead.status}</span>
                        <span>{lead.email.substring(0, 15)}...</span>
                      </div>
                    </div>
                  ))}
                  {totalRawLeads === 0 && (
                    <div style={{ textAlign: 'center', color: 'var(--text-dim)', padding: '2rem', fontSize: '0.8rem' }}>
                      No raw leads remaining.
                    </div>
                  )}
                </div>
              </div>

              {/* Column 2: AI Enriched */}
              <div className="kanban-column">
                <div className="column-header">
                  <span className="column-title" style={{ color: 'var(--accent)' }}>
                    <Sparkles size={16} />
                    AI Enriched
                  </span>
                  <span className="column-count">{totalEnrichedLeads}</span>
                </div>
                <div className="kanban-cards">
                  {leads.filter(l => l.status === 'ENRICHED').map(lead => (
                    <div key={lead.id} className="lead-card" onClick={() => setSelectedLead(lead)}>
                      <h4 className="card-lead-name">{lead.name}</h4>
                      <p className="card-lead-company">{lead.company}</p>
                      <div style={{ display: 'flex', gap: '4px', marginBottom: '0.5rem', flexWrap: 'wrap' }}>
                        {lead.enrichedIndustry && (
                          <span style={{ fontSize: '0.65rem', background: 'var(--bg-subtle)', padding: '2px 6px', borderRadius: '4px', color: 'var(--text-muted)' }}>
                            {lead.enrichedIndustry}
                          </span>
                        )}
                        {lead.enrichedTechStack && (
                          <span style={{ fontSize: '0.65rem', background: 'var(--accent-glow)', padding: '2px 6px', borderRadius: '4px', color: 'var(--accent)', fontWeight: 600 }}>
                            {lead.enrichedTechStack}
                          </span>
                        )}
                      </div>
                      <div className="card-lead-meta">
                        <span className="badge badge-enriched">{lead.status}</span>
                        <span>{lead.email.substring(0, 15)}...</span>
                      </div>
                    </div>
                  ))}
                  {totalEnrichedLeads === 0 && (
                    <div style={{ textAlign: 'center', color: 'var(--text-dim)', padding: '2rem', fontSize: '0.8rem' }}>
                      No enriched leads. Ask the AI steward to "enrich leads".
                    </div>
                  )}
                </div>
              </div>

              {/* Column 3: Golden Masters */}
              <div className="kanban-column">
                <div className="column-header">
                  <span className="column-title" style={{ color: 'var(--green)' }}>
                    <Layers size={16} />
                    Golden Master
                  </span>
                  <span className="column-count">{goldenLeads.length}</span>
                </div>
                <div className="kanban-cards">
                  {goldenLeads.map(golden => (
                    <div key={golden.id} className="lead-card" style={{ borderLeft: '3px solid var(--green)' }} onClick={() => setSelectedLead({ ...golden, status: 'GOLDEN' })}>
                      <h4 className="card-lead-name" style={{ color: 'var(--green)' }}>{golden.name}</h4>
                      <p className="card-lead-company">{golden.company}</p>
                      <p style={{ fontSize: '0.7rem', color: 'var(--text-muted)', marginBottom: '0.5rem' }}>
                        Consolidated {golden.mergedRecordIds.length} duplicate records
                      </p>
                      <div className="card-lead-meta">
                        <span className="badge badge-merged" style={{ color: '#fff', background: 'var(--green)' }}>Master</span>
                        <span style={{ fontFamily: 'JetBrains Mono', fontSize: '0.65rem' }}>{golden.id.substring(0, 6)}...</span>
                      </div>
                    </div>
                  ))}
                  {goldenLeads.length === 0 && (
                    <div style={{ textAlign: 'center', color: 'var(--text-dim)', padding: '2rem', fontSize: '0.8rem' }}>
                      No Golden Master records yet. Ask the AI steward to "find and merge duplicates".
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* VIEW: Dashboard - Table View OR VIEW: Lead Database */}
          {((activeNav === 'dashboard' && activeTab === 'table') || activeNav === 'leads') && (
            <div className="card-wrap">
              <div className="card-title">
                <span>Incoming Lead Sheet</span>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>
                  Total Count: {leads.length}
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
                      <th>Industry</th>
                      <th>Tech Stack</th>
                    </tr>
                  </thead>
                  <tbody>
                    {leads.length === 0 ? (
                      <tr>
                        <td colSpan="6" style={{ textAlign: 'center', color: 'var(--text-dim)', padding: '2rem' }}>
                          No leads found. Reset demo data to populate.
                        </td>
                      </tr>
                    ) : (
                      leads.map(lead => (
                        <tr 
                          key={lead.id} 
                          className={lead.status === 'MERGED' ? 'merged' : ''} 
                          onClick={() => setSelectedLead(lead)}
                          style={{ cursor: 'pointer' }}
                        >
                          <td style={{ fontWeight: 700 }}>{lead.name}</td>
                          <td>{lead.company}</td>
                          <td style={{ fontFamily: 'JetBrains Mono', fontSize: '0.75rem' }}>
                            <span style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
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
                              <code style={{ fontSize: '0.75rem', color: 'var(--accent)', background: 'var(--accent-glow)', padding: '2px 6px', borderRadius: '4px' }}>
                                {lead.enrichedTechStack}
                              </code>
                            ) : '—'}
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* VIEW: Golden Records Only */}
          {activeNav === 'golden' && (
            <div className="card-wrap">
              <div className="card-title">
                <span>Golden Master Records</span>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>
                  Active Master Count: {goldenLeads.length}
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
                      <th>Consolidated IDs</th>
                      <th>Last Updated</th>
                    </tr>
                  </thead>
                  <tbody>
                    {goldenLeads.length === 0 ? (
                      <tr>
                        <td colSpan="6" style={{ textAlign: 'center', color: 'var(--text-dim)', padding: '2rem' }}>
                          No Golden Records created yet. Tell the AI steward to "find and merge duplicates" to start.
                        </td>
                      </tr>
                    ) : (
                      goldenLeads.map(golden => (
                        <tr key={golden.id} onClick={() => setSelectedLead({ ...golden, status: 'GOLDEN' })} style={{ cursor: 'pointer' }}>
                          <td style={{ fontFamily: 'JetBrains Mono', fontSize: '0.7rem', color: 'var(--text-dim)' }}>
                            {golden.id.substring(0, 10)}...
                          </td>
                          <td style={{ fontWeight: 700, color: 'var(--green)' }}>{golden.name}</td>
                          <td>{golden.company}</td>
                          <td style={{ fontFamily: 'JetBrains Mono', fontSize: '0.75rem' }}>{golden.email}</td>
                          <td>
                            <span style={{ display: 'flex', gap: '4px', flexWrap: 'wrap' }}>
                              {golden.mergedRecordIds.map(rid => (
                                <span key={rid} style={{ fontSize: '0.65rem', background: 'var(--bg-subtle)', border: '1px solid var(--border)', padding: '1px 6px', borderRadius: '3px', color: 'var(--text-muted)' }}>
                                  {rid.substring(0, 5)}...
                                </span>
                              ))}
                            </span>
                          </td>
                          <td style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                            {new Date(golden.updatedAt).toLocaleTimeString()}
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          )}

        </div>
      </main>

      {/* 3. Right Sidebar (Chat Console Panel) */}
      <section className="chat-sidebar">
        <div className="chat-header">
          <div className="chat-header-status"></div>
          <span className="chat-header-title">SmartLead AI Steward</span>
        </div>

        {/* Chat Messages */}
        <div className="chat-messages">
          {messages.map((msg, index) => (
            <div key={index} className={`message ${msg.sender}`}>
              {msg.text}
            </div>
          ))}
          {isTyping && (
            <div className="typing-indicator">
              <div className="typing-dot" style={{ width: '6px', height: '6px', backgroundColor: 'var(--text-muted)', borderRadius: '50%', animation: 'typing 1.4s infinite ease-in-out both' }}></div>
              <div className="typing-dot" style={{ width: '6px', height: '6px', backgroundColor: 'var(--text-muted)', borderRadius: '50%', animation: 'typing 1.4s infinite ease-in-out both', animationDelay: '0.2s' }}></div>
              <div className="typing-dot" style={{ width: '6px', height: '6px', backgroundColor: 'var(--text-muted)', borderRadius: '50%', animation: 'typing 1.4s infinite ease-in-out both', animationDelay: '0.4s' }}></div>
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>

        {/* Quick Command Suggestion Chips */}
        <div style={{ padding: '0.75rem 1.25rem', display: 'flex', flexDirection: 'column', gap: '6px', borderTop: '1px solid var(--border)' }}>
          <span style={{ fontSize: '0.65rem', color: 'var(--text-dim)', fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em' }}>Quick Commands</span>
          <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
            <button 
              onClick={() => handleSendMessage("enrich all raw leads")} 
              style={{ fontSize: '0.75rem', background: 'var(--card)', border: '1px solid var(--border)', borderRadius: '15px', padding: '5px 12px', color: 'var(--text-muted)', cursor: 'pointer', outline: 'none', transition: 'all 0.2s' }}
              disabled={isTyping}
            >
              🪄 Enrich Leads
            </button>
            <button 
              onClick={() => handleSendMessage("find and merge duplicates")} 
              style={{ fontSize: '0.75rem', background: 'var(--card)', border: '1px solid var(--border)', borderRadius: '15px', padding: '5px 12px', color: 'var(--text-muted)', cursor: 'pointer', outline: 'none', transition: 'all 0.2s' }}
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
            style={{ padding: '0.75rem' }}
            disabled={isTyping}
          >
            <Send size={14} />
          </button>
        </form>
      </section>

      {/* 4. Sliding Side Drawer for Lead Details */}
      {selectedLead && (
        <div className="drawer-overlay" onClick={() => setSelectedLead(null)}>
          <div className="drawer-content" onClick={(e) => e.stopPropagation()}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid var(--border)', paddingBottom: '1rem' }}>
              <h3 style={{ fontSize: '1.1rem', fontWeight: 800 }}>Lead Profile Details</h3>
              <button 
                onClick={() => setSelectedLead(null)} 
                style={{ background: 'transparent', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}
              >
                <X size={18} />
              </button>
            </div>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem', flexGrow: 1, overflowY: 'auto' }}>
              <div>
                <span style={{ fontSize: '0.7rem', color: 'var(--text-dim)', textTransform: 'uppercase', fontWeight: 700, letterSpacing: '0.03em' }}>Name</span>
                <p style={{ fontSize: '1.1rem', fontWeight: 800, marginTop: '0.25rem' }}>{selectedLead.name}</p>
              </div>
              
              <div>
                <span style={{ fontSize: '0.7rem', color: 'var(--text-dim)', textTransform: 'uppercase', fontWeight: 700, letterSpacing: '0.03em' }}>Company</span>
                <p style={{ fontSize: '0.95rem', fontWeight: 700, marginTop: '0.25rem', color: 'var(--text-muted)' }}>{selectedLead.company}</p>
              </div>

              <div>
                <span style={{ fontSize: '0.7rem', color: 'var(--text-dim)', textTransform: 'uppercase', fontWeight: 700, letterSpacing: '0.03em' }}>Email Address</span>
                <p style={{ fontSize: '0.9rem', fontFamily: 'JetBrains Mono', marginTop: '0.25rem' }}>{selectedLead.email}</p>
              </div>

              <div>
                <span style={{ fontSize: '0.7rem', color: 'var(--text-dim)', textTransform: 'uppercase', fontWeight: 700, letterSpacing: '0.03em' }}>Pipeline Status</span>
                <div style={{ marginTop: '0.25rem' }}>
                  <span className={`badge ${selectedLead.status === 'GOLDEN' ? 'badge-merged' : `badge-${selectedLead.status.toLowerCase()}`}`}>
                    {selectedLead.status}
                  </span>
                </div>
              </div>

              {selectedLead.enrichedIndustry && (
                <div>
                  <span style={{ fontSize: '0.7rem', color: 'var(--text-dim)', textTransform: 'uppercase', fontWeight: 700, letterSpacing: '0.03em' }}>Enriched Industry</span>
                  <p style={{ fontSize: '0.9rem', marginTop: '0.25rem', display: 'flex', alignItems: 'center', gap: '6px', fontWeight: 600 }}>
                    <Briefcase size={14} color="var(--text-muted)" />
                    {selectedLead.enrichedIndustry}
                  </p>
                </div>
              )}

              {selectedLead.enrichedTechStack && (
                <div>
                  <span style={{ fontSize: '0.7rem', color: 'var(--text-dim)', textTransform: 'uppercase', fontWeight: 700, letterSpacing: '0.03em' }}>Tech Stack</span>
                  <p style={{ fontSize: '0.9rem', marginTop: '0.25rem', display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Cpu size={14} color="var(--accent)" />
                    <code style={{ fontSize: '0.8rem', color: 'var(--accent)', background: 'var(--accent-glow)', padding: '2px 6px', borderRadius: '4px' }}>
                      {selectedLead.enrichedTechStack}
                    </code>
                  </p>
                </div>
              )}

              {selectedLead.outreachMessage && (
                <div style={{ background: 'var(--bg-subtle)', padding: '1rem', borderRadius: '8px', border: '1px solid var(--border)', marginTop: '0.5rem' }}>
                  <span style={{ fontSize: '0.7rem', color: 'var(--text-dim)', textTransform: 'uppercase', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '6px', letterSpacing: '0.03em' }}>
                    <Sparkles size={12} color="var(--accent)" />
                    AI Generated Outreach Pitch
                  </span>
                  <p style={{ fontSize: '0.85rem', lineHeight: 1.5, marginTop: '0.5rem', color: 'var(--text)' }}>
                    {selectedLead.outreachMessage}
                  </p>
                </div>
              )}

              {/* Consolidated Raw IDs (for Golden Master only) */}
              {selectedLead.mergedRecordIds && (
                <div>
                  <span style={{ fontSize: '0.7rem', color: 'var(--text-dim)', textTransform: 'uppercase', fontWeight: 700, letterSpacing: '0.03em' }}>Consolidated CRM Record IDs</span>
                  <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap', marginTop: '0.5rem' }}>
                    {selectedLead.mergedRecordIds.map(rid => (
                      <span key={rid} style={{ fontSize: '0.7rem', background: 'var(--bg-subtle)', border: '1px solid var(--border)', padding: '2px 6px', borderRadius: '4px', fontFamily: 'JetBrains Mono', color: 'var(--text-muted)' }}>
                        {rid}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* 5. Live Notification Toast */}
      {notification && (
        <div className="live-alert">
          <Sparkles size={14} />
          <span>{notification}</span>
        </div>
      )}
    </div>
  )
}
