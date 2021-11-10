package view.management.playbooks;

import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import com.google.gson.JsonSyntaxException;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import models.activity.Activity;
import models.project.Project;
import models.services_manager.ServicesManager;
import org.apache.http.auth.AuthenticationException;
import services.ActivityService;
import view.FathersComponentTab;
import view.management.playbooks.actions.NotApplicable;
import view.management.playbooks.actions.UploadEvidence;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class PlaybookTab extends FathersComponentTab {

    private JTable tblInProgressPlaybooks;
    private DefaultTableModel tblInProgressPlaybooksModel;

    private JTable tblNotStartedPlaybooks;
    private DefaultTableModel tblNotStartedPlaybooksModel;

    private JTable tblDonePlaybooks;
    private DefaultTableModel tblDonePlaybooksModel;

    private JTable tblNotApplicable;
    private DefaultTableModel tblNotApplicableModel;

    private JPanel rootPanel;
    private JProgressBar pgBarDone;
    private JProgressBar pgBarInProgress;
    private JProgressBar pgBarNotStarted;
    private JLabel lblWorkingProjectTitle;
    private JPanel progressBarsPanel;
    private JButton btnFinish;
    private JButton btnNotApplicable;
    private JButton btnStart;
    private JButton btnRestart;

    private JButton btnRestartNotApplicable;
    private JTabbedPane tabbedPane;
    private JPanel pnlInProgress;
    private JPanel pnlNotStarted;
    private JPanel pnlDone;
    private JPanel pnlNotApplicable;
    private JLabel lblProgress;

    private int selectedIndex = 0;

    public PlaybookTab(final IBurpExtenderCallbacks callbacks, final IExtensionHelpers helpers, ServicesManager servicesManager) {
        super(callbacks, helpers, servicesManager);
    }

    public void initializeComponent() {
        // GUI initializer generated by IntelliJ IDEA GUI Designer
        // >>> IMPORTANT!! <<<
        // DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();

        this.initializePlaybooksTables();

        super.addLblBoldListener(lblWorkingProjectTitle);
        super.addLblBoldListener(lblProgress);


        rootPanel.addPropertyChangeListener(evt -> {
            if (evt.getPropertyName().equals("foreground")) {

            }
        });

        btnFinish.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Dimension defaultDimension = (Dimension) UIManager.get("OptionPane.minimumSize");
                selectedIndex = 0;
                try {
                    UIManager.put("OptionPane.minimumSize", new Dimension(800, 320));
                    JOptionPane.showOptionDialog(rootPanel, new UploadEvidence(servicesManager, PlaybookTab.this, util).$$$getRootComponent$$$(), "Upload evidence", JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, null, new Object[]{}, null);
                } catch (ClassCastException | NullPointerException ignored) {
                    UIManager.put("OptionPane.minimumSize", defaultDimension);
                } finally {
                    UIManager.put("OptionPane.minimumSize", defaultDimension);
                }
            }
        });

        btnNotApplicable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedIndex = 0;
                Dimension defaultDimension = (Dimension) UIManager.get("OptionPane.minimumSize");
                try {
                    UIManager.put("OptionPane.minimumSize", new Dimension(800, 200));
                    JOptionPane.showOptionDialog(rootPanel, new NotApplicable(servicesManager, PlaybookTab.this, util).$$$getRootComponent$$$(), "Justification of status", JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, null, new Object[]{}, null);
                } catch (ClassCastException | NullPointerException ignored) {
                    UIManager.put("OptionPane.minimumSize", defaultDimension);
                } finally {
                    UIManager.put("OptionPane.minimumSize", defaultDimension);
                }
            }
        });

        btnStart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedIndex = 1;
                new Thread(() -> {
                    if (!btnStart.isEnabled()) {
                        return;
                    }
                    btnStart.setEnabled(false);
                    List<Integer> indexes = Arrays.stream(tblNotStartedPlaybooks.getSelectedRows()).boxed().collect(Collectors.toList());
                    Collections.reverse(indexes);
                    ActivityService activityService = servicesManager.getActivityService();
                    for (int i :
                            indexes) {
                        try {
                            activityService.updateActivityToStart((Integer) tblNotStartedPlaybooks.getValueAt(i, 0));
                        } catch (Error | JsonSyntaxException err) {
                            util.sendStderr(err.toString());
                            JOptionPane.showMessageDialog(rootPanel, "Error!\nCheck the errors in extender tab!");
                        } catch (AuthenticationException authenticationException) {
                            JOptionPane.showMessageDialog(rootPanel, "Not authorized!");
                        }
                    }

                    updatePlaybooksTables();
                    btnStart.setEnabled(true);

                }).start();

            }
        });

        btnRestart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedIndex = 2;
                restartActivity(btnRestart, tblDonePlaybooks);
            }
        });


        btnRestartNotApplicable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedIndex = 3;
                restartActivity(btnRestartNotApplicable, tblNotApplicable);
            }
        });
    }

    private void restartActivity(JButton jButton, JTable jTable) {
        new Thread(() -> {
            if (!jButton.isEnabled()) {
                return;
            }
            jButton.setEnabled(false);

            List<Integer> indexes = Arrays.stream(jTable.getSelectedRows()).boxed().collect(Collectors.toList());
            Collections.reverse(indexes);
            ActivityService activityService = servicesManager.getActivityService();
            for (int i :
                    indexes) {
                try {
                    activityService.updateActivityToRestart((Integer) jTable.getValueAt(i, 0));
                } catch (Error | JsonSyntaxException err) {
                    util.sendStderr(err.toString());
                    JOptionPane.showMessageDialog(rootPanel, "Error!\nCheck the errors in extender tab!");
                } catch (AuthenticationException authenticationException) {
                    JOptionPane.showMessageDialog(rootPanel, "Not authorized!");
                }
            }

            updatePlaybooksTables();
            jButton.setEnabled(true);
        }).start();
    }

    private void initializePlaybooksTables() {

        Object[] columnsHeaders = {"ID", "Title", "Last change", "Analyst", "Evidence"};

        this.tblNotStartedPlaybooksModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        this.tblInProgressPlaybooksModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;

            }
        };

        this.tblDonePlaybooksModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        this.tblNotApplicableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        this.tblNotStartedPlaybooksModel.setColumnIdentifiers(columnsHeaders);
        this.tblInProgressPlaybooksModel.setColumnIdentifiers(columnsHeaders);
        this.tblDonePlaybooksModel.setColumnIdentifiers(columnsHeaders);
        this.tblNotApplicableModel.setColumnIdentifiers(columnsHeaders);

        this.tblNotStartedPlaybooks.setModel(tblNotStartedPlaybooksModel);
        this.tblInProgressPlaybooks.setModel(tblInProgressPlaybooksModel);
        this.tblDonePlaybooks.setModel(tblDonePlaybooksModel);
        this.tblNotApplicable.setModel(tblNotApplicableModel);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < tblNotStartedPlaybooks.getColumnModel().getColumnCount(); i++) {
            this.tblNotStartedPlaybooks.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            this.tblInProgressPlaybooks.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            this.tblDonePlaybooks.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            this.tblNotApplicable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        this.pgBarDone.setValue(100);
        this.pgBarInProgress.setValue(100);

        this.pgBarDone.setForeground(new Color(44, 194, 86));
        this.pgBarInProgress.setForeground(new Color(242, 197, 0));


        /* Responsible for keeping the playbook table updated, but with a know timeframe of delay*/
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        long delay = 600;
        TimeUnit timeUnit = TimeUnit.SECONDS;
        executor.scheduleAtFixedRate(() -> {
            util.sendStdout("Updating playbook table.");
            servicesManager.getProjectService().updateWorkingProject();
            updatePlaybooksTables();
        }, delay, delay, timeUnit);

    }

    public synchronized void updatePlaybooksTables() {
        int notStartedActivity = 0;
        int inProgressActivity = 0;
        int doneActivity = 0;
        int notApplicable = 0;

        tblNotStartedPlaybooksModel.setRowCount(0);
        tblInProgressPlaybooksModel.setRowCount(0);
        tblDonePlaybooksModel.setRowCount(0);
        tblNotApplicableModel.setRowCount(0);


        Project workingProject = this.servicesManager.getProjectService().getWorkingProject();
        if (workingProject == null) {
            return;
        }

        this.lblWorkingProjectTitle.setText(workingProject.getPid() + " - " + workingProject.getLabel());


        for (Activity a :
                workingProject.getActivities()) {
            switch (a.getStatus()) {
                case Activity.NOT_STARTED -> {
                    this.tblNotStartedPlaybooksModel.addRow(new Object[]{a.getId(), a.getTitle(), a.getPrettyUpdateAt(), "", (a.getArchiveFilename() == null) ? a.getEvidenceText() : a.getArchiveFilename()});
                    notStartedActivity += 1;
                }
                case Activity.IN_PROGRESS -> {
                    this.tblInProgressPlaybooksModel.addRow(new Object[]{a.getId(), a.getTitle(), a.getPrettyUpdateAt(), a.getPortalUser().getName(), (a.getArchiveFilename() == null) ? a.getEvidenceText() : a.getArchiveFilename()});
                    inProgressActivity += 1;
                }
                case Activity.DONE -> {
                    this.tblDonePlaybooksModel.addRow(new Object[]{a.getId(), a.getTitle(), a.getPrettyUpdateAt(), a.getPortalUser().getName(), (a.getArchiveFilename() == null) ? a.getEvidenceText() : a.getArchiveFilename()});
                    doneActivity += 1;
                }
                case Activity.NOT_APPLICABLE -> {
                    this.tblNotApplicableModel.addRow(new Object[]{a.getId(), a.getTitle(), a.getPrettyUpdateAt(), a.getPortalUser().getName(), a.getJustify()});
                    notApplicable += 1;
                }
            }
        }

        tabbedPane.removeAll();


        if (inProgressActivity > 0) {
            tabbedPane.add("In progress", pnlInProgress);
        }

        if (notStartedActivity > 0) {
            tabbedPane.add("Not started", pnlNotStarted);
        }

        if (doneActivity > 0) {
            tabbedPane.add("Done", pnlDone);
        }

        if (notApplicable > 0) {
            tabbedPane.add("Not applicable", pnlNotApplicable);
        }

        tabbedPane.revalidate();
        tabbedPane.repaint();
        if (tabbedPane.getTabCount() < selectedIndex) {
            selectedIndex = 0;
        }

        tabbedPane.setSelectedIndex(selectedIndex);

        int totalOfActivities = notStartedActivity + inProgressActivity + doneActivity + notApplicable;
        this.pgBarNotStarted.setPreferredSize(new Dimension(((this.progressBarsPanel.getWidth() * notStartedActivity) / totalOfActivities), this.pgBarNotStarted.getHeight()));
        this.pgBarInProgress.setPreferredSize(new Dimension(((this.progressBarsPanel.getWidth() * inProgressActivity) / totalOfActivities), this.pgBarInProgress.getHeight()));
        this.pgBarDone.setPreferredSize(new Dimension(((this.progressBarsPanel.getWidth() * doneActivity) / totalOfActivities), this.pgBarDone.getHeight()));
        this.progressBarsPanel.setToolTipText("Not started: " + notStartedActivity +
                "\nIn progress: " + inProgressActivity +
                "\nDone: " + doneActivity +
                "\nNot applicable: " + notApplicable);
        this.progressBarsPanel.revalidate();
        this.progressBarsPanel.repaint();


    }


    public JTable getTblInProgressPlaybooks() {
        return tblInProgressPlaybooks;
    }

    public JTable getTblNotStartedPlaybooks() {
        return tblNotStartedPlaybooks;
    }

    public JTable getTblDonePlaybooks() {
        return tblDonePlaybooks;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        rootPanel = new JPanel();
        rootPanel.setLayout(new FormLayout("fill:30dlu:noGrow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:30dlu:noGrow", "center:9px:noGrow,top:4dlu:noGrow,center:131px:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:11dlu:noGrow,center:250dlu:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow"));
        tabbedPane = new JTabbedPane();
        Font tabbedPaneFont = this.$$$getFont$$$(null, Font.BOLD, -1, tabbedPane.getFont());
        if (tabbedPaneFont != null) tabbedPane.setFont(tabbedPaneFont);
        CellConstraints cc = new CellConstraints();
        rootPanel.add(tabbedPane, cc.xywh(3, 5, 1, 5));
        pnlInProgress = new JPanel();
        pnlInProgress.setLayout(new FormLayout("fill:481px:grow", "fill:min(p;200dlu):grow,top:4dlu:noGrow,center:max(p;10dlu):grow"));
        Font pnlInProgressFont = this.$$$getFont$$$(null, Font.BOLD, -1, pnlInProgress.getFont());
        if (pnlInProgressFont != null) pnlInProgress.setFont(pnlInProgressFont);
        tabbedPane.addTab("In progress", pnlInProgress);
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setHorizontalScrollBarPolicy(31);
        scrollPane1.setVerticalScrollBarPolicy(20);
        pnlInProgress.add(scrollPane1, cc.xy(1, 1, CellConstraints.FILL, CellConstraints.FILL));
        scrollPane1.setBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        tblInProgressPlaybooks = new JTable();
        tblInProgressPlaybooks.setAutoCreateRowSorter(true);
        tblInProgressPlaybooks.setAutoResizeMode(4);
        scrollPane1.setViewportView(tblInProgressPlaybooks);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new FormLayout("fill:d:noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow,left:4dlu:noGrow,fill:max(d;4px):noGrow", "center:max(p;10dlu):grow"));
        pnlInProgress.add(panel1, cc.xy(1, 3, CellConstraints.DEFAULT, CellConstraints.FILL));
        btnFinish = new JButton();
        btnFinish.setBackground(new Color(-14115763));
        btnFinish.setForeground(new Color(-1));
        btnFinish.setIcon(new ImageIcon(getClass().getResource("/icons/playbooks/checkbox.png")));
        btnFinish.setLabel("Finish");
        btnFinish.setText("Finish");
        panel1.add(btnFinish, cc.xy(1, 1));
        btnNotApplicable = new JButton();
        btnNotApplicable.setAutoscrolls(false);
        btnNotApplicable.setBackground(new Color(-14260834));
        btnNotApplicable.setForeground(new Color(-1));
        btnNotApplicable.setIcon(new ImageIcon(getClass().getResource("/icons/playbooks/thumb-down.png")));
        btnNotApplicable.setLabel("Not applicable");
        btnNotApplicable.setText("Not applicable");
        panel1.add(btnNotApplicable, cc.xy(3, 1));
        pnlNotStarted = new JPanel();
        pnlNotStarted.setLayout(new FormLayout("fill:481px:grow", "fill:min(p;200dlu):grow,top:4dlu:noGrow,center:max(p;10dlu):grow"));
        Font pnlNotStartedFont = this.$$$getFont$$$(null, Font.BOLD, -1, pnlNotStarted.getFont());
        if (pnlNotStartedFont != null) pnlNotStarted.setFont(pnlNotStartedFont);
        tabbedPane.addTab("Not started", pnlNotStarted);
        final JScrollPane scrollPane2 = new JScrollPane();
        pnlNotStarted.add(scrollPane2, cc.xy(1, 1, CellConstraints.FILL, CellConstraints.FILL));
        scrollPane2.setBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        tblNotStartedPlaybooks = new JTable();
        tblNotStartedPlaybooks.setAutoCreateRowSorter(true);
        tblNotStartedPlaybooks.setAutoResizeMode(4);
        scrollPane2.setViewportView(tblNotStartedPlaybooks);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new FormLayout("fill:d:noGrow", "center:max(p;10dlu):grow"));
        pnlNotStarted.add(panel2, cc.xy(1, 3, CellConstraints.DEFAULT, CellConstraints.FILL));
        btnStart = new JButton();
        btnStart.setBackground(new Color(-2643441));
        btnStart.setForeground(new Color(-1));
        btnStart.setIcon(new ImageIcon(getClass().getResource("/icons/playbooks/play.png")));
        btnStart.setLabel("Start");
        btnStart.setText("Start");
        panel2.add(btnStart, cc.xy(1, 1));
        pnlDone = new JPanel();
        pnlDone.setLayout(new FormLayout("fill:481px:grow", "fill:min(p;200dlu):grow,top:4dlu:noGrow,center:max(d;10dlu):grow"));
        Font pnlDoneFont = this.$$$getFont$$$(null, Font.BOLD, -1, pnlDone.getFont());
        if (pnlDoneFont != null) pnlDone.setFont(pnlDoneFont);
        tabbedPane.addTab("Done", pnlDone);
        final JScrollPane scrollPane3 = new JScrollPane();
        pnlDone.add(scrollPane3, cc.xy(1, 1, CellConstraints.FILL, CellConstraints.FILL));
        scrollPane3.setBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        tblDonePlaybooks = new JTable();
        tblDonePlaybooks.setAutoCreateRowSorter(true);
        tblDonePlaybooks.setAutoResizeMode(4);
        scrollPane3.setViewportView(tblDonePlaybooks);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new FormLayout("fill:d:noGrow", "center:max(p;10dlu):grow"));
        pnlDone.add(panel3, cc.xy(1, 3, CellConstraints.DEFAULT, CellConstraints.FILL));
        btnRestart = new JButton();
        btnRestart.setBackground(new Color(-7627876));
        btnRestart.setForeground(new Color(-1));
        btnRestart.setIcon(new ImageIcon(getClass().getResource("/icons/playbooks/refresh.png")));
        btnRestart.setLabel("Restart");
        btnRestart.setText("Restart");
        panel3.add(btnRestart, cc.xy(1, 1));
        pnlNotApplicable = new JPanel();
        pnlNotApplicable.setLayout(new FormLayout("fill:481px:grow", "fill:min(p;200dlu):grow,top:4dlu:noGrow,center:max(d;10dlu):grow"));
        Font pnlNotApplicableFont = this.$$$getFont$$$(null, Font.BOLD, -1, pnlNotApplicable.getFont());
        if (pnlNotApplicableFont != null) pnlNotApplicable.setFont(pnlNotApplicableFont);
        tabbedPane.addTab("Not applicable", pnlNotApplicable);
        final JScrollPane scrollPane4 = new JScrollPane();
        pnlNotApplicable.add(scrollPane4, cc.xy(1, 1, CellConstraints.FILL, CellConstraints.FILL));
        scrollPane4.setBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        tblNotApplicable = new JTable();
        tblNotApplicable.setAutoCreateRowSorter(true);
        tblNotApplicable.setAutoResizeMode(4);
        scrollPane4.setViewportView(tblNotApplicable);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new FormLayout("fill:d:noGrow", "center:max(p;10dlu):grow"));
        pnlNotApplicable.add(panel4, cc.xy(1, 3, CellConstraints.DEFAULT, CellConstraints.FILL));
        btnRestartNotApplicable = new JButton();
        btnRestartNotApplicable.setBackground(new Color(-7627876));
        btnRestartNotApplicable.setForeground(new Color(-1));
        btnRestartNotApplicable.setIcon(new ImageIcon(getClass().getResource("/icons/playbooks/refresh.png")));
        btnRestartNotApplicable.setLabel("Restart");
        btnRestartNotApplicable.setText("Restart");
        panel4.add(btnRestartNotApplicable, cc.xy(1, 1));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new FormLayout("fill:d:grow", "center:45px:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:d:grow,top:4dlu:noGrow,center:2dlu:noGrow"));
        rootPanel.add(panel5, cc.xy(3, 3, CellConstraints.FILL, CellConstraints.FILL));
        lblProgress = new JLabel();
        Font lblProgressFont = this.$$$getFont$$$(null, Font.BOLD, -1, lblProgress.getFont());
        if (lblProgressFont != null) lblProgress.setFont(lblProgressFont);
        lblProgress.setText("Progress:");
        panel5.add(lblProgress, cc.xy(1, 1, CellConstraints.CENTER, CellConstraints.BOTTOM));
        final JSeparator separator1 = new JSeparator();
        panel5.add(separator1, cc.xy(1, 7, CellConstraints.FILL, CellConstraints.FILL));
        progressBarsPanel = new JPanel();
        progressBarsPanel.setLayout(new FormLayout("fill:d:grow,left:4dlu:noGrow,fill:d:grow,left:4dlu:noGrow,fill:d:grow", "center:d:grow"));
        panel5.add(progressBarsPanel, cc.xy(1, 5, CellConstraints.FILL, CellConstraints.FILL));
        pgBarNotStarted = new JProgressBar();
        progressBarsPanel.add(pgBarNotStarted, cc.xy(5, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        pgBarInProgress = new JProgressBar();
        progressBarsPanel.add(pgBarInProgress, cc.xy(1, 1, CellConstraints.FILL, CellConstraints.DEFAULT));
        pgBarDone = new JProgressBar();
        progressBarsPanel.add(pgBarDone, cc.xy(3, 1, CellConstraints.FILL, CellConstraints.CENTER));
        lblWorkingProjectTitle = new JLabel();
        Font lblWorkingProjectTitleFont = this.$$$getFont$$$(null, Font.BOLD, -1, lblWorkingProjectTitle.getFont());
        if (lblWorkingProjectTitleFont != null) lblWorkingProjectTitle.setFont(lblWorkingProjectTitleFont);
        lblWorkingProjectTitle.setText("");
        panel5.add(lblWorkingProjectTitle, cc.xy(1, 3, CellConstraints.CENTER, CellConstraints.DEFAULT));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }

}
