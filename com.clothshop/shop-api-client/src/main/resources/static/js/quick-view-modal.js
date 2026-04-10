/**
 * Cập nhật để map đúng ProductDetailResponse và VariantDetailResponse của project.

 */

(function () {
    'use strict';

    /* ──────────────────────────────────────────────
       STATE
    ────────────────────────────────────────────── */
    const state = {
        slug:              null,
        product:           null,
        selectedVariantId: null,
        selectedSize:      null,   // sizeValue string
        quantity:          1,
        maxStock:          99,
        loading:           false,
    };

    /* ──────────────────────────────────────────────
       DOM HELPERS
    ────────────────────────────────────────────── */
    const $ = (id) => document.getElementById(id);

    /* ──────────────────────────────────────────────
       CSRF TOKEN (bắt buộc vì SecurityConfig dùng CookieCsrfTokenRepository)
       Spring đặt token trong cookie "XSRF-TOKEN", gửi lại qua header "X-XSRF-TOKEN"
    ────────────────────────────────────────────── */
    function getCsrfToken() {
        const match = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
        return match ? decodeURIComponent(match[1]) : null;
    }

    function csrfHeaders() {
        const token = getCsrfToken();
        return token ? { 'X-XSRF-TOKEN': token } : {};
    }

    /* ──────────────────────────────────────────────
       FORMAT PRICE
    ────────────────────────────────────────────── */
    function formatPrice(amount) {
        if (amount == null) return '0₫';
        return new Intl.NumberFormat('vi-VN').format(amount) + '₫';
    }

    /* ──────────────────────────────────────────────
       OPEN / CLOSE
    ────────────────────────────────────────────── */
    window.openQuickViewModal = function (btn, event) {
        if (event) event.preventDefault();
        const slug = btn.dataset.productSlug;
        if (!slug) return;

        resetState();
        state.slug = slug;
        showOverlay();
        showLoading(true);

        fetch(`/products/${encodeURIComponent(slug)}/quick-view`, {
            headers: { 'Accept': 'application/json' }
        })
            .then(res => {
                if (!res.ok) throw new Error('Không thể tải thông tin sản phẩm.');
                return res.json();
            })
            .then(data => {
                state.product = data;
                renderModal(data);
                showLoading(false);
            })
            .catch(err => {
                showLoading(false);
                showError(err.message || 'Có lỗi xảy ra, vui lòng thử lại.');
            });
    };

    /**
     * Nút Add to Cart trên card:
     *   th:data-variant-id="${item.defaultVariantId}"
     *   th:data-product-slug="${item.slug}"
     */
    window.quickAddToCart = function (btn, event) {
        if (event) event.preventDefault();
        const variantId = btn.dataset.variantId;
        const slug      = btn.dataset.productSlug;

        if (!variantId || variantId === 'null' || variantId === '') {
            if (slug) openQuickViewModal(btn, event);
            return;
        }
        postAddToCart(variantId, 1, btn);
    };

    window.closeQuickViewModal = function () {
        const overlay = $('quickViewModal');
        if (!overlay) return;
        overlay.classList.remove('is-open');
        document.body.style.overflow = '';
        setTimeout(() => {
            const c = $('qvContent');
            if (c) c.style.display = 'none';
        }, 300);
    };

    /* ──────────────────────────────────────────────
       RENDER MODAL
       ProductDetailResponse fields:
         productId, productName, productSlug, categoryName,
         description, price, imageUrl, available,
         variants: VariantDetailResponse[], images: List<String>
    ────────────────────────────────────────────── */
    function renderModal(p) {
        $('qvBrand').textContent       = p.categoryName || 'MINIMAL';
        $('qvProductName').textContent = p.productName  || '';
        $('qvDescription').textContent = p.description  || '';
        $('qvDetailLink').href         = `/products/${p.productSlug || state.slug}`;

        renderPrice(p);
        renderImages(p);
        renderVariantsAndSizes(p);
        updateQtyDisplay();
        refreshAddBtn();
    }

    /* ── Price ──
       ProductDetailResponse chỉ có 1 field: price (Double)
       Không có salePrice / originalPrice → ẩn discount badge
    ────────────────────────────────────────────── */
    function renderPrice(p) {
        $('qvPrice').textContent           = formatPrice(p.price);
        $('qvPriceOriginal').style.display = 'none';
        $('qvDiscountBadge').style.display = 'none';
        $('qvBadge').style.display         = 'none';
    }

    /* ── Images ──
       p.imageUrl  → ảnh chính
       p.images    → List<String> (URL thuần, không phải object)
    ────────────────────────────────────────────── */
    function renderImages(p) {
        const mainUrl = p.imageUrl || (p.images && p.images[0]) || '';
        $('qvMainImage').src = mainUrl;
        $('qvMainImage').alt = p.productName || '';

        const thumbsEl = $('qvThumbs');
        thumbsEl.innerHTML = '';

        // p.images là List<String>
        const images = Array.isArray(p.images) ? p.images : [];
        if (images.length > 1) {
            images.slice(0, 6).forEach((url, idx) => {
                const t     = document.createElement('img');
                t.src       = url;              // String URL trực tiếp
                t.alt       = `Ảnh ${idx + 1}`;
                t.className = 'qv-thumb' + (idx === 0 ? ' is-active' : '');
                t.addEventListener('click', () => {
                    $('qvMainImage').src = url;
                    thumbsEl.querySelectorAll('.qv-thumb')
                             .forEach(th => th.classList.remove('is-active'));
                    t.classList.add('is-active');
                });
                thumbsEl.appendChild(t);
            });
        }
    }

    /* ── Variants & Sizes ──
       VariantDetailResponse fields:
         id, sku, color, sizeValue, retailPrice, stockQuantity, imageUrl
    ────────────────────────────────────────────── */
    function renderVariantsAndSizes(p) {
        const variants = p.variants || [];

        if (!variants.length) {
            $('qvVariantSection').style.display = 'none';
            $('qvSizeSection').style.display    = 'none';
            return;
        }

        /* ── Nhóm theo color ── */
        const colorMap = buildColorMap(variants); // Map<colorLabel, VariantDetailResponse[]>

        /* Kiểm tra có nhiều màu không */
        const hasColors = colorMap.size > 1 || !colorMap.has('Mặc định');

        if (hasColors) {
            $('qvVariantSection').style.display = '';
            const variantList = $('qvVariantList');
            variantList.innerHTML = '';

            colorMap.forEach((vList, label) => {
                const btn       = document.createElement('button');
                btn.type        = 'button';
                btn.className   = 'qv-option';
                btn.textContent = label;           // VariantDetailResponse.color
                btn.dataset.color = label;

                btn.addEventListener('click', () => {
                    variantList.querySelectorAll('.qv-option')
                               .forEach(b => b.classList.remove('is-selected'));
                    btn.classList.add('is-selected');
                    $('qvSelectedVariantLabel').textContent = label;

                    // Reset size
                    state.selectedVariantId = null;
                    state.selectedSize      = null;
                    $('qvSelectedSizeLabel').textContent = '';

                    buildSizeChips(vList);
                    clearError();
                    refreshAddBtn();
                });

                variantList.appendChild(btn);
            });
        } else {
            $('qvVariantSection').style.display = 'none';
        }

        /* ── Sizes — build từ TẤT CẢ variants ban đầu ── */
        const hasSizes = variants.some(v => v.sizeValue); // field: sizeValue
        if (hasSizes) {
            $('qvSizeSection').style.display = '';
            // Nếu không có filter màu → hiện sizes của toàn bộ
            if (!hasColors) buildSizeChips(variants);
            else            $('qvSizeList').innerHTML = ''; // chờ user chọn màu trước
        } else {
            $('qvSizeSection').style.display = 'none';
            // Không có size → auto pick variant đầu (hoặc duy nhất)
            if (variants.length === 1) state.selectedVariantId = variants[0].id;
        }
    }

    function buildSizeChips(variants) {
        const sizeList = $('qvSizeList');
        sizeList.innerHTML = '';
        state.selectedSize      = null;
        state.selectedVariantId = null;
        $('qvSelectedSizeLabel').textContent = '';

        const seen = new Set();
        variants.forEach(v => {
            const sizeLabel = v.sizeValue; // VariantDetailResponse.sizeValue
            if (!sizeLabel || seen.has(sizeLabel)) return;
            seen.add(sizeLabel);

            const btn       = document.createElement('button');
            btn.type        = 'button';
            btn.className   = 'qv-option';
            btn.textContent = sizeLabel;

            const outOfStock = (v.stockQuantity || 0) <= 0;
            if (outOfStock) btn.classList.add('is-disabled');

            btn.addEventListener('click', () => {
                if (outOfStock) return;
                sizeList.querySelectorAll('.qv-option')
                        .forEach(b => b.classList.remove('is-selected'));
                btn.classList.add('is-selected');
                $('qvSelectedSizeLabel').textContent = sizeLabel;
                state.selectedSize = sizeLabel;

                // Tìm variant khớp color đang chọn + size này
                const selectedColor = $('qvVariantList').querySelector('.is-selected')?.dataset.color || null;
                const matched = variants.find(vv => {
                    const colorMatch = !selectedColor || vv.color === selectedColor;
                    return colorMatch && vv.sizeValue === sizeLabel;
                });

                if (matched) {
                    state.selectedVariantId = matched.id;
                    state.maxStock          = matched.stockQuantity || 99;
                    if (state.quantity > state.maxStock) state.quantity = state.maxStock;
                    $('qvStockHint').textContent = matched.stockQuantity <= 10
                        ? `Còn ${matched.stockQuantity} sản phẩm`
                        : '';
                }

                clearError();
                refreshAddBtn();
                updateQtyDisplay();
            });

            sizeList.appendChild(btn);
        });
    }

    function buildColorMap(variants) {
        const map = new Map();
        variants.forEach(v => {
            const label = v.color || 'Mặc định'; // VariantDetailResponse.color
            if (!map.has(label)) map.set(label, []);
            map.get(label).push(v);
        });
        return map;
    }

    /* ──────────────────────────────────────────────
       QUANTITY
    ────────────────────────────────────────────── */
    window.qvChangeQty = function (delta) {
        const next = state.quantity + delta;
        if (next < 1 || next > state.maxStock) return;
        state.quantity = next;
        updateQtyDisplay();
    };

    function updateQtyDisplay() {
        $('qvQtyDisplay').textContent = state.quantity;
    }

    /* ──────────────────────────────────────────────
       ADD TO CART
    ────────────────────────────────────────────── */
    window.qvAddToCart = function () {
        const p        = state.product;
        const variants = p?.variants || [];
        const hasSizes  = variants.some(v => v.sizeValue);
        const colorMap  = buildColorMap(variants);
        const hasColors = colorMap.size > 1 || !colorMap.has('Mặc định');

        if (hasColors && !$('qvVariantList').querySelector('.is-selected')) {
            showError('Vui lòng chọn phân loại sản phẩm.'); return;
        }
        if (hasSizes && !state.selectedSize) {
            showError('Vui lòng chọn kích cỡ.'); return;
        }
        if (!state.selectedVariantId) {
            showError('Không xác định được sản phẩm, vui lòng chọn lại.'); return;
        }

        postAddToCart(state.selectedVariantId, state.quantity, $('qvAddBtn'));
    };

    function postAddToCart(variantId, quantity, triggerEl) {
        if (state.loading) return;
        state.loading = true;

        if (triggerEl) {
            triggerEl.classList.add('is-loading');
            triggerEl.disabled = true;
        }

        fetch('/api/cart/add', {
            method:  'POST',
            headers: {
                'Content-Type': 'application/json',
                ...csrfHeaders()          // ← gửi CSRF token
            },
            body: JSON.stringify({ variantId: variantId, quantity: quantity }),
        })
            .then(res => {
                if (res.status === 401 || res.status === 403) {
                    window.location.href = '/login';
                    throw new Error('redirect');
                }
                if (!res.ok) return res.json().then(d => { throw new Error(d.message || 'Lỗi thêm vào giỏ hàng.'); });
                return res.json().catch(() => ({}));
            })
            .then(data => {
                closeQuickViewModal();
                showToast('Đã thêm vào giỏ hàng! 🛒', false);
                // Cập nhật badge nếu response trả về cartCount
                if (data.cartCount != null) setCartBadge(data.cartCount);
                else fetchAndUpdateCartBadge();
            })
            .catch(err => {
                if (err.message === 'redirect') return;
                showError(err.message);
                showToast(err.message, true);
            })
            .finally(() => {
                state.loading = false;
                if (triggerEl) {
                    triggerEl.classList.remove('is-loading');
                    triggerEl.disabled = false;
                }
            });
    }

    /* ──────────────────────────────────────────────
       CART BADGE
    ────────────────────────────────────────────── */
    function fetchAndUpdateCartBadge() {
        fetch('/api/cart/count', { headers: { 'Accept': 'application/json' } })
            .then(r => r.ok ? r.json() : null)
            .then(data => {
                if (!data) return;
                const count = typeof data === 'number' ? data : (data.count ?? 0);
                setCartBadge(count);
            })
            .catch(() => {});
    }

    function setCartBadge(count) {
        document.querySelectorAll('.cart-badge, .cart-count, [data-cart-count]').forEach(el => {
            el.textContent    = count;
            el.style.display  = count > 0 ? '' : 'none';
        });
    }

    /* ──────────────────────────────────────────────
       UI HELPERS
    ────────────────────────────────────────────── */
    function showOverlay() {
        const overlay = $('quickViewModal');
        if (!overlay) return;
        overlay.classList.add('is-open');
        document.body.style.overflow = 'hidden';
    }

    function showLoading(show) {
        $('qvLoading').style.display = show ? ''     : 'none';
        $('qvContent').style.display = show ? 'none' : 'grid';
    }

    function showError(msg) {
        const e = $('qvError');
        e.textContent   = msg;
        e.style.display = msg ? '' : 'none';
    }

    function clearError() { showError(''); }

    function refreshAddBtn() {
        const btn = $('qvAddBtn');
        if (!btn) return;

        const variants  = state.product?.variants || [];
        const hasSizes  = variants.some(v => v.sizeValue);
        const colorMap  = buildColorMap(variants);
        const hasColors = colorMap.size > 1 || !colorMap.has('Mặc định');

        const variantPicked = !hasColors || !!$('qvVariantList').querySelector('.is-selected');
        const sizePicked    = !hasSizes  || !!state.selectedSize;

        btn.disabled = !(variantPicked && sizePicked);
    }

    /* ── Toast ── */
    let toastTimer = null;
    function showToast(msg, isError = false) {
        let toast = document.querySelector('.qv-toast');
        if (!toast) {
            toast = document.createElement('div');
            toast.className = 'qv-toast';
            document.body.appendChild(toast);
        }
        toast.textContent = msg;
        toast.classList.toggle('is-error', isError);
        toast.classList.add('is-visible');
        clearTimeout(toastTimer);
        toastTimer = setTimeout(() => toast.classList.remove('is-visible'), 3000);
    }

    /* ──────────────────────────────────────────────
       RESET
    ────────────────────────────────────────────── */
    function resetState() {
        state.product           = null;
        state.selectedVariantId = null;
        state.selectedSize      = null;
        state.quantity          = 1;
        state.maxStock          = 99;
        state.loading           = false;
        clearError();
        $('qvSelectedVariantLabel').textContent = '';
        $('qvSelectedSizeLabel').textContent    = '';
        $('qvStockHint').textContent            = '';
    }

    /* ── Keyboard ── */
    document.addEventListener('keydown', e => {
        if (e.key === 'Escape') closeQuickViewModal();
    });

})();