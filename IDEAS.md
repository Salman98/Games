# Next 90 games: researched idea list (games 11 to 100)

Researched 13 Nov 2026. Ranked the way you asked: **low competition** and **ad revenue per player** (long sessions, daily return) first, then how cheaply the game fits our `core`.

Already built, not repeated here: Block Grid Puzzle, Color Sort, Crown Logic, Arrow Escape, Tile Mahjong, Sudoku, Word Search, Solitaire Klondike, Car Escape, 2048 Merge.

## Progress

Tick a batch when all 10 games are built.

- [x] Batch 1 (games 11 to 20): daily logic and word dailies. Built, not yet run on a phone
- [ ] Batch 2 (games 21 to 30): board and card classics against the computer
- [ ] Batch 3 (games 31 to 40): word, trivia and brain
- [ ] Batch 4 (games 41 to 50): relaxing puzzles
- [ ] Batch 5 (games 51 to 60): quick table games
- [ ] Batch 6 (games 61 to 70): casual puzzle hits
- [ ] Batch 7 (games 71 to 80): one-touch arcade
- [ ] Batch 8 (games 81 to 90): sports and skill
- [ ] Batch 9 (games 91 to 100): bigger builds, 6 slots still open

## What the research says

1. **Puzzle is the right place to be.** Puzzle games were downloaded 2.59 billion times in Q3 2026, the most of any genre [S1], and earned 53% of mobile game ad revenue in Feb to Apr 2026 [S2].
2. **Two of your 10 are on this year's top charts.** An arrow puzzle was the most downloaded game in the world in May 2026 [S3]. Meowdoku, No. 5 worldwide in October 2026 [S4], has the same rules as Crown Logic with cats instead of crowns [S5].
3. **The biggest ad earners are calm, daily-habit games.** Ad revenue Jan to May 2026: Block Blast $127M, Vita Mahjong $89.8M, Solitaire Associations Journey $73.3M, Wordscapes $42.6M. Easybrain (sudoku, nonogram, number match, jigsaw) earned $117.3M as a publisher [S2].
4. **Classic genres keep players longest.** Board, card, trivia and word games beat other genres on retention in every region; card games have the longest sessions; arcade games win day 1 but lose players fast [S7].
5. **Growing on Google Play:** Word +19.5%, Trivia +13.3%, Board +9.0% monthly downloads over Sep 2025 to Aug 2026 [S8].
6. **Daily logic puzzles have proven demand and few polished apps.** LinkedIn reports 84% of players return the next day [S9]. NYT puzzles were played 11.2 billion times in 2025 [S10]. Android apps with the same rules are still small: a Tango-rules app has 10K+ downloads, a Queens one 100K+, a Connections-style one about 8K [S11][S12].
7. **Crowded, enter with care:** block puzzle (2,000+ new releases in H1 2026, up 89%), sort and screw (new releases cooling), match-3 (locked market; left out of this list) [S13].

## One app = one core rule (Play safety rule)

Google Play removes "multiple apps with highly similar functionality, content, and user experience" from one account, including reskins [S40]. So every app below has its own **core rule** (the Family column), and no family appears twice in the 100, counting the 10 games already built. `store/check_ideas.py` fails if two rows share a family.

- A variant of a game we already have (Spider, FreeCell, Mini Sudoku, Killer Sudoku...) becomes a **mode inside that game**, never a new app.
- Each app also gets its own name, icon, colour theme, screenshots and store text. Release them a few at a time, not all at once.


- **Family:** the core rule. Unique across all 100.
- **Type:** P = proven mechanic with our own name and art. T = proven mechanic plus a twist.
- **Fit:** A = fits `core` as it is (tap or drag on a board, levels, hints, daily). B = needs one new piece (computer opponent, written content, drag physics). C = needs a real-time game loop or a lot of art.
- Working names are placeholders. Brand names in the Evidence column (Wordle, Tango, Pixel Flow...) are only references; never use them in a store listing.

