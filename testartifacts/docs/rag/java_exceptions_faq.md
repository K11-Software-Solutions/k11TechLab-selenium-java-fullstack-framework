# Java Exceptions FAQ for RAG Testing

## How do I handle NoSuchElementException?

`NoSuchElementException` is thrown when an element is not found, typically in Selenium or Java collections. To handle it:

- **For Selenium:**
  ```java
  try {
      WebElement element = driver.findElement(By.id("myId"));
      // use element
  } catch (NoSuchElementException e) {
      // handle missing element, e.g., log or fallback
      System.out.println("Element not found!");
  }
  ```
- **For Java Collections:**
  ```java
  Iterator<String> it = list.iterator();
  try {
      while (true) {
          String value = it.next();
          // process value
      }
  } catch (NoSuchElementException e) {
      // end of iteration
  }
  ```

**Best Practice:** Always check for element existence or use `hasNext()` with iterators to avoid this exception.

---

## How do I handle NullPointerException?

A `NullPointerException` occurs when you try to use a reference that points to no location in memory (null). To handle it:

- Check for null before using an object:
  ```java
  if (myObject != null) {
      myObject.doSomething();
  }
  ```
- Use `Optional` for safer null handling:
  ```java
  Optional<String> value = Optional.ofNullable(possibleNull);
  value.ifPresent(v -> System.out.println(v));
  ```

**Best Practice:** Initialize variables, use null checks, and prefer `Optional` where possible.

---

## How do I handle TimeoutException in Selenium?

A `TimeoutException` is thrown when a command does not complete in the expected time. To handle it:

```java
try {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("myId")));
} catch (TimeoutException e) {
    // handle timeout, e.g., log or retry
    System.out.println("Element not visible in time!");
}
```

**Best Practice:** Use explicit waits and handle timeouts gracefully, possibly with retries or fallback logic.

---

## How do I handle StaleElementReferenceException in Selenium?

A `StaleElementReferenceException` occurs when the referenced element is no longer attached to the DOM. To handle it:

```java
try {
    element.click();
} catch (StaleElementReferenceException e) {
    // re-locate the element and retry
    element = driver.findElement(By.id("myId"));
    element.click();
}
```

**Best Practice:** Always re-find elements after page updates or DOM changes.
