/**
 *    MIRES Recovery Service for mobile applications that use Firebase
 * 
 *    Thesis Project - Diogo Lopes Vaz, IST
 * 
 *    MIRESnotify contain all the necessary functions to notify the users
 */


//##############################################################################################
//
//                                        IMPORTs
//
//##############################################################################################

var MIRESconfig = require('../../../Configuration/MIRESconfiguration.json');
const auxiliarFunctions = require('./auxiliarFunctions');
const userToken = require('./getTokens');

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
  notifyUsers : async function notifyUsers(msg,user){

      var tokens=[];
      
      var payload = {
        notification: {
          title: "MIRES_RECOVERY_SERVICE",
          body:msg,
        }
      };  
  
        if(user==null){
          // Get the list of device tokens
          tokens=await userToken.getAllTokens();
        }
        else{
          tokens=await userToken.getUserToken(user);
        }

        if(tokens.length>0){

          // Send notification to each user
          tokens.forEach(async token=>{
            APPAdmin.messaging().sendToDevice(token[0], payload)
              .then(function(response) {
                auxiliarFunctions.printMessage("Notification sent to user "+ token[1]+".");
              })
              .catch(function(error) {
                if (error.code === 'messaging/invalid-registration-token' || error.code === 'messaging/registration-token-not-registered') {
                  auxiliarFunctions.printMessage(" User "+ token[1] +" not registered anymore.");
                  AppAdmin.firestore().collection(MIRESconfig.MIRES_users_tokens).doc(token[1]).delete();
                }
                else{
                  auxiliarFunctions.printMessage("Error sending notification to user "+ token[1]+".")
                }
              });
            })  
        }
        else{
          console.log("No message sent.");
        }
    },
 
};

