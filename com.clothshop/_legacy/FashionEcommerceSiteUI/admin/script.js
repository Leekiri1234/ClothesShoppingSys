/* ============================================================
   MINIMAL ADMIN — Admin Dashboard JavaScript SPA
   Fashion Store | Vietnamese | No frameworks, no backend
   ============================================================ */

'use strict';

// ============================================================
//  MOCK DATA
// ============================================================

const mockStaff = [
  { id: 1, name: 'Nguyễn Thị Lan', email: 'lan.nguyen@minimal.vn', role: 'Admin', active: true, created: '2025-01-10', permissions: ['manage_products','manage_orders','manage_staff','view_reports'] },
  { id: 2, name: 'Trần Minh Khoa', email: 'khoa.tran@minimal.vn', role: 'Staff', active: true, created: '2025-03-05', permissions: ['manage_orders','manage_customers'] },
  { id: 3, name: 'Lê Thị Hoa', email: 'hoa.le@minimal.vn', role: 'Staff', active: true, created: '2025-04-18', permissions: ['manage_products','manage_inventory'] },
  { id: 4, name: 'Phạm Văn Đức', email: 'duc.pham@minimal.vn', role: 'Warehouse', active: true, created: '2025-05-22', permissions: ['manage_inventory'] },
  { id: 5, name: 'Vũ Thu Trang', email: 'trang.vu@minimal.vn', role: 'Staff', active: false, created: '2025-06-01', permissions: ['manage_orders'] },
  { id: 6, name: 'Ngô Quang Huy', email: 'huy.ngo@minimal.vn', role: 'Warehouse', active: true, created: '2025-07-14', permissions: ['manage_inventory','view_reports'] },
  { id: 7, name: 'Đinh Thị Mai', email: 'mai.dinh@minimal.vn', role: 'Staff', active: true, created: '2025-09-03', permissions: ['manage_products','manage_orders','manage_customers'] },
];

const mockProducts = [
  { id: 1, name: 'Áo Linen Cổ V', sku: 'ALV-001', category: 'Áo', price: 450000, comparePrice: 580000, stock: 84, status: 'active', featured: true, weight: 250, colors: ['Trắng','Kem','Xanh nhạt'], sizes: ['S','M','L','XL'], sold: 156 },
  { id: 2, name: 'Quần Wide-Leg Kaki', sku: 'QWK-002', category: 'Quần', price: 620000, comparePrice: 780000, stock: 45, status: 'active', featured: false, weight: 400, colors: ['Nâu','Đen','Be'], sizes: ['S','M','L'], sold: 98 },
  { id: 3, name: 'Váy Midi Floral', sku: 'VMF-003', category: 'Váy', price: 540000, comparePrice: 680000, stock: 32, status: 'active', featured: true, weight: 300, colors: ['Xanh hoa','Hồng hoa'], sizes: ['S','M','L','XL'], sold: 124 },
  { id: 4, name: 'Áo Thun Basic Cổ Tròn', sku: 'ATB-004', category: 'Áo', price: 280000, comparePrice: 350000, stock: 120, status: 'active', featured: false, weight: 180, colors: ['Trắng','Đen','Xám'], sizes: ['XS','S','M','L','XL'], sold: 203 },
  { id: 5, name: 'Túi Tote Canvas', sku: 'TTC-005', category: 'Phụ kiện', price: 350000, comparePrice: 420000, stock: 7, status: 'active', featured: false, weight: 350, colors: ['Kem','Đen'], sizes: ['One Size'], sold: 67 },
  { id: 6, name: 'Áo Sơ Mi Linen Dài Tay', sku: 'ASL-006', category: 'Áo', price: 520000, comparePrice: 660000, stock: 0, status: 'active', featured: false, weight: 280, colors: ['Trắng','Xanh pastel'], sizes: ['S','M','L','XL'], sold: 89 },
  { id: 7, name: 'Chân Váy A-Line', sku: 'CVA-007', category: 'Váy', price: 460000, comparePrice: 580000, stock: 3, status: 'active', featured: true, weight: 320, colors: ['Đen','Trắng','Camel'], sizes: ['S','M','L'], sold: 75 },
  { id: 8, name: 'Quần Short Linen', sku: 'QSL-008', category: 'Quần', price: 380000, comparePrice: 480000, stock: 55, status: 'hidden', featured: false, weight: 220, colors: ['Be','Xanh nhạt'], sizes: ['S','M','L'], sold: 42 },
  { id: 9, name: 'Nón Bucket Vải', sku: 'NBV-009', category: 'Phụ kiện', price: 220000, comparePrice: 280000, stock: 28, status: 'active', featured: false, weight: 150, colors: ['Đen','Kem'], sizes: ['One Size'], sold: 115 },
  { id: 10, name: 'Áo Croptop Rib', sku: 'ACR-010', category: 'Áo', price: 320000, comparePrice: 400000, stock: 62, status: 'draft', featured: false, weight: 200, colors: ['Trắng','Đen','Tím pastel'], sizes: ['XS','S','M','L'], sold: 0 },
];

const mockOrders = [
  { id: 'ORD-1001', customer: 'Nguyễn Thị Bích', customerEmail: 'bich@gmail.com', phone: '0901234567', address: '12 Lê Lợi, Q1, HCM', items: 3, total: 1230000, payment: 'Chuyển khoản', paymentStatus: 'paid', status: 'pending', created: '2026-03-07 08:12', note: 'Giao giờ hành chính' },
  { id: 'ORD-1002', customer: 'Trần Hoàng Long', customerEmail: 'long@gmail.com', phone: '0912345678', address: '45 Nguyễn Huệ, Q1, HCM', items: 1, total: 450000, payment: 'COD', paymentStatus: 'pending', status: 'confirmed', created: '2026-03-07 09:05', note: '' },
  { id: 'ORD-1003', customer: 'Lê Thị Kim Oanh', customerEmail: 'oanh@gmail.com', phone: '0932345678', address: '88 Trần Duy Hưng, CG, HN', items: 2, total: 980000, payment: 'Chuyển khoản', paymentStatus: 'paid', status: 'preparing', created: '2026-03-07 09:34', note: '' },
  { id: 'ORD-1004', customer: 'Phạm Quốc Anh', customerEmail: 'anh@gmail.com', phone: '0943456789', address: '22 Đinh Tiên Hoàng, BĐ, HCM', items: 4, total: 1860000, payment: 'VNPAY', paymentStatus: 'paid', status: 'shipping', created: '2026-03-06 15:20', note: '' },
  { id: 'ORD-1005', customer: 'Hoàng Minh Tuấn', customerEmail: 'tuan@gmail.com', phone: '0954567890', address: '5 Lý Thường Kiệt, HK, HN', items: 2, total: 760000, payment: 'COD', paymentStatus: 'pending', status: 'delivered', created: '2026-03-06 11:40', note: 'Cuối tuần giao được' },
  { id: 'ORD-1006', customer: 'Vũ Ngọc Hà', customerEmail: 'ha@gmail.com', phone: '0965678901', address: '130 Nguyễn Trãi, TB, HCM', items: 1, total: 280000, payment: 'Chuyển khoản', paymentStatus: 'paid', status: 'pending', created: '2026-03-07 10:15', note: '' },
  { id: 'ORD-1007', customer: 'Đỗ Thị Thùy', customerEmail: 'thuy@gmail.com', phone: '0976789012', address: '7 Hoàng Diệu, HH, HN', items: 3, total: 1540000, payment: 'VNPAY', paymentStatus: 'paid', status: 'confirmed', created: '2026-03-07 10:55', note: '' },
  { id: 'ORD-1008', customer: 'Bùi Văn Thắng', customerEmail: 'thang@gmail.com', phone: '0987890123', address: '99 Trần Phú, ĐN', items: 2, total: 880000, payment: 'COD', paymentStatus: 'pending', status: 'cancelled', created: '2026-03-05 09:00', note: '' },
];

const mockVouchers = [
  { id: 1, code: 'SUMMER20', type: 'percent', value: 20, minOrder: 500000, quantity: 80, used: 20, expiry: '2026-06-30', active: true },
  { id: 2, code: 'NEWUSER50K', type: 'fixed', value: 50000, minOrder: 300000, quantity: 200, used: 145, expiry: '2026-12-31', active: true },
  { id: 3, code: 'FLASH15', type: 'percent', value: 15, minOrder: 0, quantity: 50, used: 50, expiry: '2026-03-10', active: false },
  { id: 4, code: 'BIRTHDAY30', type: 'percent', value: 30, minOrder: 800000, quantity: 30, used: 8, expiry: '2026-04-15', active: true },
  { id: 5, code: 'FREE100K', type: 'fixed', value: 100000, minOrder: 1000000, quantity: 100, used: 67, expiry: '2026-05-31', active: true },
  { id: 6, code: 'LOYAL10', type: 'percent', value: 10, minOrder: 0, quantity: 999, used: 342, expiry: '2026-12-31', active: true },
];

const mockBanners = [
  { id: 1, title: 'Bộ sưu tập Hè 2026', link: '/collections/summer-2026', order: 1, active: true },
  { id: 2, title: 'Sale cuối tuần — Giảm 30%', link: '/sale', order: 2, active: true },
  { id: 3, title: 'Áo Linen mới nhất', link: '/products/linen', order: 3, active: false },
  { id: 4, title: 'Flash Sale 12/3', link: '/flash-sale', order: 4, active: true },
];

const mockCollections = [
  { id: 1, name: 'Bộ sưu tập Hè 2026', desc: 'Nhẹ nhàng, thoáng mát — phong cách mùa hè tối giản', count: 18, active: true },
  { id: 2, name: 'Minimalist Basics', desc: 'Những món đồ cơ bản không bao giờ lỗi mốt', count: 24, active: true },
  { id: 3, name: 'Work Wear', desc: 'Trang phục thanh lịch cho công sở', count: 12, active: true },
  { id: 4, name: 'Weekend Casual', desc: 'Thoải mái, trẻ trung cho những ngày cuối tuần', count: 15, active: false },
  { id: 5, name: 'Monochrome Edit', desc: 'Một màu, vô vàn phong cách', count: 10, active: true },
];

const mockPushHistory = [
  { id: 1, date: '2026-03-05 10:00', title: 'Flash Sale cuối tuần!', target: 'Tất cả', sent: 12450, openRate: '34.2%' },
  { id: 2, date: '2026-02-28 09:00', title: 'Bộ sưu tập Hè 2026 vừa ra mắt', target: 'Tất cả', sent: 12800, openRate: '28.7%' },
  { id: 3, date: '2026-02-20 14:00', title: 'Chúng tôi nhớ bạn!', target: 'Không hoạt động 30 ngày', sent: 2100, openRate: '41.5%' },
  { id: 4, date: '2026-02-14 08:00', title: 'Valentine — Mua 2 giảm 20%', target: 'Đã từng mua hàng', sent: 8650, openRate: '52.3%' },
];

