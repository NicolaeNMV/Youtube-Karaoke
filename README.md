Youtube-Karaoke 0.1 Beta
===============

To run:

1. Run a webserver

2. Execute Lyrics for Google Chrome on your page https://chrome.google.com/webstore/detail/lyrics-for-google-
chrome/oglbipcbkmlknhfhabolnniekmlhfoek

3. Execute the bookmarklet
    javascript:void(function(){if(!document.getElementById('zkaraoke')){var%20zkaraoke=document.createElement('script');zkaraoke.id='zkaraoke';zkaraoke.src='http://127.0.0.1:8000/load.js';document.documentElement.appendChild(zkaraoke);}}());