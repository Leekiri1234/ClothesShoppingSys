/**
 * Tương tác JS cho trang Voucher.
 */

function copyVoucherCode(buttonElement) {
    // Get code from data attribute
    const code = buttonElement.getAttribute('data-code');
    if (!code) return;

    // Lấy nội dung chữ cũ để sau có thể restore
    const originalText = "Sao chép mã";

    // Use Clipboard API if available
    if (navigator.clipboard) {
        navigator.clipboard.writeText(code).then(() => {
            handleCopySuccess(buttonElement, originalText);
        }).catch(err => {
            console.error("Failed to copy code: ", err);
            handleCopyError(buttonElement, originalText);
        });
    } else {
        // Fallback for older browsers
        const textarea = document.createElement("textarea");
        textarea.value = code;
        textarea.style.position = "fixed";  // Prevent scrolling to bottom
        document.body.appendChild(textarea);
        textarea.select();
        try {
            document.execCommand("copy");
            handleCopySuccess(buttonElement, originalText);
        } catch (ex) {
            console.error("Failed to copy code: ", ex);
            handleCopyError(buttonElement, originalText);
        } finally {
            document.body.removeChild(textarea);
        }
    }
}

function handleCopySuccess(buttonElement, originalText) {
    buttonElement.innerText = "Đã sao chép!";
    buttonElement.classList.add("copied");

    // Revert after 1.5 seconds
    setTimeout(() => {
        buttonElement.innerText = originalText;
        buttonElement.classList.remove("copied");
    }, 1500);
}

function handleCopyError(buttonElement, originalText) {
    buttonElement.innerText = "Lỗi!";
    setTimeout(() => {
        buttonElement.innerText = originalText;
    }, 1500);
}
