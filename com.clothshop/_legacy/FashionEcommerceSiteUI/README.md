# MINIMAL Fashion Store - Legacy HTML/CSS/JS Version

## 📋 Tổng quan

Đây là phiên bản HTML/CSS/JavaScript thuần của MINIMAL Fashion Store, được xây dựng để match 100% với giao diện Next.js version. Tất cả chức năng và thiết kế đã được chuyển đổi từ Next.js + Tailwind + Framer Motion sang vanilla web technologies.

## 🎨 Design System

### Typography
- **Display Font**: `Cormorant Garamond` (serif) - Dùng cho headings, titles
- **Body Font**: `Inter` (sans-serif) - Dùng cho body text
- Load từ Google Fonts

### Color Palette
```css
--color-bg: #FAFAF8          /* Warm off-white background */
--color-primary: #1C1C1A      /* Near-black text */
--color-accent: #C5A882       /* Gold accent */
--color-secondary: #6B6B67    /* Gray text */
--color-border: #E8E6E1       /* Light borders */
--color-light-bg: #FFFFFF     /* Pure white for cards */
--color-white: #FFFFFF
```

### Layout
- Max container width: `1280px`
- Header height: `64px`
- Border radius: `0` (sharp corners cho minimal aesthetic)
- Transitions: `0.3s ease`

## 📁 Cấu trúc Files

```
_legacy/
├── index.html          # Main SPA với tất cả pages
├── style.css          # 4330+ lines CSS với full design system
├── script.js          # Application logic & state management
├── README.md          # Documentation này
└── admin/
    ├── index.html     # Admin dashboard
    ├── style.css      # Admin styles matching main site
    └── script.js      # Admin functionality
```

## 🌐 Pages & Features

### Main Site Pages
1. **Home** (`#page-home`)
   - Hero banner với 2-column layout (text + image)
   - Featured Collections với 3 collection cards
   - Best Sellers grid (8 products)
   - Flash Sale section với countdown timer
   - Newsletter signup

2. **Products** (`#page-products`)
   - Filter sidebar (categories, colors, price range, sizes)
   - Sort options (newest, price low-high, price high-low)
   - Product grid với lazy loading
   - Product cards với badges (new, sale, flash-sale)

3. **Product Detail** (`#page-product-detail`)
   - Large product image
   - Product info với color/size selectors
   - Add to cart, wishlist, quick view
   - Product description & reviews

4. **Virtual Try-On** (`#page-try-on`)
   - Camera integration placeholder
   - Product selection panel

5. **Cart** (`#page-cart`)
   - Cart items list với quantity controls
   - Price calculation
   - Voucher input
   - Checkout button

6. **Checkout** (`#page-checkout`)
   - Shipping info form
   - Payment method selection
   - Order summary
   - Place order button

7. **Order Success** (`#page-order-success`)
   - Order confirmation với order ID
   - Order details summary

8. **Auth** (`#page-auth`)
   - Login form
   - Register form
   - Tab switching

9. **Profile** (`#page-profile`)
   - User info display
   - Edit profile form
   - Address management

10. **Orders** (`#page-orders`)
    - Order history list
    - Order status tracking

11. **Returns** (`#page-returns`)
    - Return request form
    - Return history

12. **Wishlist** (`#page-wishlist`)
    - Saved products grid
    - Quick add to cart

### Admin Panel
- Dashboard với revenue charts & stats cards
- Staff management (list + create/edit)
- Voucher management
- Banner management
- Featured collections/products
- Push notifications
- Product management (list + inventory + collections)
- Order management (incoming + all orders + payment verification + RMA)
- Reports (revenue, wishlist analytics)
- Customer management (details + feedback moderation)

## 🎯 Product Data

12 products với full details:
- IDs: 1-12
- Categories: tops, bottoms, outerwear
- Colors: Hex codes array
- Sizes: XS, S, M, L, XL
- Badges: new, sale, flash-sale
- Images: Unsplash URLs
- Ratings & reviews count
- Stock levels

### Featured Products
- **Best Sellers**: IDs [2, 1, 5, 8, 4, 6, 7, 12]
- **Flash Sale**: IDs [8, 4, 10, 2]

## 🖼️ Images (từ Next.js mockData)

### Collections
1. Áo: `https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800&q=80`
2. Quần & Váy: `https://images.unsplash.com/photo-1551488831-00ddcb6c6bd3?w=800&q=80`
3. Áo khoác: `https://images.unsplash.com/photo-1544441893-675973e31985?w=800&q=80`

