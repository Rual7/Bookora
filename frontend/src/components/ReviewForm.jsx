import { useState } from 'react'
import { Link } from 'react-router-dom'
import FormField from './FormField.jsx'
import StarInput from './StarInput.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { api, formErrors } from '../api/client.js'
import { t } from '../i18n/t.js'
import './ReviewForm.css'

/** Lets a signed-in visitor write a review. It is saved as pending until an administrator approves it. */
export default function ReviewForm({ slug }) {
  const { user } = useAuth()
  const [form, setForm] = useState({ rating: 0, title: '', comment: '' })
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})
  const [busy, setBusy] = useState(false)
  const [sent, setSent] = useState(false)

  if (user === undefined) return null
  if (user === null) {
    return (
      <p>
        <Link to="/login" className="text-link">{t('review.signIn')}</Link>
      </p>
    )
  }
  if (sent) return <p className="form-ok" role="status">{t('review.thanks')}</p>

  function update(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  async function submit(event) {
    event.preventDefault()
    if (form.rating === 0) {
      setFieldErrors({ rating: t('review.chooseRating') })
      return
    }
    setBusy(true)
    setError('')
    setFieldErrors({})
    try {
      await api.post(`/api/books/${encodeURIComponent(slug)}/reviews`, form)
      setSent(true)
    } catch (problem) {
      const { fields, message } = formErrors(problem)
      setFieldErrors(fields)
      setError(message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <form className="form review-form" onSubmit={submit} noValidate>
      <h3>{t('review.write')}</h3>
      <p className="muted">{t('review.note')}</p>

      <StarInput value={form.rating} onChange={(rating) => setForm({ ...form, rating })} error={fieldErrors.rating} />
      <FormField label={t('review.title')} name="title" value={form.title} onChange={update} error={fieldErrors.title} maxLength={150} />
      <FormField label={t('review.comment')} name="comment" value={form.comment} onChange={update} error={fieldErrors.comment}
        multiline rows={5} maxLength={2000} required />

      {error && <p className="form-error" role="alert">{error}</p>}
      <button type="submit" className="btn" disabled={busy}>{busy ? t('review.sending') : t('review.send')}</button>
    </form>
  )
}
