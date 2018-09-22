package com.muksihs.ldg.chess1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

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
import com.muksihs.ldg.chess1.models.SteemAccountInformation;

import eu.bittrade.libs.steemj.SteemJ;
import eu.bittrade.libs.steemj.apis.database.models.state.Discussion;
import eu.bittrade.libs.steemj.apis.follow.model.BlogEntry;
import eu.bittrade.libs.steemj.base.models.AccountName;
import eu.bittrade.libs.steemj.base.models.DynamicGlobalProperty;
import eu.bittrade.libs.steemj.base.models.ExtendedAccount;
import eu.bittrade.libs.steemj.base.models.VoteState;
import eu.bittrade.libs.steemj.configuration.SteemJConfig;
import eu.bittrade.libs.steemj.enums.PrivateKeyType;
import eu.bittrade.libs.steemj.exceptions.SteemCommunicationException;
import eu.bittrade.libs.steemj.exceptions.SteemResponseException;
import steem.models.CommentMetadata;

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
	private Map<String, Object> getAppMetadata(){
		if (extraMetadata==null) {
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
	private void doUpvoteCheck() throws SteemCommunicationException, SteemResponseException, JsonParseException,
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
			CommentMetadata metadata = json.readValue(article.getJsonMetadata(), CommentMetadata.class);
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
}
