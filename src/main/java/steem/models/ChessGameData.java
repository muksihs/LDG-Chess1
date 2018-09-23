package steem.models;

import java.util.List;

public class ChessGameData {
	private boolean draw;
	private String fen;
	private String gameId;
	private boolean mated;
	private List<String> moveList;
	private String pgn;
	private String playerBlack;
	private String playerToMove;
	private String playerWhite;
	private String sideToMove;
	private boolean stalemate;
	private String variationType;
	public String getFen() {
		return fen;
	}
	public String getGameId() {
		return gameId;
	}
	public List<String> getMoveList() {
		return moveList;
	}
	public String getPgn() {
		return pgn;
	}
	public String getPlayerBlack() {
		return playerBlack;
	}
	public String getPlayerWhite() {
		return playerWhite;
	}
	public String getSideToMove() {
		return sideToMove;
	}
	public String getVariationType() {
		return variationType;
	}
	public boolean isDraw() {
		return draw;
	}
	public boolean isMated() {
		return mated;
	}
	public boolean isStalemate() {
		return stalemate;
	}
	public void setDraw(boolean draw) {
		this.draw = draw;
	}
	public void setFen(String fen) {
		this.fen = fen;
	}
	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
	public void setMated(boolean mated) {
		this.mated = mated;
	}
	public void setMoveList(List<String> moveList) {
		this.moveList = moveList;
	}
	public void setPgn(String pgn) {
		this.pgn = pgn;
	}
	public void setPlayerBlack(String playerBlack) {
		this.playerBlack = playerBlack;
	}
	public void setPlayerWhite(String playerWhite) {
		this.playerWhite = playerWhite;
	}
	public void setSideToMove(String sideToMove) {
		this.sideToMove = sideToMove;
	}
	public void setStalemate(boolean stalemate) {
		this.stalemate = stalemate;
	}
	public void setVariationType(String variationType) {
		this.variationType = variationType;
	}
	public String getPlayerToMove() {
		return playerToMove;
	}
	public void setPlayerToMove(String playerToMove) {
		this.playerToMove = playerToMove;
	}
}
