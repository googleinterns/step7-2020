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

package com.google.sps.model;

import java.util.List;

public class TasksResponse {
  private List<String> taskListNames;
  private int tasksToComplete;
  private int tasksDueToday;
  private int tasksCompletedToday;
  private int tasksOverdue;

  public TasksResponse(List<String> taskListNames, int tasksToComplete, int tasksDueToday, int tasksCompletedToday,
      int tasksOverdue) {
    this.taskListNames = taskListNames;
    this.tasksToComplete = tasksToComplete;
    this.tasksDueToday = tasksDueToday;
    this.tasksCompletedToday = tasksCompletedToday;
    this.tasksOverdue = tasksOverdue;
  }

  public List<String> getTaskListNames() {
    return taskListNames;
  }

  public int getTasksToComplete() {
    return tasksToComplete;
  }

  public int getTasksDueToday() {
    return tasksDueToday;
  }

  public int getTasksCompletedToday() {
    return tasksCompletedToday;
  }

  public int getTasksOverdue() {
    return tasksOverdue;
  }
}
