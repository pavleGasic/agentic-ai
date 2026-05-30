import { useState } from 'react'

export default function VendorPanel({ vendors, loading, onCreated }) {
  const [form, setForm] = useState({ vendorCode: '', name: '' })
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(null)
  const [open, setOpen] = useState(false)

  async function handleCreate(e) {
    e.preventDefault()
    setSubmitting(true)
    setError(null)
    setSuccess(null)
    try {
      const res = await fetch('/vendors', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      })
      if (!res.ok) {
        const msg = res.status === 400 ? 'Vendor code already exists' : 'Failed to create vendor'
        setError(msg)
      } else {
        setSuccess(`Vendor "${form.name}" created`)
        setForm({ vendorCode: '', name: '' })
        setOpen(false)
        onCreated()
      }
    } catch (e) {
      setError(e.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="panel vendor-panel">
      <div className="panel-header">
        🏢 Vendors
        <span className="count">{vendors.length}</span>
        <button className="vendor-add-btn" onClick={() => { setOpen(o => !o); setError(null); setSuccess(null) }}>
          {open ? '✕' : '+ Add'}
        </button>
      </div>

      {open && (
        <form className="vendor-form" onSubmit={handleCreate}>
          <input
            className="vendor-input"
            placeholder="Vendor code (e.g. ACME)"
            value={form.vendorCode}
            onChange={e => setForm(f => ({ ...f, vendorCode: e.target.value }))}
            required
          />
          <input
            className="vendor-input"
            placeholder="Name"
            value={form.name}
            onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
            required
          />
          <button className="btn btn-primary vendor-submit" disabled={submitting} type="submit">
            {submitting ? 'Saving…' : 'Save'}
          </button>
          {error && <div className="vendor-msg err">{error}</div>}
          {success && <div className="vendor-msg ok">{success}</div>}
        </form>
      )}

      <div className="panel-body vendor-list-body">
        {loading ? (
          <div className="table-empty"><div className="spinner" />Loading…</div>
        ) : vendors.length === 0 ? (
          <div className="vendor-empty">No vendors yet</div>
        ) : (
          <ul className="vendor-list">
            {vendors.map(v => (
              <li key={v.id} className="vendor-item">
                <span className="vendor-code-tag">{v.vendorCode}</span>
                <span className="vendor-name">{v.name}</span>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}
