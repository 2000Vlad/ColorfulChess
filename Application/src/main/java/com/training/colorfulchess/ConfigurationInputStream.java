package com.training.colorfulchess;

import android.graphics.Point;

import androidx.annotation.NonNull;

import com.training.colorfulchess.game.CellProperties;
import com.training.colorfulchess.game.TableCell;
import com.training.colorfulchess.game.TableCellKt;
import com.training.colorfulchess.game.Transformation;
import com.training.colorfulchess.game.modelvm2.GameConfiguration2Kt;
import com.training.colorfulchess.game.modelvm2.GameConfigurationHelperKt;
import com.training.colorfulchess.game.modelvm2.Piece;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import kotlin.Triple;

public class ConfigurationInputStream extends FilterInputStream {

    private Transformation[] transformations = new Transformation[64];
    int slotsRead = 0;
    private Header header;

    public ConfigurationInputStream(InputStream stream) {
        super(stream);
        for (int i = 0; i < 64; i++) {
            transformations[i] = new Transformation(
                    i,
                    new CellProperties(
                            TableCellKt.NONE, TableCellKt.NONE, TableCellKt.DEFAULT, false
                    )
            );
        }

    }

    public Header readHeader() {
        header = new Header(0, 0, 0);
        try {
            header.whites = read();
            header.blacks = read();
            header.player = read();
        } catch (IOException ignored) {

        }

        return header;
    }

    public Slot readSlot() {
        Slot slot = new Slot(new Point(-1, -1), 8);
        try {
            int pos = read();
            int piece = read();
            Point pt = GameConfigurationHelperKt.toPoint(pos);
            int color = slotsRead < header.whites ? TableCellKt.WHITE : TableCellKt.BLACK;
            slot.pos = pt;
            slot.piece = piece;
            transformations[pos] = new Transformation(
                    pos,
                    new CellProperties(
                            piece, color, TableCellKt.DEFAULT, color == TableCellKt.BLACK
                    )
            );
            slotsRead++;

        } catch (IOException ignored) {

        }
        return slot;
    }

    public Transformation[] getTransformations() {
        return transformations;
    }

    public void setTransformations(Transformation[] transformations) {
        this.transformations = transformations;
    }

    public class Header {
        private int whites;
        private int blacks;
        private int player;

        public Header(int whites, int blacks, int player) {
            this.whites = whites;
            this.blacks = blacks;
            this.player = player;
        }

        public int getWhites() {
            return whites;
        }

        public void setWhites(int whites) {
            this.whites = whites;
        }

        public int getBlacks() {
            return blacks;
        }

        public void setBlacks(int blacks) {
            this.blacks = blacks;
        }

        public int getPlayer() {
            return player;
        }

        public void setPlayer(int player) {
            this.player = player;
        }
    }

    public class Slot {
        private Point pos;
        @Piece
        private int piece;

        public Slot(Point pos, int piece) {
            this.pos = pos;
            this.piece = piece;
        }

        public Point getPos() {
            return pos;
        }

        public void setPos(Point pos) {
            this.pos = pos;
        }


        public int getPiece() {
            return piece;
        }

        public void setPiece(int piece) {
            this.piece = piece;
        }
    }
}
