package com.alura.literalura.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LibroDTO {
    @JsonAlias("id") String id_libro;
    @JsonAlias("title") String titulo;
    @JsonAlias("authors") List<AutorDTO> autores;
    @JsonAlias("languages") List<String> idiomas;
    @JsonAlias("download_count") int numeroDescargas;

    @Override
    public String toString() {
        return "LibroDTO{" +
                "id_libro='" + id_libro + '\'' +
                ", titulo='" + titulo + '\'' +
                ", autores=" + autores +
                ", idiomas=" + idiomas +
                ", numeroDescargas=" + numeroDescargas +
                '}';
    }
}