const mockInventory = [
  { id: 1, product: 'Áo Linen Cổ V', variant: 'Trắng / M', sku: 'ALV-001-W-M', stock: 22 },
  { id: 2, product: 'Áo Linen Cổ V', variant: 'Kem / M', sku: 'ALV-001-K-M', stock: 18 },
  { id: 3, product: 'Áo Linen Cổ V', variant: 'Xanh nhạt / L', sku: 'ALV-001-X-L', stock: 8 },
  { id: 4, product: 'Quần Wide-Leg Kaki', variant: 'Nâu / M', sku: 'QWK-002-N-M', stock: 15 },
  { id: 5, product: 'Quần Wide-Leg Kaki', variant: 'Đen / L', sku: 'QWK-002-D-L', stock: 0 },
  { id: 6, product: 'Váy Midi Floral', variant: 'Xanh hoa / S', sku: 'VMF-003-X-S', stock: 5 },
  { id: 7, product: 'Áo Thun Basic', variant: 'Trắng / XS', sku: 'ATB-004-W-XS', stock: 40 },
  { id: 8, product: 'Áo Thun Basic', variant: 'Đen / L', sku: 'ATB-004-D-L', stock: 3 },
  { id: 9, product: 'Túi Tote Canvas', variant: 'Kem / One Size', sku: 'TTC-005-K-OS', stock: 7 },
  { id: 10, product: 'Áo Sơ Mi Linen', variant: 'Trắng / M', sku: 'ASL-006-W-M', stock: 0 },
  { id: 11, product: 'Chân Váy A-Line', variant: 'Đen / S', sku: 'CVA-007-D-S', stock: 3 },
  { id: 12, product: 'Nón Bucket Vải', variant: 'Đen / One Size', sku: 'NBV-009-D-OS', stock: 14 },
];

const mockPaymentQueue = [
  { id: 1, orderId: 'ORD-1001', customer: 'Nguyễn Thị Bích', amount: 1230000, method: 'Chuyển khoản', time: '2026-03-07 08:12', status: 'pending' },
  { id: 2, orderId: 'ORD-1003', customer: 'Lê Thị Kim Oanh', amount: 980000, method: 'Chuyển khoản', time: '2026-03-07 09:34', status: 'pending' },
  { id: 3, orderId: 'ORD-1006', customer: 'Vũ Ngọc Hà', amount: 280000, method: 'Chuyển khoản', time: '2026-03-07 10:15', status: 'pending' },
  { id: 4, orderId: 'ORD-0998', customer: 'Trần Minh Châu', amount: 670000, method: 'Chuyển khoản', time: '2026-03-06 16:42', status: 'confirmed' },
];

const mockRMA = [
  { id: 'RMA-001', orderId: 'ORD-0987', customer: 'Nguyễn Quang Minh', reason: 'Sản phẩm bị lỗi vải', type: 'return', amount: 620000, status: 'pending', created: '2026-03-06' },
  { id: 'RMA-002', orderId: 'ORD-0975', customer: 'Trần Thị Lan', reason: 'Sai size (đặt M nhận L)', type: 'exchange', amount: 0, status: 'approved', created: '2026-03-04' },
  { id: 'RMA-003', orderId: 'ORD-0960', customer: 'Lê Văn Hùng', reason: 'Màu khác ảnh', type: 'return', amount: 450000, status: 'completed', created: '2026-02-28' },
  { id: 'RMA-004', orderId: 'ORD-0942', customer: 'Phạm Thị Mai', reason: 'Không còn nhu cầu', type: 'return', amount: 280000, status: 'rejected', created: '2026-02-25' },
  { id: 'RMA-005', orderId: 'ORD-0990', customer: 'Hoàng Thị Nhung', reason: 'Đường may lỗi', type: 'exchange', amount: 0, status: 'pending', created: '2026-03-07' },
];

const mockRevenueMonthly = [
  { month: 'T9/25', revenue: 68000000, orders: 142 },
  { month: 'T10/25', revenue: 74500000, orders: 158 },
  { month: 'T11/25', revenue: 82300000, orders: 174 },
  { month: 'T12/25', revenue: 112000000, orders: 240 },
  { month: 'T1/26', revenue: 94600000, orders: 198 },
  { month: 'T2/26', revenue: 78200000, orders: 162 },
  { month: 'T3/26', revenue: 31400000, orders: 67 },
];

const mockWishlistData = [
  { id: 1, product: 'Áo Linen Cổ V', wishlistCount: 342, purchaseRate: '45.6%', potential: 69836000 },
  { id: 2, product: 'Váy Midi Floral', wishlistCount: 289, purchaseRate: '42.9%', potential: 55827000 },
  { id: 3, product: 'Quần Wide-Leg Kaki', wishlistCount: 245, purchaseRate: '40.0%', potential: 60900000 },
  { id: 4, product: 'Chân Váy A-Line', wishlistCount: 198, purchaseRate: '37.9%', potential: 34538000 },
  { id: 5, product: 'Áo Sơ Mi Linen', wishlistCount: 178, purchaseRate: '50.0%', potential: 46280000 },
  { id: 6, product: 'Áo Thun Basic', wishlistCount: 156, purchaseRate: '65.4%', potential: 28570000 },
  { id: 7, product: 'Nón Bucket Vải', wishlistCount: 134, purchaseRate: '85.8%', potential: 25394000 },
  { id: 8, product: 'Túi Tote Canvas', wishlistCount: 122, purchaseRate: '54.9%', potential: 23485000 },
  { id: 9, product: 'Quần Short Linen', wishlistCount: 98, purchaseRate: '42.9%', potential: 15947000 },
  { id: 10, product: 'Áo Croptop Rib', wishlistCount: 87, purchaseRate: '0%', potential: 0 },
];

const mockCustomers = [
  { id: 1, name: 'Nguyễn Thị Bích', email: 'bich@gmail.com', phone: '0901234567', orders: 12, spent: 8640000, joined: '2025-02-14', addresses: ['12 Lê Lợi, Q1, HCM'] },
  { id: 2, name: 'Trần Hoàng Long', email: 'long@gmail.com', phone: '0912345678', orders: 5, spent: 2760000, joined: '2025-05-22', addresses: ['45 Nguyễn Huệ, Q1, HCM'] },
  { id: 3, name: 'Lê Thị Kim Oanh', email: 'oanh@gmail.com', phone: '0932345678', orders: 18, spent: 14350000, joined: '2025-01-08', addresses: ['88 Trần Duy Hưng, CG, HN'] },
  { id: 4, name: 'Phạm Quốc Anh', email: 'anh@gmail.com', phone: '0943456789', orders: 7, spent: 4820000, joined: '2025-08-30', addresses: ['22 Đinh Tiên Hoàng, BĐ, HCM'] },
  { id: 5, name: 'Hoàng Minh Tuấn', email: 'tuan@gmail.com', phone: '0954567890', orders: 3, spent: 1480000, joined: '2025-11-15', addresses: ['5 Lý Thường Kiệt, HK, HN'] },
  { id: 6, name: 'Vũ Ngọc Hà', email: 'ha@gmail.com', phone: '0965678901', orders: 9, spent: 5940000, joined: '2025-04-01', addresses: ['130 Nguyễn Trãi, TB, HCM'] },
  { id: 7, name: 'Đỗ Thị Thùy', email: 'thuy@gmail.com', phone: '0976789012', orders: 21, spent: 18230000, joined: '2024-12-20', addresses: ['7 Hoàng Diệu, HH, HN'] },
  { id: 8, name: 'Bùi Thị Hằng', email: 'hang@gmail.com', phone: '0987890123', orders: 2, spent: 890000, joined: '2026-01-25', addresses: ['99 Trần Phú, ĐN'] },
];

const mockReviews = [
  { id: 1, product: 'Áo Linen Cổ V', productId: 1, reviewer: 'Nguyễn Thị Bích', stars: 5, date: '2026-03-06', content: 'Áo rất đẹp, vải mềm mại, mặc rất thoáng mát. Màu trắng đúng như ảnh, may sẽ mua thêm màu khác!', images: 2, status: 'pending' },
  { id: 2, product: 'Váy Midi Floral', productId: 3, reviewer: 'Lê Thị Kim Oanh', stars: 4, date: '2026-03-05', content: 'Váy đẹp, size chuẩn. Chỉ tiếc giao hàng hơi chậm so với cam kết. Tuy nhiên vẫn rất hài lòng với sản phẩm.', images: 1, status: 'approved' },
  { id: 3, product: 'Quần Wide-Leg Kaki', productId: 2, reviewer: 'Phạm Quốc Anh', stars: 3, date: '2026-03-04', content: 'Quần ổn nhưng chất vải hơi cứng so với kỳ vọng. Màu sắc đẹp, đường may chắc chắn.', images: 0, status: 'pending' },
  { id: 4, product: 'Áo Thun Basic', productId: 4, reviewer: 'Hoàng Minh Tuấn', stars: 5, date: '2026-03-03', content: 'Tuyệt vời! Áo basic nhưng rất chất lượng. Đã mua 3 màu và không hối hận chút nào.', images: 3, status: 'approved' },
  { id: 5, product: 'Túi Tote Canvas', productId: 5, reviewer: 'Vũ Ngọc Hà', stars: 4, date: '2026-03-02', content: 'Túi đẹp, vải dày dặn. Kích thước vừa đủ để đựng đồ đi làm.', images: 1, status: 'rejected' },
  { id: 6, product: 'Chân Váy A-Line', productId: 7, reviewer: 'Đỗ Thị Thùy', stars: 5, date: '2026-03-01', content: 'Chân váy rất xinh, mặc với gì cũng đẹp. Lần thứ 3 mua hàng bên này rồi, lần nào cũng hài lòng.', images: 2, status: 'pending' },
];

// State
let selectedFeaturedProducts = [1, 3, 4, 9];
let currentStaffId = null;
let currentProductId = null;
let currentVoucherId = null;
let currentCollectionId = null;
let currentPaymentId = null;
let variantColors = [];
let variantSizes = [];
let selectedProductRows = new Set();

// ============================================================
//  HELPERS
// ============================================================

/**
 * Format a number as Vietnamese currency (₫)
 */
function formatPrice(num) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(num);
}

/**
 * Format a datetime string for display
 */
