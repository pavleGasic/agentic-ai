import { useState } from 'react'

export default function LogPanel({ invoiceId, logs, loading }) {
  if (!invoiceId) {
    return (
      <div className="panel log-panel">
        <div className="panel-header">📋 Processing Logs</div>
        <div className="panel-body">
          <div className="log-empty">
            <div className="log-empty-icon">🖱</div>
            Click an invoice row to view its logs
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="panel log-panel">
      <div className="panel-header">
        📋 Logs
        <span style={{ color: '#60a5fa', fontFamily: 'monospace', fontWeight: 400 }}>{invoiceId}</span>
        <span className="count">{logs.length}</span>
      </div>
      <div className="panel-body">
        {loading ? (
          <div className="log-empty"><div className="spinner" />Loading logs…</div>
        ) : logs.length === 0 ? (
          <div className="log-empty">No logs yet — processing may still be running</div>
        ) : (
          <div className="log-list">
            {logs.map(log => <LogEntry key={log.id} log={log} />)}
          </div>
        )}
      </div>
    </div>
  )
}

function LogEntry({ log }) {
  const [showTrace, setShowTrace] = useState(false)
  const time = log.timestamp ? new Date(log.timestamp).toLocaleTimeString() : ''

  return (
    <div className={`log-entry ${log.level}`}>
      <div className="log-meta">
        <span className={`log-level ${log.level}`}>{log.level}</span>
        {time && <span className="log-time">{time}</span>}
        {log.module && <span className="log-module">{log.module}</span>}
      </div>
      <div className="log-message">{log.message}</div>
      {log.stackTrace && (
        <>
          <button className="log-trace-btn" onClick={() => setShowTrace(s => !s)}>
            {showTrace ? '▲ hide stack trace' : '▼ show stack trace'}
          </button>
          {showTrace && <div className="log-trace">{log.stackTrace}</div>}
        </>
      )}
    </div>
  )
}
