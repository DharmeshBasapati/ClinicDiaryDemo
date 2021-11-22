package com.app.clinicdiarydemo.facecheck

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

import android.widget.LinearLayout
import com.app.clinicdiarydemo.R

class FaceCheckActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mDrawingView = DrawingView(this)
        setContentView(R.layout.activity_face_check)
        val mDrawingPad = findViewById<View>(R.id.view_drawing_pad) as LinearLayout
        mDrawingPad.addView(mDrawingView)
    }
}