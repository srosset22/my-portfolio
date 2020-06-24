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
import com.google.sps.Events;
import com.google.sps.MeetingRequest;
import com.google.sps.TimeRange;

public final class FindMeetingQuery {
  
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    Collection<TimeRange> answer = Arrays.asList(TimeRange.WHOLE_DAY);
    
    // duration of requested meeting
    long duration = request.getDuration();
    if (duration >= TimeRange.WHOLE_DAY.duration()) {
        return Arrays.asList();
    }

    if (events.isEmpty()) {
        return answer;
    }

    //list of requested meeting attendees
    Collection<String> requestedAttendees = request.getAttendees();
    if (requestedAttendees == null) {
        return answer;
    }

    // list of optional meeting attendees
    Collection<String> optionalAttendees = request.getOptionalAttendees();

    //list of all events
    List<Event> allEvents = new ArrayList<Event>();
    allEvents.addAll(events);

    // list of optional attendee events
    List<Event> optionalAttendeeEvents = new ArrayList<Event>();
    for (Event event : events) {
        if (optionalAttendeesOnly(event, optionalAttendees)) {
            optionalAttendeeEvents.add(event);
            System.out.println("optional event(s):" + event);
        }
    }

    List<Event> mandatoryAttendeeEvents = new ArrayList<Event>();
    mandatoryAttendeeEvents.addAll(allEvents);
    for (Event event : optionalAttendeeEvents) {
        mandatoryAttendeeEvents.remove(event);
    }
    System.out.println("mandatory events:" + mandatoryAttendeeEvents);


    //requested and existing event attendees don't match
    if (requestedAttendees.size() == 1) {
        for (Event event : events) {
            Collection<String> eventAttendees = event.getAttendees();
            for (String eventAttendee : eventAttendees) {
                for (String reqAttendee : requestedAttendees) {
                    if (!(eventAttendee.equals(reqAttendee))) {
                        return answer;
                    }
                }
            }
        }
    }
 
    Collection<TimeRange> everyoneTimeOptions = getAllTimes(allEvents, request);

    if (everyoneTimeOptions.isEmpty() && !mandatoryAttendeeEvents.isEmpty()) {
        System.out.println("Second call: on mandatory events only");
        System.out.println("mand:" + mandatoryAttendeeEvents);
        System.out.println("opt: " + optionalAttendeeEvents);
        
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
        int wholeDayDuration = TimeRange.WHOLE_DAY.duration();
        boolean last = false;

        Event firstEvent = events.get(0);
        TimeRange firstStart = firstEvent.getWhen();
        int start = firstStart.start();
        TimeRange before = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, start, false);
        if (TimeRange.START_OF_DAY != start) {
            possibleTimes.add(before); 
        }
        for (int i = 1; i < events.size(); i++) {
            TimeRange curEvent = events.get(i-1).getWhen();
            TimeRange nextEvent = events.get(i).getWhen();
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
                    if (events.size() > i+1) {
                        TimeRange nextnextEvent = events.get(i+1).getWhen();
                        int nextnextEventStart = nextnextEvent.start();
                        TimeRange timeBetweenEvents = TimeRange.fromStartEnd(curEventEnd, nextnextEventStart, false);
                        int timeBetweenEventsInt = nextnextEventStart - curEventEnd;
                        if (timeBetweenEventsInt >= requestedDuration) {
                            possibleTimes.add(timeBetweenEvents);
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
        
        Event lastEvent = events.get(events.size()-1);
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
