import './EmptyState.css'

/** A friendly box for a list with nothing in it: an icon, one sentence and, as children, the next step to take. */
export default function EmptyState({ icon: Icon, text, children }) {
  return (
    <div className="empty-state">
      <span className="empty-state-icon">
        <Icon size={32} strokeWidth={1.5} aria-hidden="true" />
      </span>
      <p>{text}</p>
      {children}
    </div>
  )
}
