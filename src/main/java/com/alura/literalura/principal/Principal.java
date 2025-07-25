package com.alura.literalura.principal;

import com.alura.literalura.dto.AutorDTO;
import com.alura.literalura.dto.LibroDTO;
import com.alura.literalura.dto.RespuestaAPI;
import com.alura.literalura.model.Autor;
import com.alura.literalura.model.Libro;
import com.alura.literalura.repository.AutorRepository;
import com.alura.literalura.repository.LibroRepository;
import com.alura.literalura.service.AutorService;
import com.alura.literalura.service.ConsumoAPI;
import com.alura.literalura.service.ConvierteDatos;
import com.alura.literalura.service.LibroService;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class Principal {

    private final Scanner teclado = new Scanner(System.in);
    private final ConsumoAPI consumoAPI = new ConsumoAPI();
    private final ConvierteDatos convierteDatos = new ConvierteDatos();
    private final String URL_BASE = "https://gutendex.com/books/";


    private final LibroService libroService;
    private final AutorService autorService;
    private final LibroRepository libroRepository;
    private final AutorRepository autorRepository;

    public Principal(LibroService libroService, AutorService autorService,
                     LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroService = libroService;
        this.autorService = autorService;
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public void muestraElMenu() {
        int opcion = -1;
        while (opcion != 0) {
            var menu = """
                    
                    *-*-*-* Menú de Opciones *-*-*-*
                    1 - Buscar libro
                    2 - Mostrar libros guardados
                    3 - Mostrar autores por nombre
                    4 - Mostrar autores vivos por años
                    5 - Mostrar libros por idioma
                    ----------------------------------
                    0 - Salir
                    """;
                System.out.println(menu);
                opcion = leerOpcionSegura(0, 5);

            switch (opcion) {
                case 1 -> buscarLibroWeb();
                case 2 -> mostrarLibros();
                case 3 -> mostrarAutores();
                case 4 -> mostrarAutoresVivos();
                case 5 -> mostrarLibrosLen();
                case 0 -> {
                    System.out.println("\n\t\tCerrando la aplicación...");
                    System.exit(0); // Finaliza completamente el programa
                }
                default -> System.out.println("\n\t\tOpción inválida");
            }
        }
        teclado.close();
    }

    public void buscarLibroWeb(){
        //Realiza la consulta a la API
        System.out.println("\n\tHola, ingresa el título a buscar: ");
        var datoBuscado = teclado.nextLine();
        var json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + datoBuscado.replace(" ", "+"));

        RespuestaAPI respuestaAPI = convierteDatos.obtenerDatos(json, RespuestaAPI.class);
        List<LibroDTO> librosDTO = respuestaAPI.getResults();

        if (librosDTO == null || librosDTO.isEmpty()) {
            System.out.println("\n\t\tNo se encontraron coincidencias.");
            return;
        }
        //Muestra las coincidencias de la consulta
        System.out.println("\n\t\t--- Coincidencias para: " + datoBuscado + " ---\n");
        for (int i = 0; i < librosDTO.size(); i++) {
            LibroDTO libro = librosDTO.get(i);
            System.out.println("\t" + (i + 1) + ". " + libro.getTitulo() + " (" +
                    (libro.getAutores().isEmpty() ? "Autor desconocido" : libro.getAutores().get(0).getNombre()) + ")");
        }
        //Elección de libro para proceder al guardado en base de datos
        System.out.print("\n\tIngresa el número del libro que deseas guardar (0 para cancelar): ");
        int opcion = leerOpcionSegura(0, librosDTO.size());
        //Opciones de guardado
        if (opcion == 0) {
            System.out.println("\n\t\tCancelado por el usuario.\nNingún libro guardado.");
            return;
        }

        if (opcion < 1 || opcion > librosDTO.size()) {
            System.out.println("\n\t\tNúmero inválido. No se seleccionó ningún libro.");
            return;
        }

        LibroDTO libroSeleccionado = librosDTO.get(opcion - 1);
        System.out.println("\n\tLibro seleccionado:");
        imprimirDetallesLibro(libroSeleccionado);

        System.out.print("\n\t¿Deseas guardarlo en la base de datos? (s/n): ");
        String confirmar = teclado.nextLine();
        if (confirmar.equalsIgnoreCase("s")) {
            guardarLibro(libroSeleccionado);
        } else {
            System.out.println("\n\t\tLibro no guardado.");
        }
    }

    private void guardarLibro(LibroDTO libroDTO){
        if (libroService.existsByTitle(libroDTO.getTitulo())) {
            System.out.println("\n\t\tLibro '" + libroDTO.getTitulo() + "' ya está registrado en la base de datos.");
            return;
        }

        Libro libro = new Libro();
        libro.setTitulo(libroDTO.getTitulo());
        libro.setDescargas(libroDTO.getNumeroDescargas());
        libro.setIdioma(libroDTO.getIdiomas().stream().findFirst().orElse("Desconocido"));

        //Procesamiento del autor
        Autor autor = libroDTO.getAutores().isEmpty() ? null : procesarAutor(libroDTO.getAutores().get(0), List.of(libroDTO));
        libro.setAutor(autor);
        // Si hay autor, agregamos el libro a su lista
        if (autor == null) {
            System.out.println("\n\t\tAdvertencia: Libro sin información de autor.");
        }
    }

    private Autor procesarAutor(AutorDTO autorDTO, List<LibroDTO> librosDelAutor) {
        Optional<Autor> autorExistente = autorService.findAuthorByName(autorDTO.getNombre());
        if (autorExistente.isPresent()) {
            Autor autor = autorExistente.get();

            // Crear los nuevos libros y asociarlos al autor existente
            List<Libro> nuevosLibros = librosDelAutor.stream().map(dto -> {
                Libro libro = new Libro();
                libro.setTitulo(dto.getTitulo());
                libro.setIdioma(dto.getIdiomas().isEmpty() ? "Desconocido" : dto.getIdiomas().get(0));
                libro.setDescargas(dto.getNumeroDescargas());
                libro.setAutor(autor); // relación inversa
                return libro;
            }).collect(Collectors.toList());

            // Guardar los libros directamente si usas cascada en Autor no es necesario
            libroRepository.saveAll(nuevosLibros); // Asegúrate de tener libroRepository

            System.out.println("\n\t\tAutor ya existente. Libros agregados a la base de datos.");
            return autor;
        } else {
            Autor nuevoAutor = new Autor();
            nuevoAutor.setNombre(autorDTO.getNombre());
            nuevoAutor.setAñoNacimiento(autorDTO.getAñoNacimiento());
            nuevoAutor.setAñoMuerte(autorDTO.getAñoMuerte());
            List<Libro> libros = librosDelAutor.stream().map(dto ->{
                Libro libro = new Libro();
                libro.setTitulo(dto.getTitulo());
                libro.setIdioma(dto.getIdiomas().isEmpty() ? "Desconocido" : dto.getIdiomas().get(0));
                libro.setDescargas(dto.getNumeroDescargas());
                libro.setAutor(nuevoAutor); // relación inversa
                return libro;
            }).collect(Collectors.toCollection(ArrayList::new));
            nuevoAutor.setLibros(libros);
            System.out.println("\n\t\tAutor agregado a Base de datos.");
            return autorRepository.save(nuevoAutor);
        }
    }

    public void mostrarLibros(){
        System.out.println("\n*************** Libros en Base de Datos ***************");
        List<Libro> listaLibros = libroService.findAll();
        if (listaLibros.isEmpty()){
            System.out.println("\n\t\tNo hay libros Registrados.");
        } else {
            imprimirLibrosBD(listaLibros);
        }
    }

    public void mostrarAutores() {
        System.out.println("\n*************** Autores en Base de Datos ***************");
        List<Autor> listaAutores = autorService.getAllAutores();
        if (listaAutores.isEmpty()) {
            System.out.println("\n\t\tNo hay autores registrados.");
        } else {
            imprimirAutoresBD(listaAutores);
        }
    }

    public void mostrarAutoresVivos() {
        System.out.println("\n\tIngresa el año: ");
        int año = validarNumero();
        List<Autor> autoresVivos =  autorRepository.findAutorVivo(año);
        System.out.println("\n\t***** Autores vivos en el año: " + año + " *****");
        imprimirAutoresBD(autoresVivos);
        System.out.println("\n\t**********************************************");
    }

    public void mostrarLibrosLen(){
        System.out.println("""
                
                Ingresa el idioma para realizar la búsqueda:
                es - Español
                en - Ingles
                fr - Frances
                pt - Portugués
                la - Latin
                ru - Ruso
                hu - Húngaro
                """);
        var idiomaSelect = teclado.nextLine();
        if (idiomaSelect.equals("es") || idiomaSelect.equals("en") || idiomaSelect.equals("pt") || idiomaSelect.equals("fr") || idiomaSelect.equals("la") || idiomaSelect.equals("ru") || idiomaSelect.equals("hu")){
            List<Libro> librosIdioma = libroService.findByLanguage(idiomaSelect);
            if (!librosIdioma.isEmpty()){
                System.out.println("\n---------- Libros en idioma: " + idiomaSelect + "----------");
                imprimirLibrosBD(librosIdioma);
            } else {
                System.out.println("\n\t\tNo existen libros registrados en ese idioma.");
            }
        } else {
            System.out.println("\n\t\tIdioma no válido.");
        }
    }

    private int leerOpcionSegura(int min, int max) {
        while (true) {
            if (min == max) {
                System.out.print("\n\tPresiona " + min + " para continuar: ");
            } else {
                System.out.print("\n\tIngresa una opción (" + min + "-" + max + "): ");
            }

            try {
                int opcion = Integer.parseInt(teclado.nextLine());
                if (opcion >= min && opcion <= max) {
                    return opcion;
                } else {
                    System.out.println("\n\t\tOpción fuera de rango. Intenta de nuevo.");
                }
            } catch (NumberFormatException e) {
                System.out.println("\n\t\tEntrada inválida. Por favor, ingresa un número válido.");
            }
        }
    }

    private void imprimirDetallesLibro(LibroDTO libro) {
        System.out.println("\n-------------------------------------------");
        System.out.println("Título:     " + libro.getTitulo());
        if (libro.getAutores() != null && !libro.getAutores().isEmpty()) {
            System.out.println("Autor(es):   " + libro.getAutores().stream()
                    .map(AutorDTO::getNombre)
                    .collect(Collectors.joining(", ")));
        } else {
            System.out.println("Autor(es):   No disponible");
        }
        System.out.println("Idioma:     " + (libro.getIdiomas().isEmpty() ? "N/A" : libro.getIdiomas().get(0)));
        System.out.println("Descargas:  " + libro.getNumeroDescargas());
        System.out.println("-------------------------------------------");
    }

    private void imprimirLibrosBD(List<Libro> libros) {
            libros.forEach(libro -> {
                System.out.println("\n-------------------------------------------------------");
                System.out.println("Título:              " + libro.getTitulo());
                System.out.println("Autor:               " + (libro.getAutor() != null ? libro.getAutor().getNombre() : "N/A"));
                System.out.println("Idioma:              " + libro.getIdioma());
                System.out.println("Número de descargas: " + libro.getDescargas());
            });
        System.out.println("-------------------------------------------------------");
        System.out.println("\n---------------------------Fin-------------------------");

    }

    private void imprimirAutoresBD(List<Autor> autores) {
        autores.forEach(autor -> {
            System.out.println("\n-------------------------------------------------------");
            System.out.println("Nombre del Autor: " + autor.getNombre());
            System.out.println("Año de Nacimiento:" + autor.getAñoNacimiento());
            System.out.println("Año de Fallecimiento: " + autor.getAñoMuerte());
            String titulos = autor.getLibros().stream()
                    .map(Libro::getTitulo)
                    .collect(Collectors.joining(", ", "[", "]"));
            System.out.println("Libros del Autor: " + titulos);
        });
        System.out.println("-------------------------------------------------------");
        System.out.println("\n---------------------------Fin-------------------------");
    }

    private int validarNumero() {
        try {
            int opcion = teclado.nextInt();
            teclado.nextLine();
            return opcion;
        } catch (InputMismatchException e) {
            System.out.println("\n\t\tEntrada inválida. Se esperaba un número.");
            teclado.nextLine();
            return -1;
        }
    }
}