function formatDate(str) {
  if (!str) return '—';
  const d = new Date(str);
  if (isNaN(d)) return str;
  return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

/**
 * Format datetime (date + time)
 */
function formatDateTime(str) {
  if (!str) return '—';
  const parts = str.split(' ');
  if (parts.length === 2) {
    const [date, time] = parts;
    const [y, m, d] = date.split('-');
    return `${d}/${m}/${y} ${time}`;
  }
  return str;
}

/**
 * Build a status badge HTML string
 */
function statusBadge(status) {
  const map = {
    pending:   ['badge-warning', 'Chờ xác nhận'],
    confirmed: ['badge-info',    'Đã xác nhận'],
    preparing: ['badge-purple',  'Đang chuẩn bị'],
    shipping:  ['badge-primary', 'Đang giao'],
    delivered: ['badge-success', 'Đã giao'],
    cancelled: ['badge-danger',  'Đã hủy'],
    active:    ['badge-success', 'Đang bán'],
    hidden:    ['badge-muted',   'Đã ẩn'],
    draft:     ['badge-warning', 'Nháp'],
    paid:      ['badge-success', 'Đã thanh toán'],
    unpaid:    ['badge-danger',  'Chưa TT'],
    approved:  ['badge-success', 'Đã duyệt'],
    rejected:  ['badge-danger',  'Từ chối'],
    completed: ['badge-primary', 'Hoàn thành'],
    exchange:  ['badge-info',    'Đổi hàng'],
    return:    ['badge-warning', 'Trả hàng'],
  };
  const [cls, label] = map[status] || ['badge-muted', status];
  return `<span class="badge ${cls}">${label}</span>`;
}

/**
 * Show a toast notification
 * @param {string} message
 * @param {'success'|'danger'|'info'|'warning'} type
 */
function showNotification(message, type = 'success') {
  const container = document.getElementById('toastContainer');
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.textContent = message;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transition = 'opacity 0.3s';
    setTimeout(() => toast.remove(), 350);
  }, 3000);
}

/**
 * Open a modal by id
 */
function openModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.add('open');
}

/**
 * Close a modal by id
 */
function closeModal(id) {
  const el = document.getElementById(id);
  if (el) el.classList.remove('open');
}

/**
 * Build star rating HTML
 */
function starRating(n) {
  let html = '';
  for (let i = 1; i <= 5; i++) {
    html += `<span class="${i <= n ? '' : 'star-empty'}">★</span>`;
  }
  return `<span class="star-rating">${html}</span>`;
}

// ============================================================
//  ROUTER
// ============================================================

const PAGE_TITLES = {
  dashboard: 'Dashboard',
  'staff-list': 'Danh sách nhân viên',
  'staff-form': 'Tạo / Sửa nhân viên',
  vouchers: 'Quản lý Voucher',
  banners: 'Quản lý Banner',
  'featured-collections': 'Bộ sưu tập nổi bật',
  'featured-products': 'Sản phẩm nổi bật',
  'push-notifications': 'Push Notification',
  'products-list': 'Danh sách sản phẩm',
  'product-form': 'Tạo / Sửa sản phẩm',
  inventory: 'Quản lý kho',
  collections: 'Quản lý bộ sưu tập',
  'incoming-orders': 'Đơn hàng mới',
  'order-management': 'Quản lý đơn hàng',
  'payment-verification': 'Xác nhận thanh toán',
  'rma-management': 'Quản lý đổi / trả',
  revenue: 'Báo cáo doanh thu',
  'wishlist-analytics': 'Phân tích Wishlist',
  'customer-details': 'Chi tiết khách hàng',
  'feedback-moderation': 'Kiểm duyệt đánh giá',
};

/**
 * Navigate to a page by its hash key (without #)
 */
function navigate(pageId) {
  // Update URL hash without triggering popstate loop
  if (window.location.hash !== `#${pageId}`) {
    history.pushState(null, '', `#${pageId}`);
  }
  activatePage(pageId);
}

/**
 * Activate a page section and sidebar link
 */
function activatePage(pageId) {
  // Hide all pages
  document.querySelectorAll('.admin-page').forEach(p => p.classList.remove('active'));

  // Show target page
  const target = document.getElementById(`page-${pageId}`);
  if (target) target.classList.add('active');

  // Update sidebar active link
  document.querySelectorAll('.nav-link').forEach(l => {
    l.classList.toggle('active', l.dataset.page === pageId);
  });

  // Update header title
  const titleEl = document.getElementById('pageTitle');
  if (titleEl) titleEl.textContent = PAGE_TITLES[pageId] || pageId;

  // Scroll to top
  document.querySelector('.admin-content').scrollTop = 0;

  // Render page-specific content
  onPageActivate(pageId);

  // Close sidebar on mobile
  if (window.innerWidth <= 768) {
    document.getElementById('adminSidebar').classList.remove('open');
    const overlay = document.getElementById('sidebarOverlay');
    if (overlay) overlay.classList.remove('show');
  }
}

/**
 * Called whenever a page becomes active — renders its data
 */
function onPageActivate(pageId) {
  switch (pageId) {
    case 'dashboard':           renderDashboard(); break;
    case 'staff-list':          renderStaffTable(mockStaff); break;
    case 'vouchers':            renderVouchersTable(); break;
    case 'banners':             renderBannerList(); break;
    case 'featured-collections': renderFeaturedCollections(); break;
    case 'featured-products':   renderFeaturedProducts(); break;
    case 'push-notifications':  renderPushHistory(); break;
    case 'products-list':       renderProductsTable(mockProducts); break;
    case 'inventory':           renderInventoryTable(); break;
    case 'collections':         renderCollectionsGrid(); break;
    case 'incoming-orders':     renderIncomingOrders('all'); break;
    case 'order-management':    renderOrderManagement(mockOrders); break;
    case 'payment-verification': renderPaymentQueue(); break;
    case 'rma-management':      renderRMATable('all'); break;
    case 'revenue':             renderRevenuePage(); break;
    case 'wishlist-analytics':  renderWishlistPage(); break;
    case 'customer-details':    renderCustomersTable(mockCustomers); break;
    case 'feedback-moderation': renderFeedbackCards('pending'); break;
  }
}

/**
 * Initialise hash router
 */
function initRouter() {
  // Create sidebar overlay
  const overlay = document.createElement('div');
  overlay.id = 'sidebarOverlay';
  overlay.className = 'sidebar-overlay';
  overlay.onclick = () => {
    document.getElementById('adminSidebar').classList.remove('open');
    overlay.classList.remove('show');
  };
  document.body.appendChild(overlay);

  // Intercept nav-link clicks
  document.querySelectorAll('.nav-link').forEach(link => {
    link.addEventListener('click', e => {
      e.preventDefault();
      const pageId = link.dataset.page;
      navigate(pageId);
    });
  });

  // Handle browser back/forward
  window.addEventListener('popstate', () => {
    const hash = window.location.hash.replace('#', '') || 'dashboard';
    activatePage(hash);
  });

  // Handle initial load
  const initial = window.location.hash.replace('#', '') || 'dashboard';
  activatePage(initial);
}

/**
 * Toggle sidebar (mobile)
 */
function toggleSidebar() {
  const sidebar = document.getElementById('adminSidebar');
  const overlay = document.getElementById('sidebarOverlay');
  sidebar.classList.toggle('open');
  if (overlay) overlay.classList.toggle('show');
}

// ============================================================
//  DASHBOARD
// ============================================================

function renderDashboard() {
  renderDashboardChart();
  renderRecentOrdersTable();
  renderTopProducts();
}

function renderDashboardChart() {
  const data = [8200000, 11400000, 9800000, 13500000, 10200000, 14800000, 12450000];
  const labels = ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'];
  drawBarChart('revenueChartSvg', data, labels);

  const labelsContainer = document.getElementById('chartLabels');
  if (labelsContainer) {
    labelsContainer.innerHTML = labels.map(l => `<span class="chart-label">${l}</span>`).join('');
  }
}

function renderRecentOrdersTable() {
  const tbody = document.querySelector('#recentOrdersTable tbody');
  if (!tbody) return;
  const recent = mockOrders.slice(0, 5);
  tbody.innerHTML = recent.map(o => `
    <tr>
      <td><strong>${o.id}</strong></td>
      <td>${o.customer}</td>
      <td class="fw-600">${formatPrice(o.total)}</td>
      <td>${statusBadge(o.status)}</td>
    </tr>
  `).join('');
}

function renderTopProducts() {
  const list = document.getElementById('topProductsList');
  if (!list) return;
  const top = [...mockProducts].sort((a, b) => b.sold - a.sold).slice(0, 5);
  const rankClass = ['gold', 'silver', 'bronze', '', ''];
  list.innerHTML = top.map((p, i) => `
    <div class="top-product-item">
      <div class="top-product-rank ${rankClass[i]}">${i + 1}</div>
      <div class="top-product-info">
        <div class="top-product-name">${p.name}</div>
        <div class="top-product-sold">${p.sold} đơn · SKU ${p.sku}</div>
      </div>
      <div class="top-product-revenue">${formatPrice(p.price * p.sold)}</div>
    </div>
  `).join('');
}

// ============================================================
//  SVG CHARTS
// ============================================================

/**
 * Draw a simple SVG bar chart
 * @param {string} svgId  - ID of the <svg> element
 * @param {number[]} data
 * @param {string[]} labels
 * @param {string} color
 */
function drawBarChart(svgId, data, labels, color = '#2c2c2c') {
  const svg = document.getElementById(svgId);
  if (!svg) return;

  const W = 700, H = 200;
  const padL = 0, padR = 0, padT = 16, padB = 0;
  const chartW = W - padL - padR;
  const chartH = H - padT - padB;

  const max = Math.max(...data) || 1;
  const n = data.length;
  const barWidth = (chartW / n) * 0.55;
  const gap = (chartW / n) * 0.45;

  let svgContent = '';

  // Grid lines
  for (let i = 0; i <= 4; i++) {
    const y = padT + (chartH * (1 - i / 4));
    svgContent += `<line x1="${padL}" y1="${y}" x2="${W - padR}" y2="${y}" class="chart-grid-line" />`;
  }

  // Bars
  data.forEach((val, i) => {
    const barH = (val / max) * chartH;
    const x = padL + i * (barWidth + gap) + gap / 2;
    const y = padT + chartH - barH;
    svgContent += `
      <rect class="chart-bar" x="${x}" y="${y}" width="${barWidth}" height="${barH}"
        fill="${color}" rx="2"
        data-value="${val}" data-label="${labels ? labels[i] : i}" />
    `;
  });

  svg.innerHTML = svgContent;
}

