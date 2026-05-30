import { useCallback, useEffect, useRef, useState } from 'react'
import UploadPanel from './components/UploadPanel'
import InvoiceTable from './components/InvoiceTable'
import LogPanel from './components/LogPanel'
import VendorPanel from './components/VendorPanel'

export default function App() {
  const [invoices, setInvoices] = useState([])
  const [invoicesLoading, setInvoicesLoading] = useState(false)
  const [statusFilter, setStatusFilter] = useState('')

  const [selectedInvoice, setSelectedInvoice] = useState(null)
  const [logs, setLogs] = useState([])
  const [logsLoading, setLogsLoading] = useState(false)

  const [vendors, setVendors] = useState([])
  const [vendorsLoading, setVendorsLoading] = useState(false)

  const autoRefreshRef = useRef(false)
  const intervalRef = useRef(null)

  const fetchVendors = useCallback(async () => {
    setVendorsLoading(true)
    try {
      const res = await fetch('/vendors')
      const data = await res.json()
      setVendors(Array.isArray(data) ? data : [])
    } catch {
      setVendors([])
    } finally {
      setVendorsLoading(false)
    }
  }, [])

  const fetchInvoices = useCallback(async (filter) => {
    setInvoicesLoading(true)
    try {
      const qs = filter ? `?status=${filter}` : ''
      const res = await fetch(`/invoices${qs}`)
      const data = await res.json()
      setInvoices(Array.isArray(data) ? data : [])
    } catch {
      setInvoices([])
    } finally {
      setInvoicesLoading(false)
    }
  }, [])

  const fetchLogs = useCallback(async (invoiceId) => {
    setLogsLoading(true)
    try {
      const res = await fetch(`/logs/${invoiceId}`)
      const data = await res.json()
      setLogs(Array.isArray(data) ? data : [])
    } catch {
      setLogs([])
    } finally {
      setLogsLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchVendors()
    fetchInvoices(statusFilter)
  }, [fetchVendors, fetchInvoices])

  useEffect(() => {
    fetchInvoices(statusFilter)
  }, [statusFilter, fetchInvoices])

  // Re-fetch logs when invoices update and one is selected
  useEffect(() => {
    if (selectedInvoice) fetchLogs(selectedInvoice.invoiceId)
  }, [invoices, selectedInvoice, fetchLogs])

  function handleSelect(inv) {
    setSelectedInvoice(inv)
    fetchLogs(inv.invoiceId)
  }

  function handleFilterChange(f) {
    setStatusFilter(f)
    setSelectedInvoice(null)
    setLogs([])
  }

  function handleRefresh(enableAuto) {
    fetchInvoices(statusFilter)
    if (selectedInvoice) fetchLogs(selectedInvoice.invoiceId)

    // toggle auto-refresh
    if (enableAuto !== undefined) {
      autoRefreshRef.current = enableAuto
      clearInterval(intervalRef.current)
      if (enableAuto) {
        intervalRef.current = setInterval(() => {
          fetchInvoices(statusFilter)
          if (selectedInvoice) fetchLogs(selectedInvoice.invoiceId)
        }, 3000)
      }
    }
  }

  // Cleanup on unmount
  useEffect(() => () => clearInterval(intervalRef.current), [])

  return (
    <>
      <header className="app-header">
        <span style={{ fontSize: '1.2rem' }}>🧾</span>
        <h1>Invoice Processing Dashboard</h1>
        <span className="badge">AI Incident Observability</span>
      </header>

      <div className="app-body">
        <UploadPanel onUploaded={() => fetchInvoices(statusFilter)} />

        <InvoiceTable
          invoices={invoices}
          loading={invoicesLoading}
          selectedId={selectedInvoice?.id}
          onSelect={handleSelect}
          onFilterChange={handleFilterChange}
          onRefresh={handleRefresh}
        />

        <LogPanel
          invoiceId={selectedInvoice?.invoiceId}
          logs={logs}
          loading={logsLoading}
        />

        <VendorPanel
          vendors={vendors}
          loading={vendorsLoading}
          onCreated={fetchVendors}
        />
      </div>
    </>
  )
}
