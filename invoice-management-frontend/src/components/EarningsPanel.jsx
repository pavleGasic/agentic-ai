import { useState } from 'react'

export default function EarningsPanel({ batch }) {
  const [loading, setLoading] = useState(false)
  const [results, setResults] = useState(null)
  const [error, setError] = useState(null)

  if (!batch) {
    return (
      <div className="panel earnings-panel">
        <div className="panel-header">💰 Earnings Calculation</div>
        <div className="panel-body">
          <div className="log-empty">
            <div className="log-empty-icon">🖱</div>
            Click a batch to calculate earnings
          </div>
        </div>
      </div>
    )
  }

  async function calculate() {
    setLoading(true)
    setError(null)
    setResults(null)
    try {
      const res = await fetch(`/batches/${batch.id}/earnings`, { method: 'POST' })
      const data = await res.json()
      if (!res.ok) {
        setError(data.message || 'Calculation failed')
      } else {
        setResults(data)
      }
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="panel earnings-panel">
      <div className="panel-header">💰 Earnings Calculation</div>
      <div className="panel-body">
        <button
          className="btn btn-primary earnings-calc-btn"
          disabled={loading}
          onClick={calculate}
        >
          {loading ? 'Calculating…' : 'Calculate earnings'}
        </button>

        {error && <div className="earnings-error">⚠ {error}</div>}
        {results && results.length === 0 && (
          <div className="earnings-empty">No processed invoices found for this batch.</div>
        )}
        {results && results.length > 0 && <EarningsResults results={results} />}
      </div>
    </div>
  )
}

function EarningsResults({ results }) {
  const total = results.reduce((sum, r) => sum + Number(r.totalAmount), 0)

  return (
    <div className="earnings-results">
      <div className="earnings-results-header">Results</div>
      <table className="earnings-table">
        <thead>
          <tr>
            <th>Vendor</th>
            <th>Base</th>
            <th>Bonus</th>
            <th>Total</th>
          </tr>
        </thead>
        <tbody>
          {results.map(r => (
            <tr key={r.vendorCode}>
              <td>
                <span className="vendor-code-tag">{r.vendorCode}</span>
                <span className="earnings-vendor-name">{r.vendorName}</span>
              </td>
              <td className="earnings-amount">{Number(r.baseAmount).toFixed(2)}</td>
              <td><BonusBadge rate={r.bonusRate} /></td>
              <td className="earnings-amount earnings-total">{Number(r.totalAmount).toFixed(2)}</td>
            </tr>
          ))}
        </tbody>
        <tfoot>
          <tr>
            <td colSpan={3} className="earnings-footer-label">Grand total</td>
            <td className="earnings-amount earnings-grand-total">{total.toFixed(2)}</td>
          </tr>
        </tfoot>
      </table>
    </div>
  )
}

function BonusBadge({ rate }) {
  const pct = (Number(rate) * 100).toFixed(0)
  if (pct === '0') return <span className="bonus-badge none">—</span>
  return <span className="bonus-badge active">+{pct}%</span>
}