/**
 * Draw SVG line chart (area)
 */
function drawLineChart(svgId, data, color = '#2c2c2c') {
  const svg = document.getElementById(svgId);
  if (!svg) return;
  if (!data || data.length < 2) return;

  const W = 700, H = 220;
  const padL = 0, padR = 0, padT = 20, padB = 20;
  const chartW = W - padL - padR;
  const chartH = H - padT - padB;
  const max = Math.max(...data) || 1;
  const min = Math.min(...data);
  const range = max - min || 1;

  const getX = i => padL + (i / (data.length - 1)) * chartW;
  const getY = v => padT + chartH - ((v - min) / range) * chartH;

  const points = data.map((v, i) => `${getX(i)},${getY(v)}`).join(' ');

  // Build area path
  const firstX = getX(0), lastX = getX(data.length - 1);
  const bottomY = padT + chartH;
  const areaPath = `M${firstX},${bottomY} L${points.replace(/,/g, ' ').split(' ').reduce((acc, val, i, arr) => {
    // just use the line path
    return acc;
  }, '')}`;

  let svgContent = '';

  // Grid lines
  for (let i = 0; i <= 4; i++) {
    const y = padT + (chartH * i / 4);
    svgContent += `<line x1="${padL}" y1="${y}" x2="${W}" y2="${y}" class="chart-grid-line" />`;
  }

  // Area
  const areaPoints = `${firstX},${bottomY} ` + data.map((v, i) => `${getX(i)},${getY(v)}`).join(' ') + ` ${lastX},${bottomY}`;
  svgContent += `<polygon points="${areaPoints}" fill="${color}" opacity="0.08" />`;

  // Line
  svgContent += `<polyline points="${points}" fill="none" stroke="${color}" stroke-width="2.5" stroke-linejoin="round" stroke-linecap="round" />`;

  // Dots
  data.forEach((v, i) => {
    svgContent += `<circle cx="${getX(i)}" cy="${getY(v)}" r="3.5" fill="${color}" />`;
  });

  svg.innerHTML = svgContent;
}

// ============================================================
//  STAFF
// ============================================================

function renderStaffTable(staff) {
  const tbody = document.getElementById('staffTableBody');
  if (!tbody) return;
  if (!staff.length) {
    tbody.innerHTML = `<tr><td colspan="6" class="empty-state">Không tìm thấy nhân viên</td></tr>`;
    return;
  }
  tbody.innerHTML = staff.map(s => `
    <tr>
      <td>
        <div class="table-person">
          <div class="table-avatar">${s.name.charAt(0)}</div>
          <span class="table-person-name">${s.name}</span>
        </div>
      </td>
      <td>${s.email}</td>
      <td><span class="badge ${s.role === 'Admin' ? 'role-badge-Admin' : s.role === 'Staff' ? 'role-badge-Staff' : 'role-badge-Warehouse'}">${s.role}</span></td>
      <td>
        <label class="toggle-switch">
          <input type="checkbox" ${s.active ? 'checked' : ''} onchange="toggleStaffStatus(${s.id})" />
          <span class="toggle-slider"></span>
        </label>
      </td>
      <td>${formatDate(s.created)}</td>
      <td>
        <div class="actions-cell">
          <button class="btn btn-sm btn-outline" onclick="navigate('staff-form'); openStaffForm(${s.id})">Sửa</button>
          <button class="btn btn-sm ${s.active ? 'btn-danger' : 'btn-success'}" onclick="toggleStaffStatus(${s.id})">
            ${s.active ? 'Vô hiệu' : 'Kích hoạt'}
          </button>
        </div>
      </td>
    </tr>
  `).join('');
}

function filterStaff() {
  const q = document.getElementById('staffSearch').value.toLowerCase();
  const role = document.getElementById('staffRoleFilter').value;
  const filtered = mockStaff.filter(s => {
    const matchQ = s.name.toLowerCase().includes(q) || s.email.toLowerCase().includes(q);
    const matchRole = !role || s.role === role;
    return matchQ && matchRole;
  });
  renderStaffTable(filtered);
}

function toggleStaffStatus(id) {
  const staff = mockStaff.find(s => s.id === id);
  if (staff) {
    staff.active = !staff.active;
    renderStaffTable(mockStaff);
    showNotification(`${staff.active ? 'Kích hoạt' : 'Vô hiệu hóa'} nhân viên ${staff.name} thành công`);
  }
}

function openStaffForm(id) {
  currentStaffId = id;
  const form = document.getElementById('staffFormEl');
  if (!form) return;
  form.reset();

  if (id) {
    const s = mockStaff.find(x => x.id === id);
    if (s) {
      document.getElementById('staffFormTitle').textContent = `Sửa nhân viên — ${s.name}`;
      document.getElementById('sf-name').value = s.name;
      document.getElementById('sf-email').value = s.email;
      document.getElementById('sf-role').value = s.role;
      document.getElementById('sf-active').checked = s.active;
      document.querySelectorAll('#staffPermissions input[type=checkbox]').forEach(cb => {
        cb.checked = s.permissions.includes(cb.value);
      });
    }
  } else {
    document.getElementById('staffFormTitle').textContent = 'Tạo nhân viên mới';
  }
}

function saveStaff(e) {
  e.preventDefault();
  const name = document.getElementById('sf-name').value;
  const email = document.getElementById('sf-email').value;
  const role = document.getElementById('sf-role').value;
  const active = document.getElementById('sf-active').checked;
  const permissions = [...document.querySelectorAll('#staffPermissions input:checked')].map(c => c.value);

  if (currentStaffId) {
    const s = mockStaff.find(x => x.id === currentStaffId);
    if (s) { s.name = name; s.email = email; s.role = role; s.active = active; s.permissions = permissions; }
    showNotification('Cập nhật nhân viên thành công');
  } else {
    mockStaff.push({ id: Date.now(), name, email, role, active, permissions, created: new Date().toISOString().slice(0, 10) });
    showNotification('Tạo nhân viên thành công');
  }
  navigate('staff-list');
}

// ============================================================
//  VOUCHERS
// ============================================================

function renderVouchersTable() {
  const tbody = document.getElementById('vouchersTableBody');
  if (!tbody) return;
  tbody.innerHTML = mockVouchers.map(v => `
    <tr>
      <td><strong>${v.code}</strong></td>
      <td>${v.type === 'percent' ? 'Phần trăm (%)' : 'Số tiền (₫)'}</td>
      <td class="fw-600">${v.type === 'percent' ? v.value + '%' : formatPrice(v.value)}</td>
      <td>${v.minOrder ? formatPrice(v.minOrder) : '—'}</td>
      <td>${v.quantity - v.used} / ${v.quantity}</td>
      <td>${formatDate(v.expiry)}</td>
      <td>${v.active ? '<span class="badge badge-success">Đang dùng</span>' : '<span class="badge badge-muted">Hết hạn</span>'}</td>
      <td>
        <div class="actions-cell">
          <button class="btn btn-sm btn-outline" onclick="openVoucherModal(${v.id})">Sửa</button>
          <button class="btn btn-sm btn-danger" onclick="deleteVoucher(${v.id})">Xóa</button>
        </div>
      </td>
    </tr>
  `).join('');

  renderPagination('vouchersPagination', 1, 1);
}

function openVoucherModal(id) {
  currentVoucherId = id;
  const form = document.getElementById('voucherForm');
  if (form) form.reset();

  if (id) {
    const v = mockVouchers.find(x => x.id === id);
    if (v) {
      document.getElementById('voucherModalTitle').textContent = `Sửa voucher — ${v.code}`;
      document.getElementById('vm-code').value = v.code;
      document.getElementById('vm-type').value = v.type;
      document.getElementById('vm-value').value = v.value;
      document.getElementById('vm-min-order').value = v.minOrder;
      document.getElementById('vm-quantity').value = v.quantity;
      document.getElementById('vm-expiry').value = v.expiry;
    }
  } else {
    document.getElementById('voucherModalTitle').textContent = 'Tạo voucher mới';
  }
  openModal('voucherModal');
}

function saveVoucher(e) {
  e.preventDefault();
  const code = document.getElementById('vm-code').value.toUpperCase();
  const type = document.getElementById('vm-type').value;
  const value = +document.getElementById('vm-value').value;
  const minOrder = +document.getElementById('vm-min-order').value || 0;
  const quantity = +document.getElementById('vm-quantity').value || 100;
  const expiry = document.getElementById('vm-expiry').value;

  if (currentVoucherId) {
    const v = mockVouchers.find(x => x.id === currentVoucherId);
    if (v) { v.code = code; v.type = type; v.value = value; v.minOrder = minOrder; v.quantity = quantity; v.expiry = expiry; }
    showNotification('Cập nhật voucher thành công');
  } else {
    mockVouchers.push({ id: Date.now(), code, type, value, minOrder, quantity, used: 0, expiry, active: true });
    showNotification('Tạo voucher thành công');
  }
  closeModal('voucherModal');
  renderVouchersTable();
}

function deleteVoucher(id) {
  if (!confirm('Xác nhận xóa voucher này?')) return;
  const idx = mockVouchers.findIndex(v => v.id === id);
  if (idx !== -1) mockVouchers.splice(idx, 1);
  showNotification('Đã xóa voucher', 'danger');
  renderVouchersTable();
}

// ============================================================
//  BANNERS
// ============================================================

function renderBannerList() {
  const list = document.getElementById('bannerList');
  if (!list) return;
  list.innerHTML = mockBanners.map(b => `
    <div class="banner-item" draggable="true">
      <span class="drag-handle">⠿</span>
      <div class="banner-preview">Banner ${b.order}</div>
      <div class="banner-info">
        <div class="banner-title">${b.title}</div>
        <div class="banner-link">${b.link}</div>
      </div>
      <div class="banner-order">Thứ tự: ${b.order}</div>
      <label class="toggle-switch">
        <input type="checkbox" ${b.active ? 'checked' : ''} onchange="toggleBanner(${b.id})" />
        <span class="toggle-slider"></span>
      </label>
      <div class="actions-cell">
        <button class="btn btn-sm btn-outline" onclick="editBanner(${b.id})">Sửa</button>
        <button class="btn btn-sm btn-danger" onclick="deleteBanner(${b.id})">Xóa</button>
      </div>
    </div>
  `).join('');

  // Bind upload zone
  const zone = document.getElementById('bannerUploadZone');
  const fileInput = document.getElementById('bannerFileInput');
  if (zone && fileInput) {
    zone.onclick = () => fileInput.click();
    fileInput.onchange = () => {
      if (fileInput.files.length) {
        showNotification('Banner đã được tải lên (demo)', 'info');
        fileInput.value = '';
      }
    };
  }
}

