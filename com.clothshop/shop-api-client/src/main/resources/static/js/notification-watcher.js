// 1. Dùng localStorage để nhớ ID, tránh bị spam thông báo cũ khi F5 (Refresh) trang
let lastNotificationId = parseInt(localStorage.getItem('minimal_last_noti_id'));
let isFirstTime = false;

// Nếu chưa từng vào web (localStorage trống)
if (isNaN(lastNotificationId)) {
    lastNotificationId = 0;
    isFirstTime = true; // Đánh dấu lần đầu để không hiện Toast tin cũ
}

let unreadCount = 0;

function startMemoryWatcher() {
    const bellIcon = document.getElementById('notification-bell');
    const dropdown = document.getElementById('noti-dropdown');

    // TÁCH BIỆT LOGIC DROPDOWN ĐỂ NÓ LUÔN CHẠY
    if (bellIcon && dropdown) {
        bellIcon.addEventListener('click', (e) => {
            e.stopPropagation();
            const isDisplaying = dropdown.style.display === 'block';
            dropdown.style.display = isDisplaying ? 'none' : 'block';
            if (!isDisplaying) {
                resetBell();
            }
        });
        document.addEventListener('click', () => dropdown.style.display = 'none');
    }

    // VÒNG LẶP POLLING GỌI SANG ADMIN (PORT 8081)
    setInterval(async () => {
        try {
            const response = await fetch(`http://localhost:8081/api/sync/check?lastId=${lastNotificationId}`);

            if (!response.ok) return;

            const data = await response.json();
            // console.log("Check RAM Admin:", data); // Có thể ẩn dòng này đi cho sạch Console

            if (data.status === "UPDATE_FOUND") {
                if (isFirstTime) {
                    // Lần đầu vào web: Chỉ lưu ID để đồng bộ RAM, KHÔNG hiện Toast tránh spam
                    lastNotificationId = data.newId;
                    localStorage.setItem('minimal_last_noti_id', lastNotificationId);
                    isFirstTime = false;
                } else {
                    // CÓ THÔNG BÁO MỚI THẬT SỰ
                    lastNotificationId = data.newId;
                    localStorage.setItem('minimal_last_noti_id', lastNotificationId); // Lưu lại ngay

                    unreadCount++;
                    updateBellUI(unreadCount);
                    addNotiToDropdown(data.title, data.newId);
                    showMinimalToast(data.title, data.newId); // Truyền thêm ID vào đây
                }
            }
        } catch (e) {
            // Không log lỗi ra console liên tục để tránh làm treo trình duyệt
        }
    }, 2000);
}

function updateBellUI(count) {
    const badge = document.getElementById('noti-count');
    const bell = document.querySelector('#notification-bell svg');
    if (badge) {
        badge.textContent = count > 9 ? "9+" : count;
        badge.style.display = 'flex';
    }
    if (bell) bell.parentElement.classList.add('bell-anim');
}

function resetBell() {
    const badge = document.getElementById('noti-count');
    const bell = document.querySelector('#notification-bell svg');
    if (badge) badge.style.display = 'none';
    unreadCount = 0;
    if (bell) bell.parentElement.classList.remove('bell-anim');
}

// 2. Sửa Dropdown: Thay thẻ <div> thành thẻ <a> để click chuyển trang được
function addNotiToDropdown(title, id) {
    const container = document.getElementById('noti-items-container');
    if (!container) return;

    const noNoti = container.querySelector('div[style*="text-align: center"]');
    if (noNoti) noNoti.remove();

    const html = `
        <a href="/profile/notifications/${id}" class="noti-item unread" style="display:block; text-decoration:none;">
            <div class="noti-item-title">${title}</div>
            <div class="noti-item-time">Vừa xong • ID: ${id}</div>
        </a>
    `;
    container.insertAdjacentHTML('afterbegin', html);
}

// 3. Cập nhật hàm showMinimalToast: Thêm sự kiện onClick
function showMinimalToast(message, id) {
    if (typeof Toastify === 'undefined') {
        console.error("Thư viện Toastify chưa được tải!");
        return;
    }

    Toastify({
        text: message,
        duration: 8000,
        gravity: "top",
        position: "right",
        style: {
            background: "#1c1c1a",
            color: "#ffffff",
            fontSize: "14px",
            fontWeight: "300",
            borderRadius: "0px",
            padding: "15px 25px",
            cursor: "pointer" // Thêm icon bàn tay khi di chuột vào Toast
        },
        onClick: function() {
            // Click vào Toast bay thẳng vào trang chi tiết
            if (id) {
                window.location.href = `/profile/notifications/${id}`;
            }
        }
    }).showToast();
}

// KHỞI CHẠY
document.addEventListener('DOMContentLoaded', startMemoryWatcher);