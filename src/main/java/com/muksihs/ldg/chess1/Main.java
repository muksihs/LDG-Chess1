package com.muksihs.ldg.chess1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.cherokeelessons.gui.AbstractApp;
import com.cherokeelessons.gui.MainWindow;
import com.cherokeelessons.gui.MainWindow.Config;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Constants;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.game.GameContext;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;

public class Main extends AbstractApp {

	private final Board board;

	public Main(Config config, String[] args) throws MoveGeneratorException {
		super(config, args);
		board = new Board(new GameContext(), true);
	}

	public static void main(String[] args) {
		MainWindow.Config config = new Config() {
			@Override
			public String getApptitle() {
				return "LDG Chess 1";
			}

			@Override
			public Runnable getApp(String... args) throws Exception {
				return new Main(this, args);
			}
		};
		MainWindow.init(config, args);
	}

	@Override
	protected void parseArgs(Iterator<String> iargs) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void execute() throws IOException, SecurityException, Exception {
		// TODO Auto-generated method stub
		// "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
		// "rnbqkbnr/ppp1pppp/8/3p4/3P4/4P3/PPP11PPP/RNBQKBNR w KQkq - 0 1"
		List<Move> history = new ArrayList<>();
		Move move = new Move(Square.E2, Square.E3);
		history.add(move);
		board.doMove(move, true);
		move = new Move(Square.D7, Square.D5);
		history.add(move);
		board.doMove(move, true);
		move = new Move(Square.D2, Square.D4);
		history.add(move);
		board.doMove(move, true);
		move = new Move(Square.C8, Square.G4);
		Set<Move> legal = new HashSet<>(MoveGenerator.generateLegalMoves(board));
		if (!legal.contains(move)) {
			System.err.println("Not a legal move.");
			System.out.println(DogChessUtils.getChessboardUrl(board.getFen(false)));
		} else {
			history.add(move);
			board.doMove(move, true);
			System.out.println(DogChessUtils.getChessboardUrl(board.getFen(false)));
			System.out.println(DogChessUtils.getChessboardUrlRotated(board.getFen(false)));
			System.out.println("FEN: " + board.getFen());
			System.out.println("SIDE TO MOVE: " + board.getSideToMove());
			System.out.println("HISTORY: " + history);
		}
	}

}
