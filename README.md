# Signal Navigator

An Android application that helps users find and navigate to locations with better mobile signal strength based on historical data and recommendations.

## Features

- **Real-time Signal Strength Monitoring**: View your current location and mobile signal strength in dBm
- **Location Recommendations**: Get recommendations for locations with stronger signal strength for your mobile carrier
- **Interactive Map**: View recommended locations with color-coded markers based on signal strength quality
- **Turn-by-turn Navigation**: Get walking directions to recommended locations
- **Chatbot Interface**: Natural language interface to ask for recommendations and get signal strength information
- **Signal Comparison**: Compare signal strength between different locations and carriers
- **Location History**: Store and retrieve historical signal strength data for locations you've visited

## Screenshots

<img src="Img 1.png" alt="Home Screen" width="300">
<img src="Img 2.png" alt="Signal Map" width="300">
<img src="Img 3.png" alt="Home Screen" width="300">
<img src="Img 4.png" alt="Signal Map" width="300">
<img src="Img 5.png" alt="Home Screen" width="300">

## Requirements

- Android 6.0 (Marshmallow) or higher
- Google Maps API key
- Location permissions
- Phone state permissions

## Installation

1. Clone this repository
2. Add your Google Maps API key in your `AndroidManifest.xml`:
   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="YOUR_API_KEY_HERE" />
   ```
3. Build and run the application on your device

## Usage

### Basic Usage

1. Launch the app to see your current location and signal strength
2. Tap the chat button in the bottom right to open the chatbot interface
3. Ask for recommendations like "Where can I find better Jio signal?" or "Best Airtel spots near me"
4. Tap on a recommendation to view it on the map and get directions

### Chatbot Commands

The chatbot understands natural language queries such as:
- "Where's the best Jio signal around here?"
- "Compare Airtel and Vodafone in this area"
- "Show me places with good BSNL reception"
- "Get me directions to better signal"

### Map Features

- **Color-coded Markers**: 
  - Green: Good signal (≥ -80 dBm)
  - Yellow: Fair signal (≥ -95 dBm)
  - Red: Poor signal (< -95 dBm)
- **Navigation**: Tap a recommended location to see walking directions

## How It Works

The app collects and stores signal strength data tied to locations. When you request recommendations, the system:

1. Checks your current location and mobile carrier
2. Queries the server with this information
3. Returns locations with stronger signal for your carrier
4. Displays these on the map with signal strength details
5. Provides navigation assistance to these locations

## Architecture

The app follows MVVM architecture with Room database for local storage and Retrofit for API communication.

Key components:
- `MainActivity`: Main UI and map handling
- `ChatBotDialog`: Dialog interface for natural language interaction
- `ApiService`: Handles API calls to the backend service
- `LocationSignalDatabase`: Local database for storing signal history
- `RetrofitClient`: Network client for API communication

## Backend Service

The app communicates with a backend service hosted at `https://signal-chatbot-api-2.onrender.com` which provides:
- Chatbot natural language processing
- Signal strength prediction and recommendations
- Carrier comparison data

## Permissions

The app requires the following permissions:
- `ACCESS_FINE_LOCATION`: To get your precise location
- `READ_PHONE_STATE`: To access mobile signal information

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

[Insert your license here]

## Acknowledgments

- Google Maps API for mapping and navigation
- Retrofit library for API communication
- Room persistence library for local database storage
