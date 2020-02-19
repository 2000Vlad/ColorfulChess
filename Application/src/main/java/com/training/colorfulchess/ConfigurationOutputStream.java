package com.training.colorfulchess;

import android.graphics.Point;

import com.training.colorfulchess.game.GameConfigurationKt;
import com.training.colorfulchess.game.modelvm2.Piece;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ConfigurationOutputStream extends FilterOutputStream {
    public ConfigurationOutputStream(OutputStream stream) {
        super(stream);
    }
    public void writeSlot(Point pos, @Piece int piece) {
        try{
            write(GameConfigurationKt.toIndex(pos));
            write(piece);

        }catch (IOException ignored) {

        }
    }

    public void writeHeader(int whites, int blacks, int player) {
        try {
            write(whites);
            write(blacks);
            write(player);
        }catch (IOException ignored) {

        }
    }

    public class Slot {
        Point pos;
        int piece;

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
