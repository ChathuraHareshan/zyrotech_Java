async function sendOTP() {
    Notiflix.Loading.pulse("Wait...", {
        clickToClose: false,
        svgColor: '#0284c7'
    });


    let email = document.getElementById("email");

    const admin = {
        email: email.value,
    }

    try {
        const response = await fetch("api/admin", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(admin)
        });


        if (response.ok) {
            const data = await response.json();
            if (data.status) {


                Notiflix.Notify.success(data.message, {
                    position: 'center-top'
                });




            } else {
                Notiflix.Notify.failure(data.message,{
                    position:'center-top'
                });
            }
        } else {
            Notiflix.Notify.failure('Something went wrong. Please check your credentials',{
                position:'center-top'
            });
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message,{
            position:'topRight'
        });
    }finally {
        Notiflix.Loading.remove(1000);
    }
}


async function verifyOTP(){

    Notiflix.Loading.pulse("Wait...", {
        clickToClose: false,
        svgColor: '#0284c7'
    });


    let email = document.getElementById("email");
    let otp = document.getElementById("otp");

    const admin = {
        email: email.value,
        verificationCode: otp.value
    }

    try {
        const response = await fetch("api/admin/verify", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(admin)
        });


        if (response.ok) { // 200
            const data = await response.json();
            if (data.status) {
                Notiflix.Report.success(
                    'OpenBay',
                    data.message,
                    'Okay', // button title
                    () => {
                        window.location = "dashboard.html"
                    },
                );

            } else {
                Notiflix.Notify.failure(data.message,{
                    position:'center-top'
                });
            }
        } else {
            Notiflix.Notify.failure('Something went wrong. Please check your credentials',{
                position:'center-top'
            });
        }
    } catch (e) {
        Notiflix.Notify.failure(e.message,{
            position:'center-top'
        });
    }finally {
        Notiflix.Loading.remove(1000);
    }

}