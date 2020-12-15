import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

/*
 *     Program: NarrowBridgeSimulation
 *        Plik: LogsPanel.java
 *       Autor: Michał Sieroń
 *        Data: 2020 December
 */

public class LogsPanel extends JScrollPane {
    
    private static final long serialVersionUID = 1L;

    private JTextArea textArea = new JTextArea();
    
    public LogsPanel() {
        textArea.setEditable(false);
        getViewport().add(textArea);

        setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }

    public void append(String str) {
        textArea.append(str);
    }

    public void insert(String str, int i) {
        textArea.insert(str, i);
    }
}
