import { useState } from 'react'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import IncidentDetailPage from './pages/IncidentDetailPage'

export default function App() {
  const [auth, setAuth] = useState(() => {
    const token = localStorage.getItem('token')
    const role = localStorage.getItem('role')
    const username = localStorage.getItem('username')
    return token ? { token, role, username } : null
  })

  const [selectedIncidentId, setSelectedIncidentId] = useState(null)

  function handleLogin(loginData) {
    localStorage.setItem('token', loginData.token)
    localStorage.setItem('role', loginData.role)
    localStorage.setItem('username', loginData.username)
    setAuth(loginData)
  }

  function handleLogout() {
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    localStorage.removeItem('username')
    setAuth(null)
    setSelectedIncidentId(null)
  }

  if (!auth) {
    return <LoginPage onLogin={handleLogin} />
  }

  if (selectedIncidentId !== null) {
    return (
      <IncidentDetailPage
        incidentId={selectedIncidentId}
        role={auth.role}
        username={auth.username}
        onBack={() => setSelectedIncidentId(null)}
        onLogout={handleLogout}
      />
    )
  }

  return (
    <DashboardPage
      role={auth.role}
      username={auth.username}
      onSelect={setSelectedIncidentId}
      onLogout={handleLogout}
    />
  )
}
