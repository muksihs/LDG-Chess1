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

	private static final String URL_FEN2IMAGE = "http://www.fen-to-image.com/image/128/double/coords/_FEN_";
	private static final String URL_FEN2IMAGE_ROTATED = "http://www.fen-to-image.com/image/128/double/_FEN_";

	public static String getChessboardRotatedImageMarkdown(String fen) {
		return "![CHESS BOARD ROTATED]("+getChessboardUrlRotated(fen)+")";
	}
	
	public static String getChessboardRotatedImageHtml(String fen) {
		return "<img alt='CHESS BOARD ROTATED' src='"+getChessboardUrlRotated(fen)+"'/>";
	}
	
	public static String getChessboardImageHtml(String fen) {
		return "<img alt='CHESS BOARD' src='"+getChessboardUrl(fen)+"'/>";
	}
	
	public static String getChessboardUrl(String fen) {
		fen = StringUtils.substringBefore(fen, " ");
		return URL_FEN2IMAGE.replace("_FEN_", fen);
	}

	public static String getChessboardUrlRotated(String fen) {
		fen = StringUtils.substringBefore(fen, " ");
		return URL_FEN2IMAGE_ROTATED.replace("_FEN_", StringUtils.reverse(fen));
	}
}
