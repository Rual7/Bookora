import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import Captcha from '../components/Captcha.jsx'
import FormField from '../components/FormField.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { t } from '../i18n/t.js'
import { usePageMeta } from '../hooks/usePageMeta.js'
import { TURNSTILE_SITE_KEY } from '../utils/captcha.js'
import './AuthPage.css'

const EMPTY_FORM = { firstName: '', lastName: '', email: '', password: '' }
const CAPTCHA_ON = Boolean(TURNSTILE_SITE_KEY)

/** One page for both forms: `mode` is "login" or "register". */
export default function AuthPage({ mode }) {
  usePageMeta({ title: mode === 'register' ? t('auth.registerTitle') : t('nav.signIn'), noindex: true })

  const isRegister = mode === 'register'
  const { user, login, register } = useAuth()
  const navigate = useNavigate()

  const [form, setForm] = useState(EMPTY_FORM)
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})
  const [busy, setBusy] = useState(false)
  const [captchaToken, setCaptchaToken] = useState('')
  const [captchaRound, setCaptchaRound] = useState(0)

  if (user) return <Navigate to="/" replace />

  function update(event) {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  async function submit(event) {
    event.preventDefault()
    setBusy(true)
    setError('')
    setFieldErrors({})
    try {
      if (isRegister) {
        await register(form, captchaToken)
      } else {
        await login(form.email, form.password, captchaToken)
      }
      navigate('/', { replace: true })
    } catch (problem) {
      const fields = problem.fieldErrors ?? {}
      setFieldErrors(fields)
      // Field messages already explain what is wrong, so the general message would only repeat it.
      setError(Object.keys(fields).length > 0 ? '' : problem.message)
      setCaptchaRound(captchaRound + 1)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="container auth-page">
      <section className="auth-card" aria-labelledby="auth-title">
        <h1 id="auth-title">{isRegister ? t('auth.registerTitle') : t('nav.signIn')}</h1>

        <form className="form" onSubmit={submit} noValidate>
          {isRegister && (
            <>
              <FormField label={t('field.firstName')} name="firstName" value={form.firstName} onChange={update}
                error={fieldErrors.firstName} autoComplete="given-name" required />
              <FormField label={t('field.lastName')} name="lastName" value={form.lastName} onChange={update}
                error={fieldErrors.lastName} autoComplete="family-name" required />
            </>
          )}
          <FormField label={t('field.email')} name="email" type="email" value={form.email} onChange={update}
            error={fieldErrors.email} autoComplete="email" required />
          <FormField label={t('field.password')} name="password" type="password" value={form.password} onChange={update}
            error={fieldErrors.password} autoComplete={isRegister ? 'new-password' : 'current-password'}
            hint={isRegister ? t('auth.passwordHint') : undefined} required />

          <Captcha onToken={setCaptchaToken} resetKey={captchaRound} />

          {error && <p className="form-error" role="alert">{error}</p>}

          <button type="submit" className="btn" disabled={busy || (CAPTCHA_ON && !captchaToken)}>
            {busy ? t('auth.wait') : isRegister ? t('auth.create') : t('nav.signIn')}
          </button>
        </form>

        <p className="auth-switch muted">
          {isRegister ? t('auth.haveAccount') : t('auth.newHere')}{' '}
          <Link to={isRegister ? '/login' : '/register'} className="text-link">
            {isRegister ? t('nav.signIn') : t('auth.createLink')}
          </Link>
        </p>
      </section>
    </div>
  )
}
