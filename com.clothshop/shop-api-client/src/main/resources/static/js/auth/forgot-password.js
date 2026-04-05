/* =============================================================
   FORGOT PASSWORD PAGE SCRIPTS
   Template: templates/client/auth/forgot-password.html
   ============================================================= */

'use strict';

/**
 * Optional: disable submit button sau khi gửi để tránh double-submit
 */
document.addEventListener('DOMContentLoaded', function () {
  const form = document.getElementById('forgot-password-form');
  if (!form) return;

  form.addEventListener('submit', function () {
    const btn = form.querySelector('button[type="submit"]');
    if (btn) {
      btn.disabled = true;
      btn.textContent = 'Đang gửi...';
    }
  });
});
