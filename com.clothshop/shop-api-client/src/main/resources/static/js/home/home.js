// =============================================
// PRODUCT ACTIONS - quickAddToCart, quickView, wishlist
// =============================================

// ── Add to Cart ──────────────────────────────
async function quickAddToCart(button, event) {
    event.preventDefault();
    event.stopPropagation();

    const variantId = button.getAttribute("data-variant-id");

    if (!variantId || variantId === "null") {
        showToast("Sản phẩm này chưa có biến thể khả dụng.", "error");
        return;
    }

    // Visual feedback
    button.disabled = true;
    const originalHTML = button.innerHTML;
    button.innerHTML = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 6L9 17l-5-5"/></svg>`;

    try {
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

        const response = await fetch("/cart/add", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({ variantId: parseInt(variantId), quantity: 1 })
        });

        const result = await response.json();

        if (response.ok && result.success) {
            showToast(result.message || "Đã thêm vào giỏ hàng!", "success");
            await updateCartCount();   // Cập nhật badge số lượng trên header
            await loadCartSidebar();   // Load items vào sidebar
            openCartSidebar();         // Mở sidebar
        } else {
            showToast(result.message || "Có lỗi xảy ra!", "error");
        }
    } catch (err) {
        console.error("Add to cart error:", err);
        showToast("Không thể thêm sản phẩm vào giỏ.", "error");
    } finally {
        // Khôi phục button sau 1.5s
        setTimeout(() => {
            button.innerHTML = originalHTML;
            button.disabled = false;
        }, 1500);
    }
}

// ── Cập nhật badge số lượng trên header ──────
async function updateCartCount() {
    try {
        const response = await fetch("/cart/count");
        const data = await response.json();
        // FIX: layout.html dùng id="cart-count", không phải "cart-count-badge"
        const badge = document.getElementById("cart-count");
        if (badge) badge.textContent = data.count ?? 0;

        // Cập nhật số trong header sidebar luôn
        const sidebarCount = document.getElementById("cart-sidebar-count");
        if (sidebarCount) sidebarCount.textContent = data.count ?? 0;
    } catch (err) {
        console.error("Failed to update cart count:", err);
    }
}

// ── Load items vào Cart Sidebar ───────────────
async function loadCartSidebar() {
    const body = document.getElementById("cart-sidebar-body");
    if (!body) return;

    try {
        const response = await fetch("/cart/summary");
        if (!response.ok) throw new Error("Not authenticated");

        const data = await response.json();

        // Cập nhật count
        const sidebarCount = document.getElementById("cart-sidebar-count");
        if (sidebarCount) sidebarCount.textContent = data.totalItems ?? 0;

        const badge = document.getElementById("cart-count");
        if (badge) badge.textContent = data.totalItems ?? 0;

        // Cập nhật tổng tiền
        const totalEl = document.getElementById("cart-sidebar-total");
        if (totalEl) totalEl.textContent = formatCurrency(data.totalAmount ?? 0);

        // Render items
        if (!data.items || data.items.length === 0) {
            body.innerHTML = `<div style="text-align:center;padding:60px 0;color:#999;font-size:14px">Giỏ hàng trống</div>`;
            return;
        }

        body.innerHTML = data.items.map(item => `
            <div class="cart-sidebar-item" data-item-id="${item.cartItemId}" style="display:flex;gap:12px;padding:16px 0;border-bottom:1px solid #f0f0f0;align-items:flex-start;">
                <a href="/products/${item.productSlug}" style="flex-shrink:0;">
                    <img src="${item.imageUrl || '/images/no-image.png'}"
                         alt="${item.productName}"
                         style="width:72px;height:90px;object-fit:cover;background:#f5f5f5;">
                </a>
                <div style="flex:1;min-width:0;">
                    <a href="/products/${item.productSlug}"
                       style="font-size:13px;font-weight:400;color:#1C1C1A;text-decoration:none;display:block;margin-bottom:4px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">
                        ${item.productName}
                    </a>
                    <div style="font-size:12px;color:#888;margin-bottom:8px;">
                        ${item.colorName ? item.colorName : ''}${item.colorName && item.sizeName ? ' / ' : ''}${item.sizeName ? item.sizeName : ''}
                    </div>
                    <div style="display:flex;align-items:center;justify-content:space-between;gap:8px;">
                        <div style="display:flex;align-items:center;border:1px solid #e0e0e0;height:28px;">
                            <button onclick="changeSidebarQty(${item.cartItemId}, ${item.quantity - 1})"
                                    style="width:28px;height:28px;border:none;background:none;cursor:pointer;font-size:16px;display:flex;align-items:center;justify-content:center;color:#555;">−</button>
                            <span style="width:28px;text-align:center;font-size:13px;">${item.quantity}</span>
                            <button onclick="changeSidebarQty(${item.cartItemId}, ${item.quantity + 1})"
                                    ${item.quantity >= item.maxStock ? 'disabled' : ''}
                                    style="width:28px;height:28px;border:none;background:none;cursor:pointer;font-size:16px;display:flex;align-items:center;justify-content:center;color:#555;">+</button>
                        </div>
                        <span style="font-size:13px;font-weight:500;color:#1C1C1A;">${formatCurrency(item.subtotal)}</span>
                        <button onclick="removeSidebarItem(${item.cartItemId})"
                                title="Xóa"
                                style="width:24px;height:24px;border:none;background:none;cursor:pointer;color:#aaa;display:flex;align-items:center;justify-content:center;padding:0;flex-shrink:0;">
                            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                            </svg>
                        </button>
                    </div>
                </div>
            </div>
        `).join('');

    } catch (err) {
        // Nếu chưa đăng nhập hoặc lỗi, không làm gì
        console.log("Cart sidebar load error (maybe not logged in):", err.message);
    }
}

