package services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // <--- 1. IMPORTANTE: Importar el módulo

import java.io.File;
import java.io.IOException;

/**
 * Utilidad genérica para exportar/importar JSON usando Jackson.
 * - write(file, data): serializa cualquier objeto a JSON
 * - read(file, Class<T>): deserializa JSON a un tipo concreto
 */
public final class JsonIO {

    // ObjectMapper es el motor de Jackson: convierte Java <-> JSON
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // Bloque estático para configurar el mapper una sola vez al iniciar
    static {
        // 2. REGISTRAR EL MÓDULO DE FECHAS (Soluciona el error de "not supported")
        MAPPER.registerModule(new JavaTimeModule());

        // 3. CONFIGURACIÓN VISUAL
        // Para que las fechas se guarden como "2023-10-25" y no como [2023, 10, 25]
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Para que el JSON se vea bonito y ordenado (con sangrías)
        MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private JsonIO() { }

    /** Escribe un objeto Java como JSON en el fichero. */
    public static <T> void write(File file, T data) throws IOException {
        // Si el fichero está en una carpeta que no existe, la creamos
        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();

        MAPPER.writeValue(file, data);
    }

    /** Lee un JSON desde fichero y lo convierte al tipo indicado. */
    public static <T> T read(File file, Class<T> type) throws IOException {
        return MAPPER.readValue(file, type);
    }
}