package plugins

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.server.application.*
import java.io.FileInputStream

fun Application.configureFirebaseAdmin() {
    if (FirebaseApp.getApps().isNotEmpty()) return

    val serviceAccountPath =
        System.getenv("FIREBASE_SERVICE_ACCOUNT_PATH") ?: "KEYS/serviceAccount.json"

    val options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(FileInputStream(serviceAccountPath)))
        .build()

    FirebaseApp.initializeApp(options)
}
