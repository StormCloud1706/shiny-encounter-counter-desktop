import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.stage.Popup;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ShinyCounterApp extends Application {

    private final ObservableList<Pokemon> allPokemon = FXCollections.observableArrayList();
    private final ObservableList<Pokemon> filteredPokemon = FXCollections.observableArrayList();

    private ComboBox<Pokemon> pokemonComboBox;
    private TextField searchField;
    private final Popup searchSuggestions = new Popup();
    private final ListView<Pokemon> suggestionList = new ListView<>();
    private ComboBox<String> genComboBox;
    private ImageView shinyImageView;

    private Label counterLabel;
    private Label statusLabel;
    private Label pokemonNameLabel;
    private Label spriteStatusLabel;
    private HBox counterControls;
    private boolean updatingPokemonList;
    private int shinyCounter = 0;
    private static final int POKEMON_COUNT = 1025;

    // National Pokédex ranges, including Hisui and the Gen 9 DLC additions.
    private static final Map<String, int[]> GEN_RANGES = Map.of(
            "All", new int[]{1, POKEMON_COUNT},
            "Gen 1", new int[]{1, 151},
            "Gen 2", new int[]{152, 251},
            "Gen 3", new int[]{252, 386},
            "Gen 4", new int[]{387, 493},
            "Gen 5", new int[]{494, 649},
            "Gen 6", new int[]{650, 721},
            "Gen 7", new int[]{722, 809},
            "Gen 8", new int[]{810, 905},
            "Gen 9", new int[]{906, POKEMON_COUNT}
    );

    private static final List<String> GENERATIONS = List.of(
            "All", "Gen 1", "Gen 2", "Gen 3", "Gen 4", "Gen 5", "Gen 6", "Gen 7", "Gen 8", "Gen 9"
    );

    private String currentGen = "All";

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(18);
        root.setPadding(new Insets(28));
        root.setAlignment(Pos.TOP_CENTER);
        root.getStyleClass().add("app-content");
        Label title = new Label("Shiny Encounter Counter");
        title.getStyleClass().add("app-title");
        title.setWrapText(true);
        title.setAlignment(Pos.CENTER);
        Label subtitle = new Label("A little patience. A little luck. One more encounter.");
        subtitle.getStyleClass().add("subtitle");
        subtitle.setWrapText(true);
        subtitle.setAlignment(Pos.CENTER);
        VBox header = new VBox(6, title, subtitle);
        header.setAlignment(Pos.CENTER);

        searchField = new TextField();
        searchField.setPromptText("Search Pokémon by name...");
        searchField.setDisable(true);
        suggestionList.getStyleClass().add("search-suggestions");
        suggestionList.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        suggestionList.setFixedCellSize(36);
        suggestionList.setFocusTraversable(false);
        searchSuggestions.getContent().add(suggestionList);
        searchSuggestions.setAutoHide(true);
        searchSuggestions.setHideOnEscape(true);
        suggestionList.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                selectSearchSuggestion();
            }
        });
        searchField.setOnKeyPressed(e -> {
            if (!searchSuggestions.isShowing()) {
                return;
            }
            if (e.getCode() == KeyCode.DOWN) {
                suggestionList.getSelectionModel().selectNext();
                suggestionList.scrollTo(suggestionList.getSelectionModel().getSelectedIndex());
                e.consume();
            } else if (e.getCode() == KeyCode.UP) {
                suggestionList.getSelectionModel().selectPrevious();
                suggestionList.scrollTo(suggestionList.getSelectionModel().getSelectedIndex());
                e.consume();
            } else if (e.getCode() == KeyCode.ENTER) {
                selectSearchSuggestion();
                e.consume();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                searchSuggestions.hide();
                e.consume();
            }
        });
        genComboBox = new ComboBox<>();
        genComboBox.getItems().addAll(GENERATIONS);
        genComboBox.setValue("All");
        genComboBox.setDisable(true);
        genComboBox.setMaxWidth(Double.MAX_VALUE);
        Label generationLabel = new Label("Generation");
        generationLabel.setLabelFor(genComboBox);
        VBox generationBox = new VBox(6, generationLabel, genComboBox);
        generationBox.setPrefWidth(140);
        generationBox.setMinWidth(140);
        Label searchLabel = new Label("Find your Pokémon");
        searchLabel.setLabelFor(searchField);
        VBox searchBox = new VBox(6, searchLabel, searchField);
        searchBox.setMinWidth(0);
        HBox.setHgrow(searchBox, Priority.ALWAYS);
        HBox filtersBox = new HBox(14, generationBox, searchBox);
        statusLabel = new Label("Loading Pokémon...");
        statusLabel.setWrapText(true);
        statusLabel.getStyleClass().add("status");

        pokemonComboBox = new ComboBox<>();
        pokemonComboBox.setMaxWidth(Double.MAX_VALUE);
        pokemonComboBox.setPromptText("Select Pokémon");
        pokemonComboBox.setDisable(true);
        pokemonComboBox.setItems(filteredPokemon);
        VBox filtersCard = new VBox(12, filtersBox, pokemonComboBox, statusLabel);
        filtersCard.getStyleClass().add("card");

        shinyImageView = new ImageView();
        shinyImageView.setFitWidth(160);
        shinyImageView.setFitHeight(160);
        shinyImageView.setPreserveRatio(true);
        shinyImageView.setSmooth(false);
        Circle spriteBackdrop = new Circle(86);
        spriteBackdrop.getStyleClass().add("sprite-backdrop");
        spriteStatusLabel = new Label("Your shiny goes here");
        spriteStatusLabel.getStyleClass().add("sprite-status");
        StackPane spriteFrame = new StackPane(spriteBackdrop, shinyImageView, spriteStatusLabel);
        spriteFrame.setMinHeight(180);
        spriteFrame.setPrefHeight(180);
        pokemonNameLabel = new Label("Choose your next shiny");
        pokemonNameLabel.getStyleClass().add("pokemon-name");
        pokemonNameLabel.setWrapText(true);
        pokemonNameLabel.setAlignment(Pos.CENTER);

        Label counterCaption = new Label("ENCOUNTERS");
        counterCaption.getStyleClass().add("counter-caption");
        counterLabel = new Label("0");
        counterLabel.getStyleClass().add("counter-value");
        Button plusBtn = new Button("+");
        Button minusBtn = new Button("−");
        Button resetBtn = new Button("Reset");
        plusBtn.getStyleClass().add("encounter-increment");
        minusBtn.getStyleClass().add("encounter-decrement");
        plusBtn.setAccessibleText("Add one encounter");
        minusBtn.setAccessibleText("Remove one encounter");
        counterControls = new HBox(12, minusBtn, plusBtn, resetBtn);
        counterControls.setAlignment(Pos.CENTER);
        counterControls.setDisable(true);
        VBox counterBox = new VBox(4, counterCaption, counterLabel, counterControls);
        counterBox.setAlignment(Pos.CENTER);

        VBox huntCard = new VBox(12, pokemonNameLabel, spriteFrame, counterBox);
        huntCard.setAlignment(Pos.CENTER);
        huntCard.getStyleClass().addAll("card", "hunt-card");
        Label footer = new Label("One encounter closer.");
        footer.getStyleClass().add("subtitle");
        root.getChildren().addAll(header, filtersCard, huntCard, footer);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        Scene scene = new Scene(scrollPane, 600, 820);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Shiny Encounter Counter");
        primaryStage.setMinWidth(560);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        // Load Pokémon list async
        loadPokemonList();

        // Event Listeners:
        genComboBox.setOnAction(e -> {
            currentGen = genComboBox.getValue();
            filterAndUpdatePokemonList();
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndUpdatePokemonList());
        searchField.focusedProperty().addListener((obs, wasFocused, focused) -> {
            if (focused) {
                updateSearchSuggestions();
            } else {
                searchSuggestions.hide();
            }
        });

        pokemonComboBox.setOnAction(e -> {
            if (updatingPokemonList) {
                return;
            }
            updateSelectedPokemon();
        });

        plusBtn.setOnAction(e -> incrementCounter());
        minusBtn.setOnAction(e -> decrementCounter());
        resetBtn.setOnAction(e -> resetCounter());
    }

    private void updateSelectedPokemon() {
        Pokemon selected = pokemonComboBox.getSelectionModel().getSelectedItem();
        pokemonNameLabel.setText(selected == null ? "Choose your next shiny" : selected.name);
        counterControls.setDisable(selected == null);
        updateShinyImage(selected);
        resetCounter();
    }

    private void loadPokemonList() {
        Thread loader = new Thread(() -> {
            try {
                String urlStr = "https://pokeapi.co/api/v2/pokemon?limit=" + POKEMON_COUNT;
                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(10000);

                List<Pokemon> tempList;
                try {
                    int responseCode = conn.getResponseCode();
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        throw new IOException("PokéAPI returned HTTP " + responseCode);
                    }

                    try (InputStreamReader reader = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                        JsonObject response = new Gson().fromJson(reader, JsonObject.class);
                        JsonArray results = response.getAsJsonArray("results");
                        tempList = new ArrayList<>();
                        for (int i = 0; i < results.size(); i++) {
                            JsonObject pokeObj = results.get(i).getAsJsonObject();
                            String name = pokeObj.get("name").getAsString();
                            tempList.add(new Pokemon(i + 1, name));
                        }
                    }
                } finally {
                    conn.disconnect();
                }

                Platform.runLater(() -> {
                    allPokemon.setAll(tempList);
                    searchField.setDisable(false);
                    genComboBox.setDisable(false);
                    pokemonComboBox.setDisable(false);
                    filterAndUpdatePokemonList();
                });
            } catch (IOException | JsonParseException e) {
                Platform.runLater(() -> statusLabel.setText("Couldn't load Pokémon. Check your internet connection and try again."));
            }
        }, "pokemon-list-loader");
        loader.setDaemon(true);
        loader.start();
    }

    private void filterAndUpdatePokemonList() {
        String filter = searchField.getText();
        int[] genRange = GEN_RANGES.get(currentGen);

        List<Pokemon> filtered = allPokemon.stream()
                .filter(p -> p.id >= genRange[0] && p.id <= genRange[1])
                .filter(p -> filter == null || filter.isEmpty()
                        || p.name.toLowerCase(Locale.ROOT).startsWith(filter.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());

        Pokemon selected = pokemonComboBox.getValue();
        if (!filteredPokemon.equals(filtered)) {
            updatingPokemonList = true;
            try {
                filteredPokemon.setAll(filtered);
                pokemonComboBox.setValue(filtered.contains(selected) ? selected : null);
            } finally {
                updatingPokemonList = false;
            }
            if (pokemonComboBox.getValue() != selected) {
                updateSelectedPokemon();
            }
        }
        statusLabel.setText(filtered.isEmpty() ? "No Pokémon match your search."
                : filtered.size() + " Pokémon to choose from");
        updateSearchSuggestions();
    }

    private void updateSearchSuggestions() {
        if (!searchField.isFocused() || searchField.getText().isBlank() || filteredPokemon.isEmpty()) {
            searchSuggestions.hide();
            suggestionList.getItems().clear();
            return;
        }

        suggestionList.getItems().setAll(filteredPokemon.stream().limit(8).toList());
        suggestionList.getSelectionModel().clearSelection();
        suggestionList.setPrefWidth(searchField.getWidth());
        suggestionList.setPrefHeight(suggestionList.getItems().size() * 36 + 2);
        if (!searchSuggestions.isShowing()) {
            var bounds = searchField.localToScreen(searchField.getBoundsInLocal());
            searchSuggestions.show(searchField, bounds.getMinX(), bounds.getMaxY() + 4);
        }
    }

    private void selectSearchSuggestion() {
        Pokemon selected = suggestionList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            pokemonComboBox.setValue(selected);
            searchSuggestions.hide();
        }
    }

    private void updateShinyImage(Pokemon pokemon) {
        if (pokemon == null) {
            shinyImageView.setImage(null);
            spriteStatusLabel.setText("Your shiny goes here");
            spriteStatusLabel.setVisible(true);
            return;
        }
        String url = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/shiny/" + pokemon.id + ".png";
        Image shinyImage = new Image(url, true);
        shinyImageView.setImage(shinyImage);
        spriteStatusLabel.setText("Loading shiny...");
        spriteStatusLabel.setVisible(true);
        shinyImage.progressProperty().addListener((obs, previous, progress) -> {
            if (shinyImageView.getImage() == shinyImage && progress.doubleValue() == 1) {
                spriteStatusLabel.setVisible(shinyImage.isError());
            }
        });
        shinyImage.errorProperty().addListener((obs, previous, error) -> {
            if (error && shinyImageView.getImage() == shinyImage) {
                spriteStatusLabel.setText("Sprite unavailable");
                spriteStatusLabel.setVisible(true);
            }
        });
        if (shinyImage.isError()) {
            spriteStatusLabel.setText("Sprite unavailable");
        } else if (shinyImage.getProgress() == 1) {
            spriteStatusLabel.setVisible(false);
        }
    }

    private void incrementCounter() {
        shinyCounter++;
        updateCounterLabel();
    }

    private void decrementCounter() {
        if (shinyCounter > 0) {
            shinyCounter--;
            updateCounterLabel();
        }
    }

    private void resetCounter() {
        shinyCounter = 0;
        updateCounterLabel();
    }

    private void updateCounterLabel() {
        counterLabel.setText(Integer.toString(shinyCounter));
    }

    private static class Pokemon {
        private final int id;
        private final String name;

        Pokemon(int id, String name) {
            this.id = id;
            this.name = capitalize(name);
        }

        @Override
        public String toString() {
            return name;
        }

        private static String capitalize(String str) {
            if (str == null || str.isEmpty()) return str;
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }
    }
}
