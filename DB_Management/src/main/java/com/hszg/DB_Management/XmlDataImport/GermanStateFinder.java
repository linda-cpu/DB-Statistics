package com.hszg.DB_Management.XmlDataImport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

/**
 * json data from gitHub isellsoap/deutschlandGeoJSON
 */
@Slf4j
@Service
public class GermanStateFinder {

    // Storage for the compiled geographic paths of each federal state
    private final Map<String, List<Path2D>> stateMap = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${geo.json.path:3_mittel.geo.json}")
    private String geoJsonPath;

    @PostConstruct
    public void init() {
        try {
            log.info("load GeoJSON-Daten from: {}", geoJsonPath);
            ClassPathResource resource = new ClassPathResource(geoJsonPath);
            if (!resource.exists()) {
                log.error("CRITICAL: GeoJSON file not found at: {}", geoJsonPath);
                return;
            }
            loadGeoJson(resource.getInputStream());
            log.info("GeoJSON successfully loaded! {} federal states registered.", stateMap.size());
        } catch (IOException e) {
            log.error("Error initializing GeoJSON data", e);
        }
    }

    /**
     * Parses the GeoJSON string or file according to your exact structure.
     */
    public void loadGeoJson(InputStream geoJsonStream) throws IOException {
        JsonNode rootNode = objectMapper.readTree(geoJsonStream);
        JsonNode features = rootNode.path("features");

        for (JsonNode feature : features) {
            // Extracts "Baden-Württemberg" from properties -> name
            String stateName = feature.path("properties").path("id").asText();
            
            JsonNode geometry = feature.path("geometry");
            String geometryType = geometry.path("type").asText();
            JsonNode coordinatesNode = geometry.path("coordinates");

            List<Path2D> paths = new ArrayList<>();
            if ("MultiPolygon".equals(geometryType)) {
                for (JsonNode polygon : coordinatesNode) {
                    Path2D subPath = new Path2D.Double();
                    JsonNode outerRing = polygon.get(0);
                    appendRingToPath(subPath, outerRing);
                    paths.add(subPath);
                }
            } else if ("Polygon".equals(geometryType)) {
                Path2D subPath = new Path2D.Double();
                appendRingToPath(subPath, coordinatesNode.get(0));
                paths.add(subPath);
            }
            stateMap.put(stateName, paths);
        }
    }

    /**
     * Helper to read a sequence of coordinates and draw/append it to the Path2D object.
     */
    private void appendRingToPath(Path2D path, JsonNode ring) {
        if (ring == null || !ring.isArray() || ring.size() == 0) {
            return;
        }

        // Exact match for your data: index 0 is Longitude, index 1 is Latitude
        double startLon = ring.get(0).get(0).asDouble();
        double startLat = ring.get(0).get(1).asDouble();
        path.moveTo(startLon, startLat);

        for (int i = 1; i < ring.size(); i++) {
            double lon = ring.get(i).get(0).asDouble();
            double lat = ring.get(i).get(1).asDouble();
            path.lineTo(lon, lat);
        }
        path.closePath();
    }

    /**
     * Verifies which state contains the given coordinate pair.
     * * @param latitude  e.g., 48.7758
     * @param longitude e.g., 9.1829
     * @return Name of the state, or "Not Found"
     */
    public String identifyState(double latitude, double longitude) {
        Point2D pointToTest = new Point2D.Double(longitude, latitude);
        for (Map.Entry<String, List<Path2D>> state : stateMap.entrySet()) {
            for (Path2D path : state.getValue()) {
                if (path.contains(pointToTest)) {
                    log.info("Coordinate ({}, {}) belongs to state: {}", latitude, longitude, state.getKey());
                    return state.getKey();
                }
            }
        }
        log.warn("Coordinate ({}, {}) is not within any known state boundaries.", latitude, longitude);
        return "State not found / Outside boundaries";
    }

}
