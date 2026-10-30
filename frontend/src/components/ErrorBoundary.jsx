import { Component } from 'react'

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
      <div className="container page">
        <h1>Something went wrong</h1>
        <p className="muted">Please reload the page. If it keeps happening, try again in a few minutes.</p>
        <p><button type="button" className="btn" onClick={() => window.location.reload()}>Reload</button></p>
      </div>
    )
  }
}
