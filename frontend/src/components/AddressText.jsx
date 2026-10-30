import './AddressText.css'

/** An address written out as lines. Works for saved addresses and for the copy kept on an order. */
export default function AddressText({ address }) {
  const place = [address.postalCode, address.city, address.countyRegion].filter(Boolean).join(', ')
  return (
    <address className="address-text">
      <strong>{address.recipientName}</strong>
      <span>{address.streetAddress}</span>
      {address.buildingDetails && <span>{address.buildingDetails}</span>}
      <span>{place}</span>
      <span>{address.country}</span>
      <span>{address.phone}</span>
    </address>
  )
}
