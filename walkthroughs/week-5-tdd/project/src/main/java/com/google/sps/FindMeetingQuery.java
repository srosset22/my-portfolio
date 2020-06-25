// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import com.google.sps.Events;
import com.google.sps.MeetingRequest;
import com.google.sps.TimeRange;

public final class FindMeetingQuery {
  
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    
    // duration of requested meeting
    long duration = request.getDuration();
    if (duration >= TimeRange.WHOLE_DAY.duration()) {
        return Arrays.asList();
    }

    if (events.isEmpty()) {
        return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    //list of requested meeting attendees
    Collection<String> requestedAttendees = request.getAttendees();
    List<String> reqAttendees = new ArrayList<>();
    reqAttendees.addAll(requestedAttendees);
    if (reqAttendees == null) {
        return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    // list of optional meeting attendees
    Collection<String> optionalAttendees = request.getOptionalAttendees();

    //list of all events
    List<Event> allEvents = new ArrayList<>();
    allEvents.addAll(events);

    // list of optional attendee events
    List<Event> optionalAttendeeEvents = new ArrayList<>();
    for (Event event : events) {
        if (optionalAttendeesOnly(event, optionalAttendees)) {
            optionalAttendeeEvents.add(event);
        }
    }

    List<Event> mandatoryAttendeeEvents = new ArrayList<>();
    mandatoryAttendeeEvents.addAll(allEvents);
    for (Event event : optionalAttendeeEvents) {
        mandatoryAttendeeEvents.remove(event);
    }

    //requested and existing event attendees don't match
    if (reqAttendees.size() == 1) {
        String reqAttendee = reqAttendees.get(0);
        for (Event event : events) {
            Collection<String> eventAttendees = event.getAttendees();
            for (String eventAttendee : eventAttendees) {
                if (!(eventAttendee.equals(reqAttendee))) {
                    return Arrays.asList(TimeRange.WHOLE_DAY);
                }
            }
        }
    }
 
    // find available times based on all events
    Collection<TimeRange> everyoneTimeOptions = getAllTimes(allEvents, request);

    if (everyoneTimeOptions.isEmpty() && !mandatoryAttendeeEvents.isEmpty()) {
        // find available times based on mandatory events only
        return getAllTimes(mandatoryAttendeeEvents, request);
    }

    return everyoneTimeOptions;
  }

    private boolean optionalAttendeesOnly(Event event, Collection<String> optionalAttendees) {
        Set<String> eventAttendees = event.getAttendees();
        for (String optAttendee : optionalAttendees) {
            if (eventAttendees.contains(optAttendee)) {
                return true;
            }
        }
        return false;
    }

    private Collection<TimeRange> getAllTimes (List<Event> events, MeetingRequest request) {
        Collection<TimeRange> possibleTimes = new ArrayList<TimeRange>();
        long requestedDuration = request.getDuration();
        boolean last = false;
        
        // sort events by starting time
        Map<Integer, Event> eventStartTimes = new HashMap<>(); // key: event start time, value: event
        for (Event event : events) {
            int eventStart = event.getWhen().start();
            eventStartTimes.put(eventStart, event);
        }
        List<Integer> eventStartTimesList = new ArrayList<>(eventStartTimes.keySet());
        Collections.sort(eventStartTimesList);

        List<Event> sortedEvents = new ArrayList<>();
        for (int startTime : eventStartTimesList) {
            sortedEvents.add(eventStartTimes.get(startTime));
        }

        TimeRange firstEventTime = sortedEvents.get(0).getWhen();
        int start = firstEventTime.start();
        int ending = firstEventTime.end();
        TimeRange before = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, start, false);
        if (start == TimeRange.START_OF_DAY && (ending-1) == TimeRange.END_OF_DAY) {
            return possibleTimes;
        } else if (TimeRange.START_OF_DAY != start) {
            possibleTimes.add(before); 
        }

        for (int i = 1; i < sortedEvents.size(); i++) {
            TimeRange curEvent = sortedEvents.get(i-1).getWhen();
            TimeRange nextEvent = sortedEvents.get(i).getWhen();
            int curEventEnd = curEvent.end();
            int nextEventStart = nextEvent.start();
            int nextEventEnd = nextEvent.end();

            if (!curEvent.overlaps(nextEvent)) {
                int timeBetweenEventsInt = nextEventStart - curEventEnd;
                if (timeBetweenEventsInt >= requestedDuration) {
                    possibleTimes.add(TimeRange.fromStartEnd(curEventEnd, nextEventStart, false));
                } 
            } else if (curEvent.overlaps(nextEvent)) {
                if (curEventEnd > nextEventEnd) {   //nested events
                    if (sortedEvents.size() > i+1) {
                        TimeRange nextnextEvent = sortedEvents.get(i+1).getWhen();
                        int nextnextEventStart = nextnextEvent.start();
                        if (nextnextEventStart > curEventEnd) {
                            TimeRange timeBetweenEvents = TimeRange.fromStartEnd(curEventEnd, nextnextEventStart, false);
                            int timeBetweenEventsInt = nextnextEventStart - curEventEnd;
                            if (timeBetweenEventsInt >= requestedDuration) {
                                possibleTimes.add(timeBetweenEvents);
                            } 
                        } else if (curEventEnd == TimeRange.END_OF_DAY) {
                            return null;
                        }            
                   } else {
                        last = true;
                        TimeRange timeBetweenEvents = TimeRange.fromStartEnd(curEventEnd, TimeRange.END_OF_DAY, true);
                        int timeBetweenEventsInt = TimeRange.END_OF_DAY - curEventEnd;
                        if (timeBetweenEventsInt >= requestedDuration) {
                            possibleTimes.add(timeBetweenEvents);
                        }    
                    }
                }
            }
        }
        
        Event lastEvent = sortedEvents.get(sortedEvents.size()-1);
        TimeRange lastEnd = lastEvent.getWhen();
        int end = lastEnd.end();
        TimeRange after = TimeRange.fromStartEnd(end, TimeRange.END_OF_DAY, true);
        int timeAfterEventsInt = TimeRange.END_OF_DAY - end;
        if (!last && TimeRange.END_OF_DAY >= end){
            possibleTimes.add(after);
        }   

        return possibleTimes;
    }    
}
