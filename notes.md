# Java 8 Notes — Collectors & Streams

## Functional Interfaces
Four core ones: Supplier (`() -> T`), Consumer (`T -> void`), Predicate (`T -> boolean`), Function (`T -> R`).
Declare reusable ones as fields at the top of the class (or pass inline) rather than
rewriting the same lambda logic repeatedly.

- `predicateA.and(predicateB)`, `.or(...)`, `.negate()` — combine predicates instead of
  writing one big `&&`/`||` lambda.
- If you already have a `Function<T,R>` variable, pass it directly to `.map()` —
  don't wrap it in `order -> myFunction.apply(order)`, that's redundant indirection.
  `map()` just needs an object of the right type; it doesn't care how you got it.

## Streams: terminal vs intermediate
`collect()` is a **terminal** operation — it's what actually triggers execution.
Intermediate ops (`filter`, `map`, `sorted`, `peek`) just describe a pipeline step;
nothing runs until a terminal op (`collect`, `forEach`, `count`, `anyMatch`, `findFirst`,
etc.) is called.

Reliable test: does the method return `Stream<T>` (or `IntStream`/etc.)? → intermediate.
Anything else (collection, `Optional`, `boolean`, `void`, plain value) → terminal.

`peek()` returns `Stream<T>` so it's intermediate — it does **nothing** without a
terminal op after it, even though it has a side effect (printing). Laziness still applies.
It's meant for debugging, not for real side effects the program depends on, since the
JDK is allowed to skip calls it can prove aren't needed by the terminal op.

## Short-circuiting operations
`anyMatch` / `allMatch` / `noneMatch` / `findFirst` / `findAny` stop as soon as they
have an answer — they don't process the rest of the stream.
`filter().count() > 0` does **not** short-circuit — `count()` must walk the entire
stream to produce an exact total, even if the first element already matched.
Use `anyMatch` (or the others) whenever the real question is "does at least one
exist," not "how many exist."

## groupingBy
Returns a `Map`. Three overloads, increasing complexity:
- `groupingBy(classifier)` → `Map<K, List<T>>` — groups into lists, that's it.
- `groupingBy(classifier, downstreamCollector)` → the one used most often, e.g.
  `groupingBy(Order::getStatus, summingDouble(Order::getAmount))`.
- `groupingBy(classifier, mapSupplier, downstreamCollector)` → rarely needed;
  lets you pick the `Map` implementation (e.g. `TreeMap` for sorted keys,
  `EnumMap` when the key is an enum).

Never returns `null` — worst case (empty input) is an empty `Map`. That's why
wrapping a `groupingBy` result in `Optional` is pointless: a `Map` already has
its own natural "no data" state.

### Downstream collectors used with groupingBy
- `Collectors.summingDouble(fn)` / `counting()` / `averagingDouble(fn)` — reduce
  each group down to **one value**.
- `Collectors.mapping(transformFn, downstream)` — **transform** each element
  first, then collect the transformed values (usually into a `toList()`).
  Sequence per element: **classify → transform → collect the transformed value.**
- `Collectors.partitioningBy(predicate)` — a binary split into
  `Map<Boolean, List<T>>`. Different from `groupingBy`: always exactly two
  buckets (`true`/`false`), keyed by boolean, not by an arbitrary classifier.
- `Collectors.joining(delimiter)` — concatenates a `Stream<String>` into one
  `String`. Only works on strings — map non-string elements to `String` first
  (e.g. `.map(String::valueOf)` or `.map(Order::getId).map(String::valueOf)`).

## map + reduce (used directly on the stream, not a collector)
To sum a field: map to extract the value, then reduce to combine them.
```java
orders.stream()
    .map(Order::getAmount)
    .reduce(0.0, (a, b) -> a + b);
```
- First argument to `reduce` is the **identity** (starting value).
- Second argument is a `BinaryOperator<T>` (takes two `T`s, returns one `T`) —
  the accumulator that combines the running total with each new element.
- Don't try to sum via `forEach` with a mutable local variable — the compiler
  rejects it, since a lambda can only capture effectively-final locals. `reduce`
  exists precisely so you don't need mutation to accumulate a result.

## sorted() + Comparator
`Comparator.comparing(keyExtractor)` builds a full comparator from a field's
natural ordering — no need to write raw `compareTo` logic yourself.
- `.reversed()` — descending order.
- `.thenComparing(otherKeyExtractor)` — tie-breaker, used only when the first
  comparator considers two elements equal.
- Two-arg form `Comparator.comparing(keyExtractor, comparator)` — lets you
  supply a custom rule for the extracted key instead of its natural ordering
  (e.g. `Comparator.comparing(Order::getAmount, Comparator.reverseOrder())`).

**Enum gotcha:** sorting by an enum field's natural order sorts by **declaration
position** (`ordinal()`), not alphabetically. Reordering an enum's declared
constants silently changes sort behavior everywhere that relies on natural
ordering — a real, easy-to-miss bug source. If declaration order isn't
meant to carry sort meaning, use an explicit `Comparator` (e.g. a priority map)
instead of relying on natural enum ordering.
