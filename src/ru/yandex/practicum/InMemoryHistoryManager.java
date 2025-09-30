package ru.yandex.practicum;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    private final Node head;
    private final Node tail;
    private final Map<Integer, Node> historyMap;

    public InMemoryHistoryManager() {
        this.historyMap = new HashMap<>();
        this.head = new Node(null);
        this.tail = new Node(null);
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public void add(Task task) {
        Node node = historyMap.get(task.getId());
        if (node == null) {
            put(task);
        } else {
            remove(task.getId());
            put(task);
        }
    }

    @Override
    public void remove(int id) {
        Node removeNode = historyMap.get(id);
        if (removeNode != null) {
            removeNode(removeNode);
            historyMap.remove(id);
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> history = new ArrayList<>();
        Node current = head.next;
        while (current != tail) {
            history.add(current.data);
            current = current.next;
        }
        return history;
    }

    private void linkLast(Node node) {
        node.prev = tail.prev;
        node.next = tail;
        tail.prev.next = node;
        tail.prev = node;
    }

    private void removeNode(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void put(Task task) {
        Node newNode = new Node(task);
        linkLast(newNode);
        historyMap.put(task.getId(), newNode);
    }

    private static class Node {
        Task data;
        Node prev;
        Node next;

        Node(Task data) {
            this.data = data;
        }
    }
}
