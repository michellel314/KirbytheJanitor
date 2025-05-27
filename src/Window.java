import javax.swing.JFrame;
public class Window {
    public Window(){
        JFrame frame = new JFrame("f");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        Panel panel = new Panel();
        frame.add(panel);
        frame.setResizable(false);
        frame.setVisible(true);

    }

}
