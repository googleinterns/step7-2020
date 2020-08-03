// Copyright 2020 Google LLC
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

package com.google.sps.data;

import com.google.sps.utility.DateInterval;
import java.util.*;

/** Class containing the response to be converted to Json. */
public final class PlanMailResponse {

  final int wordCount;
  final int averageReadingSpeed;
  final int minutesToRead;
  final List<DateInterval> potentialMeetingTimes;

  /** Initialize the class with all the parameters required. */
  public PlanMailResponse(
      int wordCount,
      int averageReadingSpeed,
      int minutesToRead,
      List<DateInterval> potentialMeetingTimes) {
    this.wordCount = wordCount;
    this.averageReadingSpeed = averageReadingSpeed;
    this.minutesToRead = minutesToRead;
    this.potentialMeetingTimes = potentialMeetingTimes;
  }

  public int getWordCount() {
    // Useful for testing purposes
    return wordCount;
  }

  public int getAverageReadingSpeed() {
    // Useful for testing purposes
    return averageReadingSpeed;
  }

  public int getMinutesToRead() {
    // Useful for testing purposes
    return minutesToRead;
  }

  public List<DateInterval> getPotentialMeetingTimes() {
    // Useful for testing purposes
    return potentialMeetingTimes;
  }
}
