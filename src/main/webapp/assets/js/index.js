window.addEventListener("load", async () => {

    try {

        LoadProducts();


    }catch (error) {
        console.error(error);
    }

});

async function LoadProducts() {

    const body = document.getElementById("productBody");
    body.innerHTML = "";

    try {
        const response = await fetch("api/content/product");
        if (response.ok) {
            const data = await response.json();
            console.log(data);

            data.newArrivals.forEach((product) => {

                body.innerHTML += `<a href="singleProductView.html?id=${product.productId}">
<div class="product-card bg-white rounded-xl overflow-hidden card-shadow border border-red-50 transition hover:shadow-lg">
            <img src="${product.images[0]}" alt="product" class="product-img w-full">
            <div class="p-3"><h3 class="font-semibold text-sm truncate">${product.title}</h3>
                <p class="text-red-primary font-bold text-sm">Rs.${product.price}</p>
                <div class="flex justify-between items-center mt-2">
                    <div class="flex gap-2">
                        <button class="action-icon"><i class="fas fa-cart-plus"></i></button>
                        <button class="action-icon"><i class="far fa-heart"></i></button>
                    </div>
                    <span class="text-xs bg-red-soft px-2 py-0.5 rounded-full text-red-primary">${product.categoryName}</span></div>
            </div>
        </div></a>`

            });


        } else {
            Notiflix.Notify.failure("Product loading failed!", {
                position: 'center-top'
            });
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message, {
            position: 'center-top'
        });
    }

}