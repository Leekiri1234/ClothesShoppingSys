// notification-watcher.js
let lastUnreadCount = -1;
let lastNotiId = -1;

async function checkNotifications() {
    try {
        const [latestRes, countRes] = await Promise.all([
            fetch('/profile/notifications/latest'),
            fetch('/profile/notifications/unread-count')
        ]);

        if (countRes.ok) {
            const count = await countRes.json();
            if (count !== lastUnreadCount) {
                lastUnreadCount = count;
                updateBellUI(count);

                const dropdown = document.getElementById('noti-dropdown');
                if (dropdown && dropdown.style.display === 'block') {
                    await loadNotificationsFromServer();
                }
            }
        }

        if (latestRes.ok) {
            // endpoint trả về chuỗi rỗng nếu không có tin nào thay vì lỗi
            const text = await latestRes.text();
            if (text) {
                const data = JSON.parse(text);
                if (data && data.newId) {
                    if (lastNotiId === -1) {
                        lastNotiId = data.newId;
                    } else if (data.newId > lastNotiId) {
                        lastNotiId = data.newId;
                        showMinimalToast(data.title, data.newId);
                    }
                }
            }
        }
    } catch (e) {
        // silent fail
    }
}

function startMemoryWatcher() {
    checkNotifications();

    const bellIcon = document.getElementById('notification-bell');
    const dropdown = document.getElementById('noti-dropdown');

    if (bellIcon && dropdown) {
        bellIcon.addEventListener('click', async (e) => {
            e.stopPropagation();
            const isOpening = dropdown.style.display !== 'block';

            if (isOpening) {
                dropdown.style.display = 'block';
                bellIcon.classList.remove('bell-anim');
                await loadNotificationsFromServer();
            } else {
                dropdown.style.display = 'none';
            }
        });
        document.addEventListener('click', () => dropdown.style.display = 'none');
    }

    setInterval(checkNotifications, 3000);
}

async function loadNotificationsFromServer() {
    const container = document.getElementById('noti-items-container');
    if (!container) return;
    try {
        const response = await fetch('/profile/notifications/fragment');
        if (response.ok) {
            const html = await response.text();
            // Thay thế nội dung cũ bằng fragment mới từ server
            container.outerHTML = html;
        }
    } catch (e) {
        console.error("Lỗi đồng bộ fragment:", e);
    }
}

// Các hàm bổ trợ UI giữ nguyên
function updateBellUI(count) {
    const badge = document.getElementById('noti-count');
    const bellWrapper = document.getElementById('notification-bell');
    if (badge) {
        if (count > 0) {
            badge.textContent = count > 9 ? "9+" : count;
            badge.style.display = 'flex';
        } else {
            badge.style.display = 'none';
        }
    }
    if (bellWrapper && count > 0) bellWrapper.classList.add('bell-anim');
}

function resetBell() {
    unreadCount = 0;
    updateBellUI(0);
}

function showMinimalToast(message, id) {
    if (typeof Toastify !== 'undefined') {
        Toastify({
            text: message,
            duration: 8000,
            gravity: "top",
            position: "right",
            destination: id ? `/profile/notifications/${id}` : undefined,
            style: { background: "#1c1c1a", color: "#fff", borderRadius: "0px" }
        }).showToast();
    }
}

window.markAllNotificationsAsRead = function() {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

    fetch('/profile/notifications/mark-all-read', {
        method: 'POST',
        headers: {
            [csrfHeader]: csrfToken
        }
    })
    .then(res => {
        if (res.ok) {
            checkNotifications(); // Refresh count right away
            if (window.location.pathname.includes('/profile/notifications')) {
                window.location.reload();
            } else {
                loadNotificationsFromServer();
            }
        }
    });
};

document.addEventListener('DOMContentLoaded', startMemoryWatcher);