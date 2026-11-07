const SERVER_URL = "/GymForge";


// ==========================================
// LOAD TRAINERS
// ==========================================

document.addEventListener("DOMContentLoaded", function () {

    loadTrainers();

});


// ==========================================
// GET TRAINERS FROM JAVA
// ==========================================

async function loadTrainers() {

    const trainerGrid =
        document.getElementById("trainerGrid");

    try {

        const response =
            await fetch(
                SERVER_URL + "/trainers",
                {
                    method: "GET",
                    credentials: "include"
                }
            );


        if (response.status === 401) {

            trainerGrid.innerHTML =
                "<p>Please login to view trainers.</p>";

            return;
        }


        const result =
            await response.json();


        console.log(
            "Trainers response:",
            result
        );


        if (!result.success) {

            trainerGrid.innerHTML =
                "<p>Unable to load trainers.</p>";

            return;
        }


        displayTrainers(
            result.trainers
        );


    } catch (error) {

        console.error(
            "Trainer loading error:",
            error
        );


        trainerGrid.innerHTML =
            "<p>Unable to connect to the server.</p>";
    }

}


// ==========================================
// DISPLAY TRAINERS
// ==========================================
function displayTrainers(trainers) {

    const trainerGrid =
        document.getElementById("trainerGrid");

    const trainerCount =
        document.getElementById("trainerCount");

    // Update trainer count
    if (trainerCount) {
        trainerCount.textContent =
            trainers ? trainers.length : 0;
    }

    if (!trainers || trainers.length === 0) {

        trainerGrid.innerHTML =
            '<div class="loading-message">No trainers available.</div>';

        return;
    }

    trainerGrid.innerHTML = "";

    trainers.forEach(function (trainer) {

        const card = document.createElement("div");

        card.className = "trainer-card";

        card.innerHTML = `

            <div class="trainer-card-header">

                <div class="trainer-avatar">
                    ${getInitial(trainer.trainerName)}
                </div>

                <div>
                    <h3>
                        ${escapeHtml(trainer.trainerName)}
                    </h3>

                    <p>
                        ${escapeHtml(trainer.specialization)}
                    </p>
                </div>

            </div>

            <div class="trainer-details">

                <div>
                    <strong>Experience</strong>
                    <span>
                        ${trainer.experienceYears} years
                    </span>
                </div>

                <div>
                    <strong>Availability</strong>
                    <span>
                        ${escapeHtml(trainer.availability)}
                    </span>
                </div>

                <div>
                    <strong>Phone</strong>
                    <span>
                        ${escapeHtml(trainer.phone || "N/A")}
                    </span>
                </div>

            </div>

            <p class="trainer-description">
                ${escapeHtml(trainer.description || "")}
            </p>

            <button
                class="choose-plan-btn"
                onclick="requestTrainer(${trainer.id})"
            >
                Request Trainer →
            </button>
        `;

        trainerGrid.appendChild(card);
    });
}
// ==========================================
// REQUEST TRAINER
// ==========================================

async function requestTrainer(
    trainerId
) {

    try {

        const response =
            await fetch(
                SERVER_URL + "/trainers",
                {
                    method: "POST",

                    credentials: "include",

                    headers: {
                        "Content-Type":
                            "application/x-www-form-urlencoded"
                    },

                    body:
                        "trainerId=" +
                        encodeURIComponent(
                            trainerId
                        )
                }
            );


        const result =
            await response.json();


        console.log(
            "Trainer request response:",
            result
        );


        if (response.ok &&
            result.success) {

            alert(
                result.message
            );

        } else {

            alert(
                result.message ||
                "Trainer request failed."
            );
        }


    } catch (error) {

        console.error(
            "Trainer request error:",
            error
        );


        alert(
            "Unable to connect to the server."
        );

    }

}


// ==========================================
// GET INITIAL
// ==========================================

function getInitial(name) {

    if (!name) {
        return "T";
    }

    return name
        .charAt(0)
        .toUpperCase();

}


// ==========================================
// ESCAPE HTML
// ==========================================

function escapeHtml(value) {

    if (value === null ||
        value === undefined) {

        return "";
    }

    return String(value)
        .replace(
            /&/g,
            "&amp;"
        )
        .replace(
            /</g,
            "&lt;"
        )
        .replace(
            />/g,
            "&gt;"
        )
        .replace(
            /"/g,
            "&quot;"
        )
        .replace(
            /'/g,
            "&#039;"
        );

}