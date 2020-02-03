/*
 * Copyright 2018 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nick.secretar

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.collision.CollisionShape
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.nick.secretar.builder.ModelBuilder
import com.nick.secretar.builder.ModelItem
import com.nick.secretar.builder.RenderableItem
import java.util.*


class SecretArActivity : AppCompatActivity() {

    private var arFragment: ArFragment? = null
    private var heartRenderable: ModelRenderable? = null
    private var tapIndex = 0;
    private var APP_SCALE: Float = 0.25f
    private var nodeList = emptyList<Node>().toMutableList()

    override// CompletableFuture requires api level 24
    // FutureReturnValueIgnored is not valid
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return
        }

        setContentView(R.layout.activity_ux)

        val buttonView = findViewById<FloatingActionButton>(R.id.fab)
        buttonView.isEnabled = false
        buttonView.setOnClickListener(View.OnClickListener {
            nodeList.forEach { it ->
                it.isEnabled = false
                arFragment!!.arSceneView.scene.removeChild(it)
            }
            tapIndex = 0
            setButtonState(false)
        })

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?

        var modelBuilder = ModelBuilder(arrayOf(
                ModelItem(R.raw.iggy, "Iggy", getDrawable(R.drawable.blackcat)),
                ModelItem(R.raw.miso, "Miso", getDrawable(R.drawable.cat)),
                ModelItem(R.raw.alex, "Alex Gracie...", getDrawable(R.drawable.heart)),
                ModelItem(R.raw.nick, "Will you marry me?", getDrawable(R.drawable.engagement))),
                this,
                R.layout.text_view
        ).build()

        arFragment!!.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
            val itemRenderable: RenderableItem? = modelBuilder.get(tapIndex);
            if (itemRenderable != null && tapIndex < modelBuilder.size()) {
                tapIndex++;

                val box: Box? = itemRenderable.renderable.getCollisionShape() as Box?

                // Create the Anchor.
                val anchor = hitResult.createAnchor()
                val anchorNode = AnchorNode(anchor)
                anchorNode.setParent(arFragment!!.arSceneView.scene)
                // Create the transformable andy and add it to the anchor.
                val item = TransformableNode(arFragment!!.transformationSystem)
                item.scaleController.minScale = 0.1f
                item.localScale = Vector3(APP_SCALE, APP_SCALE, APP_SCALE)
                item.setParent(anchorNode)
                item.renderable = itemRenderable.renderable

                val text = TransformableNode(arFragment!!.transformationSystem)
                text.scaleController.minScale = 0.1f
                text.scaleController.maxScale = 10f
                text.localPosition = Vector3(0f, box!!.size.y * APP_SCALE, 0f)
                text.localScale = Vector3(1f, 1f, 1f)
                text.setParent(anchorNode)

                val textView: Chip = itemRenderable.textRenderable.view as Chip
                textView.text = itemRenderable.name
                textView.chipIcon = itemRenderable.icon
                text.renderable = itemRenderable.textRenderable

                text.isEnabled = true
                item.select()

                nodeList.add(anchorNode)

                if (tapIndex == modelBuilder.size()) {
                    setButtonState(true)
                }
            }
        }
    }

    private fun setButtonState(enabled: Boolean) {
        val buttonView = findViewById<FloatingActionButton>(R.id.fab)
        if (enabled) {
            buttonView.isClickable = true
            buttonView.isEnabled = true
        } else {
            buttonView.isClickable = false
            buttonView.isEnabled = false
        }
    }



    companion object {
        private val TAG = SecretArActivity::class.java.simpleName
        private val MIN_OPENGL_VERSION = 3.0

        /**
         * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
         * on this device.
         *
         *
         * Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
         *
         *
         * Finishes the activity if Sceneform can not run
         */
        fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
            if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
                Log.e(TAG, "Sceneform requires Android N or later")
                Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show()
                activity.finish()
                return false
            }
            val openGlVersionString = (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                    .deviceConfigurationInfo
                    .glEsVersion
            if (java.lang.Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
                Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later")
                Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                        .show()
                activity.finish()
                return false
            }
            return true
        }
    }
}
