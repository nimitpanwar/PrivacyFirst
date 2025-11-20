# Whitelist API Documentation for Android

## Base URL
```
http://YOUR_SERVER_IP:5001
```

## Authentication Flow

### 1. Login
Get a JWT token for authenticated requests.

**Endpoint:** `POST /api/login`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "admin",
  "password": "Byf8$G&F*G8vGEfuhfuhfEHU!89f2qfiHT88%ffyutf7^s"
}
```

**Success Response (200):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": "1h"
}
```

**Error Responses:**
- `400` - Missing username or password
- `401` - Invalid credentials

**Android Example (OkHttp):**
```kotlin
val client = OkHttpClient()
val mediaType = "application/json".toMediaType()
val body = """{"username":"admin","password":"Byf8$G&F*G8vGEfuhfuhfEHU!89f2qfiHT88%ffyutf7^s"}""".toRequestBody(mediaType)

val request = Request.Builder()
    .url("http://YOUR_SERVER_IP:5001/api/login")
    .post(body)
    .build()

client.newCall(request).execute().use { response ->
    if (response.isSuccessful) {
        val jsonObject = JSONObject(response.body!!.string())
        val token = jsonObject.getString("token")
        // Save token for future requests
    }
}
```

---

## Protected Endpoints
All endpoints below require authentication. Include the JWT token in the Authorization header:

```
Authorization: Bearer YOUR_TOKEN_HERE
```

---

### 2. Get Whitelist
Retrieve all whitelisted URLs.

**Endpoint:** `GET /api/whitelist`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
```

**Success Response (200):**
```json
{
  "urls": [
    "https://example.com",
    "https://another-site.com"
  ],
  "count": 2
}
```

**Error Responses:**
- `401` - No token provided or invalid token
- `500` - Server error

**Android Example (OkHttp):**
```kotlin
val request = Request.Builder()
    .url("http://YOUR_SERVER_IP:5001/api/whitelist")
    .addHeader("Authorization", "Bearer $token")
    .get()
    .build()

client.newCall(request).execute().use { response ->
    if (response.isSuccessful) {
        val jsonObject = JSONObject(response.body!!.string())
        val urls = jsonObject.getJSONArray("urls")
        // Process URLs
    }
}
```

---

### 3. Add URL to Whitelist
Add a new URL to the whitelist.

**Endpoint:** `POST /api/whitelist/add`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: application/json
```

**Request Body (Option 1 - JSON):**
```json
{
  "url": "https://newsite.com"
}
```

**Request Body (Option 2 - Query String):**
```
POST /api/whitelist/add?url=https://newsite.com
```

**Success Response (201):**
```json
{
  "message": "added",
  "doc": {
    "url": "https://newsite.com",
    "addedBy": "admin",
    "createdAt": "2025-11-20T10:30:00.000Z",
    "_id": "..."
  }
}
```

**Error Responses:**
- `400` - URL missing or invalid format
- `401` - No token or invalid token
- `409` - URL already whitelisted
- `500` - Server error

**Android Example (OkHttp - JSON Body):**
```kotlin
val mediaType = "application/json".toMediaType()
val body = """{"url":"https://newsite.com"}""".toRequestBody(mediaType)

val request = Request.Builder()
    .url("http://YOUR_SERVER_IP:5001/api/whitelist/add")
    .addHeader("Authorization", "Bearer $token")
    .post(body)
    .build()

client.newCall(request).execute().use { response ->
    if (response.isSuccessful) {
        val jsonObject = JSONObject(response.body!!.string())
        // URL added successfully
    }
}
```

**Android Example (Query String - simpler for some clients):**
```kotlin
val url = "https://newsite.com".encodeURLQueryComponent()
val request = Request.Builder()
    .url("http://YOUR_SERVER_IP:5001/api/whitelist/add?url=$url")
    .addHeader("Authorization", "Bearer $token")
    .post("".toRequestBody())
    .build()

client.newCall(request).execute()
```

---

### 4. Update URL in Whitelist
Update an existing URL in the whitelist.

**Endpoint:** `PUT /api/whitelist/update`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: application/json
```

**Request Body (Option 1 - JSON):**
```json
{
  "oldUrl": "https://oldsite.com",
  "newUrl": "https://updatedsite.com"
}
```

**Request Body (Option 2 - Query String):**
```
PUT /api/whitelist/update?oldUrl=https://oldsite.com&newUrl=https://updatedsite.com
```

**Success Response (200):**
```json
{
  "message": "updated",
  "doc": {
    "url": "https://updatedsite.com",
    "addedBy": "admin",
    "createdAt": "2025-11-20T10:30:00.000Z",
    "_id": "..."
  }
}
```

**Error Responses:**
- `400` - Missing oldUrl/newUrl or invalid format
- `401` - No token or invalid token
- `404` - oldUrl not found
- `500` - Server error

**Android Example (OkHttp):**
```kotlin
val mediaType = "application/json".toMediaType()
val body = """{"oldUrl":"https://oldsite.com","newUrl":"https://updatedsite.com"}""".toRequestBody(mediaType)

val request = Request.Builder()
    .url("http://YOUR_SERVER_IP:5001/api/whitelist/update")
    .addHeader("Authorization", "Bearer $token")
    .put(body)
    .build()

client.newCall(request).execute()
```

---

### 5. Delete URL from Whitelist
Remove a URL from the whitelist.

**Endpoint:** `DELETE /api/whitelist/delete`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: application/json
```

**Request Body (Option 1 - JSON):**
```json
{
  "url": "https://sitetoremove.com"
}
```

**Request Body (Option 2 - Query String):**
```
DELETE /api/whitelist/delete?url=https://sitetoremove.com
```

