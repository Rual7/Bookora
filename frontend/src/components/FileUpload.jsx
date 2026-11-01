import { useId, useRef, useState } from 'react'
import { t } from '../i18n/t.js'
import './FileUpload.css'

/** One file input with its own upload button. `onUpload` gets the list of chosen files and may throw an ApiError. */
export default function FileUpload({ label, accept, hint, buttonLabel, multiple = false, onUpload }) {
  const id = useId()
  const input = useRef(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  async function submit(event) {
    event.preventDefault()
    const files = Array.from(input.current.files)
    if (files.length === 0) {
      setError(t('admin.chooseFile'))
      return
    }
    setBusy(true)
    setError('')
    try {
      await onUpload(files)
      input.current.value = ''
    } catch (problem) {
      setError(problem.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <form className="file-upload" onSubmit={submit}>
      <label htmlFor={id} className="field-label">{label}</label>
      <input id={id} ref={input} type="file" accept={accept} multiple={multiple} />
      {hint && <p className="field-hint">{hint}</p>}
      {error && <p className="form-error" role="alert">{error}</p>}
      <button type="submit" className="btn btn-small btn-outline" disabled={busy}>
        {busy ? t('admin.uploading') : buttonLabel}
      </button>
    </form>
  )
}
