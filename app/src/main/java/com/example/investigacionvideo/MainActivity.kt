package com.example.investigacionvideo

import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.ComponentActivity
import java.io.FileInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date



class MainActivity : ComponentActivity() {
    private val TOMA_VIDEO: Int = -1
    private lateinit var videoV: VideoView
    private lateinit var spn: Spinner
    private lateinit var listaVideos: Array<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        videoV = findViewById(R.id.videoView1)
        spn = findViewById(R.id.spinner)
        listaVideos = fileList()
        val adater = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, listaVideos)
        spn.adapter = adater
    }
    fun grabarVideo(v: View) {
        var intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(intent, TOMA_VIDEO)
    }

        @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            Toast.makeText(this,"entro a onActivityResult", Toast.LENGTH_SHORT).show()
            if(requestCode == TOMA_VIDEO && resultCode == RESULT_OK) {
                val videoUri: Uri? = data?.data
                videoV.setVideoURI(videoUri)
                videoV.start()

                try {
                    val videoAsert: AssetFileDescriptor? = data?.data?.let { contentResolver.openAssetFileDescriptor(it, "r") }
                    val ins : FileInputStream? = videoAsert?.createInputStream()
                    val archivo = openFileOutput(crearNombreArchivoMP4(), Context.MODE_PRIVATE)

                    val buf = ByteArray(1024)
                    var len: Int
                    len = ins!!.read(buf)
                    while (len > 0) {
                        archivo.write(buf, 0, len)
                        len = ins.read(buf)
                    }
                    archivo.close()
                    ins.close()
                } catch (e: IOException ) {
                    Toast.makeText(this, "Error al grabar video", Toast.LENGTH_SHORT).show()
                }
            }
        }
    fun crearNombreArchivoMP4(): String {
        val fecha : String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val nombre : String =  fecha + ".mp4"
        return nombre
    }
    fun verVideo(v: View) {
        Toast.makeText(this,"$listaVideos.size  ", Toast.LENGTH_SHORT).show()
        val pos: Int = spn.selectedItemPosition
//        val videoFile = File(getF, "/" + listaVideos[pos])
        videoV.setVideoPath("$filesDir/${listaVideos[pos]}")
        videoV.start()

    }



}