## Batch 1 (games 11 to 20): daily logic and word dailies

Best match for both of your ranking rules. Each plays differently from all games already built.

| # | Working name | What the player does | Evidence | Family | Type | Fit |
|---|---|---|---|---|---|---|
| 11 | Sun & Moon | Fill the grid with two symbols: equal count per row and column, never three in a row | LinkedIn Tango; logic games are the fan favourites there [S9]. Same-rules Android app only 10K+ downloads [S11] | two-symbol balance grid | P | A |
| 12 | Number Path | Draw one line through every cell, passing the numbers in order | LinkedIn Zip, learnable in seconds [S9] | single path through all cells | P | A |
| 13 | Nonogram | Use row and column number clues to reveal a pixel picture | Easybrain Nonogram.com is one of its growth drivers [S14]; Nonogram Crossing ~946K installs [S15] | picture from row/column clues | P | A |
| 14 | Rectangles | Cut the grid into rectangles, each holding one number equal to its size | LinkedIn Patches; a look-alike app is on Play [S11] | divide grid into numbered areas | P | A |
| 15 | Number Match | Clear the board by pairing equal numbers or pairs that add to 10 | Easybrain market leader in Apr 2026 [S14]; audience 35+, revenue mostly from ads [S16] | pair-and-clear numbers | P | A |
| 16 | Cryptogram | Decode a quote where each letter is replaced by a number | A faith-themed cryptogram estimated at $8K to $11K a day in ads [S17]; a cryptogram app is in a Sensor Tower word top-5 list [S18] | letter substitution decode | P | B (quotes) |
| 17 | Five Letters | Guess the hidden 5-letter word in 6 tries | Wordle: 4.2B plays in 2025 [S10]; the "Wordle!" app has 14.5M Android installs [S12] | guess a hidden word | P | A (word list) |
| 18 | Word Groups | Sort 16 words into 4 hidden categories | NYT Connections: 1.6B solves in 2025 [S10]; Android look-alike only ~8K installs [S12] | sort words into groups | P | B (written sets) |
| 19 | Domino Logic | Place dominoes so the pips meet each region's rule | NYT Pips: 612M solves in 2025, its first year [S10] | domino placement logic | P | A |
| 20 | Bridges | Connect islands with the number of bridges each asks for | Hashi apps on Play [S11][S27] | connect islands with bridges | P | A |


## Batch 2 (games 21 to 30): board and card classics against the computer

Longest sessions and best retention of any genre [S7]. One computer opponent engine per game.

