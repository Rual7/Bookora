import { useState } from 'react'
import AccountNav from '../components/AccountNav.jsx'
import AddressForm from '../components/AddressForm.jsx'
import AddressText from '../components/AddressText.jsx'
import { t } from '../i18n/t.js'
import { useAddresses } from '../hooks/useAddresses.js'
import './AddressesPage.css'

export default function AddressesPage() {
  const { addresses, error, save, remove } = useAddresses()
  // null = no form, 'new' = adding, otherwise the id of the address being edited
  const [editing, setEditing] = useState(null)
  const [problem, setProblem] = useState('')

  async function handleSave(form) {
    await save(form, editing === 'new' ? undefined : editing)
    setEditing(null)
  }

  async function handleRemove(id) {
    setProblem('')
    try {
      await remove(id)
    } catch (failure) {
      setProblem(failure.message)
    }
  }

  return (
    <div className="container page">
      <h1>{t('nav.account')}</h1>
      <AccountNav />

      <h2 className="address-title">{t('addresses.title')}</h2>
      {(error || problem) && <p className="form-error" role="alert">{error || problem}</p>}
      {addresses === null && !error && <p className="muted">{t('common.loading')}</p>}
      {addresses?.length === 0 && editing !== 'new' && <p className="muted">{t('addresses.none')}</p>}

      <ul className="address-list">
        {addresses?.map((address) => (
          <li key={address.id} className="address-card">
            {editing === address.id ? (
              <AddressForm address={address} onSave={handleSave} onCancel={() => setEditing(null)} />
            ) : (
              <>
                <p className="address-card-head">
                  <strong>{address.label || t('addresses.fallbackLabel')}</strong>
                  {address.defaultAddress && <span className="address-default">{t('addresses.default')}</span>}
                </p>
                <AddressText address={address} />
                <div className="address-card-actions">
                  <button type="button" className="link-btn" onClick={() => setEditing(address.id)}>{t('addresses.edit')}</button>
                  <button type="button" className="link-btn" onClick={() => handleRemove(address.id)}>{t('addresses.delete')}</button>
                </div>
              </>
            )}
          </li>
        ))}
      </ul>

      {editing === 'new' ? (
        <section className="address-card" aria-label={t('checkout.newAddress')}>
          <AddressForm onSave={handleSave} onCancel={() => setEditing(null)} />
        </section>
      ) : (
        <button type="button" className="btn" onClick={() => setEditing('new')}>{t('addresses.add')}</button>
      )}
    </div>
  )
}
