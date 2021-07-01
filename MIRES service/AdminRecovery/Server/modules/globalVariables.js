/**
 *    MIRES Recovery Service for mobile applications that use Firebase
 * 
 *    Thesis Project - Diogo Lopes Vaz, IST
 * 
 *    Global Variables  used on the recovery Admin Recovery    
 */
//##############################################################################################
//
//                                        IMPORTS
//
//##############################################################################################



//##############################################################################################
//
//                                        VARIABLES
//
//##############################################################################################

 // Blocked files
 var blocked_files = []
 // Blocked docs
 var blocked_docs = [] /*new Set();*/
 // Timestamp when blocked documents where infected
 var infected_timestamp={}
 // Transaction to analyze
 var transactions_to_analyze = []
 // Beggining of each transaction
 var transactions_begin = {}
 // Transaction already analyzed and identified as malicious
 var malicious_transactions =[]


//##############################################################################################
//
//                                        MAIN FUNCTIONS
//
//##############################################################################################

module.exports = {
    resetGlobalVariables : function resetGlobalVariables() {
        blocked_docs = []
        infected_timestamp={}
        transactions_to_analyze=[]
        transactions_begin = {}
        malicious_transactions = []
        blocked_files = []
      },
  
      //***************************************************************************************** */

      // Add transaction to analyse
      addTransactionToAnalyse : function addTransactionToAnalyse(transaction){
        transactions_to_analyze.push(transaction)
      },

      getNumberOfTransactionsToAnalyse : function getNumberOfTransactionsToAnalyse(){
        return transactions_to_analyze.length;
      },

      getTransactionAndRemove : function getTransactionAndRemove(){
        return transactions_to_analyze.shift();
      },
  
      //***************************************************************************************** */

      // Add document to block
      addDocToBlock : function addDocToBlock(doc){
        blocked_docs.push(doc)
      },

      blockedDocAlreadyStored : function blockedDocAlreadyStored(doc){
        return blocked_docs.includes(doc)
      },

      getBlockedDocs : function getBlockedDocs(){
        return blocked_docs
      },
  
      //***************************************************************************************** */

      // Add file to block
      addFileToBlock : function addFileToBlock(file){
          blocked_files.push(file)
      },

      blockedFileAlreadyStored : function blockedFileAlreadyStored(file){
        return blocked_files.includes(file)
      },

      getBlockedFiles : function getBlockedFiles(){
        return blocked_files
      },

      //***************************************************************************************** */

      // Add Beggining of Transaction
      addBegginingOfTransaction : function addBegginingOfTransaction(transaction_id, local_id){
        transactions_begin[transaction_id]=local_id
      },

      getTransactionBeggin : function getTransactionBegin(transaction_id){
        return transactions_begin[transaction_id]
      },

      transactionBegginingAlreadyStored : function transactionBegginingAlreadyStored(transaction_id){
        return transactions_begin.hasOwnProperty(transaction_id)
      },


      //***************************************************************************************** */
  
      // Add timestamp of infected transaction
      addTimestampOfInfectedTransaction : function addTimestampOfInfectedTransaction(doc, timestamp){
        infected_timestamp[doc]=timestamp;
      },

      getInfectedTimestamps : function getInfectedTimestamps(){
          return infected_timestamp;
      },

      //***************************************************************************************** */


      addMaliciousTransaction : function addMaliciousTransaction(transaction){
      malicious_transactions.push(transaction)
      },

      maliciousTransactionAlreadyStored : function maliciousTransactionAlreadyStored(transaction_id){
        return malicious_transactions.includes(transaction_id)
      },

      getMaliciousTransactions : function getMaliciousTransactions(){
          return malicious_transactions;
      }

    };