package Main;
import java.awt.Graphics2D;
import java.util.ArrayList;

import Pieces.*;
import Squares.*;

public class Board {
	
	public Piece pieces[][];
	public Piece latestState[][];
	public ISquare squares[][];
	public HLSquare hlSquare;
	
	PieceMoveHandler moveHandler;
	
	History history;
	Player player1, player2;
	Clock clock;
	
	public boolean colorToPlay;
	int totalMoves;
	int currentMove;
	
	public ArrayList<ISquare> possibleMoves;
	
	public static int SQUARES = 8;
	
	public Board() {
		history = new History();
		moveHandler = new PieceMoveHandler(this);
		player1 = new Player(true/*white*/, "Player1");
		player2 = new Player(false/*black*/, "Player2");
		
		colorToPlay = true; // white to move first
		totalMoves = 0;
		currentMove = 0;
	}
	
	public void movePieceToSquare(ISquare source, ISquare target) {
		int si = source.getCoords().getI(),
			sj = source.getCoords().getJ(),
			ti = target.getCoords().getI(),
			tj = target.getCoords().getJ();
		
		totalMoves++;
		currentMove++;
		
		// record a move before switching those indexes
		// TODO: measure timeSpent
		if (colorToPlay) player1.recordMove(source, target, this, -1);
		else player2.recordMove(source, target, this, -1);
		
		history.recordMove(new Move(
				source,
				target,
				pieces[ti][tj],
				-1,
				totalMoves,
				colorToPlay
		));
		
		pieces[si][sj].movePiece(target.getCoords());
		pieces[ti][tj] = pieces[si][sj];
		pieces[si][sj] = null;
		
		latestState = pieces;
		colorToPlay = !colorToPlay;
	}
	
	
	public void highlightPossibleMoveSquares(Piece piece) {
		possibleMoves = piece.getAvalibleMoves(moveHandler);
		int i, j;
		for (ISquare s : possibleMoves) {
			i = s.getCoords().getI();
			j = s.getCoords().getJ();
			if (pieces[i][j] != null) {
				s.setColorAsAttacked();
			} else {
				s.setColorAsMovable();
			}
		}
	}
	
	public void resetPossibleMoveSquares() {
		for (int i=0; i<Board.SQUARES; i++)
		for (int j=0; j<Board.SQUARES; j++) {
			squares[i][j].setDefaultSquareColor();
		}
	}
	
	public void highlightSelectedSquare(ISquare square) {
		square.setColorAsSelected();
	}
	
	public void highlightHoveredSquare(ISquare square) {
		hlSquare = new HLSquare(square.getCoords());
	}
	
	public void resetSelectedPiece() {
		
	}

	
	

	

	public void undoOneMove(Painter painter) {
		if (totalMoves == 0) return;
		totalMoves--;
		
		// if whites turn, undo blacks move and make his turn to play
		// same for white
		if (colorToPlay) {
			player2.removeLastMoveFromHistory();
		} else {
			player1.removeLastMoveFromHistory();
		}
		colorToPlay = !colorToPlay;

		Move latestMove = history.getLastMove();
		int di = latestMove.getDest().getCoords().getI(),
			dj = latestMove.getDest().getCoords().getJ(),
			si = latestMove.getSource().getCoords().getI(),
			sj = latestMove.getSource().getCoords().getJ();
		
		System.out.println(latestMove.toString());
		
		// put piece that moved back to original square		
		pieces[si][sj] = pieces[di][dj];
		pieces[si][sj].movePiece(new Coordinates(si, sj));
		pieces[di][dj] = null;
		
		// if a piece was captured, restore it
		if (latestMove.getPieceCaptured() != null) {
			System.out.println("Piece was captured: "+latestMove.getPieceCaptured().getClass().getName());
			// could also use coordinates from int di, dj ?
			int i = latestMove.getPieceCaptured().getCoords().getI();
			int j = latestMove.getPieceCaptured().getCoords().getJ();
			pieces[i][j] = latestMove.pieceCaptured;
		}
		
		history.removeLastMoveFromHistory();
		painter.repaint();
	}
	
	public void redoOneMove(Painter painter) {
		System.out.println("Redo not yet implemented.");
		// TODO:
		// problems with histories from board and players
		// remove this (useless?) feature?
	}
	
	
	public void jumpToCurrentState() {
		// pieces = latestState; ??
		// TODO:
		// problem with histories again.
		// remove with redoOneMove() function?
	}
	public void resetMatch() {
		// just initPieces() again?
		// also, clear histories and reset totalMoves & currentMoves?
		// currentMoves var is useless?
	}
	
	
	
