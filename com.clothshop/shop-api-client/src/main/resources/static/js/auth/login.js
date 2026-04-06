/* =============================================================
   LOGIN PAGE SCRIPTS
   Template: templates/client/auth/login.html
   ============================================================= */

'use strict';

/**
 * Hiện / Ẩn mật khẩu
 * @param {string} id  - ID của input password
 * @param {HTMLElement} el - phần tử span trigger (hiển thị "hiện"/"ẩn")
 */
function togglePasswordVisibility(id, el) {
  const input = document.getElementById(id);
  if (!input) return;

  if (input.type === 'password') {
    input.type = 'text';
    if (el) el.innerText = 'ẩn';
  } else {
    input.type = 'password';
    if (el) el.innerText = 'hiện';
  }
}
