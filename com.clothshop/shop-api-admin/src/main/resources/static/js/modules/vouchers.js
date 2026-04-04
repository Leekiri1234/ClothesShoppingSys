(function () {
  'use strict';

  document.addEventListener('DOMContentLoaded', function () {
    initVoucherListPage();
    initVoucherFormPage();
  });

  function initVoucherListPage() {
    var page = document.querySelector('.vouchers-list-page');
    if (!page) return;

    page.querySelectorAll('.voucher-status-form').forEach(function (form) {
      form.addEventListener('submit', function (event) {
        var message = form.dataset.confirmMessage || 'Ban co chac chan cap nhat trang thai voucher?';
        if (!window.confirm(message)) {
          event.preventDefault();
        }
      });
    });
  }

  function initVoucherFormPage() {
    var page = document.querySelector('.vouchers-form-page');
    if (!page) return;

    var form = document.getElementById('voucherForm');
    var discountType = document.getElementById('discountType');
    var discountDisplay = document.getElementById('discountValueDisplay');
    var minOrderDisplay = document.getElementById('minOrderValueDisplay');
    var maxDiscountDisplay = document.getElementById('maxDiscountDisplay');
    var validFromDisplay = document.getElementById('validFromDisplay');
    var validToDisplay = document.getElementById('validToDisplay');

    hydrateInitialDisplays();
    hydrateDateDisplays();
    updateDiscountTypeUI();

    if (discountType) {
      discountType.addEventListener('change', function () {
        updateDiscountTypeUI();
        formatDiscountValue(true);
      });
    }

    if (discountDisplay) {
      discountDisplay.addEventListener('input', function () {
        formatDiscountValue(false);
      });
      discountDisplay.addEventListener('blur', function () {
        formatDiscountValue(true);
      });
    }

    if (minOrderDisplay) {
      minOrderDisplay.addEventListener('input', function () {
        formatCurrencyField('minOrderValueDisplay', 'minOrderValueRaw', false);
      });
      minOrderDisplay.addEventListener('blur', function () {
        formatCurrencyField('minOrderValueDisplay', 'minOrderValueRaw', true);
      });
    }

    if (maxDiscountDisplay) {
      maxDiscountDisplay.addEventListener('input', function () {
        formatCurrencyField('maxDiscountDisplay', 'maxDiscountRaw', false);
      });
      maxDiscountDisplay.addEventListener('blur', function () {
        formatCurrencyField('maxDiscountDisplay', 'maxDiscountRaw', true);
      });
    }

    if (validFromDisplay) {
      validFromDisplay.addEventListener('input', function () {
        syncDateField('validFromDisplay', 'validFromRaw');
      });
    }

    if (validToDisplay) {
      validToDisplay.addEventListener('input', function () {
        syncDateField('validToDisplay', 'validToRaw');
      });
    }

    if (form) {
      form.addEventListener('submit', function () {
        syncAllNumericFields();
        syncDateField('validFromDisplay', 'validFromRaw');
        syncDateField('validToDisplay', 'validToRaw');
      });
    }
  }

  function updateDiscountTypeUI() {
    var type = document.getElementById('discountType')?.value;
    var suffix = document.getElementById('discountSuffix');
    var maxDiscountWrapper = document.getElementById('maxDiscountWrapper');
    var maxDiscountDisplay = document.getElementById('maxDiscountDisplay');
    var maxDiscountRaw = document.getElementById('maxDiscountRaw');

    if (suffix) {
      suffix.textContent = type === 'PERCENTAGE' ? '%' : '₫';
    }

    if (!maxDiscountWrapper) return;

    if (type === 'PERCENTAGE') {
      maxDiscountWrapper.style.display = 'block';
    } else {
      maxDiscountWrapper.style.display = 'none';
      if (maxDiscountDisplay) maxDiscountDisplay.value = '';
      if (maxDiscountRaw) maxDiscountRaw.value = '';
    }
  }

  function syncAllNumericFields() {
    formatDiscountValue(true);
    formatCurrencyField('minOrderValueDisplay', 'minOrderValueRaw', true);
    formatCurrencyField('maxDiscountDisplay', 'maxDiscountRaw', true);
  }

  function formatDiscountValue(finalize) {
    var discountType = document.getElementById('discountType')?.value;
    var display = document.getElementById('discountValueDisplay');
    var hidden = document.getElementById('discountValueRaw');
    if (!display || !hidden) return;

    var raw = discountType === 'PERCENTAGE' ? parseDecimal(display.value) : parseCurrency(display.value);
    hidden.value = raw == null ? '' : String(raw);

    if (finalize) {
      if (raw == null) {
        display.value = '';
      } else if (discountType === 'PERCENTAGE') {
        display.value = formatPercent(raw);
      } else {
        display.value = formatVnd(raw);
      }
    }
  }

  function formatCurrencyField(displayId, hiddenId, finalize) {
    var display = document.getElementById(displayId);
    var hidden = document.getElementById(hiddenId);
    if (!display || !hidden) return;

    var raw = parseCurrency(display.value);
    hidden.value = raw == null ? '' : String(raw);

    if (finalize) {
      display.value = raw == null ? '' : formatVnd(raw);
    }
  }

  function hydrateInitialDisplays() {
    var discountType = document.getElementById('discountType')?.value;

    hydrateNumericDisplay('discountValueRaw', 'discountValueDisplay', discountType === 'PERCENTAGE');
    hydrateNumericDisplay('minOrderValueRaw', 'minOrderValueDisplay', false);
    hydrateNumericDisplay('maxDiscountRaw', 'maxDiscountDisplay', false);
  }

  function hydrateNumericDisplay(hiddenId, displayId, isPercent) {
    var hidden = document.getElementById(hiddenId);
    var display = document.getElementById(displayId);
    if (!hidden || !display || !hidden.value) return;

    var value = Number(hidden.value);
    if (Number.isNaN(value)) return;

    display.value = isPercent ? formatPercent(value) : formatVnd(value);
  }

  function hydrateDateDisplays() {
    hydrateDateField('validFromRaw', 'validFromDisplay');
    hydrateDateField('validToRaw', 'validToDisplay');

    syncDateField('validFromDisplay', 'validFromRaw');
    syncDateField('validToDisplay', 'validToRaw');
  }

  function hydrateDateField(hiddenId, displayId) {
    var hidden = document.getElementById(hiddenId);
    var display = document.getElementById(displayId);
    if (!hidden || !display || display.value || !hidden.value) return;

    var date = new Date(hidden.value);
    if (Number.isNaN(date.getTime())) return;

    var pad = function (number) {
      return String(number).padStart(2, '0');
    };

    display.value =
      date.getFullYear() + '-' +
      pad(date.getMonth() + 1) + '-' +
      pad(date.getDate()) + 'T' +
      pad(date.getHours()) + ':' +
      pad(date.getMinutes());
  }

  function syncDateField(displayId, hiddenId) {
    var display = document.getElementById(displayId);
    var hidden = document.getElementById(hiddenId);
    if (!display || !hidden) return;

    hidden.value = display.value || '';
  }

  function parseCurrency(value) {
    if (!value) return null;
    var digitsOnly = value.replace(/[^0-9]/g, '');
    if (!digitsOnly) return null;
    return Number(digitsOnly);
  }

  function parseDecimal(value) {
    if (!value) return null;
    var normalized = value.replace(/,/g, '.');
    var parsed = parseFloat(normalized);
    return Number.isNaN(parsed) ? null : parsed;
  }

  function formatVnd(value) {
    if (value == null || value === '') return '';
    var numberValue = Number(value);
    if (Number.isNaN(numberValue)) return '';
    return new Intl.NumberFormat('vi-VN').format(numberValue);
  }

  function formatPercent(value) {
    if (value == null || value === '') return '';
    var numberValue = Number(value);
    if (Number.isNaN(numberValue)) return '';
    return new Intl.NumberFormat('vi-VN', {
      maximumFractionDigits: 2,
      minimumFractionDigits: 0
    }).format(numberValue);
  }
})();
