'use strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);


/*
 * 'OnWrite' works as 'addValueEventListener' for android. It will fire the function
 * everytime there is some item added, removed or changed from the provided 'database.ref'
 * 'sendNotification' is the name of the function, which can be changed according to
 * your requirement
 */

exports.sendNotification = functions.database.ref('/Notifications/{user_id}/{notification_id}').onWrite((change, context) => {


  /*
   * You can store values as variables from the 'database.ref'
   * Just like here, I've done for 'user_id' and 'notification'
   */

  const user_id = context.params.user_id;
  const notification_id = context.params.notification_id;

  console.log('We have a notification to send to : ', user_id);

  if (!change.after.val()) {
  	
  	return console.log("A notification has been deleted from database : " , notification_id);

  };

  const fromUser = admin.database().ref(`/Notifications/${user_id}/${notification_id}`).once('value');
  return fromUser.then(fromUserResult => {

    const fromUserId = fromUserResult.val().from;

    console.log("You have new notification from : " , fromUserId);

    const userQuery = admin.database().ref(`Users/${fromUserId}/name`).once('value');
    const deviceToken = admin.database().ref(`/Users/${user_id}/deviceToken`).once('value');

    return Promise.all([userQuery, deviceToken]).then(result => {
      
      const userName = result[0].val();
      const token_id = result[1].val();

      const payload = {
            notification: {
              title: "Friend Request",
              body: `${userName} has sent you a request`,
              icon: "default",
              click_action: "FRIEND_REQUEST_NOTIFICATION_TARGET"
            },
            data: {
              from_user_id: fromUserId
            }
        };

        return admin.messaging().sendToDevice(token_id, payload).then(response =>{
      
          console.log("This was the notification feature");
        
        });


    });

    
  });


return 0;

});
