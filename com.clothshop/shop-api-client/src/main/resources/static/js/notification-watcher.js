let lastNotificationId = parseInt(localStorage.getItem('minimal_last_noti_id')) || 0;
let isFirstTime = (localStorage.getItem('minimal_last_noti_id') === null);
let unreadCount = 0;

function startMemoryWatcher() {
    const bellIcon = document.getElementById('notification-bell');
    const dropdown = document.getElementById('noti-dropdown');

    if (bellIcon && dropdown) {
        bellIcon.addEventListener('click', async (e) => {
            e.stopPropagation();
            const isOpening = dropdown.style.display !== 'block';

            if (isOpening) {
                dropdown.style.display = 'block';
                resetBell();
                // TÍCH HỢP: Load dữ liệu cũ từ Database Client (8080)
                await loadNotificationsFromServer();
            } else {
                dropdown.style.display = 'none';
            }
        });
        document.addEventListener('click', () => dropdown.style.display = 'none');
    }

    // POLLING: Kiểm tra tin mới từ RAM Admin (8081)
    setInterval(async () => {
        try {
            const response = await fetch(`http://localhost:8081/api/sync/check?lastId=${lastNotificationId}`);
            if (!response.ok) return;
            const data = await response.json();

            if (data.status === "UPDATE_FOUND") {
                lastNotificationId = data.newId;
                localStorage.setItem('minimal_last_noti_id', lastNotificationId);

                if (!isFirstTime) {
                    unreadCount++;
                    updateBellUI(unreadCount);
                    showMinimalToast(data.title, data.newId);
                }
                isFirstTime = false;
            }
        } catch (e) {}
    }, 2000);
}

// Hàm gọi Fragment từ Server 8080
async function loadNotificationsFromServer() {
    const container = document.getElementById('noti-items-container');
    try {
        const response = await fetch('/profile/notifications/fragment');
        if (response.ok) {
            const html = await response.text();
            // Ghi đè vào vùng chứa trong dropdown
            container.outerHTML = html;
        }
    } catch (e) {
        console.error("Lỗi đồng bộ thông báo:", e);
    }
}

function updateBellUI(count) {
    const badge = document.getElementById('noti-count');
    const bellWrapper = document.getElementById('notification-bell');
    if (badge) {
        badge.textContent = count > 9 ? "9+" : count;
        badge.style.display = 'flex';
    }
    if (bellWrapper) bellWrapper.classList.add('bell-anim');
}

function resetBell() {
    const badge = document.getElementById('noti-count');
    const bellWrapper = document.getElementById('notification-bell');
    if (badge) badge.style.display = 'none';
    if (bellWrapper) bellWrapper.classList.remove('bell-anim');
    unreadCount = 0;
}

function showMinimalToast(message, id) {
    if (typeof Toastify !== 'undefined') {
        Toastify({
            text: message,
            duration: 5000,
            gravity: "top",
            position: "right",
            style: { background: "#1c1c1a", color: "#fff", borderRadius: "0px" },
            onClick: () => { if(id) window.location.href = `/profile/notifications/${id}`; }
        }).showToast();
    }
}

document.addEventListener('DOMContentLoaded', startMemoryWatcher);