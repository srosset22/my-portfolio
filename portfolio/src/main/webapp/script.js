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

google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawChart);

/**
 * Adds a random fact about me to the page.
 */
function addRandomFact() {
  const facts =
      ['I am half Brazilian', 'I love long distance running', 'I play the piano'];

  // Pick a random greeting
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page
  const factContainer = document.getElementById('fact-container');
  factContainer.innerText = fact;
}

/*
function getGreeting() {
  const greeting = 'Hello Sophia!';
  fetch('/data').then(response => response.text()).then((greeting) => {
    document.getElementById('greeting-container').innerText = greeting;
  });
}
*/

/** Creates an <li> element containing text. */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}

/** Creates an comment element containing author and content. */
function createCommentElement(author, content) {
  const com = document.createElement('li');
  com.innerText = author + ": " + content;
  return com;
}

function displayComments() {
  fetch('/add-comment').then(response => response.json()).then((comments) => {
      const commentList = document.getElementById('comment-container');
      for (var comment in comments) {
        commentList.appendChild(createCommentElement(comments[comment], comment));
      }
  });
}

function drawChart() {
  fetch('/country-data').then(response => response.json())
  .then((countryVotes) => {
    const data = new google.visualization.DataTable();
    data.addColumn('string', 'Country');
    data.addColumn('number', 'Votes');
    Object.keys(countryVotes).forEach((country) => {
      data.addRow([country, countryVotes[country]]);
    });

    const options = {
      'title': 'Which Country Should I Visit Next?',
      'width':600,
      'height':500
    };

    const chart = new google.visualization.ColumnChart(
        document.getElementById('chart-container'));
    chart.draw(data, options);
  });
}

//Fetches the login status from the servlet. If user is logged in, unhide comment form
//if user is not logged in, display a login link

function fetchLoginStatus () {
    fetch('/login').then(response => response.json()).then((login) => {
        const greeting = document.getElementById('login-greeting');
        greeting.innerText = "Hello " + login.loginInfo[0];
        
        if (login.loginInfo[0].localeCompare("Guest") != 0){
        console.log("Is logged in");
        console.log(login);
        console.log(login.loginInfo[0]);
        document.getElementById('comment-form').style.display = 'block';   

        const loginContainer = document.getElementById('login-container');
        loginContainer.innerHTML = '<a href="' + login.loginInfo[1] + '">Logout here</a>';

        }
        else {
        console.log("is not logged in");
        console.log(login);
        const loginContainer = document.getElementById('login-container');        
        loginContainer.innerHTML = '<a href="' + login.loginInfo[1] + '">Login here</a>';
        }
    });
}

fetchLoginStatus();
