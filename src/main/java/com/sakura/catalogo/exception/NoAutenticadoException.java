package com.sakura.catalogo.exception;

public class NoAutenticadoException extends RuntimeException {
	public NoAutenticadoException(String mensaje) {
		super(mensaje);
	}
}