// ── Thay đổi số lượng từ Sidebar ─────────────
async function changeSidebarQty(itemId, newQty) {
    if (newQty < 1) {
        removeSidebarItem(itemId);
        return;
    }

    try {
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

        // Dùng fetch thay vì form submit để không reload trang
        const params = new URLSearchParams({ quantity: newQty });
        await fetch(`/cart/update/${itemId}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                [csrfHeader]: csrfToken
            },
            body: params.toString()
        });

        await loadCartSidebar();
    } catch (err) {
        console.error("Update qty error:", err);
    }
}

// ── Xóa item khỏi Sidebar ────────────────────
async function removeSidebarItem(itemId) {
    try {
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

        await fetch(`/cart/remove/${itemId}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                [csrfHeader]: csrfToken
            }
        });

        await loadCartSidebar();
    } catch (err) {
        console.error("Remove item error:", err);
    }
}

// ── Mở/Đóng Sidebar ──────────────────────────
function openCartSidebar() {
    if (typeof toggleCartSidebar === "function") {
        toggleCartSidebar(true);
        return;
    }

    const sidebar = document.getElementById("cart-sidebar");
    const overlay = document.getElementById("cart-overlay");
    if (!sidebar || !overlay) return;

    sidebar.classList.add("open");
    overlay.classList.add("open");
    document.body.style.overflow = "hidden";
}

// ── Toast Notification ────────────────────────
function showToast(msg, type = "info") {
    const container = document.getElementById("toast-container") || document.body;
    const toast = document.createElement("div");
    toast.className = `toast toast--${type}`;
    toast.textContent = msg;
    toast.style.cssText = `
        padding: 12px 20px;
        background: ${type === 'success' ? '#1C1C1A' : '#e63946'};
        color: white;
        border-radius: 4px;
        font-size: 13px;
        letter-spacing: 0.02em;
        margin-top: 8px;
        animation: slideInRight 0.3s ease;
        pointer-events: none;
    `;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.animation = "fadeOut 0.3s ease forwards";
        setTimeout(() => toast.remove(), 300);
    }, 2700);
}

// ── Helper: Format tiền VNĐ ───────────────────
function formatCurrency(amount) {
    if (!amount && amount !== 0) return '0₫';
    return new Intl.NumberFormat('vi-VN').format(Math.round(amount)) + '₫';
}

// ── Quick View ────────────────────────────────
async function openQuickViewModal(button, event) {
    event.preventDefault();
    event.stopPropagation();

    const slug = button.getAttribute("data-product-slug");
    if (!slug) return;

    try {
        const response = await fetch(`/products/${slug}/quick-view`);
        const product = await response.json();
        renderQuickViewModal(product);
        document.getElementById("modal-overlay")?.classList.add("active");
    } catch (err) {
        console.error("Quick view error:", err);
        showToast("Không thể tải thông tin sản phẩm.", "error");
    }
}

