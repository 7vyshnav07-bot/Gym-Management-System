const sections = [
    { key: "members", body: "membersRows", columns: ["full_name", "username", "email", "phone"], action: "Edit", editable: true, deletable: true },
    { key: "memberships", body: "membershipsRows", columns: ["username", "plan_name", "start_date", "end_date", "status"], action: "Edit", editable: true },
    { key: "plans", body: "plansRows", columns: ["plan_name", "duration_months", "price", "description", "features"], action: "Edit", editable: true, deletable: true },
    { key: "trainerRecords", body: "trainersRows", columns: ["trainer_name", "specialization", "experience_years", "phone", "availability"], action: "Edit", editable: true, deletable: true },
    { key: "trainerRequests", body: "trainerRequestsRows", columns: ["username", "trainer_name", "request_date", "status"], action: "Review", editable: true },
    { key: "workouts", body: "workoutsRows", columns: ["username", "fitness_goal", "experience_level", "days_per_week", "created_date", "status"], action: "Edit status", editable: true, deletable: true },
    { key: "paymentRecords", body: "paymentsRows", columns: ["username", "amount", "payment_date", "payment_method", "payment_status"] },
    { key: "attendanceRecords", body: "attendanceRows", columns: ["username", "attendance_date", "status"] }
];

const fields = {
    members: [
        { key: "full_name", label: "Full name", required: true },
        { key: "username", label: "Username", required: true, readOnlyOnEdit: true },
        { key: "email", label: "Email", type: "email", required: true },
        { key: "phone", label: "Phone", required: true },
        { key: "date_of_birth", label: "Date of birth", type: "date" },
        { key: "country", label: "Country" },
        { key: "gender", label: "Gender" },
        { key: "password", label: "Initial / new password", type: "password", createRequired: true, hint: "Leave blank when editing to keep the current password." }
    ],
    trainers: [
        { key: "trainer_name", label: "Trainer name", required: true },
        { key: "specialization", label: "Specialization", required: true },
        { key: "experience_years", label: "Years of experience", type: "number", min: 0, max: 80, required: true },
        { key: "phone", label: "Phone" },
        { key: "availability", label: "Availability", required: true },
        { key: "description", label: "Description", type: "textarea" }
    ],
    plans: [
        { key: "plan_name", label: "Plan name", required: true },
        { key: "duration_months", label: "Duration (months)", type: "number", min: 1, max: 120, required: true },
        { key: "price", label: "Price (INR)", type: "number", min: 0, step: "0.01", required: true },
        { key: "description", label: "Description", type: "textarea" },
        { key: "features", label: "Features", type: "textarea", hint: "Separate features with commas." }
    ],
    memberships: [
        { key: "username", label: "Member username", required: true },
        { key: "plan_id", label: "Membership plan", type: "plan", required: true },
        { key: "start_date", label: "Start date", type: "date", required: true },
        { key: "end_date", label: "End date", type: "date", required: true },
        { key: "status", label: "Status", type: "select", options: ["Active", "Pending", "Expired", "Cancelled"], required: true }
    ],
    workouts: [{ key: "status", label: "Workout status", type: "select", options: ["Active", "Completed", "Paused", "Cancelled"], required: true }],
    trainerRequests: [{ key: "status", label: "Request status", type: "select", options: ["Pending", "Approved", "Rejected"], required: true }]
};

const state = { data: {} };
const errorBox = () => document.getElementById("dashboardError");

async function loadDashboard() {
    const error = errorBox();
    error.hidden = true;
    try {
        const response = await fetch("./admin/overview", { credentials: "same-origin", cache: "no-store" });
        if (response.status === 401) { location.href = "admin-login.html"; return; }
        if (!response.ok) throw new Error("Unable to load admin data. Please refresh and try again.");
        const data = await response.json();
        state.data = data;
        setText("totalUsers", data.users);
        setText("activeMemberships", data.activeMemberships);
        setText("totalTrainers", data.trainers);
        setText("totalWorkoutPlans", data.workoutPlans);
        setText("totalPayments", data.payments);
        setText("totalAttendance", data.attendance);
        if (data.adminUsername) setText("adminName", data.adminUsername);
        for (const section of sections) renderRows(section, data[section.key] || []);
        setText("lastUpdated", `Updated ${new Date().toLocaleTimeString()}`);
    } catch (e) {
        error.textContent = e.message || "Unable to load admin data.";
        error.hidden = false;
    }
}

