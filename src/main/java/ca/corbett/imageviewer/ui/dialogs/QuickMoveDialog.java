package ca.corbett.imageviewer.ui.dialogs;

import ca.corbett.extras.MessageUtil;
import ca.corbett.imageviewer.QuickMoveManager;
import ca.corbett.imageviewer.extensions.ImageViewerExtensionManager;
import ca.corbett.imageviewer.ui.MainWindow;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.BevelBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.logging.Logger;

/**
 * Provides a way to view or edit the quick move destination tree.
 *
 * @author scorbo2
 * @since ImageViewer 2.0 (extracted from the old PreferencesDialog)
 */
public class QuickMoveDialog extends JDialog {

    private static final Logger logger = Logger.getLogger(QuickMoveDialog.class.getName());
    private MessageUtil messageUtil;
    private static JTree quickMoveTree;
    private static QuickMoveManager.TreeNode rootNode; // keep this around between instances
    private static boolean isLoaded = false;

    public QuickMoveDialog() {
        super(MainWindow.getInstance(), "Quick Move destinations", true);
        setSize(400, 360);
        setMinimumSize(new Dimension(400, 360));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        initComponents();
    }

    /**
     * Overridden to update our position if the main window moves.
     *
     * @param visible Whether to show or hide the form.
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            setLocationRelativeTo(MainWindow.getInstance());
            if (!isLoaded) {
                reloadQuickMoveTree();
                isLoaded = true;
            }
            else if (getSelectedNode() != null) {
                quickMoveTree.scrollPathToVisible(new TreePath(getSelectedNode().getPath()));
            }

        }
        super.setVisible(visible);
    }

    /**
     * Returns the currently selected tree node, if any. Note that the root node does
     * not count as a selectable node for this purpose - if the root node is currently
     * selected, then null is returned.
     *
     * @return The currently selected node, or null if no selection.
     */
    public static QuickMoveManager.TreeNode getSelectedNode() {
        if (quickMoveTree.getSelectionPath() == null) {
            return null;
        }
        QuickMoveManager.TreeNode selectedNode;
        selectedNode = (QuickMoveManager.TreeNode)quickMoveTree.getSelectionPath().getLastPathComponent();
        if (selectedNode == null || selectedNode == rootNode) {
            return null;
        }

        return selectedNode;
    }

    /**
     * Reloads the quick move tree, replacing any changes that have been made in this dialog.
     */
    public void reloadQuickMoveTree() {
        rootNode = QuickMoveManager.getInstance().loadQuickMoveTree();
        DefaultTreeModel model = (DefaultTreeModel)quickMoveTree.getModel();
        model.setRoot(rootNode);
        model.reload(rootNode);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        add(buildQuickMoveOptionsPanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);
    }

    /**
     * Internal utility method to build out the quick move options panel.
     *
     * @return A JPanel containing quick move options controls.
     */
    private JPanel buildQuickMoveOptionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        if (rootNode == null) {
            rootNode = QuickMoveManager.getInstance().new TreeNode(null, "Quick Move destinations");
        }
        quickMoveTree = new JTree(rootNode);
        quickMoveTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(quickMoveTree);
        //scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        constraints.insets = new Insets(4, 10, 4, 0);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.gridheight = GridBagConstraints.REMAINDER;
        panel.add(scrollPane, constraints);

