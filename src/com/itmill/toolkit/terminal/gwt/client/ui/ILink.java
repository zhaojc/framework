package com.itmill.toolkit.terminal.gwt.client.ui;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.itmill.toolkit.terminal.gwt.client.ApplicationConnection;
import com.itmill.toolkit.terminal.gwt.client.ErrorMessage;
import com.itmill.toolkit.terminal.gwt.client.Paintable;
import com.itmill.toolkit.terminal.gwt.client.UIDL;

public class ILink extends HTML implements Paintable, ClickListener {

    public static final String CLASSNAME = "i-link";

    private static final int BORDER_STYLE_DEFAULT = 0;
    private static final int BORDER_STYLE_MINIMAL = 1;
    private static final int BORDER_STYLE_NONE = 2;

    private String src;

    private String target;

    private int borderStyle = BORDER_STYLE_DEFAULT;

    private boolean enabled;

    private boolean readonly;

    private int width;

    private int height;

    private Element errorIndicatorElement;

    private Element captionElement = DOM.createSpan();

    private ErrorMessage errorMessage;

    private Icon icon;

    public ILink() {
        super();
        DOM.appendChild(getElement(), captionElement);
        addClickListener(this);
        setStyleName(CLASSNAME);
    }

    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {

        // Ensure correct implementation,
        // but don't let container manage caption etc.
        if (client.updateComponent(this, uidl, false)) {
            return;
        }

        enabled = uidl.hasAttribute("disabled") ? false : true;
        readonly = uidl.hasAttribute("readonly") ? true : false;

        if (uidl.hasAttribute("name")) {
            target = uidl.getStringAttribute("name");
        }
        if (uidl.hasAttribute("src")) {
            src = client.translateToolkitUri(uidl.getStringAttribute("src"));
        }

        if (uidl.hasAttribute("border")) {
            if ("none".equals(uidl.getStringAttribute("border"))) {
                borderStyle = BORDER_STYLE_NONE;
            } else {
                borderStyle = BORDER_STYLE_MINIMAL;
            }
        } else {
            borderStyle = BORDER_STYLE_DEFAULT;
        }

        height = uidl.hasAttribute("height") ? uidl.getIntAttribute("height")
                : -1;
        width = uidl.hasAttribute("width") ? uidl.getIntAttribute("width") : -1;

        // Set link caption
        DOM.setInnerText(captionElement, uidl.getStringAttribute("caption"));

        // handle error
        if (uidl.hasAttribute("error")) {
            UIDL errorUidl = uidl.getErrors();
            if (errorIndicatorElement == null) {
                errorIndicatorElement = DOM.createDiv();
                DOM.setElementProperty(errorIndicatorElement, "className",
                        "i-errorindicator");
                DOM.sinkEvents(errorIndicatorElement, Event.MOUSEEVENTS);
                sinkEvents(Event.MOUSEEVENTS);
            }
            DOM.insertChild(getElement(), errorIndicatorElement, 0);
            if (errorMessage == null) {
                errorMessage = new ErrorMessage();
            }
            errorMessage.updateFromUIDL(errorUidl);

        } else if (errorIndicatorElement != null) {
            DOM.setStyleAttribute(errorIndicatorElement, "display", "none");
        }

        if (uidl.hasAttribute("icon")) {
            if (icon == null) {
                icon = new Icon(client);
                DOM.insertChild(getElement(), icon.getElement(), 0);
            }
            icon.setUri(uidl.getStringAttribute("icon"));
        }

        // handle description
        if (uidl.hasAttribute("description")) {
            setTitle(uidl.getStringAttribute("description"));
        }

    }

    public void onClick(Widget sender) {
        if (enabled && !readonly) {
            if (target == null) {
                target = "_self";
            }
            String features;
            switch (borderStyle) {
            case BORDER_STYLE_NONE:
                features = "menubar=no,location=no,status=no";
                break;
            case BORDER_STYLE_MINIMAL:
                features = "menubar=yes,location=no,status=no";
                break;
            default:
                features = "";
                break;
            }

            if (width > 0) {
                features += (features.length() > 0 ? "," : "") + "width="
                        + width;
            }
            if (height > 0) {
                features += (features.length() > 0 ? "," : "") + "height="
                        + height;
            }

            Window.open(src, target, features);
        }
    }

    public void onBrowserEvent(Event event) {
        Element target = DOM.eventGetTarget(event);
        if (errorIndicatorElement != null
                && DOM.compare(target, errorIndicatorElement)) {
            switch (DOM.eventGetType(event)) {
            case Event.ONMOUSEOVER:
                showErrorMessage();
                break;
            case Event.ONMOUSEOUT:
                hideErrorMessage();
                break;
            case Event.ONCLICK:
                ApplicationConnection.getConsole().log(
                        DOM.getInnerHTML(errorMessage.getElement()));
                return;
            default:
                break;
            }
        }
        super.onBrowserEvent(event);
    }

    private void hideErrorMessage() {
        errorMessage.hide();
    }

    private void showErrorMessage() {
        if (errorMessage != null) {
            errorMessage.showAt(errorIndicatorElement);
        }
    }

}
