package ca.corbett.imageviewer.ui;

import ca.corbett.extras.properties.AbstractProperty;
import ca.corbett.extras.properties.FormFieldGenerationListener;
import ca.corbett.forms.fields.FormField;
import ca.corbett.forms.fields.KeyStrokeField;
import ca.corbett.imageviewer.AppConfig;

/**
 * This class works around a bug in KeyStrokeProperty where all attempts to set
 * a list of reserved keystrokes are ignored. The workaround is to attach a
 * FormFieldGenerationListener to the property, and set the reserved keystrokes
 * list directly on the generated KeyStrokeField.
 * <p>
 * This bug was discovered in swing-extras 2.7, and is being tracked in
 * <a href="https://github.com/scorbo2/swing-extras/issues/322">issue 322</a>.
 * A future release of ImageViewer can remove this workaround once the bug
 * is fixed in swing-extras.
 * </p>
 *
 * @author <a href="https://github.com/scorbo2">scorbo2</a>
 */
public class ReservedKeyStrokeWorkaround implements FormFieldGenerationListener {
    @Override
    public void formFieldGenerated(AbstractProperty property, FormField formField) {
        if (!(formField instanceof KeyStrokeField keyStrokeField)) {
            return;
        }

        keyStrokeField.setReservedKeyStrokes(AppConfig.RESERVED_KEYSTROKES);
    }
}
