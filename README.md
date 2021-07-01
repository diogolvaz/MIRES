**MIRES** (**M**obile Applications **I**ntrusion **RE**covery **S**ervice) is an intrusion recovery service for mobile applications that use Backend-as-a-Service, e.g., Google's [Firebase](https://firebase.google.com/). The MIRES recovery model is based on a two-phase process that aims to reconstruct the corrupted data concurrently to users’ interaction with the backend, by
restoring the integrity of the systems’ state with a focus on maintaining the availability of the mobile application system. Besides the main intrusion recovery mechanism, MIRES also provides a user
recovery mechanism that allows the application users to recover from mistakes.

![alt text](https://github.com/inesc-id/MIRESPrototype/blob/master/MIRES%20service/MIRES_architecture.png?raw=true)

For more information regarding MIRES please visit https://www.gsd.inesc-id.pt/~mpc/pubs/MIRES__Diogo_Vaz_.pdf


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
5. Get the service account key from MIRES Project and save the file in ```MIRES service/Configuration``` with the name ```MIRESAccountKey``` (https://stackoverflow.com/questions/40799258/where-can-i-get-serviceaccountcredentials-json-for-firebase-admin);
6. Go to Firebase and create another Firebase project (this will be the Application container);
7. Get the service account key from the Application project and save the file in ```MIRES service/Configuration``` with the name ```APPAccountKey```;
8. Go to file ```/MIRES service/Configuration/initializeAdmins.js``` and update the APPadmin storageBucker property with the url of the Application Firebase storage;
9. Finally you can run ```npm run start-admin-console``` to initiate the console and access the interface using the url ```http://localhost:4000``` (you can also run ```npm run start-user-recovery-module``` to start the Users Recovery module that will run on port 5000. However, this module does not have an interface);


### Start Mobile Application

1. Go to the Application Firebase project and create the Firestore database (europe-west2);
2. Add the security rules on file ```/Application/SecurityRules/SecurityRules.txt``` to the Firestore Security Rules;
3. Enable Email/Password authentication model on the Application Firebase project;
4. Add the android application (```/Mobile Application/Hify```) to the Application Firebase project;
5. Copy file ```/MIRES service/Configuration/MIRESAccountKey.json``` to ```/Mobile Application/Hify/functions```;
6. Go to file ```/Mobile Application/Hify/.firebaserc``` and change the default property with the Firebase Application project Id;
7. Change the Application Firebase project pricing plan to Blaze (pay-as-you-go);
7. Got to folder ```/Mobile Application/Hify``` and run ```firebase deploy --only functions``` to deploy the cloud functions;
8. Now, you can start recovering the application! In the begginning, when recovering an action, some errors will appear "The query requires an index". Just copy the provided url and create the index on both;

Feel free to contact me if you have any question about the project! 


### Executions supported

The configuration of the application allows the following actions:

1. Administrator Recovery:
- Recover the creation of users;
- Recover login actions;
- Recover the creation of text and image posts;
- Recover the deletion of (only) text posts;

2. User Recovery:
- Recover the creation of text and image posts;
- Recover the deletion of (only) text posts;

3. Snapshot Process:
- Applied to the Users collection, where logins create new version of the documents;

4. Multi-service Recovery:
- Recover the creation of users;
- Recover the creeation of image posts;


**FINAL NOTES:** 
1. Firebase is constantly updating, meaning that besides the previous steps, other actions may be necessary.


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


