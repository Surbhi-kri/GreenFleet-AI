package org.example.cache;

import org.example.model.RouteResult;
import java.util.HashMap;

public class LRUCache {

    // ── Inner Node class ──────────────────────────────────────────────────
    private static class Node {
        String      key;
        RouteResult value;
        Node        prev;
        Node        next;

        Node(String key, RouteResult value) {
            this.key   = key;
            this.value = value;
        }
    }

    // ── Fields ────────────────────────────────────────────────────────────
    private final int                   capacity;
    private final HashMap<String, Node> map;
    private final Node                  head;
    private final Node                  tail;
    private       int                   hits;
    private       int                   misses;

    // ── Constructor ───────────────────────────────────────────────────────
    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.map      = new HashMap<>();
        this.hits     = 0;
        this.misses   = 0;

        // dummy head and tail — never hold real data
        head      = new Node("HEAD", null);
        tail      = new Node("TAIL", null);
        head.next = tail;
        tail.prev = head;
    }

    // ── get ───────────────────────────────────────────────────────────────
    /**
     * Returns cached RouteResult if found, null if not.
     * On HIT  → moves node to HEAD (marks as recently used)
     * On MISS → returns null (caller must compute and call put)
     */
    public RouteResult get(String key) {
        Node node = map.get(key);

        if (node == null) {
            misses++;
            return null;
        }

        hits++;
        moveToHead(node);
        return node.value;
    }

    // ── put ───────────────────────────────────────────────────────────────
    /**
     * Stores a RouteResult in the cache.
     * If key exists → update value and move to HEAD.
     * If new key    → add at HEAD, evict TAIL if over capacity.
     */
    public void put(String key, RouteResult result) {
        Node existing = map.get(key);

        if (existing != null) {
            existing.value = result;
            moveToHead(existing);
            return;
        }

        Node newNode = new Node(key, result);
        map.put(key, newNode);
        addToHead(newNode);

        if (map.size() > capacity) {
            Node evicted = removeTail();
            map.remove(evicted.key);
            System.out.println("[Cache] Evicted: " + evicted.key);
        }
    }

    // ── private helpers ───────────────────────────────────────────────────

    private void removeNode(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void addToHead(Node node) {
        node.prev      = head;
        node.next      = head.next;
        head.next.prev = node;
        head.next      = node;
    }

    private void moveToHead(Node node) {
        removeNode(node);
        addToHead(node);
    }

    private Node removeTail() {
        Node lru = tail.prev;
        removeNode(lru);
        return lru;
    }

    // ── stats ─────────────────────────────────────────────────────────────
    public void printStats() {
        int    total = hits + misses;
        double ratio = total > 0 ? (hits * 100.0 / total) : 0.0;
        System.out.println("\n  ── Cache Statistics ──");
        System.out.printf("  Size      : %d / %d%n",  map.size(), capacity);
        System.out.printf("  Hits      : %d%n",        hits);
        System.out.printf("  Misses    : %d%n",        misses);
        System.out.printf("  Hit ratio : %.1f%%%n",    ratio);
        if (ratio >= 70) {
            System.out.println("  Status    : GOOD — cache is working!");
        } else if (total == 0) {
            System.out.println("  Status    : No requests yet.");
        } else {
            System.out.println("  Status    : Low — try same route again.");
        }
    }

    public int size()      { return map.size(); }
    public int getHits()   { return hits; }
    public int getMisses() { return misses; }
}
/*``

        ---

        ## How LRU Cache works visually
```
capacity = 3

Step 1: put("Mumbai|Bangalore|FASTEST|1", result1)
HEAD ←→ [Mumbai|Bang] ←→ TAIL

Step 2: put("Pune|Delhi|FASTEST|1", result2)
HEAD ←→ [Pune|Delhi] ←→ [Mumbai|Bang] ←→ TAIL

Step 3: put("Delhi|Hyd|FASTEST|1", result3)
HEAD ←→ [Delhi|Hyd] ←→ [Pune|Delhi] ←→ [Mumbai|Bang] ←→ TAIL

Step 4: get("Pune|Delhi|FASTEST|1")  ← HIT
Move to HEAD
HEAD ←→ [Pune|Delhi] ←→ [Delhi|Hyd] ←→ [Mumbai|Bang] ←→ TAIL

Step 5: put("Nagpur|Pune|FASTEST|1", result4)  ← FULL!
Evict TAIL = Mumbai|Bangalore (least recently used)
HEAD ←→ [Nagpur|Pune] ←→ [Pune|Delhi] ←→ [Delhi|Hyd] ←→ TAIL

 */