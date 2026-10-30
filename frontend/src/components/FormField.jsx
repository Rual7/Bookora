import { useId } from 'react'
import { capitalize } from '../utils/format.js'

/** A labelled input (or textarea) with an optional hint and error message. */
export default function FormField({ label, name, error, hint, multiline = false, ...controlProps }) {
  const id = useId()
  const messageId = `${id}-message`
  const Control = multiline ? 'textarea' : 'input'

  return (
    <div className="field">
      <label htmlFor={id} className="field-label">{label}</label>
      <Control
        id={id}
        name={name}
        aria-invalid={error ? 'true' : undefined}
        aria-describedby={error || hint ? messageId : undefined}
        {...controlProps}
      />
      {error ? (
        <p id={messageId} className="field-error">{capitalize(error)}</p>
      ) : (
        hint && <p id={messageId} className="field-hint">{hint}</p>
      )}
    </div>
  )
}
