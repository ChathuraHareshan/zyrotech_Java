
async function addToCart(stockId, qty) {
    try {
        Notiflix.Loading.pulse("Adding to cart...", {
            clickToClose: false,
            svgColor: '#c8102e'
        });

        if (!stockId || stockId <= 0) {
            Notiflix.Notify.failure("Invalid product!", {
                position: 'center-top'
            });
            Notiflix.Loading.remove();
            return;
        }

        if (!qty || qty < 1) {
            Notiflix.Notify.failure("Please select a valid quantity!", {
                position: 'center-top'
            });
            Notiflix.Loading.remove();
            return;
        }

        const response = await fetch(`api/carts/add-to-cart?sId=${stockId}&qty=${qty}`);

        if (response.ok) {
            const data = await response.json();
            if (data.status) {
                Notiflix.Notify.success(data.message, {
                    position: 'center-top',
                    timeout: 3000
                });
                // await loadCartItems();
            } else {
                Notiflix.Notify.failure(data.message || "Add to cart process failed!", {
                    position: 'center-top',
                    timeout: 3000
                });
            }
        } else {
            const errorData = await response.json();
            Notiflix.Notify.failure(errorData.message || "Server error occurred!", {
                position: 'center-top',
                timeout: 3000
            });
        }
    } catch (e) {
        console.error("Error adding to cart:", e);
        Notiflix.Notify.failure("An error occurred while adding to cart!", {
            position: 'center-top',
            timeout: 3000
        });
    } finally {
        Notiflix.Loading.remove();
    }
}

async function loadCartItems() {
    try {
        Notiflix.Loading.pulse("Loading cart...", {
            clickToClose: false,
            svgColor: '#c8102e'
        });

        const response = await fetch("api/carts/all-carts");
        if (response.ok) {
            const data = await response.json();
            if (data.status) {
                renderingMainPanel(data.cartItems);
            } else {
                Notiflix.Notify.info(data.message, {
                    position: 'center-top'
                });
                renderingMainPanel([]);
            }
        } else {
            Notiflix.Notify.failure("Cart items loading failed!", {
                position: 'center-top'
            });
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message, {
            position: 'center-top'
        });
    } finally {
        Notiflix.Loading.remove();
    }
}

function renderingMainPanel(cartItems) {
    const cartContainer = document.getElementById("cart-item");
    const cartItemsCount = document.querySelector('.text-sm.font-medium.text-gray-500.bg-gray-100');

    if (!cartItems || cartItems.length === 0) {
        cartContainer.innerHTML = `
            <div class="text-center py-12 bg-white rounded-2xl card-shadow border border-red-50">
                <i class="fas fa-shopping-cart text-6xl text-gray-300 mb-4"></i>
                <h3 class="text-xl font-semibold text-gray-600">Your cart is empty</h3>
                <p class="text-gray-400 mt-2">Start shopping to add items to your cart</p>
                <a href="index.html" class="inline-block mt-4 bg-red-primary text-white px-6 py-2 rounded-full hover:bg-red-light transition">Browse Products</a>
            </div>
        `;
        if (cartItemsCount) {
            cartItemsCount.textContent = '0 items';
        }
        document.getElementById("qtyText").innerHTML = 'Subtotal (0 items)';
        document.getElementById("order-subtotal").innerHTML = '0.00';
        document.getElementById("order-total").innerHTML = '0.00';
        return;
    }

    if (cartItemsCount) {
        cartItemsCount.textContent = `${cartItems.length} items`;
    }

    cartContainer.innerHTML = '';
    let total = 0;
    let totalQty = 0;

    cartItems.forEach((cart) => {
        let itemTotal = parseFloat(cart.price) * parseInt(cart.qty);
        total += itemTotal;
        totalQty += parseInt(cart.qty);

        const imageUrl = cart.images && cart.images.length > 0 ? cart.images[0] : 'https://picsum.photos/seed/default/200/200';

        cartContainer.innerHTML += `
            <div class="cart-item bg-white rounded-2xl card-shadow border border-red-50 p-4 flex flex-wrap md:flex-nowrap gap-4 items-center" data-cart-id="${cart.cartId}" data-stock-id="${cart.stockId}">
                <img src="${imageUrl}" alt="${cart.productTitle}" class="w-24 h-24 rounded-xl object-cover bg-red-soft">
                <div class="flex-1 min-w-[160px]">
                    <div class="flex flex-wrap items-start justify-between gap-2">
                        <div>
                            <h3 class="font-bold text-gray-800">${cart.productTitle}</h3>
                        </div>
                        <span class="font-bold text-red-primary text-lg">Rs.${parseFloat(cart.price).toFixed(2)}</span>
                    </div>
                    <div class="flex flex-wrap items-center gap-4 mt-2">
                        <div class="flex items-center gap-2">
                            <span class="text-sm text-gray-500">Qty</span>
                            <button class="qty-btn" onclick="updateQty(this, -1, ${cart.cartId}, ${cart.stockId})">−</button>
                            <span class="qty-value w-6 text-center font-semibold">${cart.qty}</span>
                            <button class="qty-btn" onclick="updateQty(this, 1, ${cart.cartId}, ${cart.stockId})">+</button>
                        </div>
                        <button class="remove-btn text-sm" onclick="removeCartItem(${cart.cartId})">
                            <i class="far fa-trash-alt"></i> Remove
                        </button>
                    </div>
                </div>
            </div>
        `;
    });

    document.getElementById("qtyText").innerHTML = `Subtotal (${totalQty} items)`;
    document.getElementById("order-subtotal").innerHTML = total.toFixed(2);
    document.getElementById("order-total").innerHTML = (total + 500).toFixed(2);
}

