import { Route, Routes } from 'react-router-dom'
import Header from './components/Header.jsx'
import Footer from './components/Footer.jsx'
import CartDrawer from './components/CartDrawer.jsx'
import WishlistDrawer from './components/WishlistDrawer.jsx'
import StorageNotice from './components/StorageNotice.jsx'
import RequireAuth from './components/RequireAuth.jsx'
import HomePage from './pages/HomePage.jsx'
import BooksPage from './pages/BooksPage.jsx'
import BookPage from './pages/BookPage.jsx'
import CategoriesPage from './pages/CategoriesPage.jsx'
import AboutPage from './pages/AboutPage.jsx'
import CartPage from './pages/CartPage.jsx'
import AuthPage from './pages/AuthPage.jsx'
import PlaceholderPage from './pages/PlaceholderPage.jsx'

export default function App() {
  return (
    <>
      <a className="skip-link" href="#main">
        Skip to main content
      </a>
      <Header />
      <main id="main" className="main-content">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/books" element={<BooksPage />} />
          <Route path="/books/:slug" element={<BookPage />} />
          <Route path="/categories" element={<CategoriesPage />} />
          <Route path="/about" element={<AboutPage />} />
          <Route path="/cart" element={<CartPage />} />
          <Route path="/login" element={<AuthPage mode="login" />} />
          <Route path="/register" element={<AuthPage mode="register" />} />
          <Route path="/orders" element={<RequireAuth><PlaceholderPage title="Orders" /></RequireAuth>} />
          <Route path="*" element={<PlaceholderPage title="Page not found" />} />
        </Routes>
      </main>
      <Footer />
      <CartDrawer />
      <WishlistDrawer />
      <StorageNotice />
    </>
  )
}
