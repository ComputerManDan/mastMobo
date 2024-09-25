package com.google.mediapipe.examples.poselandmarker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.mediapipe.examples.poselandmarker.fragment.CameraFragment
import com.google.mediapipe.examples.poselandmarker.fragment.GalleryFragment
import androidx.fragment.app.FragmentManager

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val cameraButton = findViewById<Button>(R.id.cameraButton)
        val galleryButton = findViewById<Button>(R.id.galleryButton)

        // Navigate to CameraFragment when cameraButton is clicked
        cameraButton.setOnClickListener { v: View? ->
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, CameraFragment())
                .addToBackStack(null)
                .commit()
        }

        // Navigate to GalleryFragment when galleryButton is clicked
        galleryButton.setOnClickListener { v: View? ->
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, GalleryFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