async function updateQty(btn, delta, cartId, stockId) {
    const parent = btn.closest('.flex.items-center.gap-2');
    const qtySpan = parent.querySelector('.qty-value');
    let currentQty = parseInt(qtySpan.textContent, 10);
    let newQty = currentQty + delta;

    if (newQty < 1) {
        Notiflix.Notify.warning("Quantity cannot be less than 1", {
            position: 'center-top'
        });
        return;
    }

    try {
        Notiflix.Loading.pulse("Updating...", {
            clickToClose: false,
            svgColor: '#c8102e'
        });

        const response = await fetch(`api/carts/update-cart?cartId=${cartId}&qty=${newQty}`);

        if (response.ok) {
            const data = await response.json();
            if (data.status) {
                qtySpan.textContent = newQty;
                await loadCartItems();
                Notiflix.Notify.success("Cart updated successfully!", {
                    position: 'center-top',
                    timeout: 2000
                });
            } else {
                Notiflix.Notify.failure(data.message || "Update failed!", {
                    position: 'center-top'
                });
            }
        } else {
            Notiflix.Notify.failure("Server error occurred!", {
                position: 'center-top'
            });
        }
    } catch (e) {
        console.error("Error updating cart:", e);
        Notiflix.Notify.failure("An error occurred!", {
            position: 'center-top'
        });
    } finally {
        Notiflix.Loading.remove();
    }
}

async function removeCartItem(cartId) {
    try {
        Notiflix.Loading.pulse("Removing...", {
            clickToClose: false,
            svgColor: '#c8102e'
        });

        const response = await fetch(`api/carts/delete-cart?cartId=${cartId}`, {
            method: "DELETE"
        });

        if (response.ok) {
            const data = await response.json();
            if (data.status) {
                await loadCartItems();
                Notiflix.Notify.success("Item removed from cart!", {
                    position: 'center-top',
                    timeout: 2000
                });
            } else {
                Notiflix.Notify.failure(data.message || "Remove failed!", {
                    position: 'center-top'
                });
            }
        } else {
            Notiflix.Notify.failure("Server error occurred!", {
                position: 'center-top'
            });
        }
    } catch (e) {
        console.error("Error removing cart item:", e);
        Notiflix.Notify.failure("An error occurred!", {
            position: 'center-top'
        });
    } finally {
        Notiflix.Loading.remove();
    }
}