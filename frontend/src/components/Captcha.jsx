import { useEffect, useRef } from 'react'
import { TURNSTILE_SITE_KEY } from '../utils/captcha.js'
import './Captcha.css'

const SCRIPT_URL = 'https://challenges.cloudflare.com/turnstile/v0/api.js?render=explicit'

let scriptLoaded

/** Loads Cloudflare's script the first time a form needs it. */
function loadScript() {
  scriptLoaded ??= new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = SCRIPT_URL
    script.async = true
    script.onload = () => resolve(window.turnstile)
    script.onerror = () => {
      scriptLoaded = undefined
      reject(new Error('Turnstile could not be loaded'))
    }
    document.head.append(script)
  })
  return scriptLoaded
}

/**
 * The "I am human" check. Calls `onToken` with a one-use token, or with '' when it expires.
 * Change `resetKey` after a submit to get a fresh check, because a token cannot be used twice.
 */
export default function Captcha({ onToken, resetKey = 0 }) {
  const box = useRef(null)
  const onTokenRef = useRef(onToken)

  useEffect(() => {
    onTokenRef.current = onToken
  })

  useEffect(() => {
    if (!TURNSTILE_SITE_KEY) return undefined
    let widgetId
    let removed = false
    loadScript()
      .then((turnstile) => {
        if (removed) return
        onTokenRef.current('')
        widgetId = turnstile.render(box.current, {
          sitekey: TURNSTILE_SITE_KEY,
          size: 'flexible',
          theme: document.documentElement.dataset.theme ?? 'auto',
          callback: (token) => onTokenRef.current(token),
          'expired-callback': () => onTokenRef.current(''),
          'error-callback': () => onTokenRef.current(''),
        })
      })
      .catch(() => onTokenRef.current(''))
    return () => {
      removed = true
      if (widgetId !== undefined) window.turnstile?.remove(widgetId)
    }
  }, [resetKey])

  if (!TURNSTILE_SITE_KEY) return null
  return <div ref={box} className="captcha" />
}
