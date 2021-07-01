**MIRES** (**M**obile Applications **I**ntrusion **RE**covery **S**ervice) is an intrusion recovery service for mobile applications that use Backend-as-a-Service, e.g., Google's [Firebase](https://firebase.google.com/). The recovery is done online, mostly in parallel with the normal functioning of the application, to increase availability, on the contrary of what offline recovery mechanisms do. 
Moreover, although most applications still use a single backend data store (e.g., a database), many recent applications are starting to use more than one (e.g., a database and a file store), so MIRES supports multi-service recovery.
Finally, MIRES supports a form of client-side recovery in the mobile device to allow users to recover from mistakes.

![alt text](https://github.com/diogolvaz/MIRES/blob/master/MIRES%20service/MIRES_architecture.png?raw=true)



## Getting Started

These instructions will get you a copy of the project up and running on your local machine.


### Prerequisites

- Basic knowledge on Firebase and Android Java development
- Firebase console installed
- NPM installed


### Start MIRES

1. Go to https://firebase.google.com/ and create a Firebase project (this will be MIRES container);
2. Initiate the Firestore database (europe-west2);
3. Change the MIRES Firebase project pricing plan to Blaze (pay-as-you-go);
4. Go to the root folder of MIRES (the folder that contains the package.json file) and run ```npm install``` to install the dependencies;
5. Get the service account key from MIRES Project and save the file in ```MIRES components/Configuration``` with the name ```MIRESAccountKey``` (https://stackoverflow.com/questions/40799258/where-can-i-get-serviceaccountcredentials-json-for-firebase-admin);
6. Go to Firebase and create another Firebase project (this will be the Application container);
7. Get the service account key from the Application project and save the file in ```MIRES components/Configuration``` with the name ```APPAccountKey```;
8. Go to file ```/MIRES components/Configuration/initializeAdmins.js``` and update the APPadmin storageBucker property with the url of the Application Firebase storage;
9. Finally you can run ```npm run start-admin-console``` to initiate the console and access the interface using the url ```http://localhost:4000``` (you can also run ```npm run start-user-recovery-module``` to start the Users Recovery module that will run on port 5000. However, this module does not have an interface);


### Start Mobile Application

1. Go to the Application Firebase project and create the Firestore database (europe-west2);
2. Add the security rules on file ```/Mobile Application components/SecurityRules/SecurityRules.txt``` to the Firestore Security Rules;
3. Enable Email/Password authentication model on the Application Firebase project;
4. Add the android application (```/Mobile Application example/Hify```) to the Application Firebase project;
5. Copy file ```/MIRES components/Configuration/MIRESAccountKey.json``` to ```/Mobile Application example/Hify/functions```;
6. Go to file ```/Mobile Application/Hify/.firebaserc``` and change the default property with the Firebase Application project Id;
7. Change the Application Firebase project pricing plan to Blaze (pay-as-you-go);
7. Got to folder ```/Mobile Application example/Hify``` and run ```firebase deploy --only functions``` to deploy the cloud functions;
8. Now, you can start recovering the application! In the begginning, when recovering an action, some errors will appear "The query requires an index". Just copy the provided url and create the indexes;

Feel free to contact me if you have any question about the project! 

#####################################################################

### Executions supported

The configuration of the Mobile Application example allows the following actions:

- Administrator Recovery:
  - Recover the creation of users;
  - Recover login actions;
  - Recover the creation of text and image posts;
  - Recover the deletion of (only) text posts;

- User Recovery:
  - Recover the creation of text and image posts;
  - Recover the deletion of (only) text posts;

- Snapshot Process:
  - Applied to the Users collection, where logins create new version of the documents;

- Multi-Service Recovery:
  - Recover the creation of users;
  - Recover the creeation of image posts;


**FINAL NOTES:** 
- Firebase is constantly updating, meaning that, beyond the previous steps, other actions may be necessary.


#####################################################################

### Built With

* [NodeJs] - *development* (https://nodejs.org/)
* [Firebase] - *storing logs, snapshots and the protected application* (https://firebase.google.com/)
* [Socket.IO] - *communication between frontend and backend* (https://socket.io/)


### Authors

* **Diogo Vaz** - development - [Github](https://github.com/diogolvaz)


### License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details


### Acknowledgments

* **[Dr. David Matos](https://github.com/davidmatos)** - CoAuthor of the article
* **[Prof. Miguel Pardal](https://github.com/miguelpardal)** - CoAuthor of the article
* **[Prof. Miguel Correia](https://github.com/mpcorreia)** - CoAuthor of the article


