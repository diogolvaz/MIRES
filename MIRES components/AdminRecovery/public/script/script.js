/**
*    MIRES Recovery Service for mobile applications that use Firebase
* 
*    Thesis Project - Diogo Lopes Vaz, IST
*/


//###################################################################################################################################################
//
//                                                                          CONFIG
//
//###################################################################################################################################################

// Socket conection
var port = window.location.port
var domain = window.location.hostname
var socket;

// Start list for the log
listenForLog(domain,port);


//###################################################################################################################################################
//
//                                                                          GLOBAL DATA
//
//###################################################################################################################################################

// Transaction to recover
var transaction_to_recover=null;

// Operation selected
var operation_selected=null;

//  Users Ids to send message
var user_IDs=[];

//###################################################################################################################################################
//
//                                                                    OPERATIONS - FUNCTIONS
//
//###################################################################################################################################################

// Construct the operations table
function constructOperationsTable(operations){
  
  operations.forEach(row => {

    // Empty content string.
    var tableContent = '';

    //Add log row.
    tableContent += '<tr onclick="getOperationData(\''+row.operation_id+'\')" id="operation_'+row.operation_id+'">';
    tableContent += '<td width="40%" class="text-center">'+ row.operation_id+'</td>';
    tableContent += '<td width="40%" class="text-center">' + row.timestamp + '</td>';
    tableContent += '<td width="20%" class="text-center">' + row.type + '</td>';
    tableContent += '</tr>';

    $("#operations_table_content").append(tableContent);
  });

}


// Get data associated with the request
function getOperationData(operation_id){

  mainLoaderON("LOADING")

 $.ajax({  
   url: 'http://'+domain+':'+port+'/getOperationData',  
   type: 'POST',   
   data: {operation_id:operation_id}, 
   success: function (data, textStatus, xhr) {
    mainLoaderOFF();
    // Show operation's data
    operationInfoModalON(data.data, data.doc, data.reads, data.files);
    },  
   error: function (xhr, textStatus, errorThrown) {
    mainLoaderOFF();
    alert('ERROR: Error when getting the data about the operation: ' + operation_id);  
    }  
});
}

// Clear the operations table
function clearOperationsTable(){
  document.getElementById("operations_table_content").innerHTML='';
}


// Opearations Info Modal ON
function operationInfoModalON(data, doc, reads, files){

  // Show doc path
  document.getElementById("operation_document_path").innerHTML=' <p>'+doc+'</p>';
  document.getElementById("operation_data_content").innerHTML= '<p>'+printOperationRequestData(data)+'</p>';

  // Set read dependencies (if exists)
  if(reads.length>0){
    tableContent='<h3 class="text-center">READ</h3>';
    reads.forEach(read=>{
      tableContent+="<h5>Path:</h5>";
      tableContent+="<div class='data_row'><p>"+ read.doc +"</p></div>";
      tableContent+="<h5>Version:</h5>";
      tableContent+="<div class='data_row'><p>"+ read.version +"</p></div>";
      tableContent+="<h5>Field-values:</h5>";
      tableContent+="<div class='data_row'><p>"+ read.data +"</p></div>";
      tableContent+="<p></p>";
    })
    document.getElementById("operation_data_reads").innerHTML=tableContent;
  }

  // Set images (if exists)
  if(files.length>0){
    console.log(files);
    tableContent='<h3 class="text-center">IMAGES</h3>';
    files.forEach(file => {
      tableContent+="<h5>Image:</h5>";
      tableContent+="<div class='data_row'><p>"+ file.path +"</p></div>";
      tableContent+="<p></p>";
    })
    document.getElementById("operation_data_images").innerHTML=tableContent;
  }

  blockModal('#operations_info_modal');

  // Show the modal
  $('#operations_info_modal').modal('show');
}

function operationInfoModalOFF(){
  // Show the modal
  $('#operations_info_modal').modal('hide');
}

// Print the write request
function printOperationRequestData(data){

  // Empty content string.
  var tableContent = '<div>';
  for (var key in data) {
    tableContent+= '<p id="operations_request_data_row">'+ key.bold()+ " : " + data[key] +'</p>';
  }
  tableContent += '</div>';
  return tableContent;
}

// Manage the transactions to Recover
function addRemoveOperation(operation_id){
  if(operation_selected!=null){
    // Unselect last transaction
    let previous_operation_id = operation_selected;
    if(operation_id!=previous_operation_id){
      setBackgroundColor("operation_"+previous_operation_id,"white","black");
      setBackgroundColor("operation_"+operation_id,"grey","white");
      operation_selected=operation_id;
    }
    else{
      // Clear operations table
      operation_selected=null;
    }
  }
  else{
    // Select current transaction
    setBackgroundColor("operation_"+operation_id,"grey","white");
    operation_selected=operation_id;
  }
}

//####################################################################################################################################################
//
//                                                                    TRANSACTIONS FUNCTIONS 
//
//###################################################################################################################################################

