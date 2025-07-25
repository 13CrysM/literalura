package com.alura.literalura.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RespuestaAPI {
    private List<LibroDTO> results;
    public List<LibroDTO> getResults() {
        return results;
    }
    public void SetResults(List<LibroDTO> results) {this.results = results;}
}
