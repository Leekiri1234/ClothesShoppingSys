/* =============================================================
   PROFILE EDIT PAGE SCRIPTS
   Template: templates/client/profile/edit.html
   Chức năng: Chỉnh sửa thông tin hồ sơ cá nhân
   ============================================================= */

'use strict';

/**
 * Chuyển đổi tab trong sidebar profile (active state)
 * @param {HTMLElement} el - link được click
 */
function switchProfileTab(el) {
  document.querySelectorAll('.profile-sidebar-link').forEach(l => l.classList.remove('active'));
  el.classList.add('active');
}

/**
 * Xác nhận trước khi hủy bỏ thay đổi và quay lại profile
 * @param {string} profileUrl - URL trang profile
 */
function confirmCancel(profileUrl) {
  if (confirm('Bạn có chắc muốn hủy? Các thay đổi chưa lưu sẽ bị mất.')) {
    window.location.href = profileUrl;
  }
}

/**
 * Disable nút submit sau khi form được submit để tránh double-submit
 */
document.addEventListener('DOMContentLoaded', function () {
  const form = document.querySelector('form[action*="profile/update"]');
  if (!form) return;

  form.addEventListener('submit', function () {
    const btn = form.querySelector('button[type="submit"]');
    if (btn) {
      btn.disabled = true;
      btn.textContent = 'Đang lưu...';
    }
  });
});