**Success Response (200):**
```json
{
  "message": "deleted",
  "doc": {
    "url": "https://sitetoremove.com",
    "addedBy": "admin",
    "createdAt": "2025-11-20T10:30:00.000Z",
    "_id": "..."
  }
}
```

**Error Responses:**
- `400` - URL missing
- `401` - No token or invalid token
- `404` - URL not found
- `500` - Server error

**Android Example (OkHttp - JSON Body):**
```kotlin
val mediaType = "application/json".toMediaType()
val body = """{"url":"https://sitetoremove.com"}""".toRequestBody(mediaType)

val request = Request.Builder()
    .url("http://YOUR_SERVER_IP:5001/api/whitelist/delete")
    .addHeader("Authorization", "Bearer $token")
    .delete(body)
    .build()

client.newCall(request).execute()
```

**Android Example (Query String):**
```kotlin
val url = "https://sitetoremove.com".encodeURLQueryComponent()
val request = Request.Builder()
    .url("http://YOUR_SERVER_IP:5001/api/whitelist/delete?url=$url")
    .addHeader("Authorization", "Bearer $token")
    .delete()
    .build()

client.newCall(request).execute()
```

---

## Complete Android Implementation Example

```kotlin
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlinx.coroutines.*

class WhitelistApiClient(private val baseUrl: String) {
    private val client = OkHttpClient()
    private val jsonMediaType = "application/json".toMediaType()
    private var authToken: String? = null

    suspend fun login(username: String, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val body = """{"username":"$username","password":"$password"}""".toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url("$baseUrl/api/login")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = JSONObject(response.body!!.string())
                    authToken = json.getString("token")
                    true
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getWhitelist(): List<String>? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/api/whitelist")
                .addHeader("Authorization", "Bearer $authToken")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = JSONObject(response.body!!.string())
                    val urlsArray = json.getJSONArray("urls")
                    List(urlsArray.length()) { i -> urlsArray.getString(i) }
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun addUrl(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val body = """{"url":"$url"}""".toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url("$baseUrl/api/whitelist/add")
                .addHeader("Authorization", "Bearer $authToken")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteUrl(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val body = """{"url":"$url"}""".toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url("$baseUrl/api/whitelist/delete")
                .addHeader("Authorization", "Bearer $authToken")
                .delete(body)
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateUrl(oldUrl: String, newUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val body = """{"oldUrl":"$oldUrl","newUrl":"$newUrl"}""".toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url("$baseUrl/api/whitelist/update")
                .addHeader("Authorization", "Bearer $authToken")
                .put(body)
                .build()

            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

// Usage example in Activity/Fragment:
class MainActivity : AppCompatActivity() {
    private val apiClient = WhitelistApiClient("http://192.168.1.100:5001")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            // Login first
            val loginSuccess = apiClient.login("admin", "Byf8\$G&F*G8vGEfuhfuhfEHU!89f2qfiHT88%ffyutf7^s")
            
            if (loginSuccess) {
                // Get whitelist
                val urls = apiClient.getWhitelist()
                urls?.forEach { url ->
                    Log.d("Whitelist", url)
                }
                
                // Add a URL
                apiClient.addUrl("https://example.com")
                
                // Update a URL
                apiClient.updateUrl("https://old.com", "https://new.com")
                
                // Delete a URL
                apiClient.deleteUrl("https://example.com")
            }
        }
    }
}
```

---

## Important Notes

### URL Validation
- URLs must start with `http://` or `https://`
- Maximum URL length: 2083 characters
- Maximum hostname length: 255 characters

### Token Management
- Tokens expire after 1 hour
- Store the token securely (Android KeyStore recommended)
- Re-login when you receive a 401 Unauthorized response

### Error Handling
Always check HTTP status codes:
- `200/201` - Success
- `400` - Bad request (check your parameters)
- `401` - Unauthorized (token missing/invalid/expired)
- `404` - Resource not found
- `409` - Conflict (URL already exists)
- `500` - Server error

### Network Security Config
Add to your `AndroidManifest.xml` if using HTTP (for development only):
```xml
<application
    android:usesCleartextTraffic="true"
    ...>
```

For production, always use HTTPS.

### Required Permissions
Add to your `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### Dependencies
Add to your `build.gradle`:
```gradle
dependencies {
    implementation 'com.squareup.okhttp3:okhttp:4.11.0'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
}
```

---

## Testing with cURL

Before implementing in Android, test the API with cURL:

```bash
# Login
curl -X POST http://YOUR_SERVER_IP:5001/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Byf8$G&F*G8vGEfuhfuhfEHU!89f2qfiHT88%ffyutf7^s"}'

# Save the token from response, then:

# Get whitelist
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://YOUR_SERVER_IP:5001/api/whitelist

# Add URL
curl -X POST -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"url":"https://example.com"}' \
  http://YOUR_SERVER_IP:5001/api/whitelist/add

# Delete URL
curl -X DELETE -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"url":"https://example.com"}' \
  http://YOUR_SERVER_IP:5001/api/whitelist/delete
```

---

## Troubleshooting

### "No token provided"
- Ensure you're including the `Authorization` header
- Check the format: `Bearer YOUR_TOKEN` (with space after "Bearer")

### "Invalid or expired token"
- Token has expired (1 hour lifetime)
- Call `/api/login` again to get a new token

### Connection refused / Timeout
- Check if server is running
- Verify the IP address and port
- Check firewall settings
- Ensure both devices are on the same network (for local testing)

### JSON parsing errors
- Ensure `Content-Type: application/json` header is set
- Verify JSON format is valid
- Check for special characters in strings (escape them properly)
