package com.acme.csdn_download.frame;


import com.acme.csdn_download.spider.CrawlingHelper;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Description TODO
 * @Author wangkai
 * @Date 2023/1/29 10:59
 */
@Slf4j
public class MainFrame {

    private static int screenWidth;
    private static int screenHeight;
    private static DefaultListModel<String> listModel = new DefaultListModel<>();
    private static Map<String,String> downloadFileMap = new HashMap<>();
    private static JList<String> listBox;
    private static JPopupMenu jPopupMenu = new JPopupMenu();

    static{

        listBox = new JList<>(listModel);
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();
        screenHeight = screenSize.height;
        screenWidth = screenSize.width;


    }

    public static void main(String[] args) {
        //1.创建一个窗口对象
        JFrame frame = new JFrame("CSDN文章下载器（markdown格式）");
        frame.setFont(new Font(Font.DIALOG_INPUT, 1, 22));
        //取屏幕大小
        frame.setSize(screenWidth/2 , screenHeight/ 2 );
        //置于屏幕中央
        frame.setLocationRelativeTo(null);

        //关闭窗口退出程序
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //创建上下两个容器
        JPanel panelUp = new JPanel();
        JPanel panelDown = new JPanel();

        //容器实现上下布局
        panelUp.setLayout(new FlowLayout(FlowLayout.CENTER,5,15));
        panelDown.setLayout(new BorderLayout(0,0));

        panelUp.setBorder(new LineBorder(Color.lightGray));
        panelDown.setBorder(new LineBorder(Color.lightGray));


        frame.add(panelUp,BorderLayout.NORTH);
        frame.add(panelDown,BorderLayout.CENTER);


        /*
         * 添加组件到容器面板
         */
        placeComponents(panelUp,panelDown);





        frame.setVisible(true);
    }

    private static void placeComponents(JPanel panelUp, JPanel panelDown) {

        // 创建 JLabel
        JLabel label = new JLabel("输入Url:");
        label.setFont(new Font(Font.DIALOG_INPUT, 1, 22));
        /* 这个方法定义了组件的位置。
         * setBounds(x, y, width, height)
         * x 和 y 指定左上角的新位置，由 width 和 height 指定新的大小。
         */
        // userLabel.setBounds(10,20,80,25);
        panelUp.add(label);


        /**
         * 创建文本域
         * MJTextField自定义文本域实现文本框复制粘贴等功能
         */
        final MJTextField textField = new MJTextField(50);
        textField.setBounds(0,0,200,30);

        //文本域增加keyListener，监听回车事件，回车下载文件
        textField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyChar()=='\n'){
                    downFile(textField);
                }
            }
        });
        panelUp.add(textField);


        // 创建下载按钮
        JButton downloadButton = new JButton("下载");
        downloadButton.setFont(new Font(Font.DIALOG_INPUT, 1, 16));

        downloadButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                downFile(textField);
            }

        });
        panelUp.add(downloadButton);

        //创建刷新按钮
        JButton refreshButton = new JButton("刷新列表");
        refreshButton.setFont(new Font(Font.DIALOG_INPUT, 1, 16));

        refreshButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshListModel(listBox);

            }
        });
        panelUp.add(refreshButton);

        // 刷新列表方法
        refreshListModel(listBox);

        //设置列表居中，放大字体等
        DefaultListCellRenderer renderer = new DefaultListCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        renderer.setFont(new Font(Font.DIALOG_INPUT,Font.ITALIC,30));
        listBox.setCellRenderer(renderer);
        listBox.setFont(listBox.getFont().deriveFont(20.0f));


        //列表鼠标事件监听
        listBox.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList)evt.getSource();
                if (evt.getClickCount() == 2) {
                    int size = listModel.size();
                    int index = list.locationToIndex(evt.getPoint());
                    if(size == 1){
                        JOptionPane.showMessageDialog(null, "下载文件为空，请先下载");
                        return;
                    }else if (index == 0){
                        String key = (String)listModel.getElementAt(1);
                        String filePath = downloadFileMap.get(key);
                        try {
                            Desktop.getDesktop().open(new File(filePath.substring(0,filePath.lastIndexOf(File.separator))));
                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }else{
                        try {
                            String key = (String)listModel.getElementAt(index);
                            Desktop.getDesktop().open(new File(downloadFileMap.get(key)));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }




                }
            }
            //鼠标右键选中列表行
            public void mousePressed(MouseEvent e) {
                if(e.getButton()==3 && listBox.getSelectedValuesList().size()==1){
                    int index=listBox.locationToIndex(e.getPoint());
                    listBox.setSelectedIndex(index);
                }
            }

        });

        //删除菜单
        JMenuItem deleteMenuItem = new JMenuItem("删除");
        deleteMenuItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                List<String> selectedValuesList = listBox.getSelectedValuesList();
                for(String key : selectedValuesList){
                    if(key.startsWith("---------双击")){
                        continue;
                    }
                    String filePath = downloadFileMap.get(key);
                    File file = new File(filePath);
                    if(file != null && file.isFile()){
                        file.delete();
                    }
                }
                refreshListModel(listBox);
            }
        });
        jPopupMenu.add(deleteMenuItem);


        jPopupMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String toolTipText = jPopupMenu.getToolTipText(e);
                log.info(toolTipText);
            }
        });
        listBox.add(jPopupMenu);
        listBox.addMouseListener(new myJListListener());
        JScrollPane jsp=new JScrollPane(listBox);
        jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panelDown.add(jsp); // 在顶部面板上添加列表框
    }

    public static void downFile(MJTextField textField){
        String url = textField.getText();
        if(url.length()==0){
            JOptionPane.showMessageDialog(null, "url不能为空");
        }else{
            CrawlingHelper.climbDetailByUrl(url);
            refreshListModel(listBox);
        }
    }

    private static void refreshListModel(JList<String> listBox) {
        listModel.removeAllElements();
        new Thread(()->{
            listModel.addElement("---------双击下列文件名称打开文件，双击此条所在打开文件夹---------");
            downloadFileMap = CrawlingHelper.getDownloadFileMap();
            Iterator<String> iterator = downloadFileMap.keySet().iterator();
            while(iterator.hasNext()){
                listModel.addElement(iterator.next());
            }
            // 创建一个列表框list
            if(listModel.size()>1){
                listBox.setSelectedIndex(1);
            }else{
                listBox.setSelectedIndex(0);
            }
            log.info("刷新成功");
        }).start();

    }

    public  static class myJListListener extends MouseAdapter {
        //e.getButton() 返回值有 1，2，3。1代表鼠标左键，3代表鼠标右键
        //jList.getSelected() 返回的是选中的JList中的项数。
        //if语句的意思也就是，在JList 中点击了右键而且JList选中了某项，显示右键菜单
        //e.getX() , e.getY() 返回的是鼠标目前的位置！也就是在目前鼠标的位置上弹出右键
        public void mouseClicked(MouseEvent e) {
            if(e.getButton() == 3 && listBox.getSelectedIndex() != -1)
                jPopupMenu.show(listBox,e.getX(),e.getY());
        }
    }






}

