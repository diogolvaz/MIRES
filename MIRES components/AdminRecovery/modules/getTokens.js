
/**
 *    MIRES Recovery Service for mobile applications that use Firebase
 * 
 *    Thesis Project - Diogo Lopes Vaz, IST
 * 
 *    Module that loads the tokens necessary to communicate with the mobile devices
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
//                                        MAIN FUNCTIONS
//
//##############################################################################################


// Get user token
module.exports = {
    getUserToken : async function getUserToken(user_id){
      var tokens = [];
        return await APPAdmin.firestore().collection(MIRESconfig.MIRES_users_tokens).doc(user_id).get()
          .then(doc =>{
            if(doc.exists){
              var user =[];
              user.push(doc.data().token);
              user.push(user_id)
              tokens.push(user)
              return tokens
            }
            else{
              console.log("User "+user_id+" token not found.");
              return tokens
            }
          })
    },
// Get all users tokens
    getAllTokens : async function getAllTokens(){
        const allTokens = await APPAdmin.firestore().collection(MIRESconfig.MIRES_users_tokens).get();
        var tokens = [];
        allTokens.forEach((tokenDoc) => {
            var user =[];
            user.push(tokenDoc.data().token);
            user.push(tokenDoc.id);
            tokens.push(user);
        });
        return tokens;
    },

    // Get users ids
    getAllUsersIDS : async function getAllUsersIDS(){
      try{
        const allTokens = await APPAdmin.firestore().collection(MIRESconfig.MIRES_users_tokens).get();
        var users =[];
        allTokens.forEach((tokenDoc) => {
            users.push(tokenDoc.id);
        });
        return users;
      }
      catch(Error){
        return [];
      }
    }
  };