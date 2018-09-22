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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import com.cherokeelessons.gui.AbstractApp;
import com.cherokeelessons.gui.MainWindow;
import com.cherokeelessons.gui.MainWindow.Config;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Square;
import com.github.bhlangonijr.chesslib.game.GameContext;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import com.muksihs.ldg.chess1.models.NewGameInviteInfo;
import com.muksihs.ldg.chess1.models.PlayerChallenge;
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
import eu.bittrade.libs.steemj.configuration.SteemJConfig;
import eu.bittrade.libs.steemj.enums.PrivateKeyType;
import eu.bittrade.libs.steemj.exceptions.SteemCommunicationException;
import eu.bittrade.libs.steemj.exceptions.SteemInvalidTransactionException;
import eu.bittrade.libs.steemj.exceptions.SteemResponseException;
import eu.bittrade.libs.steemj.util.SteemJUtils;
import steem.models.ChessCommentMetadata;

public class Main extends AbstractApp {

	private final Board board;
	/**
	 * Java properties file to obtain <strong>posting</strong> key and account name
	 * from.
	 */
	private File authFile = null;

	public Main(Config config, String[] args) throws MoveGeneratorException {
		super(config, args);
		board = new Board(new GameContext(), true);
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
			extraMetadata.put("app", "LDG-Chess1/20180921");
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

		doUpvoteChecks();
		doAnnounceGamePost();
		doRunGameTurns();
		doNewPlayerSignups();

		if (true)
			return;

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

	private void doNewPlayerSignups() throws JsonParseException, JsonMappingException, SteemCommunicationException,
			SteemResponseException, IOException, SteemInvalidTransactionException {
		Set<PlayerChallenge> newPlayers = newPlayersWantingToPlay();
		Set<String> semaphores = getListOfActiveGames();
		Set<SignupReject> rejects = new HashSet<>();
		Iterator<PlayerChallenge> iPlayers = newPlayers.iterator();
		while (iPlayers.hasNext()) {
			PlayerChallenge newPlayerPair = iPlayers.next();
			AccountName challenger = newPlayerPair.getChallenger();
			AccountName challenged = newPlayerPair.getChallenged();
			Permlink permlink = newPlayerPair.getPermlink();
			String[] tags = newPlayerPair.getTags().toArray(new String[0]);
			String semaphore = "@" + challenger.getName();
			if (challenged != null) {
				semaphore += "-@" + challenged.getName();
			}
			if (semaphores.contains(semaphore)) {
				SignupReject reject = new SignupReject();
				reject.setChallenger(challenger);
				reject.setPermlink(permlink);
				reject.setTags(tags);
				reject.setReason("<html><h3>Rejected</h3><h4>Match already in progress.</h4></html>");
				rejects.add(reject);
				iPlayers.remove();
				continue;
			}
			if (challenger.equals(challenged)) {
				SignupReject reject = new SignupReject();
				reject.setChallenger(challenger);
				reject.setPermlink(permlink);
				reject.setTags(tags);
				reject.setReason("<html><h3>Rejected</h3><h4>You are not allowed to play yourself.</h4></html>");
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
					reject.setTags(tags);
					reject.setReason("<html><h3>Rejected</h3><h4>The account @" + challenged.getName()
							+ " does not exist.</h4></html>");
					rejects.add(reject);
					iPlayers.remove();
					continue;
				}
			}
		}

		doNewPlayerSignupRejects(rejects);
		
		List<PlayerMatch> matches = getConfirmedMathes(newPlayers);
		for (PlayerMatch match: matches) {
			System.out.println("=== MATCH: ");
			System.out.println(" -- Player 1");
			System.out.println("    @"+match.getPlayer1().getChallenger().getName());
			System.out.println("    @"+match.getPlayer1().getChallenged().getName());
			System.out.println(" -- Player 2");
			System.out.println("    @"+match.getPlayer2().getChallenger().getName());
			System.out.println("    @"+match.getPlayer2().getChallenged().getName());
		}

		iPlayers = newPlayers.iterator();
		while (iPlayers.hasNext()) {
			PlayerChallenge newPlayerPair = iPlayers.next();
			System.out.println(newPlayerPair.getChallenger().getName() + " challenges "
					+ (newPlayerPair.getChallenged() == null ? "ANYONE" : newPlayerPair.getChallenged().getName()));
		}
	}

