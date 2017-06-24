package br.pucpr.cg;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javafx.scene.paint.Color;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import br.pucpr.mage.Keyboard;
import br.pucpr.mage.Mesh;
import br.pucpr.mage.Scene;
import br.pucpr.mage.Shader;
import br.pucpr.mage.Window;
import br.pucpr.mage.phong.DirectionalLight;
import br.pucpr.mage.phong.Material;

import javax.imageio.ImageIO;

public class Terrain implements Scene {
    private Keyboard keys = Keyboard.getInstance();

    //Dados da cena
    private Camera camera = new Camera();
    private DirectionalLight light = new DirectionalLight(
            new Vector3f( 1.0f, -3.0f, -1.0f), //direction
            new Vector3f( 0.02f,  0.02f,  0.02f),   //ambient
            new Vector3f( 1.0f,  1.0f,  1.0f),   //diffuse
            new Vector3f( 1.0f,  1.0f,  1.0f));  //specular

    //Dados da malha
    private Mesh mesh;
    private Material material = new Material(
            new Vector3f(1f, 1f, 1f), //ambient
            new Vector3f(1f, 1f, 1f), //diffuse
            new Vector3f(0f, 0f, 0f), //specular
            512.0f);                    //specular power

    private float angleX = 0.0f;
    private float angleY = 0.5f;
    
    @Override
    public void init() {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glPolygonMode(GL_FRONT_FACE, GL_LINE_STRIP);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

       try {
            mesh = MeshFactory.loadTerrain(Config.scale);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        camera.getPosition().y = 50.0f;
        camera.getPosition().z = 50.0f;
    }

    @Override
    public void update(float secs) {
        if (keys.isPressed(GLFW_KEY_ESCAPE)) {
            glfwSetWindowShouldClose(glfwGetCurrentContext(), GLFW_TRUE);
            return;
        }

        if (keys.isDown(GLFW_KEY_A)) {
            angleY += Math.toRadians(180) * secs;
        }

        if (keys.isDown(GLFW_KEY_D)) {
            angleY -= Math.toRadians(180) * secs;
        }
        
        if (keys.isDown(GLFW_KEY_W)) {
            angleX += Math.toRadians(180) * secs;
        }

        if (keys.isDown(GLFW_KEY_S)) {
            angleX -= Math.toRadians(180) * secs;
        }

        if (keys.isDown(GLFW_KEY_F1)) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            glDisable(GL_TEXTURE_2D);
        }

        if (keys.isDown(GLFW_KEY_F2)) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            glEnable(GL_TEXTURE_2D);
        }

        if (keys.isDown(GLFW_KEY_UP)) {
            Config.scale += 0.01;
            refreshMesh();
        }

        if (keys.isDown(GLFW_KEY_DOWN)) {
            Config.scale -= 0.01;
            refreshMesh();
        }

        if (keys.isDown(GLFW_KEY_R)) {
            Random rand = new Random();
            Config.seed += rand.nextInt();
            refreshMesh();
        }

        if (keys.isDown(GLFW_KEY_F5)) {
            if(Config.noiseOctave==6)
                return;
            Config.noiseOctave += 1;
            refreshMesh();
        }

        if (keys.isDown(GLFW_KEY_F6)) {
            if(Config.noiseOctave==0)
                return;
            Config.noiseOctave -= 1;
            refreshMesh();
        }

        if (keys.isDown(GLFW_KEY_F7)) {
            if(Config.perlinOctave==6)
                return;

            Config.perlinOctave += 1;
            refreshMesh();
        }

        if (keys.isDown(GLFW_KEY_F8)) {
            if(Config.perlinOctave==1)
                return;

            Config.perlinOctave -= 1;
            refreshMesh();
        }

        if (keys.isDown(GLFW_KEY_R)) {
            Random rand = new Random();
            Config.seed += rand.nextInt();
            refreshMesh();
        }

        if (keys.isDown(GLFW_KEY_SPACE)) {
            angleX = 0;
            angleY = 0;
        }
    }

    @Override
    public void draw() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        Shader shader = mesh.getShader();
        shader.bind()
            .setUniform("uProjection", camera.getProjectionMatrix())
            .setUniform("uView", camera.getViewMatrix())
            .setUniform("uCameraPosition", camera.getPosition());
        light.apply(shader);
        material.apply(shader);
        shader.unbind();

        mesh.setUniform("uWorld", new Matrix4f().rotateY(angleY).rotateX(angleX));
        mesh.draw();
    }

    @Override
    public void deinit() {
    }

    public void refreshMesh(){
        init();
    }

    public static void main(String[] args) {
        new Window(new Terrain(), "Procedural Terrain", 1024, 768).show();
    }
}
