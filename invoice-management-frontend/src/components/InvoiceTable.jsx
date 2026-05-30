import { useState } from 'react'

const FILTERS = ['ALL', 'PENDING', 'PROCESSED', 'FAILED']

export default function InvoiceTable({ invoices, loading, selectedId, onSelect, onFilterChange, onRefresh }) {
  const [activeFilter, setActiveFilter] = useState('ALL')
  const [autoRefresh, setAutoRefresh] = useState(false)

  function setFilter(f) {
    setActiveFilter(f)
    onFilterChange(f === 'ALL' ? '' : f)
  }

  return (
    <div className="panel invoices-panel">
      <div className="filters-bar">
        {FILTERS.map(f => (
          <button
            key={f}
            className={`filter-btn${activeFilter === f ? ' active ' + f : ''}`}
            onClick={() => setFilter(f)}
          >
            {f === 'ALL' ? 'All' : f.charAt(0) + f.slice(1).toLowerCase()}
          </button>
        ))}

        <div className="auto-refresh-row">
          <label className="toggle">
            <input
              type="checkbox"
              checked={autoRefresh}
              onChange={e => {
                setAutoRefresh(e.target.checked)
                onRefresh(e.target.checked)
              }}
            />
            <span className="slider" />
          </label>
          Auto-refresh
        </div>

        <button className="refresh-btn" onClick={() => onRefresh(false)}>↻ Refresh</button>
      </div>

      <div className="table-wrap">
        {loading ? (
          <div className="table-empty"><div className="spinner" />Loading…</div>
        ) : invoices.length === 0 ? (
          <div className="table-empty">No invoices found</div>
        ) : (
          <table className="invoice-table">
            <colgroup>
              <col /><col /><col /><col /><col /><col /><col /><col />
            </colgroup>
            <thead>
              <tr>
                <th>Invoice ID</th>
                <th>Customer</th>
                <th>Amount</th>
                <th>Currency</th>
                <th>Issue Date</th>
                <th>Vendor</th>
                <th>Status</th>
                <th>Error</th>
              </tr>
            </thead>
            <tbody>
              {invoices.map(inv => (
                <tr
                  key={inv.id}
                  className={selectedId === inv.id ? 'selected' : ''}
                  onClick={() => onSelect(inv)}
                >
                  <td><span className="invoice-id">{inv.invoiceId}</span></td>
                  <td><span className="customer">{inv.customerName}</span></td>
                  <td><span className="amount">{Number(inv.amount).toFixed(2)}</span></td>
                  <td><span className="currency-tag">{inv.currency}</span></td>
                  <td><span className="date-cell">{inv.issueDate}</span></td>
                  <td>
                    {inv.vendor
                      ? <span className="vendor-code-tag">{inv.vendor.vendorCode}</span>
                      : <span className="no-vendor">—</span>}
                  </td>
                  <td><StatusBadge status={inv.status} /></td>
                  <td>
                    {inv.errorMessage && (
                      <span className="error-msg" title={inv.errorMessage}>
                        {inv.errorMessage}
                      </span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <div className="panel-header" style={{ borderTop: '1px solid #f1f5f9', borderBottom: 'none', marginTop: 'auto' }}>
        <span style={{ color: '#94a3b8' }}>{invoices.length} invoice{invoices.length !== 1 ? 's' : ''}</span>
      </div>
    </div>
  )
}

function StatusBadge({ status }) {
  return (
    <span className={`status-badge ${status}`}>
      <span className="dot" />
      {status}
    </span>
  )
}
