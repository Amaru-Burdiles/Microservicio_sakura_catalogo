package com.sakura.catalogo.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.sakura.catalogo.model.Categoria;
import com.sakura.catalogo.model.Servicio;
import com.sakura.catalogo.repository.CategoriaRepository;
import com.sakura.catalogo.repository.ServicioRepository;

@Component
public class DatosIniciales implements CommandLineRunner {

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Autowired
	private ServicioRepository servicioRepository;

	@Override
	public void run(String... args) {
		if (categoriaRepository.count() > 0) {
			return;
		}
		Categoria cabello = guardarCategoria("Cabello");
		Categoria unas = guardarCategoria("Uñas");
		Categoria ojos = guardarCategoria("Ojos");

		guardarServicio("Botox capilar", "Tratamiento de reparación capilar", 30000, 60, cabello);
		guardarServicio("Alisado permanente", "Alisado de larga duración", 70000, 240, cabello);
		guardarServicio("Coloración", "Color uniforme y luminoso", 45000, 90, cabello);
		guardarServicio("Masaje hidratación", "Hidratación y masaje capilar", 30000, 45, cabello);
		guardarServicio("Retiro esmalte", "Retiro de esmalte permanente", 10000, 30, unas);
		guardarServicio("Polygel", "Extensión de uñas en polygel", 35000, 90, unas);
		guardarServicio("Manicure rusa", "Manicure con técnica rusa", 50000, 75, unas);
		guardarServicio("Esmaltado Permanente", "Esmaltado de larga duración", 20000, 60, unas);
		guardarServicio("Lifting Tradicional", "Lifting de pestañas", 25000, 60, ojos);
		guardarServicio("Lifting Coreano", "Lifting de pestañas técnica coreana", 35000, 75, ojos);
		guardarServicio("Perfilado de cejas", "Diseño y perfilado de cejas", 10000, 30, ojos);
		guardarServicio("Laminado de cejas", "Laminado y fijación de cejas", 15000, 45, ojos);
	}

	private Categoria guardarCategoria(String nombre) {
		Categoria categoria = new Categoria();
		categoria.setNombre(nombre);
		return categoriaRepository.save(categoria);
	}

	private void guardarServicio(String nombre, String descripcion, double precio, int minutos, Categoria categoria) {
		Servicio servicio = new Servicio();
		servicio.setNombre(nombre);
		servicio.setDescripcion(descripcion);
		servicio.setPrecio(precio);
		servicio.setDuracionMinutos(minutos);
		servicio.setCategoria(categoria);
		servicio.setActivo(true);
		servicioRepository.save(servicio);
	}

}
