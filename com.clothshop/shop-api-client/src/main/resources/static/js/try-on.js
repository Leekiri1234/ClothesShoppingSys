let currentSelectedProductId = null;
let currentImageFile = null;

document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const productIdParam = urlParams.get('productId');

    if (productIdParam) {
        const itemToSelect = document.querySelector(`.tryon-catalog-item[data-product-id="${productIdParam}"]`);
        if (itemToSelect) {
            selectTryOnItem(itemToSelect, productIdParam);
            setTimeout(() => itemToSelect.scrollIntoView({ behavior: 'smooth', block: 'center' }), 100);
            return;
        }
    }

    // Select the first item by default if available
    const firstItem = document.querySelector('.tryon-catalog-item');
    if (firstItem) {
        currentSelectedProductId = firstItem.getAttribute('data-product-id');
        firstItem.classList.add('selected');
    }
});

function selectTryOnItem(element, productId) {
    // Remove selected class from all
    document.querySelectorAll('.tryon-catalog-item').forEach(el => {
        el.classList.remove('selected');
    });
    // Add to clicked
    element.classList.add('selected');
    currentSelectedProductId = productId;
}

function handleTryOnUpload(event) {
    const file = event.target.files[0];
    if (file) {
        currentImageFile = file;
        const reader = new FileReader();
        reader.onload = function(e) {
            const preview = document.getElementById('user-image-preview');
            preview.src = e.target.result;
            preview.style.display = 'block';
            
            // Hide video if it was active
            document.getElementById('tryon-video').style.display = 'none';
        }
        reader.readAsDataURL(file);
    }
}

function startCamera() {
    const video = document.getElementById('tryon-video');
    const preview = document.getElementById('user-image-preview');
    
    if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
        navigator.mediaDevices.getUserMedia({ video: true })
            .then(function(stream) {
                video.srcObject = stream;
                video.style.display = 'block';
                preview.style.display = 'none';
                
                // Set a flag or mechanism to capture from video before processing
                video.setAttribute('data-active', 'true');
            })
            .catch(function(error) {
                alert("Không thể truy cập camera: " + error.message);
            });
    } else {
        alert("Trình duyệt không hỗ trợ camera.");
    }
}

function dataURItoBlob(dataURI) {
    var byteString = atob(dataURI.split(',')[1]);
    var ab = new ArrayBuffer(byteString.length);
    var ia = new Uint8Array(ab);
    for (var i = 0; i < byteString.length; i++) {
        ia[i] = byteString.charCodeAt(i);
    }
    return new Blob([ab], { type: 'image/png' });
}

async function processTryOn() {
    const video = document.getElementById('tryon-video');
    let fileToUpload = currentImageFile;

    // Check if video is active to capture image
    if (video.getAttribute('data-active') === 'true' && video.style.display !== 'none') {
        const canvas = document.getElementById('tryon-canvas');
        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;
        const context = canvas.getContext('2d');
        context.drawImage(video, 0, 0, canvas.width, canvas.height);
        
        const dataURL = canvas.toDataURL('image/png');
        fileToUpload = dataURItoBlob(dataURL);
        // stop camera
        stream = video.srcObject;
        if(stream) {
            tracks = stream.getTracks();
            tracks.forEach(function(track) { track.stop(); });
        }
        video.style.display = 'none';
        video.removeAttribute('data-active');
        
        // show preview
        const preview = document.getElementById('user-image-preview');
        preview.src = dataURL;
        preview.style.display = 'block';
    }

    if (!fileToUpload) {
        alert("Vui lòng tải ảnh lên hoặc chụp hình (chỉ chấp nhận 1 hình ảnh).");
        return;
    }
    if (!currentSelectedProductId || currentSelectedProductId === 'null' || currentSelectedProductId === 'undefined') {
        alert("Vui lòng chọn một sản phẩm từ danh sách bên trái để thử.");
        return;
    }

    // Set UI to loading state
    const submitBtn = document.getElementById('tryon-submit-btn');
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="spin"><path d="M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83"/></svg> Đang xử lý...';
    submitBtn.disabled = true;

    const tryonPreview = document.getElementById('tryon-preview');
    document.getElementById('tryon-result-placeholder').innerHTML = '<div style="font-size:12px;color:#ccc;letter-spacing:1px;margin-top:8px">Đang tạo ảnh AI... chờ chút nhé!</div>';

    var formData = new FormData();
    formData.append("image", fileToUpload, "user-image.png");
    formData.append("productId", currentSelectedProductId);

    try {
        const response = await fetch('/try-on/generate', {
            method: 'POST',
            headers: {
                [csrfHeader]: csrfToken
            },
            body: formData
        });

        // Handle non-JSON responses (e.g., 413 from Tomcat returns HTML)
        const contentType = response.headers.get('content-type');
        if (!contentType || !contentType.includes('application/json')) {
            if (response.status === 413) {
                throw new Error('Ảnh quá lớn. Vui lòng sử dụng ảnh dưới 10MB.');
            }
            throw new Error('Lỗi server (HTTP ' + response.status + '). Vui lòng thử lại.');
        }

        const data = await response.json();
        
        if (data.success && data.imageUrl) {
            // Set result image — this replaces all children including placeholder
            tryonPreview.innerHTML = `<img src="${data.imageUrl}" alt="Kết quả thử đồ" style="width:100%;height:100%;object-fit:contain;">`;
        } else {
            const msg = data.message || 'Đã xảy ra lỗi không xác định.';
            alert('Lỗi: ' + msg);
            const ph = document.getElementById('tryon-result-placeholder');
            if (ph) ph.innerHTML = '<div style="color:#e74c3c;font-size:13px;padding:16px;text-align:center;">' + msg + '</div>';
        }

    } catch (error) {
        console.error('Try-On error:', error);
        const msg = error.message || 'Đã xảy ra lỗi kết nối. Vui lòng kiểm tra mạng và thử lại.';
        alert(msg);
        const ph = document.getElementById('tryon-result-placeholder');
        if (ph) ph.innerHTML = '<div style="color:#e74c3c;font-size:13px;padding:16px;text-align:center;">' + msg + '</div>';
    } finally {
        submitBtn.innerHTML = originalText;
        submitBtn.disabled = false;
    }
}

function saveTryOnImage() {
    const img = document.querySelector('#tryon-preview img');
    if (img) {
        const link = document.createElement('a');
        link.href = img.src;
        link.download = 'minimal-tryon-result.png';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    } else {
        alert("Vui lòng thử đồ trước khi lưu ảnh.");
    }
}

function shareTryOnImage() {
    alert("Chức năng chia sẻ đang được phát triển.");
}
