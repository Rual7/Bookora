import { useEffect } from 'react'
import { useLocation } from 'react-router-dom'
import { t } from '../i18n/t.js'

const MAX_DESCRIPTION = 155

/** Cuts a text at a word boundary so it fits in a search result. */
export function shorten(text, max = MAX_DESCRIPTION) {
  const clean = text.replace(/\s+/g, ' ').trim()
  if (clean.length <= max) return clean
  return `${clean.slice(0, max - 1).replace(/\s+\S*$/, '')}…`
}

/** Sets an attribute on a head element that is created when missing and removed when `value` is empty. */
function setHeadTag(tag, key, keyValue, attribute, value) {
  let element = document.head.querySelector(`${tag}[${key}="${keyValue}"]`)
  if (!value) {
    element?.remove()
    return
  }
  if (!element) {
    element = document.createElement(tag)
    element.setAttribute(key, keyValue)
    document.head.append(element)
  }
  element.setAttribute(attribute, value)
}

/**
 * Sets the tab title, the description search engines show, and the canonical address for the page.
 * Pages that belong to one customer (cart, orders, admin) pass `noindex` so they stay out of search results.
 * The home page passes `home` to use the site title as it is, without the " | Bookora" ending.
 */
export function usePageMeta({ title, description, noindex = false, home = false }) {
  const { pathname } = useLocation()

  useEffect(() => {
    document.title = home ? title : `${title} | Bookora`
    setHeadTag('meta', 'name', 'description', 'content', description ?? t('meta.siteDescription'))
    setHeadTag('meta', 'name', 'robots', 'content', noindex ? 'noindex, follow' : '')
    setHeadTag('link', 'rel', 'canonical', 'href', noindex ? '' : `${window.location.origin}${pathname}`)
  }, [title, description, noindex, home, pathname])
}
