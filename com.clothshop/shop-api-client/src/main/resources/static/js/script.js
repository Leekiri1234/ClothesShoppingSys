/* =============================================================
   FASHION STORE SPA — MASTER SCRIPT
   ============================================================= */

'use strict';

// ─────────────────────────────────────────
//  MOCK DATA - Updated from Next.js
// ─────────────────────────────────────────
const PRODUCTS = [
  { id: '1', name: 'Áo Linen Trắng Tinh', brand: 'MINIMAL', price: 890000, originalPrice: null, category: 'tops', colors: ['#FFFFFF','#C5A882'], sizes: ['XS','S','M','L'], badge: null, rating: 4.8, reviews: 24, stock: 29, description: 'Áo linen trắng tinh tế với đường cắt may tối giản', bg: '#F0EDE7', image: 'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=800&q=80' },
  { id: '2', name: 'Áo Silk Tay Phồng', brand: 'MINIMAL', price: 1290000, originalPrice: null, category: 'tops', colors: ['#FFFEF0','#FFB5C8'], sizes: ['S','M'], badge: 'new', rating: 4.9, reviews: 18, stock: 23, description: 'Áo lụa tay phồng nhẹ nhàng, nữ tính', bg: '#FFF5E0', image: 'https://images.unsplash.com/photo-1529139574466-a303027c1d8b?w=800&q=80' },
  { id: '3', name: 'Quần Culottes Xám', brand: 'MINIMAL', price: 790000, originalPrice: null, category: 'bottoms', colors: ['#9CA3AF','#1C1C1A'], sizes: ['S','M','L'], badge: null, rating: 4.6, reviews: 31, stock: 50, description: 'Quần culottes xám thanh lịch, ống rộng tạo dáng đẹp', bg: '#E8E6E1', image: 'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=800&q=80' },
  { id: '4', name: 'Váy Midi Đen Thanh Lịch', brand: 'MINIMAL', price: 890000, originalPrice: 1190000, category: 'bottoms', colors: ['#1C1C1A'], sizes: ['XS','S','M','L'], badge: 'sale', rating: 4.9, reviews: 42, stock: 25, description: 'Váy midi đen thanh lịch với đường xẻ nhẹ phía sau', bg: '#F0F0F0', image: 'https://images.unsplash.com/photo-1551488831-00ddcb6c6bd3?w=800&q=80' },
  { id: '5', name: 'Áo Khoác Dạ Kem Cao Cấp', brand: 'MINIMAL', price: 2490000, originalPrice: null, category: 'outerwear', colors: ['#FFF5E0','#C68642'], sizes: ['S','M','L'], badge: null, rating: 4.8, reviews: 15, stock: 17, description: 'Áo khoác dạ kem sang trọng, thiết kế dáng dài kinh điển', bg: '#FFF5E0', image: 'https://images.unsplash.com/photo-1544441893-675973e31985?w=800&q=80' },
  { id: '6', name: 'Áo Croptop Cotton Cơ Bản', brand: 'MINIMAL', price: 590000, originalPrice: null, category: 'tops', colors: ['#FFFFFF','#1C1C1A'], sizes: ['XS','S','M'], badge: null, rating: 4.5, reviews: 67, stock: 105, description: 'Áo croptop cotton mềm mại, thoải mái cho ngày hè', bg: '#FAFAF8', image: 'https://images.unsplash.com/photo-1539109136881-3be0616acf4b?w=800&q=80' },
  { id: '7', name: 'Quần Wide Leg Be Thanh Thoát', brand: 'MINIMAL', price: 990000, originalPrice: null, category: 'bottoms', colors: ['#C5A882','#B0D4E8'], sizes: ['XS','S','M','L'], badge: null, rating: 4.7, reviews: 22, stock: 46, description: 'Quần wide leg màu be thanh thoát, tôn dáng', bg: '#FFF5E0', image: 'https://images.unsplash.com/photo-1541099649105-f69ad21f3246?w=800&q=80' },
  { id: '8', name: 'Áo Blazer Đen Cổ Điển', brand: 'MINIMAL', price: 1290000, originalPrice: 1690000, category: 'tops', colors: ['#1C1C1A'], sizes: ['S','M','L','XL'], badge: 'flash-sale', rating: 4.8, reviews: 12, stock: 20, description: 'Áo blazer đen cổ điển kiểu dáng slim fit', bg: '#F0EDE7', image: 'https://images.unsplash.com/photo-1548094990-c16ca90f1f0d?w=800&q=80' },
  { id: '9', name: 'Áo Cardigan Len Mỏng', brand: 'MINIMAL', price: 1390000, originalPrice: null, category: 'outerwear', colors: ['#FFF5E0','#9CA3AF'], sizes: ['S','M','L'], badge: null, rating: 4.6, reviews: 19, stock: 28, description: 'Áo cardigan len mỏng, nhẹ nhàng cho thu', bg: '#FFF5E0', image: 'https://images.unsplash.com/photo-1562157873-818bc0726f68?w=800&q=80' },
  { id: '10', name: 'Váy Maxi Hoa Nhí', brand: 'MINIMAL', price: 1590000, originalPrice: 1990000, category: 'bottoms', colors: ['#FFFEF0'], sizes: ['XS','S','M','L'], badge: 'sale', rating: 4.7, reviews: 33, stock: 12, description: 'Váy maxi hoa nhí nữ tính, phù hợp dạo phố', bg: '#FFF5E0', image: 'https://images.unsplash.com/photo-1596516109370-29001ec8ec36?w=800&q=80' },
  { id: '11', name: 'Quần Jeans Straight Fit', brand: 'MINIMAL', price: 890000, originalPrice: null, category: 'bottoms', colors: ['#1B2A4A','#1C1C1A'], sizes: ['S','M','L','XL'], badge: null, rating: 4.5, reviews: 44, stock: 60, description: 'Quần jeans straight fit cổ điển, dễ phối đồ', bg: '#E8E6E1', image: 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=800&q=80' },
  { id: '12', name: 'Áo Sơ Mi Oversized Trắng', brand: 'MINIMAL', price: 790000, originalPrice: null, category: 'tops', colors: ['#FFFFFF','#C5A882'], sizes: ['S','M','L','XL'], badge: 'new', rating: 4.7, reviews: 28, stock: 42, description: 'Áo sơ mi oversized trắng phong cách unisex', bg: '#FAFAF8', image: 'https://images.unsplash.com/photo-1598554747436-c9293d6a588f?w=800&q=80' }
];

const FLASH_SALE_IDS = ['8', '4', '10', '2'];
const BEST_SELLER_IDS = ['2', '1', '5', '8', '4', '6', '7', '12'];

const SAMPLE_ORDERS = [
  { id: '#MN-2025-0042', date: '05/03/2025', total: 2580000, status: 'shipped', items: [{ name: 'Áo sơ mi Linen', qty: 1, price: 1290000, bg: '#f5f0ea' }, { name: 'Chân váy Chữ A', qty: 2, price: 690000, bg: '#f8f8f8' }] },
  { id: '#MN-2025-0038', date: '28/02/2025', total: 890000, status: 'delivered', items: [{ name: 'Quần tây Slim Fit', qty: 1, price: 890000, bg: '#e8e8e8' }] },
  { id: '#MN-2025-0031', date: '20/02/2025', total: 1272000, status: 'delivered', items: [{ name: 'Váy Midi Elegant', qty: 1, price: 1272000, bg: '#fafafa' }] },
  { id: '#MN-2025-0025', date: '10/02/2025', total: 2190000, status: 'pending', items: [{ name: 'Blazer Cổ điển', qty: 1, price: 2190000, bg: '#f0f0f0' }] },
  { id: '#MN-2025-0019', date: '01/02/2025', total: 490000, status: 'cancelled', items: [{ name: 'Áo thun Pima Cotton', qty: 1, price: 490000, bg: '#f0ede8' }] }
];

const ORDER_STATUS_LABELS = {
  pending: 'Chờ xác nhận', processing: 'Đang xử lý',
  shipped: 'Đang giao', delivered: 'Đã giao', cancelled: 'Đã hủy'
};

// ─────────────────────────────────────────
//  STATE
// ─────────────────────────────────────────
let cart = [];
let wishlist = [];
let currentPage = 'home';
let currentProductId = null;
let modalProductId = null;
let countdownInterval = null;
let carouselIndex = 0;
let filteredProducts = [...PRODUCTS];
let currentPagination = { page: 1, perPage: 6 };
let appliedVoucher = null;
let selectedColorFilters = [];
let orderCount = 42;

// ─────────────────────────────────────────
//  ROUTER
// ─────────────────────────────────────────
function navigate(pageId) {
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  const target = document.getElementById('page-' + pageId);
  if (target) {
    target.classList.add('active');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
  currentPage = pageId;

  document.querySelectorAll('.nav-link').forEach(n => n.classList.remove('active'));
  const activeLink = document.querySelector(`.nav-link[onclick*="'${pageId}'"]`);
  if (activeLink) activeLink.classList.add('active');

  // Page-specific init
  if (pageId === 'products') renderProductGrid();
  if (pageId === 'cart') renderCartPage();
  if (pageId === 'checkout') renderCheckoutSummary();
  if (pageId === 'orders') renderOrders('all');
  if (pageId === 'wishlist') renderWishlistPage();
  if (pageId === 'try-on') renderTryOnItems();
  if (pageId === 'order-success') renderOrderSuccess();
}

function filterAndNavigate(category) {
  navigate('products');
  setTimeout(() => {
    document.querySelectorAll('#product-grid .product-card').forEach(c => {
      c.style.display = c.dataset.category === category ? '' : 'none';
    });
    const count = PRODUCTS.filter(p => p.category === category).length;
    document.getElementById('product-count').textContent = `Hiển thị ${count} sản phẩm`;
  }, 50);
}

// ─────────────────────────────────────────
//  PRODUCT CARD BUILDER - Next.js Style
// ─────────────────────────────────────────
function buildProductCard(product, showRemoveWishlist = false) {
  const isInWishlist = wishlist.includes(product.id);
  const hasSale = product.originalPrice !== null;
  const discount = hasSale ? Math.round((1 - product.price / product.originalPrice) * 100) : 0;

  const priceHTML = hasSale
    ? `<span class="product-price-sale">${formatPrice(product.price)}</span><span class="product-price-original">${formatPrice(product.originalPrice)}</span>`
    : `<span class="product-price-current">${formatPrice(product.price)}</span>`;

  let badgeHTML = '';
  if (hasSale && product.badge === 'sale') {
    badgeHTML = `<div class="product-card-badge sale">-${discount}%</div>`;
  } else if (product.badge === 'flash-sale') {
    badgeHTML = `<div class="product-card-badge flash-sale">Flash Sale</div>`;
  } else if (product.badge === 'new') {
    badgeHTML = `<div class="product-card-badge new">Mới</div>`;
  }

  const imageHTML = product.image
    ? `<img src="${product.image}" alt="${product.name}" loading="lazy" />`
    : `<div class="img-placeholder" style="width:100%;height:100%;display:flex;align-items:center;justify-content:center;font-size:11px;letter-spacing:0.1em;color:#C5A882">${product.name}</div>`;

  const colorSwatchesHTML = product.colors.slice(0, 3).map(color => {
    const isWhite = color === '#FFFFFF' || color === '#FFFEF0';
    return `<div class="product-color-swatch ${isWhite ? 'white' : ''}" style="background-color:${color}" title="${color}"></div>`;
  }).join('');

  return `
    <div class="product-card" data-id="${product.id}" data-category="${product.category}" onclick="openProductDetail('${product.id}')">
      <div class="product-card-image" style="background:${product.bg}">
        ${badgeHTML}
        <div class="product-card-actions">
          <button class="product-card-action-btn ${isInWishlist ? 'active' : ''}" onclick="toggleWishlist('${product.id}');event.stopPropagation()" title="${isInWishlist ? 'Bỏ yêu thích' : 'Yêu thích'}">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="${isInWishlist ? 'currentColor' : 'none'}" stroke="currentColor" stroke-width="1.5"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
          </button>
          <button class="product-card-action-btn" onclick="openQuickView('${product.id}');event.stopPropagation()" title="Xem nhanh">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
          </button>
          <button class="product-card-action-btn" onclick="quickAddToCart('${product.id}');event.stopPropagation()" title="Thêm vào giỏ">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 0 1-8 0"/></svg>
          </button>
        </div>
        ${imageHTML}
      </div>
      <div class="product-card-info">
        <div class="product-card-brand">${product.brand}</div>
        <div class="product-card-name">${product.name}</div>
        <div class="product-card-price">${priceHTML}</div>
        ${product.colors.length > 0 ? `<div class="product-card-colors">${colorSwatchesHTML}</div>` : ''}
      </div>
    </div>`;
}

// ─────────────────────────────────────────
//  PRODUCT GRID (Listing Page)
// ─────────────────────────────────────────
function renderProductGrid() {
  const grid = document.getElementById('product-grid');
  if (!grid) return;
  const countEl = document.getElementById('product-count');
  const { page, perPage } = currentPagination;
  const start = (page - 1) * perPage;
  const paginated = filteredProducts.slice(start, start + perPage);

  grid.innerHTML = paginated.map(p => buildProductCard(p)).join('');
  if (countEl) countEl.textContent = `Hiển thị ${filteredProducts.length} sản phẩm`;
  renderPagination();
}

function renderPagination() {
  const total = Math.ceil(filteredProducts.length / currentPagination.perPage);
  const pg = currentPagination.page;
  const el = document.getElementById('pagination');
  if (total <= 1) { el.innerHTML = ''; return; }

  let html = `<button class="pagination-btn prev" onclick="goToPage(${pg - 1})" ${pg === 1 ? 'disabled style="opacity:.4"' : ''}>‹</button>`;
  for (let i = 1; i <= total; i++) {
    html += `<button class="pagination-btn ${i === pg ? 'active' : ''}" onclick="goToPage(${i})">${i}</button>`;
  }
  html += `<button class="pagination-btn next" onclick="goToPage(${pg + 1})" ${pg === total ? 'disabled style="opacity:.4"' : ''}>›</button>`;
  el.innerHTML = html;
}

function goToPage(page) {
  const total = Math.ceil(filteredProducts.length / currentPagination.perPage);
  if (page < 1 || page > total) return;
  currentPagination.page = page;
  renderProductGrid();
  document.getElementById('page-products').scrollIntoView({ behavior: 'smooth' });
}

function applyFilters() {
  const checkedCategories = [...document.querySelectorAll('#fg-category input[type=checkbox]:checked')].map(c => c.value);
  const minPrice = parseFloat(document.getElementById('price-min').value) || 0;
  const maxPrice = parseFloat(document.getElementById('price-max').value) || Infinity;

  filteredProducts = PRODUCTS.filter(p => {
    const catOk = checkedCategories.length === 0 || checkedCategories.includes(p.category);
    const colorOk = selectedColorFilters.length === 0; // simplified
    const priceOk = p.price >= minPrice && p.price <= maxPrice;
    return catOk && colorOk && priceOk;
  });
  currentPagination.page = 1;
  renderProductGrid();
}

function clearFilters() {
  document.querySelectorAll('#fg-category input[type=checkbox]').forEach(c => c.checked = false);
  document.getElementById('price-min').value = '';
  document.getElementById('price-max').value = '';
  selectedColorFilters = [];
  document.querySelectorAll('.color-swatch').forEach(s => s.classList.remove('selected'));
  filteredProducts = [...PRODUCTS];
  currentPagination.page = 1;
  renderProductGrid();
}

function toggleColorFilter(color, el) {
  el.classList.toggle('selected');
  if (selectedColorFilters.includes(color)) {
    selectedColorFilters = selectedColorFilters.filter(c => c !== color);
  } else {
    selectedColorFilters.push(color);
  }
  applyFilters();
}

function sortProducts(method) {
  switch (method) {
    case 'price-asc': filteredProducts.sort((a, b) => a.price - b.price); break;
    case 'price-desc': filteredProducts.sort((a, b) => b.price - a.price); break;
    case 'popular': filteredProducts.sort((a, b) => b.reviews - a.reviews); break;
    default: filteredProducts.sort((a, b) => b.id - a.id);
  }
  currentPagination.page = 1;
  renderProductGrid();
}

function setGridCols(cols, btn) {
  document.querySelectorAll('.grid-toggle-btn').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
  const grid = document.getElementById('product-grid');
  grid.style.gridTemplateColumns = `repeat(${cols}, 1fr)`;
}

function toggleFilterGroup(id) {
  document.getElementById(id).classList.toggle('collapsed');
}

function updatePriceMax(value) {
  document.getElementById('price-max').value = value;
  applyFilters();
}

// ─────────────────────────────────────────
//  PRODUCT DETAIL
// ─────────────────────────────────────────
function openProductDetail(productId) {
  const product = PRODUCTS.find(p => p.id === productId);
  if (!product) return;
  currentProductId = productId;

  document.getElementById('detail-breadcrumb-name').textContent = product.name;
  document.getElementById('detail-name').textContent = product.name;
  document.getElementById('detail-description').textContent = product.description;

  const priceEl = document.getElementById('detail-price');
  if (product.originalPrice) {
    const pct = Math.round((1 - product.price / product.originalPrice) * 100);
    priceEl.innerHTML = `<span class="price-sale-main">${formatPrice(product.price)}</span><span class="price-crossed">${formatPrice(product.originalPrice)}</span><span class="price-discount">-${pct}%</span>`;
  } else {
    priceEl.innerHTML = `<span class="price-main">${formatPrice(product.price)}</span>`;
  }

  document.getElementById('detail-stock').textContent = product.stock > 10 ? '● Còn hàng' : product.stock > 0 ? `● Sắp hết — chỉ còn ${product.stock}` : '○ Hết hàng';
  document.getElementById('detail-qty').value = 1;

  // Thumbs
  const thumbsEl = document.getElementById('gallery-thumbs');
  thumbsEl.innerHTML = Array.from({ length: 4 }, (_, i) => {
    const thumbContent = product.image
      ? `<img src="${product.image}" alt="${product.name}" style="width:100%;height:100%;object-fit:cover">`
      : `<div style="width:100%;height:100%;background:${product.bg}"></div>`;
    return `<div class="gallery-thumb ${i === 0 ? 'active' : ''}" onclick="selectThumb(this, ${i})">${thumbContent}</div>`;
  }).join('');

  // Main gallery image
  const mainImgEl = document.getElementById('gallery-main-img');
  mainImgEl.style.background = product.bg;
  mainImgEl.innerHTML = product.image
    ? `<img src="${product.image}" alt="${product.name}" style="width:100%;height:100%;object-fit:cover">`
    : '';

  // Colors
  document.getElementById('detail-colors').innerHTML = product.colors.map(c =>
    `<div class="color-swatch" style="background:${c};${c === '#fff' || c === '#f5f0ea' ? 'border:1px solid #e8e8e8' : ''}" onclick="selectDetailColor(this,'${c}')"></div>`
  ).join('');

  // Sizes
  document.getElementById('detail-sizes').innerHTML = product.sizes.map((s, i) =>
    `<button class="size-btn ${i === 1 ? 'selected' : ''}" onclick="selectDetailSize(this,'${s}')">${s}</button>`
  ).join('');
  document.getElementById('detail-selected-size').textContent = product.sizes[1];

  // Wishlist btn
  const wBtn = document.getElementById('detail-wishlist-btn');
  wBtn.classList.toggle('active', wishlist.includes(productId));

  navigate('product-detail');
}

function selectThumb(el, index) {
  document.querySelectorAll('.gallery-thumb').forEach(t => t.classList.remove('active'));
  el.classList.add('active');
}

function selectDetailColor(el, color) {
  document.querySelectorAll('#detail-colors .color-swatch').forEach(s => s.classList.remove('selected'));
  el.classList.add('selected');
}

function selectDetailSize(el, size) {
  document.querySelectorAll('#detail-sizes .size-btn').forEach(b => b.classList.remove('selected'));
  el.classList.add('selected');
  document.getElementById('detail-selected-size').textContent = size;
}

function changeDetailQty(delta) {
  const input = document.getElementById('detail-qty');
  const newVal = Math.max(1, Math.min(10, parseInt(input.value || 1) + delta));
  input.value = newVal;
}

function addDetailToCart() {
  const product = PRODUCTS.find(p => p.id === currentProductId);
  if (!product) return;
  const qty = parseInt(document.getElementById('detail-qty').value) || 1;
  const size = document.getElementById('detail-selected-size').textContent;
  addToCartItem(product, qty, size);
}

function toggleDetailWishlist() {
  if (!currentProductId) return;
  toggleWishlist(currentProductId);
  const wBtn = document.getElementById('detail-wishlist-btn');
  wBtn.classList.toggle('active', wishlist.includes(currentProductId));
}

// ─────────────────────────────────────────
//  QUICK VIEW MODAL
// ─────────────────────────────────────────
function openQuickView(productId) {
  const product = PRODUCTS.find(p => p.id === productId);
  if (!product) return;
  modalProductId = productId;

  document.getElementById('modal-name').textContent = product.name;
  document.getElementById('modal-image').style.background = product.bg;
  document.getElementById('modal-image').textContent = product.name;

  const priceWrap = document.getElementById('modal-price-wrap');
  if (product.originalPrice) {
    priceWrap.innerHTML = `<span class="price-sale-main">${formatPrice(product.price)}</span><span class="price-crossed" style="margin-left:8px">${formatPrice(product.originalPrice)}</span>`;
  } else {
    priceWrap.innerHTML = `<span class="price-main">${formatPrice(product.price)}</span>`;
  }

  document.getElementById('modal-colors').innerHTML = product.colors.map(c =>
    `<div class="color-swatch" style="background:${c};${c === '#fff' || c === '#f5f0ea' ? 'border:1px solid #e8e8e8' : ''}" onclick="selectModalColor(this)"></div>`
  ).join('');

  document.getElementById('modal-sizes').innerHTML = product.sizes.map((s, i) =>
    `<button class="size-btn ${i === 1 ? 'selected' : ''}" onclick="selectModalSize(this,'${s}')">${s}</button>`
  ).join('');
  document.getElementById('modal-selected-size').textContent = product.sizes[1];
  document.getElementById('modal-qty').value = 1;

  document.getElementById('modal-overlay').classList.add('open');
  document.body.style.overflow = 'hidden';
}

function closeQuickView() {
  document.getElementById('modal-overlay').classList.remove('open');
  document.body.style.overflow = '';
  modalProductId = null;
}

function closeModal(e) {
  if (e.target === document.getElementById('modal-overlay')) closeQuickView();
}

function selectModalColor(el) {
  document.querySelectorAll('#modal-colors .color-swatch').forEach(s => s.classList.remove('selected'));
  el.classList.add('selected');
}

function selectModalSize(el, size) {
  document.querySelectorAll('#modal-sizes .size-btn').forEach(b => b.classList.remove('selected'));
  el.classList.add('selected');
  document.getElementById('modal-selected-size').textContent = size;
}

function changeModalQty(delta) {
  const input = document.getElementById('modal-qty');
  input.value = Math.max(1, Math.min(10, parseInt(input.value || 1) + delta));
}

function addModalToCart() {
  const product = PRODUCTS.find(p => p.id === modalProductId);
  if (!product) return;
  const qty = parseInt(document.getElementById('modal-qty').value) || 1;
  const size = document.getElementById('modal-selected-size').textContent;
  addToCartItem(product, qty, size);
  closeQuickView();
}

function addModalToWishlist() {
  if (!modalProductId) return;
  toggleWishlist(modalProductId);
  closeQuickView();
}

// ─────────────────────────────────────────
//  CART
// ─────────────────────────────────────────
function addToCartItem(product, qty = 1, size = 'M') {
  const key = `${product.id}-${size}`;
  const existing = cart.find(i => i.key === key);
  if (existing) {
    existing.qty = Math.min(existing.qty + qty, 10);
  } else {
    cart.push({ key, id: product.id, name: product.name, price: product.price, bg: product.bg, size, qty });
  }
  updateCartUI();
  showToast(`${product.name} đã thêm vào giỏ hàng`, 'success');
}

function quickAddToCart(productId) {
  const product = PRODUCTS.find(p => p.id === productId);
  if (!product) return;
  addToCartItem(product, 1, 'M');
}

function removeFromCart(key) {
  cart = cart.filter(i => i.key !== key);
  updateCartUI();
  if (currentPage === 'cart') renderCartPage();
}

function updateCartQty(key, delta) {
  const item = cart.find(i => i.key === key);
  if (!item) return;
  item.qty += delta;
  if (item.qty <= 0) cart = cart.filter(i => i.key !== key);
  updateCartUI();
  if (currentPage === 'cart') renderCartPage();
}

function updateCartUI() {
  const total = cart.reduce((s, i) => s + i.qty, 0);
  document.getElementById('cart-count').textContent = total;
  document.getElementById('cart-sidebar-count').textContent = total;

  const body = document.getElementById('cart-sidebar-body');
  if (cart.length === 0) {
    body.innerHTML = '<div style="text-align:center;padding:60px 0;color:#999;font-size:14px">Giỏ hàng trống</div>';
    document.getElementById('cart-sidebar-total').textContent = '0₫';
    return;
  }

  body.innerHTML = cart.map(item => `
    <div class="cart-sidebar-item">
      <div class="cart-sidebar-item-image img-placeholder" style="background:${item.bg}"></div>
      <div class="cart-sidebar-item-info">
        <h4>${item.name}</h4>
        <div class="cart-sidebar-item-variants">Size ${item.size}</div>
        <div style="display:flex;align-items:center;gap:8px">
          <button class="qty-btn" style="background:#f5f5f5;border:none;width:24px;height:24px;cursor:pointer" onclick="updateCartQty('${item.key}',-1)">−</button>
          <span style="font-size:13px;min-width:20px;text-align:center">${item.qty}</span>
          <button class="qty-btn" style="background:#f5f5f5;border:none;width:24px;height:24px;cursor:pointer" onclick="updateCartQty('${item.key}',1)">+</button>
        </div>
      </div>
      <div>
        <div class="cart-sidebar-item-price">${formatPrice(item.price * item.qty)}</div>
        <button style="background:none;border:none;color:#ccc;cursor:pointer;margin-top:8px;display:block" onclick="removeFromCart('${item.key}')">✕</button>
      </div>
    </div>`).join('');

  const subtotal = cart.reduce((s, i) => s + i.price * i.qty, 0);
  document.getElementById('cart-sidebar-total').textContent = formatPrice(subtotal);
}

function toggleCartSidebar() {
  document.getElementById('cart-sidebar').classList.toggle('open');
  document.getElementById('cart-overlay').classList.toggle('open');
}

function renderCartPage() {
  const container = document.getElementById('cart-page-items');
  if (cart.length === 0) {
    container.innerHTML = `
      <div style="text-align:center;padding:80px 0">
        <p style="color:#999;margin-bottom:24px">Giỏ hàng của bạn đang trống</p>
        <button class="btn btn--primary" onclick="navigate('products')">Tiếp tục mua sắm</button>
      </div>`;
    updateCartSummary(0, 0);
    return;
  }

  container.innerHTML = cart.map(item => `
    <div class="cart-item">
      <div class="cart-item-image img-placeholder" style="background:${item.bg}"></div>
      <div class="cart-item-info">
        <h4>${item.name}</h4>
        <div class="brand">MINIMAL</div>
        <div class="variants">Size: ${item.size}</div>
      </div>
      <div class="cart-item-price">${formatPrice(item.price)}</div>
      <div>
        <div class="quantity-selector">
          <button class="quantity-btn" onclick="updateCartQty('${item.key}',-1)">−</button>
          <input class="quantity-input" type="number" value="${item.qty}" min="1" max="10" style="height:36px" onchange="setCartQty('${item.key}',this.value)">
          <button class="quantity-btn" onclick="updateCartQty('${item.key}',1)">+</button>
        </div>
      </div>
      <div class="cart-item-total">${formatPrice(item.price * item.qty)}</div>
      <button class="cart-item-remove" onclick="removeFromCart('${item.key}')">✕</button>
    </div>`).join('');

  const subtotal = cart.reduce((s, i) => s + i.price * i.qty, 0);
  const discount = appliedVoucher ? Math.round(subtotal * appliedVoucher.pct) : 0;
  updateCartSummary(subtotal, discount);
}

function updateCartSummary(subtotal, discount) {
  const total = subtotal - discount;
  document.getElementById('cart-subtotal').textContent = formatPrice(subtotal);
  document.getElementById('cart-discount').textContent = '-' + formatPrice(discount);
  document.getElementById('cart-total-price').textContent = formatPrice(total);
  document.getElementById('cart-shipping').textContent = subtotal > 500000 ? 'Miễn phí' : '30.000₫';
}

function setCartQty(key, value) {
  const item = cart.find(i => i.key === key);
  if (!item) return;
  item.qty = Math.max(1, Math.min(10, parseInt(value) || 1));
  updateCartUI();
  renderCartPage();
}

function applyVoucher() {
  const code = document.getElementById('voucher-input').value.trim().toUpperCase();
  const msgEl = document.getElementById('voucher-msg');
  const vouchers = { 'MINIMAL20': 0.20, 'SALE15': 0.15, 'FREESHIP': 0 };

  if (vouchers[code] !== undefined) {
    appliedVoucher = { code, pct: vouchers[code] };
    msgEl.textContent = code === 'FREESHIP' ? '✓ Miễn phí vận chuyển!' : `✓ Giảm ${vouchers[code] * 100}% toàn bộ đơn hàng`;
    msgEl.className = 'voucher-message success';
    renderCartPage();
  } else {
    appliedVoucher = null;
    msgEl.textContent = 'Mã giảm giá không hợp lệ hoặc hết hạn';
    msgEl.className = 'voucher-message error';
  }
}

// ─────────────────────────────────────────
//  CHECKOUT
// ─────────────────────────────────────────
function renderCheckoutSummary() {
  const subtotal = cart.reduce((s, i) => s + i.price * i.qty, 0);
  const list = document.getElementById('checkout-items-list');
  list.innerHTML = cart.map(item => `
    <div style="display:flex;justify-content:space-between;padding:8px 0;font-size:13px;border-bottom:1px solid #f5f5f5">
      <span>${item.name} × ${item.qty}</span>
      <span>${formatPrice(item.price * item.qty)}</span>
    </div>`).join('');
  document.getElementById('checkout-subtotal').textContent = formatPrice(subtotal);
  document.getElementById('checkout-total').textContent = formatPrice(subtotal);
}

function selectShipping(el) {
  document.querySelectorAll('.shipping-method').forEach(m => m.classList.remove('selected'));
  el.classList.add('selected');
  el.querySelector('input[type=radio]').checked = true;
}

function selectPayment(el) {
  document.querySelectorAll('.payment-method').forEach(m => m.classList.remove('selected'));
  el.classList.add('selected');
  el.querySelector('input[type=radio]').checked = true;
}

function placeOrder() {
  if (cart.length === 0) { showToast('Giỏ hàng trống!', 'error'); return; }
  showLoading();
  setTimeout(() => {
    hideLoading();
    orderCount++;
    document.getElementById('success-order-code').textContent = `#MN-2025-00${orderCount}`;
    cart = [];
    appliedVoucher = null;
    updateCartUI();
    navigate('order-success');
  }, 1500);
}

function renderOrderSuccess() {
  const subtotal = 2580000; // mock
  document.getElementById('success-total').textContent = formatPrice(subtotal);
  document.getElementById('success-items-list').innerHTML = `<div style="font-size:13px;color:#666;padding:8px 0;border-bottom:1px solid #f5f5f5">Áo sơ mi Linen × 1 — ${formatPrice(1290000)}</div><div style="font-size:13px;color:#666;padding:8px 0;border-bottom:1px solid #f5f5f5">Chân váy Chữ A × 2 — ${formatPrice(1380000)}</div>`;
}

// ─────────────────────────────────────────
//  WISHLIST
// ─────────────────────────────────────────
function toggleWishlist(productId) {
  const idx = wishlist.indexOf(productId);
  const product = PRODUCTS.find(p => p.id === productId);
  if (idx === -1) {
    wishlist.push(productId);
    showToast(`${product.name} đã thêm vào wishlist`, 'success');
  } else {
    wishlist.splice(idx, 1);
    showToast(`Đã xóa khỏi wishlist`);
  }
  if (currentPage === 'wishlist') renderWishlistPage();
}

function renderWishlistPage() {
  const count = document.getElementById('wishlist-count');
  const content = document.getElementById('wishlist-content');
  count.textContent = `${wishlist.length} sản phẩm`;

  if (wishlist.length === 0) {
    content.innerHTML = `
      <div class="wishlist-empty">
        <div class="wishlist-empty-icon">♡</div>
        <h4>Wishlist trống</h4>
        <p>Thêm sản phẩm yêu thích của bạn vào đây</p>
        <button class="btn btn--primary" onclick="navigate('products')">Khám phá sản phẩm</button>
      </div>`;
    return;
  }

  const items = PRODUCTS.filter(p => wishlist.includes(p.id));
  content.innerHTML = `<div class="wishlist-grid">${items.map(p => buildProductCard(p, true)).join('')}</div>`;
}

// ─────────────────────────────────────────
//  SEARCH
// ─────────────────────────────────────────
function toggleSearch() {
  const overlay = document.getElementById('search-overlay');
  overlay.classList.toggle('open');
  if (overlay.classList.contains('open')) {
    setTimeout(() => document.getElementById('search-input').focus(), 100);
  } else {
    document.getElementById('search-input').value = '';
    document.getElementById('search-suggestions').innerHTML = '';
  }
}

function handleSearch(query) {
  const suggestEl = document.getElementById('search-suggestions');
  if (!query.trim()) { suggestEl.innerHTML = ''; return; }
  const results = PRODUCTS.filter(p => p.name.toLowerCase().includes(query.toLowerCase())).slice(0, 5);
  if (results.length === 0) {
    suggestEl.innerHTML = `<div style="color:#999;font-size:14px;padding:8px 0">Không tìm thấy kết quả</div>`;
    return;
  }
  suggestEl.innerHTML = results.map(p => `
    <div class="search-suggestion-item" onclick="openProductDetail(${p.id});toggleSearch()">
      <div style="width:32px;height:40px;background:${p.bg};flex-shrink:0"></div>
      <div><div style="font-size:14px">${p.name}</div><div style="font-size:12px;color:#999">${formatPrice(p.price)}</div></div>
    </div>`).join('');
}

// ─────────────────────────────────────────
//  FLASH SALE & CAROUSEL
// ─────────────────────────────────────────
//  HOME PAGE SECTIONS
// ─────────────────────────────────────────
function renderBestSellers() {
  const grid = document.getElementById('best-sellers-grid');
  if (!grid) return;

  const items = BEST_SELLER_IDS.map(id => PRODUCTS.find(p => p.id === id)).filter(Boolean).slice(0, 8);
  grid.innerHTML = items.map(p => buildProductCard(p)).join('');
}

function renderFlashSale() {
  const grid = document.getElementById('flash-sale-grid');
  if (!grid) return;

  const items = FLASH_SALE_IDS.map(id => PRODUCTS.find(p => p.id === id)).filter(Boolean);
  grid.innerHTML = items.map(p => buildProductCard(p)).join('');
}

function renderCarousel() {
  // Deprecated - using grid now
  renderBestSellers();
}

function carouselPrev() { moveCarousel(-1); }
function carouselNext() { moveCarousel(1); }

function moveCarousel(dir) {
  const track = document.getElementById('carousel-track');
  const items = track.children;
  const maxIndex = Math.max(0, items.length - 4);
  carouselIndex = Math.max(0, Math.min(maxIndex, carouselIndex + dir));
  const cardWidth = track.parentElement.offsetWidth / 4 + 6;
  track.style.transform = `translateX(-${carouselIndex * cardWidth}px)`;
  updateCarouselDots();
}

function goToCarouselDot(index) {
  carouselIndex = index * 4;
  moveCarousel(0);
}

function updateCarouselDots() {
  const dotIndex = Math.floor(carouselIndex / 4);
  document.querySelectorAll('.carousel-dot').forEach((d, i) => d.classList.toggle('active', i === dotIndex));
}

// ─────────────────────────────────────────
//  COUNTDOWN
// ─────────────────────────────────────────
function initCountdown() {
  const cdHoursElem = document.getElementById('cd-hours');
  if (!cdHoursElem) return;

  const end = new Date();
  end.setHours(end.getHours() + 2, end.getMinutes() + 34, end.getSeconds() + 59);

  clearInterval(countdownInterval);
  countdownInterval = setInterval(() => {
    const diff = end - new Date();
    if (diff <= 0) { clearInterval(countdownInterval); return; }
    const h = Math.floor(diff / 3600000);
    const m = Math.floor((diff % 3600000) / 60000);
    const s = Math.floor((diff % 60000) / 1000);
    cdHoursElem.textContent = String(h).padStart(2, '0');
    document.getElementById('cd-mins').textContent = String(m).padStart(2, '0');
    document.getElementById('cd-secs').textContent = String(s).padStart(2, '0');
  }, 1000);
}

// ─────────────────────────────────────────
//  AUTH
// ─────────────────────────────────────────
function switchAuthTab(tab, el) {
  document.querySelectorAll('.auth-tab').forEach(t => t.classList.remove('active'));
  el.classList.add('active');
  document.getElementById('login-form').style.display = tab === 'login' ? 'flex' : 'none';
  document.getElementById('register-form').style.display = tab === 'register' ? 'flex' : 'none';
}

function togglePasswordVisibility(inputId) {
  const input = document.getElementById(inputId);
  input.type = input.type === 'password' ? 'text' : 'password';
}

function handleLogin(e) {
  e.preventDefault();
  showLoading();
  setTimeout(() => {
    hideLoading();
    showToast('Đăng nhập thành công!', 'success');
    navigate('profile');
  }, 1200);
}

function handleRegister(e) {
  e.preventDefault();
  const p1 = document.getElementById('reg-pass').value;
  const p2 = document.getElementById('reg-pass2').value;
  if (p1 !== p2) { showToast('Mật khẩu xác nhận không khớp!', 'error'); return; }
  showLoading();
  setTimeout(() => {
    hideLoading();
    showToast('Đăng ký thành công! Vui lòng đăng nhập.', 'success');
    switchAuthTab('login', document.querySelector('.auth-tab'));
  }, 1200);
}

function handleLogout() {
  showToast('Đã đăng xuất');
  navigate('home');
}

// ─────────────────────────────────────────
//  PROFILE
// ─────────────────────────────────────────
function toggleEditProfile(btn) {
  const inputs = document.querySelectorAll('#profile-fields .form-input');
  const saveBtn = document.getElementById('profile-save-btn');
  const isEditing = btn.textContent === 'Lưu';
  if (isEditing) {
    inputs.forEach(i => i.setAttribute('readonly', ''));
    btn.textContent = 'Chỉnh sửa';
    saveBtn.style.display = 'none';
    showToast('Đã lưu thông tin', 'success');
  } else {
    inputs.forEach(i => i.removeAttribute('readonly'));
    btn.textContent = 'Lưu';
    saveBtn.style.display = 'block';
  }
}

function saveProfile() {
  showToast('Đã lưu thông tin tài khoản', 'success');
  document.querySelectorAll('#profile-fields .form-input').forEach(i => i.setAttribute('readonly', ''));
  document.getElementById('profile-save-btn').style.display = 'none';
}

function switchProfileTab(tab, el) {
  document.querySelectorAll('.profile-sidebar-link').forEach(l => l.classList.remove('active'));
  el.classList.add('active');
}

// ─────────────────────────────────────────
//  ORDERS
// ─────────────────────────────────────────
function renderOrders(filter) {
  const orders = filter === 'all' ? SAMPLE_ORDERS : SAMPLE_ORDERS.filter(o => o.status === filter);
  const container = document.getElementById('orders-list');

  if (orders.length === 0) {
    container.innerHTML = '<div style="text-align:center;padding:64px 0;color:#999">Không có đơn hàng nào</div>';
    return;
  }

  container.innerHTML = orders.map(order => `
    <div class="order-card">
      <div class="order-card-header">
        <div class="order-card-meta">
          <span class="order-id">${order.id}</span>
          <span>Ngày đặt: ${order.date}</span>
        </div>
        <span class="order-status-badge ${order.status}">${ORDER_STATUS_LABELS[order.status] || order.status}</span>
      </div>
      <div class="order-card-body">
        <div class="order-items-preview">
          ${order.items.map(item => `<div class="order-item-thumb img-placeholder" style="background:${item.bg}"></div>`).join('')}
          <div style="margin-left:12px">
            ${order.items.map(item => `<div style="font-size:13px;margin-bottom:4px">${item.name} × ${item.qty}</div>`).join('')}
          </div>
        </div>
        ${order.status === 'shipped' || order.status === 'delivered' ? `
        <div class="order-timeline" style="margin-top:16px">
          ${buildTimeline(order.status)}
        </div>` : ''}
      </div>
      <div class="order-card-footer">
        <span class="order-total">Tổng: <strong>${formatPrice(order.total)}</strong></span>
        <div class="order-actions">
          ${order.status === 'delivered' ? `<button class="btn btn--outline btn--sm" onclick="navigate('returns')">Đổi/Trả</button>` : ''}
          ${order.status === 'pending' ? `<button class="btn btn--ghost btn--sm" style="color:#e63946;border-color:#e63946">Hủy đơn</button>` : ''}
          <button class="btn btn--ghost btn--sm" onclick="showToast('Tính năng đang phát triển')">Xem chi tiết</button>
        </div>
      </div>
    </div>`).join('');
}

function buildTimeline(status) {
  const steps = [
    { label: 'Đặt hàng', desc: 'Đơn hàng đã được tạo', done: true },
    { label: 'Xác nhận', desc: 'Đơn hàng đã xác nhận', done: true },
    { label: 'Đóng gói', desc: 'Đang chuẩn bị hàng', done: status === 'shipped' || status === 'delivered' },
    { label: 'Giao hàng', desc: 'Đang trên đường giao', active: status === 'shipped', done: status === 'delivered' },
    { label: 'Hoàn thành', desc: 'Đã nhận hàng', done: status === 'delivered' }
  ];
  return steps.map(s => `
    <div class="timeline-step ${s.done ? 'done' : s.active ? 'active' : ''}">
      <div class="timeline-dot"><div class="timeline-dot-inner"></div></div>
      <div class="timeline-content-title">${s.label}</div>
      <div class="timeline-content-desc">${s.desc}</div>
    </div>`).join('');
}

function filterOrders(status, el) {
  document.querySelectorAll('.status-tab').forEach(t => t.classList.remove('active'));
  el.classList.add('active');
  renderOrders(status);
}

// ─────────────────────────────────────────
//  RETURNS & REVIEWS
// ─────────────────────────────────────────
function switchReturnsTab(tab, el) {
  document.querySelectorAll('.mode-tab').forEach(t => t.classList.remove('active'));
  el.classList.add('active');
  document.getElementById('return-form-panel').style.display = tab === 'return' ? 'block' : 'none';
  document.getElementById('review-form-panel').style.display = tab === 'review' ? 'block' : 'none';
}

function submitReturn(e) {
  e.preventDefault();
  showToast('Yêu cầu đổi/trả đã được gửi. Chúng tôi sẽ liên hệ trong 24h.', 'success');
}

function submitReview(e) {
  e.preventDefault();
  showToast('Đánh giá của bạn đã được gửi. Cảm ơn bạn!', 'success');
}

let selectedStars = 0;
function setStars(n) {
  selectedStars = n;
  const stars = document.querySelectorAll('#star-rating span');
  stars.forEach((s, i) => s.style.color = i < n ? '#2c2c2c' : '#e8e8e8');
}

// ─────────────────────────────────────────
//  VIRTUAL TRY-ON
// ─────────────────────────────────────────
function switchTryOnMode(mode, el) {
  document.querySelectorAll('.mode-tab').forEach(t => t.classList.remove('active'));
  el.classList.add('active');
  const cameraMode = document.getElementById('tryon-camera-mode');
  const uploadArea = document.getElementById('tryon-upload-area');
  cameraMode.closest ? null : null;
  if (mode === 'camera') {
    document.getElementById('tryon-camera-area').parentElement.style.display = '';
    document.getElementById('tryon-upload-area').style.display = 'none';
  } else {
    document.getElementById('tryon-camera-area').parentElement.style.display = 'none';
    document.getElementById('tryon-upload-area').style.display = 'block';
  }
}

function startCamera() {
  if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
    navigator.mediaDevices.getUserMedia({ video: { facingMode: 'user' } }).then(stream => {
      const video = document.getElementById('tryon-video');
      video.srcObject = stream;
      video.style.display = 'block';
      // Add a capture button below video
      let captureBtn = document.getElementById('tryon-capture-btn');
      if (!captureBtn) {
        captureBtn = document.createElement('button');
        captureBtn.id = 'tryon-capture-btn';
        captureBtn.className = 'btn btn--primary btn--sm';
        captureBtn.style.marginTop = '8px';
        captureBtn.textContent = 'Chụp ảnh';
        captureBtn.onclick = captureTryOnPhoto;
        video.parentNode.insertBefore(captureBtn, video.nextSibling);
      }
      document.getElementById('tryon-camera-btn').textContent = 'Camera đang bật';
    }).catch(() => showToast('Không thể truy cập camera', 'error'));
  } else {
    showToast('Trình duyệt không hỗ trợ camera', 'error');
  }
}

function captureTryOnPhoto() {
  const video = document.getElementById('tryon-video');
  const canvas = document.getElementById('tryon-canvas');
  canvas.width = video.videoWidth;
  canvas.height = video.videoHeight;
  canvas.getContext('2d').drawImage(video, 0, 0);
  const dataUrl = canvas.toDataURL('image/png');
  const preview = document.getElementById('tryon-preview');
  preview.innerHTML = `<img src="${dataUrl}" style="width:100%;height:100%;object-fit:cover">`;
  // Stop camera
  const stream = video.srcObject;
  if (stream) stream.getTracks().forEach(t => t.stop());
  video.style.display = 'none';
  const captureBtn = document.getElementById('tryon-capture-btn');
  if (captureBtn) captureBtn.remove();
  document.getElementById('tryon-camera-btn').textContent = 'Chụp ảnh';
  showToast('Đã chụp ảnh! Chọn sản phẩm để thử.', 'success');
}

function handleTryOnUpload(e) {
  const file = e.target.files[0];
  if (!file) return;
  const url = URL.createObjectURL(file);
  const preview = document.getElementById('tryon-preview');
  preview.innerHTML = `<img src="${url}" style="width:100%;height:100%;object-fit:cover">`;
  showToast('Ảnh đã tải lên! Chọn sản phẩm để thử.', 'success');
}

function processTryOn() {
  showToast('Đang xử lý hình ảnh...', 'info');
  const preview = document.getElementById('tryon-preview');
  preview.innerHTML = `<div class="spinner" style="width:32px;height:32px"></div><span style="font-size:12px;color:#999;margin-top:12px">Đang xử lý AI...</span>`;
  setTimeout(() => {
    preview.innerHTML = `<div class="img-placeholder" style="width:100%;height:100%;flex-direction:column;gap:12px"><svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg><span style="font-size:12px;color:#ccc">Kết quả thử đồ</span></div>`;
    showToast('Hoàn thành! Đây là kết quả thử đồ của bạn.', 'success');
  }, 2000);
}

function saveTryOnImage() { showToast('Đã lưu ảnh vào thư viện', 'success'); }
function shareTryOnImage() { showToast('Tính năng chia sẻ đang phát triển'); }
function addTryOnToCart() { showToast('Vui lòng chọn sản phẩm trước'); }

function renderTryOnItems() {
  const container = document.getElementById('tryon-items');
  container.innerHTML = PRODUCTS.slice(0, 4).map((p, i) => `
    <div class="tryon-item img-placeholder ${i === 0 ? 'selected' : ''}" style="background:${p.bg};font-size:10px;color:#bbb" onclick="selectTryOnItem(this,'${p.id}')">${p.name}</div>`
  ).join('');
}

function selectTryOnItem(el, id) {
  document.querySelectorAll('.tryon-item').forEach(t => t.classList.remove('selected'));
  el.classList.add('selected');
}

// ─────────────────────────────────────────
//  ACCORDION
// ─────────────────────────────────────────
function toggleAccordion(header) {
  const item = header.closest('.accordion-item');
  const isOpen = item.classList.contains('open');
  document.querySelectorAll('.accordion-item.open').forEach(i => i.classList.remove('open'));
  if (!isOpen) item.classList.add('open');
}

// ─────────────────────────────────────────
//  NEWSLETTER
// ─────────────────────────────────────────
function handleNewsletter(e) {
  e.preventDefault();
  const email = e.target.querySelector('input').value;
  showLoading();
  setTimeout(() => {
    hideLoading();
    showToast(`Cảm ơn! Chúng tôi sẽ gửi tin tức đến ${email}`, 'success');
    e.target.reset();
  }, 800);
}

// ─────────────────────────────────────────
//  MOBILE NAV
// ─────────────────────────────────────────
function toggleMobileNav() {
  document.getElementById('mobile-nav').classList.toggle('open');
}

// ─────────────────────────────────────────
//  HEADER SCROLL
// ─────────────────────────────────────────
window.addEventListener('scroll', () => {
  const header = document.getElementById('site-header');
  header.classList.toggle('scrolled', window.pageYOffset > 50);
});

// ─────────────────────────────────────────
//  TOAST NOTIFICATIONS
// ─────────────────────────────────────────
function showToast(message, type = 'default') {
  const container = document.getElementById('toast-container');
  const icons = { success: '✓', error: '✕', warning: '!', default: 'i' };
  const toast = document.createElement('div');
  toast.className = `toast ${type !== 'default' ? type : ''}`;
  toast.innerHTML = `<span class="toast-icon">${icons[type] || 'i'}</span><span class="toast-message">${message}</span><span class="toast-close" onclick="this.parentElement.remove()">✕</span>`;
  container.appendChild(toast);
  setTimeout(() => {
    toast.classList.add('exiting');
    setTimeout(() => toast.remove(), 350);
  }, 3500);
}

// ─────────────────────────────────────────
//  LOADING
// ─────────────────────────────────────────
function showLoading() {
  const bar = document.getElementById('loading-bar');
  bar.classList.add('active');
}

function hideLoading() {
  document.getElementById('loading-bar').classList.remove('active');
}

// ─────────────────────────────────────────
//  UTILITIES
// ─────────────────────────────────────────
function formatPrice(n) {
  return new Intl.NumberFormat('vi-VN').format(n) + '₫';
}

// ─────────────────────────────────────────
//  SCROLL ANIMATIONS
// ─────────────────────────────────────────
function initScrollAnimations() {
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.style.animation = 'fadeInUp 0.5s ease forwards';
        observer.unobserve(entry.target);
      }
    });
  }, { threshold: 0.1, rootMargin: '0px 0px -50px 0px' });

  document.querySelectorAll('.product-card, .collection-card, .section-header').forEach(el => {
    el.style.opacity = '0';
    observer.observe(el);
  });
}

// Close search on Escape
document.addEventListener('keydown', e => {
  if (e.key === 'Escape') {
    closeQuickView();
    if (document.getElementById('search-overlay').classList.contains('open')) toggleSearch();
    if (document.getElementById('cart-sidebar').classList.contains('open')) toggleCartSidebar();
    if (document.getElementById('mobile-nav').classList.contains('open')) toggleMobileNav();
  }
});

// ─────────────────────────────────────────
//  INIT
// ─────────────────────────────────────────
window.addEventListener('DOMContentLoaded', () => {
  renderProductGrid();
  renderBestSellers();
  renderFlashSale();
  renderCarousel();
  initCountdown();
  setTimeout(initScrollAnimations, 300);
});
