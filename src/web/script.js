// ==========================================
// GYMFORGE
// Registration, Login & Validation
// Java Server + MySQL
// ==========================================


// ==========================================
// DOM ELEMENTS
// ==========================================

const registerContainer =
    document.getElementById("registerContainer");

const loginContainer =
    document.getElementById("loginContainer");

const showLogin =
    document.getElementById("showLogin");

const showRegister =
    document.getElementById("showRegister");

const registrationForm =
    document.getElementById("registrationForm");

const loginForm =
    document.getElementById("loginForm");


// ==========================================
// REGISTRATION FIELDS
// ==========================================

const fullName =
    document.getElementById("fullName");

const username =
    document.getElementById("username");

const email =
    document.getElementById("email");

const phone =
    document.getElementById("phone");

const password =
    document.getElementById("password");

const confirmPassword =
    document.getElementById("confirmPassword");


// ==========================================
// LOGIN FIELDS
// ==========================================

const loginUsername =
    document.getElementById("loginUsername");

const loginPassword =
    document.getElementById("loginPassword");


// ==========================================
// MESSAGES
// ==========================================

const registrationMessage =
    document.getElementById("registrationMessage");

const loginMessage =
    document.getElementById("loginMessage");


// ==========================================
// JAVA SERVER URL
// ==========================================

const SERVER_URL = "/GymForge";


// ==========================================
// SHOW LOGIN FORM
// ==========================================

function displayLoginForm() {

    clearAllErrors();

    registrationForm.reset();

    registrationMessage.textContent = "";

    registrationMessage.className =
        "form-message";

    registerContainer.classList.remove(
        "active"
    );

    loginContainer.classList.add(
        "active"
    );
}


// ==========================================
// SHOW REGISTER FORM
// ==========================================

function displayRegisterForm() {

    clearAllErrors();

    loginForm.reset();

    loginMessage.textContent = "";

    loginMessage.className =
        "form-message";

    loginContainer.classList.remove(
        "active"
    );

    registerContainer.classList.add(
        "active"
    );
}


// ==========================================
// FORM SWITCHING
// ==========================================

showLogin.addEventListener(
    "click",
    displayLoginForm
);

showRegister.addEventListener(
    "click",
    displayRegisterForm
);


// ==========================================
// VALIDATION FUNCTIONS
// ==========================================

function validateFullName() {

    const value =
        fullName.value.trim();

    const error =
        document.getElementById(
            "fullNameError"
        );

    if (value === "") {

        showError(
            fullName,
            error,
            "Full name is required."
        );

        return false;
    }

    if (!/^[A-Za-z ]{3,50}$/.test(value)) {

        showError(
            fullName,
            error,
            "Enter a valid name."
        );

        return false;
    }

    showSuccess(
        fullName,
        error
    );

    return true;
}


function validateUsername() {

    const value =
        username.value.trim();

    const error =
        document.getElementById(
            "usernameError"
        );

    if (value === "") {

        showError(
            username,
            error,
            "Username is required."
        );

        return false;
    }

    if (!/^[A-Za-z0-9_]{4,20}$/.test(value)) {

        showError(
            username,
            error,
            "Use 4–20 letters, numbers or underscores."
        );

        return false;
    }

    showSuccess(
        username,
        error
    );

    return true;
}


function validateEmail() {

    const value =
        email.value.trim();

    const error =
        document.getElementById(
            "emailError"
        );

    if (value === "") {

        showError(
            email,
            error,
            "Email address is required."
        );

        return false;
    }

    const emailPattern =
        /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!emailPattern.test(value)) {

        showError(
            email,
            error,
            "Enter a valid email address."
        );

        return false;
    }

    showSuccess(
        email,
        error
    );

    return true;
}


function validatePhone() {

    const value =
        phone.value.trim();

    const error =
        document.getElementById(
            "phoneError"
        );

    if (value === "") {

        showError(
            phone,
            error,
            "Phone number is required."
        );

        return false;
    }

    if (!/^[6-9][0-9]{9}$/.test(value)) {

        showError(
            phone,
            error,
            "Enter a valid 10-digit phone number."
        );

        return false;
    }

    showSuccess(
        phone,
        error
    );

    return true;
}


