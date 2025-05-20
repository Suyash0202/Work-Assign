import com.google.auth.oauth2.GoogleCredentials
import java.io.FileInputStream

object FirebaseAuthHelper {
    private const val SCOPE = "https://www.googleapis.com/auth/firebase.messaging"

    fun getAccessToken(): String {
        val credentials = GoogleCredentials
            .fromStream(FileInputStream("C:\\Users\\Suyash\\AndroidStudioProjects\\WorkAssign\\gradle\\job-assign.json"))
            .createScoped(listOf(SCOPE))

        credentials.refreshIfExpired()
        return credentials.accessToken.tokenValue
    }
}
