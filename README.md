# LSA Interpreter

A Java application for interpreting Argentine Sign Language (LSA - Lengua de Señas Argentina) using computer vision and machine learning techniques.

## Features

- Real-time sign language detection and interpretation
- Face and gesture recognition
- Grammar processing for improved accuracy
- Sign language dictionary with video demonstrations
- History tracking of detected signs
- User-friendly interface with live video feed

## Requirements

- Java 17 or higher
- OpenCV 4.7.0
- JavaFX 17

## Building

1. Clone the repository:
```bash
git clone https://github.com/RamaAlmarza/lsa-interpreter.git
cd lsa-interpreter
```

2. Build with Maven:
```bash
mvn clean install
```

## Running

Run the application using Maven:
```bash
mvn javafx:run
```

## Project Structure

- `src/main/java/com/lsa/interpreter/`
  - `Main.java` - Application entry point
  - `ui/` - User interface components
    - `MainWindow.java` - Main application window
    - `DetectorUI.java` - Video feed and detection controls
    - `DictionaryUI.java` - Sign language dictionary interface
    - `HistorySidebar.java` - Detection history tracking
  - `logic/` - Core business logic
    - `FaceDetector.java` - Facial expression detection
    - `GestureDetector.java` - Hand gesture detection
    - `FusionAI.java` - AI fusion of detection results
    - `GrammarProcessor.java` - Grammar processing
    - `DictionaryManager.java` - Dictionary management
  - `util/` - Utility classes
    - `VideoUtils.java` - Video processing utilities
    - `ErrorLogger.java` - Error logging and management

## Configuration

- OpenCV cascade classifiers are located in `src/main/resources/haarcascades/`
- Sign language dictionary data is in `src/main/resources/dictionary/lsa_dictionary.json`
- UI styling is defined in `src/main/resources/styles/main.css`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