function validatePassword() {

    const value =
        password.value;

    const error =
        document.getElementById(
            "passwordError"
        );

    if (value === "") {

        showError(
            password,
            error,
            "Password is required."
        );

        return false;
    }

    if (value.length < 8) {

        showError(
            password,
            error,
            "Password must contain at least 8 characters."
        );

        return false;
    }

    if (!/[A-Z]/.test(value)) {

        showError(
            password,
            error,
            "Include at least one uppercase letter."
        );

        return false;
    }

    if (!/[0-9]/.test(value)) {

        showError(
            password,
            error,
            "Include at least one number."
        );

        return false;
    }

    showSuccess(
        password,
        error
    );

    return true;
}


function validateConfirmPassword() {

    const value =
        confirmPassword.value;

    const error =
        document.getElementById(
            "confirmPasswordError"
        );

    if (value === "") {

        showError(
            confirmPassword,
            error,
            "Please confirm your password."
        );

        return false;
    }

    if (value !== password.value) {

        showError(
            confirmPassword,
            error,
            "Passwords do not match."
        );

        return false;
    }

    showSuccess(
        confirmPassword,
        error
    );

    return true;
}


// ==========================================
// ERROR / SUCCESS DISPLAY
// ==========================================

function showError(
    input,
    errorElement,
    message
) {

    input.classList.remove(
        "input-success"
    );

    input.classList.add(
        "input-error"
    );

    errorElement.textContent =
        message;
}


function showSuccess(
    input,
    errorElement
) {

    input.classList.remove(
        "input-error"
    );

    input.classList.add(
        "input-success"
    );

    errorElement.textContent =
        "";
}


// ==========================================
// LIVE VALIDATION
// ==========================================

fullName.addEventListener(
    "input",
    validateFullName
);

username.addEventListener(
    "input",
    validateUsername
);

email.addEventListener(
    "input",
    validateEmail
);

phone.addEventListener(
    "input",
    validatePhone
);

password.addEventListener(
    "input",
    () => {

        validatePassword();

        if (confirmPassword.value !== "") {

            validateConfirmPassword();
        }
    }
);

confirmPassword.addEventListener(
    "input",
    validateConfirmPassword
);


// ==========================================
// REGISTRATION
// ==========================================

registrationForm.addEventListener(
    "submit",
    async (event) => {

        event.preventDefault();


        // ==================================
        // VALIDATE FORM
        // ==================================

        const validName =
            validateFullName();

        const validUsername =
            validateUsername();

        const validEmail =
            validateEmail();

        const validPhone =
            validatePhone();

        const validPassword =
            validatePassword();

        const validConfirmPassword =
            validateConfirmPassword();


        if (
            !validName ||
            !validUsername ||
            !validEmail ||
            !validPhone ||
            !validPassword ||
            !validConfirmPassword
        ) {

            registrationMessage.textContent =
                "Please correct the highlighted fields.";

            registrationMessage.className =
                "form-message error";

            return;
        }


        // ==================================
        // CREATE USER
        // ==================================

        const user = {

            fullName:
                fullName.value.trim(),

            username:
                username.value.trim(),

            email:
                email.value.trim(),

            phone:
                phone.value.trim(),

            dateOfBirth:
                "",

            country:
                "",

            gender:
                "",

            password:
                password.value
        };


        // ==================================
        // SHOW LOADING MESSAGE
        // ==================================

        registrationMessage.textContent =
            "Creating your account...";

        registrationMessage.className =
            "form-message";


        try {

            // ==================================
            // SEND DATA TO JAVA SERVER
            // ==================================

            const response =
                await fetch(
                    SERVER_URL + "/register",
                    {
                        method: "POST",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body:
                            JSON.stringify(user)
                    }
                );


            const message =
                await response.text();


            // ==================================
            // REGISTRATION SUCCESS
            // ==================================

            if (response.ok) {

                /*
                 * Registration is stored in MySQL.
                 *
                 * We do NOT use gymforgeUser
                 * for the active logged-in profile.
                 */

                registrationMessage.textContent =
                    "Registration successful! Please sign in.";

                registrationMessage.className =
                    "form-message success";


                registrationForm.reset();

                clearAllErrors();


                // ==================================
                // SHOW LOGIN FORM
                // ==================================

                setTimeout(
                    () => {

                        displayLoginForm();

                        loginMessage.textContent =
                            "Registration successful! Please sign in.";

                        loginMessage.className =
                            "form-message success";

                    },
                    700
                );


            } else {

                // ==================================
                // REGISTRATION FAILED
                // ==================================

                registrationMessage.textContent =
                    message ||
                    "Registration failed.";

                registrationMessage.className =
                    "form-message error";
            }


        } catch (error) {

            // ==================================
            // SERVER CONNECTION ERROR
            // ==================================

            console.error(
                "Registration error:",
                error
            );

            registrationMessage.textContent =
                "Cannot connect to Java server. Make sure GymServer is running.";

            registrationMessage.className =
                "form-message error";
        }

    }
);


