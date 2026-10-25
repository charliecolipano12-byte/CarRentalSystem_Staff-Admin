package com.mycompany.carrentalforms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Priority queue for pending bookings, written by hand (no java.util.PriorityQueue).
 *
 * Design: TWO linked lists, one for VIP customers and one for regular customers.
 * Each list is kept sorted by arrival time (oldest first), so its head is always the
 * booking that has waited longest in that class. The next booking to handle is
 * therefore: the head of the VIP list, or, when no VIP is waiting, the head of the
 * regular list.
 *
 * Cost:  peek / dequeue / size / isEmpty = O(1)
 *        enqueue = O(1) when bookings arrive in time order (the normal case),
 *                  O(n) worst case when one has to be slotted in the middle
 *        rebuild / toSortedList = O(n)
 *
 * A booking goes into the VIP or regular list at the moment it is enqueued. If a
 * customer becomes a VIP while their booking is already waiting (a rental was just
 * returned), the booking must be moved, and that is exactly what rebuild() does:
 * it empties both lists and sorts every pending booking from the database again.
 */
public class BookingQueue {

    /** Singly linked list of bookings, oldest arrival first. */
    private static final class TimeOrderedList {
        private static final class Node {
            final Booking booking;
            Node next;
            Node(Booking booking) { this.booking = booking; }
        }

        private Node head, tail;
        private int size;

        /** Inserts by arrival time; equal times keep the order they were added in. */
        void insert(Booking b) {
            Node n = new Node(b);
            if (head == null) {                                   // empty list
                head = tail = n;
            } else if (!b.getTimestamp().isBefore(tail.booking.getTimestamp())) {
                tail.next = n;                                    // newest so far: O(1)
                tail = n;
            } else if (b.getTimestamp().isBefore(head.booking.getTimestamp())) {
                n.next = head;                                    // older than everything
                head = n;
            } else {                                              // somewhere in the middle
                Node cur = head;
                while (!b.getTimestamp().isBefore(cur.next.booking.getTimestamp())) {
                    cur = cur.next;
                }
                n.next = cur.next;
                cur.next = n;
            }
            size++;
        }

        Booking peek() { return head == null ? null : head.booking; }

        Booking removeFirst() {
            if (head == null) return null;
            Booking b = head.booking;
            head = head.next;
            if (head == null) tail = null;
            size--;
            return b;
        }

        void clear()          { head = tail = null; size = 0; }
        int size()            { return size; }
        boolean isEmpty()     { return size == 0; }

        void addTo(List<Booking> out) {
            for (Node n = head; n != null; n = n.next) out.add(n.booking);
        }
    }

    private final TimeOrderedList vip     = new TimeOrderedList();
    private final TimeOrderedList regular = new TimeOrderedList();

    public void enqueue(Booking b) {
        (b.getCustomer().isVip() ? vip : regular).insert(b);
    }

    /** Removes and returns the next booking to handle, or null if the queue is empty. */
    public Booking dequeue() {
        return vip.isEmpty() ? regular.removeFirst() : vip.removeFirst();
    }

    /** Looks at the next booking without removing it, or null if the queue is empty. */
    public Booking peek() {
        return vip.isEmpty() ? regular.peek() : vip.peek();
    }

    public int size()          { return vip.size() + regular.size(); }
    public boolean isEmpty()   { return vip.isEmpty() && regular.isEmpty(); }
    public int vipCount()      { return vip.size(); }
    public int regularCount()  { return regular.size(); }

    // Reload everything (needed because VIP status can change while waiting)
    public void rebuild(Collection<Booking> pending) {
        vip.clear();
        regular.clear();
        for (Booking b : pending) enqueue(b);
    }

    // For showing in the table: VIPs first, then regulars, each oldest first
    public List<Booking> toSortedList() {
        List<Booking> list = new ArrayList<>(size());
        vip.addTo(list);
        regular.addTo(list);
        return list;
    }
}
