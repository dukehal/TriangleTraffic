package edu.duke.pratt.hal.triangletraffic.model;

import edu.duke.pratt.hal.triangletraffic.model.Event;

/**
 * Created by tedzhu on 6/10/15.
 */
public class NotificationInfo {

    private boolean dueToTimeCrossing;
    private boolean dueToDistanceCrossing;
    private Event event;

    public NotificationInfo(Event event, boolean dueToTimeCrossing, boolean dueToDistanceCrossing) {
        this.event = event;
        this.dueToTimeCrossing = dueToTimeCrossing;
        this.dueToDistanceCrossing = dueToDistanceCrossing;
    }

    public Event getEvent() {
        return event;
    }

    public boolean isDueToTimeCrossing() {
        return dueToTimeCrossing;
    }

    public boolean isDueToDistanceCrossing() {
        return dueToDistanceCrossing;
    }

}
