package com.example.courier;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 图形界面版入口：Swing + FlatLaf（现代扁平风格外观库）。
 * 数据访问仍然复用 CourierDao，界面只负责显示和交互。
 */
public class MainFrame extends JFrame {

    private static final String[] STATUSES = {"待取件", "运输中", "已签收"};

    private static final Color PRIMARY = new Color(0x25, 0x63, 0xEB);
    private static final Color DANGER = new Color(0xDC, 0x26, 0x26);
    private static final Color PURPLE = new Color(0x7C, 0x3A, 0xED);
    private static final Color GREY = new Color(0x64, 0x74, 0x8B);
    private static final Color PAGE_BG = new Color(0xF3, 0xF4, 0xF6);
    private static final Color TEXT_MUTED = new Color(0x6B, 0x72, 0x80);

    private static final Font FONT = new Font("Microsoft YaHei UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD = new Font("Microsoft YaHei UI", Font.BOLD, 13);

    private final CourierDao dao = new CourierDao();
    private final List<Courier> rows = new ArrayList<>();
    private final DefaultTableModel model;
    private final JTable table;
    private final JTextField searchField = new JTextField();
    private final JLabel statusLabel = new JLabel("就绪");
    private final JLabel subtitleLabel = new JLabel("正在读取数据……");

    public MainFrame() {
        super("快递管理系统");

        model = new DefaultTableModel(
                new Object[]{"ID", "运单号", "寄件人", "寄件人电话", "收件人", "收件人电话", "收件地址", "状态", "录入时间"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xFA, 0xFB, 0xFD));
                }
                return c;
            }
        };

        buildUI();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 640);
        setMinimumSize(new Dimension(960, 540));
        setLocationRelativeTo(null);
        loadAll();
    }

    // ==================== 界面搭建 ====================

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(PAGE_BG);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTableArea(), BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);
        setContentPane(root);
    }

    /** 顶部：标题 + 搜索框 + 功能按钮 */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(18, 24, 14, 24));

        // 左边标题
        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new javax.swing.BoxLayout(titleBox, javax.swing.BoxLayout.Y_AXIS));
        JLabel title = new JLabel("快递管理系统");
        title.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 22));
        title.setForeground(new Color(0x11, 0x18, 0x27));
        subtitleLabel.setFont(FONT);
        subtitleLabel.setForeground(TEXT_MUTED);
        titleBox.add(title);
        titleBox.add(subtitleLabel);

        // 右边搜索
        searchField.setPreferredSize(new Dimension(240, 36));
        searchField.setFont(FONT);
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "输入运单号，回车查询");
        searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        searchField.addActionListener(e -> onSearch());
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    searchField.setText("");
                    loadAll();
                }
            }
        });

        JButton searchBtn = flatButton("查询", PRIMARY);
        searchBtn.addActionListener(e -> onSearch());
        JButton allBtn = flatButton("显示全部", GREY);
        allBtn.addActionListener(e -> {
            searchField.setText("");
            loadAll();
        });

        JPanel searchBox = new JPanel();
        searchBox.setOpaque(false);
        searchBox.add(searchField);
        searchBox.add(searchBtn);
        searchBox.add(allBtn);

        header.add(titleBox, BorderLayout.WEST);
        header.add(searchBox, BorderLayout.EAST);

        // 下面一排操作按钮
        JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(14, 24, 0, 24));

        JButton addBtn = flatButton("＋ 录入快递", PRIMARY);
        addBtn.addActionListener(e -> onAdd());
        JButton statusBtn = flatButton("修改状态", new Color(0x0E, 0xA5, 0xE9));
        statusBtn.addActionListener(e -> onUpdateStatus());
        JButton delBtn = flatButton("删除快递", DANGER);
        delBtn.addActionListener(e -> onDelete());
        JButton statBtn = flatButton("统计", PURPLE);
        statBtn.addActionListener(e -> onStats());
        JButton refreshBtn = flatButton("刷新", GREY);
        refreshBtn.addActionListener(e -> loadAll());

        actions.add(addBtn);
        actions.add(statusBtn);
        actions.add(delBtn);
        actions.add(statBtn);
        actions.add(refreshBtn);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Color.WHITE);
        wrap.add(header, BorderLayout.NORTH);
        wrap.add(actions, BorderLayout.SOUTH);
        return wrap;
    }

    private JScrollPane buildTableArea() {
        table.setFont(FONT);
        table.setRowHeight(36);
        table.setShowVerticalLines(false);
        table.putClientProperty("JTable.showHorizontalLines", true);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(0xF0, 0xF2, 0xF5));
        table.setSelectionBackground(new Color(0xDB, 0xE7, 0xFE));
        table.setSelectionForeground(new Color(0x11, 0x18, 0x27));
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.putClientProperty(FlatClientProperties.STYLE, "rowHeight:36");

        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(FONT_BOLD);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setBackground(new Color(0xF8, 0xFA, 0xFC));

        int[] widths = {45, 108, 74, 128, 74, 128, 250, 78, 140};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i : new int[]{0, 2, 3, 4, 5, 7, 8}) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }
        table.getColumnModel().getColumn(7).setCellRenderer(new StatusRenderer());

        // 双击一行 = 修改状态
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    onUpdateStatus();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(0xE5, 0xE7, 0xEB)),
                new EmptyBorder(0, 0, 0, 0)));
        card.add(scroll, BorderLayout.CENTER);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(PAGE_BG);
        wrap.setBorder(new EmptyBorder(18, 24, 18, 24));
        wrap.add(card, BorderLayout.CENTER);
        return new JScrollPane(wrap) {
            {
                setBorder(null);
                getViewport().setBackground(PAGE_BG);
                setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            }
        };
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0xEE, 0xF1, 0xF5));
        bar.setBorder(new EmptyBorder(8, 24, 8, 24));
        statusLabel.setFont(FONT);
        statusLabel.setForeground(TEXT_MUTED);
        JLabel dbLabel = new JLabel("数据库：courier_db · MySQL");
        dbLabel.setFont(FONT);
        dbLabel.setForeground(TEXT_MUTED);
        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(dbLabel, BorderLayout.EAST);
        return bar;
    }

    private JButton flatButton(String text, Color color) {
        JButton b = new JButton(text);
        b.setFont(FONT);
        b.setForeground(Color.WHITE);
        b.setBackground(color);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(112, 34));
        b.putClientProperty(FlatClientProperties.STYLE, "arc: 8; borderWidth: 0; focusWidth: 0");
        return b;
    }

    // ==================== 业务操作 ====================

    private void loadAll() {
        try {
            fill(dao.findAll());
            updateStatus("共 " + rows.size() + " 条记录");
        } catch (SQLException e) {
            fill(new ArrayList<Courier>());
            error("读取数据失败：" + e.getMessage());
        }
    }

    private void fill(List<Courier> list) {
        rows.clear();
        rows.addAll(list);
        model.setRowCount(0);
        for (Courier c : rows) {
            model.addRow(new Object[]{c.id, c.no, c.sender, c.senderPhone,
                    c.receiver, c.receiverPhone, c.address, c.status, c.createTime});
        }
        subtitleLabel.setText("courier_db  ·  共 " + rows.size() + " 条记录");
        if (rows.isEmpty()) {
            statusLabel.setText("还没有快递记录，点“录入快递”开始吧");
        }
    }

    private void onSearch() {
        String no = searchField.getText().trim();
        if (no.isEmpty()) {
            loadAll();
            return;
        }
        try {
            Courier c = dao.findByNo(no);
            if (c == null) {
                fill(new ArrayList<Courier>());
                info("没有找到运单号为 " + no + " 的快递。");
            } else {
                List<Courier> one = new ArrayList<>();
                one.add(c);
                fill(one);
                updateStatus("找到 1 条记录：" + no);
            }
        } catch (SQLException e) {
            error("查询失败：" + e.getMessage());
        }
    }

    private void onAdd() {
        Courier c = showAddDialog();
        if (c == null) {
            return;
        }
        try {
            dao.add(c);
            loadAll();
            updateStatus("已录入 " + c.no + "，状态：待取件");
        } catch (SQLException e) {
            error("录入失败：" + e.getMessage());
        }
    }

    /** 弹出录入表单，校验通过后返回对象；取消返回 null */
    private Courier showAddDialog() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(6, 4, 4, 4));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(7, 8, 7, 8);
        gc.anchor = GridBagConstraints.WEST;

        String[] labels = {"运单号 *", "寄件人 *", "寄件人电话", "收件人 *", "收件人电话", "收件地址"};
        JTextField[] fields = new JTextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            gc.gridx = 0;
            gc.gridy = i;
            gc.weightx = 0;
            gc.fill = GridBagConstraints.NONE;
            form.add(new JLabel(labels[i]), gc);

            gc.gridx = 1;
            gc.weightx = 1;
            gc.fill = GridBagConstraints.HORIZONTAL;
            fields[i] = new JTextField(20);
            fields[i].setFont(FONT);
            fields[i].putClientProperty(FlatClientProperties.STYLE, "arc: 8");
            form.add(fields[i], gc);
        }

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, form, "录入快递",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result != JOptionPane.OK_OPTION) {
                return null;
            }
            Courier c = new Courier();
            c.no = fields[0].getText().trim();
            c.sender = fields[1].getText().trim();
            c.senderPhone = fields[2].getText().trim();
            c.receiver = fields[3].getText().trim();
            c.receiverPhone = fields[4].getText().trim();
            c.address = fields[5].getText().trim();
            c.status = "待取件";

            if (c.no.isEmpty() || c.sender.isEmpty() || c.receiver.isEmpty()) {
                info("运单号、寄件人、收件人都不能为空。");
                continue;
            }
            try {
                if (dao.findByNo(c.no) != null) {
                    info("运单号 " + c.no + " 已经存在了。");
                    continue;
                }
            } catch (SQLException e) {
                error("查询失败：" + e.getMessage());
                return null;
            }
            return c;
        }
    }

    private void onUpdateStatus() {
        Courier c = selectedCourier();
        if (c == null) {
            info("请先在表格里选中一行。");
            return;
        }
        Object choice = JOptionPane.showInputDialog(this,
                "运单号：" + c.no + "\n当前状态：" + c.status + "\n\n请选择新状态：",
                "修改状态", JOptionPane.QUESTION_MESSAGE, null, STATUSES, c.status);
        if (choice == null) {
            return;
        }
        try {
            dao.updateStatus(c.no, choice.toString());
            loadAll();
            updateStatus(c.no + " 的状态已改为 " + choice);
        } catch (SQLException e) {
            error("修改失败：" + e.getMessage());
        }
    }

    private void onDelete() {
        Courier c = selectedCourier();
        if (c == null) {
            info("请先在表格里选中一行。");
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "确定要删除运单号 " + c.no + " 吗？\n删除后不可恢复。",
                "删除确认", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (dao.delete(c.no)) {
                loadAll();
                updateStatus("已删除 " + c.no);
            } else {
                info("删除失败，记录可能已经不在了。");
            }
        } catch (SQLException e) {
            error("删除失败：" + e.getMessage());
        }
    }

    private void onStats() {
        try {
            Map<String, Integer> map = dao.countByStatus();
            if (map.isEmpty()) {
                info("暂无快递数据。");
                return;
            }
            int total = 0;
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Integer> e : map.entrySet()) {
                sb.append(String.format("%-6s %4d 件%n", e.getKey(), e.getValue()));
                total += e.getValue();
            }
            sb.append("—————————————\n");
            sb.append(String.format("%-6s %4d 件", "总计", total));
            JOptionPane.showMessageDialog(this, sb.toString(), "各状态统计",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            error("统计失败：" + e.getMessage());
        }
    }

    private Courier selectedCourier() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return rows.get(table.convertRowIndexToModel(row));
    }

    // ==================== 小工具 ====================

    private void updateStatus(String text) {
        statusLabel.setText(text);
        subtitleLabel.setText("courier_db  ·  共 " + rows.size() + " 条记录");
    }

    private void info(String message) {
        JOptionPane.showMessageDialog(this, message, "提示", JOptionPane.INFORMATION_MESSAGE);
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(this, message, "出错了", JOptionPane.ERROR_MESSAGE);
    }

    /** 状态列上色：待取件橙色、运输中蓝色、已签收绿色 */
    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                                                       boolean focus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, selected, focus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(FONT_BOLD);
            String s = value == null ? "" : value.toString();
            if (selected) {
                setForeground(new Color(0x11, 0x18, 0x27));
            } else if ("已签收".equals(s)) {
                setForeground(new Color(0x15, 0x80, 0x3D));
            } else if ("运输中".equals(s)) {
                setForeground(new Color(0x1D, 0x4E, 0xD8));
            } else {
                setForeground(new Color(0xC2, 0x4A, 0x0B));
            }
            return c;
        }
    }

    // ==================== 启动 ====================

    public static void main(String[] args) {
        FlatLightLaf.setup();
        UIManager.put("defaultFont", FONT);
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 8);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new MainFrame().setVisible(true);
                } catch (Throwable t) {
                    t.printStackTrace();
                    JOptionPane.showMessageDialog(null, "启动失败：" + t.getMessage(),
                            "出错了", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
