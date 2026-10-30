import en from './en.js'

/**
 * Looks up a text of the shop by key. `{name}` is filled from `vars`; when `vars.count` is given,
 * the `_one` or `_other` version of the key is used. A key that is missing shows `fallback`, or the key itself.
 */
export function t(key, vars = {}, fallback) {
  let text
  if (vars.count !== undefined) text = en[`${key}_${vars.count === 1 ? 'one' : 'other'}`]
  text ??= en[key] ?? fallback ?? key
  return text.replace(/\{(\w+)\}/g, (_, name) => vars[name] ?? '')
}
