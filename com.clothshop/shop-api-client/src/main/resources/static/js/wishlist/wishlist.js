/* Wishlist page JavaScript functions */

function removeWishlistCard(button) {
    const card = button.closest('.wishlist-card');
    if (card) {
        setTimeout(() => {
            card.style.opacity = '0';
            card.style.transform = 'scale(0.95)';
            setTimeout(() => card.remove(), 300);
        }, 200);
    }
}

function decreaseModalQty() {
    const input = document.getElementById('modal-qty');
    if (!input) return;
    input.value = Math.max(1, parseInt(input.value || 1) - 1);
}

function increaseModalQty() {
    const input = document.getElementById('modal-qty');
    if (!input) return;
    input.value = Math.min(10, parseInt(input.value || 1) + 1);
}

function addModalToCart() {
    const productSection = document.getElementById('modal-image-section');
    const productId = productSection?.dataset.productId;

    if (!productId) {
        showToast('Vui lòng chọn sản phẩm', 'error');
        return;
    }

    fetch('/cart/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]')?.content || ''
        },
        body: JSON.stringify({
            variantId: 1,
            quantity: parseInt(document.getElementById('modal-qty')?.value || 1)
        })
    })
    .then(r => r.json())
    .then(data => {
        if (data.success) {
            showToast(data.message || 'Đã thêm vào giỏ hàng', 'success');
            closeQuickViewModal();
        } else {
            showToast(data.message || 'Có lỗi xảy ra', 'error');
        }
    })
    .catch(() => showToast('Có lỗi xảy ra', 'error'));
}

function closeQuickViewModal() {
    const modal = document.getElementById('quick-view-modal');
    if (modal) {
        modal.style.display = 'none';
    }
}

function showToast(msg, type) {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = msg;
    toast.style.cssText = 'position:fixed;bottom:20px;right:20px;padding:12px 16px;background:#1C1C1A;color:white;border-radius:4px;z-index:9999;animation:slideIn 0.3s ease';
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

async function wishlistToggle(button, event) {
    event.preventDefault();
    event.stopPropagation();

    const productId = button.getAttribute("data-product-id");
    if (!productId) return;

    try {
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

        const response = await fetch(`/wishlist/toggle/${productId}`, {
            method: "POST",
            headers: { [csrfHeader]: csrfToken }
        });

        const result = await response.json();

        if (response.ok && result.success) {
            if (result.isAdded) {
                button.classList.add("active");
                showToast("Đã thêm vào wishlist!", "success");
                // ← thêm: đánh dấu product này đang IN wishlist
                sessionStorage.setItem(`wishlist_${productId}`, 'true');
            } else {
                button.classList.remove("active");
                showToast("Đã xóa khỏi wishlist.", "info");
                // ← thêm: đánh dấu product này đã bị REMOVE
                sessionStorage.setItem(`wishlist_${productId}`, 'false');
            }

            updateWishlistCount();
        } else {
            showToast(result.message || "Có lỗi xảy ra.", "error");
        }
    } catch (err) {
        console.error("Wishlist toggle error:", err);
        showToast("Không thể cập nhật wishlist.", "error");
    }
}
function updateWishlistCount() {
    fetch("/wishlist/count")
        .then(res => res.json())
        .then(data => {
            const badge = document.getElementById("wishlist-count");
            if (!badge) return;

            const count = data.count ?? 0;
            badge.textContent = count;
            badge.style.display = count > 0 ? "flex" : "none";
        })
        .catch(err => console.error("Wishlist count error:", err));
}