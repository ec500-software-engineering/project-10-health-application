package bu.edu.ec500.sshealthapp;

import android.hardware.SensorManager;

public class SensorData {
    public float[] accelerate, gyroscope, magnetic, gameRotation;

    public long timestamp;

    public float[] worldAcc;

    public SensorData() {
        accelerate = new float[3];
        gyroscope = new float[3];
        magnetic = new float[3];
        worldAcc = new float[3];
        gameRotation = new float[3];
        timestamp = System.currentTimeMillis();
    }

    public void clone(SensorData sd) {
        System.arraycopy(sd.accelerate, 0, this.accelerate, 0, 3);
        System.arraycopy(sd.gyroscope, 0, this.gyroscope, 0, 3);
        System.arraycopy(sd.magnetic, 0, this.magnetic, 0, 3);
        System.arraycopy(sd.worldAcc, 0, this.worldAcc, 0, 3);
        System.arraycopy(sd.gameRotation, 0, this.gameRotation, 0, 3);
        this.timestamp = sd.timestamp;
    }

    public void calculateWorldAcc() {
        float[] Rotate = new float[16];
        float[] I = new float[16];
        float[] currOrientation = new float[3];

        if ((int)gameRotation[0] == 0 && (int)gameRotation[1] == 0 && (int)gameRotation[2] == 0) {
            SensorManager.getRotationMatrix(Rotate, I, accelerate, magnetic);
        } else {
            SensorManager.getRotationMatrixFromVector(Rotate, gameRotation);
        }

        float[] relativeAcc = new float[4];
        float[] earthAcc = new float[4];
        float[] inv = new float[16];
        System.arraycopy(accelerate, 0, relativeAcc, 0, 3);
        relativeAcc[3] = 0;
        android.opengl.Matrix.invertM(inv, 0, Rotate, 0);
        android.opengl.Matrix.multiplyMV(earthAcc, 0, inv, 0, relativeAcc, 0);
        System.arraycopy(earthAcc, 0, worldAcc, 0, 3);
    }
}
