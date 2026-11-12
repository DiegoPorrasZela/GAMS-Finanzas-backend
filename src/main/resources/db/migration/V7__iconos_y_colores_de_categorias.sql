-- Cada categoría tiene un ícono y un color para reconocerla de un vistazo en las listas y gráficos.
-- Se guardan como claves ("comida", "naranja"); el frontend sabe cómo dibujarlas.
-- Las claves permitidas las valida el backend (Categoria.ICONOS y Categoria.COLORES).
ALTER TABLE categoria
    ADD COLUMN icono VARCHAR(20) NOT NULL DEFAULT 'etiqueta',
    ADD COLUMN color VARCHAR(20) NOT NULL DEFAULT 'gris';

UPDATE categoria SET icono = 'maletin',   color = 'verde'    WHERE tipo = 'INGRESO' AND nombre = 'Sueldo';
UPDATE categoria SET icono = 'regalo',    color = 'violeta'  WHERE tipo = 'INGRESO' AND nombre = 'Mesada';
UPDATE categoria SET icono = 'laptop',    color = 'indigo'   WHERE tipo = 'INGRESO' AND nombre = 'Freelance';
UPDATE categoria SET icono = 'etiqueta',  color = 'ambar'    WHERE tipo = 'INGRESO' AND nombre = 'Venta';
UPDATE categoria SET icono = 'monedas',   color = 'teal'     WHERE tipo = 'INGRESO' AND nombre = 'Otros';
UPDATE categoria SET icono = 'comida',    color = 'naranja'  WHERE tipo = 'GASTO'   AND nombre = 'Comida';
UPDATE categoria SET icono = 'bus',       color = 'azul'     WHERE tipo = 'GASTO'   AND nombre = 'Transporte';
UPDATE categoria SET icono = 'rayo',      color = 'ambar'    WHERE tipo = 'GASTO'   AND nombre = 'Servicios';
UPDATE categoria SET icono = 'libro',     color = 'violeta'  WHERE tipo = 'GASTO'   AND nombre = 'Educación';
UPDATE categoria SET icono = 'salud',     color = 'rojo'     WHERE tipo = 'GASTO'   AND nombre = 'Salud';
UPDATE categoria SET icono = 'juego',     color = 'indigo'   WHERE tipo = 'GASTO'   AND nombre = 'Ocio';
UPDATE categoria SET icono = 'bolsa',     color = 'rosa'     WHERE tipo = 'GASTO'   AND nombre = 'Compras';
UPDATE categoria SET icono = 'caja',      color = 'gris'     WHERE tipo = 'GASTO'   AND nombre = 'Otros';
-- Categorías del sistema
UPDATE categoria SET icono = 'billetera', color = 'verde'    WHERE tipo = 'INGRESO' AND nombre = 'Saldo inicial';
UPDATE categoria SET icono = 'alcancia',  color = 'cielo'    WHERE tipo = 'GASTO'   AND nombre = 'Ahorro';
UPDATE categoria SET icono = 'retiro',    color = 'cielo'    WHERE tipo = 'INGRESO' AND nombre = 'Retiro de ahorros';
