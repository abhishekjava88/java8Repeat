# Java 8 Notes — Optional

## When Optional is the right tool
Use it as a **return type** for a method whose result might legitimately not
exist — e.g. `findOrderById` using `filter().findFirst()`, which already
naturally returns `Optional<T>`. The whole point: the return type itself
forces the *caller* to acknowledge "this might be empty" instead of silently
returning `null` and risking a `NullPointerException` somewhere downstream.

## Chaining instead of isPresent()
Treat `Optional` like a mini-stream — `map()` transforms the value only if
present, `orElse()`/`orElseThrow()` handle the empty case:
```java
orders.stream().filter(...).findFirst()
    .map(Order::getAmount)
    .orElse(defaultValue);
```
Avoid manually checking `isPresent()` then calling `.get()` — that defeats
the point of the chainable API.

## orElseThrow — fail loudly, with a specific exception
`.orElseThrow()` with **no argument** throws a generic `NoSuchElementException`
— technically valid but not useful, since a caller can't distinguish it from
any other `NoSuchElementException` elsewhere in a large codebase.
Prefer the `Supplier` form with a custom exception:
```java
.orElseThrow(() -> new OrderNotFoundException("No order with id " + id));
```

## orElse vs orElseGet — eager vs lazy, a real gotcha
Both "supply a fallback" but differ in **when** the fallback is computed:
- `orElse(x)` — `x` is a plain value. Java evaluates method arguments before
  the call happens, so `x` is computed **every time**, even when the
  `Optional` already has a value and the fallback is never actually used.
- `orElseGet(() -> x)` — `x` is wrapped in a `Supplier`. It's only invoked
  if the `Optional` is genuinely empty.

This is invisible when the fallback is a cheap constant (`orElse(0.0)`).
It matters when the fallback does real work (a DB call, an API call) — that
work gets wasted every time with `orElse`, only when needed with `orElseGet`.
**Only observable if you test with an ID that actually exists** — testing
with a missing ID means the fallback is needed either way, so eager vs lazy
looks identical in that test. Learned this the hard way — first attempt at
this test used a nonexistent ID and couldn't actually show the difference.

## Anti-patterns — Optional as a field or parameter
`Optional`'s value only exists at a **method's return boundary**, signaling
"caller, you must handle absence." A field or parameter doesn't have that
ambiguity — whoever's constructing the object or calling the method already
knows whether they have a value. Wrapping it in `Optional` just adds
wrap/unwrap ceremony with no real safety gained ("duplicate effort").
Fields have an extra cost too: `Optional` isn't designed for that kind of
use and complicates JSON serialization (relevant for JPA entities / Kafka
message payloads).

## Anti-pattern — wrapping a collection
Never `Optional<List<T>>` or `Optional<Map<K,V>>`. A collection already has
its own natural "no data" state — an empty list/map — so wrapping it in
`Optional` creates two ways to express the same absence
(`Optional.empty()` vs `Optional.of(List.of())`), forcing callers to unwrap
for no benefit. `Collectors.groupingBy` etc. never return `null` — worst
case is an empty `Map`, which is exactly why this never needed `Optional`
in the first place.

## Supplier vs Executable vs Function — don't confuse the lambda shape with the interface
Any `() -> something` lambda's *actual type* is determined by the target
type at the call site, not by how it looks:
- `Supplier<T>` — `() -> T`, used by `orElseGet`. Produces a value.
- `Executable` (JUnit-specific, not core Java) — `() -> void, throws Throwable`,
  used by `assertThrows`. Runs code and lets the exception get caught; return
  value (if any) is discarded.
- `Function<K,V>` — `computeIfAbsent` uses this, not `Supplier`, because the
  computation is allowed to use the *key* to produce the value.

Every lambda compiles down to a real object implementing the target
interface's single method — same mechanism whether it's a `Predicate`, a
`Supplier`, or an `Executable`. The syntax looks the same; the interface it
becomes is entirely decided by what the receiving method's parameter
declares.


Good pushback — but I think there's a slight misconception buried in it worth untangling: orElse/orElseGet aren't a separate feature from "caller checks and decides" — they are the caller deciding. They're not decoration on top of the real mechanism; they're one of the main mechanisms.

Let's compare the two ways of actually "deciding what to do":

java
// Manual check — isPresent()/get()
Optional<Order> result = findOrderById(orders, id);
double amount;
if (result.isPresent()) {
amount = result.get().getAmount();
} else {
amount = 0.0;
}
java
// orElse chaining
double amount = findOrderById(orders, id).map(Order::getAmount).orElse(0.0);