(function () {
  'use strict';

  document.addEventListener('DOMContentLoaded', function () {
    initOrdersListPage();
    initOrderDetailPage();
  });

  function initOrdersListPage() {
    var page = document.querySelector('.orders-list-page');
    if (!page) return;

    // Normalize query form UX: submit with Enter in keyword and keep date ranges valid.
    var filterForm = page.querySelector('form[action*="/admin/orders"]');
    if (!filterForm) return;

    var startDateInput = filterForm.querySelector('input[name="startDate"]');
    var endDateInput = filterForm.querySelector('input[name="endDate"]');

    if (startDateInput && endDateInput) {
      endDateInput.addEventListener('change', function () {
        if (!startDateInput.value || !endDateInput.value) return;
        if (new Date(endDateInput.value) < new Date(startDateInput.value)) {
          endDateInput.value = startDateInput.value;
        }
      });
    }
  }

  function initOrderDetailPage() {
    var page = document.querySelector('.order-detail-page');
    if (!page) return;

    var statusForm = page.querySelector('form[action*="/status"]');
    if (!statusForm) return;

    statusForm.addEventListener('submit', function (event) {
      var statusSelect = statusForm.querySelector('select[name="newStatus"]');
      if (!statusSelect || !statusSelect.value) {
        event.preventDefault();
        if (window.AdminUI && typeof window.AdminUI.showToast === 'function') {
          window.AdminUI.showToast('Vui long chon trang thai don hang.', 'warning');
        }
      }
    });
  }
})();
