-- Ahorros: el usuario separa dinero de su saldo a una "cajita" de ahorros, y de ahí aporta a sus metas.
-- Los movimientos entre el saldo y los ahorros usan dos categorías del sistema:
--   "Ahorro" (sale del saldo hacia los ahorros) y "Retiro de ahorros" (vuelve de los ahorros al saldo).
-- Lo que hay en ahorros no se guarda: se calcula (separado − retirado − aportado a metas).
INSERT INTO categoria (nombre, tipo) VALUES
    ('Ahorro', 'GASTO'),
    ('Retiro de ahorros', 'INGRESO')
ON CONFLICT ON CONSTRAINT uq_categoria_nombre_tipo DO NOTHING;

-- Los movimientos automáticos ahora pueden repetirse cada día, cada semana o cada mes
ALTER TABLE movimiento_recurrente
    ADD COLUMN frecuencia VARCHAR(10) NOT NULL DEFAULT 'MENSUAL'
        CHECK (frecuencia IN ('DIARIA', 'SEMANAL', 'MENSUAL'));

-- Antes, los aportes a metas salían del saldo sin quedar registrados. Para que todo cuadre con el nuevo
-- modelo, se crea un movimiento "Ahorro" por usuario con el total que ya había aportado.
INSERT INTO movimiento (usuario_id, categoria_id, descripcion, monto, fecha)
SELECT m.usuario_id,
       (SELECT id FROM categoria WHERE nombre = 'Ahorro' AND tipo = 'GASTO'),
       'Ahorros (anteriores)',
       SUM(a.monto),
       MIN(a.fecha)
FROM aporte a
JOIN meta_ahorro m ON m.id = a.meta_id
GROUP BY m.usuario_id;
