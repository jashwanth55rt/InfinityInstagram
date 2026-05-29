# infinity — Instagram-style Android app (Java + Firebase)

A complete Android Studio project written in **Java** that demonstrates an Instagram-like
social media app: stories, photo feed, likes, follow/unfollow, profiles, search, and
notifications. UI is built with Material Components, RecyclerView, CardView, and Glide.
Backend is **Firebase Authentication + Realtime Database + Storage**.

> Videos are intentionally disabled — images only.

---

## 1. Project folder structure

```
InfinityInstagram/
├─ build.gradle                              # top-level Gradle (plugins)
├─ settings.gradle                           # module list + repositories
├─ gradle.properties
├─ gradle/wrapper/gradle-wrapper.properties  # Gradle 8.2
└─ app/
   ├─ build.gradle                           # app-module deps (AndroidX, Firebase, Glide…)
   ├─ proguard-rules.pro
   ├─ google-services.json                   # Firebase config (REPLACE — see step 5)
   └─ src/main/
      ├─ AndroidManifest.xml
      ├─ java/com/infinity/app/
      │  ├─ InfinityApp.java                 # Application class (enables RTDB persistence)
      │  ├─ activities/
      │  │  ├─ SplashActivity.java
      │  │  ├─ LoginActivity.java
      │  │  ├─ RegisterActivity.java
      │  │  ├─ MainActivity.java             # bottom-nav host
      │  │  ├─ UploadPostActivity.java
      │  │  ├─ UploadStoryActivity.java
      │  │  ├─ EditProfileActivity.java
      │  │  └─ UserProfileActivity.java      # other user's profile + follow button
      │  ├─ fragments/
      │  │  ├─ HomeFragment.java             # stories rail + feed
      │  │  ├─ SearchFragment.java
      │  │  ├─ NotificationsFragment.java
      │  │  └─ ProfileFragment.java          # current user's profile
      │  ├─ adapters/
      │  │  ├─ PostAdapter.java
      │  │  ├─ StoryAdapter.java
      │  │  ├─ UserAdapter.java
      │  │  ├─ NotificationAdapter.java
      │  │  └─ ProfilePostAdapter.java       # 3-col grid
      │  ├─ models/
      │  │  ├─ User.java
      │  │  ├─ Post.java
      │  │  ├─ Story.java
      │  │  └─ Notification.java
      │  └─ utils/
      │     ├─ FirebaseHelper.java           # Auth + RTDB + Storage wrapper
      │     ├─ ImageUtils.java               # in-memory image compression
      │     └─ Constants.java                # DB paths, storage folders, intent extras
      └─ res/
         ├─ layout/                          # all activity_/fragment_/item_ XML
         ├─ drawable/                        # vector icons + backgrounds (no PNGs)
         ├─ menu/bottom_nav_menu.xml
         ├─ anim/                            # fade_in.xml, scale_pop.xml
         ├─ mipmap-anydpi-v26/               # adaptive launcher icons
         ├─ values/{colors,strings,styles,themes}.xml
         └─ values-night/themes.xml          # dark theme
```

## 2. Tech stack

- Java 17, AGP 8.1.4, Gradle 8.2
- minSdk 21, targetSdk/compileSdk 34
- AndroidX, Material Components, ConstraintLayout, RecyclerView, CardView,
  SwipeRefreshLayout
- Firebase BoM 32.7.0 — Auth, Realtime Database, Storage
- Glide 4.16 (image loading + on-disk cache)
- de.hdodenhof:circleimageview:3.1.0 (rounded avatars)

## 3. Features implemented

| # | Feature | Where |
| - | - | - |
| 1 | Splash screen | `SplashActivity` + `activity_splash.xml` |
| 2 | Login | `LoginActivity` |
| 3 | Register | `RegisterActivity` |
| 4 | Home feed | `HomeFragment` + `PostAdapter` |
| 5 | Stories rail | `HomeFragment` + `StoryAdapter` |
| 6 | Upload post | `UploadPostActivity` |
| 7 | Profile | `ProfileFragment` |
| 8 | Bottom navigation | `MainActivity` + `bottom_nav_menu.xml` |
| 9 | Like button | `PostAdapter#bindLikes` |
| 10 | Follow / unfollow | `UserProfileActivity#toggleFollow` |
| 11 | Edit profile | `EditProfileActivity` |
| 12 | Search users | `SearchFragment` + `UserAdapter` |
| 13 | Image upload from gallery | `ActivityResultContracts.GetContent` in upload screens |
| 14 | Firebase Storage | `FirebaseHelper.storageRef(...)` |
| 15 | Post captions | `etCaption` in `activity_upload_post.xml` |
| 16 | Display user posts | grid in `ProfileFragment` / `UserProfileActivity` |
| 17 | Story upload | `UploadStoryActivity` |
| 18 | Firebase session login | `SplashActivity#route` checks `FirebaseHelper.currentUser()` |
| 19 | Logout | `ProfileFragment#btnLogout` |
| 20 | Notifications layout | `NotificationsFragment` + `NotificationAdapter` |
| 21 | UI animations | `anim/scale_pop.xml`, `anim/fade_in.xml`, gradient story rings |

