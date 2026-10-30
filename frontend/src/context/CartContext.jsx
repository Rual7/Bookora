import { createContext, useContext, useEffect, useState } from 'react'
import { api } from '../api/client.js'
import { useAuth } from './AuthContext.jsx'

const CartContext = createContext(null)

const GUEST_KEY = 'bookora-guest-cart'
const EMPTY_CART = { items: [], itemCount: 0, subtotal: 0, shippingCost: 0, total: 0, currency: 'EUR' }

function readGuestItems() {
  try {
    return JSON.parse(localStorage.getItem(GUEST_KEY)) ?? []
  } catch {
    return []
  }
}

function saveGuestItems(items) {
  try {
    localStorage.setItem(GUEST_KEY, JSON.stringify(items))
  } catch {
    // Storage can be blocked; the cart then lasts until the page is closed.
  }
}

/**
 * Signed-in customers have their cart on the server. Visitors keep only product ids and quantities
 * in the browser; the server prices them (/api/cart/preview), and they are moved into the
 * customer's real cart when the visitor signs in.
 */
export function CartProvider({ children }) {
  const { user } = useAuth()
  const [cart, setCart] = useState(EMPTY_CART)

  useEffect(() => {
    if (user === undefined) return
    if (user) {
      moveGuestItemsToAccount().then(() => api.get('/api/cart')).then(setCart).catch(() => setCart(EMPTY_CART))
    } else {
      priceGuestItems(readGuestItems()).then(setCart).catch(() => setCart(EMPTY_CART))
    }
  }, [user])

  async function moveGuestItemsToAccount() {
    // Emptied first, so a second run of this effect (React StrictMode, a second tab) cannot add the items twice.
    const items = readGuestItems()
    saveGuestItems([])
    for (const item of items) {
      try {
        await api.post('/api/cart/items', item)
      } catch {
        // Skip what can no longer be bought (out of stock, removed).
      }
    }
  }

  // Asks the server for current prices and keeps only what can really be bought.
  async function priceGuestItems(items) {
    if (items.length === 0) return EMPTY_CART
    const priced = await api.post('/api/cart/preview', { items })
    saveGuestItems(priced.items.map(({ productId, quantity }) => ({ productId, quantity })))
    return priced
  }

  async function addItem(productId, quantity = 1) {
    if (user) {
      setCart(await api.post('/api/cart/items', { productId, quantity }))
    } else {
      setCart(await priceGuestItems([...readGuestItems(), { productId, quantity }]))
    }
  }

  async function changeQuantity(productId, quantity) {
    if (user) {
      setCart(await api.put(`/api/cart/items/${productId}`, { quantity }))
    } else {
      const others = readGuestItems().filter((item) => item.productId !== productId)
      setCart(await priceGuestItems([...others, { productId, quantity }]))
    }
  }

  async function removeItem(productId) {
    if (user) {
      setCart(await api.delete(`/api/cart/items/${productId}`))
    } else {
      setCart(await priceGuestItems(readGuestItems().filter((item) => item.productId !== productId)))
    }
  }

  // The server empties the cart when an order is placed; this loads that state.
  async function reloadCart() {
    setCart(await api.get('/api/cart'))
  }

  return (
    <CartContext value={{ cart, itemCount: cart.itemCount, addItem, changeQuantity, removeItem, reloadCart }}>
      {children}
    </CartContext>
  )
}

export function useCart() {
  return useContext(CartContext)
}
