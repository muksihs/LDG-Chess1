package com.muksihs.ldg.chess1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import com.cherokeelessons.gui.AbstractApp;
import com.cherokeelessons.gui.MainWindow;
import com.cherokeelessons.gui.MainWindow.Config;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
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
import com.github.bhlangonijr.chesslib.move.MoveConversionException;
import com.github.bhlangonijr.chesslib.move.MoveException;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import com.github.bhlangonijr.chesslib.move.MoveList;
import com.muksihs.ldg.chess1.models.MoveResponse;
import com.muksihs.ldg.chess1.models.NewGameInviteInfo;
import com.muksihs.ldg.chess1.models.PlayerChallenge;
import com.muksihs.ldg.chess1.models.PlayerMatch;
import com.muksihs.ldg.chess1.models.SignupReject;
import com.muksihs.ldg.chess1.models.SteemAccountInformation;

import eu.bittrade.libs.steemj.SteemJ;
import eu.bittrade.libs.steemj.apis.database.models.state.Discussion;
import eu.bittrade.libs.steemj.apis.follow.model.BlogEntry;
import eu.bittrade.libs.steemj.apis.follow.model.CommentBlogEntry;
import eu.bittrade.libs.steemj.base.models.AccountName;
import eu.bittrade.libs.steemj.base.models.DynamicGlobalProperty;
import eu.bittrade.libs.steemj.base.models.ExtendedAccount;
import eu.bittrade.libs.steemj.base.models.Permlink;
import eu.bittrade.libs.steemj.base.models.TimePointSec;
import eu.bittrade.libs.steemj.base.models.VoteState;
import eu.bittrade.libs.steemj.base.models.operations.CommentOperation;
import eu.bittrade.libs.steemj.configuration.SteemJConfig;
import eu.bittrade.libs.steemj.enums.PrivateKeyType;
import eu.bittrade.libs.steemj.exceptions.SteemCommunicationException;
import eu.bittrade.libs.steemj.exceptions.SteemInvalidTransactionException;
import eu.bittrade.libs.steemj.exceptions.SteemResponseException;
import eu.bittrade.libs.steemj.util.SteemJUtils;
import steem.models.ChessCommentMetadata;
import steem.models.ChessGameData;

public class Main extends AbstractApp {

	/**
	 * Java properties file to obtain <strong>posting</strong> key and account name
	 * from.
	 */
	private File authFile = null;

	public Main(Config config, String[] args) throws MoveGeneratorException {
		super(config, args);
		config.setAutoExit(true);
		config.setAutoExitOnError(true);
		initJackson();
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
		while (iargs.hasNext()) {
			String arg = iargs.next();
			switch (arg) {
			case "--auth-file":
				if (!iargs.hasNext()) {
					throw new IllegalArgumentException("You must provide an auth file.");
				}
				authFile = new File(iargs.next());
				if (authFile.isDirectory()) {
					throw new IllegalArgumentException(
							"Auth file can not be a directory: " + authFile.getAbsolutePath());
				}
				if (!authFile.canRead()) {
					throw new IllegalArgumentException(
							"Missing or unreadable auth file: " + authFile.getAbsolutePath());
				}
				break;
			default:
				throw new IllegalArgumentException("Unknown CLI option: " + arg);
			}
		}
		if (authFile == null) {
			throw new IllegalArgumentException("You must provide an auth file using --auth-file <FILE-NAME>.");
		}
	}

	private Map<String, Object> extraMetadata;

	private Map<String, Object> getAppMetadata() {
		if (extraMetadata == null) {
			extraMetadata = new HashMap<>();
			extraMetadata.put("app", "LDG-Chess1/20180923");
		}
		return new HashMap<>(extraMetadata);
	}

	private SteemJ steemJ;
	private AccountName botAccount;
	private ObjectMapper json;

	@Override
	protected void execute() throws IOException, SecurityException, Exception {
		SteemAccountInformation accountInfo = getKeyAuthData(authFile);
		steemJ = initilizeSteemJ(accountInfo);
		botAccount = accountInfo.getAccountName();

		if (DogChessUtils.doRcAbortCheck(botAccount)) {
			return;
		}
		doRunGameTurns();
		if (DogChessUtils.doRcAbortCheck(botAccount)) {
			return;
		}
		doStartNewMatches();
		if (DogChessUtils.doRcAbortCheck(botAccount)) {
			return;
		}
		doAnnounceGamePost();
		if (DogChessUtils.doRcAbortCheck(botAccount)) {
			return;
		}
		doUpvoteChecks();
	}

	private void doStartNewMatches()
			throws JsonParseException, JsonMappingException, SteemCommunicationException, SteemResponseException,
			IOException, SteemInvalidTransactionException, MoveException, MoveConversionException {
		Set<PlayerChallenge> newPlayers = newPlayersWantingToPlay();
		Set<String> activeMatches = getListOfActiveMatches();
		Set<SignupReject> rejects = new HashSet<>();
		Iterator<PlayerChallenge> iPlayers = newPlayers.iterator();
		while (iPlayers.hasNext()) {
			PlayerChallenge newPlayerPair = iPlayers.next();
			AccountName challenger = newPlayerPair.getChallenger();
			AccountName challenged = newPlayerPair.getChallenged();
			Permlink permlink = newPlayerPair.getPermlink();
			String[] tags = newPlayerPair.getTags().toArray(new String[0]);
			String wantedMatch = "@" + challenger.getName();
			String wantedMatchInverse = "-@" + challenger.getName();
			if (challenged != null) {
				wantedMatch += "-@" + challenged.getName();
				wantedMatchInverse = "@" + challenged.getName() + wantedMatchInverse;
			}

			if (activeMatches.contains(wantedMatch) || activeMatches.contains(wantedMatchInverse)) {
				SignupReject reject = new SignupReject();
				reject.setChallenger(challenger);
				reject.setPermlink(permlink);
				reject.setReason("<html><h4>Rejected</h4><h5>Match already in progress.</h5></html>");
				reject.setTags(tags);
				rejects.add(reject);
				iPlayers.remove();
				continue;
			}
			if (challenger.equals(challenged)) {
				SignupReject reject = new SignupReject();
				reject.setChallenger(challenger);
				reject.setPermlink(permlink);
				reject.setReason("<html><h4>Rejected</h4><h5>You are not allowed to play yourself.</h5></html>");
				reject.setTags(tags);
				rejects.add(reject);
				iPlayers.remove();
				continue;
			}
			if (challenged != null) {
				List<ExtendedAccount> valid = steemJ.getAccounts(Arrays.asList(challenged));
				if (valid == null || valid.isEmpty()) {
					SignupReject reject = new SignupReject();
					reject.setChallenger(challenger);
					reject.setPermlink(permlink);
					reject.setReason("<html><h4>Rejected</h4><h5>The account @" + challenged.getName()
							+ " does not exist.</h5></html>");
					reject.setTags(tags);
					rejects.add(reject);
					iPlayers.remove();
					continue;
				}
			}
		}

		doPlayerSignupRejects(rejects);

		List<PlayerMatch> matches = getConfirmedMatches(newPlayers);
		List<PlayerMatch> otherMatches = getRandomMatches(newPlayers, matches);
		matches.addAll(otherMatches);
		Map<String, Permlink> gameLinks = doPostNewMatches(matches);
		for (PlayerMatch match : matches) {
			Permlink permlink = gameLinks.get(match.getSemaphore());
			if (permlink == null) {
				continue;
			}
			notifyPlayersOfGameStart(match, permlink);
		}
	}

