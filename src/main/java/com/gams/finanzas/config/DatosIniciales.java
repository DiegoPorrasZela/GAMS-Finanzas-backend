package com.gams.finanzas.config;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.gams.finanzas.entity.Categoria;
import com.gams.finanzas.entity.Movimiento;
import com.gams.finanzas.entity.Rol;
import com.gams.finanzas.entity.TipoMovimiento;
import com.gams.finanzas.entity.Usuario;
import com.gams.finanzas.repository.CategoriaRepository;
import com.gams.finanzas.repository.MovimientoRepository;
import com.gams.finanzas.repository.RolRepository;
import com.gams.finanzas.repository.UsuarioRepository;

/**
 * Al iniciar, crea el administrador y un usuario demo (con movimientos de ejemplo) si aún no existen.
 * Se hace en Java y no en SQL para guardar las contraseñas encriptadas con BCrypt.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    public static final String EMAIL_DEMO = "demo@gams.pe";

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final CategoriaRepository categoriaRepository;
    private final MovimientoRepository movimientoRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;
    private final String demoPassword;

    public DatosIniciales(UsuarioRepository usuarioRepository, RolRepository rolRepository,
            CategoriaRepository categoriaRepository, MovimientoRepository movimientoRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email}") String adminEmail,
            @Value("${app.admin.password}") String adminPassword,
            @Value("${app.demo.password}") String demoPassword) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.categoriaRepository = categoriaRepository;
        this.movimientoRepository = movimientoRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.demoPassword = demoPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (adminPassword.length() < 8) {
            throw new IllegalStateException("ADMIN_PASSWORD debe tener al menos 8 caracteres");
        }
        usuarioRepository.findByEmail(adminEmail).ifPresentOrElse(
                this::sincronizarPasswordAdmin,
                () -> crearUsuario("Administrador", adminEmail, adminPassword, Rol.ADMIN));

        if (!usuarioRepository.existsByEmail(EMAIL_DEMO)) {
            Usuario demo = crearUsuario("Usuario Demo", EMAIL_DEMO, demoPassword, Rol.USUARIO);
            crearMovimientosDeEjemplo(demo);
        }
    }

    /** Si ADMIN_PASSWORD cambió, actualiza la contraseña guardada: así se cambia sin tocar la base de datos. */
    private void sincronizarPasswordAdmin(Usuario admin) {
        if (!passwordEncoder.matches(adminPassword, admin.getPassword())) {
            admin.cambiarPassword(passwordEncoder.encode(adminPassword));
            log.info("Contraseña del administrador actualizada desde ADMIN_PASSWORD");
        }
    }

    private Usuario crearUsuario(String nombre, String email, String password, String nombreRol) {
        Rol rol = rolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new IllegalStateException("Falta el rol " + nombreRol + " en la base de datos"));
        log.info("Creando usuario inicial {} ({})", email, nombreRol);
        return usuarioRepository.save(new Usuario(nombre, email, passwordEncoder.encode(password), rol));
    }

    private void crearMovimientosDeEjemplo(Usuario demo) {
        agregar(demo, "Pago de prácticas", "1500", 1, "Sueldo", TipoMovimiento.INGRESO);
        agregar(demo, "Menú del almuerzo", "15", 2, "Comida", TipoMovimiento.GASTO);
        agregar(demo, "Recarga de tarjeta de bus", "30", 3, "Transporte", TipoMovimiento.GASTO);
        agregar(demo, "Plan de celular", "45", 6, "Servicios", TipoMovimiento.GASTO);
        agregar(demo, "Salida al cine", "160", 9, "Ocio", TipoMovimiento.GASTO);
        agregar(demo, "Libro de cálculo", "85", 13, "Educación", TipoMovimiento.GASTO);
        agregar(demo, "Diseño de logo", "250", 16, "Freelance", TipoMovimiento.INGRESO);
    }

    private void agregar(Usuario usuario, String descripcion, String monto, int haceDias, String nombreCategoria,
            TipoMovimiento tipo) {
        Categoria categoria = categoriaRepository.findByNombreAndTipo(nombreCategoria, tipo).orElseThrow();
        LocalDate fecha = LocalDate.now().minusDays(haceDias);
        movimientoRepository.save(new Movimiento(usuario, descripcion, new BigDecimal(monto), fecha, categoria));
    }
}
