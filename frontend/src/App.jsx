import { lazy, Suspense } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import Header from './components/Header.jsx'
import Footer from './components/Footer.jsx'
import CartDrawer from './components/CartDrawer.jsx'
import WishlistDrawer from './components/WishlistDrawer.jsx'
import StorageNotice from './components/StorageNotice.jsx'
import RequireAuth from './components/RequireAuth.jsx'
import RequireAdmin from './components/RequireAdmin.jsx'
import ScrollToTop from './components/ScrollToTop.jsx'
import { t } from './i18n/t.js'
import HomePage from './pages/HomePage.jsx'
import BooksPage from './pages/BooksPage.jsx'
import BookPage from './pages/BookPage.jsx'
import CategoriesPage from './pages/CategoriesPage.jsx'
import AboutPage from './pages/AboutPage.jsx'
import CartPage from './pages/CartPage.jsx'
import AuthPage from './pages/AuthPage.jsx'
import CheckoutPage from './pages/CheckoutPage.jsx'
import OrdersPage from './pages/OrdersPage.jsx'
import OrderPage from './pages/OrderPage.jsx'
import AddressesPage from './pages/AddressesPage.jsx'
import LibraryPage from './pages/LibraryPage.jsx'
import NotFoundPage from './pages/NotFoundPage.jsx'

// The administration is only for staff, so its code is loaded when someone opens it.
const AdminLayout = lazy(() => import('./pages/admin/AdminLayout.jsx'))
const AdminBooksPage = lazy(() => import('./pages/admin/AdminBooksPage.jsx'))
const AdminBookPage = lazy(() => import('./pages/admin/AdminBookPage.jsx'))
const AdminOrdersPage = lazy(() => import('./pages/admin/AdminOrdersPage.jsx'))
const AdminOrderPage = lazy(() => import('./pages/admin/AdminOrderPage.jsx'))
const AdminReviewsPage = lazy(() => import('./pages/admin/AdminReviewsPage.jsx'))
const AdminMessagesPage = lazy(() => import('./pages/admin/AdminMessagesPage.jsx'))
const AdminCustomersPage = lazy(() => import('./pages/admin/AdminCustomersPage.jsx'))

export default function App() {
  return (
    <>
      <a className="skip-link" href="#main">
        {t('nav.skip')}
      </a>
      <ScrollToTop />
      <Header />
      <main id="main" className="main-content">
        <Suspense fallback={<p className="container page muted">{t('common.loading')}</p>}>
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/books" element={<BooksPage />} />
            <Route path="/books/:slug" element={<BookPage />} />
            <Route path="/categories" element={<CategoriesPage />} />
            <Route path="/about" element={<AboutPage />} />
            <Route path="/cart" element={<CartPage />} />
            <Route path="/login" element={<AuthPage mode="login" />} />
            <Route path="/register" element={<AuthPage mode="register" />} />
            <Route path="/checkout" element={<RequireAuth><CheckoutPage /></RequireAuth>} />
            <Route path="/orders" element={<RequireAuth><OrdersPage /></RequireAuth>} />
            <Route path="/orders/:orderNumber" element={<RequireAuth><OrderPage /></RequireAuth>} />
            <Route path="/ebooks" element={<RequireAuth><LibraryPage /></RequireAuth>} />
            <Route path="/addresses" element={<RequireAuth><AddressesPage /></RequireAuth>} />
            <Route path="/admin" element={<RequireAdmin><AdminLayout /></RequireAdmin>}>
              <Route index element={<Navigate to="books" replace />} />
              <Route path="books" element={<AdminBooksPage />} />
              <Route path="books/new" element={<AdminBookPage />} />
              <Route path="books/:id" element={<AdminBookPage />} />
              <Route path="orders" element={<AdminOrdersPage />} />
              <Route path="orders/:orderNumber" element={<AdminOrderPage />} />
              <Route path="reviews" element={<AdminReviewsPage />} />
              <Route path="messages" element={<AdminMessagesPage />} />
              <Route path="customers" element={<AdminCustomersPage />} />
            </Route>
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </Suspense>
      </main>
      <Footer />
      <CartDrawer />
      <WishlistDrawer />
      <StorageNotice />
    </>
  )
}
