//----------------------------------------------------------------------------//
//                                                                            //
//                          G l y p h T r a i n e r                           //
//                                                                            //
//  Copyright (C) Herve Bitteur 2000-2007. All rights reserved.               //
//  This software is released under the GNU General Public License.           //
//  Contact author at herve.bitteur@laposte.net to report bugs & suggestions. //
//----------------------------------------------------------------------------//
//
package omr.glyph.ui;

import omr.constant.ConstantManager;

import omr.glyph.GlyphNetwork;
import static omr.glyph.Shape.*;

import omr.plugin.Plugin;
import omr.plugin.PluginType;

import omr.ui.icon.IconManager;
import omr.ui.util.Panel;
import omr.ui.util.UILookAndFeel;

import omr.util.Implement;
import omr.util.Logger;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;

import java.awt.*;
import java.awt.event.*;
import java.util.Observable;

import javax.swing.*;
import javax.swing.event.*;

/**
 * Class <code>GlyphTrainer</code> handles a User Interface dedicated to the
 * training and testing of a glyph evaluator. This class can be launched as a
 * stand-alone program.
 *
 * <p>The frame is divided vertically in 3 parts:
 * <ol>
 * <li>The selection in repository of known glyphs ({@link SelectionPanel})
 * <li>The training of the neural network evaluator ({@link TrainingPanel})
 * <li>The validation of the neural network evaluator ({@link ValidationPanel})
 * </ol>
 *
 * @author Herv&eacute; Bitteur
 * @version $Id$
 */
public class GlyphTrainer
{
    //~ Static fields/initializers ---------------------------------------------

    /** Usual logger utility */
    private static final Logger logger = Logger.getLogger(GlyphTrainer.class);

    /** The single instance of this class */
    private static GlyphTrainer INSTANCE;

    /** To differentiate the exit action, according to the launch context */
    private static boolean standAlone = false;

    /** Standard width for fields/buttons in DLUs */
    private static final String standardWidth = "50dlu";

    /** Title for the frame */
    private static final String frameTitle = "Trainer";

    //~ Instance fields --------------------------------------------------------

    /** Related frame */
    private final JFrame frame;

    /** Panel for selection in repository */
    private final SelectionPanel selectionPanel;

    /** Panel for Neural network training */
    private final NetworkPanel networkPanel;

    /** Panel for Neural network validation */
    private final ValidationPanel validationPanel;

    /** Current task */
    private final Task task = new Task();

    //~ Constructors -----------------------------------------------------------

    //--------------//
    // GlyphTrainer //
    //--------------//
    /**
     * Create an instance of Glyph Trainer (there should be just one)
     */
    private GlyphTrainer ()
    {
        if (standAlone) {
            // UI Look and Feel
            UILookAndFeel.setUI(null);
            JFrame.setDefaultLookAndFeelDecorated(true);
        }

        frame = new JFrame();
        frame.setTitle(frameTitle);

        // Listener on remaining error
        ChangeListener errorListener = new ChangeListener() {
            public void stateChanged (ChangeEvent e)
            {
                frame.setTitle(
                    String.format(
                        "%.5f - %s",
                        networkPanel.getBestError(),
                        frameTitle));
            }
        };

        // Create the three companions
        selectionPanel = new SelectionPanel(task, standardWidth);
        networkPanel = new NetworkPanel(
            task,
            standardWidth,
            errorListener,
            selectionPanel);
        selectionPanel.setTrainingPanel(networkPanel);
        validationPanel = new ValidationPanel(
            task,
            standardWidth,
            GlyphNetwork.getInstance(),
            selectionPanel,
            networkPanel);
        frame.add(createGlobalPanel());

        // Initial state
        task.setActivity(Task.Activity.INACTIVE);

        // Specific ending if stand alone
        if (standAlone) {
            frame.addWindowListener(
                new WindowAdapter() {
                        public void windowClosing (WindowEvent e)
                        {
                            // Store latest constant values
                            ConstantManager.storeResource();

                            // That's all folks !
                            System.exit(0);
                        }
                    });
        }

        // Differ realization
        EventQueue.invokeLater(
            new Runnable() {
                    public void run ()
                    {
                        frame.pack();
                        frame.setVisible(true);
                    }
                });
    }

    //~ Methods ----------------------------------------------------------------

    //--------//
    // launch //
    //--------//
    /**
     * (Re)activate the trainer tool
     */
    public static void launch ()
    {
        getInstance().frame.setVisible(true);
        getInstance().frame.toFront();
    }

    //------//
    // main //
    //------//
    /**
     * Just to allow stand-alone testing of this class
     *
     * @param args not used
     */
    public static void main (String... args)
    {
        standAlone = true;
        getInstance();
    }

    //-------------//
    // getInstance //
    //-------------//
    private static GlyphTrainer getInstance ()
    {
        if (INSTANCE == null) {
            INSTANCE = new GlyphTrainer();
        }

        return INSTANCE;
    }

    //-------------------//
    // createGlobalPanel //
    //-------------------//
    private JPanel createGlobalPanel ()
    {
        final String    panelInterline = Panel.getPanelInterline();
        FormLayout      layout = new FormLayout(
            "pref",
            "pref," + panelInterline + "," + "pref," + panelInterline + "," +
            "pref");

        CellConstraints cst = new CellConstraints();
        PanelBuilder    builder = new PanelBuilder(layout, new Panel());
        builder.setDefaultDialogBorder();

        int r = 1; // --------------------------------
        builder.add(selectionPanel.getComponent(), cst.xy(1, r));

        r += 2; // --------------------------------
        builder.add(networkPanel.getComponent(), cst.xy(1, r));

        r += 2; // --------------------------------
        builder.add(validationPanel.getComponent(), cst.xy(1, r));

        return builder.getPanel();
    }

    //~ Inner Classes ----------------------------------------------------------

    //---------------//
    // TrainerAction //
    //---------------//
    /**
     * Class <code>TrainerAction</code> launches the window dedicated to the
     * training of the neural network
     */
    @Plugin(type = PluginType.TRAINING)
    public static class TrainerAction
        extends AbstractAction
    {
        @Implement(ActionListener.class)
        public void actionPerformed (ActionEvent e)
        {
            GlyphTrainer.launch();
        }
    }

    //------//
    // Task //
    //------//
    /**
     * Class <code>Task</code> handles which activity is currently being carried
     * out, only one being current at any time.
     */
    static class Task
        extends Observable
    {
        /**
         * Enum <code>Activity</code> defines the possible activities in
         * training.
         */
        static enum Activity {
            /** No ongoing activity */
            INACTIVE,
            /** Selecting glyph to build a population for training */
            SELECTING, 
            /** Using the population to train the evaluator */
            TRAINING;
        }

        /** Current activity */
        private Activity activity = Activity.INACTIVE;

        //-------------//
        // getActivity //
        //-------------//
        /**
         * Report the current training activity
         *
         * @return current activity
         */
        public Activity getActivity ()
        {
            return activity;
        }

        //-------------//
        // setActivity //
        //-------------//
        /**
         * Assign a new current activity and notify all observers
         *
         * @param activity
         */
        public void setActivity (Activity activity)
        {
            this.activity = activity;
            setChanged();
            notifyObservers();
        }
    }
}
