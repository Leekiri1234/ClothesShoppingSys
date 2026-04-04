(function () {
  'use strict';

  document.addEventListener('DOMContentLoaded', function () {
    initProductsListPage();
    initProductsCreatePage();
    initProductsEditPage();
    initProductsVariantStockPage();
  });

  function initProductsListPage() {
    var page = document.querySelector('.products-list-page');
    if (!page) return;

    var selectAll = document.getElementById('selectAll');
    var rowCheckboxes = page.querySelectorAll('.row-checkbox');
    var bulkActions = document.getElementById('bulkActions');
    var selectedCount = document.getElementById('selectedCount');
    var applyFiltersButton = document.getElementById('productsApplyFiltersBtn');
    var resetFiltersButton = document.getElementById('productsResetFiltersBtn');
    var bulkActivateButton = document.getElementById('productsBulkActivateBtn');
    var bulkDeactivateButton = document.getElementById('productsBulkDeactivateBtn');
    var bulkDeleteButton = document.getElementById('productsBulkDeleteBtn');
    var searchInput = document.getElementById('searchInput');
    var statusFilter = document.getElementById('statusFilter');
    var statusTabs = page.querySelectorAll('.filter-tabs .tab-btn[data-status]');

    if (selectAll) {
      selectAll.addEventListener('change', function () {
        rowCheckboxes.forEach(function (checkbox) {
          checkbox.checked = selectAll.checked;
        });
        updateBulkActions();
      });
    }

    rowCheckboxes.forEach(function (checkbox) {
      checkbox.addEventListener('change', updateBulkActions);
    });

    if (applyFiltersButton) {
      applyFiltersButton.addEventListener('click', applyFilters);
    }

    if (searchInput) {
      searchInput.addEventListener('keydown', function (event) {
        if (event.key !== 'Enter') return;
        event.preventDefault();
        applyFilters();
      });
    }

    if (resetFiltersButton) {
      resetFiltersButton.addEventListener('click', function () {
        window.location.href = '/admin/products';
      });
    }

    statusTabs.forEach(function (tab) {
      tab.addEventListener('click', function () {
        var status = tab.dataset.status || 'all';
        if (!statusFilter) return;
        statusFilter.value = status === 'all' ? '' : status;
        applyFilters();
      });
    });

    if (statusFilter) {
      var currentStatus = (statusFilter.value || '').toLowerCase();
      statusTabs.forEach(function (tab) {
        var tabStatus = (tab.dataset.status || '').toLowerCase();
        var active = (currentStatus === '' && tabStatus === 'all') || (currentStatus !== '' && tabStatus === currentStatus);
        tab.classList.toggle('active', active);
      });
    }

    if (bulkActivateButton) {
      bulkActivateButton.addEventListener('click', function () {
        var ids = getSelectedIds();
        if (!ids.length) return;
        postBulkAction('/admin/products/bulk/activate', ids, 'Da kich hoat san pham thanh cong.');
      });
    }

    if (bulkDeactivateButton) {
      bulkDeactivateButton.addEventListener('click', function () {
        var ids = getSelectedIds();
        if (!ids.length) return;
        postBulkAction('/admin/products/bulk/deactivate', ids, 'Da ngung ban san pham thanh cong.');
      });
    }

    if (bulkDeleteButton) {
      bulkDeleteButton.addEventListener('click', function () {
        var ids = getSelectedIds();
        if (!ids.length) return;
        if (!window.confirm('Ban co chac chan muon xoa ' + ids.length + ' san pham da chon?')) return;
        postBulkAction('/admin/products/bulk/delete', ids, 'Da an san pham da chon.');
      });
    }

    page.querySelectorAll('.stock-input').forEach(function (input) {
      input.addEventListener('change', function () {
        var originalValue = input.defaultValue;
        input.value = originalValue;
        notify('Ton kho tong duoc tinh tu variants. Vui long cap nhat trong trang bien the.', 'info');
      });
    });

    page.querySelectorAll('.product-toggle-form').forEach(function (form) {
      form.addEventListener('submit', function (event) {
        var message = form.dataset.confirmMessage || 'Ban co chac chan muon cap nhat trang thai san pham?';
        if (!window.confirm(message)) {
          event.preventDefault();
        }
      });
    });

    function applyFilters() {
      var search = document.getElementById('searchInput')?.value || '';
      var category = document.getElementById('categoryFilter')?.value || '';
      var status = document.getElementById('statusFilter')?.value || '';
      var url = new URL(window.location.href);

      if (search) url.searchParams.set('search', search);
      else url.searchParams.delete('search');

      if (category) url.searchParams.set('category', category);
      else url.searchParams.delete('category');

      if (status) url.searchParams.set('status', status);
      else url.searchParams.delete('status');

      url.searchParams.set('page', '0');
      window.location.href = url.toString();
    }

    function updateBulkActions() {
      if (!bulkActions || !selectedCount) return;
      var selected = getSelectedIds();
      bulkActions.style.display = selected.length ? 'block' : 'none';
      selectedCount.textContent = String(selected.length);
    }

    function getSelectedIds() {
      return Array.from(page.querySelectorAll('.row-checkbox:checked')).map(function (checkbox) {
        return checkbox.value;
      });
    }

    function postBulkAction(url, ids, successMessage) {
      var csrf = getCsrfData();
      if (!csrf.token) {
        notify('Khong tim thay CSRF token.', 'danger');
        return;
      }

      var formData = new URLSearchParams();
      ids.forEach(function (id) {
        formData.append('productIds', id);
      });

      fetch(url, {
        method: 'POST',
        headers: Object.assign(
          {
            'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
          },
          csrf.header ? { [csrf.header]: csrf.token } : { 'X-CSRF-TOKEN': csrf.token }
        ),
        body: formData.toString()
      })
        .then(function (response) {
          if (!response.ok) {
            throw new Error('Bulk action failed');
          }
          return response.json();
        })
        .then(function (result) {
          notify(result.message || successMessage, 'success');
          setTimeout(function () {
            window.location.reload();
          }, 600);
        })
        .catch(function () {
          notify('Thao tac that bai. Vui long thu lai.', 'danger');
        });
    }
  }

  function initProductsCreatePage() {
    var page = document.querySelector('.products-create-page');
    if (!page) return;

    initImagePreview(['mainImageZone', 'image1Zone', 'image2Zone']);
    initVariantBuilder(page);
  }

  function initProductsEditPage() {
    var page = document.querySelector('.products-edit-page');
    if (!page) return;

    initImagePreview(['mainImageZone', 'image1Zone', 'image2Zone']);

    var toggleButton = document.getElementById('toggleProductStatusBtn');
    if (!toggleButton) return;

    toggleButton.addEventListener('click', function () {
      var productId = page.querySelector('input[name="productId"]')?.value;
      var csrfName = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
      var csrfToken = document.querySelector('meta[name="_csrf"]')?.content;

      if (!productId || !csrfToken) {
        notify('Thieu thong tin CSRF hoac Product ID.', 'danger');
        return;
      }

      fetch('/admin/products/' + productId + '/toggle-status', {
        method: 'POST',
        headers: {
          [csrfName]: csrfToken
        }
      })
        .then(function (response) {
          if (!response.ok) throw new Error('Toggle failed');
          window.location.reload();
        })
        .catch(function () {
          notify('Khong the thay doi trang thai san pham.', 'danger');
        });
    });
  }

  function initProductsVariantStockPage() {
    var page = document.querySelector('.products-variant-stock-page');
    if (!page) return;

    var currentStockElement = document.getElementById('currentStock');
    var updateTypeSelect = document.getElementById('updateType');
    var quantityInput = page.querySelector('input[name="quantity"]');
    var previewResult = document.getElementById('previewResult');
    var operationElement = document.getElementById('operation');
    var newStockElement = document.getElementById('newStock');

    if (!currentStockElement || !updateTypeSelect || !quantityInput || !previewResult || !operationElement || !newStockElement) {
      return;
    }

    var currentStock = parseInt(currentStockElement.textContent, 10) || 0;

    function updatePreview() {
      var updateType = updateTypeSelect.value;
      var quantity = parseInt(quantityInput.value, 10) || 0;

      if (!updateType || quantity <= 0) {
        previewResult.style.display = 'none';
        return;
      }

      previewResult.style.display = 'block';
      var newStock = currentStock;

      if (updateType === 'ADD') {
        newStock = currentStock + quantity;
        operationElement.textContent = '+' + quantity + ' ->';
      } else if (updateType === 'SUBTRACT') {
        newStock = Math.max(0, currentStock - quantity);
        operationElement.textContent = '-' + quantity + ' ->';
      } else {
        newStock = quantity;
        operationElement.textContent = '->';
      }

      newStockElement.textContent = String(newStock);
      if (newStock === 0) newStockElement.style.color = 'var(--danger)';
      else if (newStock < 10) newStockElement.style.color = 'var(--warning)';
      else newStockElement.style.color = 'var(--success)';
    }

    updateTypeSelect.addEventListener('change', updatePreview);
    quantityInput.addEventListener('input', updatePreview);
  }

  function initImagePreview(zoneIds) {
    zoneIds.forEach(function (zoneId) {
      var zone = document.getElementById(zoneId);
      if (!zone) return;

      var input = zone.querySelector('input[type="file"]');
      if (!input) return;

      zone.addEventListener('click', function () {
        input.click();
      });

      input.addEventListener('change', function (event) {
        var file = event.target.files && event.target.files[0];
        if (!file) return;

        var reader = new FileReader();
        reader.onload = function (readerEvent) {
          zone.style.backgroundImage = 'url(' + readerEvent.target.result + ')';
          zone.style.backgroundSize = 'cover';
          zone.style.backgroundPosition = 'center';
          zone.textContent = '';
        };
        reader.readAsDataURL(file);
      });
    });
  }

  function initVariantBuilder(page) {
    var colorInput = document.getElementById('colorInput');
    var sizeInput = document.getElementById('sizeInput');
    var colorContainer = document.getElementById('colorChips');
    var sizeContainer = document.getElementById('sizeChips');
    var matrix = document.getElementById('variantMatrix');
    var tableBody = document.getElementById('variantTableBody');

    if (!colorInput || !sizeInput || !colorContainer || !sizeContainer || !matrix || !tableBody) {
      return;
    }

    colorInput.addEventListener('keydown', function (event) {
      if (event.key !== 'Enter') return;
      event.preventDefault();
      addChip(colorContainer, colorInput.value.trim(), 'color');
      colorInput.value = '';
      renderVariantMatrix();
    });

    sizeInput.addEventListener('keydown', function (event) {
      if (event.key !== 'Enter') return;
      event.preventDefault();
      addChip(sizeContainer, sizeInput.value.trim(), 'size');
      sizeInput.value = '';
      renderVariantMatrix();
    });

    colorContainer.addEventListener('click', function (event) {
      if (!event.target.classList.contains('chip-remove')) return;
      event.preventDefault();
      event.target.closest('.chip')?.remove();
      renderVariantMatrix();
    });

    sizeContainer.addEventListener('click', function (event) {
      if (!event.target.classList.contains('chip-remove')) return;
      event.preventDefault();
      event.target.closest('.chip')?.remove();
      renderVariantMatrix();
    });

    function addChip(container, value, type) {
      if (!value) return;
      var chip = document.createElement('div');
      chip.className = 'chip';
      chip.innerHTML =
        '<span>' + escapeHtml(value) + '</span>' +
        '<input type="hidden" name="' + type + 's" value="' + escapeHtml(value) + '">' +
        '<button type="button" class="chip-remove">x</button>';
      container.appendChild(chip);
    }

    function renderVariantMatrix() {
      var colors = Array.from(colorContainer.querySelectorAll('.chip span')).map(function (element) {
        return element.textContent;
      });
      var sizes = Array.from(sizeContainer.querySelectorAll('.chip span')).map(function (element) {
        return element.textContent;
      });

      if (!colors.length || !sizes.length) {
        matrix.style.display = 'none';
        tableBody.innerHTML = '';
        return;
      }

      matrix.style.display = 'block';
      tableBody.innerHTML = '';

      colors.forEach(function (color) {
        sizes.forEach(function (size) {
          var key = sanitizeKey(color + '-' + size);
          var row = document.createElement('tr');
          row.innerHTML =
            '<td style="padding: 6px; border: 1px solid var(--border);">' + escapeHtml(color) + '</td>' +
            '<td style="padding: 6px; border: 1px solid var(--border);">' + escapeHtml(size) + '</td>' +
            '<td style="padding: 6px; border: 1px solid var(--border);"><input type="number" name="variants[' + key + '].price" min="0" step="1000" placeholder="Gia" style="width: 100px; padding: 4px; border: 1px solid var(--border);"></td>' +
            '<td style="padding: 6px; border: 1px solid var(--border);"><input type="number" name="variants[' + key + '].stock" min="0" value="0" placeholder="SL" style="width: 80px; padding: 4px; border: 1px solid var(--border);"></td>';
          tableBody.appendChild(row);
        });
      });
    }
  }

  function sanitizeKey(text) {
    return text
      .toLowerCase()
      .replace(/\s+/g, '-')
      .replace(/[^a-z0-9\-]/g, '');
  }

  function escapeHtml(text) {
    return text
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#039;');
  }

  function notify(message, type) {
    if (window.AdminUI && typeof window.AdminUI.showToast === 'function') {
      window.AdminUI.showToast(message, type || 'info');
    }
  }

  function getCsrfData() {
    var token = document.querySelector('meta[name="_csrf"]')?.content || '';
    var header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    return { token: token, header: header };
  }
})();
