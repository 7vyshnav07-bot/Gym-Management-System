// ==========================================
// GYMFORGE
// BMI & BALANCED DIET PLANNER
// ==========================================


// ==========================================
// GET HTML ELEMENTS
// ==========================================

const weightInput =
    document.getElementById("weight");

const heightInput =
    document.getElementById("height");

const ageInput =
    document.getElementById("age");

const genderInput =
    document.getElementById("gender");

const activityInput =
    document.getElementById("activity");


const calculateButton =
    document.getElementById("calculateBMI");


const bmiValue =
    document.getElementById("bmiValue");

const bmiCategory =
    document.getElementById("bmiCategory");

const calorieValue =
    document.getElementById("calorieValue");


const dietSubtitle =
    document.getElementById("dietSubtitle");


const breakfast =
    document.getElementById("breakfast");

const midMorning =
    document.getElementById("midMorning");

const lunch =
    document.getElementById("lunch");

const eveningSnack =
    document.getElementById("eveningSnack");

const dinner =
    document.getElementById("dinner");

const hydration =
    document.getElementById("hydration");


// ==========================================
// CALCULATE BMI
// ==========================================

calculateButton.addEventListener("click", function () {

    // ==========================================
    // GET USER INPUT
    // ==========================================

    const weight =
        parseFloat(weightInput.value);

    const height =
        parseFloat(heightInput.value);

    const age =
        parseInt(ageInput.value);

    const gender =
        genderInput.value;

    const activity =
        activityInput.value;


    // ==========================================
    // INPUT VALIDATION
    // ==========================================

    if (
        isNaN(weight) ||
        isNaN(height) ||
        isNaN(age) ||
        gender === "" ||
        activity === ""
    ) {

        alert(
            "Please complete all fields before calculating."
        );

        return;
    }


    if (
        weight <= 0 ||
        height <= 0 ||
        age <= 0
    ) {

        alert(
            "Please enter valid positive values."
        );

        return;
    }


    // ==========================================
    // BMI CALCULATION
    // ==========================================

    // Convert height from centimetres to metres

    const heightInMetres =
        height / 100;


    // BMI = Weight / Height²

    const bmi =
        weight /
        (heightInMetres * heightInMetres);


    const roundedBMI =
        bmi.toFixed(1);


    // Display BMI

    bmiValue.textContent =
        roundedBMI;


    // ==========================================
    // DETERMINE BMI CATEGORY
    // ==========================================

    let category;


    if (bmi < 18.5) {

        category = "Underweight";

    } else if (bmi < 25) {

        category = "Normal Weight";

    } else if (bmi < 30) {

        category = "Overweight";

    } else {

        category = "Obesity";
    }


    // Display category

    bmiCategory.textContent =
        category;


    // ==========================================
    // CALCULATE BMR
    // ==========================================

    let bmr;


    /*
        Mifflin-St Jeor Equation

        Male:
        BMR = 10W + 6.25H - 5A + 5

        Female:
        BMR = 10W + 6.25H - 5A - 161
    */


    if (gender === "male") {

        bmr =
            (10 * weight) +
            (6.25 * height) -
            (5 * age) +
            5;

    } else {

        bmr =
            (10 * weight) +
            (6.25 * height) -
            (5 * age) -
            161;
    }


    // ==========================================
    // ACTIVITY MULTIPLIER
    // ==========================================

    let activityMultiplier;


    if (activity === "low") {

        activityMultiplier = 1.2;

    } else if (activity === "moderate") {

        activityMultiplier = 1.55;

    } else {

        activityMultiplier = 1.725;
    }


    // ==========================================
    // DAILY CALORIE REQUIREMENT
    // ==========================================

    const dailyCalories =
        Math.round(
            bmr * activityMultiplier
        );


    // ==========================================
    // DISPLAY DAILY CALORIES
    // ==========================================

    calorieValue.textContent =
        dailyCalories;


    // ==========================================
    // GENERATE DIET PLAN
    // ==========================================

    generateDietPlan(
        category,
        activity,
        gender,
        age,
        dailyCalories
    );

});


// ==========================================
// GENERATE DIET PLAN
// ==========================================

