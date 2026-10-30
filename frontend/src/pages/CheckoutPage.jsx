import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import AddressForm from '../components/AddressForm.jsx'
import AddressText from '../components/AddressText.jsx'
import { useCart } from '../context/CartContext.jsx'
import { t } from '../i18n/t.js'
import { useAddresses } from '../hooks/useAddresses.js'
import { api } from '../api/client.js'
import { formatPrice, isDigital } from '../utils/format.js'
import './CheckoutPage.css'

export default function CheckoutPage() {
  const navigate = useNavigate()
  const { cart, reloadCart } = useCart()
  const { addresses, error: loadError, save } = useAddresses()
  const [pickedId, setPickedId] = useState(null)
  const [adding, setAdding] = useState(false)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  if (cart.items.length === 0) {
    return (
      <div className="container page">
        <h1>{t('checkout.title')}</h1>
        <p className="muted">{t('cart.empty')}</p>
        <p><Link to="/books" className="btn">{t('common.browse')}</Link></p>
      </div>
    )
  }

  const needsShipping = cart.items.some((item) => !isDigital(item.format))
  const selectedId = pickedId ?? addresses?.find((address) => address.defaultAddress)?.id ?? null
  const showForm = adding || addresses?.length === 0

  async function saveNewAddress(form) {
    const saved = await save(form)
    setPickedId(saved.id)
    setAdding(false)
  }

  async function placeOrder() {
    setBusy(true)
    setError('')
    try {
      const order = await api.post('/api/checkout', { addressId: needsShipping ? selectedId : null })
      await reloadCart()
      navigate(`/orders/${order.orderNumber}`, { state: { placed: true }, replace: true })
    } catch (problem) {
      setError(problem.message)
      setBusy(false)
      // The stock may have changed, so show the cart as the server sees it now.
      reloadCart().catch(() => {})
    }
  }

  return (
    <div className="container page">
      <h1>{t('checkout.title')}</h1>

      <div className="cart-layout">
        <div>
          <section aria-labelledby="delivery-heading">
            <h2 id="delivery-heading" className="checkout-heading">{t('cart.delivery')}</h2>

            {!needsShipping && <p className="muted">{t('checkout.noShipping')}</p>}
            {needsShipping && loadError && <p className="form-error" role="alert">{loadError}</p>}
            {needsShipping && addresses === null && !loadError && <p className="muted">{t('common.loading')}</p>}

            {needsShipping && addresses?.length > 0 && (
              <fieldset className="address-choices">
                <legend className="visually-hidden">{t('checkout.chooseAddress')}</legend>
                {addresses.map((address) => (
                  <label key={address.id} className="address-choice">
                    <input type="radio" name="address" value={address.id} checked={selectedId === address.id}
                      onChange={() => setPickedId(address.id)} />
                    <AddressText address={address} />
                  </label>
                ))}
              </fieldset>
            )}

            {needsShipping && addresses && showForm && (
              <div className="checkout-form">
                <h3>{t('checkout.newAddress')}</h3>
                <AddressForm onSave={saveNewAddress} onCancel={addresses.length > 0 ? () => setAdding(false) : undefined} />
              </div>
            )}
            {needsShipping && addresses?.length > 0 && !showForm && (
              <button type="button" className="link-btn" onClick={() => setAdding(true)}>{t('checkout.different')}</button>
            )}
          </section>
        </div>

        <aside className="cart-summary" aria-label={t('cart.summary')}>
          <h2>{t('checkout.summary')}</h2>
          <ul className="checkout-lines">
            {cart.items.map((item) => (
              <li key={item.productId}>
                <span>{item.quantity} &times; {item.title} <span className="muted">({t(`format.${item.format}`)})</span></span>
                <span>{formatPrice(item.lineTotal)}</span>
              </li>
            ))}
          </ul>
          <p className="summary-row">
            <span>{t('cart.delivery')}</span>
            <span>
              {!needsShipping ? t('shipping.notNeeded') : Number(cart.shippingCost) === 0 ? t('shipping.free') : formatPrice(cart.shippingCost)}
            </span>
          </p>
          <p className="summary-row summary-total">
            <span>{t('cart.total')}</span>
            <strong>{formatPrice(cart.total)}</strong>
          </p>

          {error && <p className="form-error" role="alert">{error}</p>}
          <button type="button" className="btn btn-block" onClick={placeOrder}
            disabled={busy || (needsShipping && selectedId === null)}>
            {busy ? t('checkout.placing') : t('checkout.place')}
          </button>
        </aside>
      </div>
    </div>
  )
}
