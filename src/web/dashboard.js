const SERVER_URL = "/GymForge";

document.addEventListener("DOMContentLoaded", function () {

    loadDashboard();

    setupDashboardLinks();

    setupLogout();

    setupDietButton();

});


/* ==========================================
   LOAD DASHBOARD DATA
========================================== */

async function loadDashboard() {

    try {

        const response = await fetch(
            SERVER_URL + "/dashboard",
            {
                method: "GET",
                credentials: "include"
            }
        );


        /* ==============================
           LOGIN REQUIRED
        ============================== */

        if (response.status === 401) {

            window.location.href = "index.html";

            return;
        }


        const result = await response.json();

        console.log("Dashboard response:", result);


        if (!result.success) {

            console.error(
                "Dashboard error:",
                result.message
            );

            return;
        }


        /* ==============================
           USER
        ============================== */

        const user = result.user;

        if (user) {

            updateUserInformation(user);

        }


        /* ==============================
           MEMBERSHIP
        ============================== */

        if (result.membership) {

            updateMembership(
                result.membership
            );

        }


        /* ==============================
           ATTENDANCE
        ============================== */

        if (result.attendance) {

            updateAttendance(
                result.attendance
            );

        }


        /* ==============================
           WORKOUT PLANS
        ============================== */

        if (result.workoutPlans) {

            updateWorkoutPlans(
                result.workoutPlans
            );

        }


        /* ==============================
           PAYMENT
        ============================== */

        if (result.payment) {

            updatePayment(
                result.payment
            );

        }

    } catch (error) {

        console.error(
            "Failed to load dashboard:",
            error
        );

    }

}


/* ==========================================
   USER INFORMATION
========================================== */

function updateUserInformation(user) {

    const userName =
        document.getElementById("userName");

    const welcomeName =
        document.getElementById("welcomeName");

    const userInitial =
        document.getElementById("userInitial");

    const profileFullName =
        document.getElementById("profileFullName");

    const profileUsername =
        document.getElementById("profileUsername");

    const profileEmail =
        document.getElementById("profileEmail");

    const profilePhone =
        document.getElementById("profilePhone");


    /* Top-right name */

    if (userName) {

        userName.textContent =
            user.fullName || "Member";

    }


    /* Welcome message */

    if (welcomeName) {

        welcomeName.textContent =
            user.fullName || "Member";

    }


    /* Avatar */

    if (userInitial) {

        if (user.fullName) {

            userInitial.textContent =
                user.fullName
                    .charAt(0)
                    .toUpperCase();

        } else {

            userInitial.textContent = "M";

        }

    }


    /* Profile information */

    if (profileFullName) {

        profileFullName.textContent =
            user.fullName || "—";

    }


    if (profileUsername) {

        profileUsername.textContent =
            user.username || "—";

    }


    if (profileEmail) {

        profileEmail.textContent =
            user.email || "—";

    }


    if (profilePhone) {

        profilePhone.textContent =
            user.phone || "—";

    }

}


/* ==========================================
   MEMBERSHIP
========================================== */

function updateMembership(membership) {

    const membershipCard =
        document.querySelector(
            ".stat-card:nth-child(1) .stat-content h3"
        );


    if (!membershipCard) {
        return;
    }


    if (
        membership.status &&
        membership.status.toLowerCase() === "active"
    ) {

        membershipCard.textContent =
            "Active";

    } else {

        membershipCard.textContent =
            "Inactive";

    }


    /* Optional console information */

    console.log(
        "Membership:",
        membership.plan
    );

    console.log(
        "Membership status:",
        membership.status
    );

    console.log(
        "Membership end:",
        membership.endDate
    );

}


/* ==========================================
   ATTENDANCE
========================================== */

function updateAttendance(attendance) {

    const attendanceCard =
        document.querySelector(
            ".stat-card:nth-child(2) .stat-content h3"
        );


    if (!attendanceCard) {
        return;
    }


    const days =
        Number(attendance.days || 0);


    attendanceCard.textContent =
        days + (days === 1 ? " Day" : " Days");

}


/* ==========================================
   WORKOUT PLANS
========================================== */

function updateWorkoutPlans(workoutPlans) {

    const workoutCard =
        document.querySelector(
            ".stat-card:nth-child(3) .stat-content h3"
        );


    if (!workoutCard) {
        return;
    }


    const count =
        Number(workoutPlans.count || 0);


    workoutCard.textContent =
        count + (count === 1 ? " Plan" : " Plans");

}


/* ==========================================
   PAYMENT
========================================== */

function updatePayment(payment) {

    const paymentCard =
        document.querySelector(
            ".stat-card:nth-child(4) .stat-content h3"
        );


    if (!paymentCard) {
        return;
    }


    if (
        payment.status &&
        payment.status.toLowerCase() === "completed"
    ) {

        paymentCard.textContent =
            "Completed";

    } else if (
        payment.status
    ) {

        paymentCard.textContent =
            payment.status;

    } else {

        paymentCard.textContent =
            "Pending";

    }

}


/* ==========================================
   DASHBOARD NAVIGATION
========================================== */

function setupDashboardLinks() {

    const actionCards =
        document.querySelectorAll(
            ".action-card"
        );


    if (actionCards.length >= 4) {

        /* Member Profile */

        actionCards[0].onclick =
            function () {

                window.location.href =
                    "profile.html";

            };


        /* Membership Plans */

        actionCards[1].onclick =
            function () {

                window.location.href =
                    "membership.html";

            };


        /* Workout Plans */

        actionCards[2].onclick =
            function () {

                window.location.href =
                    "workout.html";

            };


        /* Attendance */

        actionCards[3].onclick =
            function () {

                window.location.href =
                    "attendance.html";

            };

    }

}


/* ==========================================
   LOGOUT
========================================== */

function setupLogout() {

    const logoutButton =
        document.getElementById(
            "logoutButton"
        );


    if (!logoutButton) {
        return;
    }


    logoutButton.addEventListener(
        "click",
        function () {

            /*
             * We will connect this to
             * LogoutServlet when we create
             * the logout backend.
             */

            window.location.href =
                "index.html";

        }
    );

}


/* ==========================================
   BMI / DIET PLANNER
========================================== */

function setupDietButton() {

    const dietButton =
        document.getElementById(
            "dietButton"
        );


    if (!dietButton) {
        return;
    }


    dietButton.addEventListener(
        "click",
        function () {

            window.location.href =
                "bmi.html";

        }
    );

}