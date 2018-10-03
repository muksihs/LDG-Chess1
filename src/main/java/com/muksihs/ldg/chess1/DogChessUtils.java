package com.muksihs.ldg.chess1;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import com.muksihs.farhorizons.steemapi.RcAccount;
import com.muksihs.farhorizons.steemapi.RcAccounts;
import com.muksihs.farhorizons.steemapi.SteemRcApi;

import eu.bittrade.libs.steemj.base.models.AccountName;
import eu.bittrade.libs.steemj.base.models.ExtendedAccount;

public class DogChessUtils {
	public static String whiteKing = "\u2654";
	public static String whiteQueen = "\u2655";
	public static String whiteRook = "\u2656";
	public static String whiteBishop = "\u2657";
	public static String whiteKnight = "\u2658";
	public static String whitePawn = "\u2659";

	public static String blackKing = "\u265a";
	public static String blackQueen = "\u265b";
	public static String blackRook = "\u265c";
	public static String blackBishop = "\u265d";
	public static String blackKnight = "\u265e";
	public static String blackPawn = "\u265f";

	private static final String URL_FEN2IMAGE = "http://www.fen-to-image.com/image/128/double/coords/_FEN_?chessboard.png";
	private static final String URL_FEN2IMAGE_ROTATED = "http://www.fen-to-image.com/image/128/double/_FEN_?chessboard-rotated.png";

	public static String getFen2ImageRotatedMarkdown(String fen) {
		return "![CHESS BOARD ROTATED](" + getFen2ImageRotatedUrl(fen) + ")";
	}

	public static String getFen2ImageRotatedHtml(String fen) {
		return "<img alt='CHESS BOARD ROTATED' src='" + getFen2ImageRotatedUrl(fen) + "'/>";
	}

	public static String getFen2ImageHtml(String fen) {
		return "<img alt='CHESS BOARD' src='" + getFen2ImageUrl(fen) + "'/>";
	}

	public static String getFen2ImageUrl(String fen) {
		fen = StringUtils.substringBefore(fen, " ");
		return URL_FEN2IMAGE.replace("_FEN_", fen);
	}

	public static String getFen2ImageRotatedUrl(String fen) {
		fen = StringUtils.substringBefore(fen, " ");
		return URL_FEN2IMAGE_ROTATED.replace("_FEN_", StringUtils.reverse(fen));
	}

	private static final String URL_JINCHESS_FEN2IMAGE = "http://www.jinchess.com/chessboard/?p=_FEN_&tm=_SIDE_&s=xl&bp=wooden-dark&cm=o&c=_CIRCLES_&a=_ARROWS_&filename=chessboard.png";
	private static final String URL_JINCHESS_FEN2IMAGE_ROTATED = "http://www.jinchess.com/chessboard/?p=_FEN_&tm=_SIDE_&s=xl&bp=wooden-dark&c=_CIRCLES_&a=_ARROWS_&filename=chessboard-rotated.png";

	public static String getJinchessRotatedMarkdown(String fen) {
		return "![CHESS BOARD ROTATED](" + getFen2ImageRotatedUrl(fen) + ")";
	}

	public static String getJinchessRotatedHtml(String fen, String sideToMove) {
		return "<img alt='CHESS BOARD ROTATED' src='" + getJinchessRotatedUrl(fen, sideToMove, "", "") + "'/>";
	}

	public static String getJinchessHtml(String fen, String sideToMove) {
		return "<img alt='CHESS BOARD' src='" + getJinchessUrl(fen, sideToMove, "", "") + "'/>";
	}
	
	public static String getJinchessHtml(String fen, String sideToMove, String circles, String arrows) {
		return "<img alt='CHESS BOARD' src='" + getJinchessUrl(fen, sideToMove, circles==null?"":circles, arrows==null?"":arrows) + "'/>";
	}

	public static String getJinchessUrl(String fen, String sideToMove, String circles, String arrows) {
		sideToMove = fixupSideToMove(sideToMove);
		fen = StringUtils.substringBefore(fen, " ");
		String tmp = URL_JINCHESS_FEN2IMAGE.replace("_FEN_", fen);
		tmp = tmp.replace("_CIRCLES_", circles);
		tmp = tmp.replace("_ARROWS_", arrows);
		return tmp.replace("_SIDE_", sideToMove);
	}

