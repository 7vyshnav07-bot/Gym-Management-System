const SERVER_URL = "/GymForge";


// ==========================================
// LOAD MEMBERSHIP PAGE
// ==========================================

document.addEventListener("DOMContentLoaded", function () {

    loadCurrentMembership();

});


// ==========================================
// LOAD USER'S CURRENT MEMBERSHIP
// ==========================================

async function loadCurrentMembership() {

    const currentPlan =
        document.getElementById("currentPlan");

    const currentDetails =
        document.getElementById(
            "currentMembershipDetails"
        );

    const membershipStatus =
        document.getElementById(
            "membershipStatus"
        );


    try {

        const response =
            await fetch(
                SERVER_URL + "/membership",
                {
                    method: "GET",
                    credentials: "include"
                }
            );


        // ------------------------------------------
        // NOT LOGGED IN
        // ------------------------------------------

        if (response.status === 401) {

            currentPlan.textContent =
                "Please Login";

            currentDetails.textContent =
                "Login to view your membership.";

            membershipStatus.textContent =
                "LOGIN REQUIRED";

            return;
        }


        // ------------------------------------------
        // READ RESPONSE
        // ------------------------------------------

        const result =
            await response.json();


        console.log(
            "Membership response:",
            result
        );


        // ------------------------------------------
        // NO ACTIVE MEMBERSHIP
        // ------------------------------------------

        if (
            !result.success ||
            !result.membership
        ) {

            currentPlan.textContent =
                "No Active Membership";

            currentDetails.textContent =
                "Choose a membership plan below to get started.";

            membershipStatus.textContent =
                "INACTIVE";

            return;
        }


        // ------------------------------------------
        // MEMBERSHIP FOUND
        // ------------------------------------------

        const membership =
            result.membership;


        currentPlan.textContent =
            membership.planName;


        currentDetails.textContent =
            "₹" +
            membership.price +
            " • " +
            membership.durationMonths +
            " Month" +
            (
                membership.durationMonths > 1
                    ? "s"
                    : ""
            ) +
            " • Valid from " +
            formatDate(
                membership.startDate
            ) +
            " to " +
            formatDate(
                membership.endDate
            );


        membershipStatus.textContent =
            membership.status ||
            "ACTIVE";


        // ------------------------------------------
        // STATUS STYLE
        // ------------------------------------------

        membershipStatus.style.color =
            "#8fd3a9";

    }

    catch (error) {

        console.error(
            "Membership loading error:",
            error
        );


        currentPlan.textContent =
            "Unable to Load Membership";

        currentDetails.textContent =
            "Could not connect to the membership server.";

        membershipStatus.textContent =
            "ERROR";

    }

}


// ==========================================
// SELECT MEMBERSHIP PLAN
// ==========================================

function selectPlan(planId) {

    const plans = {

        1: {
            name: "Basic",
            price: 999,
            duration: "1 Month"
        },

        2: {
            name: "Premium",
            price: 2499,
            duration: "3 Months"
        },

        3: {
            name: "Elite",
            price: 7999,
            duration: "12 Months"
        }

    };


    const selectedPlan =
        plans[planId];


    if (!selectedPlan) {

        console.error(
            "Invalid membership plan:",
            planId
        );

        return;
    }


    // ------------------------------------------
    // STORE SELECTED PLAN
    // ------------------------------------------

    localStorage.setItem(
        "gymforgeSelectedPlan",
        JSON.stringify(
            selectedPlan
        )
    );


    // ------------------------------------------
    // GO TO PAYMENT
    // ------------------------------------------

    window.location.href =
        "payment.html";

}


// ==========================================
// FORMAT DATE
// ==========================================

function formatDate(dateString) {

    if (!dateString) {
        return "N/A";
    }


    const date =
        new Date(dateString);


    if (isNaN(date.getTime())) {
        return dateString;
    }


    return date.toLocaleDateString(
        "en-IN",
        {
            day: "2-digit",
            month: "short",
            year: "numeric"
        }
    );

}