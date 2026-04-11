// Серверная часть на Node.js (Firebase Cloud Functions)
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

// Обработка нового заказа
exports.onNewOrder = functions.firestore
    .document('orders/{orderId}')
    .onCreate((snap, context) => {
        console.log('Новый заказ создан');
        return null;
    });

// HTTP эндпоинт для проверки работы сервера
exports.api = functions.https.onRequest((req, res) => {
    res.json({ status: 'Node.js сервер работает' });
});