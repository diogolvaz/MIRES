/**
 *    MIRES Recovery Service for mobile applications that use Firebase
 * 
 *    Thesis Project - Diogo Lopes Vaz, IST
 * 
 */

//##############################################################################################
//
//                                        IMPORTS
//
//##############################################################################################

const globalVariables = require('./globalVariables');
const readline = require('readline');


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

module.exports = {

    // Merge two JSON objects
    merge : function merge(beforeobj, afterobj) {

    const result = {};
    
    Object.keys(beforeobj || {}).concat(Object.keys(afterobj || {})).forEach(key => {
       if (afterobj.hasOwnProperty(key) && beforeobj.hasOwnProperty(key)){
          if(afterobj!="REMOVED"){
            result[key] = afterobj[key];
          }
      }
      else{
        if (afterobj.hasOwnProperty(key) && !beforeobj.hasOwnProperty(key)){
          result[key] = afterobj[key];
        }
        else if(beforeobj.hasOwnProperty(key) && !afterobj.hasOwnProperty(key)){
          result[key]=beforeobj[key];
        }
      }
    });
    return result;
    },

    // print message
    printMessage: function printMessage(message){
      console.log("#########################################################################################")
      console.log("====>>>> "+message)
      console.log("#########################################################################################")
      }

}
