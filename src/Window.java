import javax.swing.JFrame;
public class Window {
    public Window(){
        JFrame frame = new JFrame("f");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
    }
}
