function changeQty(btn, delta) {
            const form = btn.closest('form');
            const input = form.querySelector('input[name="quantity"]');
            const max = parseInt(input.getAttribute('max') || '999999');
            let val = parseInt(input.value || '1') + delta;

            if (val < 1) val = 1;
            if (val > max) val = max;

            input.value = val;
            submitCartFormAjax(form);
        }

        function submitCartFormAjax(form) {
            const action = form.getAttribute('action');
            const formData = new FormData(form);

            form.style.opacity = '0.5';

            fetch(action, {
                method: 'POST',
                body: new URLSearchParams(formData),
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            })
            .then(r => r.json())
            .then(data => {
                form.style.opacity = '1';
                if (data.success) {
                    refreshCartUI();
                } else {
                    alert(data.message || 'Có lỗi xảy ra');
                }
            })
            .catch(e => {
                form.style.opacity = '1';
                console.error('Lỗi khi cập nhật giỏ hàng:', e);
            });
        }

        function refreshCartUI() {
            fetch('/cart/summary')
                .then(r => r.json())
                .then(data => {
                    // Cập nhật lại UI dựa trên dữ liệu mới
                    if (!data || !data.items || data.items.length === 0) {
                        const cartTable = document.querySelector('.cart-table');
                        if (cartTable) {
                            cartTable.innerHTML = `
                                <div class="empty-cart">
                                    <p class="empty-cart__text">Giỏ hàng của bạn đang trống</p>
                                    <a class="empty-cart__btn" href="/products">Tiếp tục mua sắm</a>
                                </div>
                            `;
                        }
                        const sumSubtotal = document.getElementById('sum-subtotal');
                        if (sumSubtotal) sumSubtotal.textContent = '0đ';
                        recalcTotal();
                        updateCartBadge(0);
                        return;
                    }

                    const currentRows = document.querySelectorAll('.cart-table__row');
                    currentRows.forEach(row => {
                        const itemId = parseInt(row.getAttribute('data-item-id'));
                        const itemData = data.items.find(i => i.cartItemId === itemId);

                        if (!itemData) {
                            row.style.opacity = '0';
                            row.style.transform = 'scale(0.95)';
                            row.style.transition = 'all 0.3s ease';
                            setTimeout(() => row.remove(), 300);
                        } else {
                            const lineTotalEl = row.querySelector('.cart-line-total');
                            if (lineTotalEl) {
                                lineTotalEl.textContent = Number(itemData.subtotal).toLocaleString('vi-VN') + 'đ';
                            }
                            const inputEl = row.querySelector('input[name="quantity"]');
                            if (inputEl && inputEl.value != itemData.quantity) {
                                inputEl.value = itemData.quantity;
                            }
                        }
                    });

                    const sumSubtotal = document.getElementById('sum-subtotal');
                    if (sumSubtotal) {
                        sumSubtotal.textContent = Number(data.totalAmount).toLocaleString('vi-VN') + 'đ';
                    }

                    const voucherInput = document.getElementById('voucher-input');
                    if (voucherInput && voucherInput.value.trim() !== '') {
                        applyVoucher();
                    } else {
                        recalcTotal();
                    }
                    updateCartBadge(data.totalItems);
                })
                .catch(e => console.error('Lỗi update UI:', e));
        }

        function updateCartBadge(count) {
            const el1 = document.getElementById('cart-count');
            const el2 = document.getElementById('cart-sidebar-count');

            if (el1) {
                el1.textContent = count;
                el1.style.display = count > 0 ? 'inline-flex' : 'none';
            }

            if (el2) {
                el2.textContent = count;
            }
        }

        function applyVoucher() {
            const code = document.getElementById('voucher-input').value.trim();
            const msgEl = document.getElementById('voucher-msg');

            if (!code) {
                msgEl.style.color = '#d94f4f';
                msgEl.textContent = 'Vui lòng nhập mã giảm giá.';
                return;
            }

            const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
            const headers = { 'Content-Type': 'application/x-www-form-urlencoded' };

            if (csrfToken && csrfHeader) {
                headers[csrfHeader] = csrfToken;
            }

            fetch('/checkout/calculate?voucherCode=' + encodeURIComponent(code), {
                method: 'POST',
                headers
            })
            .then(r => r.json())
            .then(data => {
                if (data.error) {
                    msgEl.style.color = '#d94f4f';
                    msgEl.textContent = data.error;
                    document.getElementById('sum-discount').textContent = '-0đ';
                    recalcTotal();
                } else {
                    msgEl.style.color = '#3b8a5a';
                    msgEl.textContent = data.voucherMessage || 'Áp dụng thành công!';

                    if (data.discount != null) {
                        document.getElementById('sum-discount').textContent =
                            '-' + Number(data.discount).toLocaleString('vi-VN') + 'đ';
                    }

                    if (data.totalAmount != null) {
                        document.getElementById('sum-subtotal').textContent =
                            Number(data.totalAmount).toLocaleString('vi-VN') + 'đ';
                    }

                    if (data.finalAmount != null) {
                        document.getElementById('sum-total').textContent =
                            Number(data.finalAmount).toLocaleString('vi-VN') + 'đ';
                    } else {
                        recalcTotal();
                    }
                }
            })
            .catch(() => {
                msgEl.style.color = '#d94f4f';
                msgEl.textContent = 'Có lỗi xảy ra, thử lại sau.';
            });
        }

        function parseMoney(text) {
            return parseInt(String(text).replace(/[^\d]/g, '')) || 0;
        }

        function recalcTotal() {
            const subtotal = parseMoney(document.getElementById('sum-subtotal').textContent);
            const discount = parseMoney(document.getElementById('sum-discount').textContent);
            const shipping = parseMoney(document.getElementById('sum-shipping').textContent);

            const total = Math.max(0, subtotal + shipping - discount);
            document.getElementById('sum-total').textContent = total.toLocaleString('vi-VN') + 'đ';
        }

        document.getElementById('voucher-input')?.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                applyVoucher();
            }
        });

        document.addEventListener('DOMContentLoaded', function () {
            const cartCount = /*[[${cartCount}]]*/ 0;
            const el1 = document.getElementById('cart-count');
            const el2 = document.getElementById('cart-sidebar-count');

            if (el1) {
                el1.textContent = cartCount;
                el1.style.display = 'inline-flex';
            }

            if (el2) {
                el2.textContent = cartCount;
            }

            recalcTotal();
        });