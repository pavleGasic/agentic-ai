import { useState, useEffect, useCallback } from 'react'
import { getIncidentById } from '../api/incidents'
import { getMessages } from '../api/messages'
import MessageList from '../components/MessageList'
import MessageForm from '../components/MessageForm'

export default function IncidentDetailPage({ incidentId, role, username, onBack, onLogout }) {
  const [incident, setIncident] = useState(null)
  const [messages, setMessages] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const fetchAll = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const [inc, msgs] = await Promise.all([
        getIncidentById(incidentId),
        getMessages(incidentId),
      ])
      setIncident(inc)
      setMessages(msgs)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [incidentId])

  useEffect(() => {
    fetchAll()
  }, [fetchAll])

  async function handleMessageAdded() {
    try {
      const msgs = await getMessages(incidentId)
      setMessages(msgs)
    } catch {
      // non-critical
    }
  }

  if (loading) {
    return (
      <div className="page-loading">
        <div className="spinner" />
        Loading…
      </div>
    )
  }

  if (error) {
    return <div className="page-error">{error}</div>
  }

  return (
    <>
      <header className="app-header">
        <button className="btn-back" onClick={onBack}>← Back</button>
        <h1 className="app-header-title">{incident?.title}</h1>
        <div className="header-right">
          <span className="badge">{role}</span>
          <span className="header-user">{username}</span>
          <button className="btn-link" onClick={onLogout}>Sign out</button>
        </div>
      </header>

      <div className="detail-body">
        <div className="incident-card">
          <p className="incident-description">{incident?.description || 'No description provided.'}</p>
          <p className="incident-meta">Reported by <strong>{incident?.createdByUsername}</strong></p>
        </div>

        <div className="messages-section">
          <h2 className="section-title">Messages</h2>
          <MessageList messages={messages} role={role} />
          <MessageForm incidentId={incidentId} onAdded={handleMessageAdded} />
        </div>
      </div>
    </>
  )
}
