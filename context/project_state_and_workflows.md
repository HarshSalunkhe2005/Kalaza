# Kalaza Care - Project State & Workflows

## Overview
Kalaza Care is an Android application designed for a clinic/hospital environment to manage patients, staff, medication (MAR), vitals, care notes, and doctor visits. The app incorporates a role-based access control system featuring Super Admins, a restricted photo-audit-only Admin, regular Staff, and Supervisor, with an intricate approval queue for staff-made edits and a two-checkpoint (allot → administer) medication workflow.

## Technology Stack
- **Platform:** Android (Min SDK 26, Target SDK 35)
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** MVVM (Model-View-ViewModel) with StateFlow
- **Backend:** Supabase — Postgres (via `postgrest-kt`) for all app data, Supabase Auth for login/staff accounts, Supabase Storage for photo evidence. Schema + Row Level Security policies live directly in the Supabase project (Dashboard/SQL Editor) — there is currently no `seed.sql` (or any schema-as-code file) checked into this repo; a prior version of this doc claimed one existed at the repo root, that was wrong.
- **Push Notifications:** Firebase Cloud Messaging only (kept independent of the rest of the backend — no Firestore/Firebase Auth/Firebase Storage remain in the project).

---

## What is Done (Completed Features)

### 1. Role-Based Access & Authentication (UI & Logic)
- **Super Admin Role** (`UserRole.SUPER_ADMIN` — this is the old, fully-privileged `ADMIN`, renamed): Has full access. Can view the Summary Tab, add new patients directly, add/revoke/delete staff members, approve/reject staff edit requests, and is the only role that can add/edit/delete MAR (medication) entries. `SessionManager.isAdmin()` checks this role — the name wasn't changed everywhere it's used, since the check's meaning ("today's fully-privileged admin") didn't change, only the enum's name did.
- **Admin Role** (`UserRole.ADMIN`, new, restricted): A completely separate, narrow role. On login it's routed straight to a standalone **Photo Audit** screen (`Routes.PHOTO_AUDIT`, no bottom nav) that lists every medicine allotment/administration evidence photo across all patients, read-only. No dashboard, no approvals, no staff config, no other access at all. `SessionManager.isPhotoAdmin()` checks this role.
- **Staff Role (Regular):** Has limited access. Cannot view the Summary Tab. Any edits to patient data generate an Approval Request instead of saving directly.
- **Supervisor Role:** Same dashboard and permissions as Regular Staff, plus an additional **Medicine** tab for allotting doses ahead of administration (see workflow below). MAR add/edit/delete is Super Admin-only — Supervisor cannot touch MAR entries directly, only the allotment round in the Medicine tab.
- **Login is by Name, not Email.** The login screen asks for the staff member's Name; `AuthRepository.login` matches `Staff.name` case-insensitively, looks up a synthetic per-staff email, and authenticates for real against Supabase Auth (hashed password, server-side). `Staff.email` still exists as a separate contact-info field (shown in Config), it's just never the login credential. Super Admin assigns each staff member's password at creation time (`StaffRepository.addStaff`).

### 2. Navigation & UI Shell
- **Bottom Navigation Bar:** Context-aware based on the logged-in user. (Super Admin sees: Patients, Approvals, Audit Log, Config, Summary. Supervisor sees: Patients, Medicine. Regular Staff sees: Patients. The restricted Admin role sees no bottom nav at all — it only ever has the single Photo Audit screen.)
- **Top App Bar:** Customized to display the brand's red stripe (`KalazaRed`), the app logo, dynamic screen titles, and a clickable Notification Bell. On the Patients/Dashboard tab, the subtitle line shows the logged-in staff member's own name instead of a static "Dashboard" label.