function toggleBanner(id) {
  const b = mockBanners.find(x => x.id === id);
  if (b) {
    b.active = !b.active;
    showNotification(`Banner "${b.title}" ${b.active ? 'đã bật' : 'đã tắt'}`);
  }
}

function deleteBanner(id) {
  if (!confirm('Xóa banner này?')) return;
  const idx = mockBanners.findIndex(b => b.id === id);
  if (idx !== -1) mockBanners.splice(idx, 1);
  showNotification('Đã xóa banner', 'danger');
  renderBannerList();
}

function editBanner(id) {
  showNotification('Chức năng chỉnh sửa banner (demo)', 'info');
}

function openBannerModal() {
  showNotification('Form thêm banner (demo)', 'info');
}

// ============================================================
//  FEATURED COLLECTIONS
// ============================================================

function renderFeaturedCollections() {
  const grid = document.getElementById('featuredCollectionsGrid');
  if (!grid) return;
  grid.innerHTML = mockCollections.map(c => `
    <div class="collection-card">
      <div class="collection-card-img">Ảnh bộ sưu tập</div>
      <div class="collection-card-body">
        <div class="collection-card-name">${c.name}</div>
        <div class="collection-card-desc">${c.desc}</div>
        <div class="collection-card-meta">${c.count} sản phẩm</div>
        <div class="collection-card-actions">
          <label class="toggle-switch">
            <input type="checkbox" ${c.active ? 'checked' : ''} onchange="toggleCollectionFeatured(${c.id})" />
            <span class="toggle-slider"></span>
            <span class="toggle-label" style="font-size:11px">${c.active ? 'Hiển thị' : 'Ẩn'}</span>
          </label>
          <button class="btn btn-xs btn-outline">Sửa</button>
        </div>
      </div>
    </div>
  `).join('');
}

function toggleCollectionFeatured(id) {
  const c = mockCollections.find(x => x.id === id);
  if (c) {
    c.active = !c.active;
    showNotification(`Bộ sưu tập "${c.name}" ${c.active ? 'bật' : 'tắt'}`);
    renderFeaturedCollections();
  }
}

function openCollectionFeaturedModal() {
  showNotification('Thêm bộ sưu tập nổi bật (demo)', 'info');
}

// ============================================================
//  FEATURED PRODUCTS
// ============================================================

function renderFeaturedProducts() {
  renderFeaturedProductCards();
  renderFeaturedProductSearchResults(mockProducts);
}

function renderFeaturedProductCards() {
  const container = document.getElementById('featuredProductCards');
  if (!container) return;
  const products = mockProducts.filter(p => selectedFeaturedProducts.includes(p.id));
  if (!products.length) {
    container.innerHTML = '<p class="text-muted" style="font-size:13px;">Chưa có sản phẩm nổi bật</p>';
    return;
  }
  container.innerHTML = products.map(p => `
    <div class="featured-card">
      <button class="featured-card-remove" onclick="removeFeaturedProduct(${p.id})">×</button>
      <div class="featured-card-img">[Ảnh]</div>
      <div class="featured-card-name">${p.name}</div>
      <div style="font-size:11px;color:#666;">${formatPrice(p.price)}</div>
    </div>
  `).join('');
}

function renderFeaturedProductSearchResults(list) {
  const tbody = document.getElementById('featuredProductSearchBody');
  if (!tbody) return;
  tbody.innerHTML = list.map(p => `
    <tr>
      <td>
        <input type="checkbox" ${selectedFeaturedProducts.includes(p.id) ? 'checked' : ''}
          onchange="toggleFeaturedProduct(${p.id}, this)" />
      </td>
      <td>${p.name}</td>
      <td class="text-muted">${p.sku}</td>
      <td>${formatPrice(p.price)}</td>
    </tr>
  `).join('');
}

function toggleFeaturedProduct(id, cb) {
  if (cb.checked) {
    if (!selectedFeaturedProducts.includes(id)) selectedFeaturedProducts.push(id);
  } else {
    selectedFeaturedProducts = selectedFeaturedProducts.filter(x => x !== id);
  }
  renderFeaturedProductCards();
  showNotification('Đã cập nhật sản phẩm nổi bật');
}

function removeFeaturedProduct(id) {
  selectedFeaturedProducts = selectedFeaturedProducts.filter(x => x !== id);
  renderFeaturedProducts();
  showNotification('Đã gỡ sản phẩm khỏi danh sách nổi bật', 'warning');
}

function searchFeaturedProducts() {
  const q = document.getElementById('featuredProductSearch').value.toLowerCase();
  const filtered = mockProducts.filter(p => p.name.toLowerCase().includes(q) || p.sku.toLowerCase().includes(q));
  renderFeaturedProductSearchResults(filtered);
}

// ============================================================
//  PUSH NOTIFICATIONS
// ============================================================

function renderPushHistory() {
  const tbody = document.getElementById('pushHistoryBody');
  if (!tbody) return;
  tbody.innerHTML = mockPushHistory.map(h => `
    <tr>
      <td>${formatDateTime(h.date)}</td>
      <td>${h.title}</td>
      <td>${h.target}</td>
      <td>${h.sent.toLocaleString('vi-VN')}</td>
      <td><strong>${h.openRate}</strong></td>
    </tr>
  `).join('');
}

function toggleSchedule(cb) {
  const group = document.getElementById('scheduleTimeGroup');
  if (group) group.style.display = cb.checked ? 'block' : 'none';
}

function sendPushNotif(e) {
  e.preventDefault();
  const title = document.getElementById('pn-title').value;
  const target = document.getElementById('pn-target');
  const targetText = target.options[target.selectedIndex].text;
  mockPushHistory.unshift({
    id: Date.now(),
    date: new Date().toISOString().slice(0, 16).replace('T', ' '),
    title,
    target: targetText,
    sent: Math.floor(Math.random() * 5000 + 8000),
    openRate: (Math.random() * 30 + 20).toFixed(1) + '%',
  });
  showNotification('Đã gửi push notification thành công!');
  document.getElementById('pushNotifForm').reset();
  renderPushHistory();
}

// ============================================================
//  PRODUCTS
// ============================================================

function renderProductsTable(products) {
  const tbody = document.getElementById('productsTableBody');
  if (!tbody) return;
  if (!products.length) {
    tbody.innerHTML = `<tr><td colspan="8" class="empty-state">Không tìm thấy sản phẩm</td></tr>`;
    return;
  }
  tbody.innerHTML = products.map(p => `
    <tr id="prow-${p.id}">
      <td><input type="checkbox" data-id="${p.id}" onchange="toggleProductRowSelect(${p.id}, this)" /></td>
      <td>
        <div style="display:flex;align-items:center;gap:8px;">
          <div class="table-product-img"></div>
          <span class="fw-600">${p.name}</span>
        </div>
      </td>
      <td class="text-muted">${p.sku}</td>
      <td>${p.category}</td>
      <td class="fw-600">${formatPrice(p.price)}</td>
      <td class="${p.stock === 0 ? 'text-danger' : p.stock < 10 ? 'text-warning' : ''} fw-600">${p.stock}</td>
      <td>${statusBadge(p.status)}</td>
      <td>
        <div class="actions-cell">
          <button class="btn btn-sm btn-outline" onclick="navigate('product-form'); openProductForm(${p.id})">Sửa</button>
          <button class="btn btn-sm btn-outline" onclick="toggleProductStatus(${p.id})">${p.status === 'hidden' ? 'Hiện' : 'Ẩn'}</button>
          <button class="btn btn-sm btn-danger" onclick="deleteProduct(${p.id})">Xóa</button>
        </div>
      </td>
    </tr>
  `).join('');

  renderPagination('productsPagination', 1, Math.ceil(products.length / 10));
}

function filterProducts() {
  const q = document.getElementById('productSearch').value.toLowerCase();
  const cat = document.getElementById('productCatFilter').value;
  const status = document.getElementById('productStatusFilter').value;
  const filtered = mockProducts.filter(p => {
    const matchQ = p.name.toLowerCase().includes(q) || p.sku.toLowerCase().includes(q);
    const matchCat = !cat || p.category === cat;
    const matchStatus = !status || p.status === status;
    return matchQ && matchCat && matchStatus;
  });
  renderProductsTable(filtered);
}

function toggleProductRowSelect(id, cb) {
  if (cb.checked) selectedProductRows.add(id);
  else selectedProductRows.delete(id);
  const btn = document.getElementById('bulkDeleteBtn');
  if (btn) btn.style.display = selectedProductRows.size ? 'inline-flex' : 'none';
}

function selectAllProductRows(masterCb) {
  document.querySelectorAll('#productsTableBody input[type=checkbox]').forEach(cb => {
    cb.checked = masterCb.checked;
    const id = +cb.dataset.id;
    if (masterCb.checked) selectedProductRows.add(id);
    else selectedProductRows.delete(id);
  });
  const btn = document.getElementById('bulkDeleteBtn');
  if (btn) btn.style.display = selectedProductRows.size ? 'inline-flex' : 'none';
}

function bulkDeleteProducts() {
  if (!selectedProductRows.size) return;
  if (!confirm(`Xóa ${selectedProductRows.size} sản phẩm đã chọn?`)) return;
  selectedProductRows.forEach(id => {
    const idx = mockProducts.findIndex(p => p.id === id);
    if (idx !== -1) mockProducts.splice(idx, 1);
  });
  selectedProductRows.clear();
  showNotification('Đã xóa sản phẩm đã chọn', 'danger');
  renderProductsTable(mockProducts);
  const btn = document.getElementById('bulkDeleteBtn');
  if (btn) btn.style.display = 'none';
}

function toggleProductStatus(id) {
  const p = mockProducts.find(x => x.id === id);
  if (p) {
    p.status = p.status === 'hidden' ? 'active' : 'hidden';
    renderProductsTable(mockProducts);
    showNotification(`Đã ${p.status === 'hidden' ? 'ẩn' : 'hiện'} sản phẩm ${p.name}`);
  }
}

function deleteProduct(id) {
  if (!confirm('Xóa sản phẩm này?')) return;
  const idx = mockProducts.findIndex(p => p.id === id);
  if (idx !== -1) mockProducts.splice(idx, 1);
  showNotification('Đã xóa sản phẩm', 'danger');
  renderProductsTable(mockProducts);
}

// ============================================================
//  PRODUCT FORM
// ============================================================

