package com.training.colorfulchess.game

import android.content.ClipData
import android.util.Log
import android.view.DragEvent
import android.view.DragEvent.*
import android.view.View

class CellDragListener(
    val actionProvider: GameActivity.ActionProvider,
    val processor: CellClickListener.TransformationProcessor
) : View.OnDragListener {
    var position: Int = 0
    lateinit var stateProvider: DragStateProvider
    override fun onDrag(v: View?, event: DragEvent?): Boolean {
        if (event == null) return false
        val state = stateProvider.getDragState(position) == ACCEPT_DRAG
        return when (event.action) {
            ACTION_DRAG_STARTED -> state
            ACTION_DRAG_ENTERED -> true
            ACTION_DRAG_LOCATION -> true
            ACTION_DRAG_ENDED -> {
                val data = actionProvider.getActionData()
                if (data is ActionSelectedData)
                    if (!event.result) {

                        val pos = data.selectedPosition.toIndex()
                        val transformations = actionProvider.getTransformations(pos)
                         processor.processTransformations(*transformations.toTypedArray())
                    }

                true
            }
            ACTION_DROP -> {
                val transformations = actionProvider.getTransformations(position)
                processor.processTransformations(*transformations.toTypedArray())
                true
            }
            ACTION_DRAG_EXITED -> true
            else -> true

        }
    }

    interface DragStateProvider {
        fun getDragState(pos: Int): Int
    }

}

const val ACCEPT_DRAG = 1
const val REJECT_DRAG = 2

