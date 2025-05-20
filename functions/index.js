const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendNotification = functions.https.onRequest(async (req, res) => {
  if (req.method !== "POST") {
    return res.status(405).send("Method Not Allowed");
  }

  const { token, title, body, data } = req.body;

  console.log("📨 Received payload:", JSON.stringify(req.body, null, 2));

  if (!token || !title || !body) {
    console.warn("⚠️ Missing required fields:", { token, title, body });
    return res.status(400).send("Missing token, title, or body");
  }

  const message = {
    notification: { title, body },
    token,
    ...(data ? { data } : {}) // include `data` only if it exists
  };

  try {
    const response = await admin.messaging().send(message);
    console.log("✅ Notification sent. Response:", response);
    return res.status(200).send("Notification sent successfully: " + response);
  } catch (error) {
    console.error("❌ Error sending notification:", error);
    return res.status(500).send("Failed to send notification: " + error.message);
  }
});
