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

/**
 * Adds a random fact about me to the page.
 */
function addRandomFact() {
  const facts =
      ['I am half Brazilian', 'I love long distance running', 'I play the piano'];

  // Pick a random greeting.
  const fact = facts[Math.floor(Math.random() * facts.length)];

  // Add it to the page.
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

function showCities() {
    fetch('/data').then(response => response.json()).then((cities) => {
    // cities is an object, not a string, so we have to
    // reference its fields to create HTML content
    console.log(cities);
    console.log(cities.cityOne);
    
    const citiesListElement = document.getElementById('cities-container');
    citiesListElement.innerHTML = '';
    citiesListElement.appendChild(
        createListElement('First city: ' + cities.cityOne));
    citiesListElement.appendChild(
        createListElement('Second city: ' + cities.cityTwo));
    citiesListElement.appendChild(
        createListElement('Third city: ' + cities.cityThree));
    
  });
}

/** Creates an <li> element containing text. */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}

function displayComments() {
  fetch('/add-comment').then(response => response.json()).then((comments) => {
      
      console.log(comments);
    
      const commentList = document.getElementById('comment-container');
      comments.forEach((comment) => {
        commentList.appendChild(createListElement(comment));
      });

  });
}

/** Creates an <li> element containing text. */
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}