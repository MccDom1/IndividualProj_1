/* Player class will represent player state.
* So tracking room the player is in and room movement input
*
* It wil not Load files Print UI or hard code room rules. Meaning Know how rooms are stored.
*/

import java.util.Map;

public class Player {

    // Store current room id i.e room player is in!
    private int currentRoomId;

    // Creating player in starting room
    public Player(int startRoomId) {
        if (startRoomId <= 0) throw new IllegalArgumentException("Start room must be positive.");
        this.currentRoomId = startRoomId;
    }
// Returns Player current room id
    public int getCurrentRoomId() {
        return currentRoomId;
    }

    // Player only manages player state and movement request.
    // Player  does not load files, print UI, or hard code room rules.
    // Attempts to move in given direction should be possible. 
    // Returns true if move successful, false otherwise or blocked
    public boolean move(Room.Direction dir, Map<Integer, Room> rooms) {
        Room current = rooms.get(currentRoomId);
        if (current == null) return false;

        Integer nextId = current.getExit(dir);
        if (nextId == null) return false;

        if (!rooms.containsKey(nextId)) return false;
// Move is valid or successful!!!
        currentRoomId = nextId;
        return true;
    }
}
