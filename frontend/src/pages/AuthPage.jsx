import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import FormField from '../components/FormField.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import './AuthPage.css'

const EMPTY_FORM = { firstName: '', lastName: '', email: '', password: '' }

/** One page for both forms: `mode` is "login" or "register". */
export default function AuthPage({ mode }) {
  const isRegister = mode === 'register'
  const { user, login, register } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const goBackTo = location.state?.from ?? '/'

  const [form, setForm] = useState(EMPTY_FORM)
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})
  const [busy, setBusy] = useState(false)

  if (user) return <Navigate to={goBackTo} replace />

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
        await register(form)
      } else {
        await login(form.email, form.password)
      }
      navigate(goBackTo, { replace: true })
    } catch (problem) {
      const fields = problem.fieldErrors ?? {}
      setFieldErrors(fields)
      // Field messages already explain what is wrong, so the general message would only repeat it.
      setError(Object.keys(fields).length > 0 ? '' : problem.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="container auth-page">
      <section className="auth-card" aria-labelledby="auth-title">
        <h1 id="auth-title">{isRegister ? 'Create your account' : 'Sign in'}</h1>

        <form className="form" onSubmit={submit} noValidate>
          {isRegister && (
            <>
              <FormField label="First name" name="firstName" value={form.firstName} onChange={update}
                error={fieldErrors.firstName} autoComplete="given-name" required />
              <FormField label="Last name" name="lastName" value={form.lastName} onChange={update}
                error={fieldErrors.lastName} autoComplete="family-name" required />
            </>
          )}
          <FormField label="Email" name="email" type="email" value={form.email} onChange={update}
            error={fieldErrors.email} autoComplete="email" required />
          <FormField label="Password" name="password" type="password" value={form.password} onChange={update}
            error={fieldErrors.password} autoComplete={isRegister ? 'new-password' : 'current-password'}
            hint={isRegister ? 'At least 8 characters' : undefined} required />

          {error && <p className="form-error" role="alert">{error}</p>}

          <button type="submit" className="btn" disabled={busy}>
            {busy ? 'Please wait...' : isRegister ? 'Create account' : 'Sign in'}
          </button>
        </form>

        <p className="auth-switch muted">
          {isRegister ? 'Already have an account? ' : 'New to Bookora? '}
          <Link to={isRegister ? '/login' : '/register'} state={location.state} className="text-link">
            {isRegister ? 'Sign in' : 'Create an account'}
          </Link>
        </p>
      </section>
    </div>
  )
}
