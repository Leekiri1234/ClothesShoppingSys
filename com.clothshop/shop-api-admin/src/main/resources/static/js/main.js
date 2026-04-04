/**
 * ClothShop Admin - Main JavaScript
 * Handles all interactive UI behaviors for admin dashboard
 */

// ============================================================
// 1. GLOBAL STATE
// ============================================================
const APP_STATE = {
  currentPage: 'dashboard',
  sidebarOpen: false,
  modalStack: []
};

const LEGACY_DASHBOARD_DATA = {
  revenueByDay: [12.4, 14.2, 13.1, 15.8, 16.7, 18.1, 17.3],
  labels: ['T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'CN'],
  recentOrders: [
    { id: 'ORD-1001', customer: 'Nguyễn Thị Bích', total: '1.230.000₫', status: 'pending' },
    { id: 'ORD-1002', customer: 'Trần Hoàng Long', total: '450.000₫', status: 'confirmed' },
    { id: 'ORD-1003', customer: 'Lê Thị Kim Oanh', total: '980.000₫', status: 'preparing' },
    { id: 'ORD-1004', customer: 'Phạm Quốc Anh', total: '1.860.000₫', status: 'shipping' }
  ],
  topProducts: [
    { name: 'Áo Linen Cổ V', sold: 156, revenue: '12.45M' },
    { name: 'Váy Midi Floral', sold: 124, revenue: '9.10M' },
    { name: 'Quần Wide-Leg Kaki', sold: 98, revenue: '7.80M' },
    { name: 'Áo Thun Basic', sold: 89, revenue: '6.30M' }
  ]
};

// ============================================================
// 2. DOM READY INITIALIZATION
// ============================================================
document.addEventListener('DOMContentLoaded', () => {
  initSidebarNav();
  initHamburger();
  initSidebarOverlay();
  initModals();
  initToasts();
  initTables();
  initForms();
  initConfirmDelete();
  initLegacyDashboard();
  console.log('✅ Admin Dashboard JS Initialized');
});

// ============================================================
// 3. SIDEBAR NAVIGATION (SPA-like routing)
// ============================================================
function initSidebarNav() {
  const navLinks = document.querySelectorAll('.nav-link');
  const pages = document.querySelectorAll('.admin-page');
  const pageTitle = document.querySelector('.page-title');

  navLinks.forEach(link => {
    link.addEventListener('click', (e) => {
      const targetPage = link.getAttribute('data-page');

      if (!targetPage) return; // Skip if no data-page

      e.preventDefault();

      // Update active nav
      navLinks.forEach(l => l.classList.remove('active'));
      link.classList.add('active');

      // Show target page
      pages.forEach(p => p.classList.remove('active'));
      const targetPageEl = document.getElementById(`page-${targetPage}`);
      if (targetPageEl) {
        targetPageEl.classList.add('active');
      }

      // Update header title
      if (pageTitle) {
        pageTitle.textContent = link.textContent.trim();
      }

      // Store state
      APP_STATE.currentPage = targetPage;

      // Close sidebar on mobile
      if (window.innerWidth <= 768) {
        closeSidebar();
      }
    });
  });
}

// ============================================================
// 4. HAMBURGER MENU (Mobile)
// ============================================================
function initHamburger() {
  const hamburger = document.querySelector('.hamburger-btn');
  if (!hamburger) return;

  hamburger.addEventListener('click', () => {
    toggleSidebar();
  });
}

function initSidebarOverlay() {
  const overlay = document.querySelector('.sidebar-overlay');
  if (!overlay) return;

  overlay.addEventListener('click', () => {
    closeSidebar();
  });
}

function toggleSidebar() {
  const sidebar = document.querySelector('.admin-sidebar');
  const overlay = document.querySelector('.sidebar-overlay');

  if (!sidebar) return;

  APP_STATE.sidebarOpen = !APP_STATE.sidebarOpen;

  if (APP_STATE.sidebarOpen) {
    sidebar.classList.add('open');
    if (overlay) overlay.classList.add('show');
  } else {
    sidebar.classList.remove('open');
    if (overlay) overlay.classList.remove('show');
  }
}

function closeSidebar() {
  const sidebar = document.querySelector('.admin-sidebar');
  const overlay = document.querySelector('.sidebar-overlay');

  if (!sidebar) return;

  APP_STATE.sidebarOpen = false;
  sidebar.classList.remove('open');
  if (overlay) overlay.classList.remove('show');
}

// ============================================================
// 5. MODALS
// ============================================================
function initModals() {
  // Auto-bind modal triggers
  document.querySelectorAll('[data-modal]').forEach(trigger => {
    trigger.addEventListener('click', (e) => {
      e.preventDefault();
      const modalId = trigger.getAttribute('data-modal');
      openModal(modalId);
    });
  });

  // Close buttons
  document.querySelectorAll('.modal-close').forEach(btn => {
    btn.addEventListener('click', () => {
      const modal = btn.closest('.modal-overlay');
      if (modal) closeModal(modal.id);
    });
  });

  // Click outside to close
  document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) {
        closeModal(overlay.id);
      }
    });
  });
}

