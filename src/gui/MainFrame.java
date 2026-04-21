package gui;

import data.DataLoader;
import data.PreProcessor;
import classifier.KNNClassifier;
import classifier.DecisionTreeClassifier;
import evaluation.Evaluator;
import model.UserRecord;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainFrame extends JFrame {

    // --- Arayüz Bileşenleri ---
    private JRadioButton rbSingleFile;
    private JRadioButton rbSeparateFiles;
    private JButton btnSelectSingleFile;
    private JButton btnSelectTrainFile;
    private JButton btnSelectTestFile;
    private JButton btnLoadData;
    private JLabel lblSingleFile;
    private JLabel lblTrainFile;
    private JLabel lblTestFile;
    private File singleFile;
    private File trainFile;
    private File testFile;
    private JRadioButton rbKnnOnly;
    private JRadioButton rbDtOnly;
    private JRadioButton rbCompare;
    private JButton btnRunModel;

    // Parametre Bileşenleri (Fake olanlar silindi)
    private JTextField kValueField;
    private JTextField testRatioField;
    private JTextArea resultArea;
    private BarChartPanel chartPanel;

    // --- Backend Sınıfları ---
    private List<UserRecord> allData; 
    private List<UserRecord> trainDataLoad; 
    private List<UserRecord> testDataLoad; 
    private boolean isSeparateMode = false; 

    private Evaluator evaluator;

    public MainFrame() {
        setTitle("Sınıflandırma Algoritmaları Analizi (KNN vs Karar Ağacı)");
        setSize(950, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initUI();
        evaluator = new Evaluator();
    }

    private void initUI() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel dataPanel = new JPanel(new GridBagLayout());
        dataPanel.setBorder(BorderFactory.createTitledBorder("Veri Yükleme Alanı"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        rbSingleFile = new JRadioButton("Tüm Veri Kümesini Yükle", true);
        rbSeparateFiles = new JRadioButton("Eğitim ve Test Verilerini Ayrı Yükle");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbSingleFile);
        bg.add(rbSeparateFiles);

        btnSelectSingleFile = new JButton("Dosya Seç");
        btnSelectTrainFile = new JButton("Eğitim Dosyası Seç");
        btnSelectTestFile = new JButton("Test Dosyası Seç");
        btnLoadData = new JButton("Verileri Yükle");
        btnLoadData.setBackground(new Color(100, 149, 237));
        btnLoadData.setForeground(Color.WHITE);

        lblSingleFile = new JLabel("Dosya seçilmedi.");
        lblTrainFile = new JLabel("Dosya seçilmedi.");
        lblTestFile = new JLabel("Dosya seçilmedi.");

        btnSelectTrainFile.setEnabled(false);
        btnSelectTestFile.setEnabled(false);
        lblTrainFile.setEnabled(false);
        lblTestFile.setEnabled(false);

        rbSingleFile.addActionListener(e -> toggleDataMode(true));
        rbSeparateFiles.addActionListener(e -> toggleDataMode(false));

        btnSelectSingleFile.addActionListener(e -> singleFile = selectFile(lblSingleFile));
        btnSelectTrainFile.addActionListener(e -> trainFile = selectFile(lblTrainFile));
        btnSelectTestFile.addActionListener(e -> testFile = selectFile(lblTestFile));
        btnLoadData.addActionListener(e -> loadSelectedData());

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3; dataPanel.add(rbSingleFile, gbc);
        gbc.gridwidth = 1; gbc.gridy = 1; dataPanel.add(btnSelectSingleFile, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; dataPanel.add(lblSingleFile, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3; dataPanel.add(rbSeparateFiles, gbc);
        gbc.gridwidth = 1; gbc.gridy = 3; dataPanel.add(btnSelectTrainFile, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; dataPanel.add(lblTrainFile, gbc);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1; dataPanel.add(btnSelectTestFile, gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; dataPanel.add(lblTestFile, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER; dataPanel.add(btnLoadData, gbc);

        JPanel modelAndParamPanel = new JPanel();
        modelAndParamPanel.setLayout(new BoxLayout(modelAndParamPanel, BoxLayout.Y_AXIS));
        modelAndParamPanel.setBorder(BorderFactory.createTitledBorder("Model Seçimi ve Hiperparametreler"));

        JPanel modelSelectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        rbKnnOnly = new JRadioButton("Sadece KNN");
        rbDtOnly = new JRadioButton("Sadece Karar Ağacı");
        rbCompare = new JRadioButton("Karşılaştırmalı Analiz (İkisi Birlikte)", true);

        ButtonGroup modelGroup = new ButtonGroup();
        modelGroup.add(rbKnnOnly);
        modelGroup.add(rbDtOnly);
        modelGroup.add(rbCompare);

        modelSelectionPanel.add(new JLabel("Çalıştırılacak Model:"));
        modelSelectionPanel.add(rbKnnOnly);
        modelSelectionPanel.add(rbDtOnly);
        modelSelectionPanel.add(rbCompare);

        JPanel paramPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        paramPanel.add(new JLabel("Test Oranı (%):"));
        testRatioField = new JTextField("20", 3);
        paramPanel.add(testRatioField);

        paramPanel.add(new JLabel(" |  KNN (K Değeri):"));
        kValueField = new JTextField("5", 3);
        paramPanel.add(kValueField);

        // FIX 6: Fake "distanceMetricBox" ve "dtCriterionBox" arayüzden ve koddan silindi.

        modelAndParamPanel.add(modelSelectionPanel);
        modelAndParamPanel.add(paramPanel);

        rbKnnOnly.addActionListener(e -> updateParamVisibility());
        rbDtOnly.addActionListener(e -> updateParamVisibility());
        rbCompare.addActionListener(e -> updateParamVisibility());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnRunModel = new JButton("Seçili Modeli Çalıştır");
        btnRunModel.setEnabled(false);
        btnRunModel.setBackground(new Color(46, 139, 87));
        btnRunModel.setForeground(Color.WHITE);
        btnRunModel.setFont(new Font("Arial", Font.BOLD, 14));

        buttonPanel.add(btnRunModel);

        topPanel.add(dataPanel);
        topPanel.add(modelAndParamPanel);
        topPanel.add(buttonPanel);
        add(topPanel, BorderLayout.NORTH);

        btnRunModel.addActionListener(e -> {
            chartPanel.updateKNN(0);
            chartPanel.updateDT(0);
            resultArea.append("\n--------------------------------------------------\n");

            if (rbKnnOnly.isSelected()) {
                runKNNAlgorithm();
            } else if (rbDtOnly.isSelected()) {
                runDTAlgorithm();
            } else {
                runKNNAlgorithm();
                runDTAlgorithm();
            }
        });

        chartPanel = new BarChartPanel();
        chartPanel.setBorder(BorderFactory.createTitledBorder("Doğruluk (Accuracy) Grafiği"));
        add(chartPanel, BorderLayout.CENTER);

        resultArea = new JTextArea(10, 50);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Deneysel Sonuçlar ve Loglar"));
        add(scrollPane, BorderLayout.SOUTH);
    }

    private void updateParamVisibility() {
        boolean showKNN = rbKnnOnly.isSelected() || rbCompare.isSelected();
        kValueField.setEnabled(showKNN);
    }

    private void toggleDataMode(boolean isSingle) {
        isSeparateMode = !isSingle;
        btnSelectSingleFile.setEnabled(isSingle);
        lblSingleFile.setEnabled(isSingle);

        btnSelectTrainFile.setEnabled(!isSingle);
        btnSelectTestFile.setEnabled(!isSingle);
        lblTrainFile.setEnabled(!isSingle);
        lblTestFile.setEnabled(!isSingle);

        testRatioField.setEnabled(isSingle);
    }

    private File selectFile(JLabel labelToUpdate) {
        JFileChooser fileChooser = new JFileChooser(new File("data"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            labelToUpdate.setText(selected.getName());
            return selected;
        }
        return null;
    }

    private void loadSelectedData() {
        btnLoadData.setEnabled(false);
        resultArea.append(">> Veriler yükleniyor... Lütfen bekleyin.\n");

        new Thread(() -> {
            try {
                DataLoader loader = new DataLoader();

                if (!isSeparateMode) {
                    if (singleFile == null)
                        throw new Exception("Lütfen tüm veri kümesi için bir dosya seçin.");
                    allData = loader.load(singleFile.getAbsolutePath());
                    resultArea.append(">> Tekil Veri Seti Başarıyla Yüklendi! Toplam Kayıt: " + allData.size() + "\n");
                } else {
                    if (trainFile == null || testFile == null)
                        throw new Exception("Lütfen hem Eğitim hem de Test dosyalarını seçin.");
                    trainDataLoad = loader.load(trainFile.getAbsolutePath());
                    testDataLoad = loader.load(testFile.getAbsolutePath());

                    resultArea.append(">> Eğitim ve Test Veri Setleri Başarıyla Yüklendi!\n");
                    resultArea.append("   Eğitim Kayıt Sayısı: " + trainDataLoad.size() + "\n");
                    resultArea.append("   Test Kayıt Sayısı: " + testDataLoad.size() + "\n");
                }

                SwingUtilities.invokeLater(() -> {
                    resultArea.append(">> Algoritmaları test etmeye başlayabilirsiniz.\n\n");
                    btnRunModel.setEnabled(true);
                    btnLoadData.setEnabled(true);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    resultArea.append(">> HATA: " + ex.getMessage() + "\n\n");
                    btnLoadData.setEnabled(true);
                });
            }
        }).start();
    }

    private List<List<UserRecord>> getTrainTestSplits() throws Exception {
        if (isSeparateMode) {
            return List.of(trainDataLoad, testDataLoad);
        } else {
            int testRatio = Integer.parseInt(testRatioField.getText());
            if (testRatio < 1 || testRatio > 99)
                throw new Exception("Test oranı 1 ile 99 arasında olmalıdır.");

            Collections.shuffle(allData, new Random(42)); 
            int split = (int) (allData.size() * (100 - testRatio) / 100.0);
            List<UserRecord> train = allData.subList(0, split);
            List<UserRecord> test = allData.subList(split, allData.size());
            return List.of(train, test);
        }
    }

    private void runKNNAlgorithm() {
        try {
            int k = Integer.parseInt(kValueField.getText());
            List<List<UserRecord>> splits = getTrainTestSplits();
            List<UserRecord> train = splits.get(0);
            List<UserRecord> test = splits.get(1);

            resultArea.append(String.format(">> KNN (K=%d) Başlatıldı... (Eğitim: %d, Test: %d)\n", k, train.size(),
                    test.size()));

            KNNClassifier knn = new KNNClassifier(k, new PreProcessor());

            knn.trainWithTiming(train);
            Evaluator.EvaluationResult res = (Evaluator.EvaluationResult) evaluator.evaluate(knn, test);

            long executionTime = knn.getExecutionTimeMs();

            resultArea.append("--------------------------------------------------\n");
            resultArea.append(">> KNN PERFORMANS METRİKLERİ:\n");
            resultArea.append(res.toString() + "\n");
            resultArea.append(String.format(">> Toplam Çalışma Süresi: %d ms\n", executionTime));
            resultArea.append("--------------------------------------------------\n");

            double accuracyPercentage = (res.accuracy <= 1.0) ? res.accuracy * 100 : res.accuracy;
            chartPanel.updateKNN(accuracyPercentage);

        } catch (Exception ex) {
            resultArea.append("Hata (KNN): " + ex.getMessage() + "\n");
        }
    }

    private void runDTAlgorithm() {
        try {
            List<List<UserRecord>> splits = getTrainTestSplits();
            List<UserRecord> train = splits.get(0);
            List<UserRecord> test = splits.get(1);

            resultArea.append(
                    String.format(">> Karar Ağacı Başlatıldı... (Eğitim: %d, Test: %d)\n", train.size(), test.size()));

            DecisionTreeClassifier dt = new DecisionTreeClassifier(new PreProcessor());

            dt.trainWithTiming(train);
            Evaluator.EvaluationResult res = (Evaluator.EvaluationResult) evaluator.evaluate(dt, test);

            long executionTime = dt.getExecutionTimeMs();

            resultArea.append("--------------------------------------------------\n");
            resultArea.append(">> KARAR AĞACI PERFORMANS METRİKLERİ:\n");
            resultArea.append(res.toString() + "\n");
            resultArea.append(String.format(">> Toplam Çalışma Süresi: %d ms\n", executionTime));
            resultArea.append("--------------------------------------------------\n");

            double accuracyPercentage = (res.accuracy <= 1.0) ? res.accuracy * 100 : res.accuracy;
            chartPanel.updateDT(accuracyPercentage);

        } catch (Exception ex) {
            resultArea.append("Hata (DT): " + ex.getMessage() + "\n");
        }
    }

    class BarChartPanel extends JPanel {
        private double knnAccuracy = 0.0;
        private double dtAccuracy = 0.0;

        public void updateKNN(double acc) {
            this.knnAccuracy = acc;
            repaint();
        }

        public void updateDT(double acc) {
            this.dtAccuracy = acc;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int maxBarHeight = height - 60;

            g2d.setColor(Color.BLACK);
            g2d.drawLine(50, height - 30, width - 50, height - 30);
            g2d.drawLine(50, height - 30, 50, 20);

            g2d.drawString("100%", 15, 30);
            g2d.drawString("50%", 20, height / 2);
            g2d.drawString("0%", 25, height - 30);

            if (knnAccuracy > 0) {
                int knnBarHeight = (int) ((knnAccuracy / 100.0) * maxBarHeight);
                g2d.setColor(new Color(70, 130, 180));
                g2d.fillRect(width / 4, height - 30 - knnBarHeight, 80, knnBarHeight);
                g2d.setColor(Color.BLACK);
                g2d.drawString(String.format("KNN (%%% .1f)", knnAccuracy), width / 4, height - 10);
            }

            if (dtAccuracy > 0) {
                int dtBarHeight = (int) ((dtAccuracy / 100.0) * maxBarHeight);
                g2d.setColor(new Color(46, 139, 87));
                g2d.fillRect((width / 4) * 3 - 80, height - 30 - dtBarHeight, 80, dtBarHeight);
                g2d.setColor(Color.BLACK);
                g2d.drawString(String.format("DT (%%% .1f)", dtAccuracy), (width / 4) * 3 - 80, height - 10);
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}