window.addEventListener("load", async () => {
    await loadCities();
    await loadCheckoutData();
});

async function loadCheckoutData() {
    try {
        Notiflix.Loading.pulse("Loading checkout data...", {
            clickToClose: false,
            svgColor: '#c8102e'
        });

        const response = await fetch("api/checkouts/user-checkout-data");

        if (response.ok) {
            const data = await response.json();
            console.log(data);
            if (data.status) {
                renderCheckoutData(data);
                Notiflix.Notify.success(data.message, {
                    position: 'center-top',
                    timeout: 2000
                });
            } else {
                Notiflix.Notify.info(data.message, {
                    position: 'center-top',
                    timeout: 3000
                });
                if (data.message && (data.message.includes("empty") || data.message.includes("login"))) {
                    if (data.message.includes("login")) {
                        window.location.href = "login.html";
                    } else {
                        window.location.href = "cart.html";
                    }
                }
            }
        } else if (response.status === 401 || response.status === 403) {
            Notiflix.Notify.warning("Please login to continue!", {
                position: 'center-top'
            });
            setTimeout(() => {
                window.location.href = "login.html";
            }, 1500);
        } else {
            Notiflix.Notify.failure("Failed to load checkout data!", {
                position: 'center-top'
            });
        }
    } catch (e) {
        console.error("Error loading checkout data:", e);
        Notiflix.Notify.failure("An error occurred!", {
            position: 'center-top'
        });
    } finally {
        Notiflix.Loading.remove();
    }
}

function renderCheckoutData(data) {
    const userPrimaryAddress = data.userPrimaryAddress;
    const cartList = data.cartList;

    if (userPrimaryAddress) {
        document.getElementById("firstName").value = userPrimaryAddress.firstName || '';
        document.getElementById("lastName").value = userPrimaryAddress.lastName || '';
        document.getElementById("lineOne").value = userPrimaryAddress.lineOne || '';
        document.getElementById("lineTwo").value = userPrimaryAddress.lineTwo || '';
        document.getElementById("postalCode").value = userPrimaryAddress.postalCode || '';
        document.getElementById("mobile").value = userPrimaryAddress.mobile || '';
        document.getElementById('city').value = userPrimaryAddress.city || '';

        // Set city after cities are loaded
        if (userPrimaryAddress.cityDTO && userPrimaryAddress.cityDTO.id) {
            setTimeout(() => {
                const citySelect = document.getElementById("city");
                if (citySelect) {
                    citySelect.value = userPrimaryAddress.cityDTO.id;
                }
            }, 500);
        }

        document.getElementById("useCurrentAddress").checked = true;
        toggleAddressFields(true);
        document.getElementById("saveAddressWrapper").style.display = 'none';
    } else {
        document.getElementById("useCurrentAddress").checked = false;
        toggleAddressFields(false);
        document.getElementById("saveAddressWrapper").style.display = 'flex';
    }

    if (cartList && cartList.length > 0) {
        renderOrderItems(cartList);
        calculateTotal(cartList);
    }

    document.getElementById("cartCount").textContent = cartList ? cartList.length : 0;
}

function renderOrderItems(cartItems) {
    const container = document.getElementById("orderItems");
    container.innerHTML = '';
    let totalItems = 0;

    if (!cartItems || cartItems.length === 0) {
        container.innerHTML = '<div class="text-center py-4 text-gray-500">No items in cart</div>';
        return;
    }

    cartItems.forEach((item, index) => {
        const imageUrl = item.images && item.images.length > 0 ? item.images[0] : 'https://picsum.photos/seed/cart' + index + '/100/100';
        const itemTotal = item.price * item.qty;
        totalItems += item.qty;

        container.innerHTML += `
            <div class="order-item">
                <img src="${imageUrl}" alt="${item.productTitle}">
                <div class="flex-1">
                    <h4 class="font-semibold text-sm">${item.productTitle}</h4>
                    <span class="text-xs text-gray-500">Qty: ${item.qty}</span>
                    <p class="text-red-primary font-bold text-sm">Rs.${item.price.toFixed(2)}</p>
                </div>
                <span class="text-sm font-semibold text-gray-700">Rs.${itemTotal.toFixed(2)}</span>
            </div>
        `;
    });

    document.getElementById("totalItems").textContent = totalItems;
}

function calculateTotal(cartItems) {
    let subtotal = 0;
    let totalItems = 0;

    cartItems.forEach(item => {
        subtotal += item.price * item.qty;
        totalItems += item.qty;
    });

    const shipping = 500;
    const total = subtotal + shipping;

    document.getElementById("orderSubtotal").textContent = subtotal.toFixed(2);
    document.getElementById("totalItems").textContent = totalItems;
    document.getElementById("orderTotal").textContent = total.toFixed(2);
    document.getElementById("buyNowTotal").textContent = total.toFixed(2);
    document.getElementById("orderShipping").textContent = shipping.toFixed(2);
}

