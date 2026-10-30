export function isDigital(format) {
  return format === 'PDF' || format === 'EPUB'
}

export function formatDate(isoDate) {
  return new Date(isoDate).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' })
}

// The shop sells in EUR. Old orders keep the currency they were placed in, so it can be passed in.
export function formatPrice(value, currency = 'EUR') {
  return `${Number(value).toFixed(2)} ${currency}`
}

export function capitalize(text) {
  return text.charAt(0).toUpperCase() + text.slice(1)
}
