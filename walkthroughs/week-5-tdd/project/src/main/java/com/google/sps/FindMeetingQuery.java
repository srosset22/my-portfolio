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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import com.google.sps.Events;
import com.google.sps.MeetingRequest;
import com.google.sps.TimeRange;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    //throw new UnsupportedOperationException("TODO: Implement this method.");


    Collection<TimeRange> answer = Arrays.asList(TimeRange.WHOLE_DAY);

    //list of requested meeting attendees
    Collection<String> requestedAttendees = request.getAttendees();
    if (requestedAttendees == null) {
        return answer;
    }
    
    //duration of requested meeting
    long duration = request.getDuration();
    if (duration > TimeRange.WHOLE_DAY.duration()) {
        answer = Arrays.asList();
        return answer;
    }

    // The event should split the day into two options (before and after the event)
    if (events.size() == 1) {
        for (Event event : events) {
            TimeRange eventTimeRange = event.getWhen();
            int start = eventTimeRange.start();
            int end = eventTimeRange.end();

            Collection<TimeRange> splitDay = Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, start, false),
            TimeRange.fromStartEnd(end, TimeRange.END_OF_DAY, true));

            return splitDay;
            //answer+= splitDay;
        }
        //return answer;
    }


    if (events.size() == 2) {
        int count = 0;
        int firstStart = 0;
        int firstEnd = 0;
        int secondStart = 0;
        int secondEnd = 0;
        for (Event event : events) {
            TimeRange eventTimeRange = event.getWhen();
            if (count == 0) {
                firstStart = eventTimeRange.start();
                firstEnd = eventTimeRange.end();
            }
            else {
                secondStart = eventTimeRange.start();
                secondEnd = eventTimeRange.end();
            }
            count++;
        }
        Collection<TimeRange> times = Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, firstStart, false),
            TimeRange.fromStartEnd(firstEnd, secondStart, false),
            TimeRange.fromStartEnd(secondEnd, TimeRange.END_OF_DAY, true));
        return times;
    }
    


    return answer;

  }
}
