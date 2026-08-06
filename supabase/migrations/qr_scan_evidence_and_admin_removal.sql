-- ═══════════════════════════════════════════════════════════════════════════
-- Backend restructuring: QR-scan evidence (replaces photo evidence) +
-- restricted-Admin-role removal.
--
-- Run this once, directly in the Supabase SQL Editor, against the shared
-- Kalaza/Kalaza1 project. Frontend (both repos' `main`) already expects
-- these column names — this script makes the DB match.
--
-- Wrapped in one transaction: if anything fails partway, nothing commits.
-- ═══════════════════════════════════════════════════════════════════════════

begin;

-- ── 1. medications: photo columns → scanned-code columns ───────────────────
-- allotment_photo_url / administered_photo_url become the scanned-code
-- columns (straight rename, same TEXT data underneath a new name). The
-- *_expires_at columns are dropped outright — a scanned code is just text,
-- there's no retention window to track anymore.

alter table medications
  rename column allotment_photo_url to allotment_scanned_code;

alter table medications
  drop column if exists allotment_photo_expires_at;

alter table medications
  rename column administered_photo_url to administered_scanned_code;

alter table medications
  drop column if exists administered_photo_expires_at;

-- ── 2. medication_evidence_log: same treatment ──────────────────────────────

alter table medication_evidence_log
  rename column photo_url to scanned_code;

alter table medication_evidence_log
  drop column if exists expires_at;

-- ── 3. Unschedule the cleanup-photos cron job ───────────────────────────────
-- Nothing uploads to Storage anymore, so the 48h-retention sweep has nothing
-- left to clean up. Looked up by name rather than hardcoding a jobid, in
-- case it differs from what was assumed.

do $$
declare
  job record;
begin
  for job in select jobid, jobname from cron.job where jobname ilike '%cleanup-photos%' loop
    perform cron.unschedule(job.jobid);
    raise notice 'Unscheduled cron job % (jobid %)', job.jobname, job.jobid;
  end loop;
end $$;

-- ── 4. Remove Arti's restricted ADMIN staff row ─────────────────────────────
-- Same semantics as any other staff deletion in this app (StaffRepository.
-- deleteStaff): the linked Supabase Auth account is left orphaned, not
-- deleted — Auth deletion needs the service-role key, which isn't run from
-- client SQL. Harmless; matches existing behavior.
--
-- NOTE: if Arti ever performed an action that left a row referencing her
-- staff id elsewhere (audit_log.performed_by_id, medications.allotted_by_id,
-- etc.) and any of those columns carry a hard FK with no ON DELETE clause,
-- this DELETE will fail with a foreign-key-violation error instead of
-- silently doing something wrong. If that happens, tell me the exact error
-- and I'll adjust rather than force it through.

delete from staff where role = 'ADMIN';

commit;

-- ═══════════════════════════════════════════════════════════════════════════
-- NOT done by this script (can't be done in SQL):
--
-- 1. The `cleanup-photos` Edge Function itself is still deployed. Delete it
--    via the Supabase Dashboard (Edge Functions → cleanup-photos → Delete)
--    or `supabase functions delete cleanup-photos` from the CLI.
--
-- 2. The evidence Storage bucket (holding old uploaded photos, if any exist)
--    is left alone. Delete it from the Dashboard (Storage → the evidence
--    bucket) once you're sure nothing still needs those old photos.
--
-- 3. The Postgres `user_role` enum type still has 'ADMIN' as a technically
--    valid (now permanently unused) label — Postgres has no `DROP VALUE`
--    for enum types, only `ADD VALUE`. Removing it for real means creating
--    a new enum type, migrating every column off the old one, and dropping
--    the old type — high-risk for zero practical benefit since nothing will
--    ever insert 'ADMIN' again. Recommendation: leave it.
-- ═══════════════════════════════════════════════════════════════════════════
