import { createContext, useContext, useState } from 'react'

const PanelContext = createContext(null)

/** Which side panel is open: 'cart', 'wishlist' or null. */
export function PanelProvider({ children }) {
  const [panel, setPanel] = useState(null)
  return (
    <PanelContext value={{ panel, openPanel: setPanel, closePanel: () => setPanel(null) }}>
      {children}
    </PanelContext>
  )
}

export function usePanel() {
  return useContext(PanelContext)
}
