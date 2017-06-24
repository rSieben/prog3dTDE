package br.pucpr.cg;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Noise {

    static float[][] GenerateWhiteNoise()
    {
        Random random = new Random(Config.seed); //Seed to 0 for testing
        float[][] noise = new float[Config.width][Config.height];

        for (int i = 0; i < Config.width; i++)
            for (int j = 0; j < Config.height; j++)
                noise[i][j] = (float)random.nextFloat() % 1;

        return noise;
    }

    static float[][] GenerateSmoothNoise(float[][] baseNoise, int octave)
    {
        int width = baseNoise.length;
        int height = baseNoise[0].length;

        float[][] smoothNoise = new float[width][height];

        int samplePeriod = 1 << octave; // calculates 2 ^ k
        float sampleFrequency = 1.0f / samplePeriod;

        for (int i = 0; i < width; i++)
        {
            //calculate the horizontal sampling indices
            int sample_i0 = (i / samplePeriod) * samplePeriod;
            int sample_i1 = (sample_i0 + samplePeriod) % width; //wrap around
            float horizontal_blend = (i - sample_i0) * sampleFrequency;

            for (int j = 0; j < height; j++)
            {
                //calculate the vertical sampling indices
                int sample_j0 = (j / samplePeriod) * samplePeriod;
                int sample_j1 = (sample_j0 + samplePeriod) % height; //wrap around
                float vertical_blend = (j - sample_j0) * sampleFrequency;

                //blend the top two corners
                float top = Interpolate(baseNoise[sample_i0][sample_j0],
                        baseNoise[sample_i1][sample_j0], horizontal_blend);

                //blend the bottom two corners
                float bottom = Interpolate(baseNoise[sample_i0][sample_j1],
                        baseNoise[sample_i1][sample_j1], horizontal_blend);

                //final blend
                smoothNoise[i][j] = Interpolate(top, bottom, vertical_blend);
            }
        }

        return smoothNoise;
    }

    static float[][] GeneratePerlinNoise(float[][] baseNoise, int octaveCount)
    {
        int width = baseNoise.length;
        int height = baseNoise[0].length;

        float[][][] smoothNoise = new float[octaveCount][][]; //an array of 2D arrays containing

        float persistance = 0.5f;

        //generate smooth noise
        for (int i = 0; i < octaveCount; i++)
        {
            smoothNoise[i] = GenerateSmoothNoise(baseNoise, i);
        }

        float[][] perlinNoise = new float[width][height];
        float amplitude = 1.0f;
        float totalAmplitude = 0.0f;

        //blend noise together
        for (int octave = octaveCount - 1; octave >= 0; octave--)
        {
            amplitude *= persistance;
            totalAmplitude += amplitude;

            for (int i = 0; i < width; i++)
            {
                for (int j = 0; j < height; j++)
                {
                    perlinNoise[i][j] += smoothNoise[octave][i][j] * amplitude;
                }
            }
        }

        //normalisation
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                perlinNoise[i][j] /= totalAmplitude;
            }
        }

        return perlinNoise;
    }

    static int GetColor(Color gradientStart, Color gradientEnd, float t)
    {
        float u = 1 - t;

        return new Color(
                checkColorRGB((int)(gradientStart.getRed() * u + gradientEnd.getRed() * t)),
                checkColorRGB((int)(gradientStart.getGreen() * u + gradientEnd.getGreen() * t)),
                checkColorRGB((int)(gradientStart.getBlue() * u + gradientEnd.getBlue() * t)),
                255).getRGB();
    }

    static public int checkColorRGB(int value){
        if(value > 255)
            return 255;
        else if(value < 0)
            return 0;
        else
            return value;
    }

    static BufferedImage imageMapGradient(Color gradientStart, Color gradientEnd, float[][] perlinNoise)
    {
        int width = perlinNoise.length;
        int height = perlinNoise[0].length;

        BufferedImage image = new BufferedImage(Config.width,Config.height,BufferedImage.TYPE_INT_ARGB);

        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                image.setRGB(i,j, GetColor(gradientStart, gradientEnd, perlinNoise[i][j]));

        return image;
    }

    static float[][] floatMapGradient(Color gradientStart, Color gradientEnd, float[][] noise)
    {
        int width = noise.length;
        int height = noise[0].length;

        float[][]  image = new float[width][height];

        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                image[i][j] = GetColor(gradientStart, gradientEnd, noise[i][j]);

        return image;
    }

    public static BufferedImage BlendImages(BufferedImage image1, BufferedImage image2, float[][] perlinNoise)
    {
        int width = Config.width;
        int height = Config.height;

        System.out.println(image1.getWidth()+" "+image1.getHeight());
        System.out.println(image2.getWidth()+" "+image2.getHeight());

        BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);

        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                System.out.println(i+" "+j+" "+Interpolate(checkColorRGB(image1.getRGB(i,j)), checkColorRGB(image2.getRGB(i,j)), perlinNoise[i][j]));

        return image;
    }

    static float Interpolate(float x0, float x1, float alpha)
    {
        return x0 * (1 - alpha) + alpha * x1;
    }

}
