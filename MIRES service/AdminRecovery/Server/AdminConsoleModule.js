/**
 *    MIRES project
 * 
 *    Server that supports the Admin Console Module
 */


//##############################################################################################
//
//                                          IMPORTs
//
//##############################################################################################

// Modules
const express = require('express');
const app = express();
const server = require('http').createServer(app);
const bodyParser = require('body-parser');
const io = require('socket.io')(server);
const path = require('path');
const dateFormat = require('dateformat');

// Config
app.use(express.static(path.join(__dirname, '/public')))
app.use(bodyParser.urlencoded({ extended: true }));


// Imports
const MIRESconfig = require('../../Configuration/MIRESconfiguration.json')
const mainRecovery = require('./modules/mainRecovery')
const initializeApp = require('../.././Configuration/intializeAdmins');
const logListenner = require('./modules/logListenner');
const notifyUsers = require('./modules/notify');
const getTokens = require('./modules/getTokens');
const auxiliarFunctions = require('./modules/auxiliarFunctions');


// Admin SDKs
AppAdmin = initializeApp.AppAdmin;
MIRESAdmin = initializeApp.MIRESAdmin;

//##############################################################################################
//
//                                          VARIABLES
//
//##############################################################################################

//Port
var port = process.env.PORT || MIRESconfig.ADMIN_MODULE_port;

//##############################################################################################
//
//                                          INTERFACE ON
//
//##############################################################################################

  server.listen(port, function () {
    auxiliarFunctions.printMessage("ADMIN SERVER: ON || PORT: "+port);
  })

  
//##############################################################################################
//
//                                          SERVER LOGIC
//
//##############################################################################################

// Init console
app.get('/', function (req, res) {
    res.sendFile(__dirname +'/public/index.html');
  })


// Add admin undo request
app.post('/recoverTransactions', async function (req, res) {
  try{
    // Recover transaction
    var response = await mainRecovery.recoverTransaction(req.body.transactions, req.body.message);

    // Recover request initiated
    res.send(response);
   
  }
  catch(err){
    res.status(500).send("ERROR")
  }
});



// Get operations
app.post('/getOperations', function (req, res) {
  // Get transaction's operations
    MIRESAdmin.firestore().collection(MIRESconfig.MIRES_log).where("transaction_id","==",req.body.transaction_id).get().then(rows=>{
      var response=[];
      if(rows.empty){
        // If empty, return error
        res.status(500).send("ERROR")
      }
      else{
        rows.forEach(row => {
          response.push({operation_id:row.data().operation_id,timestamp:dateFormat(row.data().timestamp.toDate(), "yyyy-mm-dd, h:MM:ss TT"),type:row.data().type, doc:row.data().doc});
        });
        // Return the operations
        res.send(response)
      }
    })
})

// Get operations data
app.post('/getOperationData', function (req, res) {

  MIRESAdmin.firestore().collection(MIRESconfig.MIRES_log).where("operation_id","==",req.body.operation_id).get().then(rows=>{
    var response={};
    if(rows.empty){
      // If empty, return error
      res.status(500).send("ERROR");
    }
    else{
      rows.forEach(row => {
        // Send data (if existing)
        if(row.data().hasOwnProperty("data"))
          {
            response["data"] = row.data().data;
          }
          else{
            response["data"]={}
          }
        
        if(row.data().hasOwnProperty("read"))
          {
            response["reads"] = row.data().read;
          }
          else{
            response["reads"]=[]
          }
        if(row.data().hasOwnProperty("files"))
          {
            response["files"] = row.data().files;
          }
          else{
            response["files"]=[]
          }  

        // Send document path
        response["doc"] = row.data().doc;
      });
      // Return the operations
      res.send(response)
    }
  })
})

// Send messages to selected users
app.post('/getUsersIds', async function (req, res) {
  try{
      // Recover transaction
      var users = await getTokens.getAllUsersIDS();
      res.send(users);
  }
  catch(err){
    auxiliarFunctions.printMessage(err);
    res.status(500).send("ERROR")
  }
});


// Send messages to selected users
app.post('/sendMessages', async function (req, res) {
  try{
    req.body.usersIDs.forEach(async user_id =>{
      // Recover transaction
      await notifyUsers.notifyUsers(req.body.msg,user_id);
    })
  res.send("OK");
  }
  catch(err){
    auxiliarFunctions.printMessage(err);
    res.status(500).send("ERROR")
  }
});
 

//##############################################################################################
//
//                                          LISTENNERS 
//
//###############################################################################################


// Connect
io.on('connection', function(socket){

    // Log listenner
    logListenner.ON(io);
    socket.on('disconnect' , function(){
      logListenner.OFF();
      });     
});
