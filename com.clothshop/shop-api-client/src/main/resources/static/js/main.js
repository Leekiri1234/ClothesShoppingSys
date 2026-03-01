/**
 * ClothShop Client - Cart Interactions
 * Minimal inline JS for better performance
 */

// DOM Ready
document.addEventListener('DOMContentLoaded', function() {
    initializeCartFunctions();
    initializeSearchBar();
    initializeFormValidation();
});

/**
 * Initialize cart-related functions
 */
function initializeCartFunctions() {
    const addToCartButtons = document.querySelectorAll('.add-to-cart-btn');

    addToCartButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();

            const variantId = this.getAttribute('data-variant-id');
            const quantity = document.getElementById('quantity')?.value || 1;

            addToCart(variantId, quantity);
        });
    });
}

/**
 * Add item to cart via AJAX
 */
function addToCart(variantId, quantity) {
    // Get CSRF token from meta tag or form
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content')
                   || document.querySelector('input[name="_csrf"]')?.value;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    fetch('/cart/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify({
            variantId: variantId,
            quantity: quantity
        })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showNotification('Product added to cart!', 'success');
            updateCartCount();
        } else {
            showNotification(data.message || 'Failed to add product', 'error');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        showNotification('An error occurred', 'error');
    });
}

/**
 * Update cart item count in navigation
 */
function updateCartCount() {
    fetch('/cart/count')
        .then(response => response.json())
        .then(data => {
            const cartBadge = document.querySelector('.cart-count-badge');
            if (cartBadge) {
                cartBadge.textContent = data.count;
                cartBadge.style.display = data.count > 0 ? 'inline-block' : 'none';
            }
        })
        .catch(error => console.error('Error updating cart count:', error));
}

/**
 * Initialize search bar with debounce
 */
function initializeSearchBar() {
    const searchInput = document.getElementById('search-input');

    if (searchInput) {
        let debounceTimer;

        searchInput.addEventListener('input', function() {
            clearTimeout(debounceTimer);

            debounceTimer = setTimeout(() => {
                const query = this.value.trim();

                if (query.length >= 3) {
                    performSearch(query);
                }
            }, 300);
        });
    }
}

/**
 * Perform product search
 */
function performSearch(query) {
    // Redirect to search results page
    window.location.href = `/products?search=${encodeURIComponent(query)}`;
}

/**
 * Initialize form validation
 */
function initializeFormValidation() {
    const forms = document.querySelectorAll('.needs-validation');

    forms.forEach(form => {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }

            form.classList.add('was-validated');
        });
    });

    // Password confirmation validation
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const passwordInput = document.getElementById('password');

    if (confirmPasswordInput && passwordInput) {
        confirmPasswordInput.addEventListener('input', function() {
            if (this.value !== passwordInput.value) {
                this.setCustomValidity('Passwords do not match');
            } else {
                this.setCustomValidity('');
            }
        });
    }
}

/**
 * Show notification toast
 */
function showNotification(message, type = 'info') {
    // Create toast element
    const toast = document.createElement('div');
    toast.className = `alert alert-${type === 'success' ? 'success' : 'danger'} position-fixed top-0 end-0 m-3`;
    toast.style.zIndex = '9999';
    toast.innerHTML = `
        <i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i>
        ${message}
    `;

    document.body.appendChild(toast);

    // Auto remove after 3 seconds
    setTimeout(() => {
        toast.remove();
    }, 3000);
}

/**
 * Quantity input handlers
 */
function incrementQuantity() {
    const input = document.getElementById('quantity');
    if (input) {
        const currentValue = parseInt(input.value) || 1;
        const max = parseInt(input.max) || 999;

        if (currentValue < max) {
            input.value = currentValue + 1;
        }
    }
}

function decrementQuantity() {
    const input = document.getElementById('quantity');
    if (input) {
        const currentValue = parseInt(input.value) || 1;
        const min = parseInt(input.min) || 1;

        if (currentValue > min) {
            input.value = currentValue - 1;
        }
    }
}

/**
 * Image gallery for product details
 */
function changeProductImage(imageUrl) {
    const mainImage = document.getElementById('main-product-image');
    if (mainImage) {
        mainImage.src = imageUrl;
    }
}
