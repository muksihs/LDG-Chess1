package steem.models;

import java.util.List;

public class ChessGameData {
	private String startDate;
	private boolean resigned;
	private String resignedBy;
	private boolean draw;
	private String fen;
	private String gameId;
	private boolean mated;
	private List<String> moveList;
	private String san;
	private String fan;
	public String getSan() {
		return san;
	}
	public void setSan(String san) {
		this.san = san;
	}
	public String getFan() {
		return fan;
	}
	public void setFan(String fan) {
		this.fan = fan;
	}
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
	public boolean isResigned() {
		return resigned;
	}
	public void setResigned(boolean resigned) {
		this.resigned = resigned;
	}
	public String getResignedBy() {
		return resignedBy;
	}
	public void setResignedBy(String resignedBy) {
		this.resignedBy = resignedBy;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
}
