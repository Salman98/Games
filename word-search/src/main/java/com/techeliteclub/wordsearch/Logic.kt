package com.techeliteclub.wordsearch

import kotlin.random.Random

val WORDS = """
APPLE BANANA CHERRY GRAPE LEMON MANGO ORANGE PEACH PEAR PLUM MELON BERRY
TIGER LION ZEBRA HORSE RABBIT MONKEY PANDA KOALA EAGLE SHARK WHALE DOLPHIN TURTLE SNAKE FROG MOUSE CAMEL GOAT SHEEP
RIVER OCEAN BEACH ISLAND FOREST DESERT VALLEY MOUNTAIN CLOUD STORM RAIN SNOW WIND SUNNY THUNDER
PLANET ROCKET COMET GALAXY MOON STAR ORBIT SPACE
PIANO GUITAR DRUM FLUTE VIOLIN MUSIC SONG DANCE
SCHOOL PENCIL PAPER BOOK RULER CHALK LESSON TEACHER
BREAD BUTTER CHEESE PASTA PIZZA SALAD SOUP HONEY COOKIE CANDY
GARDEN FLOWER TULIP ROSE DAISY TREE LEAF SEED GRASS
TRAIN PLANE TRUCK BICYCLE BOAT SHIP ROAD BRIDGE
HAPPY SMILE LAUGH FRIEND FAMILY GAME PUZZLE MAGIC DREAM LIGHT
PURPLE YELLOW GREEN SILVER GOLDEN WHITE BLACK BROWN
SOCCER TENNIS HOCKEY CHESS SKATE SWIM JUMP RACE
""".trim().split(Regex("\\s+"))

private val EASY_DIRS = listOf(0 to 1, 1 to 0, 1 to 1)
private val ALL_DIRS = EASY_DIRS + listOf(0 to -1, -1 to 0, -1 to -1, 1 to -1, -1 to 1)

fun sizeFor(level: Int) = minOf(8 + level / 5, 12)
fun countFor(level: Int) = minOf(5 + level / 4, 10)

/** [placed] maps each word to its cells (row-major indexes) from first to last letter. */
class Puzzle(val n: Int, val letters: CharArray, val placed: Map<String, List<Int>>)

fun newPuzzle(level: Int, rng: Random): Puzzle {
    val n = sizeFor(level)
    val dirs = if (level < 5) EASY_DIRS else ALL_DIRS
    val grid = CharArray(n * n) { ' ' }
    val placed = LinkedHashMap<String, List<Int>>()
    for (word in WORDS.filter { it.length <= n }.shuffled(rng)) {
        if (placed.size >= countFor(level)) break
        for (attempt in 0 until 200) {
            val (dr, dc) = dirs.random(rng)
            val r0 = rng.nextInt(n)
            val c0 = rng.nextInt(n)
            val cells = word.indices.map { k -> (r0 + dr * k) to (c0 + dc * k) }
            if (cells.all { (r, c) -> r in 0 until n && c in 0 until n } &&
                cells.zip(word.toList()).all { (rc, ch) -> grid[rc.first * n + rc.second].let { it == ' ' || it == ch } }
            ) {
                val idx = cells.map { (r, c) -> r * n + c }
                idx.zip(word.toList()).forEach { (i, ch) -> grid[i] = ch }
                placed[word] = idx
                break
            }
        }
    }
    for (i in grid.indices) if (grid[i] == ' ') grid[i] = 'A' + rng.nextInt(26)
    return Puzzle(n, grid, placed)
}

/** Cells on the straight line from [a] to [b], or null if they are not in a row, column or diagonal. */
fun line(n: Int, a: Int, b: Int): List<Int>? {
    val dr = b / n - a / n
    val dc = b % n - a % n
    if (dr != 0 && dc != 0 && kotlin.math.abs(dr) != kotlin.math.abs(dc)) return null
    val steps = maxOf(kotlin.math.abs(dr), kotlin.math.abs(dc))
    val sr = Integer.signum(dr)
    val sc = Integer.signum(dc)
    return (0..steps).map { k -> (a / n + sr * k) * n + (a % n + sc * k) }
}

/** The not-yet-found word selected from [a] to [b] (either direction), if any. */
fun wordAt(p: Puzzle, a: Int, b: Int, found: Set<String>): String? {
    val cells = line(p.n, a, b) ?: return null
    return p.placed.entries.firstOrNull { (w, idx) -> w !in found && (idx == cells || idx == cells.asReversed()) }?.key
}
