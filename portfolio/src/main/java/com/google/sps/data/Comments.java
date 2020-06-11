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

package com.google.sps.data;
import java.util.ArrayList;
import java.util.HashMap;

/** Class containing comment information. */

public final class Comments {

  // List of comments and their authors
  private final HashMap<String, String> comments = new HashMap<String, String>();
  
  public void addToCommentsList(String comment, String author) {
      comments.put(comment, author);
      System.out.println(comments);
  }

  public HashMap<String, String> getComment () {
      return comments;
  }

}
