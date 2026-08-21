window.addEventListener("load", async () => {
    Notiflix.Loading.pulse("Data is loading", {
        clickToClose: false,
        svgColor: '#0284c7'
    });
    try {



    } finally {
        Notiflix.Loading.remove();
    }
});



document.getElementById("addProductBtn").addEventListener("click", async () => {
    await loadColorStorage();
});

async function loadColorStorage() {
    Notiflix.Loading.pulse("Wait...", {
        clickToClose: false,
        svgColor: '#0284c7'
    });

    try {
        const response = await fetch("api/content/colorStorage");
        if (response.ok) {
            const data = await response.json();
            console.log(data);

            const colorSelect = document.getElementById("color");
            const storageSelect = document.getElementById("storage");
            const categorySelect = document.getElementById("category");

            colorSelect.innerHTML = `<option value="0">Select a Color</option>`;
            storageSelect.innerHTML = `<option value="0">Select a Storage</option>`;
            categorySelect.innerHTML = `<option value="0">Select a Category</option>`;


            data.colors.forEach((color) => {
                const option = document.createElement("option");
                option.value = color.id;
                option.textContent = color.value;
                colorSelect.appendChild(option);
            });

            data.storage.forEach((storage) => {
                const option = document.createElement("option");
                option.value = storage.id;
                option.textContent = storage.value;
                storageSelect.appendChild(option);
            });

            data.category.forEach((category) => {
                const option = document.createElement("option");
                option.value = category.id;
                option.textContent = category.name;
                categorySelect.appendChild(option);
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

function resetAddProductForm() {
    document.getElementById("title").value = "";
    document.getElementById("description").value = "";

    document.getElementById("category").value = "0";
    document.getElementById("color").value = "0";
    document.getElementById("storage").value = "0";

    document.getElementById("qty").value = "";
    document.getElementById("price").value = "";

    document.getElementById("img1").value = "";
    document.getElementById("img2").value = "";
    document.getElementById("img3").value = "";

    removeImagePreviews();


}

function removeImagePreviews() {
    const previewImages = document.querySelectorAll('.image-preview');
    previewImages.forEach(img => {
        img.src = '';
        img.style.display = 'none';
    });

    const previewContainers = document.querySelectorAll('.preview-container');
    previewContainers.forEach(container => {
        container.innerHTML = '';
    });
}


async function addProduct(){
    Notiflix.Loading.pulse("Wait...", {
        clickToClose: false,
        svgColor: '#0284c7'
    });

    const title = document.getElementById("title");
    const description = document.getElementById("description");
    const category = document.getElementById("category");
    const qty = document.getElementById("qty");
    const color = document.getElementById("color");
    const storage = document.getElementById("storage");
    const price = document.getElementById("price");

    const productDataObj = {
        categoryId: category.value,
        title: title.value,
        description: description.value,
        storageId: storage.value,
        colorId: color.value,
        price: parseFloat(price.value),
        qty: parseInt(qty.value)
    };

    const formData = new FormData();
    formData.append("product", JSON.stringify(productDataObj));

    try {
        const response = await fetch("api/products/save-product", {
            method: "POST",
            body: formData
        });
        if (response.ok) {
            const data = await response.json();
            if (data.status) {
                await uploadProductImages(data.productId);
            } else {
                Notiflix.Notify.failure(data.message, {
                    position: 'center-top'
                });
                Notiflix.Loading.remove();
            }
        } else {
            Notiflix.Notify.failure("Product details adding failed!", {
                position: 'center-top'
            });
            Notiflix.Loading.remove();
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message, {
            position: 'center-top'
        });
        Notiflix.Loading.remove();
    }
}

async function uploadProductImages(productId) {
    let img1 = document.getElementById("img1");
    let img2 = document.getElementById("img2");
    let img3 = document.getElementById("img3");

    const formData = new FormData();
    let hasImage = false;

    if (img1.files && img1.files.length > 0 && img1.files[0]) {
        formData.append("images[]", img1.files[0]);
        hasImage = true;
    }
    if (img2.files && img2.files.length > 0 && img2.files[0]) {
        formData.append("images[]", img2.files[0]);
        hasImage = true;
    }
    if (img3.files && img3.files.length > 0 && img3.files[0]) {
        formData.append("images[]", img3.files[0]);
        hasImage = true;
    }

    if (!hasImage) {
        Notiflix.Notify.warning("Please upload at least one product image!", {
            position: 'center-top'
        });
        Notiflix.Loading.remove();
        return;
    }

    try {
        const response = await fetch(`api/products/${productId}/upload-images`, {
            method: "PUT",
            body: formData
        });

        if (response.ok) {
            const data = await response.json();
            if (data.status) {
                Notiflix.Report.success(
                    'Electro',
                    data.message,
                    'Okay',
                    function() {
                        resetAddProductForm();
                    }
                );
            } else {
                Notiflix.Notify.failure(data.message, {
                    position: 'center-top'
                });
            }
        } else {
            Notiflix.Notify.failure("Product images uploading failed!", {
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