// Start listen for the log
function listenForLog(){

  socket = io.connect('http://'+domain+':'+port);
  // Listen for log updates
  socket.on('transaction', function(message) {
    // Create row
    if (message.type === 'added') {
      addTransactionRow(message.transaction_id,message.user_id);
    }
    // Delete row
    else if (message.type === 'removed') {
      removeTransactionRow(message.transaction_id);
    }
  });
}

// Stop listenning the log
function stopListenForLog(){
  socket.disconnect();
}
 
// Clear the operations table
function clearTransactionsTable(){
  document.getElementById("transactions_table_content").innerHTML='';
}

// Refresh the transactions log
async function refreshLog(){
  mainLoaderON("LOADING");
  stopListenForLog();
  clearTransactionsTable();
  clearOperationsTable();
  listenForLog();
  mainLoaderOFF();
}

// Add a new row to the log.
function addTransactionRow(transaction_id,user_id)
{
  if(!document.getElementById(transaction_id)){
    // Empty content string.
    var tableContent = '';
    
    //Add log row.
    tableContent += '<tr onclick="addRemoveTransaction(\''+transaction_id+'\')" id=transaction_'+transaction_id+'>';
    tableContent += '<td width="60%" class="text-center">'+ transaction_id+'</td>';
    tableContent += '<td width="40%" class="text-center">' + user_id + '</td>';
    tableContent += '</tr>';

    var transaction_row = document.getElementById('transaction_'+transaction_id);

    if(transaction_row==null){
       // Add row to the log.
      $("#transactions_table_content").prepend(tableContent);
    }
    else{
      // Add row to the log.
      $("#transaction_"+transaction_id).innerHTML = tableContent;
    }
    }
}


// Remove a specific log row.
function removeTransactionRow(transaction_id){
 // Remove log row.
 $("#transaction_"+transaction_id+"").remove();
}


// Get data associated with the request
function getOperations(transaction_id){

  mainLoaderON("LOADING");

 $.ajax({  
   url: 'http://'+domain+':'+port+'/getOperations',  
   type: 'POST',   
   data: {transaction_id:transaction_id}, 
   success: function (data, textStatus, xhr) {
    constructOperationsTable(data);
    mainLoaderOFF();
    },  
   error: function (xhr, textStatus, errorThrown) {
     // Error when loading operations  
     alert('ERROR: Error when getting the operations from transaction: ' + transaction_id);
     clearOperationsTable()
     mainLoaderOFF();
    }  
});
}

// Manage the transactions to Recover
function addRemoveTransaction(transaction_id){
  operation_selected=null;
  if(transaction_to_recover!=null){
    // Unselect last transaction
    clearOperationsTable();
    let previous_transaction_id = transaction_to_recover;
    setBackgroundColor("transaction_"+previous_transaction_id,"white","black");
    if(transaction_id!=previous_transaction_id){
      // Select current transaction
      setBackgroundColor("transaction_"+transaction_id,"grey","white");
      transaction_to_recover=transaction_id;
      getOperations(transaction_id)
    }
    else{
      // Clear operations table
      transaction_to_recover=null;
    }
  }
  else{
    // Select current transaction
    setBackgroundColor("transaction_"+transaction_id,"grey","white");
    transaction_to_recover=transaction_id;
    getOperations(transaction_id)
  }

}

//####################################################################################################################################################
//
//                                                                    RECOVERY - FUNCTIONS
//
//####################################################################################################################################################

function startRecovery(){

  // If there are transactions selected
  if(transaction_to_recover!=null){
    // Show the modal
    recoveryModalON();
  }
  else{
    alert('No transaction selected.');
  }
}

// Recover transaction
function recoverTransactions(){
  
 
  // Hide recovery modal
  recoveryModalOFF();

  // Show main loader
  mainLoaderON("RECOVERING");

  // Reset operations table
  clearOperationsTable();
  
 // Send undo POST to the server
 $.ajax({  
  url: 'http://'+domain+':'+port+'/recoverTransactions',  
   type: 'POST',   
   data: {transactions:transaction_to_recover, message:$('#recovery_modal_message').val()},  
   success: function (data, textStatus, xhr) {

    mainLoaderOFF();

    alert('SUCCESS: Transaction ' + transaction_to_recover +' successfully recovered. (See shell output for more details)');

    // Reset transaction to recover
    transaction_to_recover=null;

    },  
   error: function (xhr, textStatus, errorThrown) {
    mainLoaderOFF();
    alert('ERROR: Error at recovering transaction '+ transaction_to_recover+'. (See shell output for more details)');
    
    }  
});
}

// Recovery modal ON
function recoveryModalON(){

   // Set message title and footer button
   document.getElementById("recovery_modal_title").innerHTML='<h4 class="modal-title" id="recovery_modal_title">Recovery Message</h4>'
   document.getElementById("recovery_modal_footer").innerHTML='<button id="recovery_modal_footer_button" type="button" onclick="recoverTransactions()">Confirm Recovery</button>'
   // Reset message
   document.getElementById("recovery_modal_data").innerHTML='<textarea id="recovery_modal_message" class="form-control"></textarea>'  
   
   // Prevent from closing when outside
   blockModal('#recovery_modal');

   // Show modal
   $('#recovery_modal').modal('show');
}

