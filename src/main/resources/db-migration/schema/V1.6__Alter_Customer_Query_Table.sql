ALTER TABLE IF EXISTS public.customer_query ALTER COLUMN customer_id SET NOT NULL;
ALTER TABLE IF EXISTS public.customer_query ALTER COLUMN mobile_number SET NOT NULL;
ALTER TABLE IF EXISTS public.customer_query ALTER COLUMN query SET NOT NULL;
ALTER TABLE IF EXISTS public.customer_query ALTER COLUMN status SET NOT NULL;

ALTER TABLE IF EXISTS public.customer_query ALTER COLUMN customer_id TYPE VARCHAR(35);
ALTER TABLE IF EXISTS public.customer_query ALTER COLUMN mobile_number TYPE VARCHAR(25);
ALTER TABLE IF EXISTS public.customer_query ALTER COLUMN updated_by TYPE VARCHAR(35);

ALTER TABLE IF EXISTS public.customer_query ADD COLUMN IF NOT EXISTS image_id bigint;
ALTER TABLE IF EXISTS public.customer_query DROP COLUMN IF EXISTS image_url;

ALTER TABLE IF EXISTS public.customer_query ADD COLUMN IF NOT EXISTS assigned_to varchar(35);