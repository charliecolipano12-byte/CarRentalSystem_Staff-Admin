package com.mycompany.carrentalforms;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Plain-Java tests for BookingQueue (no JUnit needed).
 * Put it in src/test/java/com/mycompany/carrentalforms/ and use "Run File" in NetBeans.
 * It prints PASS / FAIL per case, so the output can go straight into your test log.
 */
public class BookingQueueTest {

    private static int passed = 0, failed = 0;
    private static final LocalDateTime T0 = LocalDateTime.of(2026, 10, 1, 8, 0);

    private static Booking booking(String id, int completedRentals, int minutesAfterT0) {
        Customer c = new Customer();
        c.setFullName("Customer " + id);
        c.setCompletedRentals(completedRentals);
        return new Booking(id, c, "Car", "dates", 1000, Booking.PENDING, T0.plusMinutes(minutesAfterT0));
    }
    private static Booking vip(String id, int min)     { return booking(id, Customer.VIP_THRESHOLD, min); }
    private static Booking regular(String id, int min) { return booking(id, 0, min); }

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
        if (ok) passed++; else failed++;
    }
    private static String ids(List<Booking> l) {
        StringBuilder sb = new StringBuilder();
        for (Booking b : l) sb.append(sb.length() == 0 ? "" : ",").append(b.getId());
        return sb.toString();
    }

    public static void main(String[] args) {
        // ---- 0 bookings
        BookingQueue q = new BookingQueue();
        check("0 bookings: isEmpty, size 0", q.isEmpty() && q.size() == 0);
        check("0 bookings: peek and dequeue return null", q.peek() == null && q.dequeue() == null);
        check("0 bookings: toSortedList is empty", q.toSortedList().isEmpty());
        q.rebuild(new ArrayList<>());
        check("0 bookings: rebuild with nothing stays empty", q.isEmpty());

        // ---- 1 booking
        q.enqueue(regular("A", 0));
        check("1 booking: size 1, peek does not remove", q.size() == 1 && q.peek().getId().equals("A") && q.size() == 1);
        check("1 booking: dequeue returns it, queue empty again", q.dequeue().getId().equals("A") && q.isEmpty());
        check("1 booking: dequeue on the now-empty queue is null", q.dequeue() == null);

        // ---- many bookings: VIP first, then oldest first
        q = new BookingQueue();
        q.enqueue(regular("R1", 0));
        q.enqueue(regular("R2", 5));
        q.enqueue(vip("V1", 10));
        q.enqueue(regular("R3", 15));
        q.enqueue(vip("V2", 20));
        check("many: table order is VIPs first, each oldest first", ids(q.toSortedList()).equals("V1,V2,R1,R2,R3"));
        check("many: counts 2 VIP + 3 regular", q.vipCount() == 2 && q.regularCount() == 3 && q.size() == 5);
        StringBuilder order = new StringBuilder();
        while (!q.isEmpty()) order.append(order.length() == 0 ? "" : ",").append(q.dequeue().getId());
        check("many: dequeue order V1,V2,R1,R2,R3", order.toString().equals("V1,V2,R1,R2,R3"));

        // ---- out-of-order arrival times and equal times
        q = new BookingQueue();
        q.enqueue(regular("late", 30));
        q.enqueue(regular("early", 0));
        q.enqueue(regular("mid", 15));
        q.enqueue(regular("mid2", 15));          // same time as "mid": keeps insertion order
        check("out of order: sorted by arrival time, ties keep insertion order",
                ids(q.toSortedList()).equals("early,mid,mid2,late"));

        // ---- VIP promotion while waiting (the reason rebuild() exists)
        Customer promoted = new Customer();
        promoted.setFullName("Promoted"); promoted.setCompletedRentals(Customer.VIP_THRESHOLD - 1);
        Booking waiting = new Booking("P", promoted, "Car", "d", 1000, Booking.PENDING, T0.plusMinutes(50));
        List<Booking> pending = new ArrayList<>();
        pending.add(regular("X", 0)); pending.add(waiting); pending.add(regular("Y", 60));
        q = new BookingQueue();
        q.rebuild(pending);
        check("promotion: before, P waits behind X", ids(q.toSortedList()).equals("X,P,Y"));
        promoted.setCompletedRentals(Customer.VIP_THRESHOLD);     // a rental was just returned
        q.rebuild(pending);
        check("promotion: after rebuild, P jumps to the front", q.peek().getId().equals("P") && ids(q.toSortedList()).equals("P,X,Y"));
        q.rebuild(new ArrayList<>());
        check("rebuild replaces the old contents", q.isEmpty());

        // ---- 2000 random queues must match the old java.util.PriorityQueue ordering exactly
        Random rnd = new Random(42);
        boolean same = true;
        for (int run = 0; run < 2000 && same; run++) {
            int n = rnd.nextInt(40);
            PriorityQueue<Booking> ref = new PriorityQueue<>(
                    Comparator.comparing((Booking b) -> !b.getCustomer().isVip())
                              .thenComparing(Booking::getTimestamp));
            BookingQueue mine = new BookingQueue();
            for (int i = 0; i < n; i++) {
                // distinct minutes so the reference ordering is unambiguous
                Booking b = booking("B" + i, rnd.nextInt(6), i * 7 + rnd.nextInt(5));
                ref.add(b); mine.enqueue(b);
            }
            // shuffle arrival order: re-add in random order
            if (n > 0) {
                List<Booking> all = new ArrayList<>(ref);
                java.util.Collections.shuffle(all, rnd);
                mine.rebuild(all);
            }
            while (!ref.isEmpty()) {
                if (mine.dequeue() != ref.poll()) { same = false; break; }
            }
            if (!mine.isEmpty()) same = false;
        }
        check("2000 random queues: same order as java.util.PriorityQueue", same);

        System.out.println("\n" + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }
}
