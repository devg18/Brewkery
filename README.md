# ☕ Brewkery — Coffee & Bakery Ordering App

A native Android prototype for ordering coffee and fresh bakes, built with **Kotlin** and **Jetpack Compose**.
All data comes from a single REST API (raw JSON hosted on GitHub).

  Demo video: "(https://drive.google.com/file/d/1CZA1647gL9qX3CG4JQWV0awq4Bzg6DJ-/view?usp=drive_link)"

  HomeScreen Image :-    https://drive.google.com/file/d/1mtJ6JfsymQ7tOiy_KI79TtJVS36juq33/view?usp=drive_link
  ItemDetails Image :-   https://drive.google.com/file/d/16HvZdu4XjBYODDVUO661hMSXxP7iROC-/view?usp=drive_link
  CART Image :-          https://drive.google.com/file/d/1IP4oSz7jo3RZoAcnJAicwtW6_dtLE2Ft/view?usp=drive_link
  OrderScreen Image :-   https://drive.google.com/file/d/1LpUc1CJsuta0tHeo3FuANvm7VPfPpBD6/view?usp=drive_link

## Features

| Screen | What it does |
|---|---|
| **Home / Menu** | Store banner, category chips (filter), item list with thumbnail, badge, title, rating and base price. Shimmer loading skeleton and an error state with Retry. Shows an active-order card after an order is placed. |
| **Item Detail** | Hero image with parallax, info, ingredients, and customizations (`sizes`, `milk_options`, `sugar_levels`) with live price recalculation. |
| **Cart** | Items with quantity controls and delete. Subtotal + $2.50 delivery + 8% tax. Empty-cart state. |
| **Order Status** | Place order → cart is cleared, a ticket ID is generated (e.g. `BRW-4821`) and the status shows `PREPARING` with a timeline. |

Extras: light + dark theme, animated price changes, bounce-on-press buttons, screen transitions.

## Tech stack

- **Kotlin**, **Jetpack Compose** (Material 3), Navigation Compose
- **Retrofit** + Gson + **Coroutines / StateFlow**
- **Coil** for image loading
- **MVVM** (ViewModel + StateFlow), simple manual DI
- **JUnit** unit tests

## API

- Full menu & store config: `https://raw.githubusercontent.com/VivekShah138/Brewkery/main/data.json`
- Single item: `https://raw.githubusercontent.com/VivekShah138/Brewkery/main/api/items/{id}.json`

## Project structure

```
app/src/main/java/com/dev/brewkery/
├── domain/   models + PriceCalculator (all money maths)
├── data/     Retrofit API, DTOs, mappers, repository
├── di/       ServiceLocator (Retrofit + repository)
├── ui/
│   ├── theme/    colours, typography, light/dark
│   ├── common/   shared components (buttons, shimmer, steppers)
│   ├── home/  detail/  cart/  order/
├── BrewkeryApp.kt   navigation
└── MainActivity.kt
```

### Design decisions

- **Nullable DTOs → clean domain models.** A missing field in the JSON can never crash the app.
- **Hidden price in sugar labels.** Some labels look like `"Light Wildflower Honey (+0.40)"`. The mapper pulls the `+0.40` out so the total is correct.
- **All price maths in one place** (`PriceCalculator`) so it is easy to test.
- **Tax is charged on the subtotal** (delivery fee is not taxed).
- **Detail screen opens instantly** from the already-loaded menu, then refreshes from the single-item endpoint.
- The API has no announcement field, so the banner text is built from the delivery time and fee in `data.json`.

## How to run

1. Install **Android Studio Ladybug (2024.2.1) or newer**.
2. `File ▸ Open` → select this project folder, wait for Gradle sync.
3. Choose an emulator or device (API 24+) and press **Run ▶**.

## Tests

In Android Studio: right-click `app/src/test` → **Run Tests**.

Covered: price calculation with options, subtotal + delivery + 8% tax, empty cart, cart-line merging key, sugar-label price parsing, and safe handling of missing JSON fields.

---

## 🤖 How I used AI

### Tool used

- **Claude (Anthropic)**, used in chat on claude.ai, acting as a senior Android developer.
- I gave it the task brief and the API links. It planned the architecture, wrote the Kotlin/Compose code, and wrote the unit tests. I ran, reviewed and tested the result in Android Studio.

### Prompts I sent

**Prompt — the brief**

```text
hii hope you are doing well 
Today i want you as a experienced android app developer with a experience of more than 20 years and can make a company selected prototype just in a time.
The prototype details are as:-

A native Android coffee & bakery ordering app in Kotlin powered by a single REST API we provide. The page above contains:

* The full interactive 4-screen prototype and UI specifications.
* The raw API endpoints (`data.json` for full menu & `api/items/{id}.json` for single items).
* Step-by-step implementation guide.

* Create a new Android project in Kotlin (Android Studio).
* Add Retrofit + coroutines and fetch the JSON from the API URL below.
* Screen 1 — Home / Menu: show the categories row and the items list (thumbnail, badge, title, rating, base price). Includes the store announcement banner (and active order tracking once placed).
* Screen 2 — Item Detail: tap an item → hero image, info, ingredients, and dynamic customizations (`sizes`, `milk_options`, `sugar_levels`) with price recalculation.
* Screen 3 — Cart: tap cart → items list with quantities, subtotal + $2.50 delivery + 8% tax calculation.
* Screen 4 — Order Status: place order → clear cart, generate ticket ID, and show `PREPARING` status.
* Load all images from their URLs with Coil or Glide.
* Show a loading state and an error message for the network call.
* Write at least one unit test.
These are restapi endpoints 
1. Full Menu & Store Config
GET 200
Returns shop metadata (fees, delivery estimates) and all 6 categorized menu items.
https://raw.githubusercontent.com/VivekShah138/Brewkery/main/data.json
2. Single Item Detail by ID
GET 200
Returns root item resource (e.g. api/items/1.json to api/items/6.json).
https://raw.githubusercontent.com/VivekShah138/Brewkery/main/api/items/{id}.json

so now you know what i want and let me tell you how i want 
i want it for android studio setup and in kotlin language as i am android app developer with kotlin language. Keep in mind that the UI of the prototype should be advanced and modern and user-friendly with proper animated and amazing looks of ui.
```

### One thing the AI got right

Before writing code, Claude opened the real `data.json` and read its structure. Because of that it noticed that some sugar options hide an extra price inside the text (`"Light Wildflower Honey (+0.40)"`). It added a small parser plus a unit test, so the cart total stays correct. It also made every field in the JSON models nullable, so one bad value cannot crash the app.

### One thing the AI got wrong, and how it was fixed

I did not hit any bug while running the finished app: the project built and ran on the first attempt. The mistakes below were caught while the code was being generated and reviewed, before I ran it:

- **Wrong import.** Claude first used the wrong package for Compose's `animateColorAsState`, which would have failed to compile. It found this when it re-checked the file and fixed the import before handing over the project.
- **Files not saved.** The first batch of source files failed to save because the folders were created with a shell shortcut that doesn't work in that terminal. Claude noticed the error, created the folders properly and rewrote the files.

To reduce the risk of more mistakes, Claude also compiled the non-UI code (models, price maths, mappers) and ran the unit tests with the Kotlin compiler. All 9 tests passed.