function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (!modal) return;

  modal.classList.add('open');
  APP_STATE.modalStack.push(modalId);
  document.body.style.overflow = 'hidden';
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (!modal) return;

  modal.classList.remove('open');
  APP_STATE.modalStack = APP_STATE.modalStack.filter(id => id !== modalId);

  if (APP_STATE.modalStack.length === 0) {
    document.body.style.overflow = '';
  }
}

// ============================================================
// 6. TOAST NOTIFICATIONS
// ============================================================
function initToasts() {
  // Auto-dismiss existing toasts
  document.querySelectorAll('.toast').forEach(toast => {
    setTimeout(() => toast.remove(), 5000);
  });
}

function showToast(message, type = 'info') {
  const container = document.querySelector('.toast-container') || createToastContainer();

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.textContent = message;

  container.appendChild(toast);

  setTimeout(() => toast.remove(), 5000);
}

function createToastContainer() {
  const container = document.createElement('div');
  container.className = 'toast-container';
  document.body.appendChild(container);
  return container;
}

// ============================================================
// 7. TABLE INTERACTIONS
// ============================================================
function initTables() {
  initTableSort();
  initTableRowSelect();
  initExpandableRows();
}

function initTableSort() {
  document.querySelectorAll('.admin-table th[data-sort]').forEach(th => {
    th.addEventListener('click', () => {
      const table = th.closest('table');
      const column = th.getAttribute('data-sort');
      sortTable(table, column);
    });
  });
}

function sortTable(table, column) {
  // Simple client-side sort - can be replaced with server-side
  const tbody = table.querySelector('tbody');
  const rows = Array.from(tbody.querySelectorAll('tr'));

  rows.sort((a, b) => {
    const aVal = a.querySelector(`td[data-col="${column}"]`)?.textContent || '';
    const bVal = b.querySelector(`td[data-col="${column}"]`)?.textContent || '';
    return aVal.localeCompare(bVal);
  });

  rows.forEach(row => tbody.appendChild(row));
}

function initTableRowSelect() {
  document.querySelectorAll('.admin-table tbody tr').forEach(row => {
    row.addEventListener('click', (e) => {
      if (e.target.tagName === 'BUTTON' || e.target.tagName === 'A') return;
      row.classList.toggle('selected-row');
    });
  });
}

function initExpandableRows() {
  document.querySelectorAll('.order-expand-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const tr = btn.closest('tr');
      const detailRow = tr.nextElementSibling;

      if (detailRow && detailRow.classList.contains('order-detail-row')) {
        detailRow.style.display = detailRow.style.display === 'none' ? 'table-row' : 'none';
        btn.classList.toggle('open');
      }
    });
  });
}

// ============================================================
// 8. FORMS & INPUT HANDLING
// ============================================================
function initForms() {
  initFormValidation();
  initToggleSwitches();
  initChipInputs();
  initFileUploads();
}

function initFormValidation() {
  document.querySelectorAll('form[data-validate]').forEach(form => {
    form.addEventListener('submit', (e) => {
      if (!form.checkValidity()) {
        e.preventDefault();
        showToast('Vui lòng điền đầy đủ thông tin', 'warning');
      }
    });
  });
}

