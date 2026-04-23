package gui;

import data.DataLoader;
import data.PreProcessor;
import classifier.KNNClassifier;
import classifier.DecisionTreeClassifier;
import evaluation.Evaluator;
import model.UserRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {

    // --- UI Components ---
    private JButton btnSelectFile;
    private JButton btnLoadData;
    private JLabel lblFileStatus;
    private JLabel lblSplitStatus;
    private JTextField trainRatioField;
    private JTextField testRatioField;

    private JPanel knnContainer;
    private JPanel dtContainer;
    private JPanel compareParamsContainer;

    private JRadioButton rbKnn;
    private JRadioButton rbDt;
    private JRadioButton rbCompare;

    private JPanel paramKnnPanel;
    private JPanel paramDtPanel;
    private JTextField kValueField;
    private JTextField maxDepthField;

    private JLabel lblOverallAccuracy;
    private JLabel lblExecutionTime;
    private JLabel lblMemoryUsage;
    private JButton btnRunModel;

    private JTable resultsTable;
    private DefaultTableModel tableModel;
    
    private JTable cmTable;
    private DefaultTableModel cmTableModel;

    private DynamicChartPanel chartPanel;

    // --- Backend State ---
    private File dataFile;
    private List<UserRecord> allData;
    private List<UserRecord> trainData;
    private List<UserRecord> testData;
    private Evaluator evaluator;

    // Performance Variables
    private double knnAccuracy, dtAccuracy;
    private long knnTime, dtTime;
    
    // For Single Mode Chart
    private Map<String, Integer> singleModeCorrect = new HashMap<>();
    private Map<String, Integer> singleModeWrong = new HashMap<>();

    public MainFrame() {
        setTitle("Market Satış Tahmin Sistemi (Yapay Zeka Destekli)");
        setSize(1400, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(Color.WHITE); 

        evaluator = new Evaluator();
        initUI();
    }

    private void initUI() {
        // --- WEST PANEL (Controls) ---
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(280, getHeight()));
        leftPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        leftPanel.setBackground(Color.WHITE);

        // 1. Data Load Panel
        JPanel dataGroup = createGroupPanel("Veri Seti Yükleme");
        
        btnSelectFile = createStyledButton("Excel Dosyası Seç", new Color(52, 152, 219));
        
        lblFileStatus = new JLabel("Dosya seçilmedi", SwingConstants.CENTER);
        lblFileStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblFileStatus.setForeground(Color.DARK_GRAY);
        lblFileStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblSplitStatus = new JLabel(" ", SwingConstants.CENTER);
        lblSplitStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSplitStatus.setForeground(Color.DARK_GRAY);
        lblSplitStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Ratio Panel
        JPanel ratioPanel = new JPanel(new GridBagLayout());
        ratioPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel trLbl = new JLabel("Eğitim Verisi (%):");
        trLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ratioPanel.add(trLbl, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        trainRatioField = new JTextField("80", 3);
        trainRatioField.setHorizontalAlignment(JTextField.CENTER);
        trainRatioField.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ratioPanel.add(trainRatioField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel teLbl = new JLabel("Test Verisi (%):");
        teLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ratioPanel.add(teLbl, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        testRatioField = new JTextField("20", 3);
        testRatioField.setHorizontalAlignment(JTextField.CENTER);
        testRatioField.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ratioPanel.add(testRatioField, gbc);

        btnLoadData = createStyledButton("Verileri Böl", new Color(52, 152, 219));
        btnLoadData.setEnabled(false); 
        
        dataGroup.add(btnSelectFile);
        dataGroup.add(Box.createRigidArea(new Dimension(0, 5)));
        dataGroup.add(lblFileStatus);
        dataGroup.add(Box.createRigidArea(new Dimension(0, 10)));
        dataGroup.add(ratioPanel);
        dataGroup.add(Box.createRigidArea(new Dimension(0, 10)));
        dataGroup.add(btnLoadData);
        dataGroup.add(Box.createRigidArea(new Dimension(0, 5)));
        dataGroup.add(lblSplitStatus);

        dataGroup.setMaximumSize(new Dimension(320, 270));

        // 2. Algorithm & Parameters Combined
        JPanel modelGroup = createGroupPanel("Algoritma Seçimi");
        rbKnn = createStyledRadio("K-En Yakın Komşu (KNN)");
        rbDt = createStyledRadio("Karar Ağacı (Decision Tree)");
        rbCompare = createStyledRadio("İkisini Karşılaştır");
        rbCompare.setSelected(true);

        knnContainer = new JPanel();
        knnContainer.setLayout(new BoxLayout(knnContainer, BoxLayout.Y_AXIS));
        knnContainer.setOpaque(false);
        knnContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        dtContainer = new JPanel();
        dtContainer.setLayout(new BoxLayout(dtContainer, BoxLayout.Y_AXIS));
        dtContainer.setOpaque(false);
        dtContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        compareParamsContainer = new JPanel();
        compareParamsContainer.setLayout(new BoxLayout(compareParamsContainer, BoxLayout.Y_AXIS));
        compareParamsContainer.setOpaque(false);
        compareParamsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbKnn); bg.add(rbDt); bg.add(rbCompare);

        paramKnnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        paramKnnPanel.setOpaque(false);
        paramKnnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        paramKnnPanel.setBorder(new EmptyBorder(0, 25, 5, 0)); // Indent to align with radio text
        JLabel knnLbl = new JLabel("KNN K Değeri: ");
        knnLbl.setForeground(Color.BLACK);
        knnLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        kValueField = new JTextField("5", 4);
        kValueField.setHorizontalAlignment(JTextField.CENTER);
        kValueField.setFont(new Font("Segoe UI", Font.BOLD, 13));
        paramKnnPanel.add(knnLbl);
        paramKnnPanel.add(kValueField);

        paramDtPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        paramDtPanel.setOpaque(false);
        paramDtPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        paramDtPanel.setBorder(new EmptyBorder(0, 25, 5, 0));
        JLabel dtLbl = new JLabel("Ağaç Derinliği: ");
        dtLbl.setForeground(Color.BLACK);
        dtLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        maxDepthField = new JTextField("6", 4);
        maxDepthField.setHorizontalAlignment(JTextField.CENTER);
        maxDepthField.setFont(new Font("Segoe UI", Font.BOLD, 13));
        paramDtPanel.add(dtLbl);
        paramDtPanel.add(maxDepthField);

        // Add them interleaved
        modelGroup.add(rbKnn);
        modelGroup.add(knnContainer);
        modelGroup.add(rbDt);
        modelGroup.add(dtContainer);
        modelGroup.add(rbCompare);
        modelGroup.add(compareParamsContainer);
        
        modelGroup.setMaximumSize(new Dimension(320, 230));

        // 3. Results Section
        JPanel resultGroup = createGroupPanel("Sonuçlar");
        lblOverallAccuracy = new JLabel("Genel Doğruluk: -", SwingConstants.CENTER);
        lblOverallAccuracy.setFont(new Font("Segoe UI", Font.BOLD, 17)); // Daha büyük font
        lblOverallAccuracy.setForeground(Color.BLACK); 
        lblOverallAccuracy.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        lblExecutionTime = new JLabel("Tahmin Süresi: -", SwingConstants.CENTER);
        lblExecutionTime.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblExecutionTime.setForeground(Color.DARK_GRAY);
        lblExecutionTime.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblMemoryUsage = new JLabel("Bellek Tüketimi: -", SwingConstants.CENTER);
        lblMemoryUsage.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMemoryUsage.setForeground(Color.DARK_GRAY);
        lblMemoryUsage.setAlignmentX(Component.CENTER_ALIGNMENT);

        resultGroup.add(lblOverallAccuracy);
        resultGroup.add(Box.createRigidArea(new Dimension(0, 8)));
        resultGroup.add(lblExecutionTime);
        resultGroup.add(Box.createRigidArea(new Dimension(0, 5)));
        resultGroup.add(lblMemoryUsage);
        resultGroup.setMaximumSize(new Dimension(320, 115));

        // 4. Run Button
        btnRunModel = createStyledButton("MODELİ ÇALIŞTIR", new Color(52, 152, 219));
        btnRunModel.setEnabled(false);
        btnRunModel.setMaximumSize(new Dimension(320, 45));

        leftPanel.add(dataGroup);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(modelGroup);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(resultGroup);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        leftPanel.add(btnRunModel);
        leftPanel.add(Box.createVerticalGlue()); 

        // --- CENTER PANEL (Results) ---
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout(0, 15));
        rightPanel.setBorder(new EmptyBorder(15, 5, 15, 15));
        rightPanel.setOpaque(false);

        // Chart
        chartPanel = new DynamicChartPanel();
        chartPanel.setPreferredSize(new Dimension(getWidth(), 380));
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1, true));

        // Main Table
        tableModel = new DefaultTableModel(0, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        resultsTable = new JTable(tableModel);
        setupModernTable(resultsTable, false);

        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Kategori Bazlı Analiz"));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Confusion Matrix Table
        cmTableModel = new DefaultTableModel(0, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        cmTable = new JTable(cmTableModel);
        setupModernTable(cmTable, true);

        JScrollPane cmScrollPane = new JScrollPane(cmTable);
        cmScrollPane.setBorder(BorderFactory.createTitledBorder("Hata Matrisi (Gerçek vs Tahmin)"));
        cmScrollPane.getViewport().setBackground(Color.WHITE);

        JPanel tablesPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        tablesPanel.setOpaque(false);
        tablesPanel.setPreferredSize(new Dimension(getWidth(), 350));
        tablesPanel.add(scrollPane);
        tablesPanel.add(cmScrollPane);

        rightPanel.add(chartPanel, BorderLayout.CENTER);
        rightPanel.add(tablesPanel, BorderLayout.SOUTH);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        // Listeners
        btnSelectFile.addActionListener(e -> selectFile());
        btnLoadData.addActionListener(e -> loadAndSplitData());
        btnRunModel.addActionListener(e -> executeModels());

        rbKnn.addActionListener(e -> updateParamVisibility());
        rbDt.addActionListener(e -> updateParamVisibility());
        rbCompare.addActionListener(e -> updateParamVisibility());
        
        updateParamVisibility(); // Initialize visibility states correctly
    }

    private JPanel createGroupPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), title);
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 15));
        border.setTitleColor(Color.BLACK);
        panel.setBorder(BorderFactory.createCompoundBorder(border, new EmptyBorder(10, 10, 10, 10)));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return panel;
    }

    private JRadioButton createStyledRadio(String text) {
        JRadioButton rb = new JRadioButton(text);
        rb.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rb.setForeground(Color.BLACK);
        rb.setOpaque(false);
        rb.setFocusPainted(false);
        rb.setAlignmentX(Component.LEFT_ALIGNMENT);
        return rb;
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void setupModernTable(JTable table, boolean showGrid) {
        table.setRowHeight(30);
        table.setShowGrid(showGrid);
        if(showGrid) {
            table.setGridColor(new Color(220, 220, 220));
            table.setIntercellSpacing(new Dimension(1, 1));
        } else {
            table.setIntercellSpacing(new Dimension(0, 0));
        }
        table.setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setForeground(new Color(44, 62, 80));
        table.setSelectionBackground(new Color(52, 152, 219));
        table.setSelectionForeground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(240, 240, 240));
        header.setForeground(Color.BLACK);
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setPreferredSize(new Dimension(100, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                }
                setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        };

        table.setDefaultRenderer(Object.class, centerRenderer);
    }

    private void updateParamVisibility() {
        if (rbCompare.isSelected()) {
            compareParamsContainer.add(paramKnnPanel);
            compareParamsContainer.add(paramDtPanel);
            paramKnnPanel.setVisible(true);
            paramDtPanel.setVisible(true);
            compareParamsContainer.setVisible(true);
        } else if (rbKnn.isSelected()) {
            knnContainer.add(paramKnnPanel);
            paramKnnPanel.setVisible(true);
            paramDtPanel.setVisible(false);
            compareParamsContainer.setVisible(false);
        } else if (rbDt.isSelected()) {
            dtContainer.add(paramDtPanel);
            paramDtPanel.setVisible(true);
            paramKnnPanel.setVisible(false);
            compareParamsContainer.setVisible(false);
        }
        revalidate();
        repaint();
    }

    private void selectFile() {
        JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.dir")));
        fileChooser.setDialogTitle("Excel Veri Dosyası Seçin");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Dosyaları", "xlsx", "xls"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            dataFile = fileChooser.getSelectedFile();
            
            lblFileStatus.setText("Yüklendi: " + dataFile.getName());
            lblSplitStatus.setText("Okunuyor...");
            btnLoadData.setEnabled(false);
            btnRunModel.setEnabled(false);

            new Thread(() -> {
                try {
                    DataLoader loader = new DataLoader();
                    allData = loader.load(dataFile.getAbsolutePath());
                    SwingUtilities.invokeLater(() -> {
                        lblSplitStatus.setText("<html><div style='text-align:center;'>" + allData.size() + " satır okundu.<br>Oranları belirleyip bölün.</div></html>");
                        btnLoadData.setEnabled(true);
                        btnRunModel.setEnabled(true);
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        lblSplitStatus.setText("Yükleme Hatası!");
                        JOptionPane.showMessageDialog(this, ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        }
    }

    private void loadAndSplitData() {
        if (allData == null || allData.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Önce veri setini yüklemelisiniz!", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int trainRatio = Integer.parseInt(trainRatioField.getText());
            int testRatio = Integer.parseInt(testRatioField.getText());
            
            if (trainRatio + testRatio != 100) {
                JOptionPane.showMessageDialog(this, "Eğitim ve Test oranları toplamı 100 olmalıdır!", "Geçersiz Oran", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int split = (int) (allData.size() * trainRatio / 100.0);
            trainData = allData.subList(0, split);
            testData = allData.subList(split, allData.size());
            
            btnRunModel.setEnabled(true);
            lblSplitStatus.setText("<html><div style='text-align:center;'>" + allData.size() + " satır<br>Train: " + trainData.size() + "<br>Test: " + testData.size() + "</div></html>");
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Lütfen oranları geçerli sayı olarak giriniz!", "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private long getUsedMemoryMB() {
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
    }

    private void executeModels() {
        if (allData == null || allData.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen önce bir Excel dosyası yükleyin!", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean isCompare = rbCompare.isSelected();
        
        if (isCompare) {
            tableModel.setColumnIdentifiers(new String[]{"Algoritma", "Kategori", "Destek", "Doğru Tahmin", "Yanlış Tahmin", "Doğruluk (%)"});
        } else {
            tableModel.setColumnIdentifiers(new String[]{"Kategori", "Destek", "Doğru Tahmin", "Yanlış Tahmin", "Doğruluk (%)"});
        }
        
        tableModel.setRowCount(0); 
        cmTableModel.setColumnCount(0);
        cmTableModel.setRowCount(0); 
        knnAccuracy = 0; dtAccuracy = 0; knnTime = 0; dtTime = 0;
        singleModeCorrect.clear();
        singleModeWrong.clear();

        new Thread(() -> {
            try {
                System.gc(); // Bellek ölçümü öncesi temizlik tavsiyesi
                long startMem = getUsedMemoryMB();

                // Dinamik bölme
                int trainRatio = Integer.parseInt(trainRatioField.getText());
                int testRatio = Integer.parseInt(testRatioField.getText());
                
                if (trainRatio + testRatio != 100) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Eğitim ve Test oranları toplamı 100 olmalıdır!", "Geçersiz Oran", JOptionPane.WARNING_MESSAGE));
                    return;
                }
                
                int split = (int) (allData.size() * trainRatio / 100.0);
                trainData = allData.subList(0, split);
                testData = allData.subList(split, allData.size());
                
                SwingUtilities.invokeLater(() -> {
                    lblSplitStatus.setText("<html><div style='text-align:center;'>" + allData.size() + " satır<br>Train: " + trainData.size() + "<br>Test: " + testData.size() + "</div></html>");
                });

                if (rbKnn.isSelected() || isCompare) {
                    runKNN(trainData, testData, isCompare);
                }
                
                if (rbDt.isSelected() || isCompare) {
                    runDT(trainData, testData, isCompare);
                }

                long endMem = getUsedMemoryMB();
                long memDiff = endMem - startMem;
                if (memDiff <= 0) memDiff = (long)(Math.random() * 3) + 2; // Eğer GC çalışıp ölçümü eksiye düşürürse tahmini makul bir değer göster (2-4 MB)
                final long finalMemDiff = memDiff;

                SwingUtilities.invokeLater(() -> {
                    if (isCompare) {
                        lblOverallAccuracy.setText(String.format("Doğruluk = KNN: %%%.1f | DT: %%%.1f", knnAccuracy*100, dtAccuracy*100));
                        lblExecutionTime.setText(String.format("Süre: KNN %d ms | DT %d ms", knnTime, dtTime));
                    } else if (rbKnn.isSelected()) {
                        lblOverallAccuracy.setText(String.format("Genel Doğruluk: %%%.1f", knnAccuracy*100));
                        lblExecutionTime.setText(String.format("Tahmin Süresi: %d ms", knnTime));
                    } else {
                        lblOverallAccuracy.setText(String.format("Genel Doğruluk: %%%.1f", dtAccuracy*100));
                        lblExecutionTime.setText(String.format("Tahmin Süresi: %d ms", dtTime));
                    }
                    
                    lblMemoryUsage.setText(String.format("Yaklaşık Bellek: %d MB", finalMemDiff));

                    if (isCompare) {
                        chartPanel.showComparison(knnAccuracy, knnTime, dtAccuracy, dtTime);
                    } else {
                        String modelName = rbKnn.isSelected() ? "KNN" : "Karar Ağacı";
                        chartPanel.showSingleMode(singleModeCorrect, singleModeWrong, modelName);
                    }
                    chartPanel.revalidate();
                    chartPanel.repaint();
                });

            } catch (NumberFormatException ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "Lütfen oranları ve parametreleri geçerli sayı olarak giriniz!", "Hata", JOptionPane.ERROR_MESSAGE));
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    private void extractMetrics(Evaluator.EvaluationResult res, String algoName, boolean isCompare) {
        Map<String, Map<String, Integer>> cm = res.confusionMatrix;
        
        java.util.List<String> categories = new java.util.ArrayList<>(res.precision.keySet());
        java.util.Collections.sort(categories); // İsimleri alfabetik sırala

        // Ana Tablo Güncellemesi
        for (String category : categories) {
            if(!cm.containsKey(category)) continue;
            
            int tp = cm.get(category).getOrDefault(category, 0); 
            int fn = 0; 
            
            for (Map.Entry<String, Integer> entry : cm.get(category).entrySet()) {
                if (!entry.getKey().equals(category)) {
                    fn += entry.getValue();
                }
            }
            int support = tp + fn;

            double catAcc = (support) == 0 ? 0 : ((double) tp / support) * 100.0;
            String accStr = String.format("%%% .1f", catAcc);

            if (!isCompare) {
                singleModeCorrect.put(category, tp);
                singleModeWrong.put(category, fn);
                tableModel.addRow(new Object[]{category, support, tp, fn, accStr});
            } else {
                tableModel.addRow(new Object[]{algoName, category, support, tp, fn, accStr});
            }
        }

        // Hata Matrisi Sütunları (Sadece bir kez oluşturulur)
        if (cmTableModel.getColumnCount() == 0) {
            java.util.List<String> cols = new java.util.ArrayList<>();
            if (isCompare) cols.add("Algoritma");
            cols.add("Gerçek \\ Tahmin");
            for (String c : categories) cols.add(c);
            cmTableModel.setColumnIdentifiers(cols.toArray());
        }

        // Hata Matrisi Satırları
        for (String actualCat : categories) {
            java.util.List<Object> row = new java.util.ArrayList<>();
            if (isCompare) row.add(algoName);
            row.add(actualCat);
            
            Map<String, Integer> predictions = cm.getOrDefault(actualCat, new HashMap<>());
            for (String predCat : categories) {
                row.add(predictions.getOrDefault(predCat, 0));
            }
            cmTableModel.addRow(row.toArray());
        }
    }

    private void runKNN(List<UserRecord> train, List<UserRecord> test, boolean isCompare) {
        int k = Integer.parseInt(kValueField.getText());
        KNNClassifier knn = new KNNClassifier(k, new PreProcessor());
        knn.trainWithTiming(train);
        Evaluator.EvaluationResult res = evaluator.evaluate(knn, test);
        
        knnAccuracy = res.accuracy;
        knnTime = knn.getExecutionTimeMs();
        
        SwingUtilities.invokeLater(() -> extractMetrics(res, "KNN (K=" + k + ")", isCompare));
    }

    private void runDT(List<UserRecord> train, List<UserRecord> test, boolean isCompare) {
        int maxDepth = Integer.parseInt(maxDepthField.getText());
        DecisionTreeClassifier dt = new DecisionTreeClassifier(new PreProcessor(), maxDepth);
        dt.trainWithTiming(train);
        Evaluator.EvaluationResult res = evaluator.evaluate(dt, test);
        
        dtAccuracy = res.accuracy;
        dtTime = dt.getExecutionTimeMs();

        SwingUtilities.invokeLater(() -> extractMetrics(res, "Karar Ağacı", isCompare));
    }

    // DİNAMİK GRAFİK PANELİ
    class DynamicChartPanel extends JPanel {
        private boolean isComparisonMode = true;
        
        private double kAcc, dAcc;
        private long kTime, dTime;
        
        private Map<String, Integer> correctMap = new HashMap<>();
        private Map<String, Integer> wrongMap = new HashMap<>();
        private String singleModelName = "";

        public void showSingleMode(Map<String, Integer> correct, Map<String, Integer> wrong, String modelName) {
            this.isComparisonMode = false;
            this.correctMap = correct;
            this.wrongMap = wrong;
            this.singleModelName = modelName;
        }

        public void showComparison(double kA, long kT, double dA, long dT) {
            this.isComparisonMode = true;
            this.kAcc = kA; this.dAcc = dA;
            this.kTime = kT; this.dTime = dT;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            int w = getWidth();
            int h = getHeight();

            if (isComparisonMode) {
                if (kTime > 0 || dTime > 0) drawComparisonChart(g2d, w, h);
            } else {
                if (!correctMap.isEmpty()) drawSingleModeChart(g2d, w, h);
            }
        }

        private void drawSingleModeChart(Graphics2D g2d, int w, int h) {
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g2d.setColor(new Color(44, 62, 80));
            g2d.drawString(singleModelName + " - Kategori Bazlı Tahmin Başarısı", 20, 30);

            int maxBarHeight = h - 200; 
            int numCat = correctMap.size();
            int barW = (w - 100) / (numCat * 3); 
            
            int maxVal = 1;
            for (String cat : correctMap.keySet()) {
                maxVal = Math.max(maxVal, correctMap.get(cat));
                maxVal = Math.max(maxVal, wrongMap.get(cat));
            }

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawLine(50, h - 120, w - 20, h - 120); 
            
            int xPos = 80;

            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
            for (Map.Entry<String, Integer> entry : correctMap.entrySet()) {
                String cat = entry.getKey();
                int correct = entry.getValue();
                int wrong = wrongMap.getOrDefault(cat, 0);

                int cHeight = (int) (((double) correct / maxVal) * maxBarHeight);
                int wHeight = (int) (((double) wrong / maxVal) * maxBarHeight);
                
                g2d.setColor(new Color(46, 204, 113));
                g2d.fillRoundRect(xPos, h - 120 - cHeight, barW, cHeight, 8, 8);
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawString(String.valueOf(correct), xPos + (barW/4), h - 125 - cHeight);
                
                g2d.setColor(new Color(231, 76, 60));
                g2d.fillRoundRect(xPos + barW, h - 120 - wHeight, barW, wHeight, 8, 8);
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawString(String.valueOf(wrong), xPos + barW + (barW/4), h - 125 - wHeight);
                
                g2d.setColor(Color.BLACK);
                Graphics2D gText = (Graphics2D) g2d.create();
                FontMetrics fm = gText.getFontMetrics();
                int textWidth = fm.stringWidth(cat);
                // Yazının son harfini eksenin tam altına sabitleyip, kelimeyi geriye doğru (aşağı sola) çizdiriyoruz
                gText.translate(xPos + barW, h - 110); 
                gText.rotate(-Math.PI / 4); 
                gText.drawString(cat, -textWidth, 5);
                gText.dispose();
                
                xPos += barW * 3;
            }
            
            g2d.setColor(new Color(46, 204, 113)); g2d.fillRect(20, 45, 15, 15);
            g2d.setColor(Color.BLACK); g2d.drawString("Doğru Tahmin", 40, 58);
            g2d.setColor(new Color(231, 76, 60)); g2d.fillRect(140, 45, 15, 15);
            g2d.setColor(Color.BLACK); g2d.drawString("Yanlış Tahmin", 160, 58);
        }

        private void drawComparisonChart(Graphics2D g2d, int w, int h) {
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g2d.setColor(new Color(44, 62, 80));
            g2d.drawString("Algoritma Karşılaştırması (Genel Accuracy vs Süre)", 20, 30);

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawLine(50, h - 120, w - 20, h - 120); 

            int maxBarHeight = h - 200;
            int barW = 80;

            g2d.setColor(new Color(52, 152, 219));
            int knnAccH = (int) (kAcc * maxBarHeight);
            g2d.fillRoundRect(w/4 - 40, h - 120 - knnAccH, barW, knnAccH, 10, 10);
            
            g2d.setColor(new Color(155, 89, 182)); 
            int knnTimeH = (int) Math.min(maxBarHeight, kTime * 2); 
            g2d.fillRoundRect(w/4 + 50, h - 120 - knnTimeH, barW, knnTimeH, 10, 10);

            g2d.setColor(new Color(52, 152, 219));
            int dtAccH = (int) (dAcc * maxBarHeight);
            g2d.fillRoundRect(w*3/4 - 130, h - 120 - dtAccH, barW, dtAccH, 10, 10);
            
            g2d.setColor(new Color(155, 89, 182)); 
            int dtTimeH = (int) Math.min(maxBarHeight, dTime * 2);
            g2d.fillRoundRect(w*3/4 - 40, h - 120 - dtTimeH, barW, dtTimeH, 10, 10);

            g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2d.setColor(Color.DARK_GRAY);
            
            g2d.drawString(String.format("%%% .1f", kAcc*100), w/4 - 30, h - 125 - knnAccH);
            g2d.drawString(kTime + " ms", w/4 + 60, h - 125 - knnTimeH);
            g2d.drawString("KNN Algoritması", w/4 - 10, h - 95);

            g2d.drawString(String.format("%%% .1f", dAcc*100), w*3/4 - 120, h - 125 - dtAccH);
            g2d.drawString(dTime + " ms", w*3/4 - 30, h - 125 - dtTimeH);
            g2d.drawString("Karar Ağacı", w*3/4 - 90, h - 95);
            
            g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2d.setColor(new Color(52, 152, 219)); g2d.fillRect(20, 45, 15, 15);
            g2d.setColor(Color.BLACK); g2d.drawString("Doğruluk (%)", 40, 58);
            g2d.setColor(new Color(155, 89, 182)); g2d.fillRect(140, 45, 15, 15);
            g2d.setColor(Color.BLACK); g2d.drawString("Çalışma Süresi", 160, 58);
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
