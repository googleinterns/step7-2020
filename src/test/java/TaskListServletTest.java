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

import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.sps.model.TasksClient;
import com.google.sps.model.TasksClientFactory;
import com.google.sps.servlets.TaskListServlet;
import java.util.HashMap;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

/**
 * Test that the Tasks Servlet responds to the client with correctly parsed Task names. Assumes
 * AuthenticatedHttpServlet is functioning properly (those tests will fail otherwise).
 */
@RunWith(JUnit4.class)
public final class TaskListServletTest extends AuthenticatedServletTestBase {
  private TasksClientFactory tasksClientFactory;
  private TasksClient tasksClient;
  private TaskListServlet servlet;

  private static final Gson gson = new Gson();

  // Tasks must be returned in order of retrieval - JSON includes tasks in desired order
  private static final String TASK_TITLE_ONE = "task one";
  private static final String TASK_TITLE_TWO = "task two";
  private static final String TASK_TITLE_THREE = "task three";
  private static final String TASK_TITLE_FOUR = "task four";

  private static final String TASKLIST_ID_ONE = "taskListOne";
  private static final String TASKLIST_ID_TWO = "taskListTwo";

  private static final TaskList TASKLIST_ONE = new TaskList().setId(TASKLIST_ID_ONE);
  private static final TaskList TASKLIST_TWO = new TaskList().setId(TASKLIST_ID_TWO);
  private static final List<TaskList> NO_TASKLISTS = ImmutableList.of();
  private static final List<TaskList> SOME_TASKLISTS = ImmutableList.of(TASKLIST_ONE, TASKLIST_TWO);

  private static final List<Task> TASKS_ONE_TWO =
      ImmutableList.of(new Task().setTitle(TASK_TITLE_ONE), new Task().setTitle(TASK_TITLE_TWO));
  private static final List<Task> TASKS_THREE_FOUR =
      ImmutableList.of(new Task().setTitle(TASK_TITLE_THREE), new Task().setTitle(TASK_TITLE_FOUR));

  private static final String VALID_TASKLIST_TITLE = "sampleTaskListName";
  private static final TaskList validTaskList = new TaskList().setTitle(VALID_TASKLIST_TITLE);
  private static final String VALID_TASKLIST_JSON = gson.toJson(validTaskList);

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    tasksClientFactory = Mockito.mock(TasksClientFactory.class);
    tasksClient = Mockito.mock(TasksClient.class);
    servlet = new TaskListServlet(authenticationVerifier, tasksClientFactory);

    Mockito.when(tasksClientFactory.getTasksClient(Mockito.any())).thenReturn(tasksClient);
  }

  @Test
  public void getTasklists() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(SOME_TASKLISTS);
    Mockito.when(tasksClient.listTasks(SOME_TASKLISTS.get(0))).thenReturn(TASKS_ONE_TWO);
    Mockito.when(tasksClient.listTasks(SOME_TASKLISTS.get(1))).thenReturn(TASKS_THREE_FOUR);

    HashMap<String, List<Task>> tasksWithTaskLists = new HashMap<>();
    tasksWithTaskLists.put(SOME_TASKLISTS.get(0).getId(), TASKS_ONE_TWO);
    tasksWithTaskLists.put(SOME_TASKLISTS.get(1).getId(), TASKS_THREE_FOUR);

    JsonObject expectedResponseObject = new JsonObject();
    expectedResponseObject.add("tasklists", gson.toJsonTree(SOME_TASKLISTS));
    expectedResponseObject.add("tasks", gson.toJsonTree(tasksWithTaskLists));

    String expectedResponse = gson.toJson(expectedResponseObject);

    servlet.doGet(request, response);

    Assert.assertTrue(stringWriter.toString().contains(expectedResponse));
  }

  @Test
  public void getTasklistsEmpty() throws Exception {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(NO_TASKLISTS);

    HashMap<String, List<Task>> emptyTasksWithTaskLists = new HashMap<>();

    JsonObject expectedResponseObject = new JsonObject();
    expectedResponseObject.add("tasklists", gson.toJsonTree(NO_TASKLISTS));
    expectedResponseObject.add("tasks", gson.toJsonTree(emptyTasksWithTaskLists));

    String expectedResponse = gson.toJson(expectedResponseObject);

    servlet.doGet(request, response);

    Assert.assertTrue(stringWriter.toString().contains(expectedResponse));
  }

  @Test
  public void postTasklistNullNameGiven() throws Exception {
    servlet.doPost(request, response);
    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void postTasklistEmptyNameGiven() throws Exception {
    Mockito.when(request.getParameter("taskListName")).thenReturn("");
    servlet.doPost(request, response);
    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void postTasklist() throws Exception {
    Mockito.when(request.getParameter("taskListTitle")).thenReturn(VALID_TASKLIST_TITLE);
    Mockito.when(tasksClient.postTaskList(VALID_TASKLIST_TITLE)).thenReturn(validTaskList);

    servlet.doPost(request, response);
    Assert.assertTrue(stringWriter.toString().contains(VALID_TASKLIST_JSON));
  }
}
