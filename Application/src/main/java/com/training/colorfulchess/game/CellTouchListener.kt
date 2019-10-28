package com.training.colorfulchess.game

import android.content.ClipData
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Half.toFloat
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.View.DRAG_FLAG_OPAQUE
import com.training.colorfulchess.R

class CellTouchListener(
    val actionProvider: GameActivity.ActionProvider,
    val processor: CellClickListener.TransformationProcessor
) : View.OnTouchListener {
    var position: Int = 0
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event == null) return false
        if (v == null) return false
        if (event.action == ACTION_DOWN) {
            val transformations = actionProvider.getTransformations(position)
            processor.processTransformations(*transformations.toTypedArray())
            val actionData = actionProvider.getActionData()
            val cell = v as TableCell
            if (actionData is ActionSelectedData) {
                val shadowBuilder = CellShadowBuilder(
                    cell.pieceDrawable!!,
                    cell
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    cell.startDragAndDrop(
                        ClipData.newPlainText("moving", "$position"),
                        shadowBuilder,
                        null,
                        0
                    )
                } else {
                    cell.startDrag(
                        ClipData.newPlainText("moving", "$position"),
                        shadowBuilder,
                        null,
                        0
                    )
                }
            }


            return true
        }
        if (event.action == ACTION_UP) {
            v.performClick()
            return true
        }


        return false
    }


}

class CellShadowBuilder(val piece: Drawable, view: View) : View.DragShadowBuilder(view) {
    override fun onDrawShadow(canvas: Canvas?) {
        if (canvas == null) return
        val bitmap = piece as BitmapDrawable

        canvas.drawBitmap(
            bitmap.bitmap,
            null,
            bitmap.inMiddle(
                Rect(0,0,view.width,view.height),
                view.context.resources.getInteger(R.integer.marginRatio),
                false
            ),
            null
        )

    }

    override fun onProvideShadowMetrics(outShadowSize: Point?, outShadowTouchPoint: Point?) {
        if (outShadowSize == null) return
        if (outShadowTouchPoint == null) return

        outShadowSize.x = piece.intrinsicWidth
        outShadowSize.y = piece.intrinsicHeight

        outShadowTouchPoint.x = piece.intrinsicWidth / 2
        outShadowTouchPoint.y = piece.intrinsicHeight / 2
    }

}

