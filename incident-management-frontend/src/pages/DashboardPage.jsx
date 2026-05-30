import { useState, useEffect, useCallback } from 'react'
import { getIncidents, createIncident } from '../api/incidents'
import IncidentList from '../components/IncidentList'
import IncidentForm from '../components/IncidentForm'

export default function DashboardPage({ role, username, onSelect, onLogout }) {
  const [incidents, setIncidents] = useState([])
  const [loading, setLoading] = useState(false)
  const [showForm, setShowForm] = useState(false)
  const [error, setError] = useState(null)

  const fetchIncidents = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await getIncidents()
      setIncidents(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchIncidents()
  }, [fetchIncidents])

  async function handleCreate(title, description) {
    await createIncident(title, description)
    setShowForm(false)
    fetchIncidents()
  }

  return (
    <>
      <header className="app-header">
        <h1 className="app-header-title">Incident Management</h1>
        <div className="header-right">
          <span className="badge">{role}</span>
          <span className="header-user">{username}</span>
          <button className="btn-link" onClick={onLogout}>Sign out</button>
        </div>
      </header>

      <div className="dashboard-body">
        {role === 'CUSTOMER' && (
          <div className="dashboard-actions">
            <button className="btn btn-primary" onClick={() => setShowForm(s => !s)}>
              {showForm ? 'Cancel' : '+ New Incident'}
            </button>
          </div>
        )}
        {showForm && (
          <IncidentForm onSubmit={handleCreate} onCancel={() => setShowForm(false)} />
        )}
        <IncidentList
          incidents={incidents}
          loading={loading}
          error={error}
          onSelect={onSelect}
        />
      </div>
    </>
  )
}