	public static String getJinchessRotatedUrl(String fen, String sideToMove, String circles, String arrows) {
		sideToMove = fixupSideToMove(sideToMove);
		switch (sideToMove) {
		case "b":
			sideToMove = "w";
			break;
		case "w":
			sideToMove = "b";
			break;
		default:
		}
		fen = StringUtils.substringBefore(fen, " ");
		String tmp = URL_JINCHESS_FEN2IMAGE_ROTATED.replace("_FEN_", StringUtils.reverse(fen));
		tmp = tmp.replace("_CIRCLES_", circles);
		tmp = tmp.replace("_ARROWS_", arrows);
		return tmp.replace("_SIDE_", sideToMove);
	}

	private static String fixupSideToMove(String sideToMove) {
		if (sideToMove == null || sideToMove.length() < 1) {
			return "";
		}
		sideToMove = sideToMove.substring(0, 1).toLowerCase();
		if (!sideToMove.equals("w") && !sideToMove.equals("b")) {
			return "";
		}
		return sideToMove;
	}
	
	private static final BigDecimal _MIN_RCS_TO_RUN = new BigDecimal("18873834001");
	private static BigDecimal minRcsToRun(AccountName botAccount) {
		RcAccounts rcs;
		try {
			rcs = SteemRcApi.getRc(botAccount);
		} catch (IOException e) {
			return _MIN_RCS_TO_RUN;
		}
		for (RcAccount rc: rcs.getRcAccounts()) {
			if (rc.getAccount().equals(botAccount.getName())) {
				return rc.getMaxRc().divide(new BigDecimal("2")).setScale(0, RoundingMode.UP);
			}
		}
		return _MIN_RCS_TO_RUN;
	}
	public static boolean doRcAbortCheck(AccountName botAccount) {
		RcAccounts rcs;
		try {
			rcs = SteemRcApi.getRc(botAccount);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return true;
		}
		ArrayList<RcAccount> rcAccounts = rcs.getRcAccounts();
		if (rcAccounts.isEmpty()) {
			return true;
		}
		for (RcAccount rc: rcAccounts) {
			BigDecimal minRcsToRun = minRcsToRun(botAccount);
			if (rc.getEstimatedMana().compareTo(minRcsToRun)>0) {
				return false;
			}
			System.out.println("--- Available RCs "+NumberFormat.getInstance().format(rc.getEstimatedMana())+" < "+NumberFormat.getInstance().format(minRcsToRun));
		}
		return true;
	}
	
	/**
	 * Get Estimated voting power as a percentage x 100.
	 * @param lastVoteTimeSecs
	 * @param lastVotingPower
	 * @return
	 */
	public static BigDecimal getEstimateVote(ExtendedAccount account) {
		return getEstimateVote(account.getLastVoteTime().getDateTimeAsInt(), account.getVotingPower());
	}
	/**
	 * Get Estimated voting power as a percentage x 100.
	 * @param lastVoteTimeSecs
	 * @param lastVotingPower
	 * @return
	 */
	public static BigDecimal getEstimateVote(int lastVoteTimeSecs, int lastVotingPower) {
		BigDecimal maxVotingPower = BigDecimal.valueOf(10000);
		BigDecimal now = new BigDecimal(System.currentTimeMillis()/1000l);
		BigDecimal lastVoteTime = new BigDecimal(lastVoteTimeSecs);
		BigDecimal elapsedTime = now.subtract(lastVoteTime);
		BigDecimal percent = elapsedTime.divide(FIVE_DAYS_SECONDS, 3, RoundingMode.DOWN);
		BigDecimal votingPowerGained = maxVotingPower.multiply(percent).setScale(0, RoundingMode.DOWN);
		BigDecimal estimatedVotingPower = new BigDecimal(lastVotingPower).add(votingPowerGained);
		return estimatedVotingPower.min(maxVotingPower).movePointLeft(2);
	}
	public static final BigDecimal FIVE_DAYS_SECONDS = new BigDecimal((long) 5 * 24 * 60 * 60);
}
