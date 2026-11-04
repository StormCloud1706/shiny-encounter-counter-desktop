# Pokémon Shiny Encounter Counter

*A little patience, a little luck, and probably a questionable number of encounters.*

A Pokémon shiny-hunting companion built with Java and JavaFX.

Whether you're 50 encounters in or questioning your life choices at 5,000, this little app is here to keep track of the journey. Search for your Pokémon, admire its shiny sprite and keep that counter ticking.

This is my first desktop application and the beginning of my personal development portfolio. What started as a small project inspired by my love of Pokémon has become an opportunity to explore software development beyond my day-to-day work in Quality Engineering.

## ✨ See it in action

![Demo of searching for a Pokémon, viewing its shiny sprite and counting encounters.](docs/images/shiny-counter-demo.gif)

## 🕹️ Features

- **Pokémon search:** Browse 1,025 Pokémon across generations 1–9, including Hisui and Gen 9 DLC additions.
- **Generation filters:** Narrow down the Pokémon list by generation.
- **Keyboard navigation:** Navigate search suggestions using arrow keys and Enter, or dismiss them with Escape.
- **Shiny previews:** Display shiny sprites in a lavender, peach and mint interface.
- **Encounter counter:** Increase, decrease or reset your count, with safeguards against negative values.
- **User feedback:** Loading, empty search results and unavailable sprites are handled with dedicated messages.

## 🛠️ Built with

| Technology | Purpose |
| --- | --- |
| Java 21 | Application logic |
| JavaFX & CSS | Desktop interface and styling |
| Gson | JSON parsing |
| PokéAPI | Pokémon data |
| Maven | Dependency management and builds |

## 🚀 Getting started

### Requirements

- JDK 21
- [Apache Maven](https://maven.apache.org/install.html)
- Internet connection for Pokémon data and sprites
- For VS Code: the [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)

### Running the application

1. Clone this repository.
2. Open the project folder containing `pom.xml` in VS Code.
3. Allow Maven to import the project and resolve dependencies.
4. Open [`Main.java`](src/main/java/Main.java).
5. Click **Run** above the `main` method.

Alternatively, run this command from the project folder containing `pom.xml`:

```bash
mvn javafx:run
```

To compile and package the project without launching it:

```bash
mvn package
```

[`Main.java`](src/main/java/Main.java) acts as the launcher, while the JavaFX application lives in
[`ShinyCounterApp.java`](src/main/java/ShinyCounterApp.java).

## Current limitations

- Counts are not saved between sessions. Changing the selected Pokémon also resets the count.
- The list covers National Pokédex entries, rather than every alternate or regional form.
- A shiny sprite preview does not mean that Pokémon is available to shiny-hunt in every game.
- Automated regression tests are planned but are not yet included in this repository.

## 🧠 What I've learned

Building this application has given me hands-on experience with:

- Integrating public APIs and working with JSON.
- Loading data in the background without blocking the UI.
- Handling user interactions, search and filtering.
- Managing application state and counter behaviour.
- Separating visual styling from application logic.
- Structuring a Java project with Maven.
- Considering edge cases, error handling and user experience.

My background is in Quality Engineering, so I'm naturally interested in how applications behave, not just how they look. Building something myself has been a great opportunity to explore the development side of the software lifecycle.

## 🔭 What's next?

This is a starting point, not the finished evolution of the app!

Some ideas I'm exploring:

- **Multiple hunts:** Track several Pokémon hunts at once.
- **Persistent progress:** Save encounter counts between sessions.
- **Automated testing:** Introduce regression tests for search, filtering and counter behaviour.
- **React version:** Reimagine the application for the web, with saved hunts and Playwright test coverage.
- **iOS version:** Eventually explore bringing the tracker to mobile using Swift and SwiftUI.

The idea is to develop the same concept across different platforms, learning new technologies and applying lessons from each implementation.

## 📁 Project structure

```text
.vscode/
docs/
    images/
        shiny-counter-demo.gif
src/
    main/
        java/
            Main.java
            ShinyCounterApp.java
        resources/
            style.css
pom.xml
README.md
```

Maven generates a `target/` folder when building the app. It contains compiled
files and packaging output, and is excluded from Git.

## Credits

Pokémon data is provided by [PokéAPI](https://pokeapi.co/), with sprites sourced from the [PokéAPI sprites repository](https://github.com/PokeAPI/sprites).

This is an unofficial fan-made project created for learning and personal development. It is not affiliated with or endorsed by Nintendo, Game Freak or The Pokémon Company. Pokémon names and imagery belong to their respective owners.

---

*Built with Java, a love of Pokémon, and an unreasonable amount of patience for shiny hunting!* 🌙
