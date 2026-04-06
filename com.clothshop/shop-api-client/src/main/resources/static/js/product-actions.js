/**
 * Frontend handlers for product card actions (wishlist, quick view, add to cart)
 * Used on home page and wishlist pages
 */

// CSRF Token utility
function getCsrfToken() {
    return document.querySelector('meta[name="_csrf"]')?.content || '';
}

function getCsrfHeader() {
    return document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
}

/**
 * Wishlist Toggle Handler
 * Toggles product in/out of wishlist via AJAX
 */
function wishlistToggle(button, event) {
    if (event) {
        event.preventDefault?.();
        event.stopPropagation?.();
        event.stopImmediatePropagation?.();
    }
    
    const productId = button.dataset.productId;
    if (!productId) {
        showToast('Sản phẩm không hợp lệ', 'error');
        return false;
    }

    // Disable button while loading
    button.disabled = true;
    const originalOpacity = button.style.opacity;
    button.style.opacity = '0.6';

    fetch(`/wishlist/toggle/${productId}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [getCsrfHeader()]: getCsrfToken()
        }
    })
    .then(r => {
        if (r.status === 401) {
            window.location.href = '/login';
            return null;
        }
        return r.json();
    })
    .then(data => {
        button.disabled = false;
        button.style.opacity = originalOpacity;
        
        if (data && data.success) {
            const svg = button.querySelector('svg');
            if (data.isAdded) {
                button.classList.add('active');
                if (svg) {
                    svg.setAttribute('fill', 'currentColor');
                    svg.style.fill = 'currentColor';
                }
            } else {
                button.classList.remove('active');
                if (svg) {
                    svg.setAttribute('fill', 'none');
                    svg.style.fill = 'none';
                }
            }
            
            showToast(data.message || (data.isAdded ? 'Đã thêm vào yêu thích' : 'Đã xóa khỏi yêu thích'), 'success');
            loadWishlistCount();
        } else if (data && data.message) {
            showToast(data.message, 'error');
        }
    })
    .catch(e => {
        button.disabled = false;
        button.style.opacity = originalOpacity;
        console.error('Wishlist toggle error:', e);
        showToast('Có lỗi xảy ra', 'error');
    });
    
    return false;
}

/**
 * Quick View Modal Handler
 * Fetches product data and displays in modal
 */
function openQuickViewModal(button, event) {
    if (event) {
        event.preventDefault?.();
        event.stopPropagation?.();
        event.stopImmediatePropagation?.();
    }
    
    const productSlug = button.dataset.productSlug;
    if (!productSlug) {
        showToast('Sản phẩm không hợp lệ', 'error');
        return false;
    }

    // Show loading state
    showQuickViewModal(null, 'loading');
    
    fetch(`/products/${productSlug}/quick-view`)
        .then(r => r.json())
        .then(product => {
            if (product.productId) {
                showQuickViewModal(product);
            } else {
                showToast('Không thể tải sản phẩm', 'error');
                closeQuickViewModal();
            }
        })
        .catch(e => {
            console.error('Quick view error:', e);
            showToast('Có lỗi xảy ra', 'error');
            closeQuickViewModal();
        });
    
    return false;
}

/**
 * Show Quick View Modal with product data
 */
function showQuickViewModal(product, state = 'loaded') {
    const modal = document.getElementById('quick-view-modal') || createQuickViewModal();
    if (!modal) return;

    if (state === 'loading') {
        modal.innerHTML = '<div style="padding: 40px; text-align: center;">Đang tải...</div>';
        modal.style.display = 'block';
        return;
    }

    if (!product) return;

    // Store product ID for add to cart
    modal.dataset.productId = product.productId;
    modal.dataset.productSlug = product.productSlug;
    modal.dataset.variants = JSON.stringify(product.variants || []);

    // Render modal content
    const variantOptions = renderVariantOptions(product.variants || []);
    
    modal.innerHTML = `
        <button class="modal-close" onclick="closeQuickViewModal()" style="position: absolute; top: 12px; right: 16px; background: none; border: none; font-size: 24px; cursor: pointer; z-index: 10;">✕</button>
        
        <div class="modal-inner" style="display: grid; grid-template-columns: 1fr 1fr; gap: 24px; padding: 32px;">
            <!-- Product Image -->
            <div class="modal-image-section" style="display: flex; align-items: center; justify-content: center; background: #f5f5f5; border-radius: 4px; min-height: 400px;">
                <div style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center;">
                    <img src="${product.imageUrl || product.images?.[0] || ''}" alt="${product.productName}" 
                        style="max-width: 100%; max-height: 100%; object-fit: contain;">
                </div>
            </div>

            <!-- Product Info -->
            <div class="modal-info-section">
                <h2 style="font-size: 22px; font-weight: 300; margin-bottom: 8px;">${product.productName}</h2>
                <p style="font-size: 11px; letter-spacing: 2px; text-transform: uppercase; color: #999; margin-bottom: 12px;">${product.categoryName || 'MINIMAL'}</p>
                <div style="font-size: 20px; font-weight: 600; margin-bottom: 16px; color: #1C1C1A;">
                    ${(product.price ? product.price.toLocaleString('vi-VN') : '0')}₫
                </div>
                
                <!-- Variants -->
                ${variantOptions}
                
                <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 16px;">
                    <label style="font-size: 12px; text-transform: uppercase; letter-spacing: 0.05em; color: #999; flex: 0 0 50px;">Số lượng</label>
                    <div style="display: flex; align-items: center; gap: 8px; border: 1px solid #e8e8e8; border-radius: 4px; padding: 0 8px;">
                        <button onclick="changeQuickViewQty(-1)" style="background: none; border: none; cursor: pointer; padding: 6px;">−</button>
                        <input id="quick-view-qty" type="number" value="1" min="1" max="10" style="width: 40px; text-align: center; border: none; outline: none;">
                        <button onclick="changeQuickViewQty(1)" style="background: none; border: none; cursor: pointer; padding: 6px;">+</button>
                    </div>
                </div>

                <!-- Buttons -->
                <div style="display: flex; gap: 12px;">
                    <button class="btn btn--primary" style="flex: 1;" onclick="addQuickViewToCart()">Thêm vào giỏ</button>
                    <button class="btn btn--outline" style="flex: 1;" onclick="viewProductDetail()">Xem chi tiết</button>
                </div>
            </div>
        </div>
    `;
    
    modal.style.display = 'block';
}

/**
 * Create Quick View Modal if it doesn't exist
 */
function createQuickViewModal() {
    let modal = document.getElementById('quick-view-modal');
    if (modal) return modal;

    const wrapper = document.createElement('div');
    wrapper.id = 'quick-view-wrapper';
    wrapper.style.cssText = 'position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.4); z-index: 1000; display: none;';
    wrapper.onclick = e => {
        if (e.target === wrapper) closeQuickViewModal();
    };

    modal = document.createElement('div');
    modal.id = 'quick-view-modal';
    modal.style.cssText = 'position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); background: white; border-radius: 8px; width: 90%; max-width: 800px; max-height: 90vh; overflow-y: auto; box-shadow: 0 20px 60px rgba(0,0,0,0.15);';
    
    wrapper.appendChild(modal);
    document.body.appendChild(wrapper);
    
    return modal;
}

/**
 * Render variant options (colors and sizes)
 */
function renderVariantOptions(variants) {
    if (!variants || variants.length === 0) return '';

    const colors = new Set();
    const sizes = new Set();

    variants.forEach(v => {
        if (v.color) colors.add(v.color);
        if (v.sizeValue) sizes.add(v.sizeValue);
    });

    let html = '<div style="margin-bottom: 16px;">';
    
    if (colors.size > 0) {
        html += `
            <div style="margin-bottom: 12px;">
                <label style="font-size: 12px; text-transform: uppercase; letter-spacing: 0.05em; color: #999;">Màu sắc</label>
                <div id="quick-view-colors" style="display: flex; gap: 8px; margin-top: 8px;">
                    ${Array.from(colors).map((color, i) => `
                        <div class="color-swatch ${i === 0 ? 'selected' : ''}" 
                            style="width: 32px; height: 32px; border-radius: 4px; background: ${color}; cursor: pointer; border: ${i === 0 ? '2px solid #1C1C1A' : '1px solid #e8e8e8'}; transition: all 0.2s;"
                            onclick="selectQuickViewColor(this, '${color}')"></div>
                    `).join('')}
                </div>
            </div>
        `;
    }

    if (sizes.size > 0) {
        html += `
            <div style="margin-bottom: 12px;">
                <label style="font-size: 12px; text-transform: uppercase; letter-spacing: 0.05em; color: #999;">Kích cỡ</label>
                <div id="quick-view-sizes" style="display: flex; gap: 8px; margin-top: 8px; flex-wrap: wrap;">
                    ${Array.from(sizes).map((size, i) => `
                        <button class="size-btn ${i === 0 ? 'selected' : ''}"
                            onclick="selectQuickViewSize(this, '${size}')"
                            style="padding: 8px 12px; border: ${i === 0 ? '2px solid #1C1C1A' : '1px solid #e8e8e8'}; background: white; cursor: pointer; border-radius: 4px; font-size: 12px; transition: all 0.2s;">
                            ${size}
                        </button>
                    `).join('')}
                </div>
            </div>
        `;
    }

    html += '</div>';
    return html;
}

function selectQuickViewColor(el, color) {
    document.querySelectorAll('#quick-view-colors .color-swatch').forEach(s => {
        s.style.border = '1px solid #e8e8e8';
    });
    el.style.border = '2px solid #1C1C1A';
}

function selectQuickViewSize(el, size) {
    document.querySelectorAll('#quick-view-sizes .size-btn').forEach(b => {
        b.style.border = '1px solid #e8e8e8';
        b.style.color = '#1C1C1A';
    });
    el.style.border = '2px solid #1C1C1A';
}

function changeQuickViewQty(delta) {
    const input = document.getElementById('quick-view-qty');
    if (input) {
        input.value = Math.max(1, Math.min(10, parseInt(input.value || 1) + delta));
    }
}

function addQuickViewToCart() {
    const modal = document.getElementById('quick-view-modal');
    const variantJson = modal?.dataset.variants;
    const qty = parseInt(document.getElementById('quick-view-qty')?.value || 1);

    if (!variantJson) {
        showToast('Sản phẩm không hợp lệ', 'error');
        return;
    }

    let variants = [];
    try {
        variants = JSON.parse(variantJson);
    } catch (e) {
        console.error('Failed to parse variants:', e);
        showToast('Có lỗi xảy ra', 'error');
        return;
    }

    if (!variants || variants.length === 0) {
        showToast('Biến thể không khả dụng', 'error');
        return;
    }

    // Use first variant ID
    const variantId = variants[0].variantId;
    if (!variantId) {
        showToast('Biến thể không hợp lệ', 'error');
        return;
    }

    fetch('/cart/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [getCsrfHeader()]: getCsrfToken()
        },
        body: JSON.stringify({
            variantId: variantId,
            quantity: qty
        })
    })
    .then(r => r.json())
    .then(data => {
        if (data.success) {
            showToast(data.message || 'Đã thêm vào giỏ hàng', 'success');
            loadCartCount();
            closeQuickViewModal();
        } else {
            showToast(data.message || 'Không thể thêm vào giỏ', 'error');
        }
    })
    .catch(e => {
        console.error('Add to cart error:', e);
        showToast('Có lỗi xảy ra', 'error');
    });
}

function viewProductDetail() {
    const modal = document.getElementById('quick-view-modal');
    const slug = modal?.dataset.productSlug;
    if (slug) {
        window.location.href = `/products/${slug}`;
    }
    closeQuickViewModal();
}

function closeQuickViewModal() {
    const wrapper = document.getElementById('quick-view-wrapper');
    if (wrapper) {
        wrapper.style.display = 'none';
    }
    const modal = document.getElementById('quick-view-modal');
    if (modal) {
        modal.style.display = 'none';
    }
}

/**
 * Quick Add to Cart Handler
 * Directly adds product to cart from product card
 */
function quickAddToCart(button, event) {
    if (event) {
        event.preventDefault?.();
        event.stopPropagation?.();
        event.stopImmediatePropagation?.();
    }
    
    const productId = button.dataset.productId;
    const productSlug = button.parentElement.parentElement.parentElement.dataset.productSlug || button.dataset.productSlug;
    
    if (!productId || !productSlug) {
        showToast('Sản phẩm không hợp lệ', 'error');
        return false;
    }

    // Disable button while loading
    button.disabled = true;
    const originalHtml = button.innerHTML;
    button.innerHTML = '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" style="animation: spin 1s linear infinite;"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/><path d="M12 2a10 10 0 0 1 0 20" stroke="currentColor" stroke-width="2"/></svg>';

    // First fetch product details to get variant ID
    fetch(`/products/${productSlug}/quick-view`)
        .then(r => r.json())
        .then(product => {
            if (!product.variants || product.variants.length === 0) {
                showToast('Sản phẩm không có biến thể', 'error');
                button.disabled = false;
                button.innerHTML = originalHtml;
                return;
            }
            
            const variantId = product.variants[0].variantId;
            
            // Then add to cart with proper variant ID
            return fetch('/cart/add', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [getCsrfHeader()]: getCsrfToken()
                },
                body: JSON.stringify({
                    variantId: variantId,
                    quantity: 1
                })
            })
            .then(r => {
                if (r.status === 401) {
                    window.location.href = '/login';
                    return null;
                }
                return r.json();
            })
            .then(data => {
                button.disabled = false;
                button.innerHTML = originalHtml;
                
                if (data && data.success) {
                    showToast('Đã thêm vào giỏ hàng', 'success');
                    loadCartCount();
                } else if (data && data.message) {
                    showToast(data.message, 'error');
                } else {
                    showToast('Không thể thêm vào giỏ', 'error');
                }
            });
        })
        .catch(e => {
            button.disabled = false;
            button.innerHTML = originalHtml;
            console.error('Quick add to cart error:', e);
            showToast('Có lỗi xảy ra', 'error');
        });
    
    return false;
}

/**
 * Toast Notification
 */
function showToast(message, type = 'default') {
    const container = document.getElementById('toast-container') || createToastContainer();
    const toast = document.createElement('div');
    
    const colors = {
        'success': '#4CAF50',
        'error': '#f44336',
        'warning': '#ff9800',
        'default': '#333'
    };

    toast.style.cssText = `
        position: fixed;
        bottom: 20px;
        right: 20px;
        padding: 12px 16px;
        background: ${colors[type] || colors['default']};
        color: white;
        border-radius: 4px;
        font-size: 14px;
        z-index: 2000;
        animation: slideInUp 0.3s ease;
    `;
    toast.textContent = message;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.animation = 'slideOutDown 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

function createToastContainer() {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }
    return container;
}

// Global reload functions used by layout.html
function loadWishlistCount() {
    fetch('/wishlist/count')
        .then(r => r.json())
        .then(data => {
            const badge = document.getElementById('wishlist-count');
            if (badge && data.count > 0) {
                badge.textContent = data.count;
                badge.style.display = 'flex';
            } else if (badge) {
                badge.style.display = 'none';
            }
        })
        .catch(e => console.log('Failed to load wishlist count:', e));
}

function loadCartCount() {
    fetch('/cart/count')
        .then(r => r.json())
        .then(data => {
            const badge = document.getElementById('cart-count');
            if (badge) {
                badge.textContent = data.count;
            }
        })
        .catch(e => console.log('Failed to load cart count:', e));
}

// Add CSS animations
if (!document.getElementById('product-actions-styles')) {
    const style = document.createElement('style');
    style.id = 'product-actions-styles';
    style.textContent = `
        @keyframes slideInUp {
            from {
                opacity: 0;
                transform: translateY(20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        @keyframes slideOutDown {
            from {
                opacity: 1;
                transform: translateY(0);
            }
            to {
                opacity: 0;
                transform: translateY(20px);
            }
        }

        .product-card-action-btn {
            width: 36px;
            height: 36px;
            border-radius: 4px;
            background: white;
            border: 1px solid #e8e8e8;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.2s ease;
            color: #1C1C1A;
        }

        .product-card-action-btn:hover {
            background: #f5f5f5;
            border-color: #ccc;
        }

        .product-card-action-btn.active {
            background: #1C1C1A;
            border-color: #1C1C1A;
            color: white;
            fill: white;
        }
    `;
    document.head.appendChild(style);
}