// ==========================================
// LOGIN
// ==========================================

loginForm.addEventListener(
    "submit",
    async (event) => {

        event.preventDefault();


        const usernameValue =
            loginUsername.value.trim();

        const passwordValue =
            loginPassword.value;


        let valid = true;


        // ==================================
        // USERNAME VALIDATION
        // ==================================

        const loginUsernameError =
            document.getElementById(
                "loginUsernameError"
            );


        if (usernameValue === "") {

            showError(
                loginUsername,
                loginUsernameError,
                "Username is required."
            );

            valid = false;

        } else {

            showSuccess(
                loginUsername,
                loginUsernameError
            );
        }


        // ==================================
        // PASSWORD VALIDATION
        // ==================================

        const loginPasswordError =
            document.getElementById(
                "loginPasswordError"
            );


        if (passwordValue === "") {

            showError(
                loginPassword,
                loginPasswordError,
                "Password is required."
            );

            valid = false;

        } else {

            showSuccess(
                loginPassword,
                loginPasswordError
            );
        }


        if (!valid) {

            loginMessage.textContent =
                "Please enter your login details.";

            loginMessage.className =
                "form-message error";

            return;
        }


        // ==================================
        // SHOW LOADING MESSAGE
        // ==================================

        loginMessage.textContent =
            "Checking login details...";

        loginMessage.className =
            "form-message";


        try {

            // ==================================
            // SEND LOGIN TO JAVA SERVER
            // ==================================

            const response =
                await fetch(
                    SERVER_URL + "/login",
                    {
                        method: "POST",

                        headers: {
                            "Content-Type":
                                "application/json"
                        },

                        body:
                            JSON.stringify({

                                username:
                                    usernameValue,

                                password:
                                    passwordValue

                            })
                    }
                );


            // ==================================
            // LOGIN SUCCESS
            // ==================================

            if (response.ok) {

                /*
                 * IMPORTANT:
                 *
                 * Java gets the user from MySQL
                 * and returns that user's details.
                 *
                 * We now store THAT response.
                 *
                 * We do NOT read gymforgeUser.
                 */

                const user =
                    await response.json();


                // ==================================
                // STORE LOGIN STATUS
                // ==================================

                localStorage.setItem(
                    "gymforgeLoggedIn",
                    "true"
                );


                // ==================================
                // STORE CURRENT LOGGED-IN USER
                // ==================================

                localStorage.setItem(
                    "gymforgeCurrentUser",
                    JSON.stringify(user)
                );


                // ==================================
                // LOGIN SUCCESS MESSAGE
                // ==================================

                loginMessage.textContent =
                    "Login successful!";

                loginMessage.className =
                    "form-message success";


                // ==================================
                // OPEN DASHBOARD
                // ==================================

                setTimeout(
                    () => {

                        window.location.href =
                            "dashboard.html";

                    },
                    500
                );


            } else {

                // ==================================
                // LOGIN FAILED
                // ==================================

                const message =
                    await response.text();


                loginMessage.textContent =
                    message ||
                    "Invalid username or password.";

                loginMessage.className =
                    "form-message error";
            }


        } catch (error) {

            // ==================================
            // SERVER CONNECTION ERROR
            // ==================================

            console.error(
                "Login error:",
                error
            );

            loginMessage.textContent =
                "Cannot connect to Java server. Make sure GymServer is running.";

            loginMessage.className =
                "form-message error";
        }

    }
);


// ==========================================
// CLEAR ERRORS
// ==========================================

function clearAllErrors() {

    const inputs =
        document.querySelectorAll("input");


    inputs.forEach(
        (input) => {

            input.classList.remove(
                "input-error",
                "input-success"
            );
        }
    );


    const errors =
        document.querySelectorAll(
            ".error-message"
        );


    errors.forEach(
        (error) => {

            error.textContent =
                "";
        }
    );
}