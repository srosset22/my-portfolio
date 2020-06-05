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

package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import com.google.sps.data.Comments;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/add-comment")
public class CommentServlet extends HttpServlet {
  
  Comments comment_list = new Comments();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Load comments from Datastore
    Query query = new Query("Comment");
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = ds.prepare(query);

    Comments existing_comments = new Comments();
    for (Entity entity : results.asIterable()) {
      String comment = (String) entity.getProperty("content");
      existing_comments.addToCommentsList(comment);
    }
    response.setContentType("application/json");
    String json = new Gson().toJson(existing_comments.getComment());
    response.getWriter().println(json);

    
    // Get comments in the form of JSON
    //response.setContentType("application/json");
    //String json = new Gson().toJson(comment_list.getComment());
    //response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the comment input from the form
    String comment = getParameter(request, "comment-input", "");
    response.setContentType("text/html;");
    //response.getWriter().println(comment);
    
    //add new comment to the comments ArrayList
    comment_list.addToCommentsList(comment);
    response.getWriter().println(comment_list.getComment());

    //store comments as entities in Datastore
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("content", comment);
    //commentEntity.setProperty("author", author);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }


  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
}


