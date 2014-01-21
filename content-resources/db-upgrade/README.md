# README

## Getting Started

### Install Node.js

See instruction of your platform at http://nodejs.org/

### Install the application

At the root directory of the application

    npm install

### Configure

1. Rename config.js.example to config.js
2. Change the configurations according to your database

### Running scripts

At the root directory of the application

    SCRIPT=<script> node app.js

Example of running a script

    SCRIPT=wfs2-to-published-views node app.js