package editor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextEditor extends JFrame {

    private JTextField searchField;
    private JTextArea textArea;
    private JFileChooser fileChooser;
    private JCheckBox useRegexCheckBox;

    public TextEditor() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        createComponents();

        setTitle("Text editor");
        setVisible(true);
        setMinimumSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(null);
    }

    private void createComponents() {
        createMenu();
        createMainField();
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menu = new JMenu("File");
        menu.setName("MenuFile");

        JMenuItem menuOpen = new JMenuItem("Open");
        menuOpen.setName("MenuOpen");
        menuOpen.addActionListener(new OpenActionListener());

        JMenuItem menuSave = new JMenuItem("Save");
        menuSave.setName("MenuSave");
        menuSave.addActionListener(new SaveActionListener());

        JMenuItem menuExit = new JMenuItem("Exit");
        menuExit.setName("MenuExit");
        menuExit.addActionListener(new ExitActionListener());

        menu.add(menuOpen);
        menu.add(menuSave);
        menu.add(menuExit);

        menuBar.add(menu);

        //
        menu = new JMenu("Search");
        menu.setName("MenuSearch");

        JMenuItem menuStartSearch = new JMenuItem("Start search");
        menuStartSearch.setName("MenuStartSearch");
        menuStartSearch.addActionListener(new StartSearchActionListener());

        JMenuItem menuPreviousMatch = new JMenuItem("Previous match");
        menuPreviousMatch.setName("MenuPreviousMatch");
        menuPreviousMatch.addActionListener(new PreviousMatchActionListener());

        JMenuItem menuNextMatch = new JMenuItem("Next match");
        menuNextMatch.setName("MenuNextMatch");
        menuNextMatch.addActionListener(new nextMatchActionListener());

        JMenuItem menuUseRegExp = new JMenuItem("Use regular expressions");
        menuUseRegExp.setName("MenuUseRegExp");
        menuUseRegExp.addActionListener((a) -> {
            useRegexCheckBox.setSelected(!useRegexCheckBox.isSelected());
        });

        menu.add(menuStartSearch);
        menu.add(menuPreviousMatch);
        menu.add(menuNextMatch);
        menu.add(menuUseRegExp);

        menuBar.add(menu);

        setJMenuBar(menuBar);
    }

    private void createMainField() {
        String resourcePath = "/Users/Dead/IdeaProjects/images/";

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        JButton saveBtn = new JButton(new ImageIcon(resourcePath + "save.png"));
        saveBtn.setName("SaveButton");
        topPanel.add(saveBtn);

        JButton openBtn = new JButton(new ImageIcon(resourcePath + "open.png"));
        openBtn.setName("OpenButton");
        topPanel.add(openBtn);

        searchField = new JTextField();
        searchField.setName("SearchField");
        searchField.setMinimumSize(new Dimension(100, 0));
        topPanel.add(searchField);

        //
        JButton startSearchButton = new JButton(new ImageIcon(resourcePath + "search.png"));
        startSearchButton.setName("StartSearchButton");
        topPanel.add(startSearchButton);

        //
        JButton previousMatchButton = new JButton(new ImageIcon(resourcePath + "previous.png"));
        previousMatchButton.setName("PreviousMatchButton");
        topPanel.add(previousMatchButton);

        //
        JButton nextMatchButton = new JButton(new ImageIcon(resourcePath + "next.png"));
        nextMatchButton.setName("NextMatchButton");
        topPanel.add(nextMatchButton);

        //
        useRegexCheckBox = new JCheckBox("Use regex");
        useRegexCheckBox.setName("UseRegExCheckbox");
        topPanel.add(useRegexCheckBox);
        //

        add(topPanel, BorderLayout.PAGE_START);
        //

        fileChooser = new JFileChooser();
        fileChooser.setName("FileChooser");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        add(fileChooser, BorderLayout.CENTER);

        JPanel textAreaPanel = new JPanel(new BorderLayout());
        textAreaPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        textArea = new JTextArea();
        textArea.setName("TextArea");

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setName("ScrollPane");

        textAreaPanel.add(scrollPane);
        add(textAreaPanel, BorderLayout.CENTER);

        //Add action listeners
        saveBtn.addActionListener(new SaveActionListener());
        openBtn.addActionListener(new OpenActionListener());
        startSearchButton.addActionListener(new StartSearchActionListener());
        previousMatchButton.addActionListener(new PreviousMatchActionListener());
        nextMatchButton.addActionListener(new nextMatchActionListener());
    }

    private class SaveActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            File file;
            int retValue = fileChooser.showSaveDialog(null);
            if (retValue == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
            } else {
                return;
            }

            try {
                Files.writeString(Path.of(file.getPath()), textArea.getText());
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    private class OpenActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            File file;
            int retValue = fileChooser.showOpenDialog(null);
            if (retValue == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
            } else {
                return;
            }

            try {
                textArea.setText(Files.readString(Path.of(file.getPath())));
            } catch (IOException exception) {
                textArea.setText("");
                exception.printStackTrace();
            }
        }
    }

    private class ExitActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            dispose();
        }
    }

    private class StartSearchActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            searchText(0);
        }
    }

    private class PreviousMatchActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            searchText(-1);
        }
    }

    private class nextMatchActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            searchText(1);
        }
    }

    private void searchText(int direction) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                String text;

                int currentDirection = direction;
                int caretPosition = textArea.getCaretPosition();
                String fullText = textArea.getText() + "\n";
                String searchText = searchField.getText();

                for (int i = 0; i < 2; i++) {
                    int indexOfFoundText = currentDirection == 0 ? 0 : caretPosition;
                    //System.out.println("start " + caretPosition + ". Direction " + direction + ". Search " + searchText + ". Cycle " + i);
                    if (currentDirection >= 0) {
                        text = fullText.substring(indexOfFoundText);
                    } else {
                        if (i == 0) {
                            //System.out.println("start1 " + indexOfFoundText);
                            //if (indexOfFoundText == 0) {
                            //  indexOfFoundText = textArea.getText().length();
                            //} else {
                            indexOfFoundText--;
                            //}
                            //System.out.println("start2 " + indexOfFoundText);
                        }

                        text = fullText.substring(0, indexOfFoundText);
                    }

                    int index = -1;
                    String foundText = "";
                    if (useRegexCheckBox.isSelected()) {
                        Pattern pattern = Pattern.compile(searchText);
                        Matcher matcher = pattern.matcher(text);

                        while (matcher.find()) {
                            foundText = matcher.group();
                            index = matcher.start();

                            if (currentDirection >= 0) {
                                break;
                            }
                        }
                    } else {
                        if (currentDirection >= 0) {
                            index = text.indexOf(searchText);
                        } else {
                            index = text.lastIndexOf(searchText);
                        }

                        foundText = searchText;
                    }

                    if (index > -1) {
                        if (currentDirection >= 0) {
                            index += indexOfFoundText;
                        }

                        textArea.setCaretPosition(index + foundText.length());
                        textArea.select(index,index + foundText.length());
                        textArea.grabFocus();

                        //System.out.println("end " + textArea.getCaretPosition() + ". Found " + foundText + ". Direction " + currentDirection + ". Cycle " + i);
                        break;
                    } else if (i == 0 && direction != 0) {
                        if (currentDirection > 0) {
                            currentDirection = 0;
                        } else {
                            caretPosition = fullText.length();
                            currentDirection = -1;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
