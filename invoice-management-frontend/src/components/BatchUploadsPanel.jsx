import { useState } from 'react'

export default function BatchUploadsPanel({ batches, loading, onSelect, selectedBatchId }) {
  return (
    <div className="panel batch-panel">
      <div className="panel-header">
        📦 Batch Uploads
        <span className="count">{batches.length}</span>
      </div>
      <div className="panel-body">
        {loading ? (
          <div className="log-empty"><div className="spinner" />Loading…</div>
        ) : batches.length === 0 ? (
          <div className="log-empty">No batches uploaded yet</div>
        ) : (
          <div className="batch-list">
            {batches.map(b => (
              <BatchRow
                key={b.id}
                batch={b}
                selected={selectedBatchId === b.id}
                onSelect={onSelect}
              />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

function BatchRow({ batch, selected, onSelect }) {
  const started = batch.startDate ? new Date(batch.startDate).toLocaleString() : '—'
  const ok = batch.importedInvoices ?? 0
  const err = batch.errorInvoices ?? 0
  const hasErrors = err > 0

  return (
    <div
      className={`batch-row${selected ? ' selected' : ''}`}
      onClick={() => onSelect(batch)}
    >
      <div className="batch-row-top">
        <span className="batch-filename" title={batch.importFileName}>
          {batch.importFileName ?? '—'}
        </span>
        <span className="batch-date">{started}</span>
      </div>
      <div className="batch-uuid" title={batch.id}>{batch.id}</div>
      <div className="batch-stats">
        <span className="stat-pill ok">{ok} queued</span>
        {hasErrors && <span className="stat-pill fail">{err} failed</span>}
      </div>
    </div>
  )
}