function generateDietPlan(
    category,
    activity,
    gender,
    age,
    dailyCalories
) {

    let plan;


    // ==========================================
    // UNDERWEIGHT
    // ==========================================

    if (category === "Underweight") {

        plan = {

            breakfast:
                "Oats with milk, banana, nuts and eggs.",

            midMorning:
                "Banana or seasonal fruit with almonds.",

            lunch:
                "Rice or roti, dal, vegetables, curd and paneer or lean protein.",

            eveningSnack:
                "Peanut butter sandwich or a nutritious fruit smoothie.",

            dinner:
                "Roti or rice with vegetables and a protein-rich dish.",

            hydration:
                "Drink water regularly and include nutritious beverages such as milk."

        };

    }


    // ==========================================
    // NORMAL WEIGHT
    // ==========================================

    else if (category === "Normal Weight") {

        plan = {

            breakfast:
                "Oats or whole-grain toast with eggs and fresh fruit.",

            midMorning:
                "One fruit with almonds or walnuts.",

            lunch:
                "Roti or brown rice, dal, vegetables, curd and lean protein.",

            eveningSnack:
                "Sprouts, roasted chickpeas or yogurt.",

            dinner:
                "Roti with vegetables and paneer, eggs or lean protein.",

            hydration:
                "Drink water regularly throughout the day."

        };

    }


    // ==========================================
    // OVERWEIGHT
    // ==========================================

    else if (category === "Overweight") {

        plan = {

            breakfast:
                "Vegetable omelette or oats with fruit and low-fat milk.",

            midMorning:
                "One whole fruit such as apple, orange or guava.",

            lunch:
                "Roti, dal, vegetables, salad and curd.",

            eveningSnack:
                "Roasted chickpeas, sprouts or unsweetened yogurt.",

            dinner:
                "Vegetable soup with roti and a lean protein source.",

            hydration:
                "Prefer water and limit sugary drinks."

        };

    }


    // ==========================================
    // OBESITY
    // ==========================================

    else {

        plan = {

            breakfast:
                "Vegetable omelette or unsweetened oats with fruit.",

            midMorning:
                "One whole fruit and water.",

            lunch:
                "Vegetables, dal, salad and a moderate portion of roti or rice.",

            eveningSnack:
                "Sprouts or roasted chickpeas.",

            dinner:
                "Vegetables with lean protein and a moderate whole-grain portion.",

            hydration:
                "Prefer water and limit sugary drinks and highly processed foods."

        };

    }


    // ==========================================
    // AGE-BASED MESSAGE
    // ==========================================

    let ageMessage;


    if (age < 18) {

        ageMessage =
            "For younger users, focus on nutrient-rich foods and regular meals rather than calorie restriction.";

    } else if (age <= 30) {

        ageMessage =
            "Your plan emphasizes balanced protein, whole grains, vegetables and fruits to support an active lifestyle.";

    } else if (age <= 50) {

        ageMessage =
            "Your plan emphasizes balanced portions, protein, vegetables and whole grains for long-term fitness.";

    } else {

        ageMessage =
            "Your plan emphasizes nutrient-dense foods, adequate protein, vegetables and hydration.";

    }


    // ==========================================
    // ACTIVITY MESSAGE
    // ==========================================

    let activityMessage;


    if (activity === "low") {

        activityMessage =
            "Low activity level: focus on balanced portions and regular movement.";

    } else if (activity === "moderate") {

        activityMessage =
            "Moderate activity level: include sufficient protein and whole grains to support exercise.";

    } else {

        activityMessage =
            "High activity level: include adequate carbohydrates, protein and fluids to support workouts.";

    }


    // ==========================================
    // DISPLAY DIET PLAN
    // ==========================================

    breakfast.textContent =
        plan.breakfast;


    midMorning.textContent =
        plan.midMorning;


    lunch.textContent =
        plan.lunch;


    eveningSnack.textContent =
        plan.eveningSnack;


    dinner.textContent =
        plan.dinner;


    hydration.textContent =
        plan.hydration;


    // ==========================================
    // DISPLAY PERSONALIZED INFORMATION
    // ==========================================

    dietSubtitle.textContent =
        `${ageMessage} Estimated daily energy requirement: approximately ${dailyCalories} kcal. ${activityMessage}`;

}