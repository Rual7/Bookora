import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import FileUpload from '../../components/FileUpload.jsx'
import FormField from '../../components/FormField.jsx'
import { api } from '../../api/client.js'
import { t } from '../../i18n/t.js'
import { useFetch } from '../../hooks/useFetch.js'
import { isDigital } from '../../utils/format.js'
import './Admin.css'
import './AdminBookPage.css'

const FORMATS = ['PAPERBACK', 'HARDCOVER', 'PDF', 'EPUB']
const NO_ERRORS = { products: {} }

export default function AdminBookPage() {
  const { id } = useParams()
  const { data: categories } = useFetch('/api/categories')
  const { data: book, error, loading, reload } = useFetch(id ? `/api/admin/books/${id}` : null)

  if (error) return <p className="form-error" role="alert">{error.message}</p>
  if (!categories || loading) return <p className="muted">{t('admin.loading')}</p>
  return <BookEditor key={book?.id ?? 'new'} book={book} categories={categories} onChanged={reload} />
}

function BookEditor({ book, categories, onChanged }) {
  const navigate = useNavigate()
  const [form, setForm] = useState(() => toForm(book))
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState(NO_ERRORS)
  const [saved, setSaved] = useState(false)
  const [busy, setBusy] = useState(false)
  const [imageProblem, setImageProblem] = useState('')

  function update(event) {
    const { name, type, checked, value } = event.target
    setForm({ ...form, [name]: type === 'checkbox' ? checked : value })
  }

  function updateProduct(format, name, value) {
    setForm({ ...form, products: { ...form.products, [format]: { ...form.products[format], [name]: value } } })
  }

  function toggleCategory(id) {
    const chosen = form.categoryIds.includes(id) ? form.categoryIds.filter((item) => item !== id) : [...form.categoryIds, id]
    setForm({ ...form, categoryIds: chosen })
  }

  async function save(event) {
    event.preventDefault()
    setBusy(true)
    setError('')
    setFieldErrors(NO_ERRORS)
    setSaved(false)
    try {
      const request = toRequest(form)
      const result = book ? await api.put(`/api/admin/books/${book.id}`, request) : await api.post('/api/admin/books', request)
      if (book) {
        onChanged()
        setSaved(true)
      } else {
        navigate(`/admin/books/${result.id}`, { replace: true })
      }
    } catch (problem) {
      const fields = readFieldErrors(problem.fieldErrors ?? {}, soldFormats(form))
      setFieldErrors(fields)
      setError(hasFieldErrors(fields) ? '' : problem.message)
    } finally {
      setBusy(false)
    }
  }

  async function upload(path, field, files) {
    const data = new FormData()
    files.forEach((file) => data.append(field, file))
    await api.post(path, data)
    onChanged()
  }

  async function changeImages(request) {
    setImageProblem('')
    try {
      await request()
      onChanged()
    } catch (problem) {
      setImageProblem(problem.message)
    }
  }

  return (
    <div className="admin-book">
      <p><Link to="/admin/books" className="text-link">&larr; {t('admin.allBooks')}</Link></p>
      <h2>{book ? t('admin.editBook') : t('admin.newBook')}</h2>

      <form className="form admin-form" onSubmit={save} noValidate>
        <FormField label={t('admin.field.title')} name="title" value={form.title} onChange={update}
          error={fieldErrors.title} required />
        <FormField label={t('admin.field.authors')} name="authors" value={form.authors} onChange={update}
          hint={t('admin.field.authorsHint')} error={fieldErrors.authors} required />
        <FormField label={t('admin.field.description')} name="description" value={form.description} onChange={update}
          error={fieldErrors.description} multiline rows={6} />
        <div className="admin-form-row">
          <FormField label={t('admin.field.publisher')} name="publisher" value={form.publisher} onChange={update}
            error={fieldErrors.publisher} />
          <FormField label={t('admin.field.publishedOn')} name="publishedOn" type="date" value={form.publishedOn}
            onChange={update} error={fieldErrors.publishedOn} />
        </div>
        <div className="admin-form-row">
          <FormField label={t('admin.field.language')} name="languageCode" value={form.languageCode} onChange={update}
            error={fieldErrors.languageCode} />
          <FormField label={t('admin.field.pages')} name="pageCount" type="number" min="1" value={form.pageCount}
            onChange={update} error={fieldErrors.pageCount} />
        </div>

        <fieldset className="admin-fieldset">
          <legend>{t('admin.field.categories')}</legend>
          <div className="admin-checks">
            {categories.map((category) => (
              <label key={category.id} className="check">
                <input type="checkbox" checked={form.categoryIds.includes(category.id)}
                  onChange={() => toggleCategory(category.id)} />
                {t(`category.${category.slug}`, {}, category.name)}
              </label>
            ))}
          </div>
          {fieldErrors.categoryIds && <p className="field-error">{fieldErrors.categoryIds}</p>}
        </fieldset>

        <fieldset className="admin-fieldset">
          <legend>{t('admin.formats')}</legend>
          <p className="field-hint">{t('admin.formatsHint')}</p>
          {fieldErrors.formats && <p className="field-error">{fieldErrors.formats}</p>}
          {FORMATS.map((format) => (
            <FormatRow key={format} format={format} product={form.products[format]} errors={fieldErrors.products[format]}
              onChange={(name, value) => updateProduct(format, name, value)} />
          ))}
        </fieldset>

        <label className="check">
          <input type="checkbox" name="featured" checked={form.featured} onChange={update} />
          {t('admin.field.featured')}
        </label>
        <label className="check">
          <input type="checkbox" name="active" checked={form.active} onChange={update} />
          {t('admin.field.active')}
        </label>

        {error && <p className="form-error" role="alert">{error}</p>}
        {saved && <p className="form-ok" role="status">{t('admin.saved')}</p>}
        <button type="submit" className="btn" disabled={busy}>{busy ? t('admin.saving') : t('admin.save')}</button>
      </form>

      {!book && <p className="muted admin-save-first">{t('admin.saveFirst')}</p>}

      {book && (
        <section className="admin-section" aria-labelledby="images-title">
          <h3 id="images-title">{t('admin.images')}</h3>
          {book.images.length === 0 && <p className="muted">{t('admin.imagesNone')}</p>}
          <ul className="admin-images">
            {book.images.map((image, index) => (
              <li key={image.id} className="admin-image">
                <img src={image.path} alt="" width="150" height="225" loading="lazy" />
                <div className="admin-image-actions">
                  {index === 0 ? (
                    <span className="status status-clean">{t('admin.imageMain')}</span>
                  ) : (
                    <button type="button" className="link-btn" onClick={() => changeImages(() => api.put(`/api/admin/books/${book.id}/images/${image.id}/main`))}>
                      {t('admin.imageMakeMain')}
                    </button>
                  )}
                  <button type="button" className="link-btn" onClick={() => changeImages(() => api.delete(`/api/admin/books/${book.id}/images/${image.id}`))}>
                    {t('admin.imageRemove')}
                  </button>
                </div>
              </li>
            ))}
          </ul>
          {imageProblem && <p className="form-error" role="alert">{imageProblem}</p>}
          <FileUpload label={t('admin.imageFiles')} accept=".jpg,.jpeg,.png,image/jpeg,image/png" multiple
            hint={t('admin.imagesHint')} buttonLabel={t('admin.imageUpload')}
            onUpload={(files) => upload(`/api/admin/books/${book.id}/images`, 'files', files)} />
        </section>
      )}

      {book?.products.filter((product) => isDigital(product.format)).map((product) => (
        <section key={product.id} className="admin-section" aria-label={t('admin.ebook', { format: t(`format.${product.format}`) })}>
          <h3>{t('admin.ebook', { format: t(`format.${product.format}`) })}</h3>
          {product.ebook ? (
            <p className="admin-ebook-status">
              {product.ebook.originalFilename} ({formatSize(product.ebook.sizeBytes)})
              <span className={`status status-${product.ebook.scanStatus.toLowerCase()}`}>
                {t(`admin.scan.${product.ebook.scanStatus}`)}
              </span>
            </p>
          ) : (
            <p className="muted">{t('admin.ebookNone')}</p>
          )}
          <FileUpload label={t('admin.ebookUpload')} accept={product.format === 'PDF' ? '.pdf,application/pdf' : '.epub,application/epub+zip'}
            hint={t('admin.ebookHint')} buttonLabel={t('admin.ebookUpload')}
            onUpload={(files) => upload(`/api/admin/books/${book.id}/products/${product.id}/ebook`, 'file', files.slice(0, 1))} />
        </section>
      ))}
    </div>
  )
}

