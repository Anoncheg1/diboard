package webspringboot.service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import dibd.util.Log;

/**
 * Captcha Generator.
 */
public class CaptchaGen {
        public static final String ALL_ENGLISH_CHARS_AND_NUMBERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        public static final CaptchaGen DEFAULT = new CaptchaGen(CaptchaGen.ALL_ENGLISH_CHARS_AND_NUMBERS, 150, 50, 30, 4, 0.2, 8, 30, 50, 10, 2);

        private final String str;

        private final int x;
        private final int y;
        private final int size;
        private final int n;
        private final double p;
        private final int v;
        private final int d;
        private final int m;
        private final int l;
        private final int nSize;

        /**
         * construct the Captcha Generator.
         *
         * @param str   select char from the string 从这串字符串中选择字符
         * @param x     captcha X size 验证码X大小
         * @param y     captcha Y size 验证码Y大小
         * @param size  character pen size 文字笔画大小
         * @param n     word number 文字数量
         * @param p     X max changing rate X坐标最大变化率
         * @param v     Y max changing pixels Y坐标最大变化像素
         * @param d     max rotate degree (0 to 360) 最大旋转角
         * @param m     noise number 噪音数量
         * @param l     noise max length(x(+|-)l,y(+|-)l) 噪音最大长度
         * @param nSize noise pen size 噪音笔画大小
         */
        public CaptchaGen(String str, int x, int y, int size, int n, double p, int v, int d, int m, int l, int nSize) {
                this.str = str;
                this.x = x;
                this.y = y;
                this.size = size;
                this.n = n;
                this.p = p;
                this.v = v;
                this.d = d;
                this.m = m;
                this.l = l;
                this.nSize = nSize;
        }

        /**
         * generate captcha.
         *
         * @param sb records the captcha 记录验证码
         * @return generated buffered image 生成的图片
         */
        public BufferedImage generate(StringBuilder sb) {
                BufferedImage img = new BufferedImage(x, y, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = (Graphics2D) img.getGraphics();
                g.setColor(Color.white);
                g.fillRect(1, 1, x - 2, y - 2);

                /*java.util.List<String> badfonts = Arrays.asList(new String[]{ "Droid Sans Fallback","OpenSymbol", "Standard Symbols L"});
                //"MathJax_Script","MathJax_Size2", "MathJax_WinChrome","MathJax_AMS","MathJax_WinIE6", "MathJax_Size4", "MathJax_Caligraphic",
                String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
                //g.setFont(new Font("Arial", Font.BOLD, size));
                int fnum = 0;
                do{
                	fnum = (int) (Math.random() * fonts.length);
                }while(badfonts.contains(fonts[fnum])|| fonts[fnum].startsWith("STIX")||fonts[fnum].startsWith("MathJax"));
                Log.get().finest("Capcha font:"+fnum+" "+fonts[fnum]);
                g.setFont(new Font(fonts[fnum], Font.BOLD, size));*/
                g.setFont(new Font("Arial", Font.BOLD, nSize));

                int inc = x / (n + 2);

                int currentX = inc;
                int Y = (y + size / 2) / 2;
                for (int i = 0; i < n; ++i) {
                        g.setColor(randColor());
                        String s = randStr();
                        sb.append(s);

                        int wX = currentX + (int) (inc / 2 * (Math.random() * p * 2 - p));
                        int wY = Y + (int) (Math.random() * v * 2 - v);

                        g.translate(wX, wY);
                        double rotate = (Math.random() * 2 - 1) * d * Math.PI / 180;
                        g.rotate(rotate);
                        g.drawString(s, 0, 0);
                        currentX += inc;
                        g.rotate(-rotate);
                        g.translate(-wX, -wY);
                }

                //g.setFont(new Font(fonts[fnum], Font.BOLD, size));
                g.setFont(new Font("Arial", Font.BOLD, nSize));
                for (int i = 0; i < m; ++i) {
                        g.setColor(randColor());
                        int nX = (int) (Math.random() * x);
                        int nY = (int) (Math.random() * y);
                        g.drawLine(nX, nY, nX + (int) (Math.random() * l * 2 - l), nY + (int) (Math.random() * l * 2 - l));
                }

                g.setColor(Color.black);
                g.drawRect(0, 0, x, y);

                return img;
        }

        private String randStr() {
                int index = (int) (Math.random() * str.length());
                return Character.toString(str.charAt(index));
        }

        private int rand255() {
                return (int) (Math.random() * 255);
        }

        private Color randColor() {
                return new Color(rand255(), rand255(), rand255());
        }
}