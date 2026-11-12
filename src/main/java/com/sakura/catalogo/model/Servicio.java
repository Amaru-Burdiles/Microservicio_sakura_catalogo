package com.sakura.catalogo.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "servicios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Servicio {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "El nombre es obligatorio")
	@Size(max = 120, message = "El nombre no puede superar 120 caracteres")
	@Column(nullable = false, length = 120)
	private String nombre;

	@Size(max = 500, message = "La descripción no puede superar 500 caracteres")
	@Column(length = 500)
	private String descripcion;

	@NotNull(message = "El precio es obligatorio")
	@Positive(message = "El precio debe ser mayor a cero")
	@Column(nullable = false)
	private double precio;

	@NotNull(message = "La duración es obligatoria")
	@Positive(message = "La duración debe ser mayor a cero")
	@Column(nullable = false)
	private Integer duracionMinutos;

	@NotNull(message = "La categoría es obligatoria")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "categoria_id", nullable = false)
	private Categoria categoria;

	@Column(nullable = false)
	private boolean activo = true;

}
