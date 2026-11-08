-- Datos fijos que la aplicación necesita para funcionar.
-- Los usuarios iniciales (admin y demo) los crea DatosIniciales.java para poder encriptar su contraseña.

INSERT INTO rol (nombre) VALUES
    ('ADMIN'),
    ('USUARIO');

INSERT INTO categoria (nombre, tipo) VALUES
    ('Sueldo',     'INGRESO'),
    ('Mesada',     'INGRESO'),
    ('Freelance',  'INGRESO'),
    ('Venta',      'INGRESO'),
    ('Otros',      'INGRESO'),
    ('Comida',     'GASTO'),
    ('Transporte', 'GASTO'),
    ('Servicios',  'GASTO'),
    ('Educación',  'GASTO'),
    ('Salud',      'GASTO'),
    ('Ocio',       'GASTO'),
    ('Compras',    'GASTO'),
    ('Otros',      'GASTO');
