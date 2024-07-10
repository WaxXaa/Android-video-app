package com.example.investigacionvideo;

import org.apache.commons.io.IOUtils;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.VideoView;
import android.content.pm.PackageManager;
import androidx.activity.ComponentActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class MainActivity2 extends ComponentActivity {
    private final int TOMA_VIDEO = -1;
    private VideoView videoV;
    private Spinner spn;
    private String[] listaVideos;
    ArrayAdapter<String> adapter;

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
        listaVideos = fileList();
         adapter= new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listaVideos);
        spn.setAdapter(adapter);
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        } else {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listaVideos);
            spn.setAdapter(adapter);
        }
    }
    private void deleteFiles() {
        for (String nombre: fileList()) {
            deleteFile(nombre);
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }



    public void grabarVideo(View v) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, TOMA_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(this, "entro a onActivityResult", Toast.LENGTH_SHORT).show();
        if (requestCode == TOMA_VIDEO && resultCode == RESULT_OK) {

            Uri videoUri = data.getData();
            videoV.setVideoURI(videoUri);
            videoV.start();

            try {
                AssetFileDescriptor videoAsset = getContentResolver().openAssetFileDescriptor(Objects.requireNonNull(data.getData()), "r");
                assert videoAsset != null;
                FileInputStream ins = videoAsset.createInputStream();
                FileOutputStream archivo = openFileOutput(crearNombreArchivoMP4(), Context.MODE_PRIVATE);

                IOUtils.copy(ins, archivo);
                ins.close();
                archivo.close();
            } catch (IOException e) {
                Toast.makeText(this, "Error al grabar video", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            adapter.setNotifyOnChange(true);
        }
    }

    public String crearNombreArchivoMP4() {
        String fecha = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return fecha + ".mp4";
    }

    public void verVideo(View v) {
        Toast.makeText(this, "" + listaVideos.length, Toast.LENGTH_SHORT).show();
        int pos = spn.getSelectedItemPosition();
        listaVideos = fileList();

        videoV.setVideoPath(getFilesDir() + "/" + listaVideos[pos]);
        videoV.start();
    }
}

