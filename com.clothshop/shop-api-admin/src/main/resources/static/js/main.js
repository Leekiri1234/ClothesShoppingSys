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

  const label = document.createElement('span');
  label.textContent = value;

  const removeButton = document.createElement('button');
  removeButton.type = 'button';
  removeButton.className = 'chip-remove';
  removeButton.textContent = '×';
  removeButton.addEventListener('click', () => {
    chip.remove();
  });

  chip.appendChild(label);
  chip.appendChild(removeButton);
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
  closeSidebar
};

// ============================================================
// 11. CHART RENDERING (Simple SVG Bar Chart)
// ============================================================
function renderBarChart(containerId, data) {
  const container = document.getElementById(containerId);
  if (!container) return;
  if (!data || data.length === 0) {
    container.innerHTML = '<p style="text-align:center;color:var(--text-muted);padding:1rem;">No data available</p>';
    return;
  }

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