| # | Working name | What the player does | Evidence | Family | Type | Fit |
|---|---|---|---|---|---|---|
| 21 | Checkers | Capture all opponent pieces by jumping diagonally | Sensor Tower board top-5 lists, Latin America [S20] | diagonal capture board | P | B (opponent) |
| 22 | Ludo | Race 4 tokens home with dice; send rivals back | Ludo King: 79.1M monthly users, ad-funded [S2] | dice race with captures | P | B (opponent) |
| 23 | Yatzy | Roll 5 dice up to 3 times, fill the score card | Ad-funded studio portfolio [S21] | dice score card | P | A |
| 24 | Gin Rummy | Form sets and runs, knock to win | [S21][S16] | melding card game | P | B (opponent) |
| 25 | Backgammon | Move 15 checkers home by dice | Easybrain publishes one [S14] | dice race on points | P | B (opponent) |
| 26 | Spades | Partnership trick-taking, bid your tricks | Board-player profile [S16] | trick-taking cards | P | B (opponent) |
| 27 | Chess | Chess against the computer, levels and daily puzzle | Board top-5 lists [S20] | chess | P | B (strong opponent) |
| 28 | Reversi | Flip discs by surrounding them; most discs wins | Reversi apps ~565K installs [S52] | disc flipping | P | B (opponent) |
| 29 | Mancala | Sow seeds round the pits, capture the most | Mancala by AppOn ~8.8M installs [S52] | seed sowing | P | B (opponent) |
| 30 | Mill | Line up 3 to make a mill and remove a rival piece | Align It (Nine Men's Morris) 7.9M installs; Mills 6.6M [S42] | three-in-line capture | P | B (opponent) |

## Batch 3 (games 31 to 40): word, trivia and brain

Word +19.5% and Trivia +13.3% on Play [S8]. Written content is the cost.

| # | Working name | What the player does | Evidence | Family | Type | Fit |
|---|---|---|---|---|---|---|
| 31 | Word Connect | Swipe letters in a wheel to fill a crossword | Wordscapes: $42.6M ad revenue Jan to May 2026 [S2] | letter wheel crossword | P | A |
| 32 | Mini Crossword | A 5×5 crossword with written clues, solved in minutes | NYT Mini: 1.4B plays in 2025 [S10] | clue crossword | P | B (clues) |
| 33 | Word Board | Place letter tiles on a board for points against the computer | Wordfeud 10M+ Android installs [S52] | tile-scoring word board | P | B (opponent, dictionary) |
| 34 | Word Ladder | Change one letter at a time to turn one word into another | LinkedIn Crossclimb [S9] | one-letter word chain | P | A |
| 35 | Emoji Words | Guess the word or phrase from emojis | Picture-word games 500K+ installs [S22] | emoji rebus | P | B (written sets) |
| 36 | Quiz | General-knowledge multiple choice, daily set | Trivia +13.3% [S8] | multiple-choice trivia | P | B (questions) |
| 37 | Speed Math | Solve as many sums as you can before the clock runs out | Math Quiz in a 24-app trivia studio [S35] | timed arithmetic | P | A (generated) |
| 38 | Brain Riddles | Trick questions answered by tapping or dragging on the picture | Brain Puzzle 3 No. 4 worldwide, Oct 2026 [S4] | trick riddles | P | C if drawn; B if text |
| 39 | Logic Grid | Use written clues to match people, places and things | Easybrain Logic Puzzles top Board download [S14] | clue deduction grid | P | B (written clues) |
| 40 | Memory Pairs | Flip cards two at a time and find all the pairs | Memory game: Card Matching 4.1M installs [S46] | flip-and-remember pairs | P | A |

## Batch 4 (games 41 to 50): relaxing puzzles

| # | Working name | What the player does | Evidence | Family | Type | Fit |
|---|---|---|---|---|---|---|
| 41 | Number Sums | Keep only the numbers that add up to each row and column total | Easybrain release, 2023 [S14] | sum elimination | P | A |
| 42 | Jigsaw | Classic jigsaw puzzles | Easybrain market leader [S14] | jigsaw | P | C (licensed photos) |
| 43 | Spot the Difference | Find the differences between two pictures | Easybrain Differences in App Store charts [S26] | find differences | P | C (picture pairs) |
| 44 | Lights Out | Tap a light to flip it and its neighbours; switch them all off | Logic collections [S27] | toggle lights | P | A |
| 45 | Minesweeper | Clear the field using number clues, no-guess boards | Logic collections [S27] | hidden mines | P | A |
| 46 | Tangram | Fit 7 shapes into the outline | Tangram Master ~2.1M installs [S52] | shape outline fill | P | A |
| 47 | Box Pusher | Push every box onto a target without getting stuck | Sokoban apps ~330K installs [S52] | push boxes to targets | P | A |
| 48 | Peg Jump | Jump pegs over each other until one is left | Peg solitaire apps ~196K installs [S52] | peg solitaire | P | A |
| 49 | Five in a Row | Move a ball to make lines of 5; 3 new balls drop each turn | Lines 98 – Color Ball 2.9M installs [S43] | line-of-five balls | P | A |
| 50 | Cube | Turn a 3D cube until each face is one colour | Cubik's 10.8M installs [S44] | twisty cube | P | C (3D) |

## Batch 5 (games 51 to 60): quick table games

| # | Working name | What the player does | Evidence | Family | Type | Fit |
|---|---|---|---|---|---|---|
| 51 | Tic Tac Toe + | Tic-tac-toe plus 2-player offline modes on one phone | No-wifi tic-tac-toe 1M+ trending [S24] | three-in-a-row | P | A |
| 52 | Sea Battle | Hide your fleet and shoot at the computer's | Sea Battle 2: 50.9M installs [S52] | hidden fleet shooting | P | B (opponent) |
| 53 | Dots & Boxes | Draw lines; close a box to score and go again | Dots and Boxes ~131K installs [S52] | close the box | P | B (opponent) |
| 54 | Crazy Eights | Match suit or rank to empty your hand first | Crazy Eights by LITE ~5.8M installs [S52] | shed cards by match | P | B (opponent) |
| 55 | Durak | Defend each attack card with a higher one; don't be last | Durak by cmyksoft ~9.5M installs [S52] | attack-defend cards | P | B (opponent) |
| 56 | Cribbage | Score 15s, pairs and runs; peg to 121 | Ultimate Cribbage ~3M installs [S52] | pegging card game | P | B (opponent) |
| 57 | Carrom | Flick the striker to pocket your coins | Carrom King 90.3M; Carrom Pool 500M+ [S52] | flick discs into pockets | P | C (physics) |
| 58 | Air Hockey | Hit the puck into the goal before the computer does | Air Hockey by mobirix 85M installs [S52] | real-time puck duel | P | C |
| 59 | Tilt Maze | Tilt the phone to roll a ball out of the maze | Mazes & More 95.8M; Maze Games 4.3M [S45] | navigate a maze | P | B (tilt) |
| 60 | Hexa Stack | Place hexagon stacks; touching same colours move and clear at 10 | Hexa Sort ~$20M in 2025, its own sub-category [S29] | hex stack merge | P | A |

## Batch 6 (games 61 to 70): casual puzzle hits

| # | Working name | What the player does | Evidence | Family | Type | Fit |
|---|---|---|---|---|---|---|
| 61 | Rope Untangle | Move pins until no ropes cross | Twisted Tangle ~$15M from ads [S33] | untangle lines | P | B (drag) |
| 62 | Ring Turn | Rotate nested rings to line up the gaps | Rotate Rings 18.7M since Aug 2026 [S4] | rotate rings | T | B |
| 63 | Tap Blast | Tap groups of same-colour cubes to pop them | Match-2 blast +35% in H1 2026 [S13] | tap groups to pop | P | B |
| 64 | Screw Puzzle | Unscrew bolts in the right order to drop the plates | Screwdom $29M in Q3 2025 [S38] | unscrew order | P | C (physics) |
| 65 | Pixel Shooter | Send shooters round a belt to clear a pixel picture | Pixel Flow $125M in under a year [S13] | conveyor shooters | T (own rules, a copycat was removed [S13]) | C |
| 66 | Hole | Move a hole to swallow everything on the board | All in Hole $16.5M in Q3 2025 [S38] | swallow objects | P | C |
| 67 | Tidy Up | Put messy items in their place | Satistory 1M+ trending [S24] | put things away | P | C (art) |
| 68 | Antistress | Fidget toys: pop, slide, spin | Antistress 370M installs [S23] | fidget toys | P | C |
| 69 | Draw to Solve | Draw one line that falls with physics to solve the level | Brain It On 73.6M installs [S52] | physics drawing | P | C (physics) |
| 70 | Stack | Tap to drop each moving slab exactly on the last one | Stack by Ketchapp 67.4M [S52] | timed stacking | P | B |

## Batch 7 (games 71 to 80): one-touch arcade

Day-1 winners that lose players fast [S7]: cheap to make, ads on every restart.

| # | Working name | What the player does | Evidence | Family | Type | Fit |
|---|---|---|---|---|---|---|
| 71 | Helix Drop | Turn the tower so the ball falls through the gaps | Helix Jump 800M+ [S52] | fall through tower | P | C (3D) |
| 72 | Snake | Eat to grow; don't hit yourself | Snake 1997 ~19M [S52] | growing snake | P | A |
| 73 | Piano Tiles | Tap the dark tiles in time with the music | Magic Tiles 3 500M+ [S52]. Public-domain music only | rhythm tap | P | B (music) |
| 74 | Color Pass | Tap to jump; pass only through your own colour | Color Switch 150M+ [S52] | colour gate jump | P | B |
| 75 | Sky Jumper | Bounce up platforms by tilting; don't fall | Doodle Jump 80.8M [S52] | vertical bounce | P | B (tilt) |
| 76 | Road Hopper | Hop across roads and rivers without getting hit | Crossy Road 200M+ downloads [S47] | lane hopping | P | C |
| 77 | Hook Swing | Hold to grab a hook, release to fly to the next | Stickman Hook 200M+ downloads [S48] | grapple swing | P | C (physics) |
| 78 | Fruit Slice | Swipe to slice fruit, avoid bombs | Fruit Ninja 1B+ [S52] | swipe slicing | P | B |
| 79 | Bubble Shooter | Aim and shoot bubbles to match 3 | 530M installs [S23] | aim and match bubbles | P | C |
| 80 | Brick Breaker | Aim a burst of balls at numbered bricks | Easybrain Brick Blast 2026 [S14][S24] | numbered-brick volley | P | C |

## Batch 8 (games 81 to 90): sports and skill

Each sport gets its own control so no two feel alike.

| # | Working name | What the player does | Evidence | Family | Type | Fit |
|---|---|---|---|---|---|---|
| 81 | Darts | Steady the wobbling aim, release to throw | Darts Club 25M [S52] | aim-and-release target | P | B |
| 82 | Basketball | Swipe an arc to shoot hoops against the clock | Basketball Battle 56M [S52] | swipe arc shot | P | C (physics) |
| 83 | Bowling | Swipe with spin to knock all 10 pins | Bowling Crew 39M [S52] | rolling at pins | P | C (physics) |
| 84 | Mini Golf | Pull back and release; sink the ball in fewest strokes | Golf Battle 95M [S52] | putting course | P | C (physics) |
| 85 | Cricket | Time your swing to hit the bowled ball for runs | Stick Cricket Classic 12M [S52] | timed batting | P | B |
| 86 | Free Kick | Curl the ball round the wall past the keeper | Soccer Superstar 360M [S52] | curved free kick | P | C (physics) |
| 87 | Tennis | Swipe to return the ball, win the rally | Tennis Clash 150M [S52] | rally return | P | C |
| 88 | Fishing | Cast, wait for the bite, reel without snapping the line | Ace Fishing 40M [S52] | cast and reel | P | C (art) |
| 89 | Pinball | Flip the ball up the table, hit targets | Pinball Pro 37.1M [S52] | flipper table | P | C (physics) |
| 90 | Hill Climb | Balance gas and brake to drive up hills without flipping | Hill Climb Racing 2B+ downloads [S51] | physics driving | P | C (physics) |

## Batch 9 (games 91 to 100): bigger builds

Longest to make. Games 95 to 100 are **open**: I did not find 6 more ideas with real evidence that don't repeat a family above. Fill them after the first batches show what earns, and only with a new family.

| # | Working name | What the player does | Evidence | Family | Type | Fit |
|---|---|---|---|---|---|---|
| 91 | Space Shooter | Steer your ship, shoot waves of enemies | Space shooter – Galaxy attack 265M [S52] | vertical shoot-em-up | P | C |
| 92 | Area Capture | Draw loops to claim land from computer rivals | Paper.io 2 ~10M installs a month [S39] | territory loops | P | C |
| 93 | Idle Tycoon | Hire managers, upgrade, earn while away | Idle Miner Tycoon 150M+ [S49] | idle upgrade | P | C |
| 94 | Tower Defense | Build towers on the path to stop enemy waves | Kingdom Rush 30.1M installs [S50] | tower defense | P | C |
| 95 to 100 | open | | | | | |

## Already built (families taken)

| Game | Family |
|---|---|
| Block Grid Puzzle | place pieces, clear lines |
| Color Sort | pour colour layers between tubes |
| Crown Logic | one per row, column and region |
| Arrow Escape | tap arrows to exit |
| Tile Mahjong | pair free tiles |
| Sudoku | 1 to 9 in rows, columns and boxes |
| Word Search | find words in a letter grid |
| Solitaire | Klondike card building |
| Car Escape | slide vehicles out |
| 2048 Merge | swipe to merge equal tiles |

## Left out on purpose (same family as a game above)

| Idea | Too close to |
|---|---|
| Spider, FreeCell, TriPeaks, Pyramid | Solitaire (add as modes) |
| Solitaire Words | Solitaire + Word Groups |
| Dominoes (table game) | Domino Logic |
| Theme Words | Word Search |
| Spelling Hive, Anagram | Word Connect |
| Codeword | Cryptogram and crosswords |
| Hangman | Five Letters |
| Common Link | Word Groups |
| Math Crossword, Kakuro, Hitori | Number Sums |
| Connect Dots, Loop, Loop Fence, Paint Maze | Number Path / Tilt Maze |
| Color by Number | Nonogram |
| Block Jigsaw, Jigsaw Solitaire | Jigsaw / Block Grid |
| Triple Tile | Tile Mahjong |
| Battleships puzzle, Tents & Trees | Crown Logic |
| Nurikabe | Rectangles |
| Greater Than, Skyscrapers, Suguru, Calcudoku, Mini Sudoku, Killer Sudoku | Sudoku (add as modes) |
| Goods Sort, Sushi Sort, Yarn Sort | Color Sort |
| Block Out, Bus Jam, Parking Out, Seat Away | Car Escape |
| Food Quiz, Top 10 | Quiz |
| Emoji Match | Emoji Words |
| Hearts | Spades |
| Chinese Checkers | Checkers |
| Number Drop, Fruit Merge, Merge Board | 2048 |
| Slide Path | Ring Turn / Box Pusher |
| Pull the Pin | Screw Puzzle |
| Marble Sort | Pixel Shooter |
| Delete One Part | Brain Riddles |
| Pool / Billiards | Carrom |
| Archery, Knife Hit | Darts |
| Baseball | Cricket |
| Endless 3-lane runner | Road Hopper |
| Flappy-style flyer | Color Pass (and a clone-heavy market) |
| Match-3 | locked market [S13] |
| Hidden object | falling revenue [S29] |
| Casino, blackjack, bingo | content rating and Play declarations |
| Logo and celebrity quizzes | trademark and likeness |

## Not verified

- No list can promise zero Play risk. All apps share our `core` shell (home, level map, daily, win screen). Give each app its own theme, icon, screenshots and store text, and release a few at a time.
- Install and revenue figures are third-party estimates (AppGoblin, AppBrain, Sensor Tower, AppMagic) or publisher press releases.
- [S52] figures come from AppGoblin and press searches made on 13 and 14 Nov 2026; their page links were not kept. Search the app name on appgoblin.info to check.
- Competition (how many 1M+ apps per mechanic) was not measured for every idea. Check before each batch.
- Card games 24, 26, 54, 55, 56 share a card table look. Give each its own table art and layout.

## Sources

- [S1] Sensor Tower Q3 2026 summary, WN Hub: https://wnhub.io/news/analytics/item-52684
- [S2] Sensor Tower mobile game ad monetization 2026: https://gamedevreports.substack.com/p/sensor-tower-mobile-game-ad-monetization and https://gameworldobserver.com/?p=29151 and https://wnhub.io/news/analytics/item-51344
- [S3] Sensor Tower top games May 2026: https://sensortower.com/blog/top-10-worldwide-mobile-games-by-revenue-and-downloads-in-may-2026
- [S4] Sensor Tower top games October 2026: https://sensortower.com/blog/top-10-worldwide-mobile-games-by-revenue-and-downloads-in-october-2026
- [S5] Meowdoku rules: https://sourceforge.net/app/meowdoku/android/
- [S7] GameAnalytics benchmarks: https://gameindustrylibrary.com/documents/mobile-gaming-benchmarks-2024/read and https://gamedevreports.substack.com/p/gameanalytics-mobile-gaming-benchmarks
- [S8] 42matters Google Play category trends: https://data.42matters.com/top-google-play-game-categories-by-download-trends
- [S9] LinkedIn games: https://www.socialmediatoday.com/news/linkedin-adds-zip-puzzle-game/742847/ and https://jenfoley.substack.com/p/linkedin-games-edition and https://www.linkedin.com/help/linkedin/answer/a6863543
- [S10] NYT Games 2025 numbers: https://www.nbcnewyork.com/entertainment/entertainment-news/hardest-wordle-connections-games-2025/6436464/ and https://www.amediaoperator.com/analysis/wordle-tv-show-new-york-times-games-jonathan-knight/
- [S11] Android logic apps: https://play.google.com/store/apps/details?id=com.brennerd.grid_puzzle.tango and https://play.google.com/store/apps/details?id=com.brennerd.grid_puzzle.queens and https://play.google.com/store/apps/details?id=com.ols.patches
- [S12] AppGoblin: https://appgoblin.info/apps/com.vottzapps.wordle/keywords and https://appgoblin.info/apps/com.connections.wordpuzzle/keywords
- [S13] AppMagic Casual Games Report H1 2026: https://appmagic.rocks/research/casual-report-H12026
- [S14] Easybrain: https://easybrain.com/news/easybrain-surpasses-2-500-000-000-downloads and https://easybrain.com/news and https://easybrain.com/news/easybrain-2023-year-in-the-spotlight
- [S15] AppGoblin, Nonogram Crossing: https://appgoblin.info/apps/com.twodesperados.pic.cross.picross.logic/keywords
- [S16] Appodeal player profiles: https://appodeal.com/blog/who-will-play-your-games-in-2026/
- [S17] Mobile Ad Monetization Newsletter #8: https://www.gamigion.com/?p=13422
- [S18] Sensor Tower word top-5 pages: https://sensortower.com/blog/2025-q4-android-top-5-word-puzzle-games-units-us-603697f4241bc16eb8570d37 and https://sensortower.com/blog/2025-q4-android-top-5-word-puzzle-games-units-europe-603697f4241bc16eb8570d37 and https://sensortower.com/blog/2025-q4-android-top-5-word-puzzle-games-units-qa-603697f4241bc16eb8570d37 and https://sensortower.com/blog/2025-q4-android-top-5-word-puzzle-games-revenue-pl-603697f4241bc16eb8570d37
- [S19] Solitaire apps: https://appgoblin.info/apps/com.easybrain.spider.solitaire and https://www.appbrain.com/dev/Rebrik+Games%2C+TOO/
- [S20] Sensor Tower board top-5 pages: https://sensortower.com/blog/2025-q1-android-top-5-tabletop%20board%20games-units-latin_am-6013e519241bc16eb8e3caaf and https://sensortower-china.com/blog/2020-q4-android-top-5-tabletop%20board%20games-units-latin_am-6013e519241bc16eb8e3caaf
- [S21] Appodeal, Loppi Poppi story: https://appodeal.com/success-stories/loppi-poppi/
- [S22] AppBrain word developers: https://www.appbrain.com/dev/Recommended+Word+Games/ and https://www.appbrain.com/dev/Wordly+Games/ and https://www.appbrain.com/dev/Magic+Word+Games/
- [S23] AppBrain most popular puzzle games: https://www.appbrain.com/apps/most-downloaded/puzzle
- [S24] AppBrain trending puzzle games: https://www.appbrain.com/apps/trending/puzzle
- [S25] Vita Mahjong: https://appgoblin.info/apps/com.vitastudio.mahjong
- [S26] App Store puzzle top charts: https://apps.apple.com/sc/iphone/charts/7012
- [S27] Logic collections: https://play.google.com/store/apps/details?id=com.quietgrid.app and https://apkcombo.com/kpuzzle/ru.kanphis.puzzle/ and https://www.appbrain.com/appstore/puzzles-all-in-one/ios-1622138367
- [S28] AppMagic Mobile Market Landscape 2026: https://gamedevreports.substack.com/p/appmagic-mobile-market-landscape
- [S29] Naavik, niche puzzle subgenres: https://naavik.co/digest/how-niche-subgenres-are-reshaping-the-mobile-puzzle-market/
- [S30] Casual puzzle intelligence report: https://casual-report.tiiny.site/
- [S31] Casual games trends 2026: https://app.cinevva.com/guides/casual-games-trends-2026
- [S32] Deconstructor of Fun, hybrid-casual puzzles: https://www.deconstructoroffun.com/blog/2025/2/3/hybridcasual-puzzles-expanding-the-puzzle-market
- [S33] Deconstructor of Fun, Pixel Flow: https://www.deconstructoroffun.com/blog/2026/2/13/pixel-flow-the-publishers-dream
- [S34] Mobidictum, Wool Sort: https://mobidictum.com/wool-sort-new-trend-screw-puzzle-genre/
- [S35] Trivia apps: https://www.appbrain.com/dev/Trivia+Box/ and https://www.appbrain.com/dev/Trivia+Quizzes/ and https://appgoblin.info/apps/com.xinora.top10
- [S36] Brain Puzzle 2 breakdown: https://gameindustrylibrary.com/topics/puzzle
- [S37] Drop and fruit merge: https://www.ifanzine.com/suika-the-watermelon-game/ and https://appgoblin.info/apps/puzzle.suika.game.high and https://appgoblin.info/apps/com.dropmerge2048.game/keywords/discovery
- [S38] AppMagic top hybrid-casual Q3 2025: https://appmagic.rocks/blog/q3hybrid2025
- [S39] mobilegamer.biz H1 2026 downloads: https://mobilegamer.biz/2026s-top-10-mobile-game-downloads-so-far-free-fire-max-block-blast-roblox-arrows-more/
- [S40] Google Play spam and minimum functionality policy (repetitive content): https://support.google.com/googleplay/android-developer/answer/9899034
- [S42] Nine Men's Morris apps: https://appgoblin.info/apps/com.millgame.alignit and https://appgoblin.info/apps/com.donkeycat.mill
- [S43] Lines 98: https://appgoblin.info/apps/com.colorball.line
- [S44] Cube: https://appgoblin.info/apps/com.manojbhatt.cubiks
- [S45] Maze: https://appgoblin.info/apps/com.leodesol.games.classic.maze.labyrinth and https://appgoblin.info/apps/com.rvappstudios.maze.games.puzzle.mazes.labyrinth
- [S46] Memory pairs: https://appgoblin.info/apps/com.brandedbrothers.memogame
- [S47] Crossy Road: https://www.pocketgamer.biz/crossy-road-celebrates-over-200-million-downloads
- [S48] Stickman Hook: https://madbox.io/story
- [S49] Idle Miner Tycoon: https://kolibrigames.com/press/kolibri-games-hit-title-idle-miner-tycoon-surpasses-150-million-downloads
- [S50] Kingdom Rush: https://appgoblin.info/apps/com.ironhidegames.android.kingdomrush/keywords/ranking
- [S51] Hill Climb Racing: https://fingersoft.com/news/2026/01/15/hill-climb-racing-breaks-a-new-record-2-billion-downloads/
- [S52] App install lookups, 13 to 14 Nov 2026 (AppGoblin and press; app names given in each row)
