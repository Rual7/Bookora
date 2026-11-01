import { useState } from 'react'
import { api } from '../api/client.js'

/** Asks the server for the payment page of an order and sends the browser there. Throws if it cannot be opened. */
export async function goToPayment(orderNumber) {
  const { url } = await api.post(`/api/orders/${orderNumber}/payment`)
  window.location.assign(url)
}

/** The "Pay now" button of an order: tracks whether it is working and what went wrong. */
export function usePayment() {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  async function pay(orderNumber) {
    setBusy(true)
    setError('')
    try {
      await goToPayment(orderNumber)
    } catch (problem) {
      setError(problem.message)
      setBusy(false)
    }
  }

  return { pay, busy, error }
}
