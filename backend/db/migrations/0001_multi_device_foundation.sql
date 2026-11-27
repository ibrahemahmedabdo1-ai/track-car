-- Track Car multi-device compatibility foundation
-- Apply to a development branch first. This migration is additive and does not remove existing data.

ALTER TABLE public.devices
  ADD COLUMN IF NOT EXISTS manufacturer text,
  ADD COLUMN IF NOT EXISTS model text,
  ADD COLUMN IF NOT EXISTS device_codename text,
  ADD COLUMN IF NOT EXISTS android_release text,
  ADD COLUMN IF NOT EXISTS sdk_int integer,
  ADD COLUMN IF NOT EXISTS build_fingerprint text,
  ADD COLUMN IF NOT EXISTS security_patch text,
  ADD COLUMN IF NOT EXISTS cpu_abis jsonb NOT NULL DEFAULT '[]'::jsonb,
  ADD COLUMN IF NOT EXISTS root_state text NOT NULL DEFAULT 'unknown',
  ADD COLUMN IF NOT EXISTS root_manager text,
  ADD COLUMN IF NOT EXISTS installer_version text,
  ADD COLUMN IF NOT EXISTS agent_version text,
  ADD COLUMN IF NOT EXISTS capabilities jsonb NOT NULL DEFAULT '{}'::jsonb,
  ADD COLUMN IF NOT EXISTS enrollment_state text NOT NULL DEFAULT 'pending',
  ADD COLUMN IF NOT EXISTS updated_at timestamptz NOT NULL DEFAULT now();

CREATE INDEX IF NOT EXISTS devices_manufacturer_model_idx
  ON public.devices (manufacturer, model);
CREATE INDEX IF NOT EXISTS devices_sdk_int_idx
  ON public.devices (sdk_int);
CREATE INDEX IF NOT EXISTS devices_enrollment_state_idx
  ON public.devices (enrollment_state);

CREATE TABLE IF NOT EXISTS public.device_profiles (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  profile_key text NOT NULL UNIQUE,
  manufacturer text NOT NULL,
  model_pattern text NOT NULL,
  min_sdk integer NOT NULL DEFAULT 21,
  max_sdk integer,
  required_capabilities jsonb NOT NULL DEFAULT '{}'::jsonb,
  root_method_id text,
  installer_channel text NOT NULL DEFAULT 'stable',
  agent_min_version text,
  status text NOT NULL DEFAULT 'testing',
  notes text,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.app_releases (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  app_id text NOT NULL CHECK (app_id IN ('admin', 'installer', 'agent')),
  version_name text NOT NULL,
  version_code integer NOT NULL,
  min_sdk integer NOT NULL DEFAULT 21,
  download_url text NOT NULL,
  sha256 text NOT NULL,
  signing_cert_sha256 text NOT NULL,
  release_channel text NOT NULL DEFAULT 'stable',
  status text NOT NULL DEFAULT 'draft',
  created_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE (app_id, version_code, release_channel)
);

CREATE TABLE IF NOT EXISTS public.device_events (
  id bigserial PRIMARY KEY,
  device_id uuid NOT NULL REFERENCES public.devices(id) ON DELETE CASCADE,
  event_type text NOT NULL,
  event_payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS device_events_device_time_idx
  ON public.device_events (device_id, created_at DESC);

CREATE TABLE IF NOT EXISTS public.device_commands (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  device_id uuid NOT NULL REFERENCES public.devices(id) ON DELETE CASCADE,
  command_type text NOT NULL CHECK (command_type IN ('request_location_sync', 'refresh_config', 'request_diagnostics', 'start_remote_support')),
  payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  status text NOT NULL DEFAULT 'queued' CHECK (status IN ('queued', 'delivered', 'acknowledged', 'completed', 'failed', 'expired', 'cancelled')),
  requested_by uuid,
  requested_at timestamptz NOT NULL DEFAULT now(),
  expires_at timestamptz NOT NULL DEFAULT (now() + interval '10 minutes'),
  acknowledged_at timestamptz,
  completed_at timestamptz,
  result jsonb
);
CREATE INDEX IF NOT EXISTS device_commands_device_status_idx
  ON public.device_commands (device_id, status, requested_at DESC);

CREATE TABLE IF NOT EXISTS public.remote_support_sessions (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  device_id uuid NOT NULL REFERENCES public.devices(id) ON DELETE CASCADE,
  requested_by uuid,
  reason text NOT NULL,
  status text NOT NULL DEFAULT 'requested' CHECK (status IN ('requested', 'approved', 'active', 'ended', 'rejected', 'expired')),
  started_at timestamptz,
  ended_at timestamptz,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS remote_support_sessions_device_time_idx
  ON public.remote_support_sessions (device_id, created_at DESC);