function FormatRow({ format, product, errors = {}, onChange }) {
  const label = t(`format.${format}`)
  return (
    <div className="format-row">
      <label className="check">
        <input type="checkbox" checked={product.sell} onChange={(event) => onChange('sell', event.target.checked)} />
        {t('admin.sell', { format: label })}
      </label>
      {product.sell && (
        <div className="format-row-fields">
          <FormField label={t('admin.price')} type="number" step="0.01" min="0" value={product.price}
            error={errors.price} onChange={(event) => onChange('price', event.target.value)} />
          {!isDigital(format) && (
            <FormField label={t('admin.stock')} type="number" min="0" value={product.stockQuantity}
              error={errors.stockQuantity} onChange={(event) => onChange('stockQuantity', event.target.value)} />
          )}
          <FormField label={t('admin.isbn')} value={product.isbn} error={errors.isbn}
            onChange={(event) => onChange('isbn', event.target.value)} />
        </div>
      )}
    </div>
  )
}

function toForm(book) {
  const products = {}
  for (const format of FORMATS) {
    const existing = book?.products.find((item) => item.format === format && item.active)
    products[format] = {
      sell: Boolean(existing),
      price: existing?.price ?? '',
      stockQuantity: existing?.stockQuantity ?? 0,
      isbn: existing?.isbn ?? '',
    }
  }
  return {
    title: book?.title ?? '',
    authors: book?.authors.join(', ') ?? '',
    description: book?.description ?? '',
    publisher: book?.publisher ?? '',
    publishedOn: book?.publishedOn ?? '',
    languageCode: book?.languageCode ?? 'en',
    pageCount: book?.pageCount ?? '',
    featured: book?.featured ?? false,
    active: book?.active ?? true,
    categoryIds: book?.categoryIds ?? [],
    products,
  }
}