function initToggleSwitches() {
  document.querySelectorAll('.toggle-switch input[type="checkbox"]').forEach(input => {
    // Skip if element already has an onchange handler
    if (input.hasAttribute('onchange')) {
      return;
    }

    input.addEventListener('change', () => {
      console.log(`Toggle changed: ${input.checked}`);
      // Implement your logic here (e.g., AJAX call to update status)
    });
  });
}

function initChipInputs() {
  document.querySelectorAll('.chip-input-row').forEach(row => {
    const input = row.querySelector('input');
    const container = row.nextElementSibling;

    if (!input || !container) return;

    input.addEventListener('keypress', (e) => {
      if (e.key === 'Enter') {
        e.preventDefault();
        const value = input.value.trim();
        if (value) {
          addChip(container, value);
          input.value = '';
        }
      }
    });
  });
}

function addChip(container, value) {
  const chip = document.createElement('div');
  chip.className = 'chip';
  chip.innerHTML = `
    <span>${value}</span>
    <button type="button" class="chip-remove" onclick="this.parentElement.remove()">×</button>
  `;
  container.appendChild(chip);
}

function initFileUploads() {
  document.querySelectorAll('.upload-zone, .image-thumb-zone').forEach(zone => {
    zone.addEventListener('click', () => {
      const input = document.createElement('input');
      input.type = 'file';
      input.accept = 'image/*';
      input.onchange = (e) => {
        const file = e.target.files[0];
        if (file) {
          handleFileUpload(zone, file);
        }
      };
      input.click();
    });
  });
}

function handleFileUpload(zone, file) {
  const reader = new FileReader();
  reader.onload = (e) => {
    zone.style.backgroundImage = `url(${e.target.result})`;
    zone.style.backgroundSize = 'cover';
    zone.style.backgroundPosition = 'center';
    zone.textContent = '';
    showToast(`Đã tải lên: ${file.name}`, 'success');
  };
  reader.readAsDataURL(file);
}

// ============================================================
// 9. CONFIRM DELETE
// ============================================================
function initConfirmDelete() {
  document.querySelectorAll('[data-confirm-delete]').forEach(btn => {
    btn.addEventListener('click', (e) => {
      if (!confirm('Bạn có chắc chắn muốn xóa? Hành động này không thể hoàn tác.')) {
        e.preventDefault();
      }
    });
  });
}

// ============================================================
// 10. UTILITY FUNCTIONS (Public API)
// ============================================================
window.AdminUI = {
  openModal,
  closeModal,
  showToast,
  toggleSidebar,
  closeSidebar,
  initLegacyDashboard
};

// ============================================================
// 11. CHART RENDERING (Simple SVG Bar Chart)
// ============================================================
function renderBarChart(containerId, data) {
  const container = document.getElementById(containerId);
  if (!container) return;

  const width = container.clientWidth;
  const height = 200;
  const maxValue = Math.max(...data.map(d => d.value));
  const barWidth = width / data.length;

  let svg = `<svg width="${width}" height="${height}" style="display: block;">`;

  data.forEach((item, i) => {
    const barHeight = (item.value / maxValue) * (height - 30);
    const x = i * barWidth + barWidth * 0.2;
    const y = height - barHeight - 20;

    svg += `<rect class="chart-bar" x="${x}" y="${y}" width="${barWidth * 0.6}" height="${barHeight}" fill="var(--primary)" rx="2"/>`;
  });

  svg += '</svg>';
  container.innerHTML = svg;
}

function initLegacyDashboard() {
  const chartSvg = document.getElementById('revenueChartSvg');
  const labels = document.getElementById('chartLabels');
  const ordersBody = document.querySelector('#recentOrdersTable tbody');
  const topProducts = document.getElementById('topProductsList');

  if (!chartSvg || !labels || !ordersBody || !topProducts) {
    return;
  }

  const dashboardData = (window.__dashboardData
    && Array.isArray(window.__dashboardData.revenueByDay)
    && Array.isArray(window.__dashboardData.labels)
    && Array.isArray(window.__dashboardData.recentOrders)
    && Array.isArray(window.__dashboardData.topProducts))
    ? window.__dashboardData
    : LEGACY_DASHBOARD_DATA;

  initDashboardTodayText();
  renderLegacyRevenueChart(chartSvg, dashboardData.revenueByDay);
  renderLegacyChartLabels(labels, dashboardData.labels);
  renderLegacyRecentOrders(ordersBody, dashboardData.recentOrders);
  renderLegacyTopProducts(topProducts, dashboardData.topProducts);
}

