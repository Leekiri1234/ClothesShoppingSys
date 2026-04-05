  document.querySelectorAll('input[name="shippingMethod"]').forEach(function(radio) {
            radio.addEventListener('change', function() {
                const shippingEl = document.getElementById('shipping-value');
                if (this.value === 'EXPRESS') {
                    shippingEl.textContent = '30.000₫';
                } else {
                    shippingEl.textContent = 'Miễn phí';
                }
                recalcTotal();
            });
        });

        function applyVoucher() {
            const code = document.getElementById('voucher-code').value.trim();
            const msgEl = document.getElementById('voucher-message');

            if (!code) {
                msgEl.style.color = '#dc3545';
                msgEl.textContent = 'Vui lòng nhập mã giảm giá.';
                return;
            }

            const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

            const headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
            if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

            fetch('/checkout/calculate?voucherCode=' + encodeURIComponent(code), {
                method: 'POST',
                headers: headers
            })
            .then(function(res) { return res.json(); })
            .then(function(data) {
                if (data.error) {
                    msgEl.style.color = '#dc3545';
                    msgEl.textContent = data.error;
                    document.getElementById('discount-row').style.display = 'none';
                    document.getElementById('voucherCodeHidden').value = '';
                    recalcTotal();
                } else {
                    msgEl.style.color = '#27ae60';
                    msgEl.textContent = data.voucherMessage || 'Áp dụng mã giảm giá thành công!';
                    document.getElementById('voucherCodeHidden').value = data.voucherCode || code;

                    if (data.discount != null && data.discount > 0) {
                        document.getElementById('discount-row').style.display = 'flex';
                        document.getElementById('discount-value').textContent =
                            '-' + formatVND(data.discount);
                    } else {
                        document.getElementById('discount-row').style.display = 'none';
                        document.getElementById('discount-value').textContent = '-0₫';
                    }

                    if (data.totalAmount != null) {
                        document.getElementById('subtotal-value').textContent = formatVND(data.totalAmount);
                    }

                    if (data.finalAmount != null) {
                        document.getElementById('total-value').textContent = formatVND(data.finalAmount);
                    } else {
                        recalcTotal();
                    }
                }
            })
            .catch(function() {
                msgEl.style.color = '#dc3545';
                msgEl.textContent = 'Có lỗi xảy ra, vui lòng thử lại.';
            });
        }

        function formatVND(amount) {
            return Number(amount).toLocaleString('vi-VN') + '₫';
        }

        function parseMoney(text) {
            return parseInt(String(text).replace(/[^\d]/g, '')) || 0;
        }

        function recalcTotal() {
            const shippingText = document.getElementById('shipping-value').textContent;
            const shippingFee = shippingText === 'Miễn phí' ? 0 : 30000;

            const subtotal = parseMoney(document.getElementById('subtotal-value').textContent);
            const discount = parseMoney(document.getElementById('discount-value').textContent);

            const total = subtotal + shippingFee - discount;
            document.getElementById('total-value').textContent = formatVND(total);
        }

        document.getElementById('voucher-code')?.addEventListener('keydown', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                applyVoucher();
            }
        });

        document.addEventListener('DOMContentLoaded', function() {
            recalcTotal();
        });