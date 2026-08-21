const params = new URLSearchParams(window.location.search);
const productId = params.get("id");

let productQty = 1;
const qtyDisplay = document.getElementById('qtyDisplay');

window.addEventListener("load", async () => {
    try {
        Notiflix.Loading.pulse("Wait...", {
            clickToClose: false,
            svgColor: '#c8102e'
        });
        await loadProduct();
    } catch (error) {
        console.error("Error loading product:", error);
    } finally {
        Notiflix.Loading.remove();
    }
});

async function loadProduct() {
    try {
        const response = await fetch(`api/single-products/product?Id=${productId}`);

        if (response.ok) {
            const data = await response.json();

            if (data.status) {
                console.log(data);
                renderProduct(data.newArrivals[0]);
            } else {
                Notiflix.Notify.failure(data.message, {
                    position: 'center-top',
                    timeout: 3000
                });
            }
        } else {
            Notiflix.Notify.failure("Single Product data loading failed!", {
                position: 'center-top',
                timeout: 3000
            });
        }
    } catch (error) {
        console.error(error);
        Notiflix.Notify.failure("Error loading product data!", {
            position: 'center-top',
            timeout: 3000
        });
    }
}

function renderProduct(product) {
    if (!product) {
        Notiflix.Notify.warning("Product not found!", {
            position: 'center-top',
            timeout: 3000
        });
        return;
    }

    const categoryBadge = document.querySelector('.text-sm.text-red-primary.font-semibold.bg-red-soft');
    if (categoryBadge) {
        categoryBadge.textContent = product.categoryName || 'Electronics';
    }

    const titleElement = document.querySelector('h1.text-2xl.md\\:text-3xl');
    if (titleElement) {
        titleElement.textContent = product.title || 'Product Name';
    }

    const skuElement = document.querySelector('.flex.items-center.gap-2.text-sm.text-gray-500 span:last-child');
    if (skuElement && product.productId) {
        skuElement.textContent = `SKU: ELEC-${product.productId}`;
    }

    const priceElement = document.querySelector('.text-3xl.font-bold.text-red-primary');
    if (priceElement && product.price) {
        priceElement.textContent = `Rs.${product.price.toFixed(2)}`;
    }

    const mainImage = document.getElementById('mainImage');
    if (mainImage && product.images && product.images.length > 0) {
        mainImage.src = product.images[0];
        mainImage.alt = product.title || 'Product image';
    }

    const thumbnailContainer = document.querySelector('.flex.gap-3.mt-4.flex-wrap');
    if (thumbnailContainer && product.images && product.images.length > 0) {
        thumbnailContainer.innerHTML = '';
        product.images.forEach((img, index) => {
            const thumb = document.createElement('img');
            thumb.src = img;
            thumb.alt = `Thumbnail ${index + 1}`;
            thumb.className = `thumbnail-img ${index === 0 ? 'active' : ''}`;
            thumb.onclick = function() { changeImage(this, img); };
            thumbnailContainer.appendChild(thumb);
        });
    }

    const colorContainer = document.querySelector('.flex.gap-2.mt-1');
    if (colorContainer && product.colorValue) {
        colorContainer.innerHTML = '';
        const colorDot = document.createElement('span');
        colorDot.className = 'w-8 h-8 rounded-full border-2 border-red-primary cursor-pointer';
        const colors = {
            'Black': '#000000',
            'White': '#FFFFFF',
            'Red': '#FF0000',
            'Blue': '#0000FF',
            'Green': '#00FF00',
            'Yellow': '#FFFF00',
            'Silver': '#C0C0C0',
            'Gold': '#FFD700',
            'Gray': '#808080',
            'Pink': '#FFC0CB'
        };
        colorDot.style.background = colors[product.colorValue] || '#CCCCCC';
        colorContainer.appendChild(colorDot);
    }

    const storageContainer = document.querySelector('.flex.gap-2.mt-1.flex-wrap');
    if (storageContainer && product.storageValue) {
        storageContainer.innerHTML = '';
        const storageBadge = document.createElement('span');
        storageBadge.className = 'px-3 py-1 border-2 border-red-primary text-red-primary rounded-full text-sm font-medium';
        storageBadge.textContent = product.storageValue;
        storageContainer.appendChild(storageBadge);
    }

    const availabilityElement = document.querySelector('.text-xs.text-gray-400');
    if (availabilityElement && product.qty !== undefined) {
        availabilityElement.textContent = `(${product.qty} available)`;
    }

    const descriptionElement = document.querySelector('.text-sm.text-gray-600.leading-relaxed');
    if (descriptionElement && product.description) {
        descriptionElement.textContent = product.description;
    }

    // Update the Add to Cart button to use the current product ID
    const addToCartBtn = document.querySelector('.bg-red-primary.text-white.px-8.py-3.rounded-full');
    if (addToCartBtn) {
        addToCartBtn.onclick = function() {
            const qty = parseInt(document.getElementById('qtyDisplay').textContent) || 1;
            addToCart(product.productId, qty);
        };
    }

    // Update the heart button
    const heartBtn = document.querySelector('.action-icon.w-12.h-12.rounded-full');
    if (heartBtn) {
        heartBtn.onclick = function() {
            addToWishlist(product.productId);
        };
    }


}



function changeImage(el, src) {
    const mainImage = document.getElementById('mainImage');
    if (mainImage) {
        mainImage.src = src;
    }
    document.querySelectorAll('.thumbnail-img').forEach(img => img.classList.remove('active'));
    el.classList.add('active');
}

function changeQty(delta) {
    const newQty = productQty + delta;
    if (newQty >= 1 && newQty <= 10) {
        productQty = newQty;
        if (qtyDisplay) {
            qtyDisplay.textContent = productQty;
        }
    }
}

function addToWishlist(productId) {
    Notiflix.Notify.success('Added to wishlist!', {
        position: 'center-top',
        timeout: 2000
    });
}