// The formats that are switched on, in the order they are sent; the server reports product errors by that position.
function soldFormats(form) {
  return FORMATS.filter((format) => form.products[format].sell)
}

function toRequest(form) {
  return {
    title: form.title,
    description: form.description,
    publisher: form.publisher,
    publishedOn: form.publishedOn || null,
    languageCode: form.languageCode,
    pageCount: form.pageCount === '' ? null : Number(form.pageCount),
    featured: form.featured,
    active: form.active,
    authors: form.authors.split(',').map((name) => name.trim()).filter(Boolean),
    categoryIds: form.categoryIds,
    products: soldFormats(form).map((format) => {
      const product = form.products[format]
      return {
        format,
        isbn: product.isbn,
        price: product.price === '' ? null : Number(product.price),
        stockQuantity: isDigital(format) ? 0 : Number(product.stockQuantity || 0),
        active: true,
      }
    }),
  }
}

/**
 * Turns the server's `{ "products[0].price": "..." }` into messages placed where the form shows them:
 * `authors[1]` belongs to the authors field and `products[0].price` to the price of the first format that is on.
 */
function readFieldErrors(raw, sold) {
  const result = { products: {} }
  for (const [key, message] of Object.entries(raw)) {
    const product = /^products\[(\d+)\]\.(\w+)$/.exec(key)
    if (product && sold[Number(product[1])]) {
      const format = sold[Number(product[1])]
      result.products[format] = { ...result.products[format], [product[2]]: message }
    } else if (key === 'products') {
      result.formats = message
    } else {
      result[key.replace(/\[\d+\]$/, '')] = message
    }
  }
  return result
}

function hasFieldErrors(fields) {
  return Object.keys(fields).length > 1 || Object.keys(fields.products).length > 0
}

function formatSize(bytes) {
  return bytes < 1024 * 1024 ? `${Math.max(1, Math.round(bytes / 1024))} KB` : `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}
