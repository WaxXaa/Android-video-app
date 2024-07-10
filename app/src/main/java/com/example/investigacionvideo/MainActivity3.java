package com.example.investigacionvideo;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class MainActivity3 extends ComponentActivity {
    private final int TOMA_VIDEO = 1;
    private VideoView videoV;
    private Spinner spn;
    private String[] listaVideos;

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    private static final int REQUEST_CODE_PERMISSIONS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoV = findViewById(R.id.videoView1);
        spn = findViewById(R.id.spinner);

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        } else {
            setupSpinner();
        }
    }

    private void setupSpinner() {
        listaVideos = fileList();
        String[] nuevaListaVideos = Arrays.copyOfRange(listaVideos, 1, listaVideos.length);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, nuevaListaVideos);
        spn.setAdapter(adapter);
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                setupSpinner();
            } else {
                Toast.makeText(this, "Permisos no concedidos por el usuario.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void grabarVideo(View v) {
        if (allPermissionsGranted()) {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            startActivityForResult(intent, TOMA_VIDEO);
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TOMA_VIDEO && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            videoV.setVideoURI(videoUri);
            videoV.start();

            try {
                String videoName = crearNombreArchivoMP4();
                saveVideoToInternalStorage(videoUri, videoName);
                actualizarListaVideos();
                Toast.makeText(this, "Video guardado exitosamente", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(this, "Error al guardar el video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void saveVideoToInternalStorage(Uri videoUri, String videoName) throws IOException {
        ContentResolver resolver = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME, videoName);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/" + getString(R.string.app_name));

        Uri newVideoUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

        try (FileOutputStream out = (FileOutputStream) resolver.openOutputStream(newVideoUri)) {
            byte[] buffer = new byte[1024];
            int length;
            try (FileInputStream in = (FileInputStream) resolver.openInputStream(videoUri)) {
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
        }
    }

    private void actualizarListaVideos() {
        listaVideos = fileList();
        String[] nuevaListaVideos = Arrays.copyOfRange(listaVideos, 1, listaVideos.length);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, nuevaListaVideos);
        spn.setAdapter(adapter);
    }

    public String crearNombreArchivoMP4() {
        String fecha = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return fecha + ".mp4";
    }

    public void verVideo(View v) {
        int pos = spn.getSelectedItemPosition();
        videoV.setVideoPath(getFilesDir() + "/" + listaVideos[pos]);
        videoV.start();
    }
}
