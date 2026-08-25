# Multi-dimensional Arrays

Java has no true multi-dimensional arrays — it has **arrays of arrays**, which turn out to be more flexible. A "2D array" `int[][]` is a one-dimensional array whose elements are each references to `int[]` rows. Keeping that model in mind makes every rule in this lesson predictable.

---

## Declaring and Creating

```java
int[][] grid = new int[3][4];      // 3 rows, each an int[4], all zeros

grid[0][2] = 7;                    // row 0, column 2
int val = grid[2][3];              // last row, last column
```

*`new int[3][4]` allocates one outer array of 3 references, plus three 4-element rows.*

The first index selects the **row** (which inner array), the second the **column** (position within it). Reading `grid[0][2]` is genuinely two steps: fetch the array at `grid[0]`, then index into it — and each step is bounds-checked separately.

Literal initialization nests braces, one inner pair per row:

```java
int[][] board = {
    {1, 2, 3},
    {4, 5, 6},
    {7, 8, 9}
};
String[][] seating = new String[20][6];   // e.g., 20 rows of 6 seats, all null
```

*Nested initializers read exactly like the grid they define.*

Higher dimensions extend the same way (`int[][][] cube = new int[10][10][10];`) and remain arrays of arrays of arrays — rare in application code, routine in scientific and game-board domains.

---

## Lengths in Two Dimensions

`length` follows the arrays-of-arrays model: the outer array's `length` is the row count; each row reports its own:

```java
grid.length          // 3   — number of rows
grid[0].length       // 4   — columns in row 0
```

*Two different questions: how many rows, and how long is a particular row.*

There is no `grid.length[1]` or combined size — every row is an independent array and must be asked directly. This matters immediately for iteration.

---

## Iterating a Grid

Nested loops, one per dimension — the standard shape uses `grid.length` for the outer bound and `grid[row].length` (not a repeated constant) for the inner:

```java
for (int row = 0; row < grid.length; row++) {
    for (int col = 0; col < grid[row].length; col++) {
        System.out.print(grid[row][col] + " ");
    }
    System.out.println();                    // end of a row
}
```

*Row-by-row traversal; the inner bound asks each row for its own length.*

The for-each version drops the indexes when positions aren't needed — the outer loop variable is a whole row:

```java
for (int[] row : grid) {
    for (int cell : row) {
        sum += cell;
    }
}
```

*For-each over a 2D array: the elements of `grid` are `int[]` rows.*

And printing gets its own utility — `Arrays.toString` on a 2D array prints reference stamps for each row, so nested arrays use **`Arrays.deepToString(grid)`**, which recurses properly: `[[1, 2, 3], [4, 5, 6], [7, 8, 9]]`.

---

## Jagged Arrays

Because rows are independent arrays, they don't have to be the same length. Allocating only the first dimension leaves the rows `null`, to be created individually:

```java
int[][] triangle = new int[4][];      // 4 rows, none created yet

for (int i = 0; i < triangle.length; i++) {
    triangle[i] = new int[i + 1];     // row i has i+1 cells
}
// triangle: [0], [0,0], [0,0,0], [0,0,0,0]

int[][] jagged = {
    {1},
    {2, 3, 4, 5},
    {6, 7}
};
```

*A **jagged array**: each row sized on its own — impossible in languages with true 2D arrays.*

Jagged shapes are natural for real data — per-month daily readings, per-team rosters. They're also why disciplined code uses `grid[row].length` as the inner loop bound: it's correct for rectangular *and* jagged arrays, while a hard-coded column count breaks the moment rows vary. Two things to stay alert to: an unassigned row is `null` (indexing it throws a `NullPointerException`, not an index exception), and copying the outer array (`grid.clone()`) copies only row *references* — a true deep copy means copying each row.
