package org.k11techlab.framework.ai.rag.components;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * EmbeddingCache - Simple file-based cache for embeddings.
 */
public class EmbeddingCache {
    private final Path cachePath;
    private final Map<String, double[]> cache = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public EmbeddingCache(String filename) {
        this.cachePath = Paths.get(filename);
        load();
    }

    public double[] get(String key) {
        return cache.get(key);
    }

    public void put(String key, double[] vec) {
        cache.put(key, vec);
    }

    public void save() {
        try {
            Map<String, List<Double>> serializable = new HashMap<>();
            for (var e : cache.entrySet()) {
                List<Double> list = new ArrayList<>();
                for (double v : e.getValue()) list.add(v);
                serializable.put(e.getKey(), list);
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(cachePath.toFile(), serializable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        if (!Files.exists(cachePath)) return;
        try {
            Map<String, List<Double>> serializable = mapper.readValue(cachePath.toFile(), new TypeReference<>() {});
            for (var e : serializable.entrySet()) {
                double[] arr = new double[e.getValue().size()];
                for (int i = 0; i < arr.length; i++) arr[i] = e.getValue().get(i);
                cache.put(e.getKey(), arr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}