### 3. Patient Management
- **Dashboard:** Displays quick stats (total patients, pending meds, pending approvals) and a searchable list of patient cards. Reloads on resume so returning from another tab shows current data.
- **Patient Profile:**
  - **Details Tab:** View/Edit patient demographics and medical history, including the **Admission Date** (now an editable date picker, previously fixed at creation time). Staff edits go to the Approval Queue. Super Admin edits save immediately and log to Audit.
  - **Vitals Tab:** Record and view daily vitals (BP, Heart Rate, Temp, SpO2). Every role can edit an existing row via a pencil icon on that row: edits within 24h of the original entry apply directly (and are logged to Audit); edits made more than 24h after the entry go through the Approval Queue instead. Super Admin always edits directly.
  - **MAR (Medication Administration Record) Tab:** Track scheduled medications. Add/edit/delete of MAR entries is Super Admin-only. Every dose is either **recurring** (`isRecurring = true`, the default — due every day regardless of its stored date) or a **one-time dose** on a specific date (toggle + date picker in Add Medication). Overdue status is computed live on every read: for a one-time dose against its own stored date, for a recurring dose against *today's* date — and for a recurring dose, ALLOTTED/ADMINISTERED also resets to a fresh PENDING/OVERDUE view once its day has passed, so "given yesterday" doesn't suppress today's occurrence. Marking a dose "given" requires photo evidence, and shows whether the dose has been allotted yet; any staff can flag a "Request Allotment" if supervisor forgot.
  - **Utility Tab:** Log usage of medical utilities. Columns/fields are generated dynamically from whatever's configured in Config → Utility Items — adding a new item type there shows up here immediately, no code change needed. Row-level edit uses the same 24h-grace-then-approval policy as Vitals.
  - **Doctor Visits Tab:** Log specific instructions and notes left by visiting doctors, now including a visit **time** alongside the date. Visits can also be **deleted** — Super Admin deletes directly (logged to Audit); every other role's delete request goes through the Approval Queue first.
  - **Care Notes Tab:** Add general nursing/care notes for the patient, and edit an existing note via its pencil icon — same 24h-grace-then-approval policy as Vitals/Utility.
- **Medicine Tab (Supervisor only):** A facility-wide "rounds" view of every dose still awaiting allotment today, plus any pending allotment requests raised by regular staff. Allotting a dose requires photo evidence. This is unchanged by the MAR-CRUD restriction above — allotment rounds and MAR entry CRUD are separate concerns.
- **Photo Audit (restricted Admin role only):** A standalone, read-only screen (`ui/photoaudit/PhotoAuditScreen.kt`) listing every allotment/administration evidence photo across all patients — medicine name, patient, staff, timestamp, and whether the 48h retention window has expired. This is the *only* screen this role ever sees. Photo capture is real (device camera via `CameraCaptureFile` + `PhotoConfirmDialog`), uploaded to a **private** Supabase Storage bucket (`PhotoUploader`) — the screen renders each photo via a short-lived signed URL minted on demand, not a permanent public link. Data source is the permanent `medication_evidence_log` table (one row per allotment/administration event), not the `medications` table's own live fields — those reset daily for recurring doses, so reading from the log keeps this compliance record intact regardless. A scheduled Edge Function (`cleanup-photos`, hourly via `pg_cron`) actually deletes the underlying Storage object 48h after upload.

