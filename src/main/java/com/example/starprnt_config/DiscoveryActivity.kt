package com.example.starprnt_config

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.starmicronics.stario10.InterfaceType
import com.starmicronics.stario10.StarDeviceDiscoveryManager
import com.starmicronics.stario10.StarDeviceDiscoveryManagerFactory
import com.starmicronics.stario10.StarPrinter

class DiscoveryActivity: AppCompatActivity() {

    private var lanIsEnabled = true

    private var usbIsEnabled = true

    private var _manager: StarDeviceDiscoveryManager? = null

    private val requestCode = 1000

    lateinit var printerList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discovery)

        val checkBoxLan = findViewById<CheckBox>(R.id.checkBoxLan)
        checkBoxLan.setOnClickListener { lanIsEnabled = checkBoxLan.isChecked }

        val checkBoxUsb = findViewById<CheckBox>(R.id.checkBoxUsb)
        checkBoxUsb.setOnClickListener { usbIsEnabled = checkBoxUsb.isChecked }

        val buttonDiscovery = findViewById<Button>(R.id.buttonDiscovery)
        buttonDiscovery.setOnClickListener { onPressDiscoveryButton() }

        //val mainIntent = Intent(this, MainActivity::class.java)
        //val selectedPrinter = findViewById<ListView>(R.id.printerList)


    }

    private fun onPressDiscoveryButton() {
        //val editTextDevices = findViewById<EditText>(R.id.editTextDevices)
        val printerListDevces = findViewById<ListView>(R.id.printerList)

        printerList = ArrayList()

        //printerList.add("C++")
        //printerList.add("Python")

        val adapter: ArrayAdapter<String?> = ArrayAdapter<String?>(
            this,
            android.R.layout.simple_list_item_1,
            printerList as List<String?>
        )

        printerListDevces.adapter = adapter

        //editTextDevices.setText("")

        val interfaceTypes = mutableListOf<InterfaceType>()
        if (this.lanIsEnabled) {
            interfaceTypes += InterfaceType.Lan
        }

        if (this.usbIsEnabled) {
            interfaceTypes += InterfaceType.Usb
        }

        try {
            this._manager?.stopDiscovery()

            _manager = StarDeviceDiscoveryManagerFactory.create(
                interfaceTypes,
                applicationContext
            )
            _manager?.discoveryTime = 10000
            _manager?.callback = object : StarDeviceDiscoveryManager.Callback {
                override fun onPrinterFound(printer: StarPrinter) {
                    //editTextDevices.append("${printer.connectionSettings.interfaceType}:${printer.connectionSettings.identifier}\n")
                    printerList.add("${printer.connectionSettings.interfaceType}:${printer.connectionSettings.identifier}")
                    adapter.notifyDataSetChanged()

                    Log.d("Discovery", "Found printer: ${printer.connectionSettings.identifier}.")
                }

                override fun onDiscoveryFinished() {
                    Log.d("Discovery", "Discovery finished.")
                }
            }

            _manager?.startDiscovery()
        } catch (e: Exception) {
            Log.d("Discovery", "Error: ${e}")
        }

        printerListDevces.setOnItemClickListener { adapterView, view, i, l ->

            val id = printerListDevces.getItemAtPosition(i).toString()
            val printerID = id.substringAfter(":", id)
            val printerInterface = id.substringBefore(":", id)
            val confirmedPrinter = Intent(this, MainActivity::class.java)
            confirmedPrinter.putExtra(
                "confirmedPrinter",
                printerID
            )
            confirmedPrinter.putExtra("printerInterface", printerInterface)
            startActivity(confirmedPrinter)
            //Toast.makeText(applicationContext, printerListDevces.getItemAtPosition(i).toString(), Toast.LENGTH_LONG).show() }
        }

    }
}