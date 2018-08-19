package org.barrelorgandiscovery.gui.aprint.instrumentchoice.shelf;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;

public class CDShelf extends JPanel {
    private static final double ANIM_SCROLL_DELAY = 450;
    private static final int CD_SIZE = 148;

    private int displayWidth = CD_SIZE;
    private int displayHeight = (int) (CD_SIZE * 2 / 1.12);
    
    private List<Image> avatars = null;
    private List<String> avatarsText = null;
    private String avatarText = null;

    private boolean loadingDone = false;

    private Thread picturesFinder = null;
    private Timer scrollerTimer = null;
    private Timer faderTimer = null;

    private float veilAlphaLevel = 0.0f;
    private float alphaLevel = 0.0f;
    private float textAlphaLevel = 0.0f;
    
    private int avatarIndex = -1;
    private double avatarPosition = 0.0;
    private double avatarSpacing = 0.4;
    private int avatarAmount = 5;

    private double sigma;
    private double rho;
    
    private double exp_multiplier;
    private double exp_member;

    private boolean damaged = true;

    private DrawableAvatar[] drawableAvatars;

    private FocusGrabber focusGrabber;
    private AvatarScroller avatarScroller;
    private MouseAvatarSelector mouseAvatarSelector;
    private CursorChanger cursorChanger;
    private MouseWheelScroller wheelScroller;
    private KeyScroller keyScroller;
    private KeyAvatarSelector keyAvatarSelector;

    private Font avatarFont;
    private CrystalCaseFactory fx;

    public CDShelf() {
        avatarFont = new Font("Dialog", Font.PLAIN, 24);
        fx = CrystalCaseFactory.getInstance();
        
        loadAvatars();

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);
        
        setSigma(0.5);

        addComponentListener(new DamageManager());

