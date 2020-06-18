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
    
    public boolean optionalAttendeesOnly(Event event, Collection<String> optionalAttendees) {
        Set<String> eventAttendees = event.getAttendees();
        for (String optAttendee : optionalAttendees) {
            if (eventAttendees.contains(optAttendee)) {
                return true;
            }
        }
        return false;
    }
  
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    Collection<TimeRange> answer = Arrays.asList(TimeRange.WHOLE_DAY);

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
    //sortByStart(allEvents);

    // list of optional attendee events
    List<Event> optionalAttendeeEvents = new ArrayList<Event>();
    for (Event event : events) {
        if (optionalAttendeesOnly(event, optionalAttendees)) {
            optionalAttendeeEvents.add(event);
        }
    }

    List<Event> mandatoryAttendeeEvents = new ArrayList<Event>();
    mandatoryAttendeeEvents.addAll(allEvents);
    mandatoryAttendeeEvents.remove(optionalAttendeeEvents);

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
    
    //duration of requested meeting
    long duration = request.getDuration();
    if (duration > TimeRange.WHOLE_DAY.duration()) {
        answer = Arrays.asList();
        return answer;
    }

    // The event should split the day into two options (before and after the event)
    if (events.size() == 1) {
        Event e = allEvents.get(0);
        TimeRange eventTimeRange = e.getWhen();
        int start = eventTimeRange.start();
        int end = eventTimeRange.end();
        Collection<TimeRange> splitDay = Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, start, false),
            TimeRange.fromStartEnd(end, TimeRange.END_OF_DAY, true));
        return splitDay;
    }

    if (events.size() >= 2) {
        TimeRange eventOneTimeRange = null;
        TimeRange eventTwoTimeRange = null;
        TimeRange eventThreeTimeRange = null;
        int count = 0;
        for (Event event : events) {
            if (count == 0) {
                eventOneTimeRange = event.getWhen();
            }
            else if (count == 1) {
                eventTwoTimeRange = event.getWhen();
            }
            else {
                eventThreeTimeRange = event.getWhen();
            }
            count++;
        }
        int firstStart = eventOneTimeRange.start();
        int firstEnd = eventOneTimeRange.end();
        int secondStart = eventTwoTimeRange.start();
        int secondEnd = eventTwoTimeRange.end();

        if (eventOneTimeRange.contains(eventTwoTimeRange)) {
            Collection<TimeRange> containedTimes = Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, firstStart, false),
            TimeRange.fromStartEnd(firstEnd, TimeRange.END_OF_DAY, true));
            return containedTimes;
        }
        else if (eventTwoTimeRange.contains(eventOneTimeRange)) {
            Collection<TimeRange> containedTimes = Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, secondStart, false),
            TimeRange.fromStartEnd(secondEnd, TimeRange.END_OF_DAY, true));
            return containedTimes;
        }
        else if (eventOneTimeRange.overlaps(eventTwoTimeRange)) {
            Collection<TimeRange> overlapTimes =
            Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, firstStart, false),
            TimeRange.fromStartEnd(secondEnd, TimeRange.END_OF_DAY, true));
            return overlapTimes;
        }
        else if (firstStart == 0 && secondEnd == 1440) {
            int actualDuration = secondStart - firstEnd;
            if (duration > actualDuration) {
                //not enough time
                Collection<TimeRange> noTime = Arrays.asList();
                return noTime;
            }
            //just enough time
            Collection<TimeRange> justEnoughTime = Arrays.asList(TimeRange.fromStartEnd(firstEnd, secondStart, false));
            return justEnoughTime;
        }

        TimeRange before = TimeRange.fromStartEnd(TimeRange.START_OF_DAY, firstStart, false);
        TimeRange middle = TimeRange.fromStartEnd(firstEnd, secondStart, false);
        TimeRange after = TimeRange.fromStartEnd(secondEnd, TimeRange.END_OF_DAY, true);
        Collection<TimeRange> times = Arrays.asList(before, middle, after);

        //if (optionalAttendees.size() == 1) {
        //}
        
        return times;
    }

    return answer;
  }
}