### 4. Admin Workflows
- **Approval Queue:** A dedicated screen where Admins can review, approve, or reject field-level changes requested by Staff. Approving applies the change directly to the Patient record (not just the request's status) and logs to Audit; rejecting also logs to Audit.
- **Audit Logs:** A read-only chronological log of all major actions (Patient Added, Patient Edited, Approvals, Rejections, Medication Allotted, Patient Archived), each with the correct icon for its action type.
- **Configuration / Staff Management:** Admins can add new staff (Regular or Medicine role), revoke existing staff, activate revoked staff, or delete staff entirely. Admins cannot revoke themselves.
- **Archive Patient:** From a patient's profile (overflow menu, Admin-only), Admin can archive a patient's record after confirming. Archived patients are hidden from the main Dashboard list by default; a "Show Archived" toggle on the Dashboard reveals them (marked with an "Archived" badge).

### 5. UI Polish & Theming
- Strict adherence to the `KalazaRed` and `KalazaDarkMaroon` color palette.
- Pixel-perfect empty states (e.g., "No Patients Found", "No Audit Logs").
- Elegant tab navigation within the Patient Profile. The tab pager's own swipe gesture is disabled (`userScrollEnabled = false`) so it no longer fights with the Vitals/Utility tables' sideways scroll — tabs are still switchable by tapping.
- Staff cards in Config never squeeze the name/role badge regardless of state — actions (Revoke, or Activate+Delete) live on their own footer row instead of competing for space in the header.

### 6. In-App Notification System — fully live, including real push
- A real Notifications screen (bell icon → badge count → list), reachable from Dashboard and the Medicine tab.
- Notifications are generated at the actual point of the event, not just seeded: a staff edit request notifies all Admins; an approval/rejection notifies the requester; a supervisor allotment request notifies all Supervisors; fulfilling one notifies the requester back. Tapping a notification marks it read and navigates to the relevant screen (Approval Queue, Medicine tab, or the specific patient's profile).
- **Push delivery is fully wired, both sides.** Client side: `KalazaMessagingService` requests the `POST_NOTIFICATIONS` permission, registers/refreshes the device's FCM token to the staff row, displays a real system notification (foreground/background/killed, all states), and deep-links a tap back into the right screen via `MainActivity.onNewIntent`. Server side: a Supabase Database Webhook fires the `send-push` Edge Function on every new `notifications` row, which resolves recipient(s) by `recipient_staff_id`/`recipient_role`, mints an FCM v1 OAuth token from a Cloud-Messaging-scoped service account, and sends a data-only push. Confirmed working end to end, app closed included.
- **Medication deadline reminders & escalation** (`supabase/functions/medication-watchdog`, scheduled every minute via `pg_cron`): 15 min before a dose's deadline, Staff + Supervisor get a reminder; 5 min after, Admin gets a missed-dose alert; 10 min after, Super Admin gets an escalation. Each checkpoint fires at most once per (IST) calendar day per dose, tracked via `reminder_sent_at`/`admin_alert_sent_at`/`superadmin_alert_sent_at` on `medications`.
- **Real-time sync:** Dashboard, Approval Queue, Medicine tab, and the Notifications screen all subscribe to Supabase Realtime on their underlying tables (`patients`, `medications`, `approval_requests`, `allotment_requests`, `notifications`) and refetch automatically on any change — no more waiting to navigate away and back to see another staff member's update.

### 7. Input Validation
- Phone numbers (staff phone, patient emergency phone) only accept digits as typed and require exactly 10 before the form can submit.
- Patient age must be between 1 and 120.
- Staff email is validated against a standard email pattern before Admin can add them.
- Vitals fields (pulse, BP, SpO2, sugar) only accept digits; temperature accepts digits and a decimal point. All vitals fields are also range-checked (e.g. pulse 30–220, SpO2 0–100, temperature 90–110°F) with inline error text — an out-of-range value blocks Save.
- Utility quantities only accept digits.
- Staff names are trimmed before being stored, and login matching trims and case-folds the name, so trailing/leading whitespace never blocks a valid login.
- Medications can still be scheduled before a patient's admission date (there are legitimate backdating reasons), but doing so now shows a warning Toast instead of silently accepting it.

### 8. Doctor Visit Editing & Generalized Approval
- Doctor visits are editable and deletable by any role. Super Admin edits/deletes apply directly (+ Audit Log entry); Staff/Supervisor edits/deletes generate `ApprovalRequest`s (field-level diffs for edits, a single delete-flagged request for deletes) routed to the Approval Queue.
- `ApprovalRequest` now carries an `entityType` (`PATIENT`, `DOCTOR_VISIT`, `VITAL`, `UTILITY`, or `CARE_NOTE`), an `entityId`, and an `action` (`EDIT` or `DELETE`), so `ApprovalViewModel.approve()` knows which repository to apply the diff to and whether to delete or patch the record.
- Vitals, Utility records, and Care Notes are now also editable by every role, via the row-level pencil icon on each entry. These three follow a **24h grace window**: edits made within 24h of the original entry's timestamp apply directly for any role (mistakes happen — all such edits are still logged to Audit); edits made after 24h route through the Approval Queue like everything else. Super Admin always edits directly regardless of age. There is currently no delete UI for Vitals/Utility/Care Notes (edit-only, matching what was asked for) — a follow-up if delete is ever needed there too.

### 9. Time Input
- All medication scheduling (Add Medication, Edit Medication) now uses a 12-hour HH:MM + AM/PM picker (`TimeOfDayField`) instead of raw 24-hour text fields, while still storing/computing everything internally as 24-hour `LocalTime`.

### 10. Patient Profile Robustness
- Editing a patient whose data hasn't finished loading (e.g. a stale/bad deep link) no longer risks a `NullPointerException` — Save is disabled and blocked with a "still loading" message until the patient record is actually present.
- A patient profile for a non-existent ID now shows a "Patient not found" state with a Go Back button, instead of spinning forever indistinguishably from a real loading state.
- `MedicationEntry`'s live-computed OVERDUE/PENDING status is reversible in both directions — editing a dose's time to a later slot un-overdues it instead of leaving it stuck OVERDUE forever. Verified by trace: this self-heals on every subsequent read regardless of what gets persisted mid-edit, so no residual bug remains here.

### 11. Role Restructure: Super Admin + Photo-Audit-Only Admin
- `UserRole.ADMIN` was renamed to `UserRole.SUPER_ADMIN` (keeps every existing admin power — `SessionManager.isAdmin()` now checks `SUPER_ADMIN`). A brand new, much more restricted `UserRole.ADMIN` was added: on login it goes straight to a standalone Photo Audit screen (`Routes.PHOTO_AUDIT`) and has no other access anywhere in the app — no dashboard, no approvals, no config, no bottom nav.
- `StaffEditor`'s "Add Staff" role picker now excludes only `SUPER_ADMIN` (was excluding the old `ADMIN`) — Staff/Supervisor/Admin are all assignable through Config, Super Admin accounts aren't created through this dialog.
- `RoleBadge` gained a distinct color for the new `ADMIN` role so it's visually distinguishable from `SUPER_ADMIN` in staff lists.

### 12. MAR (Medication) Fixes & Delete
- MAR add/edit/delete is Super Admin-only (`SessionManager.isAdmin()`); a delete action (with confirmation dialog) was added next to the existing edit pencil — there was previously no way to remove a MAR entry at all.
- `TimeOfDayField`'s AM/PM `FilterChip`s previously left the selected chip's label using the theme's default (sometimes low-contrast) color; both chips now use an explicit `selectedLabelColor`/`labelColor` pair for reliable contrast in both states.
- `TimeOfDayField`'s HH/MM text fields now clamp live as you type (HH to 1–12, MM to 0–59) instead of only clamping the emitted value while letting the displayed text show something out of range.

### 13. Summary Report: Date Range + Per-Patient xlsx Export
- The Summary screen takes a **date range** (start + end date pickers); `SummaryViewModel.load(start, end)` aggregates stats and per-patient breakdowns across the whole range, `buildRangeReport()` returns the raw per-patient data for export.
- **(Superseded) Originally exported one combined multi-sheet workbook** (a Summary tab + one tab per patient). **Replaced** with **N separate styled `.xlsx` files, one per patient**, matching a reference "Simple Patient Report" template the team supplied: a dark navy title banner (patient name + date range), a blue column-header row (Date / Diagnosis / Medication / Vitals / Utilities / Notes / Signed By), alternating white/light-blue row banding, wrapped text, and frozen header rows. One row per calendar date in the selected range.
- `XlsxWriter` (`util/XlsxWriter.kt`) was rebuilt from a plain unstyled writer into one that emits real OOXML styling (`styles.xml`: fonts, fills, cellXfs), merged cells, column widths, row heights, and frozen panes — still dependency-free, no Apache POI. Its API is now purpose-built (`buildPatientReport(patientName, rangeLabel, rows)`) rather than a generic multi-sheet writer, since only this one report shape is ever produced.
- **Known modeling limitation, carried over from the data model (see "What is Remaining #1" below):** recurring medications have no true per-day history (their live status/administeredBy reset daily), so a recurring dose's status is only accurate for *today* — it's shown only on today's row in the report, not repeated across every day in a past range. One-time doses use their real, fixed `scheduledDate` regardless of range position.
- Each file is saved straight to the device's **Downloads** folder via `DownloadsSaver` (`util/DownloadsSaver.kt`) — no share sheet, no "send to" step, no zip bundling.

### 14. Wi-Fi-Scoped Login Gate
- Login is blocked unless the device's connected Wi-Fi network is on an allow-list, matched by the network's **gateway/router IP** (`WifiChecker.currentWifiGatewayIp`, via `ConnectivityManager`/`LinkProperties`), not by SSID — SSID reading proved unreliable across recent Android versions/OEMs (kept coming back redacted despite every documented permission being granted). `ALLOWED_GATEWAY_IPS` in `WifiChecker.kt` holds the facility's known router IPs.
- A visible "Skip Wi-Fi check (testing)" switch exists on the login screen's blocking dialog, intentionally left in for now — **known, accepted gap**: any staff member can currently bypass the network gate with it. Remove or otherwise lock this down before final handover (see Security Hardening Backlog below).
- Deferred (explicitly, not started): a way for staff/Super Admin to authenticate off-network during hospital visits — leaning toward Super-Admin-issued temporary access codes as the approach, but **intentionally not built yet** — scheduled for a later pass.

### 15. Super Admin Landing Screen: "Today" Overview
- Super Admin's post-login landing screen (`SuperAdminOverviewScreen.kt`) was first built as a tabbed Today/Weekly-Report/Utilities/Patient-Details view, then simplified down to a single, denser "Today" view per product direction — those other three tabs were removed outright, not just hidden. Current screen: stat cards, a "Needs Your Attention" section listing pending Approval/Allotment requests in full (not just a count), and a Daily Breakdown (By Category incl. Doctor Visits / By Patient toggle).
- Staff/Supervisor now land on a medicine-focused **Todo List** screen instead of the Patients dashboard (`ui/todo/TodoListScreen.kt`) — a flat, time-sorted list of today's medication tasks.
- Top app bar was rebuilt as a plain, explicitly-sized `Row` instead of Material3's `TopAppBar` (`KalazaTopBar.kt`) — the latter enforces its own fixed ~64dp minimum height regardless of content, which is why an earlier attempt to resize the logo/title didn't visibly change the bar's height at all.

### 16. Performance: Batched Queries
- `DailySummaryViewModel.load()` used to do 1 + 3×N sequential Supabase round-trips (three per-patient queries, looped over every patient) to build the Super Admin "Today" view — now a fixed 4 queries regardless of patient count, via `getVitalsForDate`/`getUtilityForDate`/`getVisitsForDate` (one query across all patients, grouped client-side).
- `ApprovalRepository`/`AllotmentRequestRepository.getPendingRequests()` and `NotificationRepository`'s unread-count/mark-all-read used to fetch the entire table's history and filter in Kotlin — now filtered server-side (`eq("status", ...)` / `eq("is_read", false)`), and `markAllReadForRecipient` is a single UPDATE instead of one per row. This mattered more than the query above long-term: it scaled with *time* (rows accumulate forever), not patient count.

### 17. RLS Policy Fixes (from a full policy review against actual app call sites)
- `doctor_visits_insert` was `is_super_admin()`-only, but the UI (`DoctorVisitsTab.kt`) deliberately lets every role schedule a visit — Staff/Supervisor tapping the FAB was silently failing (no try/catch on `addVisit()` either). Relaxed to `is_active_staff()` to match the UI's actual, intended behavior.
- `notifications_insert` was `is_active_staff()`-only with no check on *which* notification type was being sent — any staff account could insert a fake notification (e.g. a spoofed "approved by Somnath") addressed to anyone. Tightened per-type to match exactly which role actually triggers each one in the app: `APPROVAL_REQUESTED`/`ALLOTMENT_REQUESTED` (any active staff, they self-submit), `ALLOTMENT_FULFILLED` (Supervisor+), `APPROVAL_APPROVED`/`APPROVAL_REJECTED` (Super Admin only).
- Full policy set otherwise checked out clean against real insert/update/delete call sites (`patients`, `medications`, `staff`, `approval_requests`, `allotment_requests`, `audit_log`, `utility_items` all correctly gated and matched by the app's own role checks).

### 18. Mock Data
- A comprehensive 5-patient mock dataset exists (delivered as standalone SQL, not committed to the repo — run manually in the Supabase SQL Editor) covering every table, with distinct staff actors on each side of every allotment/administration/approval chain (no self-contradictory transactions). Required creating 3 additional real Supabase Auth-backed staff accounts (`staff.id` has a hard FK to `auth.users`, so a plain SQL insert into `staff` alone isn't possible) — Priya Deshmukh (Supervisor), Ramesh Kumar (Staff), Sunita Patil (Staff), shared password `KalazaStaff@123`.

---

## Security Hardening Backlog (deferred until UI/functionality reach prod level — do NOT start early per explicit instruction)

- **No inactivity/auto-logout timer.** Session persists on-device indefinitely once logged in; no timeout, no re-auth prompt. Matters for a shared/facility device left unlocked and unattended.
- **`android:allowBackup="true"` with no `dataExtractionRules`/`fullBackupContent` exclusions**, combined with the Supabase Auth plugin's default (likely unencrypted `SharedPreferences`-backed) session storage — auth tokens could plausibly ride along in an Android cloud backup / device-to-device transfer. Likely fix: `allowBackup="false"`, optionally pair with an encrypted session-storage backend for the Auth plugin.
- **No client-side password policy** when Super Admin creates a staff account (`addStaff()` passes whatever's typed straight to Supabase Auth) — whatever protection exists is whatever the Supabase project's own Auth password policy is set to (unverified from this repo).
- **No `FLAG_SECURE`** on patient-data screens — screenshots/screen recording of patient medical/personal info aren't currently blocked by the OS. Optional, lower priority.
- **No release signing config** (`app/build.gradle.kts` has no `signingConfigs` block) — a properly signed, distributable release APK/AAB can't be produced yet.
- **`isMinifyEnabled = false`** on the release build type — no code shrinking/obfuscation.
- **No automated tests** exist (unit or instrumentation) — every future change currently has zero automated safety net.
- **`google-services.json` is tracked in git history** despite being gitignored now (committed before the ignore rule existed) — contains a Firebase Android API key GitHub's Secret Scanning flags (low real risk, Android Firebase keys aren't meant to be secret, but worth a `git rm --cached` cleanup pass eventually).
- **GitHub PAT pasted in chat during this project's development** needs revoking once active development against this repo is done.
- **Excel/Summary-sheet format changes** — requested, parked; no format spec given yet.
- **Off-network (no-Wi-Fi) staff/Super-Admin login during hospital visits** — leaning toward Super-Admin-issued temporary access codes; not started.

---

## What is Remaining (Future Scope)

Backend integration, real authentication, push notification delivery, photo evidence auto-deletion, and real-time sync (all previously listed here as open) are now **done** — see Technology Stack and section 1 above, and "Push Notification System" and "Medication Deadline Reminders & Escalation" below. What's actually left:

### 1. Per-Day Medication Administration History (Medium Priority)
- A recurring dose's live PENDING/OVERDUE/ADMINISTERED status now correctly resets each day (see `MedicationRepository.withComputedStatus`), and every allotment/administration photo event is separately preserved forever in `medication_evidence_log` for Photo Audit. What's still a flat, single-row model is the *live* `medications` row itself — there's no per-day history table for it beyond the evidence log, so questions like "show me every day this dose was given over the last month" aren't answerable from the `medications` table alone (only from the evidence log, and only for doses that had photo evidence).
- **Action Required:** decide if a dedicated daily-administration-log table (mirroring `medication_evidence_log` but for every administration, not just photographed ones) is worth adding.

### 2. Test Account Coverage (Low Priority)
- Seed data currently only has Super Admin (Somnath) and Admin (Arti) accounts. There's no seeded `STAFF` or `SUPERVISOR` login, so the restricted-permission paths (approval requests, allotment requests, RLS column-restriction triggers) haven't been exercised end-to-end with a non-admin account yet. Explicitly deferred by the team — "testing will be done later."

### 3. Offline Support / Caching (Low Priority)
- Not implemented. A Room-based local cache was attempted and reverted (it required a Gradle 8→9 and AGP 8→9 jump just to get its annotation processor working, which was far more disruptive than the feature warranted) — worth revisiting on its own, in isolation, with a properly pinned KSP/Room version first.

### 4. Security Hardening & Wi-Fi-Scoped Auth (Next Up)
- Explicitly called out by the team as the remaining phase before this is considered feature-complete: a further security pass, plus restricting login/access to the facility's own Wi-Fi network.

---

## Detailed Workflows

### Staff Editing a Patient Workflow
1. Staff logs in and navigates to the Patient Profile -> Details Tab.
2. Staff modifies a field (e.g., Room Number) and clicks "Save".
3. `PatientViewModel` detects the role is Staff. Instead of updating the repository, it generates an `ApprovalRequest` containing the `oldValue` and `newValue`.
4. A Toast confirms to the Staff that the request was submitted.
5. The Admin receives a pending approval in their Approval Queue.

### Admin Approving a Request Workflow
1. Admin opens the Approval Queue.
2. Clicks on the pending request for the Room Number change.
3. Clicks "Approve".
4. `ApprovalRepository` marks the request as APPROVED and logs the reviewer's id/name.
5. `ApprovalViewModel` applies the new Room Number directly to the actual Patient record via `PatientRepository.updatePatient`.
6. An `AuditLogEntry` ("Edit Request Approved") is generated. Rejecting a request similarly logs an "Edit Request Rejected" entry, without touching the Patient record.

### Admin Archiving a Patient Workflow
1. Admin opens a patient's profile and taps the overflow menu (⋮) next to Edit.
2. Taps "Archive Patient" and confirms in the dialog.
3. `PatientRepository.archivePatient` flags the patient as archived; an `AuditLogEntry` ("Patient Archived") is generated, and the app navigates back to the Dashboard.
4. The patient no longer appears in the default Dashboard list or search results. Toggling "Show Archived" (Admin-only) reveals them again, tagged with an "Archived" badge.

### Staff Management Workflow
1. Admin navigates to the Config tab.
2. To remove a staff member temporarily: Clicks "Revoke". The staff member is flagged as inactive and cannot log in.
3. To restore a staff member: Admin clicks "Activate" on the revoked card.
4. To remove permanently: Admin clicks "Delete", destroying the record.
5. The Admin's own card omits the "Revoke" button to prevent locking themselves out.
6. When adding a staff member, Admin picks between the three operational roles — Regular Staff, Supervisor, or the restricted photo-audit Admin. (Super Admin accounts aren't created through this dialog.)

### Utility Item Workflow
1. Admin adds/removes item types in Config → Utility Items (e.g. "Syringes").
2. Any patient's Utility tab immediately reflects the change: the "Add Utility Record" dialog renders one quantity field per active item, and the table gains/loses that column — no other code path needs updating.
3. A `UtilityRecord` stores quantities as a `Map<UtilityItem.id, Int>` rather than fixed fields, which is what makes this possible.

### Notification Workflow
1. A real event happens (staff submits an edit request, Admin approves/rejects one, a regular staff flags a forgotten allotment, Supervisor fulfills that flag).
2. The relevant ViewModel calls `NotificationRepository.add(...)` with either a specific `recipientStaffId` or a broadcast `recipientRole`.
3. Whoever's affected sees the bell badge update (Dashboard and Medicine tab both show it) and can open the Notifications screen.
4. Tapping a notification marks it read and navigates to its `targetRoute` (a static route like "approval"/"medicine", or "patient/{id}").

### Medication Allotment Workflow (Supervisor)
1. Supervisor opens the Medicine tab, showing every dose across all patients still awaiting allotment today, sorted by scheduled time.
2. Supervisor taps "Allot" on a dose, takes a photo as evidence, and confirms.
3. `MedicationRepository.allotMedication` records who allotted it, when, and the photo evidence (mock URL + 48h expiry), and an Audit Log entry ("Medication Allotted") is created.
4. Whoever ultimately administers the dose (Regular or Supervisor) marks it "Given" from the patient's MAR tab, which also requires a photo, independently of the allotment checkpoint.
5. If Supervisor forgets to allot a dose ahead of time, any staff member can tap "Request Allotment" on that dose in the MAR tab. This creates an `AllotmentRequest` that surfaces at the top of the Medicine tab (standing in for a push notification) until a Supervisor fulfills it.