        initInputListeners();
        addInputListeners();
    }

    public void setAmount(int amount) {
        if (amount > avatars.size()) {
            throw new IllegalArgumentException("Too many avatars");
        }
        this.avatarAmount = amount;
        repaint();
    }

    private void setPosition(double position) {
        this.avatarPosition = position;
        this.damaged = true;
        repaint();
    }

    public void setSigma(double sigma) {
        this.sigma = sigma;
        this.rho = 1.0;
        computeEquationParts();
        this.rho = computeModifierUnprotected(0.0);
        computeEquationParts();
        this.damaged = true;
        repaint();
    }

    public void setSpacing(double avatarSpacing) {
        if (avatarSpacing < 0.0 || avatarSpacing > 1.0) {
            throw new IllegalArgumentException("Spacing must be < 1.0 and > 0.0");
        }
        this.avatarSpacing = avatarSpacing;
        this.damaged = true;
        repaint();
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(displayWidth * 5, (int) (displayHeight * 3));
    }
    
    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
    
    @Override
    public boolean isOpaque() {
        return false;
    }

    @Override
    public boolean isFocusable() {
        return true;
    }

    @Override
    protected void paintChildren(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        
        Composite oldComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                                                   veilAlphaLevel));
        super.paintChildren(g);
        g2.setComposite(oldComposite);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!isVisible()) {
            return;
        }
        
        super.paintComponent(g);

        if (!loadingDone && faderTimer == null) {
            return;
        }

        Insets insets = getInsets();

        int x = insets.left;
        int y = insets.top;

        int width = getWidth() - insets.left - insets.right;
        int height = getHeight() - insets.top - insets.bottom;

        Graphics2D g2 = (Graphics2D) g;
        
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        
        Composite oldComposite = g2.getComposite();
        
        if (damaged) {
            drawableAvatars = sortAvatarsByDepth(x, y, width, height);
            damaged = false;
        }

        drawAvatars(g2, drawableAvatars);
        
        if (drawableAvatars.length > 0) {
            drawAvatarName(g2);
        }
        
        g2.setComposite(oldComposite);
    }

    private void drawAvatars(Graphics2D g2, DrawableAvatar[] drawableAvatars) {
        for (DrawableAvatar avatar: drawableAvatars) {
            AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
                                                                  (float) avatar.getAlpha());
            g2.setComposite(composite);
            g2.drawImage(avatars.get(avatar.getIndex()),
                         (int) avatar.getX(), (int) avatar.getY(),
                         avatar.getWidth(), avatar.getHeight(), null);
        }
    }

    private DrawableAvatar[] sortAvatarsByDepth(int x, int y,
                                                int width, int height) {
        List<DrawableAvatar> drawables = new LinkedList<DrawableAvatar>();
        for (int i = 0; i < avatars.size(); i++) {
            promoteAvatarToDrawable(drawables,
                                    x, y, width, height, i - avatarIndex);
        }

        DrawableAvatar[] drawableAvatars = new DrawableAvatar[drawables.size()];
        drawableAvatars = drawables.toArray(drawableAvatars);
        Arrays.sort(drawableAvatars);
        return drawableAvatars;
    }

    private void drawAvatarName(Graphics2D g2) {
        Composite composite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                                                   textAlphaLevel));

        FontRenderContext context = g2.getFontRenderContext();
        TextLayout layout = new TextLayout(avatarText, avatarFont, context);
        Rectangle2D bounds = layout.getBounds();

        double bulletWidth = bounds.getWidth() + 12;
        double bulletHeight = bounds.getHeight() + layout.getDescent() + 4;
        
        double x = (getWidth() - bulletWidth) / 2.0;
        double y = (getHeight() + CD_SIZE) / 2.0;

        BufferedImage textImage = new BufferedImage((int) bulletWidth,
                                                    (int) bulletHeight,
                                                    BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2text = textImage.createGraphics();
        g2text.setColor(new Color(0, 0, 0, 170));
        layout.draw(g2text, 6, layout.getAscent() + 1);
        g2text.setColor(Color.WHITE);
        layout.draw(g2text, 6, layout.getAscent());
        g2text.dispose();
        
        g2.drawImage(fx.createReflectedPicture(textImage,
                                               fx.createGradientMask((int) bulletWidth,
                                                                     (int) bulletHeight)),
                                            (int) x, (int) y, null);
        g2.setComposite(composite);
    }

    private void promoteAvatarToDrawable(List<DrawableAvatar> drawables,
                                         int x, int y, int width, int height,
                                         int offset) {

        double spacing = offset * avatarSpacing;
        double avatarPosition = this.avatarPosition + spacing;

        if (avatarIndex + offset < 0 ||
            avatarIndex + offset >= avatars.size()) {
            return;
        }

        Image avatar = avatars.get(avatarIndex + offset);

        int avatarWidth = displayWidth;//avatar.getWidth(null);
        int avatarHeight = displayHeight;//avatar.getHeight(null);

        double result = computeModifier(avatarPosition);
        int newWidth = (int) (avatarWidth * result);
        if (newWidth == 0) {
            return;
        }
        int newHeight = (int) (avatarHeight * result);
        if (newHeight == 0) {
            return;
        }

        double avatar_x = x + (width - newWidth) / 2.0;
        double avatar_y = y + (height - newHeight / 2.0) / 2.0;

        double semiWidth = width / 2.0;
        avatar_x += avatarPosition * semiWidth;

        if (avatar_x >= width || avatar_x < -newWidth) {
            return;
        }
        
        drawables.add(new DrawableAvatar(avatarIndex + offset,
                                         avatar_x, avatar_y,
                                         newWidth, newHeight,
                                         avatarPosition, result));
    }
    
    private void computeEquationParts() {
        exp_multiplier = Math.sqrt(2.0 * Math.PI) / sigma / rho;
        exp_member = 4.0 * sigma * sigma;
    }

    private double computeModifier(double x) {
        double result = computeModifierUnprotected(x);
        if (result > 1.0) {
            result = 1.0;
        } else if (result < -1.0) {
            result = -1.0;
        }
        return result;
    }

    private double computeModifierUnprotected(double x) {
        return exp_multiplier * Math.exp((-x * x) / exp_member);
    }

    private void addInputListeners() {
        addMouseListener(focusGrabber);
        addMouseListener(avatarScroller);
        addMouseListener(mouseAvatarSelector);
        addMouseMotionListener(cursorChanger);
        addMouseWheelListener(wheelScroller);
        addKeyListener(keyScroller);
        addKeyListener(keyAvatarSelector);
    }

    private void initInputListeners() {
        // input listeners are all stateless
        // hence they can be instantiated once
        focusGrabber = new FocusGrabber();
        avatarScroller = new AvatarScroller();
        mouseAvatarSelector = new MouseAvatarSelector();
        cursorChanger = new CursorChanger();
        wheelScroller = new MouseWheelScroller();
        keyScroller = new KeyScroller();
        keyAvatarSelector = new KeyAvatarSelector();
    }
    
