import Hero from '../components/Hero.jsx'
import CategoryStrip from '../components/CategoryStrip.jsx'
import FeaturedCarousel from '../components/FeaturedCarousel.jsx'
import EbookPromo from '../components/EbookPromo.jsx'
import TrustBar from '../components/TrustBar.jsx'
import { usePageMeta } from '../hooks/usePageMeta.js'
import { t } from '../i18n/t.js'

export default function HomePage() {
  usePageMeta({ title: t('meta.siteTitle'), home: true })

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
