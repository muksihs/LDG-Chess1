package com.muksihs.ldg.chess1.tests;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
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
import com.muksihs.farhorizons.steemapi.RcAccount;
import com.muksihs.farhorizons.steemapi.RcAccounts;
import com.muksihs.farhorizons.steemapi.SteemRcApi;
import com.muksihs.ldg.chess1.DogChessUtils;

import eu.bittrade.libs.steemj.SteemJ;
import eu.bittrade.libs.steemj.base.models.AccountName;
import eu.bittrade.libs.steemj.base.models.ExtendedAccount;
import eu.bittrade.libs.steemj.configuration.SteemJConfig;
import eu.bittrade.libs.steemj.exceptions.SteemCommunicationException;
import eu.bittrade.libs.steemj.exceptions.SteemResponseException;

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
		ml.add(new Move("E2E3", Side.WHITE));
		ml.add(new Move("D7D5", Side.BLACK));
		ml.add(new Move("D2D4", Side.WHITE));
		ml.add(new Move("C8G4", Side.BLACK));

		List<String> moveList = new ArrayList<>();
		moveList.add("E2E3");
		moveList.add("D7D5");
		moveList.add("D2D4");
		moveList.add("C8G4");

		game.setHalfMoves(ml);
		game.gotoLast();

		System.out.println("PGN:\n" + game.toPgn(true, true));
		System.out.println("FEN: " + board.getFen(true));
		System.out.println("Draw: " + board.isDraw());
		System.out.println("Mated: " + board.isMated());
		System.out.println("Stalemate: " + board.isStaleMate());

		if (moveList.size() > 2) {
			game.gotoPrior();
			String boardOneMoveAgo = game.getBoard().getFen();
			game.gotoPrior();
			String boardTwoMovesAgo = game.getBoard().getFen();
			game.gotoLast();
			System.out.println("-Most Recent Moves");
			String twoMovesAgo = moveList.get(moveList.size() - 3);
			System.out
					.println(DogChessUtils.getJinchessHtml(boardTwoMovesAgo, "", "", StringUtils.left(twoMovesAgo, 4)));
			String oneMoveAgo = moveList.get(moveList.size() - 2);
			System.out.println(DogChessUtils.getJinchessHtml(boardOneMoveAgo, "", "", StringUtils.left(oneMoveAgo, 4)));
		}

	}
	
	@Test
	public void reportAvailableRcs() throws JsonParseException, JsonMappingException, IOException {
		AccountName account = new AccountName("leatherdog-games");
		RcAccounts rcs;
			rcs = SteemRcApi.getRc(account);
		List<RcAccount> rcAccounts = rcs.getRcAccounts();
		for (RcAccount rc : rcAccounts) {
			BigDecimal estimatedMana = rc.getEstimatedMana();
			System.out.println("--- Available RCs " + NumberFormat.getInstance().format(estimatedMana));
		}
	}

	@Test
	public void votingPower() throws Exception {
		SteemJ steemJ = getAnonAcccount();
		List<AccountName> accountNames = new ArrayList<>();
		accountNames.add(new AccountName("leatherdog-games"));
		accountNames.add(new AccountName("muksihs"));
		accountNames.add(new AccountName("pupmisfit"));
		List<ExtendedAccount> accounts = steemJ.getAccounts(accountNames);
		for (ExtendedAccount account : accounts) {
			System.out.println("=== " + account.getName().getName());
			System.out.println("Last vote time: " + account.getLastVoteTime().getDateTime());
			System.out.println(account.getVotingPower() + " => " + DogChessUtils.getEstimateVote(account));
		}
	}

	public SteemJ getAnonAcccount() throws SteemCommunicationException, SteemResponseException {
		SteemJConfig myConfig = SteemJConfig.getInstance();
		myConfig.setEncodingCharset(StandardCharsets.UTF_8);
		myConfig.setIdleTimeout(250);
		myConfig.setResponseTimeout(1000);
		myConfig.setBeneficiaryAccount(new AccountName("muksihs"));
		myConfig.setSteemJWeight((short) 0);
		return new SteemJ();
	}
}
