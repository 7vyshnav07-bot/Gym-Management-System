const SERVER_URL = "/GymForge";


// ==========================================
// PAYMENT PAGE
// ==========================================

document.addEventListener("DOMContentLoaded", function () {

    const storedPlan =
        localStorage.getItem("gymforgeSelectedPlan");


    // ------------------------------------------
    // NO PLAN SELECTED
    // ------------------------------------------

    if (!storedPlan) {

        document.getElementById(
            "selectedPlan"
        ).textContent =
            "No Plan Selected";

        document.getElementById(
            "membershipFee"
        ).textContent =
            "₹0";

        document.getElementById(
            "membershipDuration"
        ).textContent =
            "Not Available";

        return;
    }


    // ------------------------------------------
    // READ SELECTED PLAN
    // ------------------------------------------

    let plan;

    try {

        plan =
            JSON.parse(storedPlan);

    } catch (error) {

        console.error(
            "Invalid membership data:",
            error
        );

        return;
    }


    // ------------------------------------------
    // DISPLAY PLAN
    // ------------------------------------------

    document.getElementById(
        "selectedPlan"
    ).textContent =
        plan.name || "Unknown Plan";


    document.getElementById(
        "membershipFee"
    ).textContent =
        "₹" + (plan.price || 0);


    document.getElementById(
        "membershipDuration"
    ).textContent =
        plan.duration || "Not Available";


    // ------------------------------------------
    // PAYMENT METHOD
    // ------------------------------------------

    const paymentMethod =
        document.getElementById(
            "paymentMethod"
        );


    paymentMethod.addEventListener(
        "change",
        function () {

            const method =
                paymentMethod.value;


            const upiField =
                document.getElementById(
                    "upiField"
                );

            const cardFields =
                document.getElementById(
                    "cardFields"
                );


            upiField.style.display =
                "none";

            cardFields.style.display =
                "none";


            if (method === "UPI") {

                upiField.style.display =
                    "block";
            }


            if (method === "Card") {

                cardFields.style.display =
                    "block";
            }

        }
    );


    // ------------------------------------------
    // PAY NOW
    // ------------------------------------------

    document.getElementById(
        "payNowButton"
    ).addEventListener(
        "click",
        processPayment
    );

});


// ==========================================
// PROCESS PAYMENT
// ==========================================

async function processPayment() {

    const message =
        document.getElementById(
            "paymentMessage"
        );


    const storedPlan =
        localStorage.getItem(
            "gymforgeSelectedPlan"
        );


    if (!storedPlan) {

        message.textContent =
            "Please select a membership plan first.";

        message.style.color =
            "#c62828";

        return;
    }


    let plan;

    try {

        plan =
            JSON.parse(storedPlan);

    } catch (error) {

        message.textContent =
            "Invalid membership information.";

        message.style.color =
            "#c62828";

        return;
    }


    // ------------------------------------------
    // PAYMENT METHOD
    // ------------------------------------------

    const paymentMethod =
        document.getElementById(
            "paymentMethod"
        ).value;


    if (!paymentMethod) {

        message.textContent =
            "Please select a payment method.";

        message.style.color =
            "#c62828";

        return;
    }


    // ------------------------------------------
    // UPI
    // ------------------------------------------

    if (paymentMethod === "UPI") {

        const upiId =
            document.getElementById(
                "upiId"
            ).value.trim();


        if (!upiId) {

            message.textContent =
                "Please enter your UPI ID.";

            message.style.color =
                "#c62828";

            return;
        }
    }


    // ------------------------------------------
    // CARD
    // ------------------------------------------

    if (paymentMethod === "Card") {

        const cardNumber =
            document.getElementById(
                "cardNumber"
            ).value.trim();

        const cardName =
            document.getElementById(
                "cardName"
            ).value.trim();

        const expiryDate =
            document.getElementById(
                "expiryDate"
            ).value.trim();

        const cvv =
            document.getElementById(
                "cvv"
            ).value.trim();


        if (
            !cardNumber ||
            !cardName ||
            !expiryDate ||
            !cvv
        ) {

            message.textContent =
                "Please fill in all card details.";

            message.style.color =
                "#c62828";

            return;
        }
    }


    // ------------------------------------------
    // TRANSACTION ID
    // ------------------------------------------

    const transactionId =
        document.getElementById(
            "transactionId"
        ).value.trim();


    if (!transactionId) {

        message.textContent =
            "Please enter the transaction/reference ID.";

        message.style.color =
            "#c62828";

        return;
    }


    // ------------------------------------------
    // DISABLE BUTTON
    // ------------------------------------------

    const payButton =
        document.getElementById(
            "payNowButton"
        );

    payButton.disabled =
        true;

    payButton.textContent =
        "Processing...";


    // ------------------------------------------
    // SEND PAYMENT TO JAVA
    // ------------------------------------------

    try {

        const response =
            await fetch(
                SERVER_URL + "/payment",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({

                        planName:
                            plan.name,

                        price:
                            String(plan.price),

                        duration:
                            plan.duration,

                        paymentMethod:
                            paymentMethod,

                        transactionId:
                            transactionId

                    })
                }
            );


        const result =
            await response.json();


        console.log(
            "Payment response:",
            result
        );


        // ------------------------------------------
        // SUCCESS
        // ------------------------------------------

        if (response.ok &&
            result.success) {

            message.textContent =
                result.message;

            message.style.color =
                "#218646";


            document.getElementById(
                "paymentStatus"
            ).textContent =
                "Completed";


            // Remove selected plan
            // after successful payment

            localStorage.removeItem(
                "gymforgeSelectedPlan"
            );


            // Change button

            payButton.textContent =
                "Payment Completed";

            payButton.disabled =
                true;


        } else {

            message.textContent =
                result.message ||
                "Payment failed.";

            message.style.color =
                "#c62828";


            payButton.disabled =
                false;

            payButton.textContent =
                "Pay Now →";
        }


    } catch (error) {

        console.error(
            "Payment error:",
            error
        );


        message.textContent =
            "Unable to connect to the payment server.";

        message.style.color =
            "#c62828";


        payButton.disabled =
            false;

        payButton.textContent =
            "Pay Now →";
    }

}