        JButton button = new JButton("Add...");
        final int BUTTON_WIDTH = 110;
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, 26));
        button.setMinimumSize(new Dimension(BUTTON_WIDTH, 26));
        constraints = new GridBagConstraints();
        constraints.insets = new Insets(4, 4, 0, 10);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleAddQuickMove();
            }

        });
        panel.add(button, constraints);

        button = new JButton("Remove");
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, 26));
        button.setMinimumSize(new Dimension(BUTTON_WIDTH, 26));
        constraints.insets = new Insets(0, 4, 0, 10);
        constraints.gridy = 1;
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRemoveQuickMove();
            }

        });
        panel.add(button, constraints);

        button = new JButton("Edit");
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, 26));
        button.setMinimumSize(new Dimension(BUTTON_WIDTH, 26));
        constraints.gridy = 2;
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleEditQuickMove();
            }

        });
        panel.add(button, constraints);

        button = new JButton("Move up");
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, 26));
        button.setMinimumSize(new Dimension(BUTTON_WIDTH, 26));
        constraints.gridy = 3;
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleMoveQuickMoveUp();
            }

        });
        panel.add(button, constraints);

        button = new JButton("Move down");
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, 26));
        button.setMinimumSize(new Dimension(BUTTON_WIDTH, 26));
        constraints.gridy = 4;
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleMoveQuickMoveDown();
            }

        });
        panel.add(button, constraints);

        int gridY = 5;
        List<AbstractAction> actionList = ImageViewerExtensionManager.getInstance().getQuickMoveDialogActions();
        for (AbstractAction action : actionList) {
            button = new JButton(action);
            button.setPreferredSize(new Dimension(BUTTON_WIDTH, 26));
            button.setMinimumSize(new Dimension(BUTTON_WIDTH, 26));
            constraints.gridy = gridY;
            panel.add(button, constraints);
            gridY++;
        }

        JLabel dummy = new JLabel("");
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = gridY;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 0.5;
        panel.add(dummy, constraints);

        return panel;
    }

    /**
     * Builds the button panel for the bottom of the form (ok / cancel).
     *
     * @return The Button panel for the bottom of the form.
     */
    private JPanel buildButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton okButton = new JButton("OK");
        okButton.setPreferredSize(new Dimension(80, 28));
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                QuickMoveManager.getInstance().saveQuickMoveTree(rootNode);
                MainWindow.getInstance().rebuildQuickMoveMenus();
                ImageViewerExtensionManager.getInstance().quickMoveTreeChanged();
                dispose();
            }

        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(80, 28));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reloadQuickMoveTree();
                dispose();
            }

        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    /**
     * Prompts the user to add a new Quick Move destination.
     */
    private void handleAddQuickMove() {
        // The parent of this new node will either be the root node or whatever node was selected:
        QuickMoveManager.TreeNode parentNode = rootNode;
        if (quickMoveTree.getSelectionPath() != null) {
            parentNode = (QuickMoveManager.TreeNode)quickMoveTree.getSelectionPath().getLastPathComponent();
        }

        // Prompt the user for new destination details:
        QuickMoveAddDialog dialog = new QuickMoveAddDialog(this);
        dialog.setVisible(true);
        QuickMoveManager.TreeNode newNode = dialog.getResult();
        if (newNode != null) {
            // Weird case: if you add a new child node to a node that previously had no
            // children, then convert that node from a destination node to a category node.
            // The reasoning here is that a node can't both serve as a destination and also as
            // a grouping for other destinations (just based on the way the JPopupMenu works).
            // So, if it was previously a destination node, it ain't anymore:
            //if (parentNode.getChildCount() == 0) {
            //  parentNode.setDirectory(null);
            //}
            // On second thought, leave it in, in case the node is edited later to no longer be
            // a parent... still want to remember the node's directory in that case.
            // Let the menu code sort out which are category nodes and which are destination nodes.

            // Now add the new child and reload the tree:
            parentNode.add(newNode);
            ((DefaultTreeModel)quickMoveTree.getModel()).reload();
            TreePath path = new TreePath(newNode.getPath());
            quickMoveTree.getSelectionModel().setSelectionPath(path);
        }
    }

    /**
     * Prompts the user to remove the selected Quick Move destination(s).
     */
    private void handleRemoveQuickMove() {
        // Does nothing if nothing is selected:
        if (quickMoveTree.getSelectionPath() == null) {
            return;
        }

        // If the root is selected, you can remove all at once:
        DefaultMutableTreeNode selectedNode;
        selectedNode = (DefaultMutableTreeNode)quickMoveTree.getSelectionPath().getLastPathComponent();
        DefaultTreeModel model = (DefaultTreeModel)quickMoveTree.getModel();
        if (selectedNode == rootNode) {
            int input = JOptionPane.showConfirmDialog(this.getRootPane(),
                                                      "Remove all Quick Move destinations?",
                                                      "Confirm remove all",
                                                      JOptionPane.YES_NO_OPTION);
            if (input == JOptionPane.YES_OPTION) {
                rootNode.removeAllChildren();
                model.reload(rootNode);
            }
        }

        // Otherwise, remove the selected node:
        else {
            model.removeNodeFromParent(selectedNode);
        }
    }

    /**
     * Prompts the user to edit the selected Quick Move destination.
     */
    private void handleEditQuickMove() {
        // If nothing was seleced, or if the dummy root node was selected, there's nothing to edit.
        if (quickMoveTree.getSelectionPath() == null) {
            getMessageUtil().info("Nothing selected", "You must select something to edit.");
            return;
        }
        QuickMoveManager.TreeNode selectedNode;
        selectedNode = (QuickMoveManager.TreeNode)quickMoveTree.getSelectionPath().getLastPathComponent();
        if (selectedNode == rootNode) {
            getMessageUtil().info("Invalid selection", "You can't edit this node.");
            return;
        }

        // Prompt the user for new destination details:
        QuickMoveAddDialog dialog = new QuickMoveAddDialog(this, selectedNode);
        dialog.setVisible(true);
        QuickMoveManager.TreeNode newNode = dialog.getResult();
        if (newNode != null) {
            // Wholesale replacement of edited node for existing node may not be the most elegant way...
            QuickMoveManager.TreeNode parentNode = (QuickMoveManager.TreeNode)selectedNode.getParent();
            parentNode.insert(newNode, parentNode.getIndex(selectedNode));
            parentNode.remove(selectedNode);
            ((DefaultTreeModel)quickMoveTree.getModel()).reload();
            TreePath path = new TreePath(newNode.getPath());
            quickMoveTree.getSelectionModel().setSelectionPath(path);
        }

    }

    /**
     * Moves the selected Quick Move destination up in the list, if possible.
     */
    private void handleMoveQuickMoveUp() {
        // If nothing was seleced, or if the dummy root node was selected, there's nothing to do.
        if (quickMoveTree.getSelectionPath() == null) {
            return;
        }
        QuickMoveManager.TreeNode selectedNode;
        selectedNode = (QuickMoveManager.TreeNode)quickMoveTree.getSelectionPath().getLastPathComponent();
        if (selectedNode == rootNode) {
            return;
        }
        QuickMoveManager.TreeNode parentNode = (QuickMoveManager.TreeNode)selectedNode.getParent();
        int currentIndex = parentNode.getIndex(selectedNode);

        // If it's already at index 0, it can't move up any further:
        if (currentIndex <= 0) {
            return;
        }

        // otherwise, move it up:
        parentNode.remove(selectedNode);
        parentNode.insert(selectedNode, currentIndex - 1);
        ((DefaultTreeModel)quickMoveTree.getModel()).reload();
        TreePath path = new TreePath(selectedNode.getPath());
        quickMoveTree.getSelectionModel().setSelectionPath(path);
    }

    /**
     * Moves the selected Quick Move destination down in the list, if possible.
     */
    private void handleMoveQuickMoveDown() {
        // If nothing was seleced, or if the dummy root node was selected, there's nothing to do.
        if (quickMoveTree.getSelectionPath() == null) {
            return;
        }
        QuickMoveManager.TreeNode selectedNode;
        selectedNode = (QuickMoveManager.TreeNode)quickMoveTree.getSelectionPath().getLastPathComponent();
        if (selectedNode == rootNode) {
            return;
        }
        QuickMoveManager.TreeNode parentNode = (QuickMoveManager.TreeNode)selectedNode.getParent();
        int currentIndex = parentNode.getIndex(selectedNode);

        // If it's already at the end of the list, it can't move down any further:
        if (currentIndex >= parentNode.getChildCount() - 1) {
            return;
        }

        // otherwise, move it down:
        parentNode.remove(selectedNode);
        parentNode.insert(selectedNode, currentIndex + 1);
        ((DefaultTreeModel)quickMoveTree.getModel()).reload();
        TreePath path = new TreePath(selectedNode.getPath());
        quickMoveTree.getSelectionModel().setSelectionPath(path);
    }

    private MessageUtil getMessageUtil() {
        if (messageUtil == null) {
            messageUtil = new MessageUtil(MainWindow.getInstance(), logger);
        }
        return messageUtil;
    }

}
