package com.alura.literalura.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class ConvierteDatos implements IConvierteDatos {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public <T> T obtenerDatos(String json, Class<T> clase) {
        try{
            return objectMapper.readValue(json, clase);
        } catch (JsonProcessingException e){
            System.err.println("Error en conversión del JSON" + e.getMessage());
            e.printStackTrace();
            throw  new RuntimeException("Error con el JSON", e);
        }
    }
}
