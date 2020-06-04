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
import com.google.sps.data.ServerStats;
//import com.google.gson.Gson;
import java.util.Date;
//import com.google.sps.servlets.Dataservlet;
import java.util.ArrayList;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    //response.setContentType("text/html;");
    //response.getWriter().println("<h1>Hello Sophia!</h1>");

    //ArrayList<String> cities= new ArrayList<>();
    //cities.add("New York");
    //cities.add("London");
    //cities.add("Tokyo");
    String cityOne = "Paris";
    String cityTwo = "New York";
    String cityThree = "Tokyo";
    //console.log(cities);

    // Convert the cities content to JSON
    ServerStats serverStats = new ServerStats(cityOne, cityTwo, cityThree);
    String json = convertToJson(serverStats);

    // Send the JSON as the response
    response.setContentType("application/json;");
    response.getWriter().println(json);

  }

  /**
   * Converts a ServerStats instance into a JSON string using manual String concatentation.
  */
  private String convertToJson(ServerStats serverStats) {
    String json = "{";
    json += "\"cityOne\": ";
    json += "\"" + serverStats.getCityOne() + "\"";
    json += ", ";
    json += "\"cityTwo\": ";
    json += "\"" + serverStats.getCityTwo() + "\"";
    json += ", ";
    json += "\"cityThree\": ";
    json += "\"" + serverStats.getCityThree() + "\"";
    json += "}";
    return json;
  }

}


