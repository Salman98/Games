pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Games"

// Shared code (ads, theme, game screen). Every game depends on it.
include(":core")

// One line per game. Build one with: ./gradlew :block-puzzle:bundleRelease
include(
    ":block-puzzle",
    ":color-sort",
    ":crown-logic",
    ":arrow-escape",
    ":tile-mahjong",
    ":sudoku",
    ":word-search",
    ":solitaire",
    ":car-escape",
    ":merge-2048",
    ":mini-sudoku",
    ":sun-moon",
    ":number-path",
)
