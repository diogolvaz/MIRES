/**
 *    MIRES project
 * 
 *    Definition: server that supports the Admin Console Module
 */


//##############################################################################################
//
//                                          IMPORTS
//
//##############################################################################################

// Modules
const express = require('express');
const app = express();
const server = require('http').createServer(app);
const bodyParser = require('body-parser');
const io = require('socket.io')(server);
const path = require('path');

// Config
app.use(express.static(path.join(__dirname, '/public')))
app.use(bodyParser.urlencoded({ extended: true }));

// Imports
const MIRESconfig = require('../../Configuration/MIRESconfiguration.json')
const initializeApp = require('../.././Configuration/intializeAdmins');

const unblockDocuments = require('./modules/unblockDocuments');
const mainRecovery = require('./modules/mainRecovery');
const auxiliarFunctions = require('../../AdminRecovery/Server/modules/auxiliarFunctions');

// Admin SDKs
APPAdmin = initializeApp.APPAdmin;
MIRESAdmin = initializeApp.MIRESAdmin;

//##############################################################################################
//
//                                          VARIABLES
//
//##############################################################################################

//Port
var port = process.env.PORT || MIRESconfig.USERS_MODULE_port;

//##############################################################################################
//
//                                          INTERFACE ON
//
//##############################################################################################

  server.listen(port, function () {
    auxiliarFunctions.printMessage("USERS RECOVERY SERVER: ON || PORT: "+port)
  })

   
//##############################################################################################
//
//                                          CONNECTION 
//
//###############################################################################################

// Connect
io.on('connection', function(socket){
  auxiliarFunctions.printMessage("USERS RECOVERY INTERFACE: ON || PORT: "+port )
  socket.on('disconnect' , function(){
    auxiliarFunctions.printMessage("USERS RECOVERY INTERFACE: OFF")
    });     
});


//##############################################################################################
//
//                                          LISTENNERS 
//
//###############################################################################################


// Connect
unblockDocuments.ON();
mainRecovery.ON();