package org.meicode.textrecognition

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.meicode.textrecognition.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private val MY_PERMISSIONS_REQUEST_CAMERA: Int = 101
    private lateinit var binding: ActivityMainBinding
    private lateinit var textRecognizer: TextRecognizer
    private lateinit var mCameraSource: CameraSource
    private val tag: String? = "MainActivity"

    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createTextRecognizer()
        iniButtonCamera()
        surfaceCameraHolder()
        setTextRecognizer()
        binding.btnGoDataStorage.setOnClickListener {
            initButtonToStorage()
        }
        binding.btnSave.setOnClickListener {
            initButtonSaveDataDatabase()
        }
    }

    // create text recognizer
    fun createTextRecognizer() {
        textRecognizer = TextRecognizer.Builder(this).build()
        if(!textRecognizer.isOperational){
            Toast.makeText(this, "Depedencies are not loaded",Toast.LENGTH_SHORT).show()
            Log.e(tag, "Depedencies are downloading")
        }
    }

    //init camera
    fun iniButtonCamera(){
        mCameraSource = CameraSource.Builder(applicationContext, textRecognizer)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(1280, 1024)
            .setAutoFocusEnabled(true)
            .setRequestedFps(2.0f)
            .build()
    }

    // surface camera holder
    fun surfaceCameraHolder(){
        binding.surfaceCameraPreview.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceDestroyed(holder : SurfaceHolder) {
                mCameraSource.stop()
            }

            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder : SurfaceHolder) {
                try {
                    if (isCameraPermissionGranted()) {
                        mCameraSource.start(binding.surfaceCameraPreview.holder)
                    } else {
                        requestForPermission()
                    }
                } catch (e: Exception) {
                    Helper.toastText(applicationContext,"Error" + e.message)
                }
            }

            override fun surfaceChanged(
                holder : SurfaceHolder,
                format : Int,
                width : Int,
                height : Int
            ) {

            }
        }
        )
    }



    private fun isCameraPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            this@MainActivity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestForPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.CAMERA
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity,
                    Manifest.permission.CAMERA
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(
                        Manifest.permission.CAMERA
                    ),
                    MY_PERMISSIONS_REQUEST_CAMERA
                )

                // MY_PERMISSIONS_REQUEST_CAMERA is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
    }

    //for handling permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    requestForPermission()
                }
                return
            }
            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    fun setTextRecognizer() {
        textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
            var textTvResult = binding.tvResult
            override fun release() {}

            override fun receiveDetections(detections : Detector.Detections<TextBlock>) {
                val items = detections.detectedItems

                if (items.size() <= 0) {
                    return
                }

                textTvResult.post {
                    val stringBuilder = StringBuilder()
                    for (i in 0 until items.size()) {
                        val item = items.valueAt(i)
                        stringBuilder.append(item.value)
                        stringBuilder.append("\n")
                    }
                    textTvResult.text = stringBuilder.toString()
                }
            }
        })
    }

    fun initButtonToStorage(){
        val intent = Intent(this, StorageActivity::class.java)
        startActivity(intent)
    }

    fun initButtonSaveDataDatabase() {
        val editTextTitle = binding.edtTextTitle.text.toString().trim()
        val tvResult = binding.tvResult.text.toString().trim()

        // below line is used to get reference for our database.
        databaseReference = FirebaseDatabase.getInstance().getReference("Data")
        val dataClass = DataClass(editTextTitle, tvResult)

        if (editTextTitle.isEmpty()) {
            Helper.toastText(this, "Please Fill The Edit Text")
        } else {
            //init save firebase
            writeData(editTextTitle, dataClass)
        }
    }

    private fun writeData(title: String, dataClass: DataClass) {
        databaseReference.child(title).setValue(dataClass).addOnSuccessListener {
            binding.edtTextTitle.text.clear()
            Helper.toastText(this@MainActivity, "Succesful Saved")
        }.addOnFailureListener {
            Helper.toastText(applicationContext, "Failed Saved")
        }
    }
}