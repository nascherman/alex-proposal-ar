package com.nick.secretar.builder

import android.animation.FloatEvaluator
import android.content.Context
import android.view.Gravity
import android.widget.Toast
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import kotlinx.android.synthetic.main.text_view.view.*
import java.io.InputStream
import java.util.concurrent.Callable
import android.view.animation.LinearInterpolator
import com.google.ar.sceneform.math.Vector3Evaluator
import android.animation.ObjectAnimator
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3



class ModelBuilder(private var models: Array<ModelItem>, private var context: Context, private var textView: Int) {
    private var modelRenderables: MutableList<RenderableItem> = emptyList<RenderableItem>().toMutableList()

    fun build(): ModelBuilder {
        models.forEachIndexed {index, it ->
            ModelRenderable.builder()
                    .setSource(context, it.item)
                    .build()
                    .thenAccept { renderable ->
                        ViewRenderable.builder()
                                .setView(context, textView)
                                .build()
                                .thenAccept { textRenderable ->
                                    modelRenderables.add(RenderableItem(renderable, textRenderable, it.name, it.icon, index))
                                }
                                .exceptionally { throwable ->
                                    println(throwable.toString())
                                    null
                                }
                    }
                    .exceptionally { throwable ->
                        println(throwable.toString())
                        null
                    }
        }

        return this
    }

    fun get(index: Int): RenderableItem? {
        modelRenderables = modelRenderables.sortedBy { it -> it.index }.toMutableList()
        if (modelRenderables.size > index) {
            return modelRenderables[index]
        }

        return null
    }

    fun size(): Int {
        return modelRenderables.size
    }
}