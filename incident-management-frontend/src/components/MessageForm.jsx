import { useState } from 'react'
import { addMessage } from '../api/messages'

export default function MessageForm({ incidentId, onAdded }) {
  const [content, setContent] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  async function handleSubmit(e) {
    e.preventDefault()
    if (!content.trim()) return
    setLoading(true)
    setError(null)
    try {
      await addMessage(incidentId, content)
      setContent('')
      onAdded()
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <form className="message-form" onSubmit={handleSubmit}>
      <textarea
        className="message-input"
        value={content}
        onChange={e => setContent(e.target.value)}
        placeholder="Add a message…"
        rows={3}
        required
      />
      {error && <div className="form-error">{error}</div>}
      <button
        type="submit"
        className="btn btn-primary"
        disabled={loading || !content.trim()}
      >
        {loading ? 'Sending…' : 'Send'}
      </button>
    </form>
  )
}
