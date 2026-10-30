import './CategoryArt.css'

/**
 * One simple illustration per category, drawn in colours that suit its theme.
 * Replace them with photos later by swapping this component's output.
 */
const DRAWINGS = {
  fiction: (
    <>
      <path className="art-c1" d="M30 30 Q65 20 100 36 V100 Q65 86 30 96Z" />
      <path className="art-c2" d="M100 36 Q135 20 170 30 V96 Q135 86 100 100Z" />
      <path className="art-line" d="M45 50 Q65 46 85 54 M45 64 Q65 60 85 68 M115 54 Q135 46 155 50 M115 68 Q135 60 155 64" />
    </>
  ),
  fantasy: (
    <>
      <path className="art-c1" d="M112 18 A42 42 0 1 0 146 92 A34 34 0 1 1 112 18Z" />
      <path className="art-c2" d="M60 26 l4 10 10 4 -10 4 -4 10 -4 -10 -10 -4 10 -4Z" />
      <path className="art-c2" d="M166 44 l3 7 7 3 -7 3 -3 7 -3 -7 -7 -3 7 -3Z" />
      <circle className="art-c2" cx="38" cy="86" r="3" />
    </>
  ),
  'science-fiction': (
    <>
      <circle className="art-c1" cx="100" cy="60" r="28" />
      <ellipse className="art-ring" cx="100" cy="60" rx="58" ry="14" transform="rotate(-18 100 60)" />
      <path className="art-c1" d="M40 28 l2 5 5 2 -5 2 -2 5 -2 -5 -5 -2 5 -2Z" />
      <circle className="art-c2" cx="165" cy="98" r="4" />
      <circle className="art-c2" cx="162" cy="24" r="2.5" />
      <circle className="art-c2" cx="30" cy="88" r="2" />
    </>
  ),
  romance: (
    <>
      <path className="art-c1" d="M100 100 C40 62 52 24 80 28 C92 30 98 38 100 44 C102 38 108 30 120 28 C148 24 160 62 100 100Z" />
      <path className="art-c2" d="M40 34 c-8 -8 -18 4 -8 12 l8 7 8 -7 c10 -8 0 -20 -8 -12Z" />
      <path className="art-c2" d="M164 76 c-6 -6 -14 3 -6 9 l6 5 6 -5 c8 -6 0 -15 -6 -9Z" />
    </>
  ),
  philosophy: (
    <>
      <path className="art-c2" d="M40 44 L100 18 L160 44Z" />
      <rect className="art-c1" x="48" y="50" width="14" height="42" />
      <rect className="art-c1" x="78" y="50" width="14" height="42" />
      <rect className="art-c1" x="108" y="50" width="14" height="42" />
      <rect className="art-c1" x="138" y="50" width="14" height="42" />
      <rect className="art-c2" x="38" y="96" width="124" height="8" />
    </>
  ),
  'self-development': (
    <>
      <path className="art-line art-thick" d="M100 104 V56" />
      <path className="art-c2" d="M100 68 C100 42 78 32 54 36 C54 60 76 72 100 68Z" />
      <path className="art-c1" d="M100 56 C100 30 122 20 148 24 C148 48 124 60 100 56Z" />
      <path className="art-line" d="M66 104 H134" />
      <path className="art-c2" d="M46 84 l5 -10 5 10 -5 -3Z" />
    </>
  ),
  'mystery-thriller': (
    <>
      <circle className="art-glass" cx="88" cy="54" r="32" />
      <circle className="art-ring" cx="88" cy="54" r="32" />
      <path className="art-line art-thick" d="M112 78 L152 104" />
      <path className="art-line" d="M70 52 q6 -14 18 -10 M70 62 q10 -18 26 -12 M78 70 q14 -20 26 -10" />
      <path className="art-c2" d="M160 30 a8 8 0 1 0 16 0 a8 8 0 1 0 -16 0Z" />
    </>
  ),
  classics: (
    <>
      <path className="art-c1" d="M150 18 C112 22 84 52 72 96 C104 84 140 60 150 18Z" />
      <path className="art-line" d="M72 96 L50 108 M96 70 L120 58 M108 54 L132 44" />
      <path className="art-c2" d="M110 100 h40 v10 h-40Z" />
      <path className="art-c2" d="M120 100 v-8 h20 v8Z" />
    </>
  ),
}

export default function CategoryArt({ slug }) {
  return (
    <svg className={`category-art art-${slug}`} viewBox="0 0 200 120" aria-hidden="true" focusable="false">
      {DRAWINGS[slug] ?? DRAWINGS.fiction}
    </svg>
  )
}
