package com.sakura.catalogo.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServicioInsumoPedido {

	private Long insumoId;

	private Integer cantidad;

}
