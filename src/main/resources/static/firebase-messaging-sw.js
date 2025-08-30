// Firebase Service Worker
importScripts('https://www.gstatic.com/firebasejs/9.23.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.23.0/firebase-messaging-compat.js');

// Firebase 설정 (google-services.json에서 추출)
firebase.initializeApp({
    apiKey: "AIzaSyC-mtXiQdWICXo9OhKwVx01qxXqbAoCAj8",
    authDomain: "hackerton-fcm.firebaseapp.com",
    projectId: "hackerton-fcm",
    storageBucket: "hackerton-fcm.firebasestorage.app", 
    messagingSenderId: "67081294208",
    appId: "1:67081294208:android:01b8489bb9f2008d6f7165"
});

// Firebase Messaging 초기화
const messaging = firebase.messaging();

// 백그라운드 메시지 처리
messaging.onBackgroundMessage((payload) => {
    console.log('백그라운드 메시지 수신:', payload);
    
    const notificationTitle = payload.notification?.title || '알림';
    const notificationOptions = {
        body: payload.notification?.body || '내용 없음',
        icon: '/favicon.ico',
        badge: '/favicon.ico',
        data: payload.data
    };
    
    self.registration.showNotification(notificationTitle, notificationOptions);
});