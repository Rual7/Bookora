import { useState } from 'react'
import { api, formErrors } from '../api/client.js'
import Captcha from '../components/Captcha.jsx'
import FormField from '../components/FormField.jsx'
import { LogoMark } from '../components/Logo.jsx'
import { t } from '../i18n/t.js'
import { usePageMeta } from '../hooks/usePageMeta.js'
import { TURNSTILE_SITE_KEY, captchaHeaders } from '../utils/captcha.js'
import './AboutPage.css'

const EMPTY_FORM = { name: '', email: '', message: '' }

export default function AboutPage() {
  usePageMeta({ title: t('meta.about'), description: t('meta.aboutDescription') })

  return (
    <>
      <section className="about-hero" aria-labelledby="about-title">
        <div className="container about-hero-inner">
          <div>
            <h1 id="about-title">{t('hero.eyebrow')}</h1>
            <p className="about-lead">{t('about.lead')}</p>
          </div>
          <div className="about-brand" aria-hidden="true">
            <LogoMark className="about-mark" />
            <span className="about-wordmark">Bookora</span>
          </div>
        </div>
      </section>

      <div className="container about-body">
        <section aria-labelledby="what-we-do">
          <h2 id="what-we-do">{t('about.whatTitle')}</h2>
          <p>{t('about.what1')}</p>
          <p>{t('about.what2')}</p>
        </section>

        <section className="contact-section" aria-labelledby="contact-title">
          <h2 id="contact-title">{t('about.contactTitle')}</h2>
          <p className="muted">{t('about.contactIntro')}</p>
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
  const [captchaToken, setCaptchaToken] = useState('')
  const [captchaRound, setCaptchaRound] = useState(0)

  function update(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  async function submit(event) {
    event.preventDefault()
    setBusy(true)
    setError('')
    setFieldErrors({})
    try {
      await api.post('/api/contact', form, captchaHeaders(captchaToken))
      setForm(EMPTY_FORM)
      setSent(true)
    } catch (problem) {
      const { fields, message } = formErrors(problem)
      setFieldErrors(fields)
      setError(message)
    } finally {
      setCaptchaRound(captchaRound + 1)
      setBusy(false)
    }
  }

  if (sent) {
    return (
      <p className="form-ok" role="status">
        {t('contact.thanks')}{' '}
        <button type="button" className="text-link link-reset" onClick={() => setSent(false)}>
          {t('contact.another')}
        </button>
      </p>
    )
  }

  return (
    <form className="form contact-form" onSubmit={submit} noValidate>
      <FormField label={t('contact.name')} name="name" value={form.name} onChange={update} error={fieldErrors.name}
        autoComplete="name" />
      <FormField label={t('field.email')} name="email" type="email" value={form.email} onChange={update}
        error={fieldErrors.email} autoComplete="email" />
      <FormField label={t('contact.message')} name="message" multiline rows="5" maxLength="2000" value={form.message}
        onChange={update} error={fieldErrors.message} />
      <Captcha onToken={setCaptchaToken} resetKey={captchaRound} />
      {error && <p className="form-error" role="alert">{error}</p>}
      <button type="submit" className="btn" disabled={busy || (Boolean(TURNSTILE_SITE_KEY) && !captchaToken)}>{busy ? t('contact.sending') : t('contact.send')}</button>
    </form>
  )
}
