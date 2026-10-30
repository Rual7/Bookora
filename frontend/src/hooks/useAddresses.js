import { useCallback, useEffect, useState } from 'react'
import { api } from '../api/client.js'

/** The signed-in customer's saved addresses. `addresses` is null while they load. */
export function useAddresses() {
  const [addresses, setAddresses] = useState(null)
  const [error, setError] = useState('')

  const reload = useCallback(
    () => api.get('/api/addresses').then(setAddresses).catch((problem) => setError(problem.message)),
    [],
  )

  useEffect(() => {
    reload()
  }, [reload])

  // Creates the address, or updates it when `id` is given. Returns the saved address.
  async function save(form, id) {
    const saved = id ? await api.put(`/api/addresses/${id}`, form) : await api.post('/api/addresses', form)
    await reload()
    return saved
  }

  async function remove(id) {
    await api.delete(`/api/addresses/${id}`)
    await reload()
  }

  return { addresses, error, save, remove }
}
