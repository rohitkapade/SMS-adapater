ALTER TABLE IF EXISTS public.uep_callback_request ALTER COLUMN customer_id TYPE VARCHAR(35);
ALTER TABLE IF EXISTS public.uep_callback_request ALTER COLUMN mobile_number TYPE VARCHAR(25);
ALTER TABLE IF EXISTS public.uep_callback_request ALTER COLUMN callback_request_status TYPE VARCHAR(25);
ALTER TABLE IF EXISTS public.uep_callback_request ALTER COLUMN updated_by TYPE VARCHAR(35);
ALTER TABLE IF EXISTS public.uep_callback_request ADD COLUMN IF NOT EXISTS assigned_to varchar(35);