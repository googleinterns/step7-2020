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
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.repackaged.com.google.gson.reflect.TypeToken;
import com.google.common.collect.ImmutableList;
import com.google.sps.model.AuthenticationVerifier;
import com.google.sps.model.TasksClient;
import com.google.sps.model.TasksClientFactory;
import com.google.sps.servlets.TasksServlet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.hamcrest.CoreMatchers;
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
public final class TasksServletTest {
  private AuthenticationVerifier authenticationVerifier;
  private TasksClientFactory tasksClientFactory;
  private TasksClient tasksClient;
  private TasksServlet servlet;
  private HttpServletRequest request;
  private HttpServletResponseFake response;

  private static final Gson gson = new Gson();
  private static final Type LIST_OF_TASKS_TYPE = new TypeToken<List<Task>>() {}.getType();

  private static final boolean AUTHENTICATION_VERIFIED = true;
  private static final String ID_TOKEN_KEY = "idToken";
  private static final String ID_TOKEN_VALUE = "sampleId";
  private static final String ACCESS_TOKEN_KEY = "accessToken";
  private static final String ACCESS_TOKEN_VALUE = "sampleAccessToken";
  private static final Cookie sampleIdTokenCookie = new Cookie(ID_TOKEN_KEY, ID_TOKEN_VALUE);
  private static final Cookie sampleAccessTokenCookie =
      new Cookie(ACCESS_TOKEN_KEY, ACCESS_TOKEN_VALUE);
  private static final Cookie[] validCookies =
      new Cookie[] {sampleIdTokenCookie, sampleAccessTokenCookie};

  // Tasks must be returned in order of retrieval - JSON includes tasks in desired order
  private static final String TASK_TITLE_ONE = "task one";
  private static final String TASK_TITLE_TWO = "task two";
  private static final String TASK_TITLE_THREE = "task three";
  private static final String TASK_TITLE_FOUR = "task four";

  private static final String TASKLIST_ID_ONE = "taskListOne";
  private static final String TASKLIST_ID_TWO = "taskListTwo";

  private static final TaskList taskListOne = new TaskList().setId(TASKLIST_ID_ONE);
  private static final TaskList taskListTwo = new TaskList().setId(TASKLIST_ID_TWO);
  private static final List<TaskList> NO_TASKLISTS = ImmutableList.of();
  private static final List<TaskList> SOME_TASKLISTS = ImmutableList.of(taskListOne, taskListTwo);

  private static final List<Task> NO_TASKS = ImmutableList.of();
  private static final List<Task> TASKS_ONE_TWO =
      ImmutableList.of(new Task().setTitle(TASK_TITLE_ONE), new Task().setTitle(TASK_TITLE_TWO));
  private static final List<Task> TASKS_THREE_FOUR =
      ImmutableList.of(new Task().setTitle(TASK_TITLE_THREE), new Task().setTitle(TASK_TITLE_FOUR));

  private static final String SAMPLE_NOTES = "sample notes";
  private static final String DUE_DATE = "2020-07-20T00:00:00.000Z";
  private static final Task validTask =
      new Task().setTitle(TASK_TITLE_ONE).setNotes(SAMPLE_NOTES).setDue(DUE_DATE);
  private static final String VALID_TASK_JSON = gson.toJson(validTask);

  @Before
  public void setUp() throws IOException, GeneralSecurityException {
    authenticationVerifier = Mockito.mock(AuthenticationVerifier.class);
    tasksClientFactory = Mockito.mock(TasksClientFactory.class);
    tasksClient = Mockito.mock(TasksClient.class);
    servlet = new TasksServlet(authenticationVerifier, tasksClientFactory);

    Mockito.when(tasksClientFactory.getTasksClient(Mockito.any())).thenReturn(tasksClient);
    // Authentication will always pass
    Mockito.when(authenticationVerifier.verifyUserToken(Mockito.anyString()))
        .thenReturn(AUTHENTICATION_VERIFIED);

    request = Mockito.mock(HttpServletRequest.class);
    response = new HttpServletResponseFake();
    Mockito.when(request.getCookies()).thenReturn(validCookies);
  }

