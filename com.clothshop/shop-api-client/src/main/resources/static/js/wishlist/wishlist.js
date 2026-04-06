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
    input.value = Math.max(1, parseInt(input.value) - 1);
}

function increaseModalQty() {
    const input = document.getElementById('modal-qty');
    input.value = Math.min(10, parseInt(input.value) + 1);
}

function addModalToCart() {
    const productId = document.getElementById('modal-image-section').dataset.productId;
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
            quantity: parseInt(document.getElementById('modal-qty').value)
        })
    })
    .then(r => r.json())
    .then(data => {
        if (data.success) {
            showToast(data.message, 'success');
            closeQuickViewModal();
        }
    })
    .catch(e => showToast('Có lỗi xảy ra', 'error'));
}

function closeQuickViewModal() {
    document.getElementById('quick-view-modal').style.display = 'none';
}

function showToast(msg, type) {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = msg;
    toast.style.cssText = 'position:fixed;bottom:20px;right:20px;padding:12px 16px;background:#1C1C1A;color:white;border-radius:4px;z-index:9999;animation:slideIn 0.3s ease';
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}
