function StatusBadge({ status }) {
  return (
    <span className={`status-badge ${status}`}>
      <span className="dot" />
      {status}
    </span>
  )
}

export default function BatchInvoicesPanel({ batch, invoices, loading }) {
  if (!batch) {
    return (
      <div className="panel batch-invoices-panel">
        <div className="panel-header">🧾 Batch Invoices</div>
        <div className="panel-body">
          <div className="log-empty">
            <div className="log-empty-icon">🖱</div>
            Click a batch to view its invoices
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="panel batch-invoices-panel">
      <div className="panel-header">
        🧾 Invoices for batch
        <span className="batch-uuid-header" title={batch.id}>{batch.id}</span>
        <span className="count">{invoices.length}</span>
      </div>
      <div className="panel-body" style={{ padding: 0 }}>
        {loading ? (
          <div className="log-empty"><div className="spinner" />Loading invoices…</div>
        ) : invoices.length === 0 ? (
          <div className="log-empty">No invoices found for this batch</div>
        ) : (
          <table className="invoice-table">
            <colgroup>
              <col /><col /><col /><col /><col /><col /><col />
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
              </tr>
            </thead>
            <tbody>
              {invoices.map(inv => (
                <tr key={inv.id}>
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
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}