//    private void removeInputListeners() {
//        removeMouseListener(focusGrabber);
//        removeMouseListener(avatarScroller);
//        removeMouseListener(mouseAvatarSelector);
//        removeMouseMotionListener(cursorChanger);
//        removeMouseWheelListener(wheelScroller);
//        removeKeyListener(keyScroller);
//        removeKeyListener(keyAvatarSelector);
//    }

    private void loadAvatars() {
        avatars = new ArrayList<Image>();
        avatarsText = new ArrayList<String>();
        
        picturesFinder = new Thread(new PicturesFinderThread());
        picturesFinder.setPriority(Thread.MIN_PRIORITY);
        picturesFinder.start();
    }

    private void setAvatarIndex(int index) {
        avatarIndex = index;
        avatarText = avatarsText.get(index);
    }

    private void scrollBy(int increment) {
        if (loadingDone) {
            setAvatarIndex(avatarIndex + increment);
            if (avatarIndex < 0) {
                setAvatarIndex(0);
            } else if (avatarIndex >= avatars.size()) {
                setAvatarIndex(avatars.size() - 1);
            }
            damaged = true;
            repaint();
        }
    }
    
    private void scrollAndAnimateBy(int increment) {
        if (loadingDone && (scrollerTimer == null || !scrollerTimer.isRunning())) {
            int index = avatarIndex + increment;
            if (index < 0) {
                index = 0;
            } else if (index >= avatars.size()) {
                index = avatars.size() - 1;
            }
            
            DrawableAvatar drawable = null;
            if (drawableAvatars != null) {
                for (DrawableAvatar avatar: drawableAvatars) {
                    if (avatar.index == index) {
                        drawable = avatar;
                        break;
                    }
                }
            }

            if (drawable != null) {
                scrollAndAnimate(drawable);
            }
        }
    }

    private void scrollAndAnimate(DrawableAvatar avatar) {
        if (loadingDone) {
            scrollerTimer = new Timer(33, new AutoScroller(avatar));
            scrollerTimer.start();
        }
    }

    private DrawableAvatar getHitAvatar(int x, int y) {
        for (DrawableAvatar avatar: drawableAvatars) {
            Rectangle hit = new Rectangle((int) avatar.getX(), (int) avatar.getY(),
                                          avatar.getWidth(), avatar.getHeight() / 2);
            if (hit.contains(x, y)) {
                return avatar;
            }
        }
        return null;
    }
    
    private void startFader() {
        faderTimer = new Timer(35, new FaderAction());
        faderTimer.start();
    }

    private class PicturesFinderThread implements Runnable {
        private List<URL> artworks;
        
        public PicturesFinderThread() {
            artworks = new LinkedList<URL>();
            artworks.add(getClass().getResource("/artworks/Black Eyed Peas.jpg"));
            avatarsText.add("Black Eyed Peas");
            artworks.add(getClass().getResource("/artworks/Coldplay.jpg"));
            avatarsText.add("Coldplay");
            artworks.add(getClass().getResource("/artworks/Foo Fighters.jpg"));
            avatarsText.add("Foo Fighters");
            artworks.add(getClass().getResource("/artworks/Gorillaz.jpg"));
            avatarsText.add("Gorillaz");
            artworks.add(getClass().getResource("/artworks/Green Day.jpg"));
            avatarsText.add("Green Day");
            artworks.add(getClass().getResource("/artworks/Moby.jpg"));
            avatarsText.add("Moby");
            artworks.add(getClass().getResource("/artworks/Norah Jones.jpg"));
            avatarsText.add("Norah Jones");
            artworks.add(getClass().getResource("/artworks/Shivaree.jpg"));
            avatarsText.add("Shivaree");
            artworks.add(getClass().getResource("/artworks/Sin City.jpg"));
            avatarsText.add("Sin City");
        }
        
        public void run() {
            int i = 0;
            for (URL url: artworks) {
                try {
                    BufferedImage image = ImageIO.read(url);
                    avatars.add(fx.createReflectedPicture(fx.createCrystalCase(image)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                if (i++ == avatarAmount) {
                    setAvatarIndex(avatarAmount / 2);
                    startFader();
                }
            }

            loadingDone = true;
        }
    }
    
    private class FaderAction implements ActionListener {
        private long start = 0;
        
        private FaderAction() {
            alphaLevel = 0.0f;
            textAlphaLevel = 0.0f;
        }
        
        public void actionPerformed(ActionEvent e) {
            if (start == 0) {
                start = System.currentTimeMillis();
            }

            alphaLevel = (System.currentTimeMillis() - start) / 500.0f;
            textAlphaLevel = alphaLevel;
            if (alphaLevel > 1.0f) {
                alphaLevel = 1.0f;
                textAlphaLevel = 1.0f;
                faderTimer.stop();
            }

            repaint();
        }
    }

    private class DrawableAvatar implements Comparable {
        private int index;
        private double x;
        private double y;
        private int width;
        private int height;
        private double zOrder;
        private double position;

        private DrawableAvatar(int index,
                               double x, double y, int width, int height,
                               double position, double zOrder) {
            this.index = index;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.position = position;
            this.zOrder = zOrder;
        }

        public int compareTo(Object o) {
            double zOrder2 = ((DrawableAvatar) o).zOrder;
            if (zOrder < zOrder2) {
                return -1;
            } else if (zOrder > zOrder2) {
                return 1;
            }
            return 0;
        }
        
        public double getPosition() {
            return position;
        }

        public double getAlpha() {
            return zOrder * alphaLevel;
        }

        public int getHeight() {
            return height;
        }

        public int getIndex() {
            return index;
        }

        public int getWidth() {
            return width;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }
    }
    
    private class MouseWheelScroller implements MouseWheelListener {
        public void mouseWheelMoved(MouseWheelEvent e) {
            int increment = e.getWheelRotation();
            scrollAndAnimateBy(increment);
        }
    }

    private class KeyScroller extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();
            switch (keyCode) {
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_UP:
                    scrollAndAnimateBy(-1);
                    break;
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_DOWN:
                    scrollAndAnimateBy(1);
                    break;
                case KeyEvent.VK_END:
                    scrollBy(avatars.size() - avatarIndex - 1);
                    break;
                case KeyEvent.VK_HOME:
                    scrollBy(-avatarIndex - 1);
                    break;
                case KeyEvent.VK_PAGE_UP:
                    scrollAndAnimateBy(-avatarAmount / 2);
                    break;
                case KeyEvent.VK_PAGE_DOWN:
                    scrollAndAnimateBy(avatarAmount / 2);
                    break;
            }
        }
    }

    private class FocusGrabber extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            requestFocus();
        }
    }
    
    private class AvatarScroller extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if ((scrollerTimer != null && scrollerTimer.isRunning()) ||
                drawableAvatars == null) {
                return;
            }
            
            if (e.getButton() == MouseEvent.BUTTON1) {
                DrawableAvatar avatar = getHitAvatar(e.getX(), e.getY());
                if (avatar != null && avatar.getIndex() != avatarIndex) {
                    scrollAndAnimate(avatar);
                }
            }
        }
    }

    private class DamageManager extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent e) {
            damaged = true;
        }
    }

    private class AutoScroller implements ActionListener {
        private double position;
        private int index;
        private long start;

        private AutoScroller(DrawableAvatar avatar) {
            this.index = avatar.getIndex();
            this.position = avatar.getPosition();
            this.start = System.currentTimeMillis();
        }

        public void actionPerformed(ActionEvent e) {
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed < ANIM_SCROLL_DELAY / 2.0) {
                textAlphaLevel = (float) (1.0 - 2.0 * (elapsed / ANIM_SCROLL_DELAY));
            } else {
                avatarText = avatarsText.get(index);
                textAlphaLevel = (float) (((elapsed / ANIM_SCROLL_DELAY) - 0.5) * 2.0);
                if (textAlphaLevel > 1.0f) {
                    textAlphaLevel = 1.0f;
                }
            }
            if (textAlphaLevel < 0.1f) {
                textAlphaLevel = 0.1f;
            }
            double newPosition = (elapsed / ANIM_SCROLL_DELAY) * -position;

            if (elapsed >= ANIM_SCROLL_DELAY) {
                ((Timer) e.getSource()).stop();
                setAvatarIndex(index);
                setPosition(0.0);
                return;
            }

            setPosition(newPosition);
        }
    }

    private class CursorChanger extends MouseMotionAdapter {
        @Override
        public void mouseMoved(MouseEvent e) {
            if ((scrollerTimer != null && scrollerTimer.isRunning()) ||
                drawableAvatars == null) {
                return;
            }
            
            DrawableAvatar avatar = getHitAvatar(e.getX(), e.getY());
            if (avatar != null) {
                getParent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                getParent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    private class KeyAvatarSelector extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if ((scrollerTimer == null || !scrollerTimer.isRunning()) &&
                drawableAvatars != null) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                }
            }
        }
    }
    
    private class MouseAvatarSelector extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if ((scrollerTimer == null || !scrollerTimer.isRunning()) &&
                drawableAvatars != null) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    DrawableAvatar avatar = getHitAvatar(e.getX(), e.getY());
                    if (avatar != null && avatar.getIndex() == avatarIndex) {
                    }
                }
            }
        }
    }
}