	private void notifyPlayersOfGameStart(PlayerMatch match, Permlink permlink) {
		notifyPlayerOfGameStart(permlink, match.getPlayer1());
		notifyPlayerOfGameStart(permlink, match.getPlayer2());
	}

	private void notifyPlayerOfGameStart(Permlink permlink, PlayerChallenge player1) {
		Permlink parentPermlink = player1.getPermlink();
		String[] tags = player1.getTags().toArray(new String[0]);
		StringBuilder notice = new StringBuilder();
		notice.append("<html>\n");
		notice.append("Match started: <a href='");
		notice.append("https://busy.org/" + tags[0] + "/@" + botAccount.getName() + "/" + permlink.getLink());
		notice.append("'>");
		notice.append(permlink.getLink());
		notice.append("</a>\n");
		notice.append("</html>\n");
		retries: for (int retries = 0; retries < 10; retries++) {
			try {
				waitCheckBeforeReplying(steemJ);
				if (DogChessUtils.doRcAbortCheck(botAccount)) {
					return;
				}
				steemJ.createComment(player1.getChallenger(), parentPermlink, notice.toString(), tags, MIME_HTML,
						getAppMetadata());
				break retries;
			} catch (SteemCommunicationException | SteemResponseException | SteemInvalidTransactionException e) {
				if (e.getMessage().contains("wait to transact")) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private Map<String, Permlink> doPostNewMatches(List<PlayerMatch> matches)
			throws MoveException, MoveConversionException {
		Map<String, Permlink> gameLinks = new HashMap<>();
		for (PlayerMatch match : matches) {
			Board board = new Board();
			board.getContext().setVariationType(VariationType.NORMAL);

			String player1 = "@" + match.getPlayer1().getChallenger().getName();
			String player2 = "@" + match.getPlayer2().getChallenger().getName();

			Player p1 = GameFactory.newPlayer(PlayerType.HUMAN, player1);
			Player p2 = GameFactory.newPlayer(PlayerType.HUMAN, player2);

			Event newGame = GameFactory.newEvent("Leather Dog Chess 1");
			newGame.setEndDate("");
			newGame.setSite("https://busy.org/@leatherdog-games");
			String startDate = new java.sql.Date(System.currentTimeMillis()).toString();
			newGame.setStartDate(startDate);

			Round startingRound = GameFactory.newRound(newGame, 0);

			Game game = GameFactory.newGame("", startingRound);
			game.setBoard(board);
			game.setDate(startDate);
			game.setResult(GameResult.ONGOING);
			game.setVariation(board.getContext().getVariationType().name());
			if (new Random().nextBoolean()) {
				game.setWhitePlayer(p1);
				game.setBlackPlayer(p2);
			} else {
				game.setWhitePlayer(p2);
				game.setBlackPlayer(p1);
			}
			String wpName = game.getWhitePlayer().getName();
			String bpName = game.getBlackPlayer().getName();
			String gameId = "game-" + (System.currentTimeMillis() / (1000l * 60l * 5l));
			game.setGameId(gameId);
			game.setMoveText(new StringBuilder());

			MoveList ml = new MoveList();

			game.setHalfMoves(ml);
			game.gotoLast();

			StringBuilder gameTitle = new StringBuilder();
			gameTitle.append("Chess " + wpName + " vs " + bpName + " - Round " + (1 + game.getRound().getNumber())
					+ " - " + board.getSideToMove() + LSQUO + "s MOVE [" + gameId + "]");

			System.out.println("=== " + gameTitle);

			ChessGameData cgd = new ChessGameData();
			cgd.setDraw(board.isDraw());
			cgd.setFen(board.getFen(true));
			cgd.setGameId(gameId);
			cgd.setMated(board.isMated());
			cgd.setMoveList(new ArrayList<>());
			cgd.setPgn(game.toPgn(true, true));
			cgd.setPlayerBlack(bpName);
			cgd.setPlayerWhite(wpName);
			cgd.setSideToMove(board.getSideToMove().name());
			cgd.setPlayerToMove(board.getSideToMove().equals(Side.WHITE) ? wpName : bpName);
			cgd.setStalemate(board.isStaleMate());
			cgd.setVariationType(board.getContext().getVariationType().name());

			Map<String, Object> metadata = getAppMetadata();
			metadata.put("chessGameData", cgd);

			List<String> tags = new ArrayList<>();
			tags.add("playbypost");
			tags.add("chess");
			tags.add("steemchess");
			tags.add("chess-match");
			tags.add(gameId);

			String turnHtml = generateTurnHtml(cgd);

			retries: for (int retries = 0; retries < 10; retries++) {
				try {
					waitCheckBeforePosting(steemJ);
					if (DogChessUtils.doRcAbortCheck(botAccount)) {
						throw new RuntimeException("INSUFFICENT RCs");
					}
					CommentOperation info = steemJ.createPost(gameTitle.toString(), turnHtml,
							tags.toArray(new String[0]), MIME_HTML, metadata);
					gameLinks.put(match.getSemaphore(), info.getPermlink());
					break;
				} catch (SteemCommunicationException | SteemResponseException | SteemInvalidTransactionException e) {
					if (e.getMessage().contains("wait to transact")) {
						throw new RuntimeException(e);
					}
					continue retries;
				}
			}
		}
		return gameLinks;
	}

	private String generateTurnHtml(ChessGameData cgd) {
		// TODO Switch to using template HTML
		String WHITE_ORIENTATION = DogChessUtils.getJinchessHtml(cgd.getFen(), cgd.getSideToMove());
		String BLACK_ORIENTATION = DogChessUtils.getJinchessRotatedHtml(cgd.getFen(), cgd.getSideToMove());
		boolean isWhiteToMove = cgd.getSideToMove().equalsIgnoreCase("white");

		StringBuilder sb = new StringBuilder();
		sb.append("<html>\n");

		sb.append("<p><center><strong>");
		sb.append(
				cgd.getPlayerWhite() + " vs " + cgd.getPlayerBlack() + " Round " + (1 + cgd.getMoveList().size() / 2));
		sb.append("</strong></center></p>\n");

		sb.append("<p><center><strong>");
		sb.append(cgd.getSideToMove() + RSQUO + "S MOVE (" + cgd.getPlayerToMove() + ")");
		sb.append("</strong></center></p>\n");

		sb.append("<p><center>\n");
		sb.append(isWhiteToMove ? WHITE_ORIENTATION : BLACK_ORIENTATION);
		sb.append("\n</center></p>\n");
		sb.append("<p><center>\n");
		sb.append(isWhiteToMove ? BLACK_ORIENTATION : WHITE_ORIENTATION);
		sb.append("\n</center></p>\n");
		sb.append("<hr/>\n");
		sb.append(getInstructionsHtml());
		sb.append("<hr/>\n");

		sb.append("<p><center>");
		sb.append("Images created with");
		sb.append(" <a target='_blank' href='http://www.jinchess.com/chessboard/composer/'>");
		sb.append("http://www.jinchess.com/chessboard/composer/");
		sb.append("</a>");
		sb.append("</center></p>");

		sb.append("<p><center>");
		sb.append("Github: ");
		sb.append(" <a target='_blank' href='https://github.com/muksihs/LDG-Chess1'>");
		sb.append("LDG-Chess1");
		sb.append("</a>");
		sb.append("</center></p>");

		sb.append("<p>FEN: ");
		sb.append(cgd.getFen());
		sb.append("</p>\n");
		sb.append("<p>FAN: ");
		sb.append(cgd.getFan());
		sb.append("</p>\n");
		sb.append("<p>SAN: ");
		sb.append(cgd.getSan());
		sb.append("</p>\n");
		sb.append("</html>\n");
		return sb.toString();
	}

	private StringBuilder getInstructionsHtml() {
		StringBuilder sb = new StringBuilder();
		sb.append("<div>");
		sb.append("<strong>To move a piece</strong> reply with 'MOVE: '");
		sb.append("followed by the square to move a piece from ");
		sb.append("followed by the square to move a piece into.");
		sb.append(" Example:<ul><li>move: e2 e3</li></ul>");
		sb.append("</div>\n");

		sb.append("<div>");
		sb.append("When moving a <strong>pawn for promotion</strong> reply with 'MOVE: '");
		sb.append("followed by the square to move a piece from ");
		sb.append("followed by the square to move a piece into ");
		sb.append("followed by the name of the piece to promote the pawn to ");
		sb.append("(queen, knight, rook, or bishop).");
		sb.append(" Example:<ul><li>move: b2 b1 knight</li></ul>");
		sb.append("</div>\n");

		// sb.append("<div>");
		// sb.append("To perform an <strong>en passant</strong> reply with 'MOVE: square
		// from square to en passant'.");
		// sb.append(" Example:<ul><li>move: e5 f6 en passant</li></ul>");
		// sb.append("</div>\n");

		sb.append("<div>");
		sb.append("To <strong>request a draw</strong> reply with 'MOVE: draw?'.");
		sb.append("</div>\n");

		sb.append("<div>");
		sb.append("To <strong>concede</strong> reply with 'MOVE: resign'.");
		sb.append("</div>\n");

		sb.append("<div>");
		sb.append(
				"Any reply that does not start with 'MOVE: ' on the first line will be ignored so that normal commenting and replying can occur on the turn's post.");
		sb.append("</div>\n");

		sb.append("<div>");
		sb.append(
				"If you don't make a move within 6 days you will automatically be considered as conceding victory in your match.");
		sb.append("</div>\n");

		return sb;
	}

	private List<PlayerMatch> getRandomMatches(Set<PlayerChallenge> newPlayers, List<PlayerMatch> excluding) {
		List<PlayerChallenge> playerList = new ArrayList<>(newPlayers);

		List<PlayerMatch> matches = new ArrayList<>();
		Set<Permlink> alreadyPermlink = new HashSet<>();
		Set<String> already = new HashSet<>();

		for (PlayerMatch match : excluding) {
			alreadyPermlink.add(match.getPlayer1().getPermlink());
			alreadyPermlink.add(match.getPlayer2().getPermlink());
		}

		playerList.removeIf(p -> p.getChallenged() != null);
		playerList.removeIf(p -> alreadyPermlink.contains(p.getPermlink()));

		Collections.shuffle(playerList);
		ListIterator<PlayerChallenge> iPlayers = playerList.listIterator();
		while (iPlayers.hasNext()) {
			PlayerChallenge player1 = iPlayers.next();
			if (!iPlayers.hasNext()) {
				break;
			}
			PlayerChallenge player2 = iPlayers.next();
			PlayerMatch match = new PlayerMatch();
			match.setPlayer1(player1);
			match.setPlayer2(player2);
			if (already.contains(match.getSemaphore())) {
				// go back one so that player2 becomes player1 for next in list
				iPlayers.previous();
				continue;
			}
			matches.add(match);
			already.add(match.getSemaphore());
			already.add(match.getInverseSemaphore());
		}

		return matches;
	}

	private List<PlayerMatch> getConfirmedMatches(Set<PlayerChallenge> newPlayers) {
		List<PlayerMatch> matches = new ArrayList<>();
		List<PlayerChallenge> player1List = new ArrayList<>(newPlayers);
		List<PlayerChallenge> player2List = new ArrayList<>(newPlayers);
		Set<String> already = new HashSet<>();
		Iterator<PlayerChallenge> iPlayer1 = player1List.iterator();
		while (iPlayer1.hasNext()) {
			PlayerChallenge player1 = iPlayer1.next();
			Iterator<PlayerChallenge> iPlayer2 = player2List.iterator();
			while (iPlayer2.hasNext()) {
				PlayerChallenge player2 = iPlayer2.next();
				if (!player1.isMatchFor(player2)) {
					continue;
				}
				iPlayer1.remove();
				iPlayer2.remove();
				PlayerMatch match = new PlayerMatch();
				match.setPlayer1(player1);
				match.setPlayer2(player2);
				if (already.contains(match.getSemaphore())) {
					continue;
				}
				matches.add(match);
				already.add(match.getSemaphore());
				already.add(match.getInverseSemaphore());
			}
		}
		return matches;
	}

	private void doPlayerSignupRejects(Set<SignupReject> rejects)
			throws SteemCommunicationException, SteemResponseException, SteemInvalidTransactionException {
		for (SignupReject reject : rejects) {
			retries: for (int retries = 0; retries < 10; retries++) {
				try {
					System.out.println(" Reject: " + reject.getChallenger().getName() + " "
							+ getParsableBodyText(reject.getReason()));
					waitCheckBeforeReplying(steemJ);
					if (DogChessUtils.doRcAbortCheck(botAccount)) {
						return;
					}
					steemJ.createComment(reject.getChallenger(), //
							reject.getPermlink(), //
							reject.getReason(), //
							reject.getTags());
					break retries;
				} catch (Exception e) {
					if (e.getMessage().contains("wait to transact")) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	private void doPlayerResponses(Collection<MoveResponse> responses) {
		for (MoveResponse response : responses) {
			retries: for (int retries = 0; retries < 10; retries++) {
				try {
					String[] tags = null;
					Discussion content = steemJ.getContent(response.getPlayer(), response.getPermlink());
					if (content != null) {
						try {
							ChessCommentMetadata ccm = json.readValue(content.getJsonMetadata(),
									ChessCommentMetadata.class);
							tags = ccm.getTags();
						} catch (IOException e) {
						}
					}

					if (tags == null || tags.length == 0) {
						tags = new String[] { "chess" };
					}
					System.out.println("Response: @" + response.getPlayer().getName() + " => " + response.getReason());
					waitCheckBeforeReplying(steemJ);
					steemJ.createComment(response.getPlayer(), //
							response.getPermlink(), //
							response.getReason(), //
							tags);
					break retries;
				} catch (SteemCommunicationException | SteemResponseException | SteemInvalidTransactionException e) {
					if (e.getMessage().contains("wait to transact")) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	private Set<String> getListOfActiveMatches() throws SteemCommunicationException, SteemResponseException,
			JsonParseException, JsonMappingException, IOException {
		Map<Permlink, ChessGameData> activeGames = getActiveGames();
		Set<String> semaphores = new HashSet<>();
		for (ChessGameData activeGame : activeGames.values()) {
			PlayerMatch match = new PlayerMatch();
			PlayerChallenge player1 = new PlayerChallenge();
			PlayerChallenge player2 = new PlayerChallenge();
			player1.setChallenger(new AccountName(activeGame.getPlayerWhite().substring(1)));
			player2.setChallenger(new AccountName(activeGame.getPlayerBlack().substring(1)));
			match.setPlayer1(player1);
			match.setPlayer2(player2);
			semaphores.add(match.getSemaphore());
			semaphores.add(match.getInverseSemaphore());
		}
		return semaphores;
	}

	private void doRunGameTurns() throws JsonParseException, JsonMappingException, IOException,
			SteemCommunicationException, SteemResponseException, MoveException, MoveGeneratorException,
			MoveConversionException, SteemInvalidTransactionException {
		Map<Permlink, ChessGameData> activeGames = getActiveGames();
		System.out.println("=== " + NF.format(activeGames.size()) + " active games.");
		Set<Permlink> activeGamesKeySet = activeGames.keySet();
		Iterator<Permlink> iActiveGames = activeGamesKeySet.iterator();
		List<MoveResponse> responses = new ArrayList<>();
		activeGames: while (iActiveGames.hasNext()) {
			if (DogChessUtils.doRcAbortCheck(botAccount)) {
				return;
			}
			Permlink permlink = iActiveGames.next();
			ChessGameData activeGame = activeGames.get(permlink);
			AccountName playerToMove = new AccountName(activeGame.getPlayerToMove().substring(1));
			AccountName playerWhite = new AccountName(activeGame.getPlayerWhite().substring(1));
			AccountName playerBlack = new AccountName(activeGame.getPlayerBlack().substring(1));
			AccountName otherPlayer;
			if (playerToMove.equals(playerWhite)) {
				otherPlayer = playerBlack;
			} else {
				otherPlayer = playerWhite;
			}
			List<Discussion> replies = steemJ.getContentReplies(botAccount, permlink);
			if (replies == null) {
				continue activeGames;
			}
			String theMove;
			playerReplies: for (Discussion playerReply : replies) {
				String[] tags;
				try {
					ChessCommentMetadata ccm = json.readValue(playerReply.getJsonMetadata(),
							ChessCommentMetadata.class);
					tags = ccm.getTags();
				} catch (Exception e1) {
					tags = null;
				}
				if (tags == null || tags.length == 0) {
					tags = new String[] { "chess" };
				}
				if (!playerToMove.equals(playerReply.getAuthor())) {
					System.out.println(" -- Skipping reply by: " + playerReply.getAuthor().getName());
					continue playerReplies;
				}
				// make sure this hasn't been replied to by the bot with a 'reject'
				Permlink playerReplyPermlink = playerReply.getPermlink();
				List<Discussion> botReplies = steemJ.getContentReplies(playerReply.getAuthor(), playerReplyPermlink);
				if (botReplies != null) {
					botReplies: for (Discussion botReply : botReplies) {
						if (!botReply.getAuthor().equals(botAccount)) {
							continue botReplies;
						}
						String botReplyBody = getParsableBodyText(botReply.getBody());
						if (botReplyBody.startsWith("reject")) {
							System.out.println(
									" -- Skipping previously rejected move: " + playerReply.getAuthor().getName());
							continue playerReplies;
						}
						if (botReplyBody.startsWith("confirm draw")) {
							List<Discussion> drawReplies = steemJ.getContentReplies(botAccount, botReply.getPermlink());
							if (drawReplies == null) {
								// skip to next game
								continue activeGames;
							}
							otherPlayerReplies: for (Discussion otherPlayerReply : drawReplies) {
								if (!otherPlayerReply.getAuthor().equals(otherPlayer)) {
									continue otherPlayerReplies;
								}
								String drawBody = getParsableBodyText(otherPlayerReply.getBody());
								if (drawBody.startsWith("confirm draw") || drawBody.startsWith("move confirm draw")) {
									activeGame.setDraw(true);
									continue activeGames;
								}
								if (drawBody.startsWith("reject draw") || drawBody.startsWith("move reject draw")) {
									MoveResponse response = new MoveResponse();
									response.setPermlink(playerReply.getPermlink());
									response.setPlayer(playerReply.getAuthor());
									response.setReason("<html><h4>REJECTED<h4><h5>@" + otherPlayer.getName()
											+ " does not want to declare a draw.</h5></html>\n");
									responses.add(response);
									iActiveGames.remove();
									continue activeGames;
								}
							}
						}
					}
				}

				String playerReplyBody = playerReply.getBody();
				String move = getParsableBodyText(playerReplyBody);
				String[] moveParts = move.trim().split("\\s");
				if (moveParts == null) {
					System.out.println(" -- Skipping unparsable reply body: " + playerReply.getAuthor().getName());
					continue playerReplies;
				}
				ListIterator<String> iMove = Arrays.asList(moveParts).listIterator();
				if (!iMove.hasNext()) {
					System.out.println(" -- Skipping empty reply body: " + playerReply.getAuthor().getName());
					continue playerReplies;
				}
				iMove: while (iMove.hasNext()) {
					String next = iMove.next();
					if (next.startsWith("draw")) {
						MoveResponse response = new MoveResponse();
						response.setPermlink(playerReplyPermlink);
						response.setPlayer(playerReply.getAuthor());
						response.setReason("<html><h4>CONFIRM DRAW</h4><h5>@" //
								+ otherPlayer.getName() //
								+ " to declare a draw respond with: confirm draw<br/>" //
								+ "To reject the draw request respond with:" //
								+ " reject draw</h5>" //
								+ "If you don't respond in 6 days you will be resigned" //
								+ " from the game." //
								+ "</html>\n");
						responses.add(response);
						iActiveGames.remove();
						continue activeGames;
					}
					if (next.startsWith("resign") || next.startsWith("concede")) {
						activeGame.setResigned(true);
						activeGame.setResignedBy(playerToMove.getName());
						continue activeGames;
					}
					if (!next.startsWith("move")) {
						System.out.println(" -- Skipping not a move reply body: " + playerReply.getAuthor().getName());
						System.out.println(" -- " + playerReplyBody);
						continue playerReplies;
					}
					if (!iMove.hasNext()) {
						MoveResponse response = new MoveResponse();
						response.setPermlink(playerReplyPermlink);
						response.setPlayer(playerReply.getAuthor());
						response.setReason("REJECTED\nNo squares specified.");
						System.out.println(response.getReason());
						continue playerReplies;
					}
					String s1 = iMove.next();
					if (!iMove.hasNext() && s1.length() < 4) {
						MoveResponse response = new MoveResponse();
						response.setPermlink(playerReplyPermlink);
						response.setPlayer(playerReply.getAuthor());
						response.setReason("REJECTED\nNo destination square specified.");
						System.out.println(response.getReason());
						continue playerReplies;
					}
					String s2;
					if (s1.length() >= 4) {
						s2 = s1.substring(2);
						s1 = s1.substring(0, 2);
					} else {
						s2 = iMove.next();
					}
					String promotion = "";
					if (s2.length() > 2) {
						promotion = s2.substring(2);
						s2 = s2.substring(0, 2);
					}
					try {
						Square.valueOf(s1.toUpperCase());
					} catch (Exception e) {
						MoveResponse response = new MoveResponse();
						response.setPermlink(playerReplyPermlink);
						response.setPlayer(playerReply.getAuthor());
						response.setReason(
								"REJECTED\nSource square not understood.\nMust be from a1 to h8. Do not specify extra information such as the piece.\n");
						System.out.println(response.getReason());
						continue playerReplies;
					}
					try {
						Square.valueOf(s2.toUpperCase());
					} catch (Exception e) {
						MoveResponse response = new MoveResponse();
						response.setPermlink(playerReplyPermlink);
						response.setPlayer(playerReply.getAuthor());
						response.setReason(
								"REJECTED\nDestination square not understood.\nMust be from a1 to h8. Do not specify extra information such as the piece.\n");
						System.out.println(response.getReason());
						continue playerReplies;
					}

					if (StringUtils.isBlank(promotion) && iMove.hasNext()) {
						promotion = iMove.next();
					}
					if (promotion.startsWith("queen")) {
						promotion = "QUEEN";
					}
					if (promotion.startsWith("knight")) {
						promotion = "KNIGHT";
					}
					if (promotion.startsWith("rook")) {
						promotion = "ROOK";
					}
					if (promotion.startsWith("bishop")) {
						promotion = "BISHOP";
					}
					switch (promotion) {
					case "QUEEN":
					case "KNIGHT":
					case "ROOK":
					case "BISHIP":
						System.out.println("Promotion: " + promotion);
						break;
					default:
						promotion = "";
					}
					theMove = s1 + s2 + promotion;
					System.out.println("Move: " + theMove);
					if (!processActiveGameMove(activeGame, playerReply, theMove, responses)) {
						iActiveGames.remove();
					}
					continue activeGames;
				}
			}
		}
		doPlayerResponses(responses);
	}

	// TODO
	private boolean processActiveGameMove(ChessGameData activeGame, Discussion playerReply, String theMove,
			List<MoveResponse> responses) throws MoveException, MoveGeneratorException, MoveConversionException, JsonParseException, JsonMappingException, IOException {
		Board board = new Board();
		try {
			board.getContext().setVariationType(VariationType.valueOf(activeGame.getVariationType()));
		} catch (Exception e) {
			board.getContext().setVariationType(VariationType.NORMAL);
		}

		Player whitePlayer = GameFactory.newPlayer(PlayerType.HUMAN, activeGame.getPlayerWhite());
		Player blackPlayer = GameFactory.newPlayer(PlayerType.HUMAN, activeGame.getPlayerBlack());

		Event newGame = GameFactory.newEvent("Leather Dog Chess 1");
		newGame.setEndDate("");
		newGame.setSite("https://busy.org/@leatherdog-games");
		String startDate = activeGame.getStartDate() == null ? new java.sql.Date(System.currentTimeMillis()).toString()
				: activeGame.getStartDate();
		newGame.setStartDate(startDate);

		Round startingRound = GameFactory.newRound(newGame, 0);

		Game game = GameFactory.newGame("", startingRound);
		game.setBoard(board);
		game.setDate(startDate);
		game.setResult(GameResult.ONGOING);
		game.setVariation(board.getContext().getVariationType().name());

		String gameId = activeGame.getGameId();
		game.setGameId(gameId);
		game.setMoveText(new StringBuilder());

		game.setWhitePlayer(whitePlayer);
		game.setBlackPlayer(blackPlayer);

		MoveList ml = new MoveList();
		if (!StringUtils.isBlank(activeGame.getSan())) {
			ml.loadFromSan(activeGame.getSan());
		}

		game.setHalfMoves(ml);
		game.gotoLast();

		MoveList legal = MoveGenerator.generateLegalMoves(board);

		Move move = new Move(theMove, board.getSideToMove());

		if (!legal.contains(move)) {
			MoveResponse response = new MoveResponse();
			response.setPermlink(playerReply.getPermlink());
			response.setPlayer(playerReply.getAuthor());
			response.setReason("REJECTED\nNot a valid move.");
			System.out.println(response.getReason());
			responses.add(response);
			return false;
		}

		ml.add(move);
		game.setHalfMoves(ml);
		game.gotoLast();

		StringBuilder gameTitle = new StringBuilder();
		gameTitle.append("Chess " + whitePlayer + " vs " + blackPlayer + " - Round " + (1 + ml.size() / 2) + " - "
				+ board.getSideToMove() + LSQUO + "s MOVE [" + gameId + "]");

		System.out.println("=== " + gameTitle);

		ChessGameData cgd = activeGame;
		cgd.setDraw(board.isDraw());
		cgd.setFen(board.getFen(true));
		cgd.setGameId(gameId);
		cgd.setMated(board.isMated());
		cgd.getMoveList().add(theMove);
		cgd.setPgn(game.toPgn(true, true));
		cgd.setPlayerBlack(blackPlayer.getName());
		cgd.setPlayerWhite(whitePlayer.getName());
		cgd.setSideToMove(board.getSideToMove().name());
		cgd.setPlayerToMove(board.getSideToMove().equals(Side.WHITE) ? whitePlayer.getName() : blackPlayer.getName());
		cgd.setStalemate(board.isStaleMate());
		cgd.setVariationType(board.getContext().getVariationType().name());

		cgd.setFan(game.getHalfMoves().toFan());
		cgd.setSan(game.getHalfMoves().toSan());

		Map<String, Object> metadata = getAppMetadata();
		metadata.put("chessGameData", cgd);

		List<String> tags = new ArrayList<>();
		tags.add("playbypost");
		tags.add("chess");
		tags.add("steemchess");
		tags.add("chess-match");
		tags.add(gameId);

		String turnHtml = generateTurnHtml(cgd);

		retries: for (int retries = 0; retries < 10; retries++) {
			try {
				waitCheckBeforePosting(steemJ);
				if (DogChessUtils.doRcAbortCheck(botAccount)) {
					throw new RuntimeException("INSUFFICENT RCs");
				}
				CommentOperation info = steemJ.createPost(gameTitle.toString(), turnHtml, tags.toArray(new String[0]),
						MIME_HTML, metadata);
				break;
			} catch (SteemCommunicationException | SteemResponseException | SteemInvalidTransactionException e) {
				if (e.getMessage().contains("wait to transact")) {
					throw new RuntimeException(e);
				}
				continue retries;
			}
		}

		return true;
	}

	private String getParsableBodyText(String bodyText) {
		return StringUtils.normalizeSpace(bodyText.replaceAll("<[^>]*?>", " ").replace(":", " ").toLowerCase().trim());
	}

	private Map<Permlink, ChessGameData> getActiveGames() throws SteemCommunicationException, SteemResponseException,
			IOException, JsonParseException, JsonMappingException {
		Map<Permlink, ChessGameData> activeGames = new HashMap<>();

		Set<String> already = new HashSet<>();
		List<CommentBlogEntry> entries = getCachedBlogEntries();
		gameScan: for (CommentBlogEntry entry : entries) {
			// if not by game master, SKIP
			if (entry.getComment() == null) {
				System.err.println("NULL Comment?");
				continue;
			}
			if (!entry.getComment().getAuthor().equals(botAccount)) {
				continue;
			}

			Permlink permlink = entry.getComment().getPermlink();
			ChessCommentMetadata metadata = json.readValue(entry.getComment().getJsonMetadata(),
					ChessCommentMetadata.class);
			if (metadata == null) {
				System.err.println("No metadata: " + permlink.getLink());
				continue gameScan;
			}
			Set<String> tags = new HashSet<>(Arrays.asList(metadata.getTags()));

			if (!tags.contains("chess")) {
				continue gameScan;
			}
			if (tags.contains("new-game")) {
				continue gameScan;
			}
			String gameId = null;
			for (String tag : tags) {
				if (!tag.startsWith("game-")) {
					continue;
				}
				gameId = StringUtils.substringAfter(tag, "-");
				if (already.contains(tag)) {
					continue gameScan;
				}
				already.add(tag);
				break;
			}
			if (gameId == null) {
				continue gameScan;
			}
			if (metadata.getChessGameData() == null) {
				System.err.println("No chess game data: " + permlink.getLink());
				continue gameScan;
			}
			activeGames.put(permlink, metadata.getChessGameData());
		}
		return activeGames;
	}

	private List<CommentBlogEntry> cachedBlogEntries = null;

	private List<CommentBlogEntry> getCachedBlogEntries() throws SteemCommunicationException, SteemResponseException {
		if (cachedBlogEntries == null) {
			cachedBlogEntries = steemJ.getBlog(botAccount, 0, (short) 200);
		}
		return new ArrayList<>(cachedBlogEntries);
	}

	/**
	 * Upvote any posts that we haven't up voted yet that others have voted on, this
	 * rewards people who upvote our posts to encourage them to keep up voting our
	 * posts. Note: We do not up vote posts that have no up votes. We want to reward
	 * users, not waste voting power on looking like only doing self serving voting.
	 * Only vote if we have enough effective voting power currently available to
	 * have a worthwhile impact.
	 * 
	 * @throws SteemCommunicationException
	 * @throws SteemResponseException
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	private void doUpvoteChecks() throws SteemCommunicationException, SteemResponseException, JsonParseException,
			JsonMappingException, IOException {

		BigDecimal voteThreshold = new BigDecimal("90.00");
		List<BlogEntry> entriesForUpvote = new ArrayList<>();
		for (int retries = 0; retries < 10; retries++) {
			try {
				entriesForUpvote = steemJ.getBlogEntries(botAccount, 0, (short) 100);
			} catch (SteemResponseException e1) {
				e1.printStackTrace();
				break;
			}
		}
		forBlogEntries: for (BlogEntry entry : entriesForUpvote) {
			// if not by game master, SKIP
			if (!entry.getAuthor().equals(botAccount)) {
				continue;
			}
			// stop up voting if our voting power drops too low
			BigDecimal votingPower = new BigDecimal(getExtendedAccount().getVotingPower()).movePointLeft(2);
			if (votingPower.compareTo(voteThreshold) < 0) {
				System.out.println("Not up voting. Power " + votingPower + "% < " + voteThreshold + "%");
				break forBlogEntries;
			}
			Discussion article = null;
			for (int retries = 0; retries < 10; retries++) {
				try {
					article = steemJ.getContent(botAccount, entry.getPermlink());
					break;
				} catch (SteemResponseException e1) {
					if (retries == 9) {
						throw e1;
					}
					sleep(500);
				}
			}
			ChessCommentMetadata metadata = json.readValue(article.getJsonMetadata(), ChessCommentMetadata.class);
			Set<String> tags = new HashSet<>(Arrays.asList(metadata.getTags()));
			if (!tags.contains("chess")) {
				continue forBlogEntries;
			}
			boolean isGame = false;
			for (String tag : tags) {
				if (tag.startsWith("game-")) {
					isGame = true;
					break;
				}
			}
			if (!isGame) {
				continue forBlogEntries;
			}
			// must have at least two other votes
			if (article.getNetVotes() <= 1) {
				continue forBlogEntries;
			}
			// must have at least 0.017 payout value
			if (article.getPendingPayoutValue().toReal() < 0.017d) {
				continue forBlogEntries;
			}
			List<VoteState> votes = article.getActiveVotes();
			for (VoteState vote : votes) {
				if (vote.getVoter().equals(botAccount)) {
					continue forBlogEntries;
				}
			}
			waitIfLowBandwidth();
			// up vote this post. it has other votes already and we haven't up voted it yet.
			try {
				System.out.println("Upvoting: " + votingPower + "%");
				System.out.println(article.getTitle());
				steemJ.vote(botAccount, article.getPermlink(), (short) 100);
				sleep(3500);
			} catch (Exception e) {
				if (e.getMessage().contains("wait to transact")) {
					throw new RuntimeException(e);
				}
				// ignore any other up vote error types
				System.err.println("Error on up vote. IGNORED.");
				System.err.println(e.getClass().getName() + ":\n" + e.getMessage());
			}
		}
	}

	private void waitIfLowBandwidth() {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		nf.setRoundingMode(RoundingMode.DOWN);
		String prev = "";
		while (isLowBandwidth()) {
			try {
				String available = nf.format((100d - 100d * getBandwidthUsedPercent()));
				if (!prev.equals(available)) {
					prev = available;
					System.err.println("Low bandwidth. Waiting. " + available + "%");
				}
				Thread.sleep(1000l * 30l);
			} catch (SteemCommunicationException | SteemResponseException | InterruptedException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	private boolean isLowBandwidth() {
		try {
			double bandwidthUsed = (double) Math.ceil(10000d * getBandwidthUsedPercent()) / 100d;
			if ((100d - bandwidthUsed) < bandwidthRequiredPercent) {
				return true;
			}
			return false;
		} catch (SteemCommunicationException | SteemResponseException e) {
			return true;
		}
	}

	private double bandwidthRequiredPercent = 65d;

	private double getBandwidthUsedPercent() throws SteemCommunicationException, SteemResponseException {
		double MILLION = 1000000d;
		double STEEMIT_BANDWIDTH_AVERAGE_WINDOW_SECONDS = 60 * 60 * 24 * 7;
		ExtendedAccount extendedAccount = getExtendedAccount();
		DynamicGlobalProperty dynamicGlobalProperties = null;
		for (int retries = 0; retries < 10; retries++) {
			try {
				dynamicGlobalProperties = steemJ.getDynamicGlobalProperties();
			} catch (SteemResponseException e) {
				if (retries == 9) {
					throw e;
				}
				sleep(500);
			}
		}
		double vestingShares = extendedAccount.getVestingShares().getAmount();
		double receivedVestingShares = extendedAccount.getReceivedVestingShares().getAmount();
		double totalVestingShares = dynamicGlobalProperties.getTotalVestingShares().getAmount();
		double maxVirtualBandwidth = Double
				.valueOf(dynamicGlobalProperties.getMaxVirtualBandwidth().replaceAll(" .*", ""));
		double averageBandwidth = extendedAccount.getAverageBandwidth();
		double deltaTimeSecs = Math.round(
				new Date().getTime() - extendedAccount.getLastBandwidthUpdate().getDateTimeAsDate().getTime()) / 1000d;
		double bandwidthAllocated = (maxVirtualBandwidth * (vestingShares + receivedVestingShares))
				/ totalVestingShares;
		bandwidthAllocated = Math.round(bandwidthAllocated / MILLION);
		double newBandwidth = 0d;
		if (deltaTimeSecs < STEEMIT_BANDWIDTH_AVERAGE_WINDOW_SECONDS) {
			newBandwidth = (((STEEMIT_BANDWIDTH_AVERAGE_WINDOW_SECONDS - deltaTimeSecs) * averageBandwidth)
					/ STEEMIT_BANDWIDTH_AVERAGE_WINDOW_SECONDS);
			newBandwidth = Math.round(newBandwidth / MILLION);
		}
		double bandwidthUsedPercent = newBandwidth / bandwidthAllocated;
		return bandwidthUsedPercent;
	}

	private ExtendedAccount getExtendedAccount() throws SteemCommunicationException, SteemResponseException {
		for (int retries = 0; retries < 10; retries++) {
			try {
				List<ExtendedAccount> accounts = steemJ.getAccounts(Arrays.asList(botAccount));
				if (accounts.isEmpty()) {
					return null;
				}
				return accounts.iterator().next();
			} catch (Exception e) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
				}

			}
		}
		return null;
	}

	private SteemAccountInformation getKeyAuthData(File authFile) throws FileNotFoundException, IOException {
		SteemAccountInformation account = new SteemAccountInformation();
		Properties steemConfig = new Properties();
		steemConfig.load(new FileInputStream(authFile));
		account.setActiveKey(steemConfig.getProperty("activeKey"));
		if (account.getActiveKey() == null || account.getActiveKey().trim().isEmpty()) {
			account.setActiveKey("");
		}
		account.setPostingKey(steemConfig.getProperty("postingKey"));
		if (account.getPostingKey() == null || account.getPostingKey().trim().isEmpty()) {
			account.setPostingKey("");
		}
		String tmp = steemConfig.getProperty("accountName");
		if (tmp == null || tmp.trim().isEmpty()) {
			throw new IllegalArgumentException("accountName= for steem account name not found");
		}
		account.setAccountName(new AccountName(tmp));
		if (account.getPostingKey() == "") {
			throw new IllegalArgumentException("Private posting key not found");
		}
		return account;
	}

	private SteemJ initilizeSteemJ(SteemAccountInformation accountInfo)
			throws SteemCommunicationException, SteemResponseException {
		SteemJConfig myConfig = SteemJConfig.getInstance();
		myConfig.setEncodingCharset(StandardCharsets.UTF_8);
		myConfig.setIdleTimeout(250);
		myConfig.setResponseTimeout(1000);
		myConfig.setBeneficiaryAccount(new AccountName("muksihs"));
		myConfig.setSteemJWeight((short) 0);
		myConfig.setDefaultAccount(accountInfo.getAccountName());
		List<ImmutablePair<PrivateKeyType, String>> privateKeys = new ArrayList<>();
		if (!accountInfo.getActiveKey().trim().isEmpty()) {
			privateKeys.add(new ImmutablePair<>(PrivateKeyType.ACTIVE, accountInfo.getActiveKey()));
		}
		if (!accountInfo.getPostingKey().trim().isEmpty()) {
			privateKeys.add(new ImmutablePair<>(PrivateKeyType.POSTING, accountInfo.getPostingKey()));
		}
		myConfig.getPrivateKeyStorage().addAccount(myConfig.getDefaultAccount(), privateKeys);
		return new SteemJ();
	}

	private void initJackson() {
		json = new ObjectMapper();
		json.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		json.setDateFormat(dateFormat);
	}

	private void doAnnounceGamePost() throws IOException, SteemCommunicationException, SteemResponseException {

		if (!isNeedAnnounceNewGamePost()) {
			System.err.println("--- NO ANNOUNCE GAME JOIN POST - ONE STILL ACTIVE");
			return;
		}
		System.err.println("--- DO ANNOUNCE NEW GAME JOIN POST");

		NewGameInviteInfo info = generateNewGameInviteHtml();
		String[] tags = new String[5];
		tags[0] = "playbypost";
		tags[1] = "chess";
		tags[2] = "games";
		tags[3] = "contest";
		tags[4] = "new-game";
		while (true) {
			waitIfLowBandwidth();
			try {
				System.out.println("POSTING: " + info.getTitle());
				waitCheckBeforePosting(steemJ);
				if (DogChessUtils.doRcAbortCheck(botAccount)) {
					throw new RuntimeException("INSUFFICENT RCs");
				}
				steemJ.createPost(info.getTitle(), info.getHtml(), tags, MIME_HTML, getAppMetadata());
				return;
			} catch (Exception e) {
				if (e.getMessage().contains("wait to transact")) {
					throw new RuntimeException(e);
				}
				System.err.println("Posting error. Sleeping 5 minutes.");
				if (e.getMessage().contains("STEEMIT_MIN_ROOT_COMMENT_INTERVAL")) {
					System.err.println("STEEMIT_MIN_ROOT_COMMENT_INTERVAL");
				} else {
					System.err.println(e.getClass().getName() + ":\n" + e.getMessage());
				}
				sleep(5l * 60l * 1000l);
			}
		}
	}

	private boolean isNeedAnnounceNewGamePost() throws SteemCommunicationException, SteemResponseException,
			JsonParseException, JsonMappingException, IOException {
		List<CommentBlogEntry> entries = getCachedBlogEntries();
		Date mostRecent = new Date(0);
		gameScan: for (CommentBlogEntry entry : entries) {
			// if not by game master, SKIP
			if (entry.getComment() == null) {
				System.err.println("NULL Comment?");
				continue;
			}
			if (!entry.getComment().getAuthor().equals(botAccount)) {
				continue;
			}

			Permlink permlink = entry.getComment().getPermlink();
			ChessCommentMetadata metadata = json.readValue(entry.getComment().getJsonMetadata(),
					ChessCommentMetadata.class);
			if (metadata == null) {
				System.err.println("No metadata: " + permlink.getLink());
				continue gameScan;
			}
			Set<String> tags = new HashSet<>(Arrays.asList(metadata.getTags()));

			if (!tags.contains("chess")) {
				continue gameScan;
			}
			if (!tags.contains("new-game")) {
				continue gameScan;
			}
			Date created = entry.getComment().getCreated().getDateTimeAsDate();
			if (mostRecent.before(created)) {
				mostRecent = created;
			}
		}
		Date oldestThreshold = DateUtils.addDays(new Date(), -6);
		return mostRecent.before(oldestThreshold);
	}

	private Set<PlayerChallenge> newPlayersWantingToPlay() throws SteemCommunicationException, SteemResponseException,
			JsonParseException, JsonMappingException, IOException {
		List<CommentBlogEntry> entries = getCachedBlogEntries();
		Date oldestThreshold = DateUtils.addDays(new Date(), -15);
		Set<PlayerChallenge> pairings = new HashSet<>();
		gameScan: for (CommentBlogEntry entry : entries) {
			// if not by game master, SKIP
			if (entry.getComment() == null) {
				System.err.println("NULL Comment?");
				continue;
			}
			if (!entry.getComment().getAuthor().equals(botAccount)) {
				continue;
			}
			Permlink permlink = entry.getComment().getPermlink();
			ChessCommentMetadata metadata = json.readValue(entry.getComment().getJsonMetadata(),
					ChessCommentMetadata.class);
			if (metadata == null) {
				System.err.println("No metadata: " + permlink.getLink());
				continue gameScan;
			}
			Set<String> tags = new HashSet<>(Arrays.asList(metadata.getTags()));
			if (!tags.contains("chess")) {
				continue gameScan;
			}
			if (!tags.contains("new-game")) {
				continue gameScan;
			}
			Date created = entry.getComment().getCreated().getDateTimeAsDate();
			if (created.before(oldestThreshold)) {
				continue gameScan;
			}
			List<Discussion> replies = steemJ.getContentReplies(botAccount, permlink);
			if (replies == null || replies.isEmpty()) {
				continue gameScan;
			}
			replies: for (Discussion reply : replies) {
				AccountName challenger = reply.getAuthor();
				List<Discussion> botReplies = steemJ.getContentReplies(reply.getAuthor(), reply.getPermlink());
				if (botReplies == null) {
					continue;
				}
				String replyBody = getParsableBodyText(reply.getBody());
				if (!replyBody.startsWith("play")) {
					continue replies;
				}
				maybeBotReplies: for (Discussion maybeBotReply : botReplies) {
					if (!botAccount.equals(maybeBotReply.getAuthor())) {
						continue maybeBotReplies;
					}
					String botReply = getParsableBodyText(maybeBotReply.getBody());
					if (botReply.startsWith("game started")) {
						continue replies;
					}
					if (botReply.startsWith("match started")) {
						continue replies;
					}
					if (botReply.startsWith("rejected")) {
						continue replies;
					}
				}
				PlayerChallenge pair = new PlayerChallenge();
				pair.setChallenger(challenger);
				pair.setPermlink(reply.getPermlink());
				pair.setTags(Arrays.asList(metadata.getTags()));
				extractChallenged: if (replyBody.contains("@")) {
					replyBody = StringUtils.substringAfter(replyBody, "play").trim();
					if (!replyBody.startsWith("@")) {
						break extractChallenged;
					}
					replyBody = StringUtils.substringBefore(replyBody, " ").trim();
					replyBody = replyBody.substring(1); // skip the '@'
					AccountName challenged = new AccountName(replyBody);
					pair.setChallenged(challenged);
				}
				pairings.add(pair);
			}
		}
		return pairings;
	}

	private static final String MIME_HTML = "text/html";
	private static final String LDQUO = "\u201c";
	private static final String RDQUO = "\u201d";
	private static final String LSQUO = "\u2018";
	private static final String RSQUO = "\u2019";

	private static String basicEscape(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replaceAll(">", "&gt;");
	}

	private NewGameInviteInfo generateNewGameInviteHtml() throws IOException {

		String gameId = String.valueOf(System.currentTimeMillis() / 1000l / 60l / 5l);

		System.out.println("Generating HTML for new game invite.");
		StringBuilder gameInvite = new StringBuilder();
		gameInvite.append("<html>");
		gameInvite.append("<div class='pull-right'>");
		gameInvite.append(
				"<img src='http://www.jinchess.com/chessboard/?p=rnbqkbnrpppppppp--------------------------------PPPPPPPPRNBQKBNR&tm=w&s=xl&bp=wooden-dark&cm=o'/>");
		gameInvite.append("</div>\n");
		gameInvite.append("<h1>Leather Dog Chess - New Game Invite</h1>\n");
		gameInvite.append("<h3>Attention Game Players!</h3>\n");
		gameInvite.append("<ul>\n");
		gameInvite.append("<li>Reply to this post with `play @specificusername`");
		gameInvite.append("to challenge a specific user to a chess game. ");
		gameInvite.append("They will need to reply to this post with a `play @yourusername`");
		gameInvite.append(" to accept your challenge.</li>\n");
		gameInvite.append("<li>Reply to this post with just `play` to join a random chess game.</li>\n");
		gameInvite.append("</ul>\n");
		gameInvite.append("<p>Initiative will be assigned randomly.</p>\n");
		gameInvite.append("<p>This is <strong>NOT</strong> speed chess.");
		gameInvite.append(" The bot will not process orders more than a few times a day!</p>\n");
		gameInvite.append("<center><h4>");
		gameInvite.append("<a href='https://github.com/muksihs/LDG-Chess1'>");
		gameInvite.append("https://github.com/muksihs/LDG-Chess1");
		gameInvite.append("</a>");
		gameInvite.append("</h4></center>\n");
		gameInvite.append("The bot is still a work in progress. Sometimes it hiccups.");
		gameInvite.append("</html>\n");

		String title = generateGameInviteTitle(gameId);
		String permlink = "@" + botAccount.getName() + "/" + SteemJUtils.createPermlinkString(title);
		String gameInviteHtml = gameInvite.toString();
		gameInviteHtml = gameInviteHtml.replace(_PERMLINK, permlink);
		NewGameInviteInfo info = new NewGameInviteInfo();
		info.setGameId(gameId);
		info.setPermlink(permlink);
		info.setTitle(title);
		info.setHtml(gameInviteHtml);
		return info;
	}

	private static final String _PERMLINK = "_permlink_";

	private String generateGameInviteTitle(String gameId) {
		String title = "Leather Dog Chess - Player Match Signups! " + " - [" + gameId + "]";
		return title;
	}

	private GregorianCalendar newGameDeadline(Date date) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		return newGameDeadline(cal);
	}

	private GregorianCalendar newGameDeadline(GregorianCalendar cal) {
		cal.setTimeZone(EST5EDT);
		cal.add(GregorianCalendar.DAY_OF_YEAR, +2);
		// cal.add(GregorianCalendar.HOUR_OF_DAY, 2);
		int minute = cal.get(GregorianCalendar.MINUTE);
		// use int math to set to lowest matching quarter hour value;
		minute /= 15;
		minute *= 15;
		cal.set(GregorianCalendar.MINUTE, minute);
		cal.set(GregorianCalendar.SECOND, 0);
		cal.set(GregorianCalendar.MILLISECOND, 0);
		DateFormat df = DateFormat.getDateTimeInstance();
		df.setTimeZone(EST5EDT);
		// String deadlineEst5Edt = basicEscape(df.format(cal.getTime())) + " EST5EDT";
		return cal;
	}

	private GregorianCalendar newTurnDeadline(Date date) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		return newTurnDeadline(cal);
	}

	private GregorianCalendar newTurnDeadline(GregorianCalendar cal) {
		cal.setTimeZone(EST5EDT);
		cal.add(GregorianCalendar.DAY_OF_YEAR, +3);
		int minute = cal.get(GregorianCalendar.MINUTE);
		// use int math to set to lowest matching quarter hour value;
		minute /= 15;
		minute *= 15;
		cal.set(GregorianCalendar.MINUTE, minute);
		cal.set(GregorianCalendar.SECOND, 0);
		cal.set(GregorianCalendar.MILLISECOND, 0);
		DateFormat df = DateFormat.getDateTimeInstance();
		df.setTimeZone(EST5EDT);
		// String deadlineEst5Edt = basicEscape(df.format(cal.getTime())) + " EST5EDT";
		return cal;
	}

	private static final TimeZone EST5EDT = TimeZone.getTimeZone("EST5EDT");
	private static final String DIV_PULL_RIGHT_START = "<div class='pull-right' style='float:right;padding:1rem;max-width:50%;'>";
	private static final String DIV_PULL_LEFT_START = "<div class='pull-left' style='float:left;padding:1rem;max-width:50%;'>";

	private void waitCheckBeforePosting(SteemJ steemJ) throws SteemCommunicationException, SteemResponseException {
		long FIVE_MINUTES = 1000l * 60l * 5l;
		long EXTRA_SECOND = 1000l;
		SteemJConfig config = SteemJConfig.getInstance();
		AccountName account = config.getDefaultAccount();
		while (true) {
			List<ExtendedAccount> info = steemJ.getAccounts(Arrays.asList(account));
			TimePointSec now = steemJ.getDynamicGlobalProperties().getTime();
			TimePointSec lastPostTime = now;
			for (ExtendedAccount e : info) {
				lastPostTime = e.getLastRootPost();
				break;
			}
			long since = now.getDateTimeAsTimestamp() - lastPostTime.getDateTimeAsTimestamp();
			long sleepFor = FIVE_MINUTES + EXTRA_SECOND - since;
			if (sleepFor < 0) {
				return;
			}
			log.info("Last post was within 5 minutes. Sleeping " + NF.format(sleepFor / 60000f) + " minutes.");
			sleep(sleepFor);
		}
	}

	private void waitCheckBeforeReplying(SteemJ steemJ) throws SteemCommunicationException, SteemResponseException {
		final long MIN_DELAY = 1000l * 3l;
		final long EXTRA_DELAY = 1000l;
		SteemJConfig config = SteemJConfig.getInstance();
		AccountName account = config.getDefaultAccount();
		while (true) {
			List<ExtendedAccount> info = steemJ.getAccounts(Arrays.asList(account));
			TimePointSec now = steemJ.getDynamicGlobalProperties().getTime();
			TimePointSec lastPostTime = now;
			for (ExtendedAccount e : info) {
				lastPostTime = e.getLastPost();
				break;
			}
			long since = now.getDateTimeAsTimestamp() - lastPostTime.getDateTimeAsTimestamp();
			long sleepFor = MIN_DELAY + EXTRA_DELAY - since;
			if (sleepFor < 0) {
				return;
			}
			log.info("Last reply was within 3 seconds. Sleeping " + NF.format(sleepFor / 1000f) + " seconds.");
			sleep(sleepFor);
		}
	}

	private static final NumberFormat NF = NumberFormat.getInstance();
}
