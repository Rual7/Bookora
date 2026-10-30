import { createContext, useContext, useEffect, useState } from 'react'
import { api } from '../api/client.js'
import { useAuth } from './AuthContext.jsx'

const WishlistContext = createContext(null)

const GUEST_KEY = 'bookora-guest-wishlist'

function readGuestIds() {
  try {
    return JSON.parse(localStorage.getItem(GUEST_KEY)) ?? []
  } catch {
    return []
  }
}

function saveGuestIds(ids) {
  try {
    localStorage.setItem(GUEST_KEY, JSON.stringify(ids))
  } catch {
    // Storage can be blocked; the wishlist then lasts until the page is closed.
  }
}

// A signed-in customer first gets what they saved as a visitor moved into their account.
async function loadBooks(user) {
  if (user) {
    // Emptied first, so a second run at the same time does not repeat the work.
    const ids = readGuestIds()
    saveGuestIds([])
    for (const id of ids) {
      await api.put(`/api/wishlist/${id}`).catch(() => {})
    }
    return api.get('/api/wishlist')
  }
  const ids = readGuestIds()
  return ids.length === 0 ? [] : api.get(`/api/books/by-ids?ids=${ids.join(',')}`)
}

/** Customers keep the wishlist on the server; visitors keep book ids in the browser until they sign in. */
export function WishlistProvider({ children }) {
  const { user } = useAuth()
  const [books, setBooks] = useState([])

  useEffect(() => {
    if (user === undefined) return
    loadBooks(user).then(setBooks).catch(() => setBooks([]))
  }, [user])

  function has(bookId) {
    return books.some((book) => book.id === bookId)
  }

  async function toggle(book) {
    const wished = has(book.id)
    if (user) {
      await (wished ? api.delete(`/api/wishlist/${book.id}`) : api.put(`/api/wishlist/${book.id}`))
    } else {
      const ids = readGuestIds().filter((id) => id !== book.id)
      saveGuestIds(wished ? ids : [...ids, book.id])
    }
    setBooks(await loadBooks(user))
  }

  return <WishlistContext value={{ books, has, toggle }}>{children}</WishlistContext>
}

export function useWishlist() {
  return useContext(WishlistContext)
}
