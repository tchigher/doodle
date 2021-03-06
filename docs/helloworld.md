# Hello World
-------------

The classic "Hello World" app in Doodle. This version displays "Hello, world!" in the top-left of the page.

```kotlin
class HelloWorld(display: Display): Application {
    init {
        display.children += object: View() {
            init { size = display.size }

            override fun render(canvas: Canvas) {
                canvas.text(
                    "Hello, world!", 
                    at = Origin, 
                    fill = ColorFill(Black)
                )
            }
        }
    }

    override fun shutdown() {}
}

fun main() {
    application {
        HelloWorld(display = instance())
    }
}
```

```doodle
{
    "border": false,
    "height": "120px",
    "run"   : "DocApps.helloWorld"
}
```

This is an example of a top-level [**Application**](applications.md).