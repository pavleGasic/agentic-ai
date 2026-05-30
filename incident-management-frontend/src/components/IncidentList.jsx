export default function IncidentList({ incidents, loading, error, onSelect }) {
  if (loading) {
    return (
      <div className="table-empty">
        <div className="spinner" />
        Loading incidents…
      </div>
    )
  }
  if (error) {
    return <div className="table-empty error-text">{error}</div>
  }
  if (incidents.length === 0) {
    return <div className="table-empty">No incidents found</div>
  }

  return (
    <div className="table-wrap">
      <table className="incident-table">
        <thead>
          <tr>
            <th>#</th>
            <th>Title</th>
            <th>Reported by</th>
            <th>Created</th>
          </tr>
        </thead>
        <tbody>
          {incidents.map(inc => (
            <tr key={inc.id} className="clickable" onClick={() => onSelect(inc.id)}>
              <td className="id-cell">{inc.id}</td>
              <td className="incident-title-cell">{inc.title}</td>
              <td>{inc.createdByUsername}</td>
              <td className="date-cell">{new Date(inc.createdAt).toLocaleString()}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
