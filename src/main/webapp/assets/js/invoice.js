const params = new URLSearchParams(window.location.search);
const orderId = params.get("orderId");
window.addEventListener("load", async () => {
    if (orderId) {
        await loadInvoiceData(orderId);
    }
})

async function loadInvoiceData(orderId) {
    try {
        Notiflix.Loading.pulse("Wait...", {
            clickToClose: false,
            svgColor: '#0284c7'
        });

        const response = await fetch(`api/invoices/user-invoice?orderId=${orderId}`);
        if (response.ok) {
            const data = await response.json();
            if (data.status) {
                console.log(data);
                const invoice = data.invoiceData;

                let itemBody = document.getElementById("item-tbody");
                let subtotal = 0;
                invoice.invoiceItemDTOList.forEach((item, index) => {
                    let totalItemPrice = item.itemPrice * item.itemQty;
                    itemBody.innerHTML += `<tr>
                        <th scope="row">${index + 1}</th>
                        <td>${item.itemName}</td>
                        <td class="text-center">${item.itemQty}</td>
                        <td class="text-end">Rs. ${new Intl.NumberFormat("en-US", {
                        minimumFractionDigits: 2
                    }).format(item.itemPrice)}</td>
                        <td class="text-end">Rs. ${new Intl.NumberFormat("en-US", {
                        minimumFractionDigits: 2
                    }).format(totalItemPrice)}</td>
                    </tr>`;
                    subtotal += totalItemPrice;
                });
                document.getElementById("subtotal").innerHTML = new Intl.NumberFormat("en-US", {
                    minimumFractionDigits: 2
                }).format(subtotal);



            } else {
                Notiflix.Notify.failure(data.message, {
                    position: 'center-top'
                });
            }
        } else {
            Notiflix.Notify.failure("Invoice Data loading failed!", {
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