package com.gams.finanzas;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GamsFinanzasApplication {

	/** Zona horaria de los usuarios: así "hoy" es el mismo día en el servidor (Render usa UTC) y en Perú. */
	public static final String ZONA_HORARIA = "America/Lima";

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone(ZONA_HORARIA));
		SpringApplication.run(GamsFinanzasApplication.class, args);
	}

}
