package org.automation.ai.healing;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.WrapsElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HealingWebDriver implements WebDriver, JavascriptExecutor, TakesScreenshot, WrapsDriver {

    private final WebDriver delegate;
    private final HealingLocatorResolver resolver;

    public HealingWebDriver(WebDriver delegate) {
        this(delegate, new HealingLocatorResolver());
    }

    public HealingWebDriver(WebDriver delegate, HealingLocatorResolver resolver) {
        this.delegate = delegate;
        this.resolver = resolver;
    }

    @Override
    public void get(String url) {
        delegate.get(url);
    }

    @Override
    public String getCurrentUrl() {
        return delegate.getCurrentUrl();
    }

    @Override
    public String getTitle() {
        return delegate.getTitle();
    }

    @Override
    public List<WebElement> findElements(By by) {
        By preferredLocator = resolver.getPreferredLocator(delegate, delegate, by);

        List<WebElement> found = delegate.findElements(preferredLocator);
        if (found.isEmpty() && !resolver.isSameLocator(preferredLocator, by)) {
            found = delegate.findElements(by);
        }

        if (found.isEmpty()) {
            By healedLocator = resolver.resolveLocator(delegate, delegate, by, new NoSuchElementException("No elements found for " + by));
            if (healedLocator != null) {
                found = delegate.findElements(healedLocator);
                if (!found.isEmpty()) {
                    resolver.captureSnapshot(delegate, delegate, by, found.get(0));
                }
            }
        } else {
            resolver.captureSnapshot(delegate, delegate, by, found.get(0));
        }

        List<WebElement> wrapped = new ArrayList<>(found.size());
        for (WebElement element : found) {
            wrapped.add(new HealingWebElement(this, element, resolver));
        }
        return wrapped;
    }

    @Override
    public WebElement findElement(By by) {
        By preferredLocator = resolver.getPreferredLocator(delegate, delegate, by);

        try {
            WebElement found = delegate.findElement(preferredLocator);
            resolver.captureSnapshot(delegate, delegate, by, found);
            return new HealingWebElement(this, found, resolver);
        } catch (NoSuchElementException | StaleElementReferenceException firstFailure) {
            if (!resolver.isSameLocator(preferredLocator, by)) {
                try {
                    WebElement found = delegate.findElement(by);
                    resolver.captureSnapshot(delegate, delegate, by, found);
                    return new HealingWebElement(this, found, resolver);
                } catch (NoSuchElementException | StaleElementReferenceException ignored) {
                    // Continue toward healing.
                }
            }

            By healedLocator = resolver.resolveLocator(delegate, delegate, by, firstFailure);
            if (healedLocator == null) {
                throw firstFailure;
            }

            WebElement found = delegate.findElement(healedLocator);
            resolver.captureSnapshot(delegate, delegate, by, found);
            return new HealingWebElement(this, found, resolver);
        }
    }

    @Override
    public String getPageSource() {
        return delegate.getPageSource();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public void quit() {
        delegate.quit();
    }

    @Override
    public Set<String> getWindowHandles() {
        return delegate.getWindowHandles();
    }

    @Override
    public String getWindowHandle() {
        return delegate.getWindowHandle();
    }

    @Override
    public TargetLocator switchTo() {
        TargetLocator original = delegate.switchTo();
        return new TargetLocator() {
            @Override
            public WebDriver frame(int index) {
                original.frame(index);
                return HealingWebDriver.this;
            }

            @Override
            public WebDriver frame(String nameOrId) {
                original.frame(nameOrId);
                return HealingWebDriver.this;
            }

            @Override
            public WebDriver frame(WebElement frameElement) {
                original.frame(unwrapElement(frameElement));
                return HealingWebDriver.this;
            }

            @Override
            public WebDriver parentFrame() {
                original.parentFrame();
                return HealingWebDriver.this;
            }

            @Override
            public WebDriver window(String nameOrHandle) {
                original.window(nameOrHandle);
                return HealingWebDriver.this;
            }

            @Override
            public WebDriver defaultContent() {
                original.defaultContent();
                return HealingWebDriver.this;
            }

            @Override
            public WebElement activeElement() {
                return new HealingWebElement(HealingWebDriver.this, original.activeElement(), resolver);
            }

            @Override
            public Alert alert() {
                return original.alert();
            }

            @Override
            public WebDriver newWindow(WindowType typeHint) {
                original.newWindow(typeHint);
                return HealingWebDriver.this;
            }
        };
    }

    @Override
    public Navigation navigate() {
        return delegate.navigate();
    }

    @Override
    public Options manage() {
        return delegate.manage();
    }

    @Override
    public Object executeScript(String script, Object... args) {
        return ((JavascriptExecutor) delegate).executeScript(script, unwrapScriptArgs(args));
    }

    @Override
    public Object executeAsyncScript(String script, Object... args) {
        return ((JavascriptExecutor) delegate).executeAsyncScript(script, unwrapScriptArgs(args));
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> target) {
        return ((TakesScreenshot) delegate).getScreenshotAs(target);
    }

    @Override
    public WebDriver getWrappedDriver() {
        return delegate;
    }

    private Object[] unwrapScriptArgs(Object[] args) {
        Object[] unwrapped = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            unwrapped[i] = unwrapScriptArg(args[i]);
        }
        return unwrapped;
    }

    private Object unwrapScriptArg(Object arg) {
        if (arg instanceof WrapsElement wrapsElement) {
            return wrapsElement.getWrappedElement();
        }
        if (arg instanceof Object[] array) {
            Object[] unwrapped = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                unwrapped[i] = unwrapScriptArg(array[i]);
            }
            return unwrapped;
        }
        if (arg instanceof List<?> list) {
            List<Object> unwrapped = new ArrayList<>(list.size());
            for (Object value : list) {
                unwrapped.add(unwrapScriptArg(value));
            }
            return unwrapped;
        }
        if (arg instanceof Collection<?> collection) {
            List<Object> unwrapped = new ArrayList<>(collection.size());
            for (Object value : collection) {
                unwrapped.add(unwrapScriptArg(value));
            }
            return unwrapped;
        }
        if (arg instanceof Map<?, ?> map) {
            return map;
        }
        return arg;
    }

    private WebElement unwrapElement(WebElement element) {
        if (element instanceof WrapsElement wrapsElement) {
            return wrapsElement.getWrappedElement();
        }
        return element;
    }
}
