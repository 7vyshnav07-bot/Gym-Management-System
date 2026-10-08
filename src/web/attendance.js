const storedUser =
    localStorage.getItem("gymforgeCurrentUser");

if (!storedUser) {
    window.location.href = "index.html";
}

const user = JSON.parse(storedUser);

const markAttendanceButton =
    document.getElementById("markAttendance");

const attendanceMessage =
    document.getElementById("attendanceMessage");

const attendanceTableBody =
    document.getElementById("attendanceTableBody");


// =====================================
// MARK ATTENDANCE
// =====================================

markAttendanceButton.addEventListener(
    "click",
    async function () {

        if (!user || !user.username) {

            attendanceMessage.textContent =
                "User information not found. Please login again.";

            return;
        }

        const requestData = {
            username: String(user.username),
            status: "Present"
        };

        console.log(
            "Sending attendance data:",
            requestData
        );

        try {

            const response =
                await fetch("/mark-attendance", {

                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify(requestData)
                });

            const result =
                await response.json();

            console.log(
                "Attendance response:",
                result
            );

            attendanceMessage.textContent =
                result.message;

            if (result.success) {
                loadAttendance();
            }

        } catch (error) {

            console.error(
                "Attendance error:",
                error
            );

            attendanceMessage.textContent =
                "Unable to connect to the server.";
        }
    }
);


// =====================================
// LOAD ATTENDANCE HISTORY
// =====================================

async function loadAttendance() {

    if (!user || !user.username) {
        return;
    }

    try {

        const response =
            await fetch(
                "/get-attendance?username="
                + encodeURIComponent(user.username)
            );

        const records =
            await response.json();

        attendanceTableBody.innerHTML = "";

        if (records.length === 0) {

            attendanceTableBody.innerHTML =
                `<tr>
                    <td colspan="3">
                        No attendance records found.
                    </td>
                </tr>`;

            return;
        }

        records.forEach(function (record) {

            const row =
                document.createElement("tr");

            row.innerHTML = `
                <td>${record.date}</td>
                <td>${record.status}</td>
                <td>
                    <span class="status-badge">
                        ${record.status}
                    </span>
                </td>
            `;

            attendanceTableBody.appendChild(row);
        });

    } catch (error) {

        console.error(
            "Unable to load attendance:",
            error
        );
    }
}

loadAttendance();