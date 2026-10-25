package com.crimescope.desktop;

import com.crimescope.desktop.client.CrimeScopeRestClient;
import com.crimescope.desktop.jdbc.DirectJdbcDemoRunner;
import com.fasterxml.jackson.databind.JsonNode;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * CrimeScope AI - Java Swing Desktop Command Center.
 * Academic OOPS Microproject: Desktop Operator Console.
 * Demonstrates:
 * - GUI Architecture & Event-driven Programming (Swing, MVC)
 * - Object-Oriented Component Reusability & Encapsulation
 * - Dual Connectivity: REST API Client + Controlled Educational Direct JDBC Mode
 * - Java File Handling for CSV Import/Export with RFC 4180 parsing
 * - Threading: SwingWorker for asynchronous execution
 */
public class CrimeScopeDesktopApp extends JFrame {

    private static final Color BG_DARK = new Color(10, 14, 26);
    private static final Color CARD_BG = new Color(17, 24, 39);
    private static final Color CYAN_NEON = new Color(0, 240, 255);
    private static final Color TEAL_ACCENT = new Color(0, 229, 163);
    private static final Color AMBER_WARN = new Color(245, 158, 11);
    private static final Color RED_ALERT = new Color(239, 68, 68);
    private static final Color TEXT_MUTED = new Color(156, 163, 175);

    private final CrimeScopeRestClient restClient;
    private final DirectJdbcDemoRunner directJdbcRunner;

    // UI Components
    private JComboBox<String> districtCombo;
    private JComboBox<String> categoryCombo;
    private JComboBox<String> yearCombo;
    private JTable recordsTable;
    private DefaultTableModel recordsTableModel;
    private JLabel statusLabel;
    private JLabel recordCountLabel;

    // File Handling Components
    private JTextField importFilePathField;
    private JTextArea importLogsArea;
    private DefaultTableModel malformedTableModel;

    // JDBC Demo Components
    private JComboBox<String> jdbcDistrictCombo;
    private JComboBox<String> jdbcYearCombo;
    private JTextArea jdbcTelemetryArea;
    private DefaultTableModel jdbcTableModel;

    // Lookup caches
    private final Map<String, Long> districtIdMap = new HashMap<>();
    private final Map<String, Long> categoryIdMap = new HashMap<>();

    public CrimeScopeDesktopApp() {
        super("CrimeScope AI — Tamil Nadu Crime Intelligence Command Center (Desktop Edition)");
        this.restClient = new CrimeScopeRestClient("http://localhost:8080");
        this.directJdbcRunner = new DirectJdbcDemoRunner();

        initUi();
        loadMetadata();
        loadRecords();
    }

