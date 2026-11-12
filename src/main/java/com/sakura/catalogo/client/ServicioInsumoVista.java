package com.sakura.catalogo.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServicioInsumoVista {

	private Long id;

	private Long servicioId;

	private InsumoVista insumo;

	private int cantidad;

}
