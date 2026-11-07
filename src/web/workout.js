// ==========================================
// GYMFORGE
// PERSONALIZED WORKOUT PLANNER
// ==========================================


// ==========================================
// GET HTML ELEMENTS
// ==========================================

const fitnessGoal =
    document.getElementById("fitnessGoal");

const experience =
    document.getElementById("experience");

const daysPerWeek =
    document.getElementById("daysPerWeek");

const generateButton =
    document.getElementById("generateWorkout");

const workoutMessage =
    document.getElementById("workoutMessage");

const planSummary =
    document.getElementById("planSummary");

const workoutList =
    document.getElementById("workoutList");


// ==========================================
// GENERATE WORKOUT PLAN
// ==========================================

generateButton.addEventListener("click", function () {

    const goal = fitnessGoal.value;

    const level = experience.value;

    const days = parseInt(daysPerWeek.value);


    // ==========================================
    // VALIDATION
    // ==========================================

    if (
        goal === "" ||
        level === "" ||
        isNaN(days)
    ) {

        workoutMessage.textContent =
            "Please select your goal, experience level and training days.";

        workoutMessage.style.color =
            "var(--accent)";

        return;
    }


    workoutMessage.textContent = "";


    // ==========================================
    // CREATE WORKOUT PLAN
    // ==========================================

    const plan =
        createWorkoutPlan(
            goal,
            level,
            days
        );

saveWorkoutPlan(goal, level, days, plan);
    // ==========================================
    // DISPLAY SUMMARY
    // ==========================================

    let goalText;

    if (goal === "weight-loss") {

        goalText = "Weight Loss";

    } else if (goal === "muscle-gain") {

        goalText = "Muscle Gain";

    } else {

        goalText = "General Fitness";

    }


    let levelText;

    if (level === "beginner") {

        levelText = "Beginner";

    } else if (level === "intermediate") {

        levelText = "Intermediate";

    } else {

        levelText = "Advanced";

    }


    planSummary.innerHTML = `
        <strong>${goalText}</strong> plan for a
        <strong>${levelText}</strong> level trainee,
        training <strong>${days} days per week</strong>.
        Follow the recommended exercises, sets and repetitions
        while maintaining proper form.
    `;


    // ==========================================
    // DISPLAY WORKOUT
    // ==========================================

    workoutList.innerHTML = "";


    plan.forEach(function (day) {

        const dayCard =
            document.createElement("div");

        dayCard.className =
            "workout-day";


        // ==========================================
        // REST DAY
        // ==========================================

        if (day.rest) {

            dayCard.classList.add("rest-day");

            dayCard.innerHTML = `

                <div class="workout-day-name">
                    ${day.day}
                </div>

                <div class="workout-focus">
                    Recovery Day
                </div>

                <ul class="exercise-list">

                    <li>
                        Light walking or stretching
                    </li>

                    <li>
                        Mobility exercises
                    </li>

                    <li>
                        Adequate hydration and recovery
                    </li>

                </ul>

            `;

        }


        // ==========================================
        // WORKOUT DAY
        // ==========================================

        else {

            dayCard.innerHTML = `

                <div class="workout-day-name">
                    ${day.day}
                </div>

                <div class="workout-focus">
                    ${day.focus}
                </div>

                <ul class="exercise-list">

                    ${day.exercises.map(
                        exercise =>
                            `<li>${exercise}</li>`
                    ).join("")}

                </ul>

            `;

        }


        workoutList.appendChild(dayCard);

    });


    // ==========================================
    // SAVE PLAN TO DATABASE
    // ==========================================

    saveWorkoutPlan(
        goal,
        level,
        days,
        plan
    );

});


// ==========================================
// SAVE WORKOUT PLAN TO SERVER
// ==========================================

async function saveWorkoutPlan(
    goal,
    level,
    days,
    plan
) {

    try {

        const response =
            await fetch(
                "/GymForge/workout",
                {
                    method: "POST",

                    credentials: "include",

                    headers: {
                        "Content-Type":
                            "application/x-www-form-urlencoded"
                    },

                    body:
                        "fitnessGoal=" +
                        encodeURIComponent(goal) +

                        "&experienceLevel=" +
                        encodeURIComponent(level) +

                        "&daysPerWeek=" +
                        encodeURIComponent(days) +

                        "&planData=" +
                        encodeURIComponent(
                            JSON.stringify(plan)
                        )
                }
            );


        const result =
            await response.json();


        console.log(
            "Workout save response:",
            result
        );


        if (
            response.ok &&
            result.success
        ) {

            workoutMessage.textContent =
                "Workout plan saved successfully!";

            workoutMessage.style.color =
                "var(--accent)";

        } else {

            workoutMessage.textContent =
                result.message ||
                "Workout plan could not be saved.";

            workoutMessage.style.color =
                "#d98f8f";

        }


    } catch (error) {

        console.error(
            "Workout save error:",
            error
        );

        workoutMessage.textContent =
            "Unable to save workout plan.";

        workoutMessage.style.color =
            "#d98f8f";
    }

}


