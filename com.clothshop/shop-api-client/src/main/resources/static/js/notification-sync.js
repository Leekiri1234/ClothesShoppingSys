async function startNotificationWatcher() {
    // 1. Lấy địa chỉ RAM từ Server
    const response = await fetch('/api/sync/pointer');
    const data = await response.json();
    const memAddress = data.address;

    let lastId = 0;

    // 2. Dùng kỹ thuật Polling siêu nhẹ (Lightweight Polling)
    setInterval(async () => {
        // Trong thực tế, trình duyệt không thể đọc RAM trực tiếp vì bảo mật
        // Nhưng vì ta dùng JDBC Loopback, ta sẽ gọi một endpoint "Check-Only" cực nhanh
        const check = await fetch(`/api/sync/check?last=${lastId}`);
        const result = await check.json();

        if (result.newId > lastId) {
            lastId = result.newId;
            showNotificationPopup(result.title); // Hàm hiển thị UI của bạn
        }
    }, 2000); // 2 giây kiểm tra một lần
}

function showNotificationPopup(title) {
    alert("Thông báo mới từ cửa hàng: " + title);
}

startNotificationWatcher();