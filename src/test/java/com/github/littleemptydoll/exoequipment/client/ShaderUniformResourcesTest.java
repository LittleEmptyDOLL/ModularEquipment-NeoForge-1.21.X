package com.github.littleemptydoll.exoequipment.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ShaderUniformResourcesTest {

    @Test
    void shaderUniformsHaveDeclaredNumberOfValues() throws Exception {
        for (String path : new String[]{
                "/assets/exoequipment/shaders/core/thermal_marker.json",
                "/assets/exoequipment/shaders/program/thermal_tone.json",
                "/assets/exoequipment/shaders/program/thermal_vision.json"
        }) {
            try (InputStream stream = getClass().getResourceAsStream(path)) {
                assertNotNull(stream, path);
                JsonObject shader = JsonParser.parseReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8)
                ).getAsJsonObject();
                JsonArray uniforms = shader.getAsJsonArray("uniforms");
                for (int i = 0; i < uniforms.size(); i++) {
                    JsonObject uniform = uniforms.get(i).getAsJsonObject();
                    assertEquals(
                            uniform.get("count").getAsInt(),
                            uniform.getAsJsonArray("values").size(),
                            path + " " + uniform.get("name").getAsString()
                    );
                }
            }
        }
    }
}
