/** The Cloudflare Turnstile site key; without it (local development) the widget is not shown and the server skips the check. */
export const TURNSTILE_SITE_KEY = import.meta.env.VITE_TURNSTILE_SITE_KEY

export function captchaHeaders(token) {
  return token ? { 'X-Captcha-Token': token } : undefined
}
