package com.carrtre.mpmaps

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.carrtre.mpmaps.models.UserMap
import com.carrtre.mpmaps.models.Place
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.*

const val REQUEST_CODE = 429
const val EXTRA_USER_MAP = "EXTRA_USER_MAP"
const val EXTRA_MAP_TITLE = "EXTRA_MAP_TITLE"
const val FILENAME = "UserMaps.data"
private const val TAG = "MainActivity"
private lateinit var rvMaps: RecyclerView
private lateinit var fabCreateMap: FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var userMaps: MutableList<UserMap>
    private lateinit var mapAdapter: MapsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        rvMaps = findViewById(R.id.rvMaps)
        fabCreateMap = findViewById(R.id.fabCreateMap)
        userMaps = deserializeUserMaps(this).toMutableList()

        // Set layout manager on the recycler view
        rvMaps.layoutManager = LinearLayoutManager(this)
        // Set adapter on the recycler view
        mapAdapter = MapsAdapter(this, userMaps, object : MapsAdapter.OnClickListener {
            override fun onItemClick(position: Int) {
                Log.i(TAG, "onItemClick $position.")
                // When user taps on item inb RV, navigate to new activity
                val intent = Intent(this@MainActivity, DisplayMapActivity::class.java)
                intent.putExtra(EXTRA_USER_MAP, userMaps[position])
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        })
        rvMaps.adapter = mapAdapter

        fabCreateMap.setOnClickListener {
            Log.i(TAG, "Tap on FAB.")
            showAlertDialog()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // get new map data from the data
                val userMap = data?.getSerializableExtra(EXTRA_USER_MAP) as UserMap
            Log.i(TAG, "onActivityResult with new map title ${userMap.title}")
            userMaps.add(userMap)
            mapAdapter.notifyItemInserted(userMaps.size - 1)
            serializeUserMaps(this, userMaps)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showAlertDialog() {
        val mapFormView = LayoutInflater.from(this).inflate(R.layout.dialog_create_map, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Map Title")
            .setView(mapFormView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK", null)
            .show()

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val title = mapFormView.findViewById<EditText>(R.id.etMapTitle).text.toString()
            if (title.trim().isEmpty()) {
                Toast.makeText(this, "Map must have non-empty title!", Toast.LENGTH_LONG).show();
                return@setOnClickListener
            }

            // Navigate to create map activity
            val intent = Intent(this@MainActivity, CreateMapActivity::class.java)
            intent.putExtra(EXTRA_MAP_TITLE, title)
            startActivityForResult(intent, REQUEST_CODE)
            dialog.dismiss()
        }
    }

    // Called when data changes.
    private fun serializeUserMaps(context: Context, userMaps: List<UserMap>) {
        Log.i(TAG, "SerializeUserMaps")
        ObjectOutputStream(FileOutputStream(getDataFile(context))).use {it.writeObject(userMaps)}
    }

    // Called when we want to display the list.
    private fun deserializeUserMaps(context: Context) : List<UserMap> {
        Log.i(TAG, "DeserializeUserMaps")
        val dataFile = getDataFile(context)
        if (!dataFile.exists()) {
            Log.i(TAG, "Data file does not exist yet.")
            return emptyList()
        }
        ObjectInputStream(FileInputStream(dataFile)).use {return it.readObject() as List<UserMap>}
    }

    private fun getDataFile(context: Context) : File {
        Log.i(TAG, "Getting file from directory ${context.filesDir}.")
        return File(context.filesDir, FILENAME)

    }
}