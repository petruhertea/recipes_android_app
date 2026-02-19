# CookCraft — Android App

An Android recipe suggestion app. Users add the ingredients they have on hand and CookCraft suggests matching recipes fetched from the backend API, with offline support via local Room caching.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 34 (Android 14) |
| Architecture | MVVM (ViewModel + LiveData + Repository) |
| Networking | Retrofit 2 + Gson |
| Local DB | Room |
| Image Loading | Glide |
| Navigation | Jetpack Navigation Component |
| Ads | Google AdMob (banner + interstitial) |
| Build | Gradle 8.4 |

---

## Project Structure

```
app/src/main/java/com/cookcraft/
├── activity/
│   └── MainActivity.java              # Single activity, hosts ViewPager2 + AdMob
├── adapter/
│   └── ViewPagerAdapter.java          # 2-tab pager (Ingredients / Recipes)
├── fragment/
│   ├── AvailableIngredientsFragment.java  # Lists stored ingredients, swipe to edit/delete
│   ├── AddIngredientFragment.java         # Add or edit an ingredient
│   ├── RecipesFragment.java               # Recipe list with pull-to-refresh
│   └── RecipeDetailsFragment.java         # Recipe detail + beverage suggestions
├── model/
│   ├── AvailableIngredient.java       # Room entity: user's ingredient
│   ├── RecipeDetails.java             # API/display model for a recipe
│   ├── BeverageDetails.java           # API/display model for a beverage
│   ├── CachedRecipe.java              # Room entity: cached recipe
│   ├── CachedBeverage.java            # Room entity: cached beverage
│   └── IngredientDetails.java         # DTO: weight + unit for API requests
├── mvvm/
│   ├── IngredientViewModel.java       # ViewModel for ingredient CRUD
│   ├── IngredientRepository.java      # Async Room operations for ingredients
│   ├── RecipeViewModel.java           # ViewModel for recipe/beverage data
│   └── RecipeRepository.java          # Offline-first data layer (API + Room)
├── recyclerview/
│   ├── AvailableIngredientAdapter.java # DiffUtil-based ingredient list adapter
│   ├── RecipeRecyclerAdapter.java      # Recipe card list adapter
│   └── BeverageRecyclerAdapter.java    # Horizontal beverage card adapter
├── retrofit/
│   ├── RecipesApi.java                # Retrofit interface (GET/POST endpoints)
│   ├── RetrofitClient.java            # Singleton Retrofit + OkHttp with HTTP cache
│   ├── NetworkCheck.java              # Connectivity check utility
│   └── *Callback.java                 # Callback interfaces (legacy, mostly unused)
├── room/
│   ├── CookCraftDatabase.java         # Room database (v1, 3 entities)
│   ├── IngredientDAO.java             # CRUD for available_ingredients
│   ├── RecipeDAO.java                 # CRUD + expiry for cached_recipes
│   └── BeverageDAO.java               # CRUD + expiry for cached_beverages
└── util/
    └── NetworkStatusHelper.java       # LiveData<Boolean> network status monitor
```

---

## Features

### Ingredients Tab
- Add ingredients with a name, quantity, and unit of measure (pcs / g / kg / ml / l).
- Swipe **right** to delete an ingredient.
- Swipe **left** to edit an ingredient.
- Delete all ingredients at once via the trash FAB.

### Recipes Tab
- If no ingredients are stored, all recipes are displayed.
- If ingredients are stored, only recipes matching those ingredients are shown (API-filtered when online, locally filtered when offline).
- Pull down to force-refresh from the network.
- Tap a recipe card to open the full detail view.
- An offline banner appears when the device has no network connection.

### Recipe Details
- Full recipe information: title, image, description, servings, prep/cook/total time, ingredients list, and step-by-step instructions.
- Horizontal scrollable list of recommended beverage pairings with images.
- An interstitial ad is shown when this screen resumes.

---

## Offline Support

The app uses an **offline-first** strategy:

1. On launch, cached data from Room is shown immediately.
2. If the device is online, fresh data is fetched from the API and shown, then written to the Room cache.
3. If the device is offline, Room data is served directly.
4. The OkHttp client also maintains an HTTP-level cache (10 MB) for 5 minutes online / up to 7 days stale offline.
5. Cached records expire after **24 hours** and are cleaned up on app start (`cleanExpiredCache()`).

---

## Navigation

The app uses two independent Jetpack Navigation graphs, one per tab:

| Graph | Start Destination | Destinations |
|---|---|---|
| `nav_graph_ingredients` | `AvailableIngredientsFragment` | → `AddIngredientFragment` |
| `nav_graph_recipes` | `RecipesFragment` | → `RecipeDetailsFragment` |

Safe Args is used for type-safe argument passing between fragments.

---

## Setup & Configuration

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17+
- A running instance of the [CookCraft API backend](../README-backend.md)

### Backend URL

The base URL is hardcoded in `RetrofitClient.java`:

```java
private static final String BASE_URL = "http://10.0.2.2:8080/";
```

`10.0.2.2` is the Android emulator's alias for `localhost` on the host machine. For a physical device or a remote server, replace this with the actual IP/hostname.

> **Note:** `android:usesCleartextTraffic="true"` is set in the manifest to allow HTTP. For production, use HTTPS and remove this flag.

### AdMob

Ad unit IDs are stored in `res/values-ro/strings.xml` (and `res/values/strings.xml`):

```xml
<string name="admob_id">ca-app-pub-...</string>
<string name="banner_ad_id">ca-app-pub-...</string>
<string name="interstitial_ad_id">ca-app-pub-...</string>
```

Replace these with your own AdMob IDs before publishing.

---

## Building & Running

```bash
# Debug build via Gradle wrapper
./gradlew assembleDebug

# Install on connected device / emulator
./gradlew installDebug
```

Or open the project in Android Studio and run it directly.

---

## Room Database

Database name: `cookcraft_database` — version 1, `fallbackToDestructiveMigration` enabled.

| Table | Entity | Purpose |
|---|---|---|
| `available_ingredients` | `AvailableIngredient` | User's pantry |
| `cached_recipes` | `CachedRecipe` | Offline recipe cache |
| `cached_beverages` | `CachedBeverage` | Offline beverage cache |

---

## Localization

The app ships with two string resource sets:

| Folder | Language |
|---|---|
| `res/values/` | English (default) |
| `res/values-ro/` | Romanian |

---

## Responsive Layouts

Multiple layout variants are provided for different screen configurations:

| Qualifier | Usage |
|---|---|
| `layout/` | Default (phones, portrait) |
| `layout-land/` | Landscape orientation |
| `layout-sw600dp/` | Tablets (600dp+ smallest width) |

---

## Permissions

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```
