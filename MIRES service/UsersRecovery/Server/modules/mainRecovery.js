
/**
 *    MIRES Recovery Service for mobile Applications that use Firebase
 * 
 *    Thesis Project - Diogo Lopes Vaz, IST
 */


  //############################################################################################
  //
  //                                     IMPORTS
  //
  //############################################################################################

// Sleep function
const sleep = (milliseconds) => {
  return new Promise(resolve => setTimeout(resolve, milliseconds))
}

// Import
const MIRESconfig = require('../../../Configuration/MIRESconfiguration.json');
const notify = require('../../../AdminRecovery/Server/modules/notify');
const reconstructionPhase = require('../../../AdminRecovery/Server/modules/reconstructionPhase');
const auxiliarFunctions = require('../../../AdminRecovery/Server/modules/auxiliarFunctions');

  //############################################################################################
  //
  //                                     VARIABLES
  //
  //############################################################################################

let recovery_listenner;
// Miliseconds
var waiting_time=5000;

  //############################################################################################
  //
  //                                     LISTENNER
  //
  //############################################################################################

module.exports ={

  ON: function ON(){

    auxiliarFunctions.printMessage("USERS RECOVERY: ON")
    
    // Listen for users' recovery requests, from admin and clients
    recoveryListenner= APPAdmin.firestore().collection(MIRESconfig.MIRES_users_recovery)
    .onSnapshot(querySnapshot => {
      querySnapshot.docChanges().forEach(async change => {
        if(change.type === 'added'){
          // Recover user request
          recoverTransaction(change.doc.id,change.doc.data().from,change.doc.data().documents, change.doc.data().timestamp);
        }
      });
    });
  },

  OFF: function OFF(){
    auxiliarFunctions.printMessage("USERS RECOVERY: OFF")
    recovery_listenner();
    }
}


  //############################################################################################
  //
  //                                     MAIN FUNCTION
  //
  //############################################################################################



async function recoverTransaction(transaction_id, user_id,documents, recovery_timestamp){

  try{   

          await auxiliarFunctions.printMessage("USER RECOVERY INITIATED")
         
          // Wait for the transaction to be logged
          while(true){
            var transaction = await waitForDocumentsLog(documents,recovery_timestamp);
            if(transaction){
              break;
            }
            else{
              await sleep(waiting_time);
              console.log("====>>>> Waiting for the document's log.");
            }
          }

           // Notify users
           await notify.notifyUsers("Recovery initiated.",user_id);

           var begin_recovery = new Date().getTime();

          // Transaction to undo
          var malicious_transactions=[];
          malicious_transactions.push(transaction_id); 

          // Get time of infection
          var infected_timestamp = await getTimestampOfInfection(transaction_id);
        
          // Reconstruct the documents
          await reconstructionPhase.process(documents, infected_timestamp,malicious_transactions);

          // Notify users
          await notify.notifyUsers("Recovery completed.",user_id);

          var end_recovery = new Date().getTime();

          await auxiliarFunctions.printMessage("RECONSTRUCTION PHASE TERMINATED: "+(end_recovery-begin_recovery)+" (ms)");

          // Delete recovery request
          await APPAdmin.firestore().collection(MIRESconfig.MIRES_users_recovery).doc(transaction_id).delete();          

          await auxiliarFunctions.printMessage("USER RECOVERY TERMINATED.")

      }catch(err){
        auxiliarFunctions.printMessage("ERROR: "+err);
        return "ERROR";
      }
}


//############################################################################################
//
//                                     AUXILIAR FUNCTIONS
//
//############################################################################################º


// Get the infected timestamp
async function getTimestampOfInfection(transaction_id){

  var infected_timestamp={};

  return await MIRESAdmin.firestore().collection(MIRESconfig.MIRES_log).where("transaction_id", "==", transaction_id).get()
  .then(rows => {
    if(rows.empty){
      // Transaction not found on the log
      throw new Error("getTimestampOfInfection function: cannot get the timestamps of infection from each document.");
      }
    else{
      // Get the timestamps of infection
      rows.forEach(row =>{
        infected_timestamp[row.data().doc]= row.data().timestamp;
      });
      return infected_timestamp;
    }
  }).catch(err => {
    throw new Error("getTimestampOfInfection function: " + err);
  });
}


// Wait for the flags to be processed
async function waitForDocumentsLog(documents,timestamp){
  
  // Get the rest of the transaction
    var documents_length = documents.length;
    for(var i=0;i<documents_length;i++){
          return APPAdmin.firestore().collection(MIRESconfig.MIRES_users_flags).where("doc","==",documents[i]).where("timestamp","<=",timestamp).get()
          .then(flags => {
            if (flags.empty)
              {
                return true;
              } 
            else 
              { 
                 // Clean flags
                 flags.forEach(flag=>{
                  MIRESAdmin.firestore().collection(MIRESconfig.MIRES_log).doc(flag.id).get().then(log_record=>{
                    if(log_record.exists){
                      MIRESAdmin.firestore().collection(MIRESconfig.MIRES_log).doc(flag.id).delete();
                    }else{
                      return false;
                    }
                  })
                })
                return false;
              }
          })
   }
  }
