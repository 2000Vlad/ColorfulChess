package com.training.colorfulchess.game

import android.view.View

class CellClickListener(val provider : GameActivity.ActionProvider, val processor : TransformationProcessor) : View.OnClickListener {

    var position : Int = 0
    override fun onClick(v : View) {
        val transformations = provider.getTransformations(position)
        processor.processTransformations(*transformations.toTypedArray())


    }
    interface TransformationProcessor {
        fun processTransformations(vararg transformations: Transformation)
    }
}