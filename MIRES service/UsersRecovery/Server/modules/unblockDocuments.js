/**
*    MIRES Recovery Service for mobile Applications that use Firebase
* 
*    Thesis Project - Diogo Lopes Vaz, IST
*/


//##############################################################################################
//
//                                          IMPORTS
//
//##############################################################################################

// Sleep function
const sleep = (milliseconds) => {
    return new Promise(resolve => setTimeout(resolve, milliseconds))
  }

// Imports
const MIRESconfig = require('../../.././Configuration/MIRESconfiguration.json');
const auxiliarFunctions = require('../../../AdminRecovery/Server/modules/auxiliarFunctions');

//##############################################################################################
//
//                                          VARIABLES
//
//##############################################################################################

// Global variables
var flags_listenner;

//##############################################################################################
//
//                                          LISTENNER
//
//##############################################################################################

module.exports =  { 
    ON :  function ON() {

    auxiliarFunctions.printMessage("USERS UNBLOCKING PROCESS: ON");

    flags_listenner = APPAdmin.firestore().collection(MIRESconfig.MIRES_users_flags)
            .onSnapshot(querySnapshot => {
                querySnapshot.docChanges().forEach( async change => {
                // New flag
                if (change.type === 'added') {
                    // If the document is blocked
                    if(change.doc.data().blocked==true){
                        unlockDocument(change.doc.id,change.doc.data().doc)
                    }
                }
                });
            });
        
    },

    OFF: function OFF() {
        auxiliarFunctions.printMessage("USERS UNBLOCKING PROCESS: OFF");      
        flags_listenner();
    }
}

//##############################################################################################
//
//                                          MAIN FUNCTION
//
//##############################################################################################

async function unlockDocument(operation_id,doc_path){

    await sleep(MIRESconfig.USERS_MODULE_unblock_time);
    
    var begin = new Date().getTime();
  
    var app_document = APPAdmin.firestore().doc(doc_path);
                
     APPAdmin.firestore().runTransaction(function(transaction) {
                 return transaction.get(app_document).then(doc => {
                    if(doc.exists){
                        if(doc.data().MIRES_operation_id==operation_id && doc.data().MIRES_locked==false && doc.data().MIRES_blocked==true){
                            transaction.update(app_document,{MIRES_blocked:false, MIRES_ignore:true},{merge:true});
                            return true;
                        }else{
                            return false;
                        }
                    }
                    else{
                        return false;
                    }
                })           
              }).then(function(result) {
                  if(result){
                    console.log("#########################################################################################");
                    console.log("                                UNBLOCK THREAD PROCESS                                   ");
                    console.log("#########################################################################################");
                    console.log("====>>>> DOC UNLOCKED: ", doc_path);
                    console.log("====>>>> TIME: ", new Date().getTime()-begin +" (ms).");
                    console.log("#########################################################################################");
                  }
                  else{
                    console.log("#########################################################################################");
                    console.log("                                UNBLOCK THREAD PROCESS                                   ");
                    console.log("#########################################################################################");
                    console.log("====>>>> unlockDocument function: document "+ doc_path + " cannot be unlocked");
                    console.log("#########################################################################################");
                  }
              
              }).catch(function(error) {
                console.log("#########################################################################################");
                console.log("                                UNBLOCK THREAD PROCESS                                   ");
                console.log("#########################################################################################");
                console.log("====>>>> ", error);
                console.log("#########################################################################################");
              });
  
          } 



