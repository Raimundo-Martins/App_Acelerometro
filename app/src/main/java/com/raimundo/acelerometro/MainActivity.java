package com.raimundo.acelerometro;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends Activity implements SensorEventListener {

    private FileOutputStream outputStreamAcelerometro;
    private FileOutputStream outputStreamGiroscopio;
    {
        try {
            outputStreamAcelerometro = new FileOutputStream("/sdcard/ProjetoTCC/Acelerometro.csv", true);
            outputStreamGiroscopio = new FileOutputStream("/sdcard/ProjetoTCC/Giroscopio.csv", true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private TextView textViewAcelerometroX, textViewAcelerometroY, textViewAcelerometroZ;
    private TextView textViewGiroscopioX, textViewGiroscopioY, textViewGiroscopioZ;

    private float acelerometroX, acelerometroY, acelerometroZ;
    private float giroscopioX, giroscopioY, giroscopioZ;

    private Timer timer;

    private SensorManager sensorManagerAcelerometro;
    private SensorManager sensorManagerGiroscopio;
    private Sensor acelerometro;
    private Sensor giroscopio;

    public void Timer(){
        Timer timer = new Timer();
        Task task = new Task();

        timer.schedule(task, 1000, 1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        }

        textViewAcelerometroX = findViewById(R.id.textViewAcelerometroX);
        textViewAcelerometroY = findViewById(R.id.textViewAcelerometroY);
        textViewAcelerometroZ = findViewById(R.id.textViewAcelerometroZ);
        textViewGiroscopioX = findViewById(R.id.textViewGiroscopioX);
        textViewGiroscopioY = findViewById(R.id.textViewGiroscopioY);
        textViewGiroscopioZ = findViewById(R.id.textViewGiroscopioZ);

        sensorManagerAcelerometro = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManagerGiroscopio = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        acelerometro = sensorManagerAcelerometro.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        giroscopio = sensorManagerGiroscopio.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            Date data = new Date();

            Calendar cal = Calendar.getInstance();
            cal.setTime(data);
            Date data_atual = cal.getTime();
            String dataAtual = dateFormat.format(data_atual);

            FileWriter writerAcelerometro = new FileWriter(outputStreamAcelerometro.getFD());
            writerAcelerometro.append('\n');
            writerAcelerometro.append("ACELEROMETRO - " + dataAtual);
            writerAcelerometro.append('\n');
            writerAcelerometro.append('\n');
            writerAcelerometro.flush();
            writerAcelerometro.close();

            FileWriter writerGyroscopic = new FileWriter(outputStreamGiroscopio.getFD());
            writerGyroscopic.append('\n');
            writerGyroscopic.append("GIROSCOPIO - " + dataAtual);
            writerGyroscopic.append('\n');
            writerGyroscopic.append('\n');
            writerGyroscopic.flush();
            writerGyroscopic.close();
        } catch (IOException ex) {
            Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
        }

        Timer();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){

            float acelerometroX = event.values[0];
            float acelerometroY = event.values[1];
            float acelerometroZ = event.values[2];

            textViewAcelerometroX.setText("Posição X : " + acelerometroX);
            textViewAcelerometroY.setText("Posição Y : " + acelerometroY);
            textViewAcelerometroZ.setText("Posição Z : " + acelerometroZ);

            this.acelerometroX = acelerometroX;
            this.acelerometroY = acelerometroY;
            this.acelerometroZ = acelerometroZ;
        }

        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            float giroscopioX = event.values[0];
            float giroscopioY = event.values[1];
            float giroscopioZ = event.values[2];

            textViewGiroscopioX.setText("Posição X : " + giroscopioX);
            textViewGiroscopioY.setText("Posição Y : " + giroscopioY);
            textViewGiroscopioZ.setText("Posição Z : " + giroscopioZ);

            this.giroscopioX = giroscopioX;
            this.giroscopioY = giroscopioY;
            this.giroscopioZ = giroscopioZ;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    class Task extends TimerTask{

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    gerarCsvFile();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManagerAcelerometro.registerListener(this, acelerometro, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManagerGiroscopio.registerListener(this, giroscopio, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManagerAcelerometro.unregisterListener(this);
        sensorManagerGiroscopio.unregisterListener(this);
    }

    private void gerarCsvFile() {

        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");

        Date data = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(data);
        Date data_atual = cal.getTime();

        String data_completa = dateFormat.format(data_atual);
        try {
            FileWriter fileWriterAcelerometro = new FileWriter(outputStreamAcelerometro.getFD());
            FileWriter fileWriterGiroscopio = new FileWriter(outputStreamGiroscopio.getFD());
            try {
                fileWriterAcelerometro.write("Posicao X : " + acelerometroX);
                fileWriterAcelerometro.write(",");
                fileWriterAcelerometro.write(" Posicao Y: " + acelerometroY);
                fileWriterAcelerometro.write(",");
                fileWriterAcelerometro.write(" Posicao Z: " + acelerometroZ);
                fileWriterAcelerometro.write(" - ");
                fileWriterAcelerometro.write("" + data_completa);
                fileWriterAcelerometro.write("\n");
                fileWriterAcelerometro.flush();
                fileWriterAcelerometro.close();

                fileWriterGiroscopio.write("Posicao X : " + giroscopioX);
                fileWriterGiroscopio.write(",");
                fileWriterGiroscopio.write(" Posicao Y: " + giroscopioY);
                fileWriterGiroscopio.write(",");
                fileWriterGiroscopio.write(" Posicao Z: " + giroscopioZ);
                fileWriterGiroscopio.write(" - ");
                fileWriterGiroscopio.write("" + data_completa);
                fileWriterGiroscopio.write("\n");
                fileWriterGiroscopio.flush();
                fileWriterGiroscopio.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
