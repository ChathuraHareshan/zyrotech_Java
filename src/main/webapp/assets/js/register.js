async function register() {
    Notiflix.Loading.pulse("Creating account...", {
        clickToClose: false,
        svgColor: '#c8102e'
    });

    let fname = document.getElementById("fname");
    let lname = document.getElementById("lname");
    let email = document.getElementById("regEmail");
    let password = document.getElementById("regPassword");
    let lineOne = document.getElementById("lineOne");
    let lineTwo = document.getElementById("lineTwo");
    let mobile = document.getElementById("mobile");
    let city = document.getElementById("city");
    let postalCode = document.getElementById("postalCode");

    if (!fname.value || fname.value.trim() === "") {
        Notiflix.Notify.warning("First name is required!", { position: 'center-top' });
        Notiflix.Loading.remove();
        return;
    }
    if (!lname.value || lname.value.trim() === "") {
        Notiflix.Notify.warning("Last name is required!", { position: 'center-top' });
        Notiflix.Loading.remove();
        return;
    }
    if (!email.value || email.value.trim() === "") {
        Notiflix.Notify.warning("Email is required!", { position: 'center-top' });
        Notiflix.Loading.remove();
        return;
    }
    if (!password.value || password.value.trim() === "") {
        Notiflix.Notify.warning("Password is required!", { position: 'center-top' });
        Notiflix.Loading.remove();
        return;
    }
    if (password.value.length < 8) {
        Notiflix.Notify.warning("Password must be at least 8 characters!", { position: 'center-top' });
        Notiflix.Loading.remove();
        return;
    }
    if (!lineOne.value || lineOne.value.trim() === "") {
        Notiflix.Notify.warning("Address line one is required!", { position: 'center-top' });
        Notiflix.Loading.remove();
        return;
    }
    if (!mobile.value || mobile.value.trim() === "") {
        Notiflix.Notify.warning("Mobile number is required!", { position: 'center-top' });
        Notiflix.Loading.remove();
        return;
    }
    if (!city.value || city.value === "0") {
        Notiflix.Notify.warning("Please select a city!", { position: 'center-top' });
        Notiflix.Loading.remove();
        return;
    }
    if (!postalCode.value || postalCode.value.trim() === "") {
        Notiflix.Notify.warning("Postal code is required!", { position: 'center-top' });
        Notiflix.Loading.remove();
        return;
    }

    const user = {
        fname: fname.value.trim(),
        lname: lname.value.trim(),
        email: email.value.trim(),
        password: password.value,
        lineOne: lineOne.value.trim(),
        lineTwo: lineTwo.value.trim(),
        mobile: mobile.value.trim(),
        cityId: parseInt(city.value),
        postalCode: postalCode.value.trim()
    };

    try {
        const response = await fetch("api/users", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(user)
        });

        if (response.ok) {
            const data = await response.json();
            if (data.status) {
                Notiflix.Report.success(
                    'Electro',
                    data.message,
                    'Okay',
                    () => {
                        window.location.href = "Verify.html?email=" + encodeURIComponent(email.value);
                    }
                );
            } else {
                Notiflix.Notify.failure(data.message, {
                    position: 'center-top'
                });
            }
        } else {
            const errorData = await response.json();
            Notiflix.Notify.failure(errorData.message || 'Registration failed!', {
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

async function loadCitiesForRegistration() {
    try {
        const response = await fetch("api/content/cities");
        if (response.ok) {
            const data = await response.json();
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

document.addEventListener("DOMContentLoaded", function() {
    loadCitiesForRegistration();
});