function initDashboardTodayText() {
  const target = document.getElementById('dashboardTodayText');
  if (!target) return;

  const now = new Date();
  const day = String(now.getDate()).padStart(2, '0');
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const year = now.getFullYear();
  target.textContent = `Xin chào, hôm nay là ngày ${day}/${month}/${year}`;
}

function renderLegacyRevenueChart(svg, data) {
  const width = 700;
  const height = 200;
  const paddingX = 40;
  const paddingY = 24;
  const max = Math.max(...data);
  const min = Math.min(...data);
  const range = Math.max(max - min, 1);
  const stepX = (width - paddingX * 2) / (data.length - 1);

  const points = data.map((value, idx) => {
    const x = paddingX + idx * stepX;
    const y = height - paddingY - ((value - min) / range) * (height - paddingY * 2);
    return { x, y, value };
  });

  const polyline = points.map((point) => `${point.x},${point.y}`).join(' ');

  svg.innerHTML = [
    `<polyline points="${polyline}" fill="none" stroke="var(--primary)" stroke-width="2" />`,
    ...points.map((point) => `<circle cx="${point.x}" cy="${point.y}" r="3" fill="var(--primary)" />`)
  ].join('');
}

function renderLegacyChartLabels(container, labels) {
  container.innerHTML = labels.map((label) => `<span>${label}</span>`).join('');
}

function renderLegacyRecentOrders(tbody, rows) {
  tbody.innerHTML = rows.map((row) => {
    return `
      <tr>
        <td>${row.id}</td>
        <td>${row.customer}</td>
        <td>${row.total}</td>
        <td><span class="badge ${resolveOrderBadgeClass(row.status)}">${resolveOrderStatusText(row.status)}</span></td>
      </tr>
    `;
  }).join('');
}

function renderLegacyTopProducts(container, products) {
  container.innerHTML = products.map((product, index) => {
    return `
      <div class="top-product-item">
        <div class="top-product-rank ${index === 0 ? 'gold' : index === 1 ? 'silver' : index === 2 ? 'bronze' : ''}">${index + 1}</div>
        <div class="top-product-info">
          <div class="top-product-name">${product.name}</div>
          <div class="top-product-sold">Đã bán: ${product.sold}</div>
        </div>
        <div class="top-product-revenue">${product.revenue}</div>
      </div>
    `;
  }).join('');
}

function resolveOrderBadgeClass(status) {
  const normalizedStatus = (status || '').toLowerCase();
  const map = {
    pending: 'badge-warning',
    confirmed: 'badge-info',
    preparing: 'badge-primary',
    shipping: 'badge-info',
    delivered: 'badge-success',
    completed: 'badge-success',
    cancelled: 'badge-danger',
    returned: 'badge-muted'
  };
  return map[normalizedStatus] || 'badge-muted';
}

function resolveOrderStatusText(status) {
  const normalizedStatus = (status || '').toLowerCase();
  const map = {
    pending: 'Chờ xác nhận',
    confirmed: 'Đã xác nhận',
    preparing: 'Đang chuẩn bị',
    shipping: 'Đang giao',
    delivered: 'Đã giao',
    completed: 'Hoàn thành',
    cancelled: 'Đã hủy',
    returned: 'Đã trả hàng'
  };
  return map[normalizedStatus] || 'Không rõ';
}

// Note: Call renderBarChart() from your page-specific scripts with actual data from backend
// Example: renderBarChart('revenueChart', chartDataFromBackend);

// ============================================================
// 12. FILTER TABS
// ============================================================
document.querySelectorAll('.tab-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    // Implement filter logic here
  });
});

// ============================================================
// 13. DATE RANGE SELECTOR
// ============================================================
document.querySelectorAll('.range-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('.range-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    // Implement date range logic here
  });
});

