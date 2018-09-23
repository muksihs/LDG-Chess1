package com.muksihs.ldg.chess1.tests;

import org.testng.annotations.Test;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.game.Event;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.game.GameFactory;
import com.github.bhlangonijr.chesslib.game.GameResult;
import com.github.bhlangonijr.chesslib.game.Player;
import com.github.bhlangonijr.chesslib.game.PlayerType;
import com.github.bhlangonijr.chesslib.game.Round;
import com.github.bhlangonijr.chesslib.game.VariationType;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;

public class Tester {
	@Test
	public void pgn1() throws Exception {
		Board board = new Board();
		board.getContext().setVariationType(VariationType.NORMAL);
		
		Player p1 = GameFactory.newPlayer(PlayerType.HUMAN, "@muksihs");
		Player p2 = GameFactory.newPlayer(PlayerType.HUMAN, "@pupmisfit");
		
		Event newGame = GameFactory.newEvent("Leather Dog Chess 1");
		newGame.setEndDate("");
		newGame.setSite("https://busy.org/@leatherdog-games");
		newGame.setStartDate("2018-09-23");
		
		Round startingRound = GameFactory.newRound(newGame, 0);
		
		Game game = GameFactory.newGame("54321", startingRound);
		game.setBoard(board);
		game.setDate("2018-09-23");
		game.setGameId("54321");
		game.setResult(GameResult.ONGOING);
		game.setVariation(board.getContext().getVariationType().name());
		game.setWhitePlayer(p1);
		game.setBlackPlayer(p2);
		game.setMoveText(new StringBuilder());
		
		MoveList ml = new MoveList();
		ml.add(new Move(Square.E2, Square.E3));
		ml.add(new Move(Square.D7, Square.D5));
		ml.add(new Move(Square.D2, Square.D4));
		ml.add(new Move(Square.C8, Square.G4));
		
		game.setHalfMoves(ml);
		game.gotoLast();
		
		System.out.println("PGN:\n"+game.toPgn(true, true));
		System.out.println("FEN: "+board.getFen(true));
		System.out.println("Draw: "+board.isDraw());
		System.out.println("Mated: "+board.isMated());
		System.out.println("Stalemate: "+board.isStaleMate());
		
	}
	
	@Test
	public void board1() throws Exception {
//		PgnHolder pgn = new PgnHolder("");
//		Game game = new Game("12345", new Round(new Event()));
//		game.getBlackPlayer();
//		game.setBoard(new Board());
//		game.loadMoveText();
//		Board board = game.getBoard();
//		System.out.println("PGN: "+game.toPgn(true, true));
//		System.out.println("FEN: "+board.getFen(true));
	}
}
