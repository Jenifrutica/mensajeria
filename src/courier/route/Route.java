package courier.route;

import courier.shipment.Shipment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Route {
    private Stop first;
    private Stop last;
    private Stop current;
    private int size;

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public Stop getCurrent() {
        return current;
    }

    public Stop getFirst() {
        return first;
    }

    public Stop getLast() {
        return last;
    }

    public void addLast(Shipment shipment) {
        Stop stop = new Stop(shipment);
        attachLast(stop);
        size++;
        if (current == null) {
            current = stop;
        }
    }

    public boolean insertAt(Shipment shipment, int position) {
        if (position < 1 || position > size + 1) {
            return false;
        }
        Stop stop = new Stop(shipment);
        attachAt(stop, position);
        size++;
        if (current == null) {
            current = stop;
        }
        return true;
    }

    public Shipment removeByGuide(String guide) {
        Stop stop = find(guide);
        if (stop == null) {
            return null;
        }
        Stop following = stop.getNext();
        detach(stop);
        size--;
        if (current == stop) {
            current = firstNotDeliveredFrom(following);
        }
        return stop.getShipment();
    }

    public boolean moveToPosition(String guide, int position) {
        Stop stop = find(guide);
        if (stop == null || position < 1 || position > size) {
            return false;
        }
        if (positionOf(stop) == position) {
            return true;
        }
        detach(stop);
        size--;
        attachAt(stop, position);
        size++;
        return true;
    }

    public Stop relocateCurrentToEnd() {
        Stop stop = current;
        if (stop == null) {
            return null;
        }
        if (stop == last) {
            return current;
        }
        Stop following = stop.getNext();
        detach(stop);
        size--;
        attachAt(stop, size + 1);
        size++;
        current = firstNotDeliveredFrom(following);
        if (current == null) {
            current = firstNotDeliveredFrom(first);
        }
        return current;
    }

    public void advanceAfterCurrent() {
        if (current == null) {
            return;
        }
        current = firstNotDeliveredFrom(current.getNext());
    }

    public Stop find(String guide) {
        for (Stop stop = first; stop != null; stop = stop.getNext()) {
            if (stop.getShipment().getGuide().equalsIgnoreCase(guide)) {
                return stop;
            }
        }
        return null;
    }

    public Stop stopAt(int position) {
        if (position < 1 || position > size) {
            return null;
        }
        Stop stop = first;
        for (int index = 1; index < position; index++) {
            stop = stop.getNext();
        }
        return stop;
    }

    public int positionOf(Stop target) {
        int position = 1;
        for (Stop stop = first; stop != null; stop = stop.getNext()) {
            if (stop == target) {
                return position;
            }
            position++;
        }
        return -1;
    }

    public List<Stop> forwardView() {
        List<Stop> stops = new ArrayList<>(size);
        for (Stop stop = first; stop != null; stop = stop.getNext()) {
            stops.add(stop);
        }
        return Collections.unmodifiableList(stops);
    }

    public List<Stop> backwardView() {
        List<Stop> stops = new ArrayList<>(size);
        for (Stop stop = last; stop != null; stop = stop.getPrevious()) {
            stops.add(stop);
        }
        return Collections.unmodifiableList(stops);
    }

    public List<Stop> remainingForward() {
        List<Stop> stops = new ArrayList<>(size);
        for (Stop stop = first; stop != null; stop = stop.getNext()) {
            if (stop.getStatus() != StopStatus.ENTREGADA) {
                stops.add(stop);
            }
        }
        return Collections.unmodifiableList(stops);
    }

    public boolean containsGuide(String guide) {
        return find(guide) != null;
    }

    public void clear() {
        first = null;
        last = null;
        current = null;
        size = 0;
    }

    private void attachLast(Stop stop) {
        stop.setPrevious(last);
        stop.setNext(null);
        if (last == null) {
            first = stop;
        } else {
            last.setNext(stop);
        }
        last = stop;
    }

    private void attachAt(Stop stop, int position) {
        if (position == 1) {
            stop.setPrevious(null);
            stop.setNext(first);
            if (first == null) {
                last = stop;
            } else {
                first.setPrevious(stop);
            }
            first = stop;
            return;
        }
        Stop previous = stopAt(position - 1);
        Stop following = previous.getNext();
        stop.setPrevious(previous);
        stop.setNext(following);
        previous.setNext(stop);
        if (following == null) {
            last = stop;
        } else {
            following.setPrevious(stop);
        }
    }

    private void detach(Stop stop) {
        Stop previous = stop.getPrevious();
        Stop following = stop.getNext();
        if (previous == null) {
            first = following;
        } else {
            previous.setNext(following);
        }
        if (following == null) {
            last = previous;
        } else {
            following.setPrevious(previous);
        }
        stop.setPrevious(null);
        stop.setNext(null);
    }

    private Stop firstNotDeliveredFrom(Stop start) {
        Stop stop = start;
        while (stop != null && stop.getStatus() == StopStatus.ENTREGADA) {
            stop = stop.getNext();
        }
        return stop;
    }
}