function openProductForm(id) {
  currentProductId = id;
  variantColors = [];
  variantSizes = [];
  document.getElementById('colorChips').innerHTML = '';
  document.getElementById('sizeChips').innerHTML = '';
  document.getElementById('variantMatrix').innerHTML = '';

  if (id) {
    const p = mockProducts.find(x => x.id === id);
    if (p) {
      document.getElementById('productFormTitle').textContent = `Sửa sản phẩm — ${p.name}`;
      document.getElementById('pf-name').value = p.name;
      document.getElementById('pf-sku').value = p.sku;
      document.getElementById('pf-category').value = p.category;
      document.getElementById('pf-price').value = p.price;
      document.getElementById('pf-compare-price').value = p.comparePrice;
      document.getElementById('pf-weight').value = p.weight;
      document.getElementById('pf-status').value = p.status;
      document.getElementById('pf-featured').checked = p.featured;

      variantColors = [...p.colors];
      variantSizes = [...p.sizes];
      renderChips('color');
      renderChips('size');
      updateVariantMatrix();
    }
  } else {
    document.getElementById('productFormTitle').textContent = 'Tạo sản phẩm mới';
    document.getElementById('productFormEl').reset();
  }
}

function addVariantChip(type) {
  const input = document.getElementById(type === 'color' ? 'colorInput' : 'sizeInput');
  const val = input.value.trim();
  if (!val) return;
  const arr = type === 'color' ? variantColors : variantSizes;
  if (!arr.includes(val)) {
    arr.push(val);
    renderChips(type);
    updateVariantMatrix();
  }
  input.value = '';
  input.focus();
}

function removeVariantChip(type, idx) {
  if (type === 'color') variantColors.splice(idx, 1);
  else variantSizes.splice(idx, 1);
  renderChips(type);
  updateVariantMatrix();
}

function renderChips(type) {
  const arr = type === 'color' ? variantColors : variantSizes;
  const container = document.getElementById(type === 'color' ? 'colorChips' : 'sizeChips');
  if (!container) return;
  container.innerHTML = arr.map((v, i) => `
    <span class="chip">
      ${v}
      <button type="button" class="chip-remove" onclick="removeVariantChip('${type}', ${i})">×</button>
    </span>
  `).join('');
}

function updateVariantMatrix() {
  const container = document.getElementById('variantMatrix');
  if (!container) return;
  if (!variantColors.length || !variantSizes.length) {
    container.innerHTML = '<p class="text-muted" style="font-size:12px;margin-top:8px;">Thêm màu sắc và kích cỡ để tạo ma trận biến thể.</p>';
    return;
  }

  let html = `<table><thead><tr><th>Màu sắc</th><th>Kích cỡ</th><th>SKU biến thể</th><th>Tồn kho</th><th>Giá</th></tr></thead><tbody>`;
  variantColors.forEach(color => {
    variantSizes.forEach(size => {
      const skuBase = document.getElementById('pf-sku').value || 'PRD';
      const variantSku = `${skuBase}-${color.slice(0,2).toUpperCase()}-${size}`;
      html += `
        <tr>
          <td>${color}</td>
          <td>${size}</td>
          <td><input type="text" value="${variantSku}" style="width:110px;padding:3px 6px;font-size:11px;border:1px solid #e8e8e8;border-radius:2px;" /></td>
          <td><input type="number" value="10" min="0" class="variant-stock" style="width:70px;" /></td>
          <td><input type="number" placeholder="—" class="variant-price" style="width:90px;" /></td>
        </tr>
      `;
    });
  });
  html += '</tbody></table>';
  container.innerHTML = html;
}

function saveProduct(e) {
  e.preventDefault();
  const name = document.getElementById('pf-name').value;
  showNotification(`Đã lưu sản phẩm "${name}" thành công`);
  navigate('products-list');
}

function saveAndPublishProduct() {
  const name = document.getElementById('pf-name').value || 'Sản phẩm mới';
  document.getElementById('pf-status').value = 'active';
  showNotification(`Đã đăng bán sản phẩm "${name}"`, 'success');
  navigate('products-list');
}

function triggerImageUpload(idx) {
  showNotification(`Tải ảnh ${idx === 0 ? 'chính' : idx} (demo)`, 'info');
}

// ============================================================
//  INVENTORY
// ============================================================

function renderInventoryTable() {
  const tbody = document.getElementById('inventoryTableBody');
  if (!tbody) return;

  const lowStock = mockInventory.filter(i => i.stock > 0 && i.stock < 10).length;
  const outStock = mockInventory.filter(i => i.stock === 0).length;

  const totalSKUEl = document.getElementById('totalSKU');
  const lowStockEl = document.getElementById('lowStockCount');
  const outStockEl = document.getElementById('outStockCount');
  if (totalSKUEl) totalSKUEl.textContent = mockInventory.length;
  if (lowStockEl) lowStockEl.textContent = lowStock;
  if (outStockEl) outStockEl.textContent = outStock;

  tbody.innerHTML = mockInventory.map(item => {
    const cls = item.stock === 0 ? 'stock-out' : item.stock < 10 ? 'stock-low' : '';
    const inputCls = item.stock === 0 ? 'out' : item.stock < 10 ? 'low' : '';
    const badge = item.stock === 0 ? statusBadge('cancelled') : item.stock < 10 ? '<span class="badge badge-warning">Sắp hết</span>' : '<span class="badge badge-success">Còn hàng</span>';
    return `
      <tr class="${cls}">
        <td class="fw-600">${item.product}</td>
        <td>${item.variant}</td>
        <td class="text-muted">${item.sku}</td>
        <td>
          <input type="number" class="stock-input ${inputCls}" value="${item.stock}"
            onchange="inlineEditStock(${item.id}, this.value)"
            min="0" />
        </td>
        <td><button class="btn btn-xs btn-outline" onclick="restockItem(${item.id})">+ Nhập thêm</button></td>
        <td>${badge}</td>
      </tr>
    `;
  }).join('');
}

function inlineEditStock(id, value) {
  const item = mockInventory.find(i => i.id === id);
  if (item) {
    item.stock = parseInt(value) || 0;
    renderInventoryTable();
    showNotification(`Đã cập nhật tồn kho ${item.sku}: ${item.stock}`, 'info');
  }
}

function restockItem(id) {
  const item = mockInventory.find(i => i.id === id);
  if (!item) return;
  const qty = parseInt(prompt(`Nhập thêm bao nhiêu cho ${item.sku}?`, '20'));
  if (qty && qty > 0) {
    item.stock += qty;
    renderInventoryTable();
    showNotification(`Đã nhập thêm ${qty} cho ${item.sku}`);
  }
}

// ============================================================
//  COLLECTIONS (Manage)
// ============================================================

function renderCollectionsGrid() {
  const grid = document.getElementById('collectionsGrid');
  if (!grid) return;
  grid.innerHTML = mockCollections.map(c => `
    <div class="collection-card">
      <div class="collection-card-img">Ảnh bộ sưu tập</div>
      <div class="collection-card-body">
        <div class="collection-card-name">${c.name}</div>
        <div class="collection-card-desc">${c.desc}</div>
        <div class="collection-card-meta">${c.count} sản phẩm</div>
        <div class="collection-card-actions">
          <label class="toggle-switch">
            <input type="checkbox" ${c.active ? 'checked' : ''} onchange="toggleCollection(${c.id})" />
            <span class="toggle-slider"></span>
          </label>
          <div style="display:flex;gap:5px;">
            <button class="btn btn-xs btn-outline" onclick="openCollectionModal(${c.id})">Sửa</button>
            <button class="btn btn-xs btn-danger" onclick="deleteCollection(${c.id})">Xóa</button>
          </div>
        </div>
      </div>
    </div>
  `).join('');
}

function openCollectionModal(id) {
  currentCollectionId = id;
  const form = document.getElementById('collectionForm');
  if (form) form.reset();
  if (id) {
    const c = mockCollections.find(x => x.id === id);
    if (c) {
      document.getElementById('collectionModalTitle').textContent = `Sửa: ${c.name}`;
      document.getElementById('cm-name').value = c.name;
      document.getElementById('cm-desc').value = c.desc;
      document.getElementById('cm-active').checked = c.active;
    }
  } else {
    document.getElementById('collectionModalTitle').textContent = 'Tạo bộ sưu tập mới';
  }
  openModal('collectionModal');
}

function saveCollection(e) {
  e.preventDefault();
  const name = document.getElementById('cm-name').value;
  const desc = document.getElementById('cm-desc').value;
  const active = document.getElementById('cm-active').checked;
  if (currentCollectionId) {
    const c = mockCollections.find(x => x.id === currentCollectionId);
    if (c) { c.name = name; c.desc = desc; c.active = active; }
    showNotification('Cập nhật bộ sưu tập thành công');
  } else {
    mockCollections.push({ id: Date.now(), name, desc, count: 0, active });
    showNotification('Tạo bộ sưu tập thành công');
  }
  closeModal('collectionModal');
  renderCollectionsGrid();
}

function toggleCollection(id) {
  const c = mockCollections.find(x => x.id === id);
  if (c) {
    c.active = !c.active;
    showNotification(`Bộ sưu tập "${c.name}" ${c.active ? 'bật' : 'tắt'}`);
    renderCollectionsGrid();
  }
}

function deleteCollection(id) {
  if (!confirm('Xóa bộ sưu tập này?')) return;
  const idx = mockCollections.findIndex(c => c.id === id);
  if (idx !== -1) mockCollections.splice(idx, 1);
  showNotification('Đã xóa bộ sưu tập', 'danger');
  renderCollectionsGrid();
}

// ============================================================
//  INCOMING ORDERS
// ============================================================

function renderIncomingOrders(filter) {
  const tbody = document.getElementById('incomingOrdersBody');
  if (!tbody) return;

  const relevantStatuses = ['pending', 'confirmed', 'preparing'];
  let orders = mockOrders.filter(o => relevantStatuses.includes(o.status));
  if (filter !== 'all') orders = orders.filter(o => o.status === filter);

  tbody.innerHTML = orders.map(o => `
    <tr>
      <td><input type="checkbox" /></td>
      <td><strong>${o.id}</strong></td>
      <td>${o.customer}</td>
      <td>${o.items} sản phẩm</td>
      <td class="fw-600">${formatPrice(o.total)}</td>
      <td>${o.payment === 'COD' ? '<span class="badge badge-warning">COD</span>' : '<span class="badge badge-info">' + o.payment + '</span>'}</td>
      <td>${formatDateTime(o.created)}</td>
      <td>
        <div class="actions-cell">
          <button class="btn btn-xs btn-outline" onclick="viewOrder('${o.id}')">Xem</button>
          ${o.status === 'pending' ? `<button class="btn btn-xs btn-success" onclick="confirmOrder('${o.id}')">Xác nhận</button>` : ''}
          <button class="btn btn-xs btn-outline" onclick="showNotification('In đơn hàng (demo)','info')">In</button>
        </div>
      </td>
    </tr>
  `).join('');

  if (!orders.length) {
    tbody.innerHTML = `<tr><td colspan="8" class="empty-state">Không có đơn hàng</td></tr>`;
  }
}