// Recovery modal OFF
function recoveryModalOFF(){

  // Hide modal
  $('#recovery_modal').modal('hide');
}

//####################################################################################################################################################
//
//                                                                    UTILS
//
//####################################################################################################################################################

// Loader ON
function mainLoaderON(message){
  document.getElementById('main_loader_message').innerHTML='<h2>'+message+'</h2>'
  document.getElementById('main_loader').style.visibility="visible"
}

// Loader Off
function mainLoaderOFF(){
  document.getElementById('main_loader').style.visibility="hidden"
}

//Set operation ID
function setBackgroundColor(id,background_color, text_color){
  document.getElementById(id).style.backgroundColor=background_color;
  document.getElementById(id).style.color=text_color;
}

// Prevent from closing when clicking outside
function blockModal(id){
     $(id).modal({
      backdrop: 'static',
      keyboard: false
    });
}
//####################################################################################################################################################
//
//                                                                    NOTIFICATIONS
//
//####################################################################################################################################################

// Main send message function
function sendMessage(){
  mainLoaderON("LOADING");
  getUsersIds();
}

// Users modal OFF
function usersMessagesModalOFF(){
  // Hide modal
  $('#usersIds_info_modal').modal('hide');
}

// Users modal ON
function usersMessagesModalON(){

  blockModal('#usersIds_info_modal');

  // Hide modal
  $('#usersIds_info_modal').modal('show');
}

// Users modal OFF
function usersMessageNotificationModalOFF(){

  // Hide modal
  $('#recovery_notification_modal').modal('hide');
}

// Users modal ON
function usersMessageNotificationModalON(){

  // Prevent from closing when outside
  blockModal('#recovery_notification_modal');

  // Show modal
  $('#recovery_notification_modal').modal('show');
}

// Get the list of users
function getUsersIds(){
  $.ajax({  
    url: 'http://'+domain+':'+port+'/getUsersIds',  
    type: 'POST',   
    data: {}, 
    success: function (data, textStatus, xhr) {
      listOfUsers(data);
     },  
    error: function (xhr, textStatus, errorThrown) {
      // Error when loading operations  
      alert('Error when getting the user IDs.');
      mainLoaderOFF();
     }  
 });
}

// Construct the list of users
function listOfUsers(users){

  users.forEach(userID => {
    // Empty content string.
    var tableContent = '';
      
    //Add log row.
    tableContent += '<tr onclick="addRemoveUserID(\''+userID+'\')" id=user_ID_'+userID+'>';
    tableContent += '<td class="text-center">'+ userID+'</td>';
    tableContent += '</tr>';

    // Add row to the log.
    document.getElementById("userdIds_table_content").innerHTML=tableContent;
  });

  mainLoaderOFF()
  usersMessagesModalON();
}

// Add/Remove each user on the modal
function addRemoveUserID(userId){
  const index = user_IDs.indexOf(userId);
  if(index > -1 ) {
    setBackgroundColor("user_ID_"+userId,"white","black");
    user_IDs.splice(index,1);
  }
  else{
    // Select current transaction
    setBackgroundColor("user_ID_"+userId,"grey","white");
    user_IDs.push(userId);
  }
}

// Modal to define the notification message
function defineRecoveryMessage(){
  
  if(user_IDs.length>0){

    usersMessagesModalOFF();

    // Set message title and footer button
    document.getElementById("recovery_notification_modal_title").innerHTML='<h4 class="modal-title" id="recovery_notification_modal_title">Define Notification Message</h4>'
    document.getElementById("recovery_notification_modal_footer").innerHTML='<button id="recovery_notification_modal_footer_button" type="button" onclick="sendNotification()">Send</button>'
    // Reset message
    document.getElementById("recovery_notification_modal_data").innerHTML='<textarea id="recovery_notification_modal_message" class="form-control"></textarea>'  
    
    usersMessageNotificationModalON();
 
  }
  else{
    alert("No user selected.");
  }
}

// Send notification
function sendNotification(){

      //Create modal with client ids 
      $.ajax({  
        url: 'http://'+domain+':'+port+'/sendMessages',  
        type: 'POST',   
        data: {usersIDs:user_IDs,msg:$('#recovery_notification_modal_message').val()}, 
        success: function (data, textStatus, xhr) {
          users_ids=[];
          alert('SUCCESS: Notifications sent! (See shell output for more details).');
          usersMessageNotificationModalOFF();
        },  
        error: function (xhr, textStatus, errorThrown) {
          // Error when loading operations  
          alert('ERROR: Error when sending messaged to users. (See shell output for more details)');
          users_ids=[];
        }  
    });
}

// Clear users selected
function clearUsersIdsList(){
  user_IDs=[];
}


