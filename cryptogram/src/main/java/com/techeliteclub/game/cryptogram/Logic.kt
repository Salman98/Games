package com.techeliteclub.game.cryptogram

import kotlin.random.Random

const val MAX_MISTAKES = 3

/** Proverbs and sayings old enough to be in the public domain. Letters and spaces only, kept short enough for a phone. */
val QUOTES = """
A JOURNEY OF A THOUSAND MILES BEGINS WITH A SINGLE STEP
ACTIONS SPEAK LOUDER THAN WORDS
ALL THAT GLITTERS IS NOT GOLD
A STITCH IN TIME SAVES NINE
BETTER LATE THAN NEVER
THE EARLY BIRD CATCHES THE WORM
PRACTICE MAKES PERFECT
WHERE THERE IS A WILL THERE IS A WAY
TWO HEADS ARE BETTER THAN ONE
HONESTY IS THE BEST POLICY
LOOK BEFORE YOU LEAP
MANY HANDS MAKE LIGHT WORK
ROME WAS NOT BUILT IN A DAY
THE PEN IS MIGHTIER THAN THE SWORD
WELL BEGUN IS HALF DONE
KNOWLEDGE IS POWER
FORTUNE FAVORS THE BOLD
A FRIEND IN NEED IS A FRIEND INDEED
EVERY CLOUD HAS A SILVER LINING
STILL WATERS RUN DEEP
NO MAN IS AN ISLAND
TIME AND TIDE WAIT FOR NO MAN
A ROLLING STONE GATHERS NO MOSS
NECESSITY IS THE MOTHER OF INVENTION
THE BEST WAY OUT IS ALWAYS THROUGH
SLOW AND STEADY WINS THE RACE
LITTLE STROKES FELL GREAT OAKS
AN OUNCE OF PREVENTION IS WORTH A POUND OF CURE
EARLY TO BED AND EARLY TO RISE MAKES A MAN HEALTHY WEALTHY AND WISE
LOST TIME IS NEVER FOUND AGAIN
BREVITY IS THE SOUL OF WIT
ALL IS WELL THAT ENDS WELL
TO THINE OWN SELF BE TRUE
THE ONLY WAY TO HAVE A FRIEND IS TO BE ONE
WHAT WE THINK WE BECOME
THE UNEXAMINED LIFE IS NOT WORTH LIVING
WONDER IS THE BEGINNING OF WISDOM
NOTHING VENTURED NOTHING GAINED
A PICTURE IS WORTH A THOUSAND WORDS
WHEN IN ROME DO AS THE ROMANS DO
THE DARKEST HOUR IS JUST BEFORE THE DAWN
HOPE FOR THE BEST AND PREPARE FOR THE WORST
EASY COME EASY GO
OUT OF SIGHT OUT OF MIND
ONE GOOD TURN DESERVES ANOTHER
STRIKE WHILE THE IRON IS HOT
THE PROOF OF THE PUDDING IS IN THE EATING
BIRDS OF A FEATHER FLOCK TOGETHER
DO NOT COUNT YOUR CHICKENS BEFORE THEY HATCH
GREAT OAKS FROM LITTLE ACORNS GROW
IF AT FIRST YOU DO NOT SUCCEED TRY AGAIN
KINDNESS IS NEVER WASTED
LEARN TO WALK BEFORE YOU RUN
MAKE HAY WHILE THE SUN SHINES
PATIENCE IS A VIRTUE
THE SQUEAKY WHEEL GETS THE GREASE
VARIETY IS THE SPICE OF LIFE
YOU REAP WHAT YOU SOW
A WATCHED POT NEVER BOILS
CURIOSITY IS THE WICK IN THE CANDLE OF LEARNING
""".trim().lines()

/** [code] gives each letter of the quote its number. [given] letters are shown from the start. */
class Puzzle(val quote: String, val code: Map<Char, Int>, val given: Set<Char>) {
    val letters = quote.filter { it.isLetter() }.toSet()
}

/** How many letters are shown at the start: fewer on later levels, never fewer than 2. */
fun givenFor(level: Int, distinct: Int) = maxOf(2, distinct / 3 - level / 10)

fun newPuzzle(level: Int, rng: Random): Puzzle {
    val quote = QUOTES[rng.nextInt(QUOTES.size)]
    val letters = quote.filter { it.isLetter() }.toSet().sorted()
    val numbers = (1..26).shuffled(rng)
    val code = letters.mapIndexed { i, ch -> ch to numbers[i] }.toMap()
    // Show the most common letters first: they give the player the most to work from.
    val byCount = letters.sortedByDescending { ch -> quote.count { it == ch } }
    return Puzzle(quote, code, byCount.take(givenFor(level, letters.size)).toSet())
}

/** Letters the player still has to find. */
fun missing(p: Puzzle, found: Set<Char>) = p.letters - p.given - found

fun isSolved(p: Puzzle, found: Set<Char>) = missing(p, found).isEmpty()

/** Splits the quote into lines of at most [width] characters, breaking between words. */
fun wrap(quote: String, width: Int): List<List<String>> {
    val lines = mutableListOf<MutableList<String>>(mutableListOf())
    for (word in quote.split(" ")) {
        val used = lines.last().sumOf { it.length + 1 }
        if (lines.last().isNotEmpty() && used + word.length > width) lines += mutableListOf<String>()
        lines.last() += word
    }
    return lines
}

/** Found letters, mistakes, seconds. */
fun encodeGame(found: Set<Char>, mistakes: Int, seconds: Int) = found.sorted().joinToString("") + ";" + mistakes + ";" + seconds

fun decodeGame(text: String, p: Puzzle): Triple<Set<Char>, Int, Int> {
    val (f, m, s) = text.split(";")
    require(f.all { it in p.letters } && m.toInt() in 0 until MAX_MISTAKES)
    return Triple(f.toSet(), m.toInt(), s.toInt())
}
