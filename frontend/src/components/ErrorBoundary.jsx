import { Component } from 'react'
import { TriangleAlert } from 'lucide-react'
import StatusPage from './StatusPage.jsx'
import { t } from '../i18n/t.js'

/** Shows a message instead of an empty page if something in the app crashes while rendering. */
export default class ErrorBoundary extends Component {
  state = { failed: false }

  static getDerivedStateFromError() {
    return { failed: true }
  }

  componentDidCatch(error) {
    console.error('The page crashed:', error)
  }

  render() {
    if (!this.state.failed) return this.props.children
    return (
      <StatusPage icon={TriangleAlert} title={t('common.wrong')} text={t('error.crash')}>
        <button type="button" className="btn" onClick={() => window.location.reload()}>{t('error.reload')}</button>
        <a href="/" className="btn btn-outline">{t('notFound.home')}</a>
      </StatusPage>
    )
  }
}
