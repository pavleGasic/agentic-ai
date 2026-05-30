function authHeader() {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export async function getIncidents() {
  const res = await fetch('/api/incidents', {
    headers: { ...authHeader() },
  })
  if (!res.ok) throw new Error('Failed to load incidents')
  return res.json()
}

export async function createIncident(title, description) {
  const res = await fetch('/api/incidents', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeader() },
    body: JSON.stringify({ title, description }),
  })
  const data = await res.json()
  if (!res.ok) throw new Error(data.message || 'Failed to create incident')
  return data
}

export async function getIncidentById(id) {
  const res = await fetch(`/api/incidents/${id}`, {
    headers: { ...authHeader() },
  })
  if (!res.ok) throw new Error('Failed to load incident')
  return res.json()
}
