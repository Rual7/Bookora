import Hero from '../components/Hero.jsx'
import CategoryStrip from '../components/CategoryStrip.jsx'
import FeaturedCarousel from '../components/FeaturedCarousel.jsx'
import EbookPromo from '../components/EbookPromo.jsx'
import TrustBar from '../components/TrustBar.jsx'

export default function HomePage() {
  return (
    <>
      <Hero />
      <CategoryStrip />
      <FeaturedCarousel />
      <EbookPromo />
      <TrustBar />
    </>
  )
}
