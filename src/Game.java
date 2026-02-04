/* Game class is pretty Much Mission COntrol for the text adventure.
 * It loads room data, manages the main game loop, processes user input,
 * and displays room information.
* It does not manage player state (that's Player class)
 * or room details (that's Room class).
 * 
 * It is data driven: room connections are loaded from a text file (Rooms.txt).
 * No hard coded room rules, file loading, or UI logic in Room or Player classes.
*/


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Game {

    // Stores all loaded rooms by id and or entire Game state
    private final Map<Integer, Room> rooms = new HashMap<>();
    private Player player;

    public static void main(String[] args) {
        // Default rooms file name 
        
        String roomsFile = "Rooms.txt";
        // Allow custom file override via command line arg
        if (args.length > 0 && args[0] != null && !args[0].trim().isEmpty()) {
            roomsFile = args[0].trim();
        }

        Game game = new Game();

        try {
            game.loadRooms(roomsFile);
        } catch (Exception e) {
            System.out.println("ERROR loading room data: " + e.getMessage());
            return;
        }

        game.start();
    }

    // Main game loop

    private void start() {
        player = new Player(getStartRoomId());

        System.out.println("=== TEXT ADVENTURE ===");
        System.out.println("Commands: N E S W | LOOK | MAP | VMAP | HELP | EXIT");

        Scanner sc = new Scanner(System.in);

        while (true) {
            Room current = rooms.get(player.getCurrentRoomId());
            if (current == null) {
                System.out.println("ERROR: Current room missing from loaded data.");
                break;
            }

            displayRoom(current);

            System.out.print("> ");
            String input = sc.nextLine();
            if (input == null) input = "";
            String cmd = input.trim();
// Exit conditions
            if (cmd.equalsIgnoreCase("EXIT") || cmd.equalsIgnoreCase("Q") || cmd.equalsIgnoreCase("QUIT")) {
                System.out.println("Goodbye.");
                break;
            }
// Help command Menu
            if (cmd.equalsIgnoreCase("HELP")) {
                printHelp();
                continue;
            }
// Look command to redisplay current room details
            if (cmd.equalsIgnoreCase("LOOK")) {
                System.out.println(current.getName());
                System.out.println(current.getDescription());
                System.out.println(current.exitsString());
                continue;
            }
// Map command to print room connections
            if (cmd.equalsIgnoreCase("MAP")) {
                printMap();
                continue;
            }
// Visual Map command to print ASCII diagram
            if (cmd.equalsIgnoreCase("VMAP")) {
                printVisualMap();
                continue;
            }
// Attempt to parse movement direction
            Room.Direction dir = Room.parseDirection(cmd);
            if (dir == null) {
                System.out.println("Invalid command. Use N/E/S/W, LOOK, MAP, VMAP, HELP, or EXIT.");
                continue;
            }
// Attempt to move player in prompted direction
            if (!player.move(dir, rooms)) {
                System.out.println("You cannot go this way.");
            }
        }

        sc.close();
    }
// Display current room details to player
    private void displayRoom(Room room) {
        System.out.println();
        System.out.println(room.getName());
        System.out.println(room.getDescription());

        if (room.isVisited()) {
            System.out.println("You have visited this room.");
        } else {
            room.markVisited();
        }

        System.out.println(room.exitsString());
    }

    private void printHelp() {
        System.out.println("Commands:");
        System.out.println("  N E S W   - Move (or NORTH/EAST/SOUTH/WEST)");
        System.out.println("  LOOK      - Reprint current room details");
        System.out.println("  MAP       - Print room connections (logical map)");
        System.out.println("  VMAP      - Print a simple ASCII diagram (visual map)");
        System.out.println("  HELP      - Show commands");
        System.out.println("  EXIT / Q  - Quit");
    }

    // Logical map shows all rooms and their connections.
    private void printMap() {
        System.out.println();
        System.out.println("=== GAME MAP (Connections) ===");

        for (Room room : rooms.values()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Room ").append(room.getId())
              .append(" (").append(room.getName()).append("): ");

            boolean any = false;
            for (Room.Direction d : Room.Direction.values()) {
                Integer destId = room.getExit(d);
                if (destId != null) {
                    if (any) sb.append(", ");
                    Room dest = rooms.get(destId);
                    String destName = (dest == null) ? "MISSING" : dest.getName();
                    sb.append(d.name()).append("->").append(destId).append("[").append(destName).append("]");
                    any = true;
                }
            }

            if (!any) sb.append("(no exits)");
            System.out.println(sb.toString());
        }

        System.out.println("==============================");
    }

    /*
     * Visual map: derives a simple coordinate layout from exits.
     * This stays data driven.. it uses your Rooms.txt connections.
     * It does not hard-code any room positions.
     */
    private void printVisualMap() {
        if (rooms.isEmpty()) {
            System.out.println("No rooms loaded.");
            return;
        }

        // roomId -> {x,y}
        Map<Integer, int[]> coords = new HashMap<>();
        // "x,y" -> roomId (prevents overlaps)
        Map<String, Integer> taken = new HashMap<>();
// Start placing rooms from starting room at (0,0)
        int start = getStartRoomId();
        coords.put(start, new int[]{0, 0});
        taken.put("0,0", start);
// BFS to place connected rooms

        ArrayDeque<Integer> q = new ArrayDeque<>();
        q.add(start);
// While queue not empty
// process each room and attempt to place its neighbors
        while (!q.isEmpty()) {
            int id = q.removeFirst();
            Room r = rooms.get(id);
            if (r == null) continue;
// Get current room coords

            int[] xy = coords.get(id);
            int x = xy[0], y = xy[1];
// Check each possible exit direction
            for (Room.Direction d : Room.Direction.values()) {
                Integer nid = r.getExit(d);
                if (nid == null) continue;
                if (!rooms.containsKey(nid)) continue;
// Calculate neighbor coords based on direction
                int nx = x, ny = y;
                if (d == Room.Direction.N) ny -= 1;
                if (d == Room.Direction.S) ny += 1;
                if (d == Room.Direction.E) nx += 1;
                if (d == Room.Direction.W) nx -= 1;
// If neighbor not yet placed, and target coords free, place it
                if (!coords.containsKey(nid)) {
                    String key = nx + "," + ny;
                    if (!taken.containsKey(key)) {
                        coords.put(nid, new int[]{nx, ny});
                        taken.put(key, nid);
                        q.add(nid);
                    }
                }
            }
        }

        // Determine bounds of placed rooms
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
// Find min and max X/Y from placed coordinates
        for (int[] xy : coords.values()) {
            minX = Math.min(minX, xy[0]);
            maxX = Math.max(maxX, xy[0]);
            minY = Math.min(minY, xy[1]);
            maxY = Math.max(maxY, xy[1]);
        }
// Calculate grid size
        int cols = (maxX - minX + 1);
        int rows = (maxY - minY + 1);

        
        // We'll render [id] and print legend underneath.
        int cellW = 7;  // width for room label region
        int gapW  = 3;  // horizontal connector space
        int cellH = 3;  // height for room label region
        int gapH  = 1;  // vertical connector space

        int canvasW = cols * cellW + (cols - 1) * gapW;
        int canvasH = rows * cellH + (rows - 1) * gapH;

        char[][] canvas = new char[canvasH][canvasW];
        for (int r = 0; r < canvasH; r++) {
            for (int c = 0; c < canvasW; c++) canvas[r][c] = ' ';
        }

        // Convert room coordinate to canvas center
        // (0,0) ends up wherever min bounds shift it.
        for (Map.Entry<Integer, int[]> e : coords.entrySet()) {
            int id = e.getKey();
            int x = e.getValue()[0] - minX;
            int y = e.getValue()[1] - minY;

            int baseX = x * (cellW + gapW);
            int baseY = y * (cellH + gapH);

            int cx = baseX + cellW / 2;
            int cy = baseY + cellH / 2;

            String label = "[" + id + "]";
            int startX = cx - (label.length() / 2);

            for (int i = 0; i < label.length(); i++) {
                int px = startX + i;
                if (px >= 0 && px < canvasW) canvas[cy][px] = label.charAt(i);
            }
        }

        // Draw connectors between placed rooms (only straight N/E/S/W)
        for (Map.Entry<Integer, int[]> e : coords.entrySet()) {
            int id = e.getKey();
            Room r = rooms.get(id);
            if (r == null) continue;

            int x1 = e.getValue()[0] - minX;
            int y1 = e.getValue()[1] - minY;

            int baseX1 = x1 * (cellW + gapW);
            int baseY1 = y1 * (cellH + gapH);
            int cx1 = baseX1 + cellW / 2;
            int cy1 = baseY1 + cellH / 2;

            for (Room.Direction d : Room.Direction.values()) {
                Integer nid = r.getExit(d);
                if (nid == null) continue;

                int[] nxy = coords.get(nid);
                if (nxy == null) continue; // only if neighbor was placed

                int x2 = nxy[0] - minX;
                int y2 = nxy[1] - minY;

                int baseX2 = x2 * (cellW + gapW);
                int baseY2 = y2 * (cellH + gapH);
                int cx2 = baseX2 + cellW / 2;
                int cy2 = baseY2 + cellH / 2;

                if (cy1 == cy2) {
                    int from = Math.min(cx1, cx2);
                    int to   = Math.max(cx1, cx2);
                    for (int x = from + 1; x < to; x++) {
                        if (canvas[cy1][x] == ' ') canvas[cy1][x] = '-';
                    }
                } else if (cx1 == cx2) {
                    int from = Math.min(cy1, cy2);
                    int to   = Math.max(cy1, cy2);
                    for (int y = from + 1; y < to; y++) {
                        if (canvas[y][cx1] == ' ') canvas[y][cx1] = '|';
                    }
                }
            }
        }
// Print the visual map
        System.out.println();
        System.out.println("=== VISUAL MAP (ASCII) ===");
        System.out.println("Room labels show [id]. Type MAP to see names and full connections.");
        System.out.println();
// Output canvas row by row
        for (int r = 0; r < canvasH; r++) {
            System.out.println(new String(canvas[r]));
        }
// Print legend of room ids to names
        System.out.println();
        System.out.println("Legend:");
        for (Room room : rooms.values()) {
            System.out.println("  [" + room.getId() + "] = " + room.getName());
        }
    }

    
    // Data driven room loading
    
    private void loadRooms(String fileName) throws IOException {
        rooms.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            int lineNo = 0;

            while ((line = br.readLine()) != null) {
                lineNo++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) continue;

                // id|name|description|N,E,S,W
                String[] parts = line.split("\\|", -1);
                if (parts.length != 4) {
                    throw new IllegalArgumentException("Line " + lineNo + ": Expected id|name|description|N,E,S,W");
                }

                int id = parsePositiveInt(parts[0], "id", lineNo);
                String name = parts[1].trim();
                String desc = parts[2].trim();

                String[] exits = parts[3].split(",", -1);
                if (exits.length != 4) {
                    throw new IllegalArgumentException("Line " + lineNo + ": Exits must be 4 comma ints: N,E,S,W");
                }

                int n = parseNonNegativeInt(exits[0], "N", lineNo);
                int e = parseNonNegativeInt(exits[1], "E", lineNo);
                int s = parseNonNegativeInt(exits[2], "S", lineNo);
                int w = parseNonNegativeInt(exits[3], "W", lineNo);

                if (rooms.containsKey(id)) {
                    throw new IllegalArgumentException("Line " + lineNo + ": Duplicate room id " + id);
                }

                Room room = new Room(id, name, desc);
                room.setExit(Room.Direction.N, n);
                room.setExit(Room.Direction.E, e);
                room.setExit(Room.Direction.S, s);
                room.setExit(Room.Direction.W, w);

                rooms.put(id, room);
            }
        }

        if (rooms.isEmpty()) {
            throw new IllegalArgumentException("No rooms loaded. Check " + fileName);
        }
    }

    private int getStartRoomId() {
        if (rooms.containsKey(1)) return 1;

        int min = Integer.MAX_VALUE;
        for (int id : rooms.keySet()) {
            if (id < min) min = id;
        }
        return (min == Integer.MAX_VALUE) ? 1 : min;
    }

    private int parsePositiveInt(String s, String field, int lineNo) {
        int v = parseNonNegativeInt(s, field, lineNo);
        if (v <= 0) throw new IllegalArgumentException("Line " + lineNo + ": " + field + " must be positive.");
        return v;
    }
// Utility to parse non negative integers from strings
    private int parseNonNegativeInt(String s, String field, int lineNo) {
        try {
            int v = Integer.parseInt(s.trim());
            if (v < 0) throw new NumberFormatException();
            return v;
        } catch (Exception e) {
            throw new IllegalArgumentException("Line " + lineNo + ": " + field + " must be a non-negative integer.");
        }
    }
}
