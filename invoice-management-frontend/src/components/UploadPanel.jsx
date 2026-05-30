import { useRef, useState } from 'react'

export default function UploadPanel({ onUploaded }) {
  const [file, setFile] = useState(null)
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)
  const [dragover, setDragover] = useState(false)
  const inputRef = useRef()

  function pick(f) {
    if (f && f.name.endsWith('.csv')) {
      setFile(f)
      setResult(null)
    }
  }

  async function upload() {
    if (!file) return
    setLoading(true)
    setResult(null)
    try {
      const form = new FormData()
      form.append('file', file)
      const res = await fetch('/invoices/upload', { method: 'POST', body: form })
      const data = await res.json()
      if (!res.ok) {
        setResult({ type: 'err', message: data.message || 'Upload failed' })
      } else {
        const type = data.failedCount === 0 ? 'ok' : data.successCount === 0 ? 'err' : 'partial'
        setResult({ type, ...data })
        onUploaded()
      }
    } catch (e) {
      setResult({ type: 'err', message: e.message })
    } finally {
      setLoading(false)
    }
  }

  function clear() {
    setFile(null)
    setResult(null)
    inputRef.current.value = ''
  }

  return (
    <div className="panel upload-panel">
      <div className="panel-header">📤 Upload CSV</div>
      <div className="panel-body">
        <div
          className={`drop-zone${dragover ? ' dragover' : ''}`}
          onClick={() => inputRef.current.click()}
          onDragOver={e => { e.preventDefault(); setDragover(true) }}
          onDragLeave={() => setDragover(false)}
          onDrop={e => { e.preventDefault(); setDragover(false); pick(e.dataTransfer.files[0]) }}
        >
          <div className="dz-icon">📄</div>
          <div className="dz-hint">{file ? 'Click or drop to replace' : 'Click or drop a CSV file'}</div>
          {file && <div className="dz-name">{file.name}</div>}
          <input ref={inputRef} type="file" accept=".csv" onChange={e => pick(e.target.files[0])} />
        </div>

        <div className="upload-actions">
          <button className="btn btn-primary" disabled={!file || loading} onClick={upload}>
            {loading ? 'Uploading…' : 'Upload'}
          </button>
          <button className="btn btn-clear" disabled={!file || loading} onClick={clear}>
            ✕ Clear
          </button>
        </div>

        {result && <UploadResult result={result} />}
      </div>
    </div>
  )
}

function UploadResult({ result }) {
  if (result.message) {
    return (
      <div className="upload-result err">
        <div style={{ color: '#f87171', fontSize: '0.8rem' }}>{result.message}</div>
      </div>
    )
  }

  return (
    <div className={`upload-result ${result.type}`}>
      <div className="result-stats">
        <div className="stat">
          <span className="stat-value blue">{result.totalRows}</span>
          <span className="stat-label">Total rows</span>
        </div>
        <div className="stat">
          <span className="stat-value green">{result.successCount}</span>
          <span className="stat-label">Queued</span>
        </div>
        <div className="stat">
          <span className="stat-value red">{result.failedCount}</span>
          <span className="stat-label">Rejected</span>
        </div>
      </div>
      {result.errors?.length > 0 && (
        <div className="error-list">
          {result.errors.map((e, i) => (
            <div key={i} className="error-item">⚠ {e}</div>
          ))}
        </div>
      )}
    </div>
  )
}
