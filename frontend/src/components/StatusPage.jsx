import './StatusPage.css'

/** A full-page message for a missing page or a failure: a big code, a title, one sentence and the next steps as children. */
export default function StatusPage({ icon: Icon, code, title, text, children }) {
  return (
    <div className="container status-page">
      <span className="status-page-icon">
        <Icon size={40} strokeWidth={1.5} aria-hidden="true" />
      </span>
      {code && <p className="status-page-code" aria-hidden="true">{code}</p>}
      <h1>{title}</h1>
      <p className="muted status-page-text">{text}</p>
      {children && <div className="status-page-actions">{children}</div>}
    </div>
  )
}
