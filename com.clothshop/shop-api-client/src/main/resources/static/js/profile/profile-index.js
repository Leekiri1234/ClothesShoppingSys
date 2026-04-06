/* =============================================================
   PROFILE INDEX PAGE SCRIPTS
   Template: templates/client/profile/index.html
   Chức năng: Xem thông tin hồ sơ cá nhân
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
