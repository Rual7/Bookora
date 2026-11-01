import { useCallback, useEffect, useState } from 'react'
import { api } from '../api/client.js'

/** One of the customer's orders, with `reload` to fetch it again (used while a payment is being confirmed). */
export function useOrder(orderNumber) {
  const [result, setResult] = useState({ number: null, order: null, error: null })

  const reload = useCallback(
    () =>
      api
        .get(`/api/orders/${orderNumber}`)
        .then((order) => setResult({ number: orderNumber, order, error: null }))
        .catch((error) => setResult({ number: orderNumber, order: null, error })),
    [orderNumber],
  )

  useEffect(() => {
    reload()
  }, [reload])

  const settled = result.number === orderNumber
  return {
    order: settled ? result.order : null,
    error: settled ? result.error : null,
    loading: !settled,
    reload,
  }
}