	public void resizeSquaresAndPieces() {
		for (int i=0; i<SQUARES; i++)
		for (int j=0; j<SQUARES; j++) {
			if (pieces[i][j] != null) pieces[i][j].updateDrawingPosition();
			squares[i][j].updateDrawingPosition();
		}
	}
	public void drawSquares(Graphics2D g) {
		for (int i=0; i<SQUARES; i++)
		for (int j=0; j<SQUARES; j++)
			squares[i][j].drawSquare(g);
	}
	public void drawPieces(Graphics2D g) {
		for (int i=0; i<SQUARES; i++)
		for (int j=0; j<SQUARES; j++)
			if (pieces[i][j] != null) pieces[i][j].drawPiece(g);
	}
	
	
	public void initSquares() {
		squares = new ISquare[SQUARES][SQUARES];
		for (int i=0; i<SQUARES; i++)
		for (int j=0; j<SQUARES; j++)
			squares[i][j] = ((i+j) % 2 == 0)? new Square1(new Coordinates(i, j)) : new Square2(new Coordinates(i, j));
	}
	public void initPieces() {
		pieces = new Piece[SQUARES][SQUARES];
		
		// WHITE PIECES, color = true
		// pawns
		for (int j=0, i=SQUARES-2; j<SQUARES; j++) pieces[i][j] = new Pawn(new Coordinates(i, j), true, "\u2659");
		
		// rooks
		pieces[SQUARES-1][0] = new Rook(new Coordinates(SQUARES-1, 0), true, "\u2656");
		pieces[SQUARES-1][SQUARES-1] = new Rook(new Coordinates(SQUARES-1, SQUARES-1), true, "\u2656");
		
		// king
		pieces[SQUARES-1][4] = new King(new Coordinates(SQUARES-1, 4), true, "\u2654");
		
		// queen
		pieces[SQUARES-1][3] = new Queen(new Coordinates(SQUARES-1, 3), true, "\u2655");
		
		// bishop
		pieces[SQUARES-1][2] = new Bishop(new Coordinates(SQUARES-1, 2), true, "\u2657");
		pieces[SQUARES-1][5] = new Bishop(new Coordinates(SQUARES-1, 5), true, "\u2657");
		
		
		// knights
		pieces[SQUARES-1][1] = new Knight(new Coordinates(SQUARES-1, 1), true, "\u2658");
		pieces[SQUARES-1][6] = new Knight(new Coordinates(SQUARES-1, 6), true,  "\u2658");
		
		// BLACK PIECES, color = false
		// pawns
		for (int j=0, i=1; j<SQUARES; j++) pieces[i][j] = new Pawn(new Coordinates(i, j), false, "\u265F");
		
		// rooks
		pieces[0][0] = new Rook(new Coordinates(0, 0), false, "\u2656");
		pieces[0][SQUARES-1] = new Rook(new Coordinates(0, SQUARES-1), false, "\u2656");
		
		// king
		pieces[0][4] = new King(new Coordinates(0, 4), false, "\u2654");
		
		// queen
		pieces[0][3] = new Queen(new Coordinates(0, 3), false, "\u2655");
		
		// bishop
		pieces[0][2] = new Bishop(new Coordinates(0, 2), false, "\u2657");
		pieces[0][5] = new Bishop(new Coordinates(0, 5), false, "\u2657");
		
		
		// knights
		pieces[0][1] = new Knight(new Coordinates(0, 1), false, "\u2658");
		pieces[0][6] = new Knight(new Coordinates(0, 6), false, "\u2658");
	}

	public void printBoardState() {
		System.out.println("BOARD STATE");
		String color = "";
		for (int i=0; i<SQUARES; i++) {
			for (int j=0; j<SQUARES; j++) {
				if (pieces[i][j] == null) {
					System.out.print(" _ ");
					continue;
				}
				
				System.out.print(pieces[i][j].getCharValue());
			}
			System.out.println("");
		}
		/*
		
		System.out.println("Coords");
		for (int i=0; i<SQUARES; i++) {
			for (int j=0; j<SQUARES; j++) {
				if (pieces[i][j] == null) {
					System.out.print(" __________ ");
					continue;
				}
				System.out.print(" " + pieces[i][j].coords.toStringDrawingCoords() + " ");
			}
			System.out.println("");
		}
		*/
	}
	
	

}