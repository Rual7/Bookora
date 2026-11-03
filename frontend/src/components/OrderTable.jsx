import { t } from '../i18n/t.js'
import { formatPrice } from '../utils/format.js'

/**
 * The lines of an order with its delivery cost and total, used by the customer's and the administrator's order page.
 * `renderAction` may add something under a line's title, such as a download button.
 * Old orders keep the currency they were placed in, so prices are shown with order.currency.
 */
export default function OrderTable({ order, renderAction }) {
  const price = (amount) => formatPrice(amount, order.currency)

  return (
    <div className="table-wrap">
      <table className="data-table">
        <caption className="visually-hidden">{t('order.caption', { number: order.orderNumber })}</caption>
        <thead>
          <tr>
            <th scope="col">{t('order.book')}</th>
            <th scope="col">{t('order.format')}</th>
            <th scope="col" className="num">{t('order.price')}</th>
            <th scope="col" className="num">{t('order.qty')}</th>
            <th scope="col" className="num">{t('orders.total')}</th>
          </tr>
        </thead>
        <tbody>
          {order.items.map((item) => (
            <tr key={item.id}>
              <th scope="row">
                {item.title}
                {renderAction?.(item)}
              </th>
              <td data-label={t('order.format')}>{t(`format.${item.format}`)}</td>
              <td className="num" data-label={t('order.price')}>{price(item.unitPrice)}</td>
              <td className="num" data-label={t('order.qty')}>{item.quantity}</td>
              <td className="num" data-label={t('orders.total')}>{price(item.lineTotal)}</td>
            </tr>
          ))}
        </tbody>
        <tfoot>
          <tr>
            <th scope="row" colSpan={4}>{t('cart.delivery')}</th>
            <td className="num">{Number(order.shippingCost) === 0 ? t('shipping.free') : price(order.shippingCost)}</td>
          </tr>
          <tr>
            <th scope="row" colSpan={4}>{t('orders.total')}</th>
            <td className="num"><strong>{price(order.total)}</strong></td>
          </tr>
        </tfoot>
      </table>
    </div>
  )
}