function filterIncomingOrders(filter, btn) {
  document.querySelectorAll('#incomingOrderTabs .tab-btn').forEach(b => b.classList.remove('active'));
  if (btn) btn.classList.add('active');
  renderIncomingOrders(filter);
}

function confirmOrder(id) {
  const order = mockOrders.find(o => o.id === id);
  if (order) {
    order.status = 'confirmed';
    showNotification(`Đã xác nhận đơn ${id}`);
    renderIncomingOrders('all');
  }
}

function bulkConfirmOrders() {
  let count = 0;
  mockOrders.forEach(o => {
    if (o.status === 'pending') { o.status = 'confirmed'; count++; }
  });
  showNotification(`Đã xác nhận ${count} đơn hàng`);
  renderIncomingOrders('all');
}

function viewOrder(id) {
  const o = mockOrders.find(x => x.id === id);
  if (!o) return;
  showNotification(`Đơn ${id} — ${o.customer} — ${formatPrice(o.total)}`, 'info');
}

function selectAllRows(tbodyId, masterCb) {
  document.querySelectorAll(`#${tbodyId} input[type=checkbox]`).forEach(cb => {
    cb.checked = masterCb.checked;
  });
}

// ============================================================
//  ORDER MANAGEMENT
// ============================================================

function renderOrderManagement(orders) {
  const tbody = document.getElementById('orderManagementBody');
  if (!tbody) return;
  tbody.innerHTML = orders.map(o => `
    <tr>
      <td>
        <button class="order-expand-btn" onclick="expandOrderRow('${o.id}', this)">▶</button>
        <strong>${o.id}</strong>
      </td>
      <td>${o.customer}</td>
      <td>${o.items} SP</td>
      <td class="fw-600">${formatPrice(o.total)}</td>
      <td>${o.paymentStatus === 'paid' ? '<span class="badge badge-success">Đã TT</span>' : '<span class="badge badge-warning">Chưa TT</span>'}</td>
      <td>
        <select class="status-select" onchange="updateOrderStatus('${o.id}', this.value)">
          ${['pending','confirmed','preparing','shipping','delivered','cancelled'].map(s =>
            `<option value="${s}" ${o.status === s ? 'selected' : ''}>${statusBadge(s).replace(/<[^>]+>/g, '')}</option>`
          ).join('')}
        </select>
      </td>
      <td>${formatDateTime(o.created)}</td>
      <td>
        <div class="actions-cell">
          <button class="btn btn-xs btn-outline" onclick="expandOrderRow('${o.id}', this)">Chi tiết</button>
        </div>
      </td>
    </tr>
    <tr id="detail-${o.id}" class="order-detail-row" style="display:none">
      <td colspan="8">
        <div class="order-detail-inner">
          <div class="order-detail-section">
            <h4>Khách hàng</h4>
            <p>${o.customer}<br>${o.customerEmail}<br>${o.phone}</p>
          </div>
          <div class="order-detail-section">
            <h4>Địa chỉ giao hàng</h4>
            <p>${o.address}</p>
          </div>
          <div class="order-detail-section">
            <h4>Thanh toán</h4>
            <p>Phương thức: ${o.payment}<br>
            Tổng tiền: <strong>${formatPrice(o.total)}</strong><br>
            ${o.note ? 'Ghi chú: ' + o.note : ''}
            </p>
          </div>
        </div>
      </td>
    </tr>
  `).join('');

  renderPagination('ordersPagination', 1, Math.ceil(orders.length / 10));
}

function expandOrderRow(id, btn) {
  const row = document.getElementById(`detail-${id}`);
  if (!row) return;
  const isOpen = row.style.display !== 'none';
  row.style.display = isOpen ? 'none' : 'table-row';
  if (btn && btn.classList.contains('order-expand-btn')) {
    btn.classList.toggle('open', !isOpen);
  }
}

function updateOrderStatus(id, status) {
  const order = mockOrders.find(o => o.id === id);
  if (order) {
    order.status = status;
    showNotification(`Đơn ${id}: cập nhật trạng thái → ${status}`);
  }
}

function filterOrders() {
  const q = document.getElementById('orderSearch').value.toLowerCase();
  const status = document.getElementById('orderStatusFilter').value;
  const filtered = mockOrders.filter(o => {
    const matchQ = o.id.toLowerCase().includes(q) || o.customer.toLowerCase().includes(q);
    const matchStatus = !status || o.status === status;
    return matchQ && matchStatus;
  });
  renderOrderManagement(filtered);
}

// ============================================================
//  PAYMENT VERIFICATION
// ============================================================

function renderPaymentQueue() {
  const tbody = document.getElementById('paymentVerifBody');
  if (!tbody) return;
  const queue = mockPaymentQueue.filter(p => p.status === 'pending');
  tbody.innerHTML = queue.map(p => `
    <tr>
      <td><strong>${p.orderId}</strong></td>
      <td>${p.customer}</td>
      <td class="fw-600">${formatPrice(p.amount)}</td>
      <td>${p.method}</td>
      <td><div class="payment-img-placeholder">Ảnh CK</div></td>
      <td>${formatDateTime(p.time)}</td>
      <td>
        <div class="actions-cell">
          <button class="btn btn-xs btn-success" onclick="confirmPayment(${p.id})">Xác nhận</button>
          <button class="btn btn-xs btn-danger" onclick="openRejectPaymentModal(${p.id})">Từ chối</button>
        </div>
      </td>
    </tr>
  `).join('');
  if (!queue.length) {
    tbody.innerHTML = `<tr><td colspan="7" class="empty-state">Không có thanh toán chờ xác nhận</td></tr>`;
  }
}

function confirmPayment(id) {
  const item = mockPaymentQueue.find(p => p.id === id);
  if (item) {
    item.status = 'confirmed';
    const order = mockOrders.find(o => o.id === item.orderId);
    if (order) order.paymentStatus = 'paid';
    showNotification(`Đã xác nhận thanh toán đơn ${item.orderId}`);
    renderPaymentQueue();
  }
}

function openRejectPaymentModal(id) {
  currentPaymentId = id;
  const item = mockPaymentQueue.find(p => p.id === id);
  if (!item) return;
  document.getElementById('paymentModalTitle').textContent = 'Từ chối thanh toán';
  document.getElementById('paymentModalMsg').textContent = `Từ chối thanh toán đơn ${item.orderId} — ${formatPrice(item.amount)}`;
  document.getElementById('rejectReasonGroup').style.display = 'block';
  document.getElementById('paymentConfirmBtn').textContent = 'Xác nhận từ chối';
  document.getElementById('paymentConfirmBtn').onclick = () => rejectPayment(id);
  openModal('paymentModal');
}

function rejectPayment(id) {
  const item = mockPaymentQueue.find(p => p.id === id);
  const reason = document.getElementById('rejectReason').value;
  if (item) {
    item.status = 'rejected';
    showNotification(`Đã từ chối thanh toán đơn ${item.orderId}`, 'danger');
    closeModal('paymentModal');
    renderPaymentQueue();
  }
}

// ============================================================
//  RMA
// ============================================================

function renderRMATable(filter) {
  const tbody = document.getElementById('rmaTableBody');
  if (!tbody) return;
  const items = filter === 'all' ? mockRMA : mockRMA.filter(r => r.status === filter);
  tbody.innerHTML = items.map(r => `
    <tr>
      <td><strong>${r.id}</strong></td>
      <td>${r.orderId}</td>
      <td>${r.customer}</td>
      <td>${r.reason}</td>
      <td><span class="${r.type === 'exchange' ? 'rma-type-exchange' : 'rma-type-return'}">${r.type === 'exchange' ? 'Đổi hàng' : 'Trả hàng'}</span></td>
      <td>${r.amount ? formatPrice(r.amount) : '—'}</td>
      <td>${statusBadge(r.status)}</td>
      <td>
        <div class="actions-cell">
          ${r.status === 'pending' ? `
            <button class="btn btn-xs btn-success" onclick="approveRMA('${r.id}')">Duyệt</button>
            <button class="btn btn-xs btn-danger" onclick="rejectRMA('${r.id}')">Từ chối</button>
          ` : ''}
          ${r.status === 'approved' ? `<button class="btn btn-xs btn-outline" onclick="completeRMA('${r.id}')">Hoàn thành</button>` : ''}
        </div>
      </td>
    </tr>
  `).join('');
  if (!items.length) {
    tbody.innerHTML = `<tr><td colspan="8" class="empty-state">Không có yêu cầu</td></tr>`;
  }
}

function filterRMA(filter, btn) {
  document.querySelectorAll('#rmaTabs .tab-btn').forEach(b => b.classList.remove('active'));
  if (btn) btn.classList.add('active');
  renderRMATable(filter);
}

function approveRMA(id) {
  const r = mockRMA.find(x => x.id === id);
  if (r) { r.status = 'approved'; showNotification(`Đã duyệt yêu cầu ${id}`); renderRMATable('all'); }
}

function rejectRMA(id) {
  const r = mockRMA.find(x => x.id === id);
  if (r) { r.status = 'rejected'; showNotification(`Đã từ chối yêu cầu ${id}`, 'danger'); renderRMATable('all'); }
}

function completeRMA(id) {
  const r = mockRMA.find(x => x.id === id);
  if (r) { r.status = 'completed'; showNotification(`Yêu cầu ${id} hoàn thành`); renderRMATable('all'); }
}

// ============================================================
//  REVENUE
// ============================================================

let currentRevenuePeriod = 7;

function renderRevenuePage() {
  setRevenuePeriod(currentRevenuePeriod, document.querySelector('.range-btn.active'));
  renderTopRevenueProducts();
}

function setRevenuePeriod(days, btn) {
  currentRevenuePeriod = days;
  document.querySelectorAll('.range-btn').forEach(b => b.classList.remove('active'));
  if (btn) btn.classList.add('active');

  const fromInput = document.getElementById('revenueFrom');
  const toInput = document.getElementById('revenueTo');
  if (days === 0) {
    if (fromInput) fromInput.style.display = 'inline-block';
    if (toInput) toInput.style.display = 'inline-block';
  } else {
    if (fromInput) fromInput.style.display = 'none';
    if (toInput) toInput.style.display = 'none';
  }

  renderRevenueStats();
  drawRevenueBars();
  drawRevenueTrend();
}

