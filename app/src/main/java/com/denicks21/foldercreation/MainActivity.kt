package com.denicks21.foldercreation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var folderName: EditText
    private lateinit var folderCreate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        folderName = findViewById(R.id.folderName)
        folderCreate = findViewById(R.id.folderCreate)
        folderCreate.setOnClickListener {
            if (checkPermission()) {
                Log.d(TAG, "onClick: Permissions granted")
                createFolder()
            } else {
                Log.d(TAG, "onClick: Permissions not granted")
                requestPermission()
            }
        }
    }

    // Create folder
    private fun createFolder() {
        if (folderName.length() == 0) {

            // Alert message to create a folder
            Toast.makeText(this, "Create a folder", Toast.LENGTH_SHORT).show()

        } else {
            CreateFolder = folderName.text.toString().trim()
            val file = File(Environment.getExternalStorageDirectory().toString() +
                    "/" + CreateFolder)
            val folderCreated = file.mkdir()
            if (folderCreated) {
                // Confirm message of folder created
                Toast.makeText(this, "Folder '${CreateFolder}' created", Toast.LENGTH_SHORT).show()

            } else if (file.exists()) {
                // Alert message of existing folder
                Toast.makeText(this, "Folder '${CreateFolder}' existing", Toast.LENGTH_LONG).show()

            } else {
                // Alert message of folder not created
                Toast.makeText(this, "Folder '${CreateFolder}' not created", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android is 11 (R) or above
            try {
                Log.d(TAG, "requestPermission: try")
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            } catch (e: Exception) {
                Log.e(TAG, "requestPermission: catch", e)
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        } else {
            // Android is below 11 (R)
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    private val storageActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult? ->
        Log.d(TAG, "onActivityResult: ")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android is 11 (R) or above
            if (Environment.isExternalStorageManager()) {
                Log.d(TAG, "onActivityResult: Manage External Storage permissions granted")
                createFolder()
            } else {
                // Android is below 11 (R)
                Log.d(TAG, "Accept permissions to continue")

                // Alert message to accept permissions
                Toast.makeText(this, "Accept permissions to continue", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android is 11 (R) or above
            Environment.isExternalStorageManager()
        } else {
            // Android is below 11 (R)
            val write = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.size > 0) {
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (write && read) {
                    Log.d(TAG, "onRequestPermissionsResult: External Storage permissions granted")
                    createFolder()
                } else {
                    Log.d(TAG, "onRequestPermissionsResult: External Storage permissions not granted")

                    // Alert message of permissions not granted
                    Toast.makeText(this, "Permissions not granted", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        var CreateFolder: String? = null
        private const val STORAGE_PERMISSION_CODE = 100
        private const val TAG = "PERMISSION_TAG"
    }
}