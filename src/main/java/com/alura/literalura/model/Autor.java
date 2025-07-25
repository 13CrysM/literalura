package com.alura.literalura.model;

import com.alura.literalura.dto.AutorDTO;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="autores")
public class Autor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    private String nombre;
    private Integer añoNacimiento;
    private Integer añoMuerte;
    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Libro> libros = new ArrayList<>();

    public Autor(AutorDTO autorDTO) {
        this.nombre = String.valueOf(autorDTO.getNombre());
        this.añoNacimiento = Integer.valueOf(autorDTO.getAñoNacimiento());
        this.añoMuerte = Integer.valueOf(autorDTO.getAñoMuerte());
    }

    @Override
    public String toString() {
        return "Autor{" +
                "Id=" + Id +
                ", nombre='" + nombre + '\'' +
                ", añoNacimiento=" + añoNacimiento +
                ", añoMuerte=" + añoMuerte +
                '}';
    }
}
