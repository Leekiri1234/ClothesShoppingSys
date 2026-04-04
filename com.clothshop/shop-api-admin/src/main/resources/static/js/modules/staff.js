(function () {
  'use strict';

  document.addEventListener('DOMContentLoaded', function () {
    initStaffListPage();
    initStaffCreatePage();
    initStaffEditPage();
  });

  function initStaffListPage() {
    var page = document.querySelector('.staff-list-page');
    if (!page) return;

    var searchInput = document.getElementById('searchInput');
    var roleFilter = document.getElementById('roleFilter');
    var statusFilter = document.getElementById('statusFilter');
    var resetButton = document.getElementById('staffResetFiltersBtn');
    var selectAll = document.getElementById('selectAll');
    var statusToggles = document.querySelectorAll('.staff-status-toggle');
    var debounceTimer;

    function applyFilters() {
      var url = new URL(window.location.href);
      url.searchParams.set('page', '0');

      var keyword = searchInput ? searchInput.value.trim() : '';
      var role = roleFilter ? roleFilter.value : '';
      var status = statusFilter ? statusFilter.value : '';

      if (keyword) {
        url.searchParams.set('keyword', keyword);
      } else {
        url.searchParams.delete('keyword');
      }

      if (role) {
        url.searchParams.set('roleId', role);
      } else {
        url.searchParams.delete('roleId');
      }

      if (status) {
        url.searchParams.set('status', status);
      } else {
        url.searchParams.delete('status');
      }

      window.location.href = url.toString();
    }

    if (searchInput) {
      searchInput.addEventListener('input', function () {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(applyFilters, 500);
      });
    }

    if (roleFilter) {
      roleFilter.addEventListener('change', applyFilters);
    }

    if (statusFilter) {
      statusFilter.addEventListener('change', applyFilters);
    }

    if (resetButton) {
      resetButton.addEventListener('click', function () {
        window.location.href = '/admin/staff';
      });
    }

    if (selectAll) {
      selectAll.addEventListener('change', function () {
        var checkboxes = document.querySelectorAll('.row-checkbox');
        checkboxes.forEach(function (checkbox) {
          checkbox.checked = selectAll.checked;
        });
      });
    }

    statusToggles.forEach(function (toggle) {
      toggle.addEventListener('change', function () {
        var staffId = toggle.dataset.staffId;
        var isSuperAdmin = toggle.dataset.superAdmin === 'true';

        if (isSuperAdmin) {
          toggle.checked = true;
          if (window.AdminUI && typeof window.AdminUI.showToast === 'function') {
            window.AdminUI.showToast('SUPER_ADMIN la tai khoan co dinh, khong the vo hieu hoa.', 'warning');
          }
          return;
        }

        submitStaffToggle(staffId, function () {
          var message = toggle.checked
            ? 'Da kich hoat tai khoan nhan vien.'
            : 'Da vo hieu hoa tai khoan nhan vien.';
          if (window.AdminUI && typeof window.AdminUI.showToast === 'function') {
            window.AdminUI.showToast(message, 'success');
          }
          setTimeout(function () {
            window.location.reload();
          }, 700);
        }, function () {
          toggle.checked = !toggle.checked;
          if (window.AdminUI && typeof window.AdminUI.showToast === 'function') {
            window.AdminUI.showToast('Khong the thay doi trang thai tai khoan.', 'danger');
          }
        });
      });
    });
  }

  function initStaffCreatePage() {
    var page = document.querySelector('.staff-create-page');
    if (!page) return;

    var form = page.querySelector('form');
    var passwordInput = page.querySelector('input[name="password"]');
    var confirmPasswordInput = document.getElementById('confirmPassword');
    var passwordMatchMessage = document.getElementById('passwordMatch');

    function validateCreatePassword() {
      if (!confirmPasswordInput || !passwordInput || !passwordMatchMessage) return;

      if (!confirmPasswordInput.value) {
        passwordMatchMessage.style.display = 'none';
        return;
      }

      passwordMatchMessage.style.display = 'block';
      if (passwordInput.value === confirmPasswordInput.value) {
        passwordMatchMessage.style.color = 'var(--success)';
        passwordMatchMessage.textContent = 'Mat khau khop';
      } else {
        passwordMatchMessage.style.color = 'var(--danger)';
        passwordMatchMessage.textContent = 'Mat khau khong khop';
      }
    }

    if (passwordInput) passwordInput.addEventListener('input', validateCreatePassword);
    if (confirmPasswordInput) confirmPasswordInput.addEventListener('input', validateCreatePassword);

    if (form) {
      form.addEventListener('submit', function (event) {
        if (passwordInput && confirmPasswordInput && passwordInput.value !== confirmPasswordInput.value) {
          event.preventDefault();
          if (window.AdminUI && typeof window.AdminUI.showToast === 'function') {
            window.AdminUI.showToast('Mat khau xac nhan khong khop.', 'danger');
          }
          confirmPasswordInput.focus();
        }
      });
    }
  }

  function initStaffEditPage() {
    var page = document.querySelector('.staff-edit-page');
    if (!page) return;

    var form = page.querySelector('form');
    var passwordSection = document.getElementById('passwordSection');
    var togglePasswordButton = document.getElementById('togglePasswordBtn');
    var togglePasswordText = document.getElementById('togglePasswordText');
    var newPasswordInput = document.getElementById('newPassword');
    var confirmPasswordInput = document.getElementById('confirmNewPassword');
    var passwordMatchMessage = document.getElementById('passwordMatchMsg');
    var deactivateButton = document.getElementById('deactivateStaffBtn');
    var activateButton = document.getElementById('activateStaffBtn');

    if (togglePasswordButton && passwordSection && togglePasswordText) {
      togglePasswordButton.addEventListener('click', function () {
        var hidden = passwordSection.style.display === 'none' || passwordSection.style.display === '';
        passwordSection.style.display = hidden ? 'block' : 'none';
        togglePasswordText.textContent = hidden ? 'An' : 'Hien thi';
      });
    }

    function validateEditPassword() {
      if (!newPasswordInput || !confirmPasswordInput || !passwordMatchMessage) return;

      if (!newPasswordInput.value || !confirmPasswordInput.value) {
        passwordMatchMessage.style.display = 'none';
        return;
      }

      passwordMatchMessage.style.display = 'block';
      if (newPasswordInput.value === confirmPasswordInput.value) {
        passwordMatchMessage.style.color = 'var(--success)';
        passwordMatchMessage.textContent = 'Mat khau khop';
      } else {
        passwordMatchMessage.style.color = 'var(--danger)';
        passwordMatchMessage.textContent = 'Mat khau khong khop';
      }
    }

    if (newPasswordInput) newPasswordInput.addEventListener('input', validateEditPassword);
    if (confirmPasswordInput) confirmPasswordInput.addEventListener('input', validateEditPassword);

    if (form) {
      form.addEventListener('submit', function (event) {
        var newPassword = newPasswordInput ? newPasswordInput.value : '';
        var confirmPassword = confirmPasswordInput ? confirmPasswordInput.value : '';

        if ((newPassword || confirmPassword) && newPassword !== confirmPassword) {
          event.preventDefault();
          if (window.AdminUI && typeof window.AdminUI.showToast === 'function') {
            window.AdminUI.showToast('Mat khau xac nhan khong khop.', 'danger');
          }
          if (confirmPasswordInput) confirmPasswordInput.focus();
        }
      });
    }

    if (deactivateButton) {
      deactivateButton.addEventListener('click', function () {
        var staffId = deactivateButton.dataset.staffId;
        if (!window.confirm('Ban co chac chan muon VO HIEU HOA tai khoan nay?')) return;
        submitStaffToggle(staffId, function () {
          if (window.AdminUI && typeof window.AdminUI.showToast === 'function') {
            window.AdminUI.showToast('Da vo hieu hoa tai khoan.', 'success');
          }
          setTimeout(function () {
            window.location.href = '/admin/staff';
          }, 700);
        }, function () {
          if (window.AdminUI && typeof window.AdminUI.showToast === 'function') {
            window.AdminUI.showToast('Khong the vo hieu hoa tai khoan.', 'danger');
          }
        });
      });
    }

    if (activateButton) {
      activateButton.addEventListener('click', function () {
        var staffId = activateButton.dataset.staffId;
        if (!window.confirm('Ban co chac chan muon KICH HOAT lai tai khoan nay?')) return;
        submitStaffToggle(staffId, function () {
          if (window.AdminUI && typeof window.AdminUI.showToast === 'function') {
            window.AdminUI.showToast('Da kich hoat tai khoan.', 'success');
          }
          setTimeout(function () {
            window.location.href = '/admin/staff';
          }, 700);
        }, function () {
          if (window.AdminUI && typeof window.AdminUI.showToast === 'function') {
            window.AdminUI.showToast('Khong the kich hoat tai khoan.', 'danger');
          }
        });
      });
    }
  }

  function submitStaffToggle(staffId, onSuccess, onError) {
    var csrf = getCsrfData();
    if (!csrf.token) {
      if (onError) onError();
      return;
    }

    fetch('/admin/staff/toggle/' + staffId, {
      method: 'POST',
      headers: Object.assign(
        { 'Content-Type': 'application/x-www-form-urlencoded' },
        csrf.header ? { [csrf.header]: csrf.token } : { 'X-XSRF-TOKEN': csrf.token }
      )
    })
      .then(function (response) {
        if (!response.ok) throw new Error('Toggle failed');
        if (onSuccess) onSuccess();
      })
      .catch(function () {
        if (onError) onError();
      });
  }

  function getCsrfData() {
    var tokenFromCookie = getCookie('XSRF-TOKEN');
    var tokenFromMeta = document.querySelector('meta[name="_csrf"]')?.content;
    var headerFromMeta = document.querySelector('meta[name="_csrf_header"]')?.content;

    return {
      token: tokenFromCookie || tokenFromMeta || '',
      header: headerFromMeta || 'X-XSRF-TOKEN'
    };
  }

  function getCookie(name) {
    var value = '; ' + document.cookie;
    var parts = value.split('; ' + name + '=');
    if (parts.length === 2) return parts.pop().split(';').shift();
    return '';
  }
})();