async function loadCities() {
    try {
        const response = await fetch("api/content/cities");
        if (response.ok) {
            const data = await response.json();
            console.log("Cities response:", data);
            if (data.status && data.cities) {
                const citySelect = document.getElementById("city");
                if (citySelect) {
                    citySelect.innerHTML = '<option value="0">Select a City</option>';
                    data.cities.forEach(city => {
                        const option = document.createElement("option");
                        option.value = city.id;
                        option.textContent = city.name;
                        citySelect.appendChild(option);
                    });
                    console.log("Cities loaded successfully:", data.cities.length);
                }
            } else {
                console.warn("No cities data received:", data);
            }
        } else {
            console.error("Failed to load cities:", response.status);
        }
    } catch (e) {
        console.error("Error loading cities:", e);
    }
}

function toggleAddressFields(useCurrent) {
    const fields = document.querySelectorAll('.address-field');
    fields.forEach(field => {
        if (useCurrent) {
            field.disabled = true;
            field.classList.add('bg-gray-100');
        } else {
            field.disabled = false;
            field.classList.remove('bg-gray-100');
        }
    });
}

document.getElementById("useCurrentAddress").addEventListener("change", function() {
    const isChecked = this.checked;
    toggleAddressFields(isChecked);
    if (isChecked) {
        document.getElementById("saveAddressWrapper").style.display = 'none';
    } else {
        document.getElementById("saveAddressWrapper").style.display = 'flex';
    }
});

async function processCheckout() {

    let firstName = document.getElementById("firstName");
    let lastName = document.getElementById("lastName");
    let city = document.getElementById("city");
    let lineOne = document.getElementById("lineOne");
    let lineTwo = document.getElementById("lineTwo");
    let postalCode = document.getElementById("postalCode");
    let mobile = document.getElementById("mobile");
    let useCurrentAddress = document.getElementById("useCurrentAddress");

    const checkoutData = {
        useCurrentAddress : useCurrentAddress.checked,
        firstName: firstName.value,
        lastName: lastName.value,
        city: parseInt(city.value) || 0,
        lineOne: lineOne.value,
        lineTwo: lineTwo.value,
        postalCode: postalCode.value,
        mobile: mobile.value,
    }
    const checkoutDataJSON = JSON.stringify(checkoutData);

    try{
        Notiflix.Loading.pulse("Wait...", {
            clickToClose: false,
            svgColor: '#0284c7'
        });

        const response = await fetch("api/checkouts/user-checkout", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: checkoutDataJSON
        });
        if (response.ok) {
            const data = await response.json();
            if (data.status) {
                // console.log(data);
                payhere.startPayment(data.paymentDetails);
            } else {
                Notiflix.Notify.failure(data.message, {
                    position: 'center-top'
                });
            }
        } else {
            Notiflix.Notify.failure("Checkout process failed!", {
                position: 'center-top'
            });
        }

    }catch (e) {
        Notiflix.Notify.failure(e.message, {
            position: 'center-top'
        });
    } finally {
        Notiflix.Loading.remove();
    }

}

function toggleDropdown() {
    const dropdown = document.getElementById("profileDropdown");
    dropdown.classList.toggle("show");
}

document.addEventListener("click", function(event) {
    const dropdown = document.getElementById("profileDropdown");
    const trigger = document.querySelector(".profile-trigger");
    if (!trigger.contains(event.target) && !dropdown.contains(event.target)) {
        dropdown.classList.remove("show");
    }
});

payhere.onCompleted = async function onCompleted(orderId) {
    console.log("Payment completed. OrderID:" + orderId);
    // Note: validate the payment and show success or failure page to the customer
    await verifyOrder(orderId);
};

// Payment window closed
payhere.onDismissed = function onDismissed() {
    // Note: Prompt user to pay again or show an error page
    console.log("Payment dismissed");
};

// Error occurred
payhere.onError = function onError(error) {
    // Note: show an error page
    console.log("Error:" + error);
};

async function verifyOrder(orderId) {
    try {
        const response = await fetch(`api/orders/verify-order?orderId=${orderId}`);
        if (response.ok) {
            const data = await response.json();
            if (data.status) {
                window.location = `invoice.html?orderId=${orderId}`;
            } else {
                // redirect to failed page
            }

        } else {
            Notiflix.Notify.failure("Order verifying failed!", {
                position: 'center-top'
            });
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message, {
            position: 'center-top'
        });
    }
}