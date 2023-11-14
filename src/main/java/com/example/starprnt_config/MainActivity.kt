package com.example.starprnt_config

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.starmicronics.stario10.InterfaceType
import com.starmicronics.stario10.StarConnectionSettings
import com.starmicronics.stario10.StarPrinter
import com.starmicronics.stario10.starxpandcommand.DocumentBuilder
import com.starmicronics.stario10.starxpandcommand.StarXpandCommandBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.lang.Exception
import android.os.Handler

class MainActivity : AppCompatActivity() {


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val saveButton = findViewById<Button>(R.id.button)

        val serverUrlField = findViewById<EditText>(R.id.editTextText)
        val pollingTmeField = findViewById<EditText>(R.id.editTextText2)


        val discoveryButton = findViewById<Button>(R.id.button2)
        val discoveryIntent = Intent(this, DiscoveryActivity::class.java)
        discoveryButton.setOnClickListener { startActivity(discoveryIntent) }


        val selectedPrinter = intent.getStringExtra("confirmedPrinter").toString()
        val printerIdentifier = findViewById<TextView>(R.id.printerIdentifier)
        //printerIdentifier.setText(selectedPrinter)
//
        if (selectedPrinter != "null") {
            printerIdentifier.setText(selectedPrinter)
            saveButton.isEnabled = true
            saveButton.isClickable = true
            pollingTmeField.isEnabled = true
            serverUrlField.isEnabled = true
            saveButton.setOnClickListener { onPressSaveButton() }
        }else {
            printerIdentifier.setText("")
            saveButton.isEnabled = false
            saveButton.isClickable = false
            pollingTmeField.isEnabled = false
            serverUrlField.isEnabled = false
        }

    }

    private fun onPressSaveButton(){

        val saveButton = findViewById<Button>(R.id.button)
        saveButton.isClickable = false
        saveButton.isEnabled = false

        val printerIdentifierInput = findViewById<TextView>(R.id.printerIdentifier)
        val printerIdentifier = printerIdentifierInput.text.toString()

        val printerInterfces = intent.getStringExtra("printerInterface").toString()

        val interfaceType = when (printerInterfces) {
            "Lan" -> InterfaceType.Lan
            "Bluetooth" -> InterfaceType.Bluetooth
            "Usb" -> InterfaceType.Usb
            else -> return
        }


        val serverurlInput = findViewById<EditText>(R.id.editTextText)
        val serverUrl = serverurlInput.text.toString().toByteArray(Charsets.UTF_8)
        val serverUrlLength = serverurlInput.length() + 2

        val pollingtimeInput = findViewById<EditText>(R.id.editTextText2)
        val pollingTime = pollingtimeInput.text.toString().toByte()


        val executeLogin = byteArrayOf(0x1B, 0x1D, 0x29, 0x4E, 0x08, 0x00, 0x72, 0x01, 0x70, 0x75, 0x62, 0x6C, 0x69, 0x63)
        val setLoginPass = byteArrayOf(0x1B, 0x1D, 0x29, 0x4E, 0x08, 0x00, 0x80.toByte(), 0x01, 0x70, 0x75, 0x62, 0x6C, 0x69, 0x63)
        val enableCloudPRNT = byteArrayOf(0x1B, 0x1D, 0x29, 0x4E, 0x03, 0x00, 0x82.toByte(), 0x01, 0x01)
        val setCloudPRNTURL = byteArrayOf(0x1B, 0x1D, 0x29, 0x4E) + serverUrlLength.toByte() + 0x00 + 0x84.toByte() + 0x01 + serverUrl
        val setCloudPRNTPoll = byteArrayOf(0x1B, 0x1D, 0x29, 0x4E, 0x04, 0x00, 0x86.toByte(), 0x01) + pollingTime + 0x00
        val disableCloudPRNT = byteArrayOf(0x1B, 0x1D, 0x29, 0x4E, 0x03, 0x00, 0x82.toByte(), 0x01, 0x00)
        val saveSettings = byteArrayOf(0x1B, 0x1D, 0x29, 0x4E, 0x03, 0x00, 0x70, 0x01, 0x01)

        val configuration = executeLogin + setLoginPass + enableCloudPRNT + setCloudPRNTURL + setCloudPRNTPoll + saveSettings

        val setting = StarConnectionSettings(interfaceType, printerIdentifier)
        val printer = StarPrinter(setting,applicationContext)

        val job = SupervisorJob()
        val scope = CoroutineScope(Dispatchers.Default + job)

        scope.launch {
            try {
                printer.openAsync().await()
                printer.printRawDataAsync(configuration.toList()).await()
                Log.d("Configuring", "Success")

            }catch(e: Exception){
                Log.d("Configuring", "Error: ${e}")
                //Toast.makeText(applicationContext, "Configuration Unsuccessful. Error ${e}", Toast.LENGTH_LONG).show()

            }finally {
                printer.closeAsync().await()
                //Toast.makeText(applicationContext, "Configuration Successful", Toast.LENGTH_LONG).show()

            }

        }

        Handler(mainLooper).postDelayed({
            saveButton.isClickable = true
            saveButton.isEnabled = true
        },10000)
    }


}