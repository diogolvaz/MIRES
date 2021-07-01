/**
*    MIRES Recovery Service for mobile applications that use Firebase
* 
*    Thesis Project - Diogo Lopes Vaz, IST
*
*    Module that listens the log database
*/

//##############################################################################################
//
//                                        IMPORTs
//
//##############################################################################################


// Imports
var MIRESconfig = require('../../.././Configuration/MIRESconfiguration.json');
const initializeApp = require('../../.././Configuration/intializeAdmins');
const auxiliarFunction = require('./auxiliarFunctions');

// Admin SDKs
MIRESAdmin = initializeApp.MIRESAdmin;

//##############################################################################################
//
//                                        VARIABLES
//
//##############################################################################################

var log_listenner;


//##############################################################################################
//
//                                        MAIN FUNCTION
//
//##############################################################################################


module.exports =  { 
  ON :  function ON(io) {
    auxiliarFunction.printMessage("LOG LISTENNER: ON");
    log_listenner = MIRESAdmin.firestore().collection(MIRESconfig.MIRES_log).orderBy('timestamp', 'asc')
              .onSnapshot(querySnapshot => {
                querySnapshot.docChanges().forEach(change => {
                  io.emit('transaction',  {type:change.type,transaction_id: change.doc.data().transaction_id, user_id: change.doc.data().user_id});
                });
              }); 
            },

  OFF :  function OFF() {    
    auxiliarFunction.printMessage("LOG LISTENNER: OFF");
    log_listenner();
          }
      }