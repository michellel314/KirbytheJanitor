import javax.swing.JFrame;
public class Window {
    public Window(){
        JFrame frame = new JFrame("f");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
    }
}