// ==========================================
// CREATE WORKOUT PLAN
// ==========================================

function createWorkoutPlan(
    goal,
    level,
    days
) {


    // ==========================================
    // EXERCISE INTENSITY
    // ==========================================

    let sets;

    let reps;


    if (level === "beginner") {

        sets = "2–3 sets";

        reps = "10–12 reps";

    } else if (level === "intermediate") {

        sets = "3 sets";

        reps = "8–12 reps";

    } else {

        sets = "3–4 sets";

        reps = "6–12 reps";

    }


    // ==========================================
    // WEIGHT LOSS
    // ==========================================

    if (goal === "weight-loss") {

        return createWeightLossPlan(
            days,
            sets,
            reps
        );

    }


    // ==========================================
    // MUSCLE GAIN
    // ==========================================

    if (goal === "muscle-gain") {

        return createMuscleGainPlan(
            days,
            sets,
            reps
        );

    }


    // ==========================================
    // GENERAL FITNESS
    // ==========================================

    return createGeneralFitnessPlan(
        days,
        sets,
        reps
    );

}


// ==========================================
// WEIGHT LOSS PLAN
// ==========================================

function createWeightLossPlan(
    days,
    sets,
    reps
) {

    const workouts = [

        {
            day: "DAY 1",

            focus: "Full Body Strength",

            exercises: [

                `Squats — ${sets} × ${reps}`,

                `Push-ups — ${sets} × ${reps}`,

                `Dumbbell Rows — ${sets} × ${reps}`,

                `Walking Lunges — ${sets} × ${reps}`,

                "10–15 minutes moderate cardio"

            ]
        },


        {
            day: "DAY 2",

            focus: "Cardio & Core",

            exercises: [

                "20–30 minutes brisk walking or cycling",

                `Plank — ${sets} × 30–45 seconds`,

                `Mountain Climbers — ${sets} × 20 reps`,

                `Leg Raises — ${sets} × ${reps}`

            ]
        },


        {
            day: "DAY 3",

            focus: "Lower Body",

            exercises: [

                `Squats — ${sets} × ${reps}`,

                `Lunges — ${sets} × ${reps}`,

                `Glute Bridges — ${sets} × ${reps}`,

                `Calf Raises — ${sets} × ${reps}`,

                "10 minutes light cardio"

            ]
        },


        {
            day: "DAY 4",

            focus: "Upper Body",

            exercises: [

                `Push-ups — ${sets} × ${reps}`,

                `Dumbbell Rows — ${sets} × ${reps}`,

                `Shoulder Press — ${sets} × ${reps}`,

                `Biceps Curls — ${sets} × ${reps}`,

                `Triceps Extensions — ${sets} × ${reps}`

            ]
        },


        {
            day: "DAY 5",

            focus: "Full Body Conditioning",

            exercises: [

                `Goblet Squats — ${sets} × ${reps}`,

                `Push-ups — ${sets} × ${reps}`,

                `Dumbbell Rows — ${sets} × ${reps}`,

                "15–20 minutes cardio"

            ]
        },


        {
            day: "DAY 6",

            focus: "Cardio & Mobility",

            exercises: [

                "25–35 minutes brisk walking or cycling",

                "Full-body stretching",

                "Mobility exercises"

            ]
        }

    ];


    return selectTrainingDays(
        workouts,
        days
    );

}


// ==========================================
// MUSCLE GAIN PLAN
// ==========================================

function createMuscleGainPlan(
    days,
    sets,
    reps
) {

    const workouts = [

        {
            day: "DAY 1",

            focus: "Chest & Triceps",

            exercises: [

                `Bench Press — ${sets} × ${reps}`,

                `Incline Dumbbell Press — ${sets} × ${reps}`,

                `Chest Fly — ${sets} × ${reps}`,

                `Triceps Pushdown — ${sets} × ${reps}`,

                `Overhead Triceps Extension — ${sets} × ${reps}`

            ]
        },


        {
            day: "DAY 2",

            focus: "Back & Biceps",

            exercises: [

                `Lat Pulldown — ${sets} × ${reps}`,

                `Seated Cable Row — ${sets} × ${reps}`,

                `Dumbbell Row — ${sets} × ${reps}`,

                `Biceps Curl — ${sets} × ${reps}`,

                `Hammer Curl — ${sets} × ${reps}`

            ]
        },


        {
            day: "DAY 3",

            focus: "Legs",

            exercises: [

                `Squats — ${sets} × ${reps}`,

                `Leg Press — ${sets} × ${reps}`,

                `Leg Curl — ${sets} × ${reps}`,

                `Leg Extension — ${sets} × ${reps}`,

                `Calf Raises — ${sets} × ${reps}`

            ]
        },


        {
            day: "DAY 4",

            focus: "Shoulders & Core",

            exercises: [

                `Shoulder Press — ${sets} × ${reps}`,

                `Lateral Raises — ${sets} × ${reps}`,

                `Rear Delt Fly — ${sets} × ${reps}`,

                `Plank — ${sets} × 30–60 seconds`,

                `Leg Raises — ${sets} × ${reps}`

            ]
        },


        {
            day: "DAY 5",

            focus: "Upper Body",

            exercises: [

                `Bench Press — ${sets} × ${reps}`,

                `Lat Pulldown — ${sets} × ${reps}`,

                `Shoulder Press — ${sets} × ${reps}`,

                `Biceps Curl — ${sets} × ${reps}`,

                `Triceps Pushdown — ${sets} × ${reps}`

            ]
        },


        {
            day: "DAY 6",

            focus: "Lower Body",

            exercises: [

                `Romanian Deadlift — ${sets} × ${reps}`,

                `Leg Press — ${sets} × ${reps}`,

                `Walking Lunges — ${sets} × ${reps}`,

                `Leg Curl — ${sets} × ${reps}`,

                `Calf Raises — ${sets} × ${reps}`

            ]
        }

    ];


    return selectTrainingDays(
        workouts,
        days
    );

}


