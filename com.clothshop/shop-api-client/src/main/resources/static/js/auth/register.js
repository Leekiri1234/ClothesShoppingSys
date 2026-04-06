/* =============================================================
   REGISTER PAGE SCRIPTS
   Template: templates/client/auth/register.html
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

/**
 * Kiểm tra mật khẩu xác nhận khớp không (client-side feedback)
 * Gắn vào input confirm password: oninput="checkPasswordMatch(this)"
 */
function checkPasswordMatch(el) {
  const password = document.getElementById('reg-pass');
  if (!password) return;

  if (el.value && el.value !== password.value) {
    el.style.borderColor = '#c0392b';
  } else {
    el.style.borderColor = '';
  }
}
