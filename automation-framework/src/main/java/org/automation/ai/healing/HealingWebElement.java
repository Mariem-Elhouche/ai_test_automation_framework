package org.automation.ai.healing;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;

import java.util.ArrayList;
import java.util.List;

public class HealingWebElement implements WebElement, WrapsElement {

    private final WebDriver rootDriver;
    private final WebElement delegate;
    private final HealingLocatorResolver resolver;

    public HealingWebElement(WebDriver rootDriver, WebElement delegate, HealingLocatorResolver resolver) {
        this.rootDriver = rootDriver;
        this.delegate = delegate;
        this.resolver = resolver;
    }

    @Override
    public void click() {
        delegate.click();
    }

    @Override
    public void submit() {
        delegate.submit();
    }

    @Override
    public void sendKeys(CharSequence... keysToSend) {
        delegate.sendKeys(keysToSend);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public String getTagName() {
        return delegate.getTagName();
    }

    @Override
    public String getAttribute(String name) {
        return delegate.getAttribute(name);
    }

    @Override
    public boolean isSelected() {
        return delegate.isSelected();
    }

    @Override
    public boolean isEnabled() {
        return delegate.isEnabled();
    }

    @Override
    public String getText() {
        return delegate.getText();
    }

    @Override
    public List<WebElement> findElements(By by) {
        By preferredLocator = resolver.getPreferredLocator(rootDriver, delegate, by);

        List<WebElement> found = delegate.findElements(preferredLocator);
        if (found.isEmpty() && !resolver.isSameLocator(preferredLocator, by)) {
            found = delegate.findElements(by);
        }

        if (found.isEmpty()) {
            By healedLocator = resolver.resolveLocator(rootDriver, delegate, by, new NoSuchElementException("No elements found for " + by));
            if (healedLocator != null) {
                found = delegate.findElements(healedLocator);
                if (!found.isEmpty()) {
                    resolver.captureSnapshot(rootDriver, delegate, by, found.get(0));
                }
            }
        } else {
            resolver.captureSnapshot(rootDriver, delegate, by, found.get(0));
        }

        List<WebElement> wrapped = new ArrayList<>(found.size());
        for (WebElement element : found) {
            wrapped.add(new HealingWebElement(rootDriver, element, resolver));
        }
        return wrapped;
    }

    @Override
    public WebElement findElement(By by) {
        By preferredLocator = resolver.getPreferredLocator(rootDriver, delegate, by);

        try {
            WebElement found = delegate.findElement(preferredLocator);
            resolver.captureSnapshot(rootDriver, delegate, by, found);
            return new HealingWebElement(rootDriver, found, resolver);
        } catch (NoSuchElementException | StaleElementReferenceException firstFailure) {
            if (!resolver.isSameLocator(preferredLocator, by)) {
                try {
                    WebElement found = delegate.findElement(by);
                    resolver.captureSnapshot(rootDriver, delegate, by, found);
                    return new HealingWebElement(rootDriver, found, resolver);
                } catch (NoSuchElementException | StaleElementReferenceException ignored) {
                    // Continue toward healing.
                }
            }

            By healedLocator = resolver.resolveLocator(rootDriver, delegate, by, firstFailure);
            if (healedLocator == null) {
                throw firstFailure;
            }

            WebElement found = delegate.findElement(healedLocator);
            resolver.captureSnapshot(rootDriver, delegate, by, found);
            return new HealingWebElement(rootDriver, found, resolver);
        }
    }

    @Override
    public boolean isDisplayed() {
        return delegate.isDisplayed();
    }

    @Override
    public Point getLocation() {
        return delegate.getLocation();
    }

    @Override
    public Dimension getSize() {
        return delegate.getSize();
    }

    @Override
    public Rectangle getRect() {
        return delegate.getRect();
    }

    @Override
    public String getCssValue(String propertyName) {
        return delegate.getCssValue(propertyName);
    }

    @Override
    public String getDomAttribute(String name) {
        return delegate.getDomAttribute(name);
    }

    @Override
    public String getDomProperty(String name) {
        return delegate.getDomProperty(name);
    }

    @Override
    public SearchContext getShadowRoot() {
        return delegate.getShadowRoot();
    }

    @Override
    public String getAriaRole() {
        return delegate.getAriaRole();
    }

    @Override
    public String getAccessibleName() {
        return delegate.getAccessibleName();
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> target) {
        return delegate.getScreenshotAs(target);
    }

    @Override
    public WebElement getWrappedElement() {
        return delegate;
    }
}
