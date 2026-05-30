export default function MessageList({ messages }) {
  if (messages.length === 0) {
    return <div className="messages-empty">No messages yet — be the first to respond.</div>
  }

  return (
    <div className="message-list">
      {messages.map(msg => (
        <div key={msg.id} className="message-entry">
          <div className="message-meta">
            <span className="message-author">{msg.authorUsername}</span>
            <span className="message-time">{new Date(msg.createdAt).toLocaleString()}</span>
          </div>
          <div className="message-content">{msg.content}</div>
        </div>
      ))}
    </div>
  )
}
