package com.training.colorfulchess.game

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.rotationMatrix
import com.training.colorfulchess.R

class TableCell(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    var pieceDrawable: Bitmap? = null
        set(value) {
            field = value; invalidate()

        }

    var backgroundTexture: Bitmap = context.getDrawable(R.drawable.ic_launcher_background)!!.toBitmap()
        set(value) {
            field = value; invalidate()
        }

    private val drawRect: Rect = Rect(0, 0, 0, 0)

    constructor(context: Context) : this(context, null)

    init {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TableCell)


            val pieceDrawable = typedArray.getDrawable(R.styleable.TableCell_pieceDrawable)

            val pieceBackground = typedArray.getDrawable(R.styleable.TableCell_backgroundTexture)

            val piece = typedArray.getInt(R.styleable.TableCell_piece, 0)



          //  this.pieceDrawable = if (pieceColor == WHITE) pieceDrawable!!
          //                          else reverseDrawable(context, pieceDrawable!!)
          //  this.backgroundTexture = pieceBackground!!
          //  this.piece = piece




            typedArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(
            context.resources.getDimension(R.dimen.cell_size).toInt()
                    ,
            context.resources.getDimension(R.dimen.cell_size).toInt()
        )

    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    val paint = Paint()
    val destRect = Rect()
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) return

        drawRect.apply {
            top = 0
            left = 0
        }
        drawRect.right = width
        drawRect.bottom = height

        destRect.apply {
            left = 0
            top = 0
            right = width
            bottom - height
        }
        canvas.drawBitmap(backgroundTexture, null, drawRect, paint)


        if(pieceDrawable != null) {
            val dest = pieceDrawable!!.inMiddle(drawRect, context.resources.getInteger(R.integer.marginRatio))

            //bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height, rotateMatrix, false) !!
            canvas.drawBitmap(pieceDrawable!!, null, dest, paint)

        }


    }

}

fun Drawable.inMiddle(container: Rect) {

    var right : Int
    var bottom : Int
    var left : Int
    var top : Int

    val quarterWidth = container.width() / 4
    val quarterHeight = container.height() / 4

    left = container.left + quarterWidth
    top = container.top + quarterHeight
    right = container.width() - quarterWidth
    bottom = container.height() - quarterHeight

    setBounds(left, top, right, bottom)


}

fun Bitmap.inMiddle(container: Rect, marginRatio: Int) : Rect {
    var right = 0
    var bottom = 0
    var left = 0
    var top = 0

    val marginWidth = container.width() / marginRatio
    val marginHeight = container.height() / marginRatio

    left = container.left + marginWidth
    top = container.top + marginHeight
    right = container.width() - marginWidth
    bottom = container.height() - marginHeight

    return Rect(0,0,right,bottom)
}

/*fun getPieceDrawable(piece: Int, pieceColor: Int): Int =
    when (piece) {
        PAWN ->
            when (pieceColor) {
                WHITE -> R.drawable.pawn_white
                BLACK -> R.drawable.pawn_black
                else -> throw IllegalArgumentException("Piece color not set")
            }
        BISHOP ->
            when (pieceColor) {
                WHITE -> R.drawable.bishop_white
                BLACK -> R.drawable.bishop_black
                else -> throw IllegalArgumentException("Piece color not set")
            }
        KNIGHT ->
            when (pieceColor) {
                WHITE -> R.drawable.knight_white
                BLACK -> R.drawable.knight_black
                else -> throw IllegalArgumentException("Piece color not set")
            }
        ROOK ->
            when (pieceColor) {
                WHITE -> R.drawable.rook_white
                BLACK -> R.drawable.rook_black
                else -> throw IllegalArgumentException("Piece color not set")
            }
        QUEEN ->
            when (pieceColor) {
                WHITE -> R.drawable.queen_white
                BLACK -> R.drawable.queen_black
                else -> throw IllegalArgumentException("Piece color not set")
            }
        KING ->
            when (pieceColor) {
                WHITE -> R.drawable.king_white
                BLACK -> R.drawable.king_black
                else -> throw IllegalArgumentException("Piece color not set")
            }
        else -> throw IllegalArgumentException("Piece type not set")

    }

 */



/* fun getCellProperties(pieceDrawable: Int, backgroundColor: Int): CellProperties {
    val piece: Int =
        when (pieceDrawable) {
            R.drawable.pawn_white, R.drawable.pawn_black -> PAWN
            R.drawable.bishop_black, R.drawable.bishop_white -> BISHOP
            R.drawable.rook_black, R.drawable.rook_white -> ROOK
            R.drawable.knight_black, R.drawable.knight_white -> KNIGHT
            R.drawable.queen_black, R.drawable.queen_white -> QUEEN
            R.drawable.king_black, R.drawable.king_white -> KING
            else -> throw IllegalArgumentException("Piece drawable incorrect")
        }
    val color =
        when (pieceDrawable) {
            R.drawable.pawn_black,
            R.drawable.bishop_black,
            R.drawable.rook_black,
            R.drawable.knight_black,
            R.drawable.queen_black,
            R.drawable.king_black
            -> BLACK
            R.drawable.pawn_white,
            R.drawable.bishop_white,
            R.drawable.rook_white,
            R.drawable.knight_white,
            R.drawable.queen_white,
            R.drawable.king_white
            -> WHITE
            else -> throw IllegalArgumentException("Piece drawable incorrect")
        }
    val background = when (backgroundColor) {
        Color.WHITE -> WHITE
        Color.BLACK -> BLACK
        else -> throw IllegalArgumentException("Background color incorrect")

    }
    return CellProperties(piece, color, background)


} */
fun reverseDrawable(context: Context, bitmap: Bitmap): Bitmap {
    return Bitmap.createBitmap(
        bitmap,
        0,
        0,
        bitmap.width,
        bitmap.height,
        rotationMatrix(
            180f,
            (bitmap.width / 2).toFloat(),
            (bitmap.height / 2).toFloat()
        ),
        false
    )
}
val TableCell.indexInParent : Int get() = (parent as ViewGroup).indexOfChild(this)
const val PAWN = 1
const val BISHOP = 2
const val KNIGHT = 3
const val ROOK = 4
const val QUEEN = 5
const val KING = 6

const val WHITE = 7
const val BLACK = 8

const val SELECTED = 9
const val DEFAULT = 10
const val ENEMY_SELECTED = 11

const val NONE = 12