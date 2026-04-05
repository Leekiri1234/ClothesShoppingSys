async function applyVoucher() {
    const code = document.getElementById('voucherInput').value;

    try {
        const response = await fetch(`/checkout/calculate?voucherCode=${code}`, {
            method: 'POST'
        });

        const data = await response.json();

        if (response.ok) {
            document.getElementById('finalTotal').innerText =
                data.totalAmount.toLocaleString('vi-VN') + '₫';
        } else {
            alert(data.error || 'Mã voucher không hợp lệ');
        }
    } catch (error) {
        alert('Có lỗi xảy ra');
    }
}