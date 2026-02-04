/* Room class is for single location in this game world
* It holds room id, name, description, exits, and visited flag.
* Track player movement between rooms.
* Save and provide exits to other rooms
*
* Will not handle file loading, UI, or hard coded rules and or Player Movement.
*/




import java.util.EnumMap;
import java.util.Map;

public class Room {

    // Movements in the game
    public enum Direction { N, E, S, W }

    // room identity
    private final int id;
    private final String name;
    private final String description;

    // Player room Tack
    private boolean visited;
    // Maps direction to destination room id
    private final Map<Direction, Integer> exits = new EnumMap<>(Direction.class);

    // Constructor with basic room info
    // Game class allows setting exits after loading rooms.
    public Room(int id, String name, String description) {
        if (id <= 0) throw new IllegalArgumentException("Room id must be positive.");
        this.id = id;
        this.name = (name == null) ? "" : name.trim();
        this.description = (description == null) ? "" : description.trim();
        this.visited = false;
    }
// Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }

    public boolean isVisited() { return visited; }
    public void markVisited() { visited = true; }
// Exits management adding or updating an exit in a given direction
// if statement means must be 0 to be valid
    public void setExit(Direction dir, int destinationId) {
        if (dir == null) return;
        if (destinationId > 0) exits.put(dir, destinationId);
    }
/* Will return the room id in the given direction, or null if no exit there
* null means no exit in that direction
*/
    public Integer getExit(Direction dir) {
        return exits.get(dir); // null so no exit in that direction
    }
// Easy to read string of available exits
    public String exitsString() {
        StringBuilder sb = new StringBuilder("Exits: ");
        boolean any = false;

        for (Direction d : Direction.values()) {
            if (exits.containsKey(d)) {
                if (any) sb.append(", ");
                sb.append(d.name());
                any = true;
            }
        }

        if (!any) sb.append("(none)");
        return sb.toString();
    }
// Parse user input into Direction enum Uitility method
    public static Direction parseDirection(String input) {
        if (input == null) return null;

        String s = input.trim().toUpperCase();
        if (s.equals("N") || s.equals("NORTH")) return Direction.N;
        if (s.equals("E") || s.equals("EAST"))  return Direction.E;
        if (s.equals("S") || s.equals("SOUTH")) return Direction.S;
        if (s.equals("W") || s.equals("WEST"))  return Direction.W;

        return null;
    }
}
