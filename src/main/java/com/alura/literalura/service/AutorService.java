package com.alura.literalura.service;

import com.alura.literalura.model.Autor;
import com.alura.literalura.repository.AutorRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AutorService {
    @Autowired
    AutorRepository autorRepository;
    public List<Autor> getAllAutores() { return (List<Autor>) autorRepository.findAllLibros();}

    public List<Autor> findAutorVivo(int año) { return (List<Autor>) autorRepository.findAutorVivo(año);}

    public Autor saveAuthor(Autor autor) { return autorRepository.save(autor);}

    public Optional<Autor> findAuthorById(Long id) { return autorRepository.findById(id);}

    public Optional<Autor> findAuthorByName(String nombre) { return autorRepository.findByNombre(nombre);}

    public void deleteAuthor(Long id) { autorRepository.deleteById(id);}
}