// ==========================================
// GENERAL FITNESS PLAN
// ==========================================

function createGeneralFitnessPlan(
    days,
    sets,
    reps
) {

    const workouts = [

        {
            day: "DAY 1",

            focus: "Full Body",

            exercises: [

                `Squats — ${sets} × ${reps}`,

                `Push-ups — ${sets} × ${reps}`,

                `Dumbbell Rows — ${sets} × ${reps}`,

                `Shoulder Press — ${sets} × ${reps}`,

                "10 minutes cardio"

            ]
        },


        {
            day: "DAY 2",

            focus: "Cardio & Core",

            exercises: [

                "20–30 minutes moderate cardio",

                `Plank — ${sets} × 30–45 seconds`,

                `Bicycle Crunches — ${sets} × ${reps}`,

                `Leg Raises — ${sets} × ${reps}`

            ]
        },


        {
            day: "DAY 3",

            focus: "Lower Body",

            exercises: [

                `Squats — ${sets} × ${reps}`,

                `Lunges — ${sets} × ${reps}`,

                `Glute Bridges — ${sets} × ${reps}`,

                `Calf Raises — ${sets} × ${reps}`

            ]
        },


        {
            day: "DAY 4",

            focus: "Upper Body",

            exercises: [

                `Push-ups — ${sets} × ${reps}`,

                `Dumbbell Rows — ${sets} × ${reps}`,

                `Shoulder Press — ${sets} × ${reps}`,

                `Biceps Curls — ${sets} × ${reps}`,

                `Triceps Extensions — ${sets} × ${reps}`

            ]
        },


        {
            day: "DAY 5",

            focus: "Full Body Conditioning",

            exercises: [

                `Goblet Squats — ${sets} × ${reps}`,

                `Push-ups — ${sets} × ${reps}`,

                `Dumbbell Rows — ${sets} × ${reps}`,

                "15 minutes cardio"

            ]
        },


        {
            day: "DAY 6",

            focus: "Active Recovery",

            exercises: [

                "20–30 minutes walking",

                "Full-body stretching",

                "Mobility exercises"

            ]
        }

    ];


    return selectTrainingDays(
        workouts,
        days
    );

}


// ==========================================
// SELECT NUMBER OF TRAINING DAYS
// ==========================================

function selectTrainingDays(
    workouts,
    numberOfDays
) {

    const selected =
        workouts.slice(
            0,
            numberOfDays
        );


    // ==========================================
    // ADD REST DAYS
    // ==========================================

    const totalDays = 7;

    const restDays =
        totalDays - numberOfDays;


    for (
        let i = 0;
        i < restDays;
        i++
    ) {

        selected.push({

            day:
                `DAY ${numberOfDays + i + 1}`,

            rest: true

        });

    }


    return selected;

}
// ==========================================
// SAVE WORKOUT PLAN TO DATABASE
// ==========================================

async function saveWorkoutPlan(goal, level, days, plan) {

    try {

        const response = await fetch("/GymForge/workout", {
            method: "POST",
            credentials: "include",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body:
                "fitnessGoal=" + encodeURIComponent(goal) +
                "&experienceLevel=" + encodeURIComponent(level) +
                "&daysPerWeek=" + encodeURIComponent(days) +
                "&planData=" + encodeURIComponent(
                    JSON.stringify(plan)
                )
        });

        const result = await response.json();

        console.log("Workout save response:", result);

        if (result.success) {

            workoutMessage.textContent =
                "Workout plan saved successfully!";

            workoutMessage.style.color = "var(--accent)";

        } else {

            workoutMessage.textContent =
                result.message || "Failed to save workout plan.";

            workoutMessage.style.color = "red";
        }

    } catch (error) {

        console.error("Workout save error:", error);

        workoutMessage.textContent =
            "Workout generated, but could not be saved.";

        workoutMessage.style.color = "red";
    }
}