package com.sakura.catalogo.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InsumoVista {

	private Long id;

	private String nombre;

	private String categoria;

	private String unidadMedida;

	private Integer stock;

	private int stockMinimo;

	private boolean activo;

	private String estado;

}