	private List<PlayerMatch> getConfirmedMathes(Set<PlayerChallenge> newPlayers) {
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

	private void doNewPlayerSignupRejects(Set<SignupReject> rejects)
			throws SteemCommunicationException, SteemResponseException, SteemInvalidTransactionException {
		for (SignupReject reject : rejects) {
			retries: for (int retries = 0; retries < 10; retries++) {
				try {
					System.out.println(" Reject: " + reject.getChallenger().getName() + " "
							+ reject.getReason().replaceAll("<[^>]*?>", " "));
					waitCheckBeforeReplying(steemJ);
					steemJ.createComment(reject.getChallenger(), //
							reject.getPermlink(), //
							reject.getReason(), //
							reject.getTags());
					break retries;
				} catch (Exception e) {
				}
			}
		}
	}

	private Set<String> getListOfActiveGames() {
		return new HashSet<>();
	}

	private void doRunGameTurns() throws JsonParseException, JsonMappingException, IOException,
			SteemCommunicationException, SteemResponseException {
		Set<String> already = new HashSet<>();
		List<CommentBlogEntry> entries = steemJ.getBlog(botAccount, 0, (short) 100);
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
			if (metadata.getGameData() == null) {
				System.err.println("No chess game data: " + permlink.getLink());
				continue gameScan;
			}
		}
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
				// ignore any up vote errors
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
		tags[0] = "chess";
		tags[1] = "games";
		tags[2] = "new-game";
		tags[3] = "contest";
		tags[4] = "signup-" + info.getGameId();
		while (true) {
			waitIfLowBandwidth();
			try {
				System.out.println("POSTING: " + info.getTitle());
				waitCheckBeforePosting(steemJ);
				steemJ.createPost(info.getTitle(), info.getHtml(), tags, MIME_HTML, getAppMetadata());
				return;
			} catch (Exception e) {
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
		List<CommentBlogEntry> entries = steemJ.getBlog(botAccount, 0, (short) 100);
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
		Date oldestThreshold = DateUtils.addDays(new Date(), -5);
		return mostRecent.before(oldestThreshold);
	}

	private Set<PlayerChallenge> newPlayersWantingToPlay() throws SteemCommunicationException, SteemResponseException,
			JsonParseException, JsonMappingException, IOException {
		List<CommentBlogEntry> entries = steemJ.getBlog(botAccount, 0, (short) 100);
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
				String replyBody = reply.getBody();
				List<Discussion> botReplies = steemJ.getContentReplies(reply.getAuthor(), reply.getPermlink());
				if (botReplies == null) {
					continue;
				}
				replyBody = replyBody.toLowerCase();
				replyBody = replyBody.replaceAll("</?[^>]*?>", " ");
				replyBody = StringUtils.normalizeSpace(replyBody).trim();
				if (!replyBody.startsWith("play")) {
					continue replies;
				}
				maybeBotReplies: for (Discussion maybeBotReply : botReplies) {
					if (!botAccount.equals(maybeBotReply.getAuthor())) {
						continue maybeBotReplies;
					}
					String botReply = maybeBotReply.getBody().toLowerCase();
					botReply = botReply.replaceAll("</?[^>]*?>", " ");
					botReply = StringUtils.normalizeSpace(botReply).trim();
					if (botReply.startsWith("game started")) {
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

		System.out.println("Generating HTML for new game invite.");
		StringBuilder gameInvite = new StringBuilder();
		gameInvite.append("<html>");
		gameInvite.append("<div class='pull-right'>");
		gameInvite.append(
				"<img src='http://www.fen-to-image.com/image/128/double/coords/rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR'/>");
		gameInvite.append("</div>\n");
		gameInvite.append("<h1>(BETA!) Leather Dog Chess - New Game Invite</h1>\n");
		gameInvite.append("<h3>Attention Game Players!</h3>\n");
		gameInvite.append("<ul>\n");
		gameInvite.append("<li>Reply to this post with `play @specificusername`");
		gameInvite.append("to challenge a specific user to a chess game. ");
		gameInvite.append("They will need to reply to this post with a `play @yourusername`");
		gameInvite.append(" to accept your challenge.</li>\n");
		gameInvite.append("<li>Reply to this post with just `play` to join a random chess game.</li>\n");
		gameInvite.append("</ul>\n");
		gameInvite.append("<h3>ONLY FOR BETA TESTERS WHO DON" + RSQUO + "T MIND THINGS BREAKING!</h3>\n");
		gameInvite.append("<h4>More about Leather Dog Chess</h4>\n");
		gameInvite.append("<p>The first game will commence shortly");
		gameInvite.append(" after ");
		gameInvite.append(" there are at least two players registered.</p>\n");
		gameInvite.append("<p>Players will be assigned randomly.</p>\n");
		gameInvite.append("<p>Initiative will be assigned randomly.</p>\n");
		gameInvite.append("<p>This is <strong>NOT</strong> speed chess.");
		gameInvite.append(" The bot will not process orders more than a few times a day!</p>\n");
		gameInvite.append("<p>The game bot uses the same coordinate system as " + LDQUO
				+ "<a href='https://en.wikipedia.org/wiki/Algebraic_notation_(chess)'>");
		gameInvite.append("Algebraic notation (or AN)</a> for describing the moves in a game.</p>\n");
		gameInvite.append(
				"<p>Unlike full AN instead you reply with [FROM] [TO] for a move. And [FROM] [TO] [PROMOTE] for pawn promotions.</p>\n");
		gameInvite.append("<p>Better examples will be provided later.</p>\n");
		gameInvite.append(
				"<center><h4><a href='https://github.com/muksihs/LDG-Chess1'>https://github.com/muksihs/LDG-Chess1</a></h4></center>\n");
		gameInvite.append("</html>\n");

		String gameId = String.valueOf(System.currentTimeMillis() / 1000l / 60l / 5l);
		String title = "(BETA!) " + generateGameInviteTitle(gameId);
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
		String title = "Leather Dog Chess - New Player Signup! " + " - [" + gameId + "]";
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