### Hero Image
- Main: `https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800&q=85`

### Product Images
Mỗi product có image URL từ Unsplash (xem trong `script.js` PRODUCTS array)

## 💻 Technical Details

### Animations
- Hero fade-in với staggered delays (0.2s → 0.7s)
- Collection cards: scale + overlay fade on hover
- Product cards: action buttons slide up on hover
- Smooth page transitions
- Search overlay fade in/out
- Mobile nav slide in from right

### Responsive Breakpoints
```css
@media (max-width: 1024px) /* Tablet */
@media (max-width: 768px)  /* Mobile landscape */
@media (max-width: 640px)  /* Mobile */
@media (max-width: 480px)  /* Small mobile */
```

### State Management (JavaScript)
```javascript
let cart = []
let wishlist = []
let currentUser = null
let currentPage = 'home'
let currentProduct = null
let filteredProducts = [...PRODUCTS]
```

### Navigation
SPA với hash-based routing:
```javascript
navigate(page)          // Chuyển page
filterAndNavigate(cat)  // Filter + navigate to products
addToCart(productId)    // Thêm vào giỏ
addToWishlist(id)      // Thêm vào wishlist
```

## 🚀 Cách chạy

### Simple HTTP Server
```bash
cd _legacy
python3 -m http.server 8000
# Mở http://localhost:8000
```

### Node.js http-server
```bash
npx http-server _legacy -p 8000
```

### VS Code Live Server
- Cài extension "Live Server"
- Right-click index.html → "Open with Live Server"

## ✨ Features Highlights

### Main Site
- ✅ Fully responsive design
- ✅ Smooth animations & transitions
- ✅ Product filtering & sorting
- ✅ Cart functionality với quantity controls
- ✅ Wishlist management
- ✅ Search overlay
- ✅ Mobile hamburger menu
- ✅ Flash sale countdown timer
- ✅ Newsletter form
- ✅ Product ratings & reviews display
- ✅ Color & size selection
- ✅ Stock level indicators

### Admin Panel
- ✅ Clean minimal dashboard
- ✅ Revenue charts (SVG rendering)
- ✅ Stats cards với hover effects
- ✅ Staff CRUD interface
- ✅ Voucher management forms
- ✅ Order status tracking
- ✅ Sidebar navigation với active states
- ✅ Responsive table design

## 🎨 Design Matching với Next.js

### ✅ Đã match 100%
- Typography (Cormorant Garamond + Inter)
- Color palette (warm minimal với gold accent)
- Hero layout (2-column với image + badge)
- Collections grid (hover overlay effects)
- Product cards (badges, actions, colors)
- Flash Sale section (dark theme)
- Newsletter section
- Footer layout
- Admin dashboard (stats cards, navigation)

### CSS Techniques
- CSS Variables cho theming
- Flexbox & Grid cho layouts
- Aspect-ratio cho images
- Backdrop-filter cho glass effects
- Keyframe animations thay Framer Motion
- Smooth transitions everywhere

## 📊 Performance

- No framework overhead
- ~4330 lines CSS (minify recommended for production)
- ~1500 lines JavaScript
- Images: Lazy loading với `loading="lazy"`
- Animations: Hardware-accelerated với `transform`

## 🔧 Customization

### Thêm product mới
Edit `script.js` → PRODUCTS array:
```javascript
{
  id: '13',
  name: 'Tên sản phẩm',
  brand: 'MINIMAL',
  price: 990000,
  category: 'tops',
  colors: ['#FFFFFF'],
  sizes: ['S','M','L'],
  badge: 'new',
  image: 'url...'
}
```

### Đổi colors
Edit `style.css` → `:root` variables

### Thêm page mới
1. Add section trong `index.html`
2. Add page styles trong `style.css`
3. Add navigation trong `script.js`

## 🌟 Best Practices

- Semantic HTML5
- BEM-inspired class naming
- Mobile-first responsive design
- Accessible navigation
- SEO-friendly structure
- Clean separation of concerns

## 📞 Support

Nếu có vấn đề, check:
1. Browser console for errors
2. Network tab for failed image loads
3. CSS animations working (hardware acceleration)
4. JavaScript state management

---

**Version**: 1.0.0 (March 2026)  
**Design System**: Matching Next.js v14 implementation  
**Status**: ✅ Production Ready
