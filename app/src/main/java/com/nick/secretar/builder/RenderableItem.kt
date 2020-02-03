package com.nick.secretar.builder

import android.graphics.drawable.Drawable
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable

class RenderableItem(
        var renderable: ModelRenderable,
        var textRenderable: ViewRenderable,
        var name: String,
        var icon: Drawable?,
        var index: Int)