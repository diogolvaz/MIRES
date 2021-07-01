/**
 *    MIRES Recovery Service for mobile applications that use Firebase
 * 
 *    Thesis Project - Diogo Lopes Vaz, IST
 * 
 *    Module that loads the log to be analysed on the locking phase
 */

//##############################################################################################
//
//                                        IMPORTS
//
//##############################################################################################

var MIRESconfig = require('../../../Configuration/MIRESconfiguration.json');

//##############################################################################################
//
//                                        VARIABLES
//
//##############################################################################################


//##############################################################################################
//
//                                        MAIN FUNCTION
//
//##############################################################################################


//Send notifications to all users
module.exports = {
    process : async function process(id){

    // Prepare the recovery process
    var log_to_analyze=[];
    var first_record;

    first_record = await getRecord(id);

      // Add the first record
      var logRow ={
        id:first_record.id,
        data:first_record.data(),
      } 

      log_to_analyze= await getLogToAnalyze(logRow);

      return log_to_analyze;
    }
}


//##############################################################################################
//
//                                 AUXILIAR FUNCTIONS
//
//##############################################################################################


// Get the log records of the interaction
function getRecord(id){
  return MIRESAdmin.firestore().collection(MIRESconfig.MIRES_log).orderBy("timestamp","asc").where("transaction_id","==",id).limit(1).get()
      .then(querySnapshot => {
        if (querySnapshot.empty)
          {
          throw new Error("getRecord function: log is empty.");
          } 
        else 
          { 
            return querySnapshot.docs[0];
          }
      }) 
      .catch(err => {
        throw new Error("getRecord function: "+ err);
        });
}


// Get log to analyze
function getLogToAnalyze(request){

var log =[];
return MIRESAdmin.firestore().collection(MIRESconfig.MIRES_log).where('timestamp', '>=', request.data.timestamp).get()
.then(snapshot => {
      if(snapshot.empty){
        console.log("getLogToAnalyze function: log is empty.");
        return log;
      }
      else{
        // Get the existing log
        snapshot.forEach(doc => {
          const logRow ={
            id:doc.id,
            data:doc.data(),
          }
            log.push(logRow);
        });
        return log;
      }
    })
    .catch(err => {
      throw new Error("getLogToAnalyze function: "+err);
    });        
} 