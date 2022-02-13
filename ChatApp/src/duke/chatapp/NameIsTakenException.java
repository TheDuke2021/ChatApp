package duke.chatapp;

class NameIsTakenException extends Exception {

	private static final long serialVersionUID = 1L;
	
	@Override
	public String toString() {
		return "This name is already taken on the server";
	}

}
