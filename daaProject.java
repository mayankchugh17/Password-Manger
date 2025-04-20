import java.util.*;

public class daaProject {

    // ================= Trie =================
    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEnd = false;
    }

    static class Trie {
        TrieNode root = new TrieNode();

        void insert(String word) {
            TrieNode node = root;
            for (char ch : word.toLowerCase().toCharArray()) {
                node.children.putIfAbsent(ch, new TrieNode());
                node = node.children.get(ch);
            }
            node.isEnd = true;
        }

        List<String> search(String prefix) {
            List<String> result = new ArrayList<>();
            TrieNode node = root;
            for (char ch : prefix.toLowerCase().toCharArray()) {
                if (!node.children.containsKey(ch)) return result;
                node = node.children.get(ch);
            }
            collectAllWords(prefix, node, result);
            return result;
        }

        void collectAllWords(String prefix, TrieNode node, List<String> result) {
            if (node.isEnd) result.add(prefix);
            for (char ch : node.children.keySet()) {
                collectAllWords(prefix + ch, node.children.get(ch), result);
            }
        }
    }

    // ================= Encryption =================
    static String encrypt(String text, int shift) {
        StringBuilder sb = new StringBuilder();
        for (char ch : text.toCharArray()) {
            sb.append((char) ((ch + shift) % 256));
        }
        return sb.toString();
    }

    static String decrypt(String text, int shift) {
        StringBuilder sb = new StringBuilder();
        for (char ch : text.toCharArray()) {
            sb.append((char) ((ch - shift + 256) % 256));
        }
        return sb.toString();
    }

    // ================= Main =================
    private static final Map<String, String[]> data = new HashMap<>();
    private static final Trie trie = new Trie();
    private static final Stack<Map.Entry<String, String[]>> undoStack = new Stack<>();
    private static final Queue<Map.Entry<String, String[]>> backupQueue = new LinkedList<>();
    private static final int SHIFT = 3;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== PASSWORD MANAGER MENU =====");
            System.out.println("1. Add Entry");
            System.out.println("2. Delete Entry");
            System.out.println("3. Undo Delete");
            System.out.println("4. Search Site");
            System.out.println("5. Show All Entries");
            System.out.println("6. Show Backup");
            System.out.println("7. Exit");
            System.out.print("Choose: ");
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Site: ");
                    String site = sc.nextLine();
                    System.out.print("Username: ");
                    String user = sc.nextLine();
                    System.out.print("Password: ");
                    String pwd = sc.nextLine();
                    addEntry(site, user, pwd);
                    break;
                case "2":
                    System.out.print("Site to delete: ");
                    deleteEntry(sc.nextLine());
                    break;
                case "3":
                    undoDelete();
                    break;
                case "4":
                    System.out.print("Search prefix: ");
                    searchSites(sc.nextLine());
                    break;
                case "5":
                    showAllEntries();
                    break;
                case "6":
                    showBackup();
                    break;
                case "7":
                    System.out.println("Exiting... 🔒");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    static void addEntry(String site, String username, String password) {
        String encPwd = encrypt(password, SHIFT);
        data.put(site, new String[]{username, encPwd});
        trie.insert(site);
        backupQueue.add(Map.entry(site, new String[]{username, encPwd}));
        if (backupQueue.size() > 5) backupQueue.poll();
        System.out.println("[+] Entry added.");
    }

    static void deleteEntry(String site) {
        if (data.containsKey(site)) {
            undoStack.push(Map.entry(site, data.get(site)));
            data.remove(site);
            System.out.println("[-] Entry deleted. You can undo this.");
        } else {
            System.out.println("[!] Site not found.");
        }
    }

    static void undoDelete() {
        if (!undoStack.isEmpty()) {
            Map.Entry<String, String[]> last = undoStack.pop();
            data.put(last.getKey(), last.getValue());
            System.out.println("[↩] Undo successful. Restored: " + last.getKey());
        } else {
            System.out.println("[!] Nothing to undo.");
        }
    }

    static void searchSites(String prefix) {
        List<String> matches = trie.search(prefix);
        if (matches.isEmpty()) {
            System.out.println("[!] No sites found.");
            return;
        }
        System.out.println("[🔍] Matches:");
        for (String site : matches) {
            String[] info = data.get(site);
            if (info != null) {
                System.out.println(site + " → Username: " + info[0] + ", Password: " + decrypt(info[1], SHIFT));
            }
        }
    }

    static void showAllEntries() {
        List<String> keys = new ArrayList<>(data.keySet());
        Collections.sort(keys);
        System.out.println("[📋] All Entries:");
        for (String site : keys) {
            String[] info = data.get(site);
            System.out.println(site + " → Username: " + info[0] + ", Password: " + decrypt(info[1], SHIFT));
        }
    }

    static void showBackup() {
        System.out.println("[💾] Backup (last 5):");
        for (Map.Entry<String, String[]> entry : backupQueue) {
            System.out.println(entry.getKey() + " → Username: " + entry.getValue()[0] + ", Password: " + decrypt(entry.getValue()[1], SHIFT));
        }
    }
}
