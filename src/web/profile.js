const SERVER_URL = "/GymForge";

const storedUser =
    localStorage.getItem("gymforgeCurrentUser");

if (!storedUser) {

    window.location.href = "index.html";

} else {

    const user =
        JSON.parse(storedUser);

    // -----------------------------
    // DISPLAY USER INFORMATION
    // -----------------------------

    document.getElementById("fullName").value =
        user.fullName || "";

    document.getElementById("username").value =
        user.username || "";

    document.getElementById("email").value =
        user.email || "";

    document.getElementById("phone").value =
        user.phone || "";

    document.getElementById("dateOfBirth").value =
        user.dateOfBirth || "";

    document.getElementById("country").value =
        user.country || "";

    document.getElementById("gender").value =
        user.gender || "";


    // -----------------------------
    // PROFILE SUMMARY
    // -----------------------------

    document.getElementById("profileDisplayName").textContent =
        user.fullName || "Member";

    document.getElementById("profileDisplayUsername").textContent =
        user.username
            ? "@" + user.username
            : "@username";


    // -----------------------------
    // AVATAR
    // -----------------------------

    const firstLetter =
        (user.fullName || "U")
            .charAt(0)
            .toUpperCase();

    document.getElementById("profileAvatar").textContent =
        firstLetter;


    // -----------------------------
    // SAVE PROFILE
    // -----------------------------

    document.getElementById("profileForm")
        .addEventListener("submit", async function (event) {

            event.preventDefault();

            const message =
                document.getElementById("profileMessage");

            const password =
                document.getElementById("password").value;


            const updatedUser = {

                username:
                    user.username,

                fullName:
                    document.getElementById("fullName").value.trim(),

                email:
                    document.getElementById("email").value.trim(),

                phone:
                    document.getElementById("phone").value.trim(),

                dateOfBirth:
                    document.getElementById("dateOfBirth").value,

                country:
                    document.getElementById("country").value.trim(),

                gender:
                    document.getElementById("gender").value,

                password:
                    password

            };


            // -----------------------------
            // BASIC VALIDATION
            // -----------------------------

            if (
                !updatedUser.fullName ||
                !updatedUser.email ||
                !updatedUser.phone
            ) {

                message.textContent =
                    "Please fill in all required fields.";

                message.style.color =
                    "#c62828";

                return;
            }


            try {

                // -----------------------------
                // SEND TO JAVA SERVLET
                // -----------------------------

                const response =
                    await fetch(
                        SERVER_URL + "/update-profile",
                        {
                            method: "POST",

                            headers: {
                                "Content-Type":
                                    "application/json"
                            },

                            body:
                                JSON.stringify(updatedUser)
                        }
                    );


                // Servlet returns plain text
                const result =
                    await response.text();


                console.log(
                    "Update profile response:",
                    result
                );


                // -----------------------------
                // SUCCESS
                // -----------------------------

                if (response.ok) {

                    const newUser = {

                        ...user,

                        fullName:
                            updatedUser.fullName,

                        email:
                            updatedUser.email,

                        phone:
                            updatedUser.phone,

                        dateOfBirth:
                            updatedUser.dateOfBirth,

                        country:
                            updatedUser.country,

                        gender:
                            updatedUser.gender

                    };


                    // Update localStorage
                    localStorage.setItem(
                        "gymforgeCurrentUser",
                        JSON.stringify(newUser)
                    );


                    // Update displayed name
                    document.getElementById(
                        "profileDisplayName"
                    ).textContent =
                        updatedUser.fullName;


                    // Update username display
                    document.getElementById(
                        "profileDisplayUsername"
                    ).textContent =
                        "@" + updatedUser.username;


                    // Update avatar
                    document.getElementById(
                        "profileAvatar"
                    ).textContent =
                        updatedUser.fullName
                            .charAt(0)
                            .toUpperCase();


                    message.textContent =
                        result;

                    message.style.color =
                        "#218646";


                    // Clear password field
                    document.getElementById(
                        "password"
                    ).value = "";


                } else {

                    message.textContent =
                        result;

                    message.style.color =
                        "#c62828";
                }


            } catch (error) {

                console.error(
                    "Profile update error:",
                    error
                );

                message.textContent =
                    "Unable to connect to the server.";

                message.style.color =
                    "#c62828";
            }

        });
}