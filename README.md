# ChatApp

#### ChatApp is an Android application which allows users to communicate with each other via both text messages and images. In the welcome screen, users can sign up using either an email address or a Gmail account. The idea behind the development of this project was to implement some of the most common Firebase features, such as Firebase Realtime Database, Firebase Storage and Firebase Authentication.

#### ChatApp utilizes the library called FirebaseUI for Android to handle the UI flow for authenticating with Firebase while implementing the best practices for sign-in and following the brand guidelines for each of the providers.

</br>

<img src="https://firebasestorage.googleapis.com/v0/b/inventoryapp-c8633.appspot.com/o/ChatApp%2F1.png?alt=media&token=1fc0d834-f411-45f2-ae52-00845fcbe596" width="420" height="692" style="margin:4px"> <img/><img src="https://firebasestorage.googleapis.com/v0/b/inventoryapp-c8633.appspot.com/o/ChatApp%2F2.png?alt=media&token=e89d95d4-1a41-4f46-9c42-2d5fd2b58c28" width="420" height="692" style="margin:4px">
<img src="https://firebasestorage.googleapis.com/v0/b/inventoryapp-c8633.appspot.com/o/ChatApp%2F3.png?alt=media&token=54f04df2-b0f7-4026-a944-7cddc5df426c" width="420" height="692" style="margin:4px"> <img/><img
src="https://firebasestorage.googleapis.com/v0/b/inventoryapp-c8633.appspot.com/o/ChatApp%2F4.png?alt=media&token=1935a6f0-c151-4733-a17a-e58f14e86bbe" width="420" height="692" style="margin:4px">
<img src="https://firebasestorage.googleapis.com/v0/b/inventoryapp-c8633.appspot.com/o/ChatApp%2F5.png?alt=media&token=21cf6482-bb43-47b6-978c-06f3cf953d07" width="420" height="692" style="margin:4px"> <img/><img
src="https://firebasestorage.googleapis.com/v0/b/inventoryapp-c8633.appspot.com/o/ChatApp%2F6.png?alt=media&token=44573169-28de-47fa-99a2-9b54b6c820e3" width="420" height="692" style="margin:4px">

## Getting Started

#### These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

## Prerequisites

#### ChatApp was developed using Android Studio IDE so you must install it on your computer before proceeding:

###### https://developer.android.com/studio/

## Next Steps

#### You can proceed to clone the project to your local machine, but DO NOT enter Android Studio yet. First, you need to set up your Firebase project as indicated in the next paragraph.

#### ChatApp requires Firebase Realtime Database and Firebase Storage for uploading text messages and images, and Firebase Authentication for security reasons and user identification. Therefore, in order to use ChatApp, you need to set up a project in your Firebase console and then add Firebase to your Android app by clicking on the corresponding button in the Project Overview section. This last part involves that you include the required data of your local machine such as the project package name and the SHA-1 fingerprint certificate. For further information, check this link:

###### https://firebase.google.com/docs/android/setup

#### Once the project ready, you must add Firebase to your Android app. Remember to download the **_google-services.json_** file and move it to the app directory (into the app module). The Firebase platform will ask you to run the app so it can confirm a successful communication. Therefore, open Android Studio, build the project and run it but DO NOT try to sign up in the app yet.

#### If the communication is successful, uninstall the app and go to the Authentication section in the Firebase console to enable email and Gmail sign-in methods.

#### Next, create a Realtime Database using the Database section in the Firebase console and set these security rules:

```
{
 "rules": {
   "messages": {
     // only authenticated users can read and write the messages node
     ".read": "auth != null",
     ".write": "auth != null",
     "$id": {
       // the read and write rules cascade to the individual messages
       // messages should have a 'name' and 'text' key or a 'name' and 'photoUrl' key
       ".validate": "newData.hasChildren(['name', 'text']) && !newData.hasChildren(['photoUrl']) || newData.hasChildren(['name', 'photoUrl']) && !newData.hasChildren(['text'])"
     }
   }
 }
}
```

#### Then, go to the Storage section in the Firebase console and set it up with the default rules.

#### Finally, create a folder in your local machine and set up Cloud Functions. Copy the code shown below in the index.js file and deploy it to Firebase. For detailed information about Cloud Functions, check this link:

###### https://firebase.google.com/docs/functions/

#### Index.js:

```
var functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

// replaces keywords with emoji in the "text" key of messages
// pushed to /messages
exports.emojify =
    functions.database.ref('/messages/{pushId}/text')
    .onWrite((change, context) => {
        // Database write events include new, modified, or deleted
        // database nodes. All three types of events at the specific
        // database path triggers this cloud function.
        // For this function, we only want to emojify new database nodes,
        // so we'll first check to exit out of the function early if
        // this isn't a new message.

        // !change.after.val() is a deleted event and in this case change.after.val() is null
        // change.before.val() is a modified event and in this case change.before.val() is different from null
        if (!change.after.val() || change.before.val()) {
            console.log("not a new write event");
            return null;
        }

        // Now we begin the emoji transformation
        console.log("emojifying!");

        // Get the value from the 'text' key of the message
        const originalText = change.after.val();
        const emojifiedText = emojifyText(originalText);

        // Return a JavaScript Promise to update the database node
        return change.after.ref.set(emojifiedText);
    });

// Returns text with keywords replaced by emoji
// Replacing with the regular expression /.../ig does a case-insensitive
// search (i flag) for all occurrences (g flag) in the string
function emojifyText(text) {
    var emojifiedText = text;
    emojifiedText = emojifiedText.replace(/\blol\b/ig, "😂");
    emojifiedText = emojifiedText.replace(/\bcat\b/ig, "😸");
    return emojifiedText;
}
```

#### Proceed to reinstall the app and start using it on your Android device.

## Compatibility

#### Minimum Android SDK: ChatApp requires a minimum API level of 15.
#### Compile Android SDK: ChatApp requires you to compile against API 27 or later.

## Getting Help

#### To report a problem or request features, open a new issue on Github. For questions, suggestions, or anything else, email to:

###### arturo_lpc@hotmail.com

## Author

#### Daniel Bedoya - @engspa12 on GitHub

## License

#### See the LICENSE file for details.
