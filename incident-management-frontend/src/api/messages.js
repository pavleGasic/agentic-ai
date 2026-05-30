function authHeader() {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export async function getMessages(incidentId) {
  const res = await fetch(`/api/incidents/${incidentId}/messages`, {
    headers: { ...authHeader() },
  })
  if (!res.ok) throw new Error('Failed to load messages')
  return res.json()
}

export async function addMessage(incidentId, content) {
  const res = await fetch(`/api/incidents/${incidentId}/messages`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeader() },
    body: JSON.stringify({ content }),
  })
  const data = await res.json()
  if (!res.ok) throw new Error(data.message || 'Failed to add message')
  return data
}
