package com.thomaskioko.podadddict.app.data.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harjot on 15-May-16.
 */
public class Queue {
    private List<UnifiedTrack> queue;

    public Queue() {
        queue = new ArrayList<>();
    }

    public List<UnifiedTrack> getQueue() {
        return queue;
    }

    public void setQueue(List<UnifiedTrack> queue) {
        this.queue = queue;
    }

    public void addToQueue(UnifiedTrack track){
        queue.add(track);
    }

}
