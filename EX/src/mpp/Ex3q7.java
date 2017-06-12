package mpp;

import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.locks.ReentrantLock;

public class Ex3q7 {

    public static void main(String[] args){
        //TODO
    }

    public interface PriorityQueue<T> {
        void add(T item, int score);

        T removeMin();
    }

    static class LazyPriorityQueue<T> implements PriorityQueue<T> {

        private final LazyNode head;

        public LazyPriorityQueue() {
            head = new LazyNode(null, Integer.MIN_VALUE);
            head.next = new LazyNode(null, Integer.MAX_VALUE);
        }

        @Override
        public void add(T item, int score) {
            while (true) {
                LazyNode pred = head, curr = head.next;
                while (curr.score < score) {
                    pred = curr;
                    curr = curr.next;
                }
                pred.lock();
                try {
                    curr.lock();
                    try {
                        if (validate(pred, curr)) {
                            LazyNode node = new LazyNode(item, score);
                            node.next = curr;
                            pred.next = node;
                            return;
                        }
                    } finally {
                        curr.unlock();
                    }
                } finally {
                    pred.unlock();
                }
            }
        }

        @Override
        public T removeMin() {
            head.lock();
            try {
                LazyNode curr = head.next;
                curr.lock();
                try {
                    if (curr.next != null && validate(head, curr)) {
                        curr.marked = true;
                        head.next = curr.next;
                        return curr.item;
                    } else {
                        return null;
                    }
                } finally {
                    curr.unlock();
                }
            } finally {
                head.unlock();
            }
        }

        private boolean validate(LazyNode pred, LazyNode curr) {
            return !pred.marked && !curr.marked && pred.next == curr;
        }

        class LazyNode {
            final T item;
            final int score;
            volatile boolean marked = false;
            volatile LazyNode next = null;
            private final ReentrantLock lock = new ReentrantLock();

            public LazyNode(T item, int score) {
                this.item = item;
                this.score = score;
            }

            public void lock() {
                lock.lock();
            }

            public void unlock() {
                lock.unlock();
            }
        }
    }

    static class LockFreePriorityQueue<T> implements PriorityQueue<T> {
        private final LFNode head;

        public LockFreePriorityQueue() {
            head = new LFNode(null, Integer.MIN_VALUE);
            head.next.set(new LFNode(null, Integer.MAX_VALUE), false);
        }

        @Override
        public void add(T item, int score) {
            LFNode pred, curr, node = new LFNode(item, score);
            do {
                Window window = find(score);
                pred = window.pred;
                curr = window.curr;
                node.next.set(curr, false);
            } while (!pred.next.compareAndSet(curr, node, false, false));
        }

        @Override
        public T removeMin() {
            boolean snip;
            while (true) {
                Window window = findMin();
                LFNode pred = window.pred, curr = window.curr, succ = curr.next.getReference();
                if(succ == null){//empty list
                    return null;
                }
                snip = curr.next.compareAndSet(succ, succ, false, true);
                if(!snip) continue;
                pred.next.compareAndSet(curr,succ,false,false);
                return curr.item;
            }
        }

        private Window findMin() {
            return find(Integer.MIN_VALUE);
        }

        private Window find(int score) {
            Window result;
            while ((result = tryFind(score)) == null) ;
            return result;
        }

        private Window tryFind(int score) {
            boolean[] marked = {false};
            boolean snip;
            LFNode pred = head;
            LFNode curr = pred.next.getReference();
            LFNode succ;
            while (true) {
                succ = curr.next.get(marked);
                while (marked[0]) {
                    snip = pred.next.compareAndSet(curr, succ, false, false);
                    if (!snip) return null;
                    curr = succ;
                    succ = curr.next.get(marked);
                }
                if (curr.score >= score)
                    return new Window(pred, curr);
                pred = curr;
                curr = succ;
            }
        }

        private class Window {
            public LFNode pred, curr;

            public Window(LFNode pred, LFNode curr) {
                this.pred = pred;
                this.curr = curr;
            }
        }

        class LFNode {
            final T item;
            final int score;
            final AtomicMarkableReference<LFNode> next = new AtomicMarkableReference<>(null, false);

            LFNode(T item, int score) {
                this.item = item;
                this.score = score;
            }
        }
    }
}