## 4. Realtime Database structure

```
/users/{uid} = {
  uid, username, fullName, email, bio, avatarUrl
}

/posts/{postId} = {
  postId, authorId, imageUrl, caption, timestamp, likeCount
  /likes/{uid} = true
}

/stories/{storyId} = {
  storyId, authorId, imageUrl, timestamp
}

/follows/{followerUid}/{followingUid}     = true
/followers/{followingUid}/{followerUid}   = true

/notifications/{recipientUid}/{notifId} = {
  notifId, fromUid, type ("like"|"follow"|"comment"), postId?, timestamp
}
```

Storage layout:
```
/avatars/{uid}.jpg
/posts/{postId}.jpg
/stories/{storyId}.jpg
```

## 5. Firebase setup (required before running)

The provided `google-services.json` is built from your web Firebase config
(project `premium-eae4a`). To run on a **real Android device**, the Firebase project
must contain an **Android app registered with package name `com.infinity.app`**:

1. Go to <https://console.firebase.google.com/> and open project **premium-eae4a**.
2. *Project settings → General → Your apps → Add app → Android*.
   - Package name: `com.infinity.app`
   - App nickname: `infinity`
   - Skip the SHA-1 step unless you plan to add Google Sign-In.
3. Download the generated **`google-services.json`** and place it at
   `app/google-services.json` (overwrite the placeholder in this repo).
4. In the Firebase console, enable the services this app uses:
   - **Authentication → Sign-in method → Email/Password → Enable**
   - **Realtime Database → Create database** (URL must match
     `https://premium-eae4a-default-rtdb.firebaseio.com`).
     For local development, you can start in *test mode* rules:
     ```json
     {
       "rules": { ".read": "auth != null", ".write": "auth != null" }
     }
     ```
   - **Storage → Get started** with default rules:
     ```
     rules_version = '2';
     service firebase.storage {
       match /b/{bucket}/o {
         match /{allPaths=**} {
           allow read, write: if request.auth != null;
         }
       }
     }
     ```

> The `FirebaseHelper.database()` call already pins the RTDB URL to
> `https://premium-eae4a-default-rtdb.firebaseio.com`, so even if a different
> `google-services.json` is dropped in, RTDB still talks to the right instance.

## 6. Run instructions

1. Open Android Studio (Hedgehog or newer) → **Open** → select the
   `InfinityInstagram` folder.
2. Let Gradle sync. AGP 8.1 needs JDK 17 (Android Studio bundles it).
3. Replace `app/google-services.json` per step 5 above.
4. Pick an emulator (API 24+ recommended) or a connected device.
5. Run the **app** configuration. The first launch shows the splash, then the
   login screen. Create an account and you're in.

### First-time smoke test
- Register → land on Home (empty).
- Tap the camera icon (top-right of Home) → upload a story.
- Tap the center "+" tab → upload a post with a caption.
- Tap the heart on the post — like count updates.
- Use Search to find another test account → tap → Follow.
- Other account's Notifications tab now shows a new entry.

## 7. Notes & tradeoffs

- Reads use `addListenerForSingleValueEvent` (one-shot fetch). For a production
  app, switch to `ChildEventListener` / paging for live updates and large feeds.
- Search loads `/users` once and filters client-side. For >1k users, switch to
  a server-side query on `username`.
- Likes are stored as `/posts/{postId}/likes/{uid}=true`; counts are computed
  by `getChildrenCount()` on read. For very hot posts you'd add a denormalized
  counter and use Cloud Functions / transactions.
- Stories are filtered to the last 24h client-side. A scheduled Cloud Function
  could clean up expired stories from Storage.
- Image compression caps the longest side at 1080px (posts / stories) and
  512px (avatars), at 80% JPEG quality.