function renderRevenueStats() {
  const revenue = mockRevenueMonthly.reduce((s, m) => s + m.revenue, 0);
  const orders = mockRevenueMonthly.reduce((s, m) => s + m.orders, 0);
  const avg = orders ? Math.round(revenue / orders) : 0;

  const kpiRevenue = document.getElementById('kpiRevenue');
  const kpiOrders = document.getElementById('kpiOrders');
  const kpiAvg = document.getElementById('kpiAvgOrder');
  const kpiReturn = document.getElementById('kpiReturnRate');

  if (kpiRevenue) kpiRevenue.textContent = formatPrice(revenue);
  if (kpiOrders) kpiOrders.textContent = orders.toLocaleString('vi-VN');
  if (kpiAvg) kpiAvg.textContent = formatPrice(avg);
  if (kpiReturn) kpiReturn.textContent = '2.4%';
}

function drawRevenueBars() {
  const data = mockRevenueMonthly.map(m => m.revenue);
  const labels = mockRevenueMonthly.map(m => m.month);
  drawBarChart('revenueBarChart', data, labels);

  const labelsContainer = document.getElementById('revenueChartLabels');
  if (labelsContainer) {
    labelsContainer.innerHTML = labels.map(l => `<span class="chart-label">${l}</span>`).join('');
  }
}

function drawRevenueTrend() {
  const data = mockRevenueMonthly.map(m => m.revenue);
  drawLineChart('revenueTrendChart', data);
}

function renderTopRevenueProducts() {
  const tbody = document.getElementById('topRevenueProductsBody');
  if (!tbody) return;
  const total = mockProducts.reduce((s, p) => s + p.price * p.sold, 0) || 1;
  const sorted = [...mockProducts].sort((a, b) => (b.price * b.sold) - (a.price * a.sold)).slice(0, 10);
  tbody.innerHTML = sorted.map((p, i) => {
    const rev = p.price * p.sold;
    return `
      <tr>
        <td>${i + 1}</td>
        <td class="fw-600">${p.name}</td>
        <td>${p.sold}</td>
        <td class="fw-600">${formatPrice(rev)}</td>
        <td>
          <div style="display:flex;align-items:center;gap:8px;">
            <div style="width:${Math.round(rev/total*100)}%;background:#2c2c2c;height:6px;border-radius:3px;min-width:4px;"></div>
            <span>${(rev / total * 100).toFixed(1)}%</span>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

// ============================================================
//  WISHLIST ANALYTICS
// ============================================================

function renderWishlistPage() {
  renderWishlistTable();
  drawWishlistChart();
}

function renderWishlistTable() {
  const tbody = document.getElementById('wishlistTableBody');
  if (!tbody) return;
  tbody.innerHTML = mockWishlistData.map((w, i) => `
    <tr>
      <td>${i + 1}</td>
      <td class="fw-600">${w.product}</td>
      <td>${w.wishlistCount.toLocaleString('vi-VN')}</td>
      <td>${w.purchaseRate}</td>
      <td>${w.potential ? formatPrice(w.potential) : '—'}</td>
    </tr>
  `).join('');
}

function drawWishlistChart() {
  const data = mockWishlistData.slice(0, 10).map(w => w.wishlistCount);
  const labels = mockWishlistData.slice(0, 10).map(w => w.product.split(' ').slice(0, 2).join(' '));
  drawBarChart('wishlistChart', data, labels, '#4CAF50');
}

// ============================================================
//  CUSTOMERS
// ============================================================

function renderCustomersTable(customers) {
  const tbody = document.getElementById('customersTableBody');
  if (!tbody) return;
  tbody.innerHTML = customers.map(c => `
    <tr>
      <td>
        <div class="table-person">
          <div class="table-avatar">${c.name.charAt(0)}</div>
          <span class="table-person-name">${c.name}</span>
        </div>
      </td>
      <td>${c.email}</td>
      <td>${c.phone}</td>
      <td>${c.orders}</td>
      <td class="fw-600">${formatPrice(c.spent)}</td>
      <td>${formatDate(c.joined)}</td>
      <td>
        <div class="actions-cell">
          <button class="btn btn-xs btn-outline" onclick="openCustomerModal(${c.id})">Xem hồ sơ</button>
        </div>
      </td>
    </tr>
  `).join('');
}

function searchCustomers() {
  const q = document.getElementById('customerSearch').value.toLowerCase();
  const filtered = mockCustomers.filter(c =>
    c.name.toLowerCase().includes(q) ||
    c.email.toLowerCase().includes(q) ||
    c.phone.includes(q)
  );
  renderCustomersTable(filtered);
}

function openCustomerModal(id) {
  const c = mockCustomers.find(x => x.id === id);
  if (!c) return;
  document.getElementById('customerModalName').textContent = c.name;

  const recentOrders = mockOrders.filter(o => o.customer === c.name).slice(0, 5);
  const orderRows = recentOrders.length
    ? recentOrders.map(o => `<tr><td>${o.id}</td><td>${formatPrice(o.total)}</td><td>${statusBadge(o.status)}</td><td>${formatDateTime(o.created)}</td></tr>`).join('')
    : '<tr><td colspan="4" class="empty-state">Chưa có đơn hàng</td></tr>';

  document.getElementById('customerModalBody').innerHTML = `
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:16px;margin-bottom:16px;">
      <div>
        <h4 style="font-size:12px;color:#666;margin-bottom:6px;text-transform:uppercase;letter-spacing:.05em;">Thông tin liên hệ</h4>
        <p style="font-size:13px;line-height:1.7">${c.name}<br>${c.email}<br>${c.phone}</p>
      </div>
      <div>
        <h4 style="font-size:12px;color:#666;margin-bottom:6px;text-transform:uppercase;letter-spacing:.05em;">Thống kê</h4>
        <p style="font-size:13px;line-height:1.7">
          Tổng đơn: <strong>${c.orders}</strong><br>
          Tổng chi tiêu: <strong>${formatPrice(c.spent)}</strong><br>
          Đăng ký: ${formatDate(c.joined)}
        </p>
      </div>
    </div>
    <h4 style="font-size:12px;color:#666;margin-bottom:8px;text-transform:uppercase;letter-spacing:.05em;">Đơn hàng gần đây</h4>
    <table class="admin-table">
      <thead><tr><th>Mã đơn</th><th>Tổng tiền</th><th>Trạng thái</th><th>Thời gian</th></tr></thead>
      <tbody>${orderRows}</tbody>
    </table>
    <h4 style="font-size:12px;color:#666;margin-top:14px;margin-bottom:6px;text-transform:uppercase;letter-spacing:.05em;">Địa chỉ</h4>
    <ul style="padding-left:16px;font-size:13px;">${c.addresses.map(a => `<li>${a}</li>`).join('')}</ul>
  `;
  openModal('customerModal');
}

// ============================================================
//  FEEDBACK MODERATION
// ============================================================

function renderFeedbackCards(filter) {
  const grid = document.getElementById('feedbackGrid');
  if (!grid) return;
  const items = mockReviews.filter(r => r.status === filter);

  if (!items.length) {
    grid.innerHTML = '<div class="empty-state">Không có đánh giá</div>';
    return;
  }

  grid.innerHTML = items.map(r => `
    <div class="review-card" id="review-${r.id}">
      <div class="review-card-header">
        <div class="review-product-img">[Ảnh]</div>
        <div class="review-product-info">
          <div class="review-product-name">${r.product}</div>
          <div class="review-reviewer">${r.reviewer}</div>
          <div class="review-date">${formatDate(r.date)}</div>
        </div>
        ${statusBadge(r.status)}
      </div>
      ${starRating(r.stars)}
      <div class="review-content">${r.content}</div>
      ${r.images ? `<div class="review-images">${Array(r.images).fill('<div class="review-img-placeholder">Ảnh</div>').join('')}</div>` : ''}
      ${r.status === 'pending' ? `
      <div class="review-actions">
        <button class="btn btn-xs btn-success" onclick="approveFeedback(${r.id})">Duyệt</button>
        <button class="btn btn-xs btn-danger" onclick="rejectFeedback(${r.id})">Từ chối</button>
      </div>` : ''}
    </div>
  `).join('');
}

function filterFeedback(filter, btn) {
  document.querySelectorAll('#feedbackTabs .tab-btn').forEach(b => b.classList.remove('active'));
  if (btn) btn.classList.add('active');
  renderFeedbackCards(filter);
}

function approveFeedback(id) {
  const r = mockReviews.find(x => x.id === id);
  if (r) {
    r.status = 'approved';
    showNotification('Đã duyệt đánh giá');
    renderFeedbackCards('pending');
  }
}

function rejectFeedback(id) {
  const r = mockReviews.find(x => x.id === id);
  if (r) {
    r.status = 'rejected';
    showNotification('Đã từ chối đánh giá', 'danger');
    renderFeedbackCards('pending');
  }
}

// ============================================================
//  PAGINATION
// ============================================================

function renderPagination(containerId, current, total) {
  const container = document.getElementById(containerId);
  if (!container || total <= 1) {
    if (container) container.innerHTML = '';
    return;
  }
  let html = `<button class="page-btn" ${current === 1 ? 'disabled' : ''} onclick="goToPage('${containerId}', ${current - 1})">‹</button>`;
  for (let i = 1; i <= total; i++) {
    html += `<button class="page-btn ${i === current ? 'active' : ''}" onclick="goToPage('${containerId}', ${i})">${i}</button>`;
  }
  html += `<button class="page-btn" ${current === total ? 'disabled' : ''} onclick="goToPage('${containerId}', ${current + 1})">›</button>`;
  container.innerHTML = html;
}

function goToPage(containerId, page) {
  showNotification(`Trang ${page} (phân trang demo)`, 'info');
}

// ============================================================
//  INIT
// ============================================================

document.addEventListener('DOMContentLoaded', () => {
  initRouter();

  // Close modals on backdrop click
  document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', e => {
      if (e.target === overlay) overlay.classList.remove('open');
    });
  });

  // Enter key on chip inputs
  document.getElementById('colorInput').addEventListener('keydown', e => {
    if (e.key === 'Enter') { e.preventDefault(); addVariantChip('color'); }
  });
  document.getElementById('sizeInput').addEventListener('keydown', e => {
    if (e.key === 'Enter') { e.preventDefault(); addVariantChip('size'); }
  });

  console.log('%c MINIMAL ADMIN — Fashion Store Dashboard ', 'background:#2c2c2c;color:#fff;padding:4px 8px;font-weight:bold;');
  console.log('SPA loaded. Navigate via sidebar or URL hash.');
});
