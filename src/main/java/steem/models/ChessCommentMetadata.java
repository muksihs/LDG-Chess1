package steem.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChessCommentMetadata {
	public static class ChessData {
		private String fen;
		private List<String> moveHistory;
		public String getFen() {
			return fen;
		}
		public void setFen(String fen) {
			this.fen = fen;
		}
		public List<String> getMoveHistory() {
			return moveHistory;
		}
		public void setMoveHistory(List<String> moveHistory) {
			this.moveHistory = moveHistory;
		}
		public String getPlayerWhite() {
			return playerWhite;
		}
		public void setPlayerWhite(String playerWhite) {
			this.playerWhite = playerWhite;
		}
		public String getPlayerBlack() {
			return playerBlack;
		}
		public void setPlayerBlack(String playerBlack) {
			this.playerBlack = playerBlack;
		}
		private String playerWhite;
		private String playerBlack;
	}
	private String app;
	private String canonical;
	private String format;
	private String[] image;
	private String[] links;
	private String status;
	private String[] tags;
	private String[] users;
	private String community;
	private ChessData gameData;

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public String getCanonical() {
		return canonical;
	}

	public void setCanonical(String canonical) {
		this.canonical = canonical;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String[] getImage() {
		return image;
	}

	public void setImage(String[] image) {
		this.image = image;
	}

	public String[] getLinks() {
		return links;
	}

	public void setLinks(String[] links) {
		this.links = links;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String[] getTags() {
		return tags;
	}

	public void setTags(String[] tags) {
		this.tags = tags;
	}

	public String[] getUsers() {
		return users;
	}

	public void setUsers(String[] users) {
		this.users = users;
	}

	public String getCommunity() {
		return community;
	}

	public void setCommunity(String community) {
		this.community = community;
	}

	public ChessData getGameData() {
		return gameData;
	}

	public void setGameData(ChessData gameData) {
		this.gameData = gameData;
	}
}