  @Test
  public void noTaskLists() throws IOException, ServletException {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(NO_TASKLISTS);
    servlet.doGet(request, response);
    List<Task> tasks = gson.fromJson(response.getStringWriter().toString(), LIST_OF_TASKS_TYPE);
    Assert.assertTrue(tasks.isEmpty());
  }

  @Test
  public void emptyTaskLists() throws IOException, ServletException {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(SOME_TASKLISTS);
    Mockito.when(tasksClient.listTasks(SOME_TASKLISTS.get(0))).thenReturn(NO_TASKS);
    Mockito.when(tasksClient.listTasks(SOME_TASKLISTS.get(1))).thenReturn(NO_TASKS);
    servlet.doGet(request, response);
    List<Task> tasks = gson.fromJson(response.getStringWriter().toString(), LIST_OF_TASKS_TYPE);
    Assert.assertTrue(tasks.isEmpty());
  }

  @Test
  public void oneEmptyTaskList() throws IOException, ServletException {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(SOME_TASKLISTS);
    Mockito.when(tasksClient.listTasks(SOME_TASKLISTS.get(0))).thenReturn(NO_TASKS);
    Mockito.when(tasksClient.listTasks(SOME_TASKLISTS.get(1))).thenReturn(TASKS_ONE_TWO);
    servlet.doGet(request, response);

    List<Task> tasks = gson.fromJson(response.getStringWriter().toString(), LIST_OF_TASKS_TYPE);
    Assert.assertEquals(2, tasks.size());
    Assert.assertThat(tasks, CoreMatchers.hasItems(TASKS_ONE_TWO.get(0), TASKS_ONE_TWO.get(1)));
  }

  @Test
  public void completeTaskList() throws IOException, ServletException {
    Mockito.when(tasksClient.listTaskLists()).thenReturn(SOME_TASKLISTS);
    Mockito.when(tasksClient.listTasks(SOME_TASKLISTS.get(0))).thenReturn(TASKS_ONE_TWO);
    Mockito.when(tasksClient.listTasks(SOME_TASKLISTS.get(1))).thenReturn(TASKS_THREE_FOUR);
    servlet.doGet(request, response);

    List<Task> tasks = gson.fromJson(response.getStringWriter().toString(), LIST_OF_TASKS_TYPE);
    Assert.assertEquals(4, tasks.size());
    Assert.assertThat(
        tasks,
        CoreMatchers.hasItems(
            TASKS_ONE_TWO.get(0),
            TASKS_ONE_TWO.get(1),
            TASKS_THREE_FOUR.get(0),
            TASKS_THREE_FOUR.get(1)));
  }

  @Test
  public void postTaskNullTaskListId() throws IOException, ServletException {
    servlet.doPost(request, response);
    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void postTaskEmptyTaskListId() throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("taskListId"))).thenReturn("");
    servlet.doPost(request, response);
    Assert.assertEquals(400, response.getStatus());
  }

  @Test
  public void postValidTask() throws IOException, ServletException {
    Mockito.when(request.getParameter(Mockito.eq("taskListId"))).thenReturn(TASKLIST_ID_ONE);
    Mockito.when(tasksClient.postTask(Mockito.eq(TASKLIST_ID_ONE), Mockito.any()))
        .thenReturn(validTask);

    StringReader reader = new StringReader(VALID_TASK_JSON);
    BufferedReader bufferedReader = new BufferedReader(reader);
    Mockito.when(request.getReader()).thenReturn(bufferedReader);

    servlet.doPost(request, response);

    Task postedTask = gson.fromJson(response.getStringWriter().toString(), Task.class);

    Assert.assertEquals(validTask.getTitle(), postedTask.getTitle());
    Assert.assertEquals(validTask.getNotes(), postedTask.getNotes());
    Assert.assertEquals(validTask.getDue(), postedTask.getDue());
  }
}