    private void initUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 820);
        setMinimumSize(new Dimension(1000, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DARK);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(BG_DARK);
        mainPanel.setBorder(new EmptyBorder(12, 14, 12, 14));

        mainPanel.add(buildHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(buildTabbedPane(), BorderLayout.CENTER);
        mainPanel.add(buildFooterPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(31, 41, 55), 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titlePanel.setBackground(CARD_BG);

        JLabel logoBadge = new JLabel(" CRIMESCOPE AI ");
        logoBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoBadge.setForeground(Color.BLACK);
        logoBadge.setBackground(CYAN_NEON);
        logoBadge.setOpaque(true);
        logoBadge.setBorder(new EmptyBorder(4, 8, 4, 8));

        JLabel titleLabel = new JLabel("TAMIL NADU INTELLIGENCE COMMAND DESKTOP");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(Color.WHITE);

        titlePanel.add(logoBadge);
        titlePanel.add(titleLabel);

        JPanel telemetryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        telemetryPanel.setBackground(CARD_BG);

        statusLabel = new JLabel("● SYSTEM LINKED [8080]");
        statusLabel.setFont(new Font("Consolas", Font.BOLD, 12));
        statusLabel.setForeground(TEAL_ACCENT);

        recordCountLabel = new JLabel("RECORDS: 0");
        recordCountLabel.setFont(new Font("Consolas", Font.BOLD, 12));
        recordCountLabel.setForeground(CYAN_NEON);

        telemetryPanel.add(statusLabel);
        telemetryPanel.add(recordCountLabel);

        header.add(titlePanel, BorderLayout.WEST);
        header.add(telemetryPanel, BorderLayout.EAST);
        return header;
    }

    private JTabbedPane buildTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(BG_DARK);
        tabbedPane.setForeground(Color.WHITE);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("COMMAND RECORDS GRID", buildRecordsTab());
        tabbedPane.addTab("CSV FILE HANDLING (IMPORT/EXPORT)", buildFileHandlingTab());
        tabbedPane.addTab("ACADEMIC DIRECT JDBC DEMONSTRATION", buildJdbcDemoTab());

        return tabbedPane;
    }

    // --- TAB 1: Records Grid ---

    private JPanel buildRecordsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_DARK);

        // Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterBar.setBackground(CARD_BG);
        filterBar.setBorder(new LineBorder(new Color(31, 41, 55), 1));

        districtCombo = new JComboBox<>(new String[]{"All Tamil Nadu Districts"});
        categoryCombo = new JComboBox<>(new String[]{"All Crime Categories"});
        yearCombo = new JComboBox<>(new String[]{"All Years", "2023", "2022", "2021", "2020", "2019", "2018"});

        JButton filterBtn = createCyberButton("FILTER", CYAN_NEON);
        filterBtn.addActionListener(e -> loadRecords());

        JButton resetBtn = createCyberButton("RESET", TEXT_MUTED);
        resetBtn.addActionListener(e -> {
            districtCombo.setSelectedIndex(0);
            categoryCombo.setSelectedIndex(0);
            yearCombo.setSelectedIndex(0);
            loadRecords();
        });

        filterBar.add(new JLabel("District:"));
        filterBar.add(districtCombo);
        filterBar.add(new JLabel("Category:"));
        filterBar.add(categoryCombo);
        filterBar.add(new JLabel("Year:"));
        filterBar.add(yearCombo);
        filterBar.add(filterBtn);
        filterBar.add(resetBtn);

        // Table
        String[] columns = {"ID", "District", "Category", "Year", "Cases", "Arrests", "Convicted", "Clearance %", "Status", "Source Ref"};
        recordsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        recordsTable = new JTable(recordsTableModel);
        styleDarkTable(recordsTable);

        JScrollPane scrollPane = new JScrollPane(recordsTable);
        scrollPane.getViewport().setBackground(CARD_BG);
        scrollPane.setBorder(new LineBorder(new Color(31, 41, 55), 1));

        // Action Toolbar
        JPanel actionToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        actionToolbar.setBackground(CARD_BG);

        JButton inspectBtn = createCyberButton("INSPECT RECORD DETAILS", TEAL_ACCENT);
        inspectBtn.addActionListener(e -> showRecordDetails());

        JButton addBtn = createCyberButton("+ NEW RECORD", CYAN_NEON);
        addBtn.addActionListener(e -> showAddRecordDialog());

        JButton refreshBtn = createCyberButton("REFRESH", TEXT_MUTED);
        refreshBtn.addActionListener(e -> loadRecords());

        actionToolbar.add(inspectBtn);
        actionToolbar.add(addBtn);
        actionToolbar.add(refreshBtn);

        panel.add(filterBar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(actionToolbar, BorderLayout.SOUTH);

        return panel;
    }

    // --- TAB 2: File Handling (CSV Import / Export) ---

    private JPanel buildFileHandlingTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_DARK);

        JPanel topBox = new JPanel(new GridLayout(2, 1, 6, 6));
        topBox.setBackground(CARD_BG);
        topBox.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(31, 41, 55), 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        // File selection row
        JPanel fileRow = new JPanel(new BorderLayout(10, 0));
        fileRow.setBackground(CARD_BG);

        importFilePathField = new JTextField();
        importFilePathField.setEditable(false);
        importFilePathField.setBackground(new Color(24, 32, 47));
        importFilePathField.setForeground(Color.WHITE);

        JButton browseBtn = createCyberButton("BROWSE CSV...", CYAN_NEON);
        browseBtn.addActionListener(e -> chooseCsvFile());

        JButton runImportBtn = createCyberButton("EXECUTE BATCH IMPORT", TEAL_ACCENT);
        runImportBtn.addActionListener(e -> executeCsvImport());

        fileRow.add(new JLabel("Select CSV File: "), BorderLayout.WEST);
        fileRow.add(importFilePathField, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(CARD_BG);
        btnPanel.add(browseBtn);
        btnPanel.add(runImportBtn);
        fileRow.add(btnPanel, BorderLayout.EAST);

        // Actions row
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionRow.setBackground(CARD_BG);

        JButton downloadTemplateBtn = createCyberButton("DOWNLOAD CSV TEMPLATE", CYAN_NEON);
        downloadTemplateBtn.addActionListener(e -> saveCsvTemplate());

        JButton exportBtn = createCyberButton("EXPORT RECORDS TO CSV", AMBER_WARN);
        exportBtn.addActionListener(e -> exportRecordsToCsv());

        actionRow.add(downloadTemplateBtn);
        actionRow.add(exportBtn);

        topBox.add(fileRow);
        topBox.add(actionRow);

        // Diagnostics Center Panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.4);
        splitPane.setBackground(BG_DARK);

        importLogsArea = new JTextArea("Awaiting CSV file execution...\nRFC 4180 parsing, duplicate detection, and malformed row reporting active.\n");
        importLogsArea.setBackground(new Color(15, 23, 42));
        importLogsArea.setForeground(CYAN_NEON);
        importLogsArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        importLogsArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(importLogsArea);
        logScroll.setBorder(BorderFactory.createTitledBorder(new LineBorder(new Color(31, 41, 55)), "IMPORT EXECUTION LOG"));

        String[] malformedCols = {"Line #", "Raw CSV Row", "Failed Field", "Validation Error"};
        malformedTableModel = new DefaultTableModel(malformedCols, 0);
        JTable malformedTable = new JTable(malformedTableModel);
        styleDarkTable(malformedTable);
        JScrollPane malformedScroll = new JScrollPane(malformedTable);
        malformedScroll.setBorder(BorderFactory.createTitledBorder(new LineBorder(RED_ALERT), "MALFORMED ROWS DIAGNOSTIC INSPECTOR"));

        splitPane.setTopComponent(logScroll);
        splitPane.setBottomComponent(malformedScroll);

        panel.add(topBox, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    // --- TAB 3: Academic Direct JDBC Demonstration ---

    private JPanel buildJdbcDemoTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_DARK);

        JPanel controlBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        controlBar.setBackground(CARD_BG);
        controlBar.setBorder(new LineBorder(new Color(31, 41, 55), 1));

        jdbcDistrictCombo = new JComboBox<>(new String[]{"CHN - Chennai", "CBE - Coimbatore", "MDU - Madurai", "TRY - Tiruchirappalli", "SLM - Salem", "All Districts"});
        jdbcYearCombo = new JComboBox<>(new String[]{"2023", "2022", "2021", "2020", "2019", "2018", "All Years"});

        JButton executeJdbcBtn = createCyberButton("EXECUTE DIRECT JDBC QUERY", TEAL_ACCENT);
        executeJdbcBtn.addActionListener(e -> executeDirectJdbcQuery());

        controlBar.add(new JLabel("Target District Code:"));
        controlBar.add(jdbcDistrictCombo);
        controlBar.add(new JLabel("Target Year:"));
        controlBar.add(jdbcYearCombo);
        controlBar.add(executeJdbcBtn);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.35);
        splitPane.setBackground(BG_DARK);

        jdbcTelemetryArea = new JTextArea();
        jdbcTelemetryArea.setBackground(new Color(15, 23, 42));
        jdbcTelemetryArea.setForeground(CYAN_NEON);
        jdbcTelemetryArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        jdbcTelemetryArea.setEditable(false);
        jdbcTelemetryArea.setText("""
            === ACADEMIC JDBC DEMONSTRATION MODULE ===
            Fulfills academic requirement for direct database connectivity (java.sql.Connection, PreparedStatement, ResultSet).
            Click 'EXECUTE DIRECT JDBC QUERY' above to test low-level JDBC driver communication alongside Spring Data JPA.
            """);

        JScrollPane teleScroll = new JScrollPane(jdbcTelemetryArea);
        teleScroll.setBorder(BorderFactory.createTitledBorder(new LineBorder(new Color(31, 41, 55)), "JDBC DRIVER TELEMETRY & TRANSACTION BOUNDARY"));

        String[] cols = {"District Code", "District Name", "Category", "Year", "Cases", "Arrests", "Convictions"};
        jdbcTableModel = new DefaultTableModel(cols, 0);
        JTable jdbcTable = new JTable(jdbcTableModel);
        styleDarkTable(jdbcTable);
        JScrollPane tableScroll = new JScrollPane(jdbcTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder(new LineBorder(TEAL_ACCENT), "LIVE RESULTSET CURSOR DATA (PARAMETRIC SQL)"));

        splitPane.setTopComponent(teleScroll);
        splitPane.setBottomComponent(tableScroll);

        panel.add(controlBar, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BG_DARK);

        JLabel info = new JLabel("CrimeScope AI v1.0.0 | Academic OOPS Microproject | Spring Boot 3.3 + Java 21 + MySQL + Swing");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        info.setForeground(TEXT_MUTED);

        footer.add(info, BorderLayout.WEST);
        return footer;
    }

    // --- Business Logic & Data Fetching ---

    private void loadMetadata() {
        new SwingWorker<Void, Void>() {
            private JsonNode districts;
            private JsonNode categories;

            @Override
            protected Void doInBackground() throws Exception {
                districts = restClient.getDistricts();
                categories = restClient.getCategories();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    if (districts != null && districts.isArray()) {
                        for (JsonNode d : districts) {
                            String code = d.path("code").asText();
                            String name = d.path("name").asText();
                            long id = d.path("id").asLong();
                            String label = code + " - " + name;
                            districtCombo.addItem(label);
                            districtIdMap.put(label, id);
                        }
                    }

                    if (categories != null && categories.isArray()) {
                        for (JsonNode c : categories) {
                            String name = c.path("name").asText();
                            long id = c.path("id").asLong();
                            categoryCombo.addItem(name);
                            categoryIdMap.put(name, id);
                        }
                    }
                } catch (Exception e) {
                    statusLabel.setText("● BACKEND OFFLINE [RECONNECTING...]");
                    statusLabel.setForeground(RED_ALERT);
                }
            }
        }.execute();
    }

    private void loadRecords() {
        new SwingWorker<JsonNode, Void>() {
            @Override
            protected JsonNode doInBackground() throws Exception {
                String selectedDist = (String) districtCombo.getSelectedItem();
                Long distId = (selectedDist != null && !selectedDist.startsWith("All")) ? districtIdMap.get(selectedDist) : null;

                String selectedCat = (String) categoryCombo.getSelectedItem();
                Long catId = (selectedCat != null && !selectedCat.startsWith("All")) ? categoryIdMap.get(selectedCat) : null;

                String selectedYear = (String) yearCombo.getSelectedItem();
                Integer year = (selectedYear != null && !selectedYear.startsWith("All")) ? Integer.parseInt(selectedYear) : null;

                return restClient.getCrimeRecords(distId, catId, year, 0, 100);
            }

            @Override
            protected void done() {
                try {
                    JsonNode result = get();
                    recordsTableModel.setRowCount(0);
                    if (result != null && result.has("content")) {
                        JsonNode content = result.get("content");
                        int count = 0;
                        for (JsonNode r : content) {
                            count++;
                            long id = r.path("id").asLong();
                            String district = r.path("district").path("name").asText("N/A");
                            String category = r.path("category").path("name").asText("N/A");
                            int year = r.path("reportingYear").asInt();
                            int cases = r.path("caseCount").asInt();
                            int arrests = r.path("arrestCount").asInt();
                            int convictions = r.path("convictedCount").asInt();
                            int chargesheeted = r.path("chargesheetedCount").asInt();

                            double clearance = cases > 0 ? ((double) chargesheeted / cases) * 100.0 : 0.0;
                            String status = r.path("verificationStatus").asText("VERIFIED");
                            String sourceRef = r.path("sourceReference").asText("-");

                            recordsTableModel.addRow(new Object[]{
                                    id, district, category, year, cases, arrests, convictions,
                                    String.format("%.1f%%", clearance), status, sourceRef
                            });
                        }
                        recordCountLabel.setText("RECORDS: " + count + " LOADED");
                        statusLabel.setText("● SYSTEM OPERATIONAL");
                        statusLabel.setForeground(TEAL_ACCENT);
                    }
                } catch (Exception e) {
                    recordCountLabel.setText("RECORDS: 0");
                    statusLabel.setText("● DISCONNECTED FROM BACKEND");
                    statusLabel.setForeground(RED_ALERT);
                }
            }
        }.execute();
    }

    private void showRecordDetails() {
        int selectedRow = recordsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record from the table first.",
                    "No Record Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Object id = recordsTable.getValueAt(selectedRow, 0);
        Object dist = recordsTable.getValueAt(selectedRow, 1);
        Object cat = recordsTable.getValueAt(selectedRow, 2);
        Object year = recordsTable.getValueAt(selectedRow, 3);
        Object cases = recordsTable.getValueAt(selectedRow, 4);
        Object arrests = recordsTable.getValueAt(selectedRow, 5);
        Object convictions = recordsTable.getValueAt(selectedRow, 6);
        Object clearance = recordsTable.getValueAt(selectedRow, 7);
        Object status = recordsTable.getValueAt(selectedRow, 8);
        Object source = recordsTable.getValueAt(selectedRow, 9);

        String msg = String.format("""
            === CRIME RECORD DOSSIER ===
            Record ID: %s
            District: %s
            Crime Category: %s
            Reporting Year: %s
            Case Count (FIRs): %s
            Persons Arrested: %s
            Convictions Secured: %s
            Chargesheet Clearance: %s
            Verification Status: %s
            Source Reference: %s
            """, id, dist, cat, year, cases, arrests, convictions, clearance, status, source);

        JTextArea ta = new JTextArea(msg);
        ta.setEditable(false);
        ta.setBackground(CARD_BG);
        ta.setForeground(CYAN_NEON);
        ta.setFont(new Font("Consolas", Font.PLAIN, 13));
        JOptionPane.showMessageDialog(this, ta, "Record Dossier #" + id, JOptionPane.PLAIN_MESSAGE);
    }

    private void showAddRecordDialog() {
        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));

        JComboBox<String> distBox = new JComboBox<>();
        districtIdMap.keySet().forEach(distBox::addItem);

        JComboBox<String> catBox = new JComboBox<>();
        categoryIdMap.keySet().forEach(catBox::addItem);

        JTextField yearField = new JTextField("2023");
        JTextField casesField = new JTextField("50");
        JTextField arrestsField = new JTextField("35");
        JTextField convictionsField = new JTextField("15");

        form.add(new JLabel("District:"));
        form.add(distBox);
        form.add(new JLabel("Category:"));
        form.add(catBox);
        form.add(new JLabel("Reporting Year:"));
        form.add(yearField);
        form.add(new JLabel("Registered Cases (FIRs):"));
        form.add(casesField);
        form.add(new JLabel("Arrests:"));
        form.add(arrestsField);
        form.add(new JLabel("Convictions:"));
        form.add(convictionsField);

        int result = JOptionPane.showConfirmDialog(this, form, "Enter New Crime Record", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String distKey = (String) distBox.getSelectedItem();
                String catKey = (String) catBox.getSelectedItem();

                Map<String, Object> payload = new HashMap<>();
                payload.put("districtId", districtIdMap.get(distKey));
                payload.put("categoryId", categoryIdMap.get(catKey));
                payload.put("reportingYear", Integer.parseInt(yearField.getText().trim()));
                payload.put("caseCount", Integer.parseInt(casesField.getText().trim()));
                payload.put("arrestCount", Integer.parseInt(arrestsField.getText().trim()));
                payload.put("convictedCount", Integer.parseInt(convictionsField.getText().trim()));
                payload.put("chargesheetedCount", Integer.parseInt(arrestsField.getText().trim()));

                restClient.createCrimeRecord(payload);
                JOptionPane.showMessageDialog(this, "Record successfully created!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadRecords();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to create record: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void chooseCsvFile() {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            importFilePathField.setText(file.getAbsolutePath());
        }
    }

    private void executeCsvImport() {
        String path = importFilePathField.getText();
        if (path == null || path.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a valid CSV file first.", "No File Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File file = new File(path);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "File does not exist: " + path, "File Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        importLogsArea.setText("Starting RFC 4180 parsing and import on " + file.getName() + "...\n");
        malformedTableModel.setRowCount(0);

        new SwingWorker<JsonNode, Void>() {
            @Override
            protected JsonNode doInBackground() throws Exception {
                return restClient.importCsvFile(file);
            }

            @Override
            protected void done() {
                try {
                    JsonNode res = get();
                    if (res != null) {
                        int total = res.path("totalRows").asInt();
                        int valid = res.path("validRecordsImported").asInt();
                        int malformed = res.path("malformedRows").asInt();
                        int duplicates = res.path("duplicateRowsSkipped").asInt();
                        long time = res.path("executionTimeMs").asLong();

                        StringBuilder sb = new StringBuilder();
                        sb.append("=== CSV BATCH IMPORT COMPLETED ===\n");
                        sb.append(String.format("File: %s\n", res.path("fileName").asText()));
                        sb.append(String.format("Execution Time: %d ms\n", time));
                        sb.append(String.format("Total Rows Processed: %d\n", total));
                        sb.append(String.format("Valid Records Successfully Imported: %d\n", valid));
                        sb.append(String.format("Duplicate Records Skipped: %d\n", duplicates));
                        sb.append(String.format("Malformed Rows Identified: %d\n\n", malformed));

                        JsonNode details = res.path("malformedRowDetails");
                        if (details.isArray()) {
                            int lineNo = 1;
                            for (JsonNode m : details) {
                                String text = m.asText();
                                sb.append("• ").append(text).append("\n");
                                malformedTableModel.addRow(new Object[]{lineNo++, "Raw Content", "Validation Error", text});
                            }
                        }

                        importLogsArea.setText(sb.toString());
                        loadRecords();
                        JOptionPane.showMessageDialog(CrimeScopeDesktopApp.this,
                                String.format("Import Complete: %d valid records saved, %d malformed, %d duplicates skipped.", valid, malformed, duplicates),
                                "Batch Import Finished", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception e) {
                    importLogsArea.setText("Import Failed: " + e.getMessage());
                }
            }
        }.execute();
    }

    private void saveCsvTemplate() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("crimescope_template.csv"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            try {
                String template = restClient.downloadTemplate();
                try (FileWriter fw = new FileWriter(chooser.getSelectedFile())) {
                    fw.write(template);
                }
                JOptionPane.showMessageDialog(this, "Template saved successfully to " + chooser.getSelectedFile().getName(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error saving template: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportRecordsToCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("crimescope_export.csv"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(chooser.getSelectedFile())) {
                fw.write("id,district,category,year,cases,arrests,convictions,clearance_pct,status,source_ref\n");
                for (int i = 0; i < recordsTableModel.getRowCount(); i++) {
                    StringBuilder line = new StringBuilder();
                    for (int j = 0; j < recordsTableModel.getColumnCount(); j++) {
                        Object val = recordsTableModel.getValueAt(i, j);
                        line.append("\"").append(val != null ? val.toString() : "").append("\"");
                        if (j < recordsTableModel.getColumnCount() - 1) line.append(",");
                    }
                    fw.write(line.toString() + "\n");
                }
                JOptionPane.showMessageDialog(this, "Records exported successfully to " + chooser.getSelectedFile().getName(),
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error exporting CSV: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void executeDirectJdbcQuery() {
        String distStr = (String) jdbcDistrictCombo.getSelectedItem();
        String distCode = (distStr != null && !distStr.startsWith("All")) ? distStr.substring(0, 3) : null;

        String yearStr = (String) jdbcYearCombo.getSelectedItem();
        Integer year = (yearStr != null && !yearStr.startsWith("All")) ? Integer.parseInt(yearStr) : null;

        new SwingWorker<JsonNode, Void>() {
            @Override
            protected JsonNode doInBackground() throws Exception {
                return restClient.runJdbcDemo(distCode, year);
            }

            @Override
            protected void done() {
                try {
                    JsonNode res = get();
                    jdbcTableModel.setRowCount(0);
                    if (res != null) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("=== DIRECT JDBC PREPAREDSTATEMENT EXECUTION ===\n");
                        sb.append(String.format("Database Product: %s (v%s)\n", res.path("databaseProductName").asText(), res.path("databaseProductVersion").asText()));
                        sb.append(String.format("Driver Name: %s (v%s)\n", res.path("driverName").asText(), res.path("driverVersion").asText()));
                        sb.append(String.format("Transaction Isolation Level: %s | AutoCommit: %s\n", res.path("transactionIsolationLevel").asText(), res.path("autoCommitEnabled").asText()));
                        sb.append(String.format("Execution Time: %d ms | Returned Rows: %d\n", res.path("executionTimeMs").asLong(), res.path("returnedRowCount").asInt()));
                        sb.append(String.format("Executed SQL: %s\n", res.path("executedSql").asText()));

                        jdbcTelemetryArea.setText(sb.toString());

                        JsonNode records = res.path("records");
                        if (records.isArray()) {
                            for (JsonNode r : records) {
                                jdbcTableModel.addRow(new Object[]{
                                        r.path("districtCode").asText(),
                                        r.path("districtName").asText(),
                                        r.path("categoryName").asText(),
                                        r.path("reportingYear").asInt(),
                                        r.path("caseCount").asInt(),
                                        r.path("arrestCount").asText("-"),
                                        r.path("convictedCount").asText("-")
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    jdbcTelemetryArea.setText("Direct JDBC Call Error: " + e.getMessage());
                }
            }
        }.execute();
    }

    // --- Cyber UI Styling Helpers ---

    private JButton createCyberButton(String text, Color accentColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(accentColor);
        btn.setBackground(new Color(24, 32, 47));
        btn.setBorder(new LineBorder(accentColor, 1));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void styleDarkTable(JTable table) {
        table.setBackground(CARD_BG);
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(31, 41, 55));
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setBackground(new Color(15, 23, 42));
        table.getTableHeader().setForeground(CYAN_NEON);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    public static void main(String[] args) {
        try {
            FlatDarkLaf.setup();
            UIManager.put("TabbedPane.selectedBackground", new Color(24, 32, 47));
            UIManager.put("TabbedPane.selectedForeground", CYAN_NEON);
            UIManager.put("TabbedPane.underlineColor", CYAN_NEON);
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            CrimeScopeDesktopApp app = new CrimeScopeDesktopApp();
            app.setVisible(true);
        });
    }
}
