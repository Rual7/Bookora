export const FORMAT_NAMES = {
  PAPERBACK: 'Paperback',
  HARDCOVER: 'Hardcover',
  PDF: 'E-book (PDF)',
  EPUB: 'E-book (EPUB)',
}

export function formatPrice(value, currency = 'RON') {
  return `${Number(value).toFixed(2)} ${currency}`
}

export function capitalize(text) {
  return text.charAt(0).toUpperCase() + text.slice(1)
}
