
/*
 *    MIRES Recovery Service for mobile applications that use Firebase
 *  
 *    Recovery module : module that supports the Admin Recovery
 */

//############################################################################################
//
//                                     IMPORTs
//
//############################################################################################

// Sleep function
const sleep = (milliseconds) => {
  return new Promise(resolve => setTimeout(resolve, milliseconds))
}

// Import
const MIRESconfig = require('../../../Configuration/MIRESconfiguration.json');
const notify = require('./notify');
const initializeApp = require('../../../Configuration/intializeAdmins');
const getlog = require('./getLog');
const lockingPhase = require('./lockingPhase');
const reconstructionPhase = require('./reconstructionPhase');
const globalVariables = require('./globalVariables');
const auxiliarFunctions = require('./auxiliarFunctions');
const fileRecovery = require('./fileRecovery.js');


// Admin SDKs
APPAdmin = initializeApp.APPAdmin;
MIRESAdmin = initializeApp.MIRESAdmin;


//############################################################################################
//
//                                     VARIABLES
//
//############################################################################################

var waiting_time=5000

//############################################################################################
//
//                                     MAIN FUNCTION
//
//############################################################################################

module.exports ={
  recoverTransaction : async function recoverTransaction(transaction_id, message){

    try{    
          await auxiliarFunctions.printMessage("RECOVERY STARTED");

          // Send notification with admin's message
          await notify.notifyUsers("Recovery initiated: "+message,null);
        
          // Block database
          await APPAdmin.firestore().collection(MIRESconfig.MIRES_database_block).doc(MIRESconfig.MIRES_doc_block).set({recovery:true});

          await auxiliarFunctions.printMessage("DATABASE LOCKED");

          // Wait for users recovery request
            var result=true;
            while(true){
              result = await waitForUserRecovery();
              if(result){
                break;
              }
              else{
                // Sleep for 5 seconds
                console.log("====>>>> Waiting for the users recovery.");
                await sleep(waiting_time)
              }
            }

            // Wait for a empty pool
            var result=true;
            while(true){
              result = await waitForPool();
              if(result){
                break;
              }
              else{
                // Sleep for 5 seconds
                console.log("====>>>> Waiting for the process of flags.");
                await sleep(waiting_time)
              }
            }

            // Lock phase initiated
            var begin_lock_phase = new Date().getTime();
        
            // Begin
            globalVariables.addTransactionToAnalyse(transaction_id);
            globalVariables.addBegginingOfTransaction(transaction_id,0);

            // Get log
            var log = await getlog.process(transaction_id);

            var files_log = await fileRecovery.getLog();

            while(true){

                //var local_log;
                var local_id;
               
                // If there nothing more to recover
                if (globalVariables.getNumberOfTransactionsToAnalyse()==0)
                {break;}

                else{local_id=globalVariables.getTransactionAndRemove();}

                // Block documents
                await lockingPhase.process(local_id,log);

                if(MIRESconfig.FILE_RECOVERY=="true"){
                  // Block files
                  await fileRecovery.blockFiles(local_id,files_log);
                }

                // Add interactions recovered
                globalVariables.addMaliciousTransaction(local_id);       
            }
            
            console.log("Malicious files: "+globalVariables.getBlockedFiles().length)

            console.log("Malicious transactions: "+globalVariables.getMaliciousTransactions().length)
            
            // Reset variables
            lockingPhase.resetVariables();
            
            var end_lock_phase = new Date().getTime();

            await auxiliarFunctions.printMessage("LOCKING PHASE TERMINATED: "+(end_lock_phase-begin_lock_phase)+" (ms)");

            // Unblock all the DB and say to users
            await APPAdmin.firestore().collection(MIRESconfig.MIRES_database_block).doc(MIRESconfig.MIRES_doc_block).delete();

            await auxiliarFunctions.printMessage("DATABASE UNLOCKED")
          
            await notify.notifyUsers("Recovery: application unlocked however some actions cannot be pefomed.",null);

            // Reconstruct the documents
            await reconstructionPhase.process(globalVariables.getBlockedDocs(), globalVariables.getInfectedTimestamps(),globalVariables.getMaliciousTransactions());

            if(MIRESconfig.FILE_RECOVERY=="true"){
              // File recovery
              await fileRecovery.reconstructFiles(globalVariables.getBlockedFiles(),globalVariables.getMaliciousTransactions());
            }

            var end_reconstruction_phase = new Date().getTime();

            await auxiliarFunctions.printMessage("RECONSTRUCTION PHASE TERMINATED: "+(end_reconstruction_phase-end_lock_phase)+" (ms)");

            // Notify users
            await notify.notifyUsers("Recovery completed.",null);
            
            await globalVariables.resetGlobalVariables();
   
            auxiliarFunctions.printMessage("RECOVERY TERMINATED")
            
          return "OK";
          

        }catch(err){
          auxiliarFunctions.printMessage("ERROR: "+err);
          throw new Error();
        }
  }
}


//##############################################################################################
//
//                                          AUXILIAR FUNCTIONS
//
//##############################################################################################



// Wait for users' recovery requests to finish
function waitForUserRecovery(){

  return APPAdmin.firestore().collection(MIRESconfig.MIRES_users_recovery).get().then(snapshot =>{
    // If recovery does not have users' recovery requests
    if(snapshot.empty){
         return true;
    }
    // Else, continue to wait
    else{
      return false;
      }
    }).catch(err => {
      throw new Error("waitForUserRecovery function: "+err)
    })
  }

  // Wait for users' recovery requests to finish 
function waitForPool(){

  return APPAdmin.firestore().collection(MIRESconfig.MIRES_users_flags).get().then(flags =>{
    // If recovery does not have users' recovery requests
    if(flags.empty){
         return true;
    }
    // Else, clean flags pool if possible
    else{
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
    }).catch(err => {
      throw new Error("waitForPool function: " + err)
    })
  }
