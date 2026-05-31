import { useCallback, useEffect, useRef, useState } from 'react'
import UploadPanel from './components/UploadPanel'
import InvoiceTable from './components/InvoiceTable'
import VendorPanel from './components/VendorPanel'
import BatchUploadsPanel from './components/BatchUploadsPanel'
import BatchInvoicesPanel from './components/BatchInvoicesPanel'

export default function App() {
  const [invoices, setInvoices] = useState([])
  const [invoicesLoading, setInvoicesLoading] = useState(false)
  const [statusFilter, setStatusFilter] = useState('')
  const [selectedInvoice, setSelectedInvoice] = useState(null)

  const [vendors, setVendors] = useState([])
  const [vendorsLoading, setVendorsLoading] = useState(false)

  const [batches, setBatches] = useState([])
  const [batchesLoading, setBatchesLoading] = useState(false)
  const [selectedBatch, setSelectedBatch] = useState(null)
  const [batchInvoices, setBatchInvoices] = useState([])
  const [batchInvoicesLoading, setBatchInvoicesLoading] = useState(false)

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

  const fetchBatches = useCallback(async () => {
    setBatchesLoading(true)
    try {
      const res = await fetch('/batches')
      const data = await res.json()
      setBatches(Array.isArray(data) ? data : [])
    } catch {
      setBatches([])
    } finally {
      setBatchesLoading(false)
    }
  }, [])

  const fetchBatchInvoices = useCallback(async (batchId) => {
    setBatchInvoicesLoading(true)
    try {
      const res = await fetch(`/batches/${batchId}/invoices`)
      const data = await res.json()
      setBatchInvoices(Array.isArray(data) ? data : [])
    } catch {
      setBatchInvoices([])
    } finally {
      setBatchInvoicesLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchVendors()
    fetchInvoices(statusFilter)
    fetchBatches()
  }, [fetchVendors, fetchInvoices, fetchBatches])

  useEffect(() => {
    fetchInvoices(statusFilter)
  }, [statusFilter, fetchInvoices])

  function handleSelect(inv) {
    setSelectedInvoice(inv)
  }

  function handleFilterChange(f) {
    setStatusFilter(f)
    setSelectedInvoice(null)
  }

  function handleBatchSelect(batch) {
    setSelectedBatch(batch)
    fetchBatchInvoices(batch.id)
  }

  function handleRefresh(enableAuto) {
    fetchInvoices(statusFilter)
    fetchBatches()
    if (selectedBatch) fetchBatchInvoices(selectedBatch.id)

    if (enableAuto !== undefined) {
      autoRefreshRef.current = enableAuto
      clearInterval(intervalRef.current)
      if (enableAuto) {
        intervalRef.current = setInterval(() => {
          fetchInvoices(statusFilter)
          fetchBatches()
          if (selectedBatch) fetchBatchInvoices(selectedBatch.id)
        }, 3000)
      }
    }
  }

  useEffect(() => () => clearInterval(intervalRef.current), [])

  return (
    <>
      <header className="app-header">
        <span style={{ fontSize: '1.2rem' }}>🧾</span>
        <h1>Invoice Processing Dashboard</h1>
        <span className="badge">AI Incident Observability</span>
      </header>

      <div className="app-body">
        <div className="col-left">
          <UploadPanel onUploaded={() => { fetchInvoices(statusFilter); fetchBatches() }} />
          <VendorPanel
            vendors={vendors}
            loading={vendorsLoading}
            onCreated={fetchVendors}
          />
        </div>

        <InvoiceTable
          invoices={invoices}
          loading={invoicesLoading}
          selectedId={selectedInvoice?.id}
          onSelect={handleSelect}
          onFilterChange={handleFilterChange}
          onRefresh={handleRefresh}
        />

        <div className="col-right">
          <BatchUploadsPanel
            batches={batches}
            loading={batchesLoading}
            selectedBatchId={selectedBatch?.id}
            onSelect={handleBatchSelect}
          />
          <BatchInvoicesPanel
            batch={selectedBatch}
            invoices={batchInvoices}
            loading={batchInvoicesLoading}
          />
        </div>
      </div>
    </>
  )
}
