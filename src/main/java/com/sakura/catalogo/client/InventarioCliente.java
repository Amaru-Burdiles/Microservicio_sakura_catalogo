package com.sakura.catalogo.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.sakura.catalogo.exception.ReglaNegocioException;

@Component
public class InventarioCliente {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${sakura.inventario.url}")
	private String baseUrl;

	public List<ServicioInsumoVista> listarDeServicio(Long servicioId) {
		try {
			List<ServicioInsumoVista> cuerpo = restTemplate.exchange(
					baseUrl + "/api/v1/insumos/servicio/" + servicioId, HttpMethod.GET, null,
					new ParameterizedTypeReference<List<ServicioInsumoVista>>() {
					}).getBody();
			return cuerpo == null ? List.of() : cuerpo;
		} catch (RestClientException ex) {
			throw new ReglaNegocioException("No se pudo consultar el inventario");
		}
	}

	public List<ServicioInsumoVista> asociar(Long servicioId, List<ServicioInsumoPedido> pedidos, String authorization) {
		try {
			HttpHeaders headers = new HttpHeaders();
			if (authorization != null) {
				headers.set(HttpHeaders.AUTHORIZATION, authorization);
			}
			List<ServicioInsumoVista> cuerpo = restTemplate.exchange(
					baseUrl + "/api/v1/insumos/servicio/" + servicioId, HttpMethod.PUT,
					new HttpEntity<>(pedidos, headers), new ParameterizedTypeReference<List<ServicioInsumoVista>>() {
					}).getBody();
			return cuerpo == null ? List.of() : cuerpo;
		} catch (RestClientException ex) {
			throw new ReglaNegocioException("No se pudo asociar los insumos");
		}
	}

}
