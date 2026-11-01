import { Navigate, Route, Routes } from 'react-router-dom'
import RequireAdmin from './components/RequireAdmin.jsx'
import AdminLayout from './pages/admin/AdminLayout.jsx'
import AdminBooksPage from './pages/admin/AdminBooksPage.jsx'
import AdminBookPage from './pages/admin/AdminBookPage.jsx'
import AdminOrdersPage from './pages/admin/AdminOrdersPage.jsx'
import AdminOrderPage from './pages/admin/AdminOrderPage.jsx'
import AdminReviewsPage from './pages/admin/AdminReviewsPage.jsx'
import AdminMessagesPage from './pages/admin/AdminMessagesPage.jsx'
import AdminCustomersPage from './pages/admin/AdminCustomersPage.jsx'
import Header from './components/Header.jsx'
import { t } from './i18n/t.js'
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
import CheckoutPage from './pages/CheckoutPage.jsx'
import OrdersPage from './pages/OrdersPage.jsx'
import OrderPage from './pages/OrderPage.jsx'
import AddressesPage from './pages/AddressesPage.jsx'
import LibraryPage from './pages/LibraryPage.jsx'
import NotFoundPage from './pages/NotFoundPage.jsx'

export default function App() {
  return (
    <>
      <a className="skip-link" href="#main">
        {t('nav.skip')}
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
          <Route path="/checkout" element={<RequireAuth><CheckoutPage /></RequireAuth>} />
          <Route path="/orders" element={<RequireAuth><OrdersPage /></RequireAuth>} />
          <Route path="/orders/:orderNumber" element={<RequireAuth><OrderPage /></RequireAuth>} />
          <Route path="/ebooks" element={<RequireAuth><LibraryPage /></RequireAuth>} />
          <Route path="/addresses"element={<RequireAuth><AddressesPage /></RequireAuth>} />
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
      </main>
      <Footer />
      <CartDrawer />
      <WishlistDrawer />
      <StorageNotice />
    </>
  )
}
