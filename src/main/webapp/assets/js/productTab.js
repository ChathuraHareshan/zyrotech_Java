document.getElementById("productList").addEventListener("click", async () => {
    await LoadProducts();
});

async function LoadProducts(){

    const body = document.getElementById("productTableBody");
    body.innerHTML = "";

    try {
        const response = await fetch("api/content/product");
        if (response.ok) {
            const data = await response.json();
            console.log(data);

            data.newArrivals.forEach((product) => {

                body.innerHTML += `<tr>
                             
                                <td>${product.title}</td>
                                <td><small>${product.description}</small></td>
                                <td>
                                    <span class="storage-chip">${product.storageValue}</span>
                                </td>
                                <td>
                                    <span >${product.colorValue}</span>
                                    
                                </td>
                                <td>${product.qty}</td>
                                <td>
                                    <div><span class="price-tag">Rs. ${product.price}.00</span></div>
                                </td>
                                <td><span class="category-badge">${product.categoryName}</span></td>
                                <td>
                                    <button class="btn-icon"><i class="fas fa-edit"></i></button>
                                    <button class="btn-icon"><i class="fas fa-trash"></i></button>
                                    <button class="btn-icon"><i class="fas fa-copy"></i></button>
                                </td>
                            </tr>`

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