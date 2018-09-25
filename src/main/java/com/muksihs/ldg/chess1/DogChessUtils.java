package com.muksihs.ldg.chess1;

import org.apache.commons.lang3.StringUtils;

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

	public static String getChessboardRotatedImageMarkdown(String fen) {
		return "![CHESS BOARD ROTATED](" + getChessboardUrlRotated(fen) + ")";
	}

	public static String getChessboardRotatedImageHtml(String fen) {
		return "<img alt='CHESS BOARD ROTATED' src='" + getChessboardUrlRotated(fen) + "'/>";
	}

	public static String getChessboardImageHtml(String fen) {
		return "<img alt='CHESS BOARD' src='" + getChessboardUrl(fen) + "'/>";
	}

	public static String getChessboardUrl(String fen) {
		fen = StringUtils.substringBefore(fen, " ");
		return URL_FEN2IMAGE.replace("_FEN_", fen);
	}

	public static String getChessboardUrlRotated(String fen) {
		fen = StringUtils.substringBefore(fen, " ");
		return URL_FEN2IMAGE_ROTATED.replace("_FEN_", StringUtils.reverse(fen));
	}

	private static final String URL_JINCHESS_FEN2IMAGE = "http://www.jinchess.com/chessboard/?p=_FEN_&tm=_SIDE_&s=xl&bp=wooden-dark&cm=o&filename=chessboard.png";
	private static final String URL_JINCHESS_FEN2IMAGE_ROTATED = "http://www.jinchess.com/chessboard/?p=_FEN_&tm=_SIDE_&s=xl&bp=wooden-dark&filename=chessboard-rotated.png";

	public static String getJinchessChessboardRotatedImageMarkdown(String fen) {
		return "![CHESS BOARD ROTATED](" + getChessboardUrlRotated(fen) + ")";
	}

	public static String getJinchessChessboardRotatedImageHtml(String fen, String sideToMove) {
		return "<img alt='CHESS BOARD ROTATED' src='" + getJinchessChessboardUrlRotated(fen, sideToMove) + "'/>";
	}

	public static String getJinchessChessboardImageHtml(String fen, String sideToMove) {
		return "<img alt='CHESS BOARD' src='" + getJinchessChessboardUrl(fen, sideToMove) + "'/>";
	}

	public static String getJinchessChessboardUrl(String fen, String sideToMove) {
		sideToMove = fixupSideToMove(sideToMove);
		fen = StringUtils.substringBefore(fen, " ");
		String tmp = URL_JINCHESS_FEN2IMAGE.replace("_FEN_", fen);
		return tmp.replace("_SIDE_", sideToMove);
	}

	public static String getJinchessChessboardUrlRotated(String fen, String sideToMove) {
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
}
