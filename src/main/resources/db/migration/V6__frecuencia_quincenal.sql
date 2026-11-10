-- Nueva frecuencia QUINCENAL (el 15 y el último día de cada mes).
-- La restricción CHECK de la V5 no tenía nombre: se busca y se reemplaza por una con nombre.
DO $$
DECLARE
    restriccion TEXT;
BEGIN
    SELECT conname INTO restriccion
    FROM pg_constraint
    WHERE conrelid = 'movimiento_recurrente'::regclass
      AND contype = 'c'
      AND pg_get_constraintdef(oid) LIKE '%frecuencia%';
    IF restriccion IS NOT NULL THEN
        EXECUTE format('ALTER TABLE movimiento_recurrente DROP CONSTRAINT %I', restriccion);
    END IF;
END $$;

ALTER TABLE movimiento_recurrente
    ADD CONSTRAINT ck_recurrente_frecuencia
        CHECK (frecuencia IN ('DIARIA', 'SEMANAL', 'QUINCENAL', 'MENSUAL'));
