import { useState } from 'react'
import FormField from './FormField.jsx'
import { t } from '../i18n/t.js'
import './AddressForm.css'

const EMPTY_ADDRESS = {
  label: '',
  recipientName: '',
  phone: '',
  country: 'Romania',
  city: '',
  countyRegion: '',
  postalCode: '',
  streetAddress: '',
  buildingDetails: '',
  defaultAddress: false,
}

/** Create or edit an address. `onSave` gets the form and may throw an ApiError; `onCancel` is optional. */
export default function AddressForm({ address, onSave, onCancel }) {
  const [form, setForm] = useState({ ...EMPTY_ADDRESS, ...withoutNulls(address) })
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})
  const [busy, setBusy] = useState(false)

  function update(event) {
    const { name, type, checked, value } = event.target
    setForm({ ...form, [name]: type === 'checkbox' ? checked : value })
  }

  async function submit(event) {
    event.preventDefault()
    setBusy(true)
    setError('')
    setFieldErrors({})
    try {
      await onSave(form)
    } catch (problem) {
      const fields = problem.fieldErrors ?? {}
      setFieldErrors(fields)
      setError(Object.keys(fields).length > 0 ? '' : problem.message)
      setBusy(false)
    }
  }

  const field = (name, label, props = {}) => (
    <FormField label={label} name={name} value={form[name]} onChange={update} error={fieldErrors[name]} {...props} />
  )

  return (
    <form className="form address-form" onSubmit={submit} noValidate>
      {field('recipientName', t('address.fullName'), { autoComplete: 'name', required: true })}
      {field('phone', t('address.phone'), { type: 'tel', autoComplete: 'tel', required: true })}
      {field('streetAddress', t('address.street'), { autoComplete: 'address-line1', required: true })}
      {field('buildingDetails', t('address.building'), { autoComplete: 'address-line2' })}
      <div className="address-form-row">
        {field('city', t('address.city'), { autoComplete: 'address-level2', required: true })}
        {field('countyRegion', t('address.county'), { autoComplete: 'address-level1' })}
      </div>
      <div className="address-form-row">
        {field('postalCode', t('address.postalCode'), { autoComplete: 'postal-code', required: true })}
        {field('country', t('address.country'), { autoComplete: 'country-name', required: true })}
      </div>
      {field('label', t('address.label'), { placeholder: t('address.labelPlaceholder') })}

      <label className="check">
        <input type="checkbox" name="defaultAddress" checked={form.defaultAddress} onChange={update} />
        {t('address.makeDefault')}
      </label>

      {error && <p className="form-error" role="alert">{error}</p>}

      <div className="address-form-actions">
        <button type="submit" className="btn" disabled={busy}>{busy ? t('address.saving') : t('address.save')}</button>
        {onCancel && <button type="button" className="link-btn" onClick={onCancel}>{t('address.cancel')}</button>}
      </div>
    </form>
  )
}

function withoutNulls(address) {
  if (!address) return {}
  return Object.fromEntries(Object.entries(address).filter(([, value]) => value !== null))
}
