# UI Test Plan

This plan defines end-to-end tests for the Grug command-line interface. Tests use Java 25 and are run from the repository root with `./gradlew run` (or `./gradlew.bat run` on Windows).

Each test case is isolated in a fresh application process. The `commands` and `expected output` entries are positional: each command is checked against the output produced for that command. Startup text is excluded from the per-command output blocks; the test session should still record the complete console transcript, including startup text.

## Test case template

Copy this template when adding a case. Keep the number and order of expected-output blocks equal to the number and order of commands.

### `<case name>`

**Aim:** <what behavior this test verifies>

**Inputs (commands):**

1. `<command 1>`
2. `<command 2>`

**Expected output:**

#### Command 1

```text
<response printed for command 1>
```

#### Command 2

```text
<response printed for command 2>
```

## Planned test cases

### Quit immediately

**Aim:** Verify that the application accepts the quit command and terminates.

**Inputs (commands):**

1. `bye`

**Expected output:**

#### Command 1

```text
Unga. Bye. さよなら
____________________________________________________________
```

### Add and list a todo

**Aim:** Verify that a todo can be added and then appears in the task list.

**Inputs (commands):**

1. `todo buy milk`
2. `list`
3. `bye`

**Expected output:**

#### Command 1

```text
added: [T][ ] buy milk
____________________________________________________________
```

#### Command 2

```text
1. [T][ ] buy milk
____________________________________________________________
```

#### Command 3

```text
Unga. Bye. さよなら
____________________________________________________________
```

### Empty input and unknown command

**Aim:** Verify that blank input is ignored, unknown commands are rejected, and the application remains usable afterward.

**Inputs (commands):**

1. `<blank line>`
2. `wat`
3. `list`
4. `bye now`
5. `bye`

**Expected output:**

#### Command 1

```text
____________________________________________________________
```

#### Command 2

```text
Unknown command `wat`
____________________________________________________________
```

#### Command 3

```text
No tasks added.
____________________________________________________________
```

#### Command 4

```text
Invalid usage. Proper usage: bye (with no arguments)
____________________________________________________________
```

#### Command 5

```text
Unga. Bye. さよなら
____________________________________________________________
```

### Interleaved valid and invalid commands preserve state

**Aim:** Verify that invalid commands between valid commands do not add, remove, or modify tasks.

**Inputs (commands):**

1. `todo buy milk`
2. `todo`
3. `list`
4. `deadline submit report /by`
5. `deadline submit report /by Friday`
6. `event project meeting /from 2pm`
7. `list`
8. `mark abc`
9. `mark 1`
10. `list`
11. `unmark 1`
12. `event project meeting /from 2pm /to 4pm`
13. `list`
14. `bye`

**Expected output:**

#### Command 1

```text
added: [T][ ] buy milk
____________________________________________________________
```

#### Command 2

```text
Invalid usage. Proper usage: todo <details>
____________________________________________________________
```

#### Command 3

```text
1. [T][ ] buy milk
____________________________________________________________
```

#### Command 4

```text
Invalid usage. Proper usage: deadline <details> /by <deadline>
____________________________________________________________
```

#### Command 5

```text
added: [D][ ] submit report (by: Friday)
____________________________________________________________
```

#### Command 6

```text
Invalid usage. Proper usage: event <details> /from <from> /to <to>
____________________________________________________________
```

#### Command 7

```text
1. [T][ ] buy milk
2. [D][ ] submit report (by: Friday)
____________________________________________________________
```

#### Command 8

```text
Invalid argument(s) for `mark <tasknum>`: tasknum must be an integer
____________________________________________________________
```

#### Command 9

```text
Updated task 1: [T][X] buy milk
____________________________________________________________
```

#### Command 10

```text
1. [T][X] buy milk
2. [D][ ] submit report (by: Friday)
____________________________________________________________
```

#### Command 11

```text
Updated task 1: [T][ ] buy milk
____________________________________________________________
```

#### Command 12

```text
added: [E][ ] project meeting (from: 2pm | to: 4pm)
____________________________________________________________
```

#### Command 13

```text
1. [T][ ] buy milk
2. [D][ ] submit report (by: Friday)
3. [E][ ] project meeting (from: 2pm | to: 4pm)
____________________________________________________________
```

#### Command 14

```text
Unga. Bye. さよなら
____________________________________________________________
```

### Invalid task operations

**Aim:** Verify that invalid task numbers and malformed task operations are reported without changing existing tasks.

**Inputs (commands):**

1. `todo read book`
2. `mark 0`
3. `mark -1`
4. `mark 2`
5. `list`
6. `unmark 2 extra`
7. `unmark nope`
8. `list`
9. `bye`

**Expected output:**

#### Command 1

```text
added: [T][ ] read book
____________________________________________________________
```

#### Command 2

```text
Can't find task number 0
____________________________________________________________
```

#### Command 3

```text
Can't find task number -1
____________________________________________________________
```

#### Command 4

```text
Can't find task number 2
____________________________________________________________
```

#### Command 5

```text
1. [T][ ] read book
____________________________________________________________
```

#### Command 6

```text
Invalid argument(s) for `unmark <tasknum>`: must provide exactly 1 integer tasknum
____________________________________________________________
```

#### Command 7

```text
Invalid argument(s) for `unmark <tasknum>`: tasknum must be an integer
____________________________________________________________
```

#### Command 8

```text
1. [T][ ] read book
____________________________________________________________
```

#### Command 9

```text
Unga. Bye. さよなら
____________________________________________________________
```
