package me.lyric.skyfall.api.utils.shader.screen;

import org.lwjgl.opengl.GL20;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author ?
 * shader instantiation for shaders rendering over the screen.
 */

public class ScreenShader {
    private final int time, mouse, resolution, programId;

    public ScreenShader(String fragmentShader) throws IOException {
        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, createShader("/shaders/passthrough.vsh", ScreenShader.class.getResourceAsStream("/assets/minecraft/textures/shaders/passthrough.vsh"), 35633));
        GL20.glAttachShader(program, createShader(fragmentShader, ScreenShader.class.getResourceAsStream(fragmentShader), 35632));
        GL20.glLinkProgram(program);
        int linked = GL20.glGetProgrami(program, 35714);
        if (linked == 0) {
            System.err.println(GL20.glGetProgramInfoLog(program, GL20.glGetProgrami(program, 35716)));
            throw new IllegalStateException("Shader Screen failed to link - report this!");
        }
        programId = program;
        GL20.glUseProgram(program);
        time = GL20.glGetUniformLocation(program, "time");
        mouse = GL20.glGetUniformLocation(program, "mouse");
        resolution = GL20.glGetUniformLocation(program, "resolution");
        GL20.glUseProgram(0);
    }

    public void useShader(int width, int height, float mouseX, float mouseY, float timevar) {
        GL20.glUseProgram(programId);
        GL20.glUniform2f(resolution, width, height);
        GL20.glUniform2f(mouse, (mouseX / width), (1.0f - mouseY / height));
        GL20.glUniform1f(time, timevar);
    }

    private int createShader(String check, InputStream inputStream, int shaderType) throws IOException {
        if (inputStream == null) {
            throw new IOException("Failed to load shader resource: " + check + " (resource not found)");
        }
        int shader = GL20.glCreateShader(shaderType);
        GL20.glShaderSource(shader, readStreamToString(inputStream));
        GL20.glCompileShader(shader);
        int compiled = GL20.glGetShaderi(shader, 35713);
        if (compiled == 0) {
            System.err.println(GL20.glGetShaderInfoLog(shader, GL20.glGetShaderi(shader, 35716)));
            System.err.println("Caused by " + check);
            throw new IllegalStateException("Failed to compile shader: " + check);
        }
        return shader;
    }

    private String readStreamToString(InputStream inputStream) throws IOException {
        int read;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[512];
        while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, read);
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

}

