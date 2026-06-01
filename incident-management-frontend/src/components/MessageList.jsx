export default function MessageList({ messages, role }) {
  if (messages.length === 0) {
    return <div className="messages-empty">No messages yet — be the first to respond.</div>
  }

  return (
    <div className="message-list">
      {messages.map(msg => (
        <MessageEntry key={msg.id} msg={msg} role={role} />
      ))}
    </div>
  )
}

function MessageEntry({ msg, role }) {
  const isSystem = msg.authorRole === 'SYSTEM'
  const isDevOnly = msg.visibility === 'DEVELOPER_ONLY'

  return (
    <div className={`message-entry${isSystem ? ' message-system' : ''}${isDevOnly ? ' message-dev-only' : ''}`}>
      <div className="message-meta">
        <span className="message-author">
          {isSystem ? '🤖 system' : msg.authorUsername}
        </span>
        {isDevOnly && (
          <span className="message-visibility-badge">Developer only</span>
        )}
        <span className="message-time">{new Date(msg.createdAt).toLocaleString()}</span>
      </div>
      <div className="message-content">{msg.content}</div>
    </div>
  )
}
