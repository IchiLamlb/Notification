importScripts('https://www.gstatic.com/firebasejs/10.7.1/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.7.1/firebase-messaging-compat.js');

firebase.initializeApp({
    apiKey: "AIzaSyDFBMJ3jLDz38MMKTfmIUDmu0TNbx7wUo0",
    authDomain: "noti-8532b.firebaseapp.com",
    projectId: "noti-8532b",
    storageBucket: "noti-8532b.firebasestorage.app",
    messagingSenderId: "548079551425",
    appId: "1:548079551425:web:fe9bebbe0b8be1dd8b6540",
});

const messaging = firebase.messaging();