function setText(id, value) {
    const node = document.getElementById(id);
    if (node) node.textContent = value ?? 0;
}

function formatValue(key, value) {
    if (value == null || value === "") return "—";
    if (key === "price" || key === "amount") {
        const number = Number(value);
        if (!Number.isNaN(number)) return new Intl.NumberFormat("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 2 }).format(number);
    }
    if (key === "experience_years") return `${value} years`;
    if (key === "duration_months") return `${value} months`;
    return String(value);
}

function renderRows(section, rows) {
    const body = document.getElementById(section.body);
    body.replaceChildren();
    const count = document.querySelector(`[data-count="${section.key}"]`);
    if (count) count.textContent = `${rows.length} records`;
    if (!rows.length) {
        const tr = document.createElement("tr");
        const td = document.createElement("td");
        td.colSpan = section.columns.length + (section.editable || section.deletable ? 1 : 0);
        td.className = "empty";
        td.textContent = "No records found.";
        tr.append(td);
        body.append(tr);
        return;
    }
    rows.forEach((row, index) => {
        const tr = document.createElement("tr");
        for (const column of section.columns) {
            const td = document.createElement("td");
            td.textContent = formatValue(column, row[column]);
            tr.append(td);
        }
        if (section.editable) {
            const td = document.createElement("td");
            const button = document.createElement("button");
            button.type = "button";
            button.className = "row-action";
            button.textContent = section.action;
            button.dataset.edit = section.key;
            button.dataset.index = String(index);
            td.append(button);
            if (section.deletable) {
                const remove = document.createElement("button");
                remove.type = "button";
                remove.className = "row-action danger-action";
                remove.textContent = "Remove";
                remove.dataset.remove = section.key;
                remove.dataset.index = String(index);
                td.append(remove);
            }
            tr.append(td);
        }
        body.append(tr);
    });
}

function fieldNode(definition, value, create) {
    const wrapper = document.createElement("label");
    wrapper.className = "field";
    const caption = document.createElement("span");
    caption.textContent = definition.label;
    wrapper.append(caption);
    let input;
    if (definition.type === "textarea") {
        input = document.createElement("textarea");
        input.rows = 3;
    } else if (definition.type === "select" || definition.type === "plan") {
        input = document.createElement("select");
        const options = definition.type === "plan"
            ? (state.data.plans || []).map(p => [String(p.id), `${p.plan_name} · ${formatValue("price", p.price)} / ${p.duration_months} mo`])
            : definition.options.map(v => [v, v]);
        for (const [optionValue, optionText] of options) {
            const option = document.createElement("option");
            option.value = optionValue;
            option.textContent = optionText;
            input.append(option);
        }
        if (value != null && value !== "" && !Array.from(input.options).some(option => option.value === String(value))) {
            const current = document.createElement("option");
            current.value = String(value);
            current.textContent = `${value} (current)`;
            input.append(current);
        }
    } else {
        input = document.createElement("input");
        input.type = definition.type || "text";
        if (definition.min != null) input.min = String(definition.min);
        if (definition.max != null) input.max = String(definition.max);
        if (definition.step) input.step = definition.step;
    }
    input.name = definition.key;
    input.value = definition.key === "password" ? "" : (value ?? "");
    input.required = Boolean(definition.required || (create && definition.createRequired));
    if (!create && definition.readOnlyOnEdit) input.readOnly = true;
    if (definition.hint) {
        const hint = document.createElement("small");
        hint.textContent = definition.hint;
        wrapper.append(input, hint);
    } else wrapper.append(input);
    return wrapper;
}

function openEditor(entity, record = null) {
    const create = record == null;
    const specs = fields[entity];
    if (!specs) return;
    if (entity === "memberships" && !(state.data.plans || []).length) {
        showError("Create a membership plan before assigning one.");
        return;
    }
    const dialog = document.getElementById("recordDialog");
    const fieldContainer = document.getElementById("formFields");
    fieldContainer.replaceChildren();
    document.getElementById("dialogTitle").textContent = `${create ? "Add" : "Edit"} ${entity === "trainerRequests" ? "trainer request" : entity === "plans" ? "membership plan" : entity.replace(/([A-Z])/g, " $1").toLowerCase()}`;
    const error = document.getElementById("formError"); error.hidden = true;
    if (!create && record.id != null) {
        const id = document.createElement("input"); id.type = "hidden"; id.name = "id"; id.value = record.id; fieldContainer.append(id);
    }
    if (entity === "members" && !create) {
        const original = document.createElement("input"); original.type = "hidden"; original.name = "original_username"; original.value = record.username; fieldContainer.append(original);
    }
    for (const spec of specs) fieldContainer.append(fieldNode(spec, record?.[spec.key], create));
    const form = document.getElementById("recordForm");
    form.dataset.entity = entity;
    form.dataset.action = create ? "create" : "update";
    const saveButton = form.querySelector("button[type=submit]");
    saveButton.textContent = create ? "Create" : "Save changes";
    dialog.showModal();
}

function showError(message) {
    const box = errorBox(); box.textContent = message; box.hidden = false; box.scrollIntoView({ behavior: "smooth", block: "center" });
}

async function saveRecord(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const message = document.getElementById("formError");
    message.hidden = true;
    const payload = new URLSearchParams(new FormData(form));
    payload.set("action", form.dataset.action);
    try {
        const response = await fetch(`./admin/manage/${encodeURIComponent(form.dataset.entity)}`, {
            method: "POST", credentials: "same-origin", body: payload
        });
        const result = await response.json();
        if (response.status === 401) { location.href = "admin-login.html"; return; }
        if (!response.ok || !result.success) throw new Error(result.message || "Unable to save this change.");
        document.getElementById("recordDialog").close();
        await loadDashboard();
    } catch (e) {
        message.textContent = e.message || "Unable to save this change.";
        message.hidden = false;
    }
}

async function removeRecord(entity, record) {
    const title = entity === "members" ? `member ${record.username}` : `${entity.replace(/([A-Z])/g, " $1").toLowerCase()} record`;
    if (!confirm(`Remove this ${title}? This action cannot be undone.`)) return;
    const payload = new URLSearchParams({ action: "delete" });
    if (entity === "members") payload.set("original_username", record.username);
    else payload.set("id", record.id);
    try {
        const response = await fetch(`./admin/manage/${encodeURIComponent(entity)}`, {
            method: "POST", credentials: "same-origin", body: payload
        });
        const result = await response.json();
        if (response.status === 401) { location.href = "admin-login.html"; return; }
        if (!response.ok || !result.success) throw new Error(result.message || "Unable to remove this record.");
        await loadDashboard();
    } catch (e) {
        showError(e.message || "Unable to remove this record.");
    }
}

document.addEventListener("DOMContentLoaded", () => {
    loadDashboard();
    document.getElementById("refresh").addEventListener("click", loadDashboard);
    document.getElementById("logout").addEventListener("click", async () => {
        try { await fetch("./admin/logout", { method: "POST", credentials: "same-origin" }); }
        finally { location.href = "admin-login.html"; }
    });
    document.getElementById("recordForm").addEventListener("submit", saveRecord);
    document.getElementById("dialogClose").addEventListener("click", () => document.getElementById("recordDialog").close());
    document.getElementById("dialogCancel").addEventListener("click", () => document.getElementById("recordDialog").close());
    document.querySelectorAll("[data-create]").forEach(button => button.addEventListener("click", () => openEditor(button.dataset.create)));
    document.addEventListener("click", event => {
        const remove = event.target.closest("[data-remove]");
        if (remove) {
            const record = state.data[remove.dataset.remove]?.[Number(remove.dataset.index)];
            if (record) removeRecord(remove.dataset.remove, record);
            return;
        }
        const button = event.target.closest("[data-edit]");
        if (!button) return;
        const row = state.data[button.dataset.edit]?.[Number(button.dataset.index)];
        if (row) openEditor(button.dataset.edit, row);
    });
    document.querySelectorAll("#adminNav a").forEach(link => link.addEventListener("click", () => {
        document.querySelectorAll("#adminNav a").forEach(item => item.classList.remove("active"));
        link.classList.add("active");
    }));
});
