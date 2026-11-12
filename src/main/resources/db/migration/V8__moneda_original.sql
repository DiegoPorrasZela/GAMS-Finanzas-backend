-- Gastos en otra moneda (ej. una suscripción de US$ 10): el monto se guarda siempre en soles,
-- y además el monto y la moneda en que se pagó, para mostrarlos tal como fueron.
-- La tasa no se guarda: se obtiene dividiendo ambos montos (guardarla sería un dato derivado).
ALTER TABLE movimiento
    ADD COLUMN moneda_original CHAR(3),
    ADD COLUMN monto_original  NUMERIC(12, 2) CHECK (monto_original > 0),
    -- O se guardan los dos o ninguno
    ADD CONSTRAINT ck_movimiento_moneda_original
        CHECK ((moneda_original IS NULL) = (monto_original IS NULL)),
    ADD CONSTRAINT ck_movimiento_moneda_permitida
        CHECK (moneda_original IS NULL OR moneda_original IN ('USD', 'EUR'));