function renderQuickViewModal(product) {
    const name = product.productName || product.name || '';
    const price = product.price || 0;
    const imageUrl = product.imageUrl || product.thumbnail || '';
    const category = product.categoryName || 'MINIMAL';

    const nameEl = document.getElementById("modal-name");
    const brandEl = document.getElementById("modal-brand");
    const imageEl = document.getElementById("modal-image");
    const priceEl = document.getElementById("modal-price-wrap");

    if (nameEl) nameEl.textContent = name;
    if (brandEl) brandEl.textContent = category.toUpperCase();
    if (imageEl) {
        if (imageUrl) {
            imageEl.innerHTML = `<img src="${imageUrl}" alt="${name}" style="width:100%;height:100%;object-fit:cover;">`;
            imageEl.classList.remove("img-placeholder");
        } else {
            imageEl.innerHTML = 'NO IMAGE';
            imageEl.classList.add("img-placeholder");
        }
    }
    if (priceEl) {
        priceEl.innerHTML = `<span class="product-price-current">${formatCurrency(price)}</span>`;
    }

    // Render variants
    const colorsEl = document.getElementById("modal-colors");
    const sizesEl = document.getElementById("modal-sizes");
    if (colorsEl) colorsEl.innerHTML = '';
    if (sizesEl) sizesEl.innerHTML = '';

    window._modalSelectedVariantId = null;
    window._modalVariants = product.variants || [];

    // Build unique colors
    const colors = [...new Set((product.variants || []).map(v => v.color).filter(Boolean))];
    const sizes = [...new Set((product.variants || []).map(v => v.sizeValue).filter(Boolean))];

    if (colorsEl && colors.length > 0) {
        colors.forEach(color => {
            const btn = document.createElement('button');
            btn.className = 'color-swatch';
            btn.textContent = color;
            btn.style.cssText = 'padding:4px 10px;border:1px solid #ccc;background:white;cursor:pointer;font-size:12px;margin:2px;';
            btn.onclick = () => {
                document.querySelectorAll('.color-swatch').forEach(b => b.style.borderColor = '#ccc');
                btn.style.borderColor = '#000';
                selectModalVariant();
            };
            btn.dataset.color = color;
            colorsEl.appendChild(btn);
        });
    }

    if (sizesEl && sizes.length > 0) {
        sizes.forEach(size => {
            const btn = document.createElement('button');
            btn.className = 'size-btn';
            btn.textContent = size;
            btn.style.cssText = 'padding:6px 14px;border:1px solid #ccc;background:white;cursor:pointer;font-size:12px;margin:2px;';
            btn.onclick = () => {
                document.querySelectorAll('.size-btn').forEach(b => b.style.borderColor = '#ccc');
                btn.style.borderColor = '#000';
                selectModalVariant();
            };
            btn.dataset.size = size;
            sizesEl.appendChild(btn);
        });
    }

    // Nếu chỉ có 1 variant, chọn luôn
    if ((product.variants || []).length === 1) {
        window._modalSelectedVariantId = product.variants[0].variantId || product.variants[0].id;
    }
}

function selectModalVariant() {
    const selectedColor = document.querySelector('.color-swatch[style*="border-color: rgb(0, 0, 0)"], .color-swatch[style*="border-color:#000"]')?.dataset.color;
    const selectedSize = document.querySelector('.size-btn[style*="border-color: rgb(0, 0, 0)"], .size-btn[style*="border-color:#000"]')?.dataset.size;
    const variants = window._modalVariants || [];

    const match = variants.find(v =>
        (!selectedColor || v.color === selectedColor) &&
        (!selectedSize || v.sizeValue === selectedSize)
    );

    window._modalSelectedVariantId = match ? (match.variantId || match.id) : null;
}

function changeModalQty(delta) {
    const input = document.getElementById("modal-qty");
    if (!input) return;
    const newVal = Math.max(1, parseInt(input.value || 1) + delta);
    input.value = newVal;
}

async function addModalToCart() {
    const variantId = window._modalSelectedVariantId;
    const qty = parseInt(document.getElementById("modal-qty")?.value || 1);

    if (!variantId) {
        showToast("Vui lòng chọn màu sắc và kích cỡ.", "error");
        return;
    }

    try {
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || '';
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

        const response = await fetch("/cart/add", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({ variantId: parseInt(variantId), quantity: qty })
        });

        const result = await response.json();
        if (response.ok && result.success) {
            showToast(result.message || "Đã thêm vào giỏ hàng!", "success");
            closeQuickView();
            await updateCartCount();
            await loadCartSidebar();
            openCartSidebar();
        } else {
            showToast(result.message || "Có lỗi xảy ra!", "error");
        }
    } catch (err) {
        console.error("Modal add to cart error:", err);
        showToast("Không thể thêm sản phẩm.", "error");
    }
}

// ── Wishlist Toggle ───────────────────────────
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
       if (result.isAdded) {
           button.classList.add("active");
           showToast("Đã thêm vào wishlist!", "success");
       } else {
           button.classList.remove("active");
           showToast("Đã xóa khỏi wishlist.", "info");
       }

       updateWishlistCount();
    } catch (err) {
        console.error("Wishlist toggle error:", err);
    }
}

// ── Khởi tạo khi trang load ───────────────────
document.addEventListener("DOMContentLoaded", () => {
    // Load cart sidebar data ngay khi trang load (để có sẵn items)
    loadCartSidebar();
});
