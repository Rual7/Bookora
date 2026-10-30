import { useState } from 'react'
import { api } from '../api/client.js'
import FormField from '../components/FormField.jsx'
import { LogoMark } from '../components/Logo.jsx'
import './AboutPage.css'

const EMPTY_FORM = { name: '', email: '', message: '' }

export default function AboutPage() {
  return (
    <>
      <section className="about-hero" aria-labelledby="about-title">
        <div className="container about-hero-inner">
          <div>
            <h1 id="about-title">Books for every story of your life</h1>
            <p className="about-lead">
              Bookora is a small online bookstore for printed books and e-books. We started it because finding
              a good book should feel like walking into a shop you trust: easy to browse, honest about what
              is in stock, and quick to deliver.
            </p>
          </div>
          <div className="about-brand" aria-hidden="true">
            <LogoMark className="about-mark" />
            <span className="about-wordmark">Bookora</span>
          </div>
        </div>
      </section>

      <div className="container about-body">
        <section aria-labelledby="what-we-do">
          <h2 id="what-we-do">What we do</h2>
          <p>
            Every book has a page with its authors, categories and reviews. Pick the paperback or hardcover
            and we deliver it to your door, or choose the e-book and download it right after you pay.
          </p>
          <p>Payments are handled securely online, and your e-books stay available in your account.</p>
        </section>

        <section className="contact-section" aria-labelledby="contact-title">
          <h2 id="contact-title">Contact us</h2>
          <p className="muted">Questions about an order or a book? Send us a message.</p>
          <ContactForm />
        </section>
      </div>
    </>
  )
}

function ContactForm() {
  const [form, setForm] = useState(EMPTY_FORM)
  const [fieldErrors, setFieldErrors] = useState({})
  const [error, setError] = useState('')
  const [sent, setSent] = useState(false)
  const [busy, setBusy] = useState(false)

  function update(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  async function submit(event) {
    event.preventDefault()
    setBusy(true)
    setError('')
    setFieldErrors({})
    try {
      await api.post('/api/contact', form)
      setForm(EMPTY_FORM)
      setSent(true)
    } catch (problem) {
      const fields = problem.fieldErrors ?? {}
      setFieldErrors(fields)
      setError(Object.keys(fields).length > 0 ? '' : problem.message)
    } finally {
      setBusy(false)
    }
  }

  if (sent) {
    return (
      <p className="form-ok" role="status">
        Thank you! Your message was sent and we will get back to you by email.{' '}
        <button type="button" className="text-link link-reset" onClick={() => setSent(false)}>
          Send another message
        </button>
      </p>
    )
  }

  return (
    <form className="form contact-form" onSubmit={submit} noValidate>
      <FormField label="Name" name="name" value={form.name} onChange={update} error={fieldErrors.name}
        autoComplete="name" />
      <FormField label="Email" name="email" type="email" value={form.email} onChange={update}
        error={fieldErrors.email} autoComplete="email" />
      <FormField label="Message" name="message" multiline rows="5" maxLength="2000" value={form.message}
        onChange={update} error={fieldErrors.message} />
      {error && <p className="form-error" role="alert">{error}</p>}
      <button type="submit" className="btn" disabled={busy}>{busy ? 'Sending...' : 'Send message'}</button>
    </form>
  )
}
