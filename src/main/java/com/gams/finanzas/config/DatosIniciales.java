package com.gams.finanzas.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.gams.finanzas.entity.Categoria;
import com.gams.finanzas.entity.MetaAhorro;
import com.gams.finanzas.entity.Movimiento;
import com.gams.finanzas.entity.MovimientoRecurrente;
import com.gams.finanzas.entity.PresupuestoMensual;
import com.gams.finanzas.entity.Rol;
import com.gams.finanzas.entity.TipoMovimiento;
import com.gams.finanzas.entity.Usuario;
import com.gams.finanzas.repository.CategoriaRepository;
import com.gams.finanzas.repository.MetaAhorroRepository;
import com.gams.finanzas.repository.MovimientoRecurrenteRepository;
import com.gams.finanzas.repository.MovimientoRepository;
import com.gams.finanzas.repository.PresupuestoMensualRepository;
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
    private final PresupuestoMensualRepository presupuestoRepository;
    private final MetaAhorroRepository metaRepository;
    private final MovimientoRecurrenteRepository recurrenteRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;
    private final String demoPassword;

    public DatosIniciales(UsuarioRepository usuarioRepository, RolRepository rolRepository,
            CategoriaRepository categoriaRepository, MovimientoRepository movimientoRepository,
            PresupuestoMensualRepository presupuestoRepository, MetaAhorroRepository metaRepository,
            MovimientoRecurrenteRepository recurrenteRepository, PasswordEncoder passwordEncoder,
            @Value("${app.admin.email}") String adminEmail,
            @Value("${app.admin.password}") String adminPassword,
            @Value("${app.demo.password}") String demoPassword) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.categoriaRepository = categoriaRepository;
        this.movimientoRepository = movimientoRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.metaRepository = metaRepository;
        this.recurrenteRepository = recurrenteRepository;
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

        usuarioRepository.findByEmail(EMAIL_DEMO).ifPresent(demo -> {
            if (!presupuestoRepository.existsByUsuarioId(demo.getId())
                    && !metaRepository.existsByUsuarioId(demo.getId())) {
                crearPlanDeEjemplo(demo);
            }
            if (!recurrenteRepository.existsByUsuarioId(demo.getId())) {
                crearAutomaticosDeEjemplo(demo);
            }
        });
    }

    /** Sueldo y plan de celular automáticos desde el próximo mes (los de este mes ya están registrados). */
    private void crearAutomaticosDeEjemplo(Usuario demo) {
        log.info("Creando movimientos automáticos de ejemplo para {}", demo.getEmail());
        YearMonth siguiente = YearMonth.now().plusMonths(1);
        recurrenteRepository.saveAll(List.of(
                new MovimientoRecurrente(demo, categoria("Sueldo", TipoMovimiento.INGRESO), "Pago de prácticas",
                        new BigDecimal("1500"), siguiente.atDay(7)),
                new MovimientoRecurrente(demo, categoria("Servicios", TipoMovimiento.GASTO), "Plan de celular",
                        new BigDecimal("45"), siguiente.atDay(2))));
    }

    private Categoria categoria(String nombre, TipoMovimiento tipo) {
        return categoriaRepository.findByNombreAndTipo(nombre, tipo).orElseThrow();
    }

    /** Presupuesto del mes actual y dos metas con aportes para la cuenta demo. */
    private void crearPlanDeEjemplo(Usuario demo) {
        log.info("Creando presupuesto y metas de ejemplo para {}", demo.getEmail());
        YearMonth actual = YearMonth.now();
        presupuestoRepository.save(new PresupuestoMensual(demo, actual.getYear(), actual.getMonthValue(),
                new BigDecimal("600"), new BigDecimal("300")));

        MetaAhorro laptop = new MetaAhorro(demo, "Laptop para la universidad", new BigDecimal("2500"),
                actual.plusMonths(6).atEndOfMonth());
        laptop.aportar(new BigDecimal("300"), LocalDate.now().minusDays(40));
        laptop.aportar(new BigDecimal("250"), LocalDate.now().minusDays(10));

        MetaAhorro viaje = new MetaAhorro(demo, "Viaje de fin de ciclo", new BigDecimal("800"),
                actual.plusMonths(2).atEndOfMonth());
        viaje.aportar(new BigDecimal("150"), LocalDate.now().minusDays(5));

        metaRepository.saveAll(List.of(laptop